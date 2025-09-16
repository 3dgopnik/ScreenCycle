package com.example.screencycle.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.screencycle.R
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.screencycle.ui.BlockActivity

class CycleService : Service() {
    companion object {
        const val CHANNEL_ID = "cycle_channel"
        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val ACTION_INCREASE_PLAY = "INCREASE_PLAY"
        const val ACTION_DECREASE_PLAY = "DECREASE_PLAY"
        const val ACTION_INCREASE_REST = "INCREASE_REST"
        const val ACTION_DECREASE_REST = "DECREASE_REST"
        const val ACTION_STATE = "com.example.screencycle.STATE"
        const val EXTRA_REST = "rest"
        const val EXTRA_REMAINING = "remaining"
        private const val MIN_MINUTES = 1
        private const val MAX_MINUTES = 180
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val settings by lazy { SettingsRepository(this) }
    private var running = false
    private var inRest = false
    private val stateMutex = Mutex()
    private var currentPhase: PhaseState? = null

    private data class PhaseState(
        val isRest: Boolean,
        var totalMillis: Long,
        var remainingMillis: Long
    )

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1, notification(getString(R.string.service_started)))
        scope.launch { refreshNotification() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startCycle()
            ACTION_STOP -> stopSelf()
            ACTION_INCREASE_PLAY -> adjustDuration(rest = false, deltaMinutes = 1)
            ACTION_DECREASE_PLAY -> adjustDuration(rest = false, deltaMinutes = -1)
            ACTION_INCREASE_REST -> adjustDuration(rest = true, deltaMinutes = 1)
            ACTION_DECREASE_REST -> adjustDuration(rest = true, deltaMinutes = -1)
        }
        return START_STICKY
    }

    private fun startCycle() {
        if (running) return
        running = true
        scope.launch { loop() }
    }

    private suspend fun loop() {
        while (running) {
            inRest = false
            runPhase(false, settings.getGameMinutes())
            if (!running) break
            inRest = true
            runPhase(true, settings.getRestMinutes())
        }
    }

    private suspend fun runPhase(rest: Boolean, minutes: Int) {
        val totalMillis = minutes * 60_000L
        val phase = PhaseState(rest, totalMillis, totalMillis)
        stateMutex.withLock { currentPhase = phase }
        if (rest) {
            showRestScreen()
        }
        while (running) {
            val remaining = stateMutex.withLock { phase.remainingMillis }
            if (remaining <= 0) break
            updateNotif(formatTimerText(rest, remaining))
            sendState(rest, remaining)
            delay(1_000)
            stateMutex.withLock {
                if (phase.remainingMillis > 0) {
                    phase.remainingMillis = (phase.remainingMillis - 1_000L).coerceAtLeast(0L)
                }
            }
        }
        stateMutex.withLock {
            if (currentPhase === phase) {
                currentPhase = null
            }
        }
    }

    private fun sendState(rest: Boolean, remaining: Long) {
        val i = Intent(ACTION_STATE).apply {
            putExtra(EXTRA_REST, rest)
            putExtra(EXTRA_REMAINING, remaining)
            setPackage(packageName)
        }
        sendBroadcast(i)
    }

    private fun notification(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_remove_24,
                getString(R.string.decrease_play),
                actionIntent(ACTION_DECREASE_PLAY, 1)
            )
            .addAction(
                R.drawable.ic_add_24,
                getString(R.string.increase_play),
                actionIntent(ACTION_INCREASE_PLAY, 2)
            )
            .addAction(
                R.drawable.ic_remove_24,
                getString(R.string.decrease_rest),
                actionIntent(ACTION_DECREASE_REST, 3)
            )
            .addAction(
                R.drawable.ic_add_24,
                getString(R.string.increase_rest),
                actionIntent(ACTION_INCREASE_REST, 4)
            )
            .build()

    private fun updateNotif(text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1, notification(text))
    }

    private fun actionIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, CycleService::class.java).apply { this.action = action }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getService(this, requestCode, intent, flags)
    }

    private fun adjustDuration(rest: Boolean, deltaMinutes: Int) {
        scope.launch {
            val current = if (rest) settings.getRestMinutes() else settings.getGameMinutes()
            val updated = (current + deltaMinutes).coerceIn(MIN_MINUTES, MAX_MINUTES)
            if (updated != current) {
                if (rest) {
                    settings.setRestMinutes(updated)
                } else {
                    settings.setGameMinutes(updated)
                }
                updateCurrentPhase(rest, current, updated)
            }
            val playMinutes = if (rest) settings.getGameMinutes() else updated
            val restMinutes = if (rest) updated else settings.getRestMinutes()
            refreshNotification(playMinutes, restMinutes)
        }
    }

    private suspend fun updateCurrentPhase(rest: Boolean, oldMinutes: Int, newMinutes: Int) {
        stateMutex.withLock {
            currentPhase?.takeIf { it.isRest == rest }?.let { phase ->
                val deltaMillis = (newMinutes - oldMinutes) * 60_000L
                phase.totalMillis = newMinutes * 60_000L
                val newRemaining = (phase.remainingMillis + deltaMillis)
                    .coerceIn(0L, phase.totalMillis)
                phase.remainingMillis = newRemaining
            }
        }
    }

    private suspend fun refreshNotification(playMinutes: Int? = null, restMinutes: Int? = null) {
        val snapshot = stateMutex.withLock {
            currentPhase?.let { PhaseStateSnapshot(it.isRest, it.remainingMillis) }
        }
        val text = if (snapshot != null) {
            formatTimerText(snapshot.isRest, snapshot.remainingMillis)
        } else {
            val play = playMinutes ?: settings.getGameMinutes()
            val rest = restMinutes ?: settings.getRestMinutes()
            getString(R.string.notification_duration_summary, play, rest)
        }
        updateNotif(text)
    }

    private fun formatTimerText(rest: Boolean, remaining: Long): String {
        val label = if (rest) getString(R.string.rest) else getString(R.string.play)
        val minutes = remaining / 60_000
        val seconds = (remaining / 1000) % 60
        return getString(R.string.timer_value, label, minutes, seconds)
    }

    private data class PhaseStateSnapshot(val isRest: Boolean, val remainingMillis: Long)

    private suspend fun showRestScreen() {
        withContext(Dispatchers.Main) {
            val intent = Intent(this@CycleService, BlockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    override fun onDestroy() {
        running = false
        scope.cancel()
        currentPhase = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
