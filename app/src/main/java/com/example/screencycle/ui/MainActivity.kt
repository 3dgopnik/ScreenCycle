package com.example.screencycle.ui

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.textfield.TextInputEditText
import com.example.screencycle.R
import com.example.screencycle.core.CycleService
import com.example.screencycle.core.Permissions
import com.example.screencycle.core.SettingsRepository
import kotlinx.coroutines.launch
import androidx.annotation.VisibleForTesting

class MainActivity : AppCompatActivity() {
    private val settings by lazy { SettingsRepository(this) }
    private lateinit var tvPackageCount: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnStart: Button
    private var cycleRunning = false

    @VisibleForTesting
    var powerManagerOverride: PowerManager? = null

    private var pinVerified = false
    private val pinLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            pinVerified = true
        } else {
            finish()
        }
    }

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == CycleService.ACTION_STATE) {
                val rest = intent.getBooleanExtra(CycleService.EXTRA_REST, false)
                val remaining = intent.getLongExtra(CycleService.EXTRA_REMAINING, 0L)
                val m = remaining / 60_000
                val s = (remaining / 1000) % 60
                val label = if (rest) getString(R.string.rest) else getString(R.string.play)
                tvTimer.text = getString(R.string.timer_value, label, m, s)
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
                btnStart.text = getString(R.string.start)
                cycleRunning = false
            } else {
                val p = etPlay.text?.toString()?.toIntOrNull() ?: 30
                val r = etRest.text?.toString()?.toIntOrNull() ?: 30
                lifecycleScope.launch {
                    settings.setGameMinutes(p)
                    settings.setRestMinutes(r)
                }
                if (ensurePermissions()) {
                    btnStart.text = getString(R.string.working)
                    startForegroundService(Intent(this, CycleService::class.java).setAction(CycleService.ACTION_START))
                    btnStart.text = getString(R.string.stop)
                    cycleRunning = true
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        cycleRunning = isCycleServiceRunning()
        btnStart.text = if (cycleRunning) getString(R.string.stop) else getString(R.string.start)
        LocalBroadcastManager.getInstance(this).registerReceiver(
            stateReceiver,
            IntentFilter(CycleService.ACTION_STATE)
        )
    }

    override fun onResume() {
        super.onResume()
        if (!pinVerified) {
            pinLauncher.launch(Intent(this, PinActivity::class.java))
        } else {
            lifecycleScope.launch {
                val count = settings.getBlockedPackages().size
                tvPackageCount.text = getString(R.string.selected_games_count, count)
            }
        }
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stateReceiver)
        super.onStop()
    }

    private fun ensurePermissions(): Boolean {
        if (!Permissions.allGranted(this)) {
            PermissionsDialogFragment().show(supportFragmentManager, "perm")
            return false
        }
        val pm = powerManagerOverride ?: getSystemService(PowerManager::class.java)
        if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
            try {
                startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:$packageName")
                    )
                )
            } catch (_: Exception) {}
            return false
        }
        return true
    }

    private fun isCycleServiceRunning(): Boolean {
        val am = getSystemService(ActivityManager::class.java) ?: return false
        val services = am.getRunningServices(Int.MAX_VALUE)
        return services.any { it.service.className == CycleService::class.java.name }
    }
}
