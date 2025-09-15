package com.example.screencycle.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.screencycle.R
import com.example.screencycle.core.SettingsRepository
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.security.MessageDigest

class PinActivity : AppCompatActivity() {
    private val settings by lazy { SettingsRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        val etPin = findViewById<TextInputEditText>(R.id.etPin)
        val etConfirm = findViewById<TextInputEditText>(R.id.etPinConfirm)
        val btnOk = findViewById<Button>(R.id.btnPinOk)

        lifecycleScope.launch {
            val saved = settings.getPinHash()
            if (saved.isNullOrEmpty()) {
                btnOk.setOnClickListener {
                    val p1 = etPin.text?.toString().orEmpty()
                    val p2 = etConfirm.text?.toString().orEmpty()
                    if (p1.isBlank() || p1 != p2) {
                        etConfirm.error = getString(R.string.pin_mismatch)
                        return@setOnClickListener
                    }
                    lifecycleScope.launch {
                        settings.setPinHash(hash(p1))
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            } else {
                etConfirm.visibility = View.GONE
                btnOk.setOnClickListener {
                    val entered = etPin.text?.toString().orEmpty()
                    if (hash(entered) == saved) {
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        etPin.error = getString(R.string.pin_invalid)
                    }
                }
            }
        }
    }

    private fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}

