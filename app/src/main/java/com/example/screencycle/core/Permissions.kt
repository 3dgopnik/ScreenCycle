package com.example.screencycle.core

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

object Permissions {
    fun canDrawOverlays(context: Context): Boolean =
        Settings.canDrawOverlays(context)

    fun hasUsageStats(context: Context): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun isAccessibilityEnabled(context: Context): Boolean {
        val am = context.getSystemService(AccessibilityManager::class.java) ?: return false
        val enabled = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabled.any {
            it.resolveInfo.serviceInfo.packageName == context.packageName &&
                it.resolveInfo.serviceInfo.name == AppAccessibilityService::class.java.name
        }
    }

    fun allGranted(context: Context): Boolean =
        canDrawOverlays(context) && hasUsageStats(context) && isAccessibilityEnabled(context)
}
