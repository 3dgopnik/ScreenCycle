package com.example.screencycle.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.screencycle.R
import com.example.screencycle.core.CycleService
import com.example.screencycle.core.SettingsRepository
import kotlinx.coroutines.launch

class BlockActivity : AppCompatActivity() {
    private val settings by lazy { SettingsRepository(this) }
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
        val messageView = findViewById<TextView>(R.id.tvRestMessage)
        val rootView = findViewById<View>(R.id.blockRoot)

        lifecycleScope.launch {
            val message = settings.getRestMessage()
            val theme = RestTheme.fromKey(settings.getRestTheme())
            messageView.text = message
            rootView.setBackgroundColor(ContextCompat.getColor(this@BlockActivity, theme.backgroundColor))
            messageView.setTextColor(ContextCompat.getColor(this@BlockActivity, theme.messageColor))
        }
        ContextCompat.registerReceiver(
            this,
            stateReceiver,
            IntentFilter(CycleService.ACTION_STATE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onDestroy() {
        unregisterReceiver(stateReceiver)
        super.onDestroy()
    }
}
