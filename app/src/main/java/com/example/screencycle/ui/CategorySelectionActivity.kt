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
    private lateinit var categories: List<Pair<Int, Int>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_selection)
        listView = findViewById(R.id.categories_list)
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        categories = buildCategories()

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

    private fun buildCategories(): List<Pair<Int, Int>> {
        val fields = ApplicationInfo::class.java.fields
        return fields.mapNotNull { field ->
            if (field.name.startsWith("CATEGORY_")) {
                val value = field.getInt(null)
                val resName = "category_${field.name.removePrefix("CATEGORY_").lowercase()}"
                val resId = resources.getIdentifier(resName, "string", packageName)
                if (resId != 0) value to resId else null
            } else {
                null
            }
        }.sortedBy { it.first }
    }
}
