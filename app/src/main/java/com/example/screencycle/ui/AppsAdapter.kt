package com.example.screencycle.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.screencycle.R

class AppsAdapter(
    private val packageManager: PackageManager,
    applications: List<ApplicationInfo>,
    private val selected: MutableSet<String>,
    private val onChanged: (MutableSet<String>) -> Unit
) : RecyclerView.Adapter<AppsAdapter.VH>() {

    private data class AppEntry(val info: ApplicationInfo, val label: String)

    private val allApps: List<AppEntry> = applications
        .map { AppEntry(it, packageManager.getApplicationLabel(it).toString()) }
        .sortedBy { it.label.lowercase() }

    private val filteredApps = allApps.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return VH(v)
    }

    override fun getItemCount() = filteredApps.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = filteredApps[position]
        val packageName = app.info.packageName
        holder.title.text = app.label
        holder.icon.setImageDrawable(packageManager.getApplicationIcon(app.info))
        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = selected.contains(packageName)
        holder.check.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selected.add(packageName) else selected.remove(packageName)
            onChanged(selected)
        }
        holder.itemView.setOnClickListener { holder.check.performClick() }
    }

    fun filter(query: String) {
        val normalized = query.trim().lowercase()
        filteredApps.clear()
        if (normalized.isEmpty()) {
            filteredApps.addAll(allApps)
        } else {
            filteredApps.addAll(allApps.filter { it.label.lowercase().contains(normalized) })
        }
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.icon)
        val title: TextView = v.findViewById(R.id.title)
        val check: CheckBox = v.findViewById(R.id.check)
    }
}
