package com.kharcha.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kharcha.app.MainActivity
import com.kharcha.app.R
import com.kharcha.app.overlay.OverlayController

/**
 * Always-on foreground service that listens for a shake. When the phone is
 * shaken it shows the floating "+" button over whatever app is on screen.
 *
 * Android requires a persistent notification for any long-running foreground
 * service; that's the "Kharcha is running" notification the user will see.
 */
class ShakeDetectionService : Service() {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var overlayController: OverlayController

    override fun onCreate() {
        super.onCreate()
        overlayController = OverlayController(this)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        shakeDetector = ShakeDetector(onShake = ::onShakeDetected)

        accelerometer?.let {
            sensorManager.registerListener(
                shakeDetector,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        startForegroundWithNotification()
        // Restart if the system kills us (best-effort; OEMs may still stop it).
        return START_STICKY
    }

    private fun onShakeDetected() {
        // Only draw the button if the user granted the overlay permission.
        if (Settings.canDrawOverlays(this)) {
            overlayController.showFloatingButton()
        }
    }

    private fun startForegroundWithNotification() {
        createChannel()

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.service_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = getString(R.string.service_channel_description)
                    setShowBadge(false)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::sensorManager.isInitialized) {
            sensorManager.unregisterListener(shakeDetector)
        }
        if (::overlayController.isInitialized) {
            overlayController.hideAll()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "kharcha_shake_service"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.kharcha.app.action.STOP_SERVICE"

        /** Starts the always-on shake listener. */
        fun start(context: Context) {
            val intent = Intent(context, ShakeDetectionService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        /** Stops the shake listener. */
        fun stop(context: Context) {
            val intent = Intent(context, ShakeDetectionService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
