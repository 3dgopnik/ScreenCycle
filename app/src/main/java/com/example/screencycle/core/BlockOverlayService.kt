package com.example.screencycle.core

import android.app.Service
import android.content.*
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.TextView

class BlockOverlayService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var overlay: View
    private var isShown = false
    private var isRest = false

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == CycleService.ACTION_STATE) {
                isRest = intent.getBooleanExtra(CycleService.EXTRA_REST, false)
                updateOverlay()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        overlay = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null)
        overlay.findViewById<TextView>(android.R.id.text1).text = "Время отдыха. Пожалуйста, сделай перерыв."
        overlay.setOnTouchListener { _, _ -> true }

        registerReceiver(stateReceiver, IntentFilter(CycleService.ACTION_STATE))
    }

    private fun show() {
        if (isShown) return
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        wm.addView(overlay, params)
        isShown = true
    }

    private fun hide() {
        if (isShown) {
            wm.removeView(overlay)
            isShown = false
        }
    }

    private fun updateOverlay() { if (isRest) show() else hide() }

    override fun onDestroy() {
        unregisterReceiver(stateReceiver)
        hide()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
