package com.example.screencycle.ui

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.screencycle.R
import com.example.screencycle.core.SettingsRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AppSelectionActivity : AppCompatActivity() {
    private val settings by lazy { SettingsRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_selection)
        val rv = findViewById<RecyclerView>(R.id.rvApps)
        rv.layoutManager = LinearLayoutManager(this)

        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .sortedBy { pm.getApplicationLabel(it).toString() }

        lifecycleScope.launch {
            val selected = settings.getBlockedPackages().toMutableSet()
            rv.adapter = AppsAdapter(apps, selected) { newSet ->
                lifecycleScope.launch { settings.setBlockedPackages(newSet) }
            }
        }
    }
}
