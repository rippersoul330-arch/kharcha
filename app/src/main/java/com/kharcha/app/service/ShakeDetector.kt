package com.kharcha.app.service

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

/**
 * Detects a *deliberate* shake and ignores everyday movement (a pocket jostle,
 * setting the phone down, a single bump).
 *
 * Rather than reacting to one or two hard jolts, it only fires when the phone is
 * genuinely being shaken for a sustained moment: it keeps a short rolling window
 * of recent sensor samples and triggers only when the phone has been "moving"
 * for at least [MIN_WINDOW_NS] with most of those samples above the movement
 * threshold. This is the same sustained-shake style used by apps like Instagram
 * ("shake to report"), so accidental triggers are rare.
 */
class ShakeDetector(
    private val onShake: () -> Unit
) : SensorEventListener {

    private val queue = SampleQueue()
    private var lastShakeNs = 0L

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val accelerating = isAccelerating(event)
        val timestampNs = event.timestamp
        queue.add(timestampNs, accelerating)

        if (queue.isShaking()) {
            // Debounce so one shake doesn't fire repeatedly.
            if (timestampNs - lastShakeNs > COOLDOWN_NS) {
                lastShakeNs = timestampNs
                queue.clear()
                onShake()
            }
        }
    }

    /** True when the total acceleration magnitude is above a firm-shake threshold. */
    private fun isAccelerating(event: SensorEvent): Boolean {
        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]
        val magnitudeSquared = (ax * ax + ay * ay + az * az).toDouble()
        return magnitudeSquared > ACCELERATION_THRESHOLD * ACCELERATION_THRESHOLD
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed.
    }

    /** A short rolling window of samples used to decide if real shaking is happening. */
    private class SampleQueue {
        private val samples = ArrayDeque<Sample>()
        private var acceleratingCount = 0

        fun add(timestampNs: Long, accelerating: Boolean) {
            purgeOlderThan(timestampNs - MAX_WINDOW_NS)
            samples.addLast(Sample(timestampNs, accelerating))
            if (accelerating) acceleratingCount++
        }

        fun isShaking(): Boolean {
            val oldest = samples.firstOrNull() ?: return false
            val newest = samples.lastOrNull() ?: return false
            // Need enough samples spanning a sustained window...
            if (samples.size < MIN_SAMPLES) return false
            if (newest.timestampNs - oldest.timestampNs < MIN_WINDOW_NS) return false
            // ...and at least ~3/4 of them must be "moving".
            return acceleratingCount >= (samples.size * 3) / 4
        }

        fun clear() {
            samples.clear()
            acceleratingCount = 0
        }

        private fun purgeOlderThan(cutoffNs: Long) {
            while (samples.isNotEmpty()) {
                val first = samples.first()
                if (first.timestampNs < cutoffNs) {
                    samples.removeFirst()
                    if (first.accelerating) acceleratingCount--
                } else {
                    break
                }
            }
        }
    }

    private data class Sample(val timestampNs: Long, val accelerating: Boolean)

    companion object {
        // Total acceleration (m/s^2) above which a sample counts as "moving".
        // At rest gravity is ~9.8; ~16 requires a firm, intentional shake.
        private const val ACCELERATION_THRESHOLD = 16f

        // The phone must be shaking continuously for at least this long (~0.35s).
        private const val MIN_WINDOW_NS = 350_000_000L

        // Keep roughly the last 0.6s of samples in the window.
        private const val MAX_WINDOW_NS = 600_000_000L

        // Minimum samples needed before we'll consider it a shake.
        private const val MIN_SAMPLES = 5

        // Ignore further triggers for 1s after a shake fires.
        private const val COOLDOWN_NS = 1_000_000_000L
    }
}
