package com.example.screencycle.core

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import com.example.screencycle.ui.BlockActivity

class AppAccessibilityService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val settings by lazy { SettingsRepository(this) }
    private var inRest = false
    private var packages: Set<String> = emptySet()
    private var categories: Set<String> = emptySet()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            val category = runCatching {
                packageManager.getApplicationInfo(pkg, 0).category
            }.getOrNull()
            if (inRest && (packages.contains(pkg) || (category != null && categories.contains(category.toString())))) {
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
        ContextCompat.registerReceiver(
            this,
            stateReceiver,
            IntentFilter(CycleService.ACTION_STATE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        scope.launch {
            packages = settings.getBlockedPackages()
            categories = settings.getBlockedCategories()
        }
    }

    override fun onDestroy() {
        unregisterReceiver(stateReceiver)
        scope.cancel()
        super.onDestroy()
    }
}
