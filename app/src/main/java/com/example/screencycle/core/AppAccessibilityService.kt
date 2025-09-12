package com.example.screencycle.core

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.screencycle.ui.BlockActivity

class AppAccessibilityService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val settings by lazy { SettingsRepository(this) }
    private var inRest = false
    private var packages: Set<String> = emptySet()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            if (inRest && packages.contains(pkg)) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                val intent = Intent(this, BlockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}

    private val stateReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: Intent?) {
            if (intent?.action == CycleService.ACTION_STATE) {
                inRest = intent.getBooleanExtra(CycleService.EXTRA_REST, false)
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
