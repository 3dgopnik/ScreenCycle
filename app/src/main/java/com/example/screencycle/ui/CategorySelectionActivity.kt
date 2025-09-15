package com.example.screencycle.ui

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.screencycle.R
import com.example.screencycle.core.SettingsRepository
import kotlinx.coroutines.launch

class CategorySelectionActivity : AppCompatActivity() {
    private val settings by lazy { SettingsRepository(this) }
    private lateinit var listView: ListView
    private val categories = listOf(
        ApplicationInfo.CATEGORY_GAME to R.string.category_game,
        ApplicationInfo.CATEGORY_SOCIAL to R.string.category_social
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_selection)
        listView = findViewById(R.id.categories_list)
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        lifecycleScope.launch {
            val selected = settings.getBlockedCategories().toMutableSet()
            val labels = categories.map { getString(it.second) }
            listView.adapter = ArrayAdapter(
                this@CategorySelectionActivity,
                android.R.layout.simple_list_item_multiple_choice,
                labels
            )
            categories.forEachIndexed { index, pair ->
                listView.setItemChecked(index, selected.contains(pair.first.toString()))
            }
            listView.setOnItemClickListener { _, _, position, _ ->
                val id = categories[position].first.toString()
                if (selected.contains(id)) {
                    selected.remove(id)
                } else {
                    selected.add(id)
                }
                lifecycleScope.launch { settings.setBlockedCategories(selected) }
            }
        }
    }
}
