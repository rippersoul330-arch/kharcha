package com.kharcha.app.service

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Detects a deliberate "shake" from the accelerometer while ignoring gentle
 * movement (walking, a phone jostling in a pocket).
 *
 * How it works:
 *  - Each sensor sample is converted to a g-force magnitude (1.0 = at rest).
 *  - A sample above [shakeThresholdGravity] counts as a "hard" movement.
 *  - We require [requiredShakeCount] hard movements within [shakeWindowMs] to
 *    consider it a real shake (a single bump won't do it).
 *  - After firing, a [cooldownMs] pause prevents one shake triggering repeatedly.
 */
class ShakeDetector(
    private val onShake: () -> Unit,
    private val shakeThresholdGravity: Float = 2.7f,
    private val requiredShakeCount: Int = 3,
    private val shakeWindowMs: Long = 1000L,
    private val cooldownMs: Long = 1500L
) : SensorEventListener {

    private var firstHardMovementMs: Long = 0
    private var hardMovementCount: Int = 0
    private var lastShakeFiredMs: Long = 0

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val gX = event.values[0] / SensorManager.GRAVITY_EARTH
        val gY = event.values[1] / SensorManager.GRAVITY_EARTH
        val gZ = event.values[2] / SensorManager.GRAVITY_EARTH
        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

        if (gForce <= shakeThresholdGravity) return

        val now = System.currentTimeMillis()

        // Still cooling down from the previous shake.
        if (now - lastShakeFiredMs < cooldownMs) return

        // Reset the counting window if it's been too long since the first movement.
        if (firstHardMovementMs == 0L || now - firstHardMovementMs > shakeWindowMs) {
            firstHardMovementMs = now
            hardMovementCount = 0
        }

        hardMovementCount++

        if (hardMovementCount >= requiredShakeCount) {
            lastShakeFiredMs = now
            firstHardMovementMs = 0
            hardMovementCount = 0
            onShake()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for shake detection.
    }
}
