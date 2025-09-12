package com.example.screencycle.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.screencycle.R
import com.example.screencycle.core.CycleService

class BlockActivity : AppCompatActivity() {
    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == CycleService.ACTION_STATE) {
                val inRest = intent.getBooleanExtra(CycleService.EXTRA_REST, false)
                if (!inRest) finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_block)
        LocalBroadcastManager.getInstance(this).registerReceiver(
            stateReceiver,
            IntentFilter(CycleService.ACTION_STATE)
        )
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stateReceiver)
        super.onDestroy()
    }
}
