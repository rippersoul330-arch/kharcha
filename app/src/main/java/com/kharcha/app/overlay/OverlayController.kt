package com.kharcha.app.overlay

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.kharcha.app.R
import kotlin.math.abs

/**
 * Owns the floating "+" button that appears over other apps after a shake.
 *
 * Behaviour matching the product spec:
 *  - [showFloatingButton] draws a draggable circular button on top of everything.
 *  - Tapping it opens the quick-entry screen and the button disappears.
 *  - If the user doesn't respond, it auto-hides after [AUTO_HIDE_MS]
 *    (covers accidental shakes in a pocket/bag).
 */
class OverlayController(private val context: Context) {

    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var buttonView: View? = null
    private val autoHideRunnable = Runnable { hideAll() }

    fun showFloatingButton() {
        // If it's already showing, just restart the auto-hide timer.
        if (buttonView != null) {
            resetAutoHide()
            return
        }

        val view = LayoutInflater.from(context).inflate(R.layout.overlay_button, null)
        val params = buildLayoutParams()

        attachDragAndTap(view, params)

        try {
            windowManager.addView(view, params)
            buttonView = view
            resetAutoHide()
        } catch (e: Exception) {
            // If the OS refuses (e.g. permission revoked mid-session), just skip.
            buttonView = null
        }
    }

    fun hideAll() {
        mainHandler.removeCallbacks(autoHideRunnable)
        buttonView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {
                // Already removed.
            }
        }
        buttonView = null
    }

    private fun resetAutoHide() {
        mainHandler.removeCallbacks(autoHideRunnable)
        mainHandler.postDelayed(autoHideRunnable, AUTO_HIDE_MS)
    }

    private fun onButtonTapped() {
        hideAll()
        val intent = Intent(context, QuickEntryActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun buildLayoutParams(): WindowManager.LayoutParams {
        @Suppress("DEPRECATION")
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = context.resources.displayMetrics.widthPixels -
                (72 * context.resources.displayMetrics.density).toInt()
            y = (context.resources.displayMetrics.heightPixels * 0.4f).toInt()
        }
    }

    /** Lets the user drag the button around; a tap (no real movement) opens entry. */
    private fun attachDragAndTap(view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var touchStartX = 0f
        var touchStartY = 0f
        var moved = false
        val touchSlop = 12 * context.resources.displayMetrics.density

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchStartX = event.rawX
                    touchStartY = event.rawY
                    moved = false
                    mainHandler.removeCallbacks(autoHideRunnable)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - touchStartX
                    val dy = event.rawY - touchStartY
                    if (abs(dx) > touchSlop || abs(dy) > touchSlop) moved = true
                    params.x = initialX + dx.toInt()
                    params.y = initialY + dy.toInt()
                    try {
                        windowManager.updateViewLayout(view, params)
                    } catch (_: Exception) {
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        v.performClick()
                        onButtonTapped()
                    } else {
                        resetAutoHide()
                    }
                    true
                }

                else -> false
            }
        }
    }

    companion object {
        private const val AUTO_HIDE_MS = 5000L
    }
}
