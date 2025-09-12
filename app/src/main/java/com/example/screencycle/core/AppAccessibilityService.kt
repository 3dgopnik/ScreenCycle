package com.example.screencycle.core

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class AppAccessibilityService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val settings by lazy { SettingsRepository(this) }
    private var inRest = false
    private var packages: Set<String> = emptySet()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            if (inRest && packages.contains(pkg)) {
                startService(Intent(this, BlockOverlayService::class.java))
            }
        }
    }

    override fun onInterrupt() {}

    private val stateReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: Intent?) {
            if (intent?.action == CycleService.ACTION_STATE) {
                inRest = intent.getBooleanExtra(CycleService.EXTRA_REST, false)
                if (!inRest) stopService(Intent(this@AppAccessibilityService, BlockOverlayService::class.java))
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            stateReceiver,
            android.content.IntentFilter(CycleService.ACTION_STATE)
        )
        scope.launch { packages = settings.getBlockedPackages() }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stateReceiver)
        scope.cancel()
        super.onDestroy()
    }
}
