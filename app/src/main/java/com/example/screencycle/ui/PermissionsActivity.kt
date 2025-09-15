package com.example.screencycle.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.screencycle.R
import com.example.screencycle.core.Permissions

class PermissionsActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private var items: List<MissingPermission> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)
        listView = findViewById(R.id.permissions_list)
        listView.setOnItemClickListener { _, _, position, _ ->
            startActivity(items[position].intent)
        }
    }

    override fun onResume() {
        super.onResume()
        rebuildList()
    }

    private fun rebuildList() {
        val pm = getSystemService(PowerManager::class.java)
        items = collectMissingPermissions(this, pm)
        if (items.isEmpty()) {
            finish()
            return
        }
        val labels = items.map { getString(it.msgRes) }
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)
    }

    data class MissingPermission(val msgRes: Int, val intent: Intent)

    companion object {
        fun collectMissingPermissions(ctx: Context, pm: PowerManager? = ctx.getSystemService(PowerManager::class.java)): List<MissingPermission> {
            val missing = mutableListOf<MissingPermission>()
            if (!Permissions.canDrawOverlays(ctx)) {
                missing += MissingPermission(
                    R.string.missing_overlay,
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}"))
                )
            }
            if (!Permissions.hasUsageStats(ctx)) {
                missing += MissingPermission(
                    R.string.missing_usage_stats,
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                )
            }
            if (!Permissions.isAccessibilityEnabled(ctx)) {
                missing += MissingPermission(
                    R.string.missing_accessibility,
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                )
            }
            if (pm != null && !pm.isIgnoringBatteryOptimizations(ctx.packageName)) {
                missing += MissingPermission(
                    R.string.missing_battery_optimization,
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:${ctx.packageName}")
                    )
                )
            }
            return missing
        }
    }
}

