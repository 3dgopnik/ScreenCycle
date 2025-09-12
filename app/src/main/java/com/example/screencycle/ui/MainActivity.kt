package com.example.screencycle.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.screencycle.R
import com.example.screencycle.core.AppAccessibilityService
import com.example.screencycle.core.CycleService
import com.example.screencycle.core.SettingsRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val settings by lazy { SettingsRepository(this) }
    private lateinit var tvPackageCount: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnStart: Button
    private var cycleRunning = false

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == CycleService.ACTION_STATE) {
                val rest = intent.getBooleanExtra(CycleService.EXTRA_REST, false)
                val remaining = intent.getLongExtra(CycleService.EXTRA_REMAINING, 0L)
                val m = remaining / 60_000
                val s = (remaining / 1000) % 60
                val label = if (rest) "Отдых" else "Игра"
                tvTimer.text = "$label: $m:${"%02d".format(s)}"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etPlay = findViewById<TextInputEditText>(R.id.etPlay)
        val etRest = findViewById<TextInputEditText>(R.id.etRest)
        val btnApps = findViewById<Button>(R.id.btnApps)
        btnStart = findViewById(R.id.btnStart)
        tvPackageCount = findViewById(R.id.tvPackageCount)
        tvTimer = findViewById(R.id.tvTimer)

        lifecycleScope.launch {
            etPlay.setText(settings.getGameMinutes().toString())
            etRest.setText(settings.getRestMinutes().toString())
        }

        btnApps.setOnClickListener {
            startActivity(Intent(this, AppSelectionActivity::class.java))
        }

        btnStart.setOnClickListener {
            if (cycleRunning) {
                stopService(Intent(this, CycleService::class.java))
                btnStart.text = "Start"
                cycleRunning = false
            } else {
                val p = etPlay.text?.toString()?.toIntOrNull() ?: 30
                val r = etRest.text?.toString()?.toIntOrNull() ?: 30
                lifecycleScope.launch {
                    settings.setGameMinutes(p)
                    settings.setRestMinutes(r)
                }
                if (ensurePermissions()) {
                    btnStart.text = "Working..."
                    startForegroundService(Intent(this, CycleService::class.java).setAction(CycleService.ACTION_START))
                    btnStart.text = "Stop"
                    cycleRunning = true
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            stateReceiver,
            IntentFilter(CycleService.ACTION_STATE)
        )
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val count = settings.getBlockedPackages().size
            tvPackageCount.text = "Выбрано игр: $count"
        }
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stateReceiver)
        super.onStop()
    }

    private fun ensurePermissions(): Boolean {
        var ok = true
        if (!Settings.canDrawOverlays(this)) {
            val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(i)
            ok = false
        }
        if (!hasUsageStats()) {
            try { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) } catch (_: Exception) {}
            ok = false
        }
        if (!isAccessibilityEnabled()) {
            MaterialAlertDialogBuilder(this)
                .setMessage("Accessibility service is disabled")
                .setPositiveButton("Enable now") { _, _ ->
                    try { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } catch (_: Exception) {}
                }
                .setNegativeButton("Later", null)
                .show()
            ok = false
        }
        return ok
    }

    private fun hasUsageStats(): Boolean {
        val appOps = getSystemService(AppOpsManager::class.java) ?: return false
        val mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isAccessibilityEnabled(): Boolean {
        val am = getSystemService(AccessibilityManager::class.java) ?: return false
        val enabled = am.enabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabled.any {
            it.resolveInfo.serviceInfo.packageName == packageName &&
                it.resolveInfo.serviceInfo.name == AppAccessibilityService::class.java.name
        }
    }
}
