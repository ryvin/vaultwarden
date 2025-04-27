package com.vw

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
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

            startServer(dataDir, 8087)
            Log.i(TAG, "Vaultwarden server started on port 8087")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native library", e)
            stopSelf()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to start server", e)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        stopServer()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("vw", "Vaultwarden", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
