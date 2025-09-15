package com.example.screencycle.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.screencycle.R
import kotlinx.coroutines.*

class CycleService : Service() {
    companion object {
        const val CHANNEL_ID = "cycle_channel"
        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val ACTION_STATE = "com.example.screencycle.STATE"
        const val EXTRA_REST = "rest"
        const val EXTRA_REMAINING = "remaining"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val settings by lazy { SettingsRepository(this) }
    private var running = false
    private var inRest = false

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1, notification(getString(R.string.service_started)))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startCycle()
            ACTION_STOP -> stopSelf()
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
        var remaining = minutes * 60_000L
        val label = if (rest) getString(R.string.rest) else getString(R.string.play)
        while (running && remaining > 0) {
            val m = remaining / 60_000
            val s = (remaining / 1000) % 60
            updateNotif(getString(R.string.timer_value, label, m, s))
            sendState(rest, remaining)
            delay(1_000)
            remaining -= 1_000
        }
    }

    private fun sendState(rest: Boolean, remaining: Long) {
        val i = Intent(ACTION_STATE).apply {
            putExtra(EXTRA_REST, rest)
            putExtra(EXTRA_REMAINING, remaining)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(i)
    }

    private fun notification(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .build()

    private fun updateNotif(text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1, notification(text))
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
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
