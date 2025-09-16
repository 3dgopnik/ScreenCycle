package com.example.screencycle.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.lifecycleScope
import com.example.screencycle.R
import com.example.screencycle.core.CycleService
import com.example.screencycle.core.SettingsRepository
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class BlockActivity : AppCompatActivity() {
    private val settings by lazy { SettingsRepository(this) }
    private val remainingMillis = MutableStateFlow<Long?>(null)
    private var totalRestMillis: Long = 0L
    private lateinit var messageView: TextView
    private lateinit var timerView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var rootView: View
    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == CycleService.ACTION_STATE) {
                val inRest = intent.getBooleanExtra(CycleService.EXTRA_REST, false)
                if (!inRest) {
                    finish()
                } else {
                    val remaining = intent.getLongExtra(CycleService.EXTRA_REMAINING, 0L)
                    remainingMillis.value = remaining
                }
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
        messageView = findViewById(R.id.tvRestMessage)
        timerView = findViewById(R.id.tvRestTimer)
        progressBar = findViewById(R.id.pbRestProgress)
        rootView = findViewById(R.id.blockRoot)

        lifecycleScope.launch {
            val message = settings.getRestMessage()
            val theme = RestTheme.fromKey(settings.getRestTheme())
            val restMinutes = settings.getRestMinutes()
            totalRestMillis = restMinutes * 60_000L
            messageView.text = message
            val backgroundColor = ContextCompat.getColor(this@BlockActivity, theme.backgroundColor)
            val messageColor = ContextCompat.getColor(this@BlockActivity, theme.messageColor)
            rootView.setBackgroundColor(backgroundColor)
            messageView.setTextColor(messageColor)
            timerView.setTextColor(messageColor)
            val progressTint = ColorStateList.valueOf(messageColor)
            val backgroundTint = ColorStateList.valueOf(
                ColorUtils.setAlphaComponent(messageColor, (0.2f * 255).roundToInt())
            )
            val secondaryTint = ColorStateList.valueOf(
                ColorUtils.setAlphaComponent(messageColor, (0.35f * 255).roundToInt())
            )
            progressBar.progressTintList = progressTint
            progressBar.progressBackgroundTintList = backgroundTint
            progressBar.secondaryProgressTintList = secondaryTint
            progressBar.secondaryProgress = progressBar.max
            progressBar.progress = 0
            timerView.text = formatRemaining(totalRestMillis)
        }

        lifecycleScope.launch {
            remainingMillis
                .filterNotNull()
                .collectLatest { remaining ->
                    updateTimer(remaining)
                }
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

    private fun updateTimer(remaining: Long) {
        val sanitizedRemaining = remaining.coerceAtLeast(0L)
        if (sanitizedRemaining > totalRestMillis) {
            totalRestMillis = sanitizedRemaining
        }
        timerView.text = formatRemaining(sanitizedRemaining)
        val total = totalRestMillis
        if (total <= 0L) {
            progressBar.progress = 0
            return
        }
        val clampedRemaining = sanitizedRemaining.coerceIn(0L, total)
        val consumed = total - clampedRemaining
        val ratio = consumed.toDouble() / total.toDouble()
        progressBar.progress = (ratio * progressBar.max).roundToInt().coerceIn(0, progressBar.max)
    }

    private fun formatRemaining(remaining: Long): String {
        val totalSeconds = (remaining / 1_000L).coerceAtLeast(0L)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
