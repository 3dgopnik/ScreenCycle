package com.example.screencycle.ui

import android.app.Activity
import android.app.ActivityManager
import android.app.Instrumentation
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anything
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.MessageDigest

@RunWith(AndroidJUnit4::class)
class PermissionsAndStartStopTest {
    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking {
            SettingsRepository(context).setPinHash(hash("1234"))
        }
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
        PermissionsActivity.powerManagerOverride = null
        unmockkAll()
    }

    @Test
    fun permissionsActivity_showsMissingPermissionsAndLaunchesSettings() {
        mockkObject(Permissions)
        every { Permissions.canDrawOverlays(any()) } returns false
        every { Permissions.hasUsageStats(any()) } returns false
        every { Permissions.isAccessibilityEnabled(any()) } returns false

        val pm = mockk<PowerManager>()
        every { pm.isIgnoringBatteryOptimizations(any()) } returns false
        PermissionsActivity.powerManagerOverride = pm

        intending(not(isInternal())).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        ActivityScenario.launch(PermissionsActivity::class.java).use {
            onView(withId(R.id.permissions_list)).check(matches(isDisplayed()))
            onView(withText(R.string.missing_overlay)).check(matches(isDisplayed()))
            onView(withText(R.string.missing_usage_stats)).check(matches(isDisplayed()))
            onView(withText(R.string.missing_accessibility)).check(matches(isDisplayed()))
            onView(withText(R.string.missing_battery_optimization)).check(matches(isDisplayed()))

            val packageUri = Uri.parse("package:${InstrumentationRegistry.getInstrumentation().targetContext.packageName}")

            onData(anything()).atPosition(0).perform(click())
            intended(allOf(hasAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), hasData(packageUri)))

            onData(anything()).atPosition(1).perform(click())
            intended(hasAction(Settings.ACTION_USAGE_ACCESS_SETTINGS))

            onData(anything()).atPosition(2).perform(click())
            intended(hasAction(Settings.ACTION_ACCESSIBILITY_SETTINGS))

            onData(anything()).atPosition(3).perform(click())
            intended(allOf(hasAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS), hasData(packageUri)))
        }
    }

    @Test
    fun mainActivity_startStopButtonControlsService() {
        mockkObject(Permissions)
        every { Permissions.canDrawOverlays(any()) } returns true
        every { Permissions.hasUsageStats(any()) } returns true
        every { Permissions.isAccessibilityEnabled(any()) } returns true

        val pm = mockk<PowerManager>()
        every { pm.isIgnoringBatteryOptimizations(any()) } returns true

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.powerManagerOverride = pm
            }
            enterPin()

            onView(withId(R.id.btnStart)).perform(click())
            scenario.onActivity { activity ->
                val am = activity.getSystemService(ActivityManager::class.java)
                val services = am.getRunningServices(Int.MAX_VALUE)
                assertTrue(services.any { it.service.className == CycleService::class.java.name })
            }

            onView(withId(R.id.btnStart)).perform(click())
            scenario.onActivity { activity ->
                val am = activity.getSystemService(ActivityManager::class.java)
                val services = am.getRunningServices(Int.MAX_VALUE)
                assertFalse(services.any { it.service.className == CycleService::class.java.name })
            }
        }
    }

    private fun enterPin() {
        onView(withId(R.id.etPin)).perform(typeText("1234"), closeSoftKeyboard())
        onView(withId(R.id.btnPinOk)).perform(click())
    }

    private fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
