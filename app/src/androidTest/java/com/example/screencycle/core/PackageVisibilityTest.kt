package com.example.screencycle.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PackageVisibilityTest {

    @Test
    fun installedApplicationsListIncludesOtherPackages() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageManager = context.packageManager

        val installedApps = packageManager.getInstalledApplications(0)
        val otherPackages = installedApps.filter { it.packageName != context.packageName }

        assertTrue(
            "QUERY_ALL_PACKAGES permission should expose third-party apps, but only ${installedApps.map { it.packageName }} were visible.",
            otherPackages.isNotEmpty()
        )
    }
}
