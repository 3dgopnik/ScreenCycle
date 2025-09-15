package com.example.screencycle.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.os.PowerManager
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.screencycle.R
import com.example.screencycle.core.Permissions

class PermissionsDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?) =
        buildDialog()

    private fun buildDialog() =
        MaterialAlertDialogBuilder(requireContext()).apply {
            val ctx = requireContext()
            val pm = ctx.getSystemService(PowerManager::class.java)
            val (msg, intent) = when {
                !Permissions.canDrawOverlays(ctx) ->
                    R.string.missing_overlay to Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${ctx.packageName}")
                    )
                !Permissions.hasUsageStats(ctx) ->
                    R.string.missing_usage_stats to Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                !Permissions.isAccessibilityEnabled(ctx) ->
                    R.string.missing_accessibility to Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                pm != null && !pm.isIgnoringBatteryOptimizations(ctx.packageName) ->
                    R.string.missing_battery_optimization to Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:${ctx.packageName}")
                    )
                else -> error("All permissions granted")
            }
            setMessage(msg)
            setPositiveButton(R.string.open_settings) { _, _ -> startActivity(intent) }
            setNegativeButton(android.R.string.cancel, null)
        }.create()
}
