package com.vw

import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), ServiceConnection {

    private lateinit var statusTextView: TextView
    private lateinit var urlTextView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    private var serverService: ServerService? = null
    private var isBound = false

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
            // Start the service, but UI update will happen in onServiceConnected or via a status query
            startService(Intent(this, ServerService::class.java))
            // Attempt to bind immediately after starting
            bindService()
        }

        stopButton.setOnClickListener {
            stopService(Intent(this, ServerService::class.java))
            // UI update happens via unbind/onServiceDisconnected or next status check
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to the service if it might already be running
        bindService()
    }

    override fun onStop() {
        super.onStop()
        unbindService()
    }

    private fun bindService() {
        if (!isBound) {
            Intent(this, ServerService::class.java).also { intent ->
                bindService(intent, this, Context.BIND_AUTO_CREATE)
            }
        }
    }

    private fun unbindService() {
        if (isBound) {
            unbindService(this)
            isBound = false
            serverService = null // Clear reference
            // Update UI to reflect disconnected state immediately
            updateUiForServiceState(false)
        }
    }

    // --- ServiceConnection Callbacks ---

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as ServerService.LocalBinder
        serverService = binder.getService()
        isBound = true
        updateUiBasedOnServiceStatus() // Update UI with actual status
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        serverService = null
        isBound = false
        updateUiForServiceState(false) // Update UI to stopped state
    }

    // --- UI Update Logic ---

    /** Updates the UI based on the current service status obtained from the binder. */
    private fun updateUiBasedOnServiceStatus() {
        val isRunning = serverService?.isServerRunning() ?: false
        updateUiForServiceState(isRunning)
    }

    /** Updates UI elements (TextViews, Button enabled state) based on whether the service is running. */
    private fun updateUiForServiceState(isRunning: Boolean) {
        statusTextView.text = getString(if (isRunning) R.string.server_status_running else R.string.server_status_stopped)

        if (isRunning) {
            val port = serverService?.getServerPort() ?: "????"
            urlTextView.text = "${getString(R.string.server_url_prefix)}https://127.0.0.1:$port"
        } else {
            urlTextView.text = getString(R.string.server_url_not_running)
        }

        updateButtonStates(isRunning)
    }

    private fun updateButtonStates(isServiceRunning: Boolean) {
        startButton.isEnabled = !isServiceRunning
        stopButton.isEnabled = isServiceRunning
    }
}
