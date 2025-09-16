package com.example.screencycle.ui

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.PowerManager
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private lateinit var tvCategoryCount: TextView
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
        val btnCategories = findViewById<Button>(R.id.btnCategories)
        val btnRestSettings = findViewById<Button>(R.id.btnRestSettings)
        btnStart = findViewById(R.id.btnStart)
        tvPackageCount = findViewById(R.id.tvPackageCount)
        tvCategoryCount = findViewById(R.id.tvCategoryCount)
        tvTimer = findViewById(R.id.tvTimer)

        lifecycleScope.launch {
            etPlay.setText(settings.getGameMinutes().toString())
            etRest.setText(settings.getRestMinutes().toString())
        }

        btnApps.setOnClickListener {
            startActivity(Intent(this, AppSelectionActivity::class.java))
        }

        btnCategories.setOnClickListener {
            startActivity(Intent(this, CategorySelectionActivity::class.java))
        }

        btnRestSettings.setOnClickListener {
            showRestSettingsDialog()
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

    private fun showRestSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rest_settings, null)
        val messageInput = dialogView.findViewById<TextInputEditText>(R.id.etRestMessage)
        val themeGroup = dialogView.findViewById<RadioGroup>(R.id.rgRestThemes)
        themeGroup.check(R.id.rbRestThemeDefault)

        lifecycleScope.launch {
            val currentMessage = settings.getRestMessage()
            val currentTheme = settings.getRestTheme()
            messageInput.setText(currentMessage)
            val selected = themeGroup.children
                .filterIsInstance<RadioButton>()
                .firstOrNull { it.tag == currentTheme }
            selected?.let { themeGroup.check(it.id) }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.rest_settings_title)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val message = messageInput.text?.toString()?.trim().takeUnless { it.isNullOrEmpty() }
                    ?: getString(R.string.rest_now)
                val checkedId = themeGroup.checkedRadioButtonId
                val selectedTheme = dialogView.findViewById<RadioButton>(checkedId)?.tag as? String
                    ?: RestTheme.DEFAULT.key
                lifecycleScope.launch {
                    settings.setRestMessage(message)
                    settings.setRestTheme(selectedTheme)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onStart() {
        super.onStart()
        cycleRunning = isCycleServiceRunning()
        btnStart.text = if (cycleRunning) getString(R.string.stop) else getString(R.string.start)
        ContextCompat.registerReceiver(
            this,
            stateReceiver,
            IntentFilter(CycleService.ACTION_STATE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onResume() {
        super.onResume()
        if (!pinVerified) {
            pinLauncher.launch(Intent(this, PinActivity::class.java))
        } else {
            lifecycleScope.launch {
                val packages = settings.getBlockedPackages().size
                val categories = settings.getBlockedCategories().size
                tvPackageCount.text = getString(R.string.selected_games_count, packages)
                tvCategoryCount.text = getString(R.string.selected_categories_count, categories)
            }
        }
    }

    override fun onStop() {
        unregisterReceiver(stateReceiver)
        super.onStop()
    }

    private fun ensurePermissions(): Boolean {
        val pm = powerManagerOverride ?: getSystemService(PowerManager::class.java)
        val missing = PermissionsActivity.collectMissingPermissions(this, pm)
        return if (missing.isEmpty()) {
            true
        } else {
            startActivity(Intent(this, PermissionsActivity::class.java))
            false
        }
    }

    private fun isCycleServiceRunning(): Boolean {
        val am = getSystemService(ActivityManager::class.java) ?: return false
        val services = am.getRunningServices(Int.MAX_VALUE)
        return services.any { it.service.className == CycleService::class.java.name }
    }
}
