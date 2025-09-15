package com.example.screencycle.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.example.screencycle.R
import com.example.screencycle.core.SettingsRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.MessageDigest

@RunWith(AndroidJUnit4::class)
class PinGuardTest {
    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking {
            SettingsRepository(context).setPinHash(hash("1234"))
        }
    }

    @Test
    fun wrongPin_staysOnPinScreen() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.etPin)).perform(typeText("0000"), closeSoftKeyboard())
            onView(withId(R.id.btnPinOk)).perform(click())
            onView(withId(R.id.etPin)).check(matches(isDisplayed()))
        }
    }

    private fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
