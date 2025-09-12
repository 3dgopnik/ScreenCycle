package com.example.screencycle.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity
import com.example.screencycle.R
import com.example.screencycle.core.CycleService
import com.example.screencycle.core.SettingsRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val settings by lazy { SettingsRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etPlay = findViewById<TextInputEditText>(R.id.etPlay)
        val etRest = findViewById<TextInputEditText>(R.id.etRest)
        val btnApps = findViewById<Button>(R.id.btnApps)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)

        lifecycleScope.launch {
            etPlay.setText(settings.getGameMinutes().toString())
            etRest.setText(settings.getRestMinutes().toString())
        }

        btnApps.setOnClickListener {
            startActivity(Intent(this, AppSelectionActivity::class.java))
        }

        btnStart.setOnClickListener {
            val p = etPlay.text?.toString()?.toIntOrNull() ?: 30
            val r = etRest.text?.toString()?.toIntOrNull() ?: 30
            lifecycleScope.launch {
                settings.setGameMinutes(p)
                settings.setRestMinutes(r)
            }
            ensurePermissions()
            startForegroundService(Intent(this, CycleService::class.java).setAction(CycleService.ACTION_START))
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, CycleService::class.java))
        }
    }

    private fun ensurePermissions() {
        if (!Settings.canDrawOverlays(this)) {
            val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(i)
        }
        try { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) } catch (_: Exception) {}
        try { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } catch (_: Exception) {}
    }
}
