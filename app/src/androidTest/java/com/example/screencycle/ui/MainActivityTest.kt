package com.example.screencycle.ui

import android.app.ActivityManager
import android.os.PowerManager
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.example.screencycle.R
import com.example.screencycle.core.CycleService
import com.example.screencycle.core.Permissions
import com.example.screencycle.core.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.MessageDigest
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking {
            SettingsRepository(context).setPinHash(hash("1234"))
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun launchMainActivity() {
        ActivityScenario.launch(MainActivity::class.java).use {
            enterPin()
        }
    }

    @Test
    fun missingPermissions_showsDialog() {
        mockkObject(Permissions)
        every { Permissions.allGranted(any()) } returns false
        every { Permissions.canDrawOverlays(any()) } returns false
        every { Permissions.hasUsageStats(any()) } returns true
        every { Permissions.isAccessibilityEnabled(any()) } returns true
        val pm = mockk<PowerManager>()
        every { pm.isIgnoringBatteryOptimizations(any()) } returns true

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.powerManagerOverride = pm }
            enterPin()
            onView(withId(R.id.btnStart)).perform(click())
            onView(withText(R.string.missing_overlay)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun batteryOptimization_showsDialog() {
        mockkObject(Permissions)
        every { Permissions.allGranted(any()) } returns true
        every { Permissions.canDrawOverlays(any()) } returns true
        every { Permissions.hasUsageStats(any()) } returns true
        every { Permissions.isAccessibilityEnabled(any()) } returns true

        val pm = mockk<PowerManager>()
        every { pm.isIgnoringBatteryOptimizations(any()) } returns false

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.powerManagerOverride = pm }
            enterPin()
            onView(withId(R.id.btnStart)).perform(click())
            onView(withText(R.string.missing_battery_optimization)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun startStopButton_controlsService() {
        mockkObject(Permissions)
        every { Permissions.allGranted(any()) } returns true
        every { Permissions.canDrawOverlays(any()) } returns true
        every { Permissions.hasUsageStats(any()) } returns true
        every { Permissions.isAccessibilityEnabled(any()) } returns true

        val pm = mockk<PowerManager>()
        every { pm.isIgnoringBatteryOptimizations(any()) } returns true

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { it.powerManagerOverride = pm }
            enterPin()
            onView(withId(R.id.btnStart)).perform(click())
            assertTrue(isServiceRunning())
            onView(withId(R.id.btnStart)).perform(click())
            assertFalse(isServiceRunning())
        }
    }

    private fun enterPin() {
        onView(withId(R.id.etPin)).perform(typeText("1234"), closeSoftKeyboard())
        onView(withId(R.id.btnPinOk)).perform(click())
    }

    private fun isServiceRunning(): Boolean {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val am = context.getSystemService(ActivityManager::class.java)
        val services = am.getRunningServices(Int.MAX_VALUE)
        return services.any { it.service.className == CycleService::class.java.name }
    }

    private fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
