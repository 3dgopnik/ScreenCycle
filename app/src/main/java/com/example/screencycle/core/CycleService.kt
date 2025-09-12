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
import kotlinx.coroutines.*

class CycleService : Service() {
    companion object {
        const val CHANNEL_ID = "cycle_channel"
        const val ACTION_START = "START"
        const val ACTION_STOP = "STOP"
        const val ACTION_STATE = "com.example.screencycle.STATE"
        const val EXTRA_REST = "rest"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var running = false
    private var inRest = false

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1, notification("Запущено"))
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
        Prefs.setRunning(this, true)
        scope.launch { loop() }
    }

    private suspend fun loop() {
        while (running) {
            inRest = false
            sendState(false)
            val playMs = Prefs.getPlayMinutes(this@CycleService) * 60_000L
            updateNotif("Игра: ${Prefs.getPlayMinutes(this@CycleService)} мин")
            delay(playMs)

            inRest = true
            sendState(true)
            val restMs = Prefs.getRestMinutes(this@CycleService) * 60_000L
            updateNotif("Отдых: ${Prefs.getRestMinutes(this@CycleService)} мин")
            delay(restMs)
        }
    }

    private fun sendState(rest: Boolean) {
        val i = Intent(ACTION_STATE).apply { putExtra(EXTRA_REST, rest) }
        sendBroadcast(i)
    }

    private fun notification(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ScreenCycle")
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
            nm.createNotificationChannel(NotificationChannel(CHANNEL_ID, "ScreenCycle", NotificationManager.IMPORTANCE_LOW))
        }
    }

    override fun onDestroy() {
        running = false
        Prefs.setRunning(this, false)
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
