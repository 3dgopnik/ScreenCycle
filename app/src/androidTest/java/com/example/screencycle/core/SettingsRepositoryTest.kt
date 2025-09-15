package com.example.screencycle.core

import android.content.pm.ApplicationInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsRepositoryTest {
    @Test
    fun saveAndLoadCategories() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repo = SettingsRepository(context)
        val categories = setOf(
            ApplicationInfo.CATEGORY_AUDIO.toString(),
            ApplicationInfo.CATEGORY_VIDEO.toString(),
            ApplicationInfo.CATEGORY_PRODUCTIVITY.toString()
        )
        repo.setBlockedCategories(categories)
        val loaded = repo.getBlockedCategories()
        assertEquals(categories, loaded)
    }
}
