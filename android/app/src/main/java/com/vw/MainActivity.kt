package com.vw

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var urlTextView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Load the layout

        // Get references to UI elements
        statusTextView = findViewById(R.id.statusTextView)
        urlTextView = findViewById(R.id.urlTextView)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)

        // Set initial button states
        updateButtonStates(isServiceRunning = false) // Assume stopped initially

        // Set button listeners
        startButton.setOnClickListener {
            startService(Intent(this, ServerService::class.java))
            updateButtonStates(isServiceRunning = true)
            // TODO: Update statusTextView and urlTextView when service is confirmed running (M3-2/M3-3)
            statusTextView.text = getString(R.string.server_status_running) // Placeholder
        }

        stopButton.setOnClickListener {
            stopService(Intent(this, ServerService::class.java))
            updateButtonStates(isServiceRunning = false)
            // TODO: Update statusTextView and urlTextView (M3-2/M3-3)
            statusTextView.text = getString(R.string.server_status_stopped)
            urlTextView.text = getString(R.string.server_url_not_running)
        }
    }

    private fun updateButtonStates(isServiceRunning: Boolean) {
        startButton.isEnabled = !isServiceRunning
        stopButton.isEnabled = isServiceRunning
    }
}
