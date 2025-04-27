package com.vw

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

class ServerService : Service() {
    private const val TAG = "VaultwardenService"
    private external fun startServer(dataDir: String, port: Int)
    private external fun stopServer()

    companion object {
        init {
            System.loadLibrary("android_ffi")
        }
    }

    private val idleHandler = Handler(Looper.getMainLooper())
    private val shutdownRunnable = Runnable { Log.i(TAG, "Stopping service due to idle timeout"); stopSelf() }
    private val IDLE_TIMEOUT_MS = 15 * 60 * 1000L // 15 minutes

    private val binder = LocalBinder()
    private var isRunning = false
    private var serverPort: Int = 8087 // Default/initial port

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = Notification.Builder(this, "vw").setContentTitle("Vaultwarden running")
            .setSmallIcon(android.R.drawable.stat_notify_sync).
            build()
        startForeground(1, notification)
        // Start embedded server and log outcome
        try {
            // Get app-specific directory for data (certs, db, etc.)
            val dataDir = filesDir.absolutePath
            Log.d(TAG, "Using data directory: $dataDir")

            val portToUse = 8087 // Could make this configurable later
            serverPort = portToUse // Store the actual port used
            startServer(dataDir, portToUse)
            Log.i(TAG, "Vaultwarden server started on port $portToUse")
            isRunning = true
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native library", e)
            isRunning = false
            stopSelf()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to start server", e)
            isRunning = false
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Reset/start the idle timer whenever the service is started/restarted
        idleHandler.removeCallbacks(shutdownRunnable) // Remove any existing callbacks
        idleHandler.postDelayed(shutdownRunnable, IDLE_TIMEOUT_MS) // Schedule new shutdown
        Log.d(TAG, "Idle shutdown timer started/reset ($IDLE_TIMEOUT_MS ms)")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed, cancelling idle timer and stopping server.")
        isRunning = false
        idleHandler.removeCallbacks(shutdownRunnable) // Ensure timer is cancelled
        stopServer()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    /** Binder for clients to interact with the service. */
    inner class LocalBinder : Binder() {
        fun getService(): ServerService = this@ServerService
    }

    /** Method for clients to query server status. */
    fun isServerRunning(): Boolean {
        // TODO: Check if the underlying Rust thread is actually alive?
        // For now, rely on the flag set during start/stop attempts.
        return isRunning
    }

    /** Method for clients to reset the idle shutdown timer. */
    fun resetIdleTimer() {
        if (isRunning) { // Only reset if the server is actually running
            idleHandler.removeCallbacks(shutdownRunnable)
            idleHandler.postDelayed(shutdownRunnable, IDLE_TIMEOUT_MS)
            Log.d(TAG, "Idle timer reset by client interaction.")
        }
    }

    /** Method for clients to get the server port. */
    fun getServerPort(): Int {
        return serverPort
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("vw", "Vaultwarden", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
