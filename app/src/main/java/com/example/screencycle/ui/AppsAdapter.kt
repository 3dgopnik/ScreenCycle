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
    private val apps: List<ApplicationInfo>,
    private val selected: MutableSet<String>,
    private val onChanged: (MutableSet<String>) -> Unit
) : RecyclerView.Adapter<AppsAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return VH(v)
    }

    override fun getItemCount() = apps.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = apps[position]
        val pm: PackageManager = holder.itemView.context.packageManager
        holder.title.text = pm.getApplicationLabel(app)
        holder.icon.setImageDrawable(pm.getApplicationIcon(app))
        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = selected.contains(app.packageName)
        holder.check.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selected.add(app.packageName) else selected.remove(app.packageName)
            onChanged(selected)
        }
        holder.itemView.setOnClickListener { holder.check.performClick() }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.icon)
        val title: TextView = v.findViewById(R.id.title)
        val check: CheckBox = v.findViewById(R.id.check)
    }
}
