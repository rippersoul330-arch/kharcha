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
import kotlin.math.hypot

/**
 * Owns the floating "+" button that appears over other apps after a shake.
 *
 * Behaviour:
 *  - [showFloatingButton] draws a draggable circular button on top of everything.
 *  - Tapping it opens the quick-entry screen and the button disappears.
 *  - Dragging it onto the ✕ "remove" target at the bottom dismisses it.
 *  - If the user doesn't respond, it auto-hides after [AUTO_HIDE_MS].
 */
class OverlayController(private val context: Context) {

    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var buttonView: View? = null
    private var removeView: View? = null
    private val autoHideRunnable = Runnable { hideAll() }

    fun showFloatingButton() {
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
            buttonView = null
        }
    }

    fun hideAll() {
        mainHandler.removeCallbacks(autoHideRunnable)
        hideRemoveTarget()
        buttonView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {
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

    // ---- Remove ("drag to dismiss") target ----

    private fun showRemoveTarget() {
        if (removeView != null) return
        val view = LayoutInflater.from(context).inflate(R.layout.overlay_remove_target, null)
        try {
            windowManager.addView(view, removeTargetParams())
            removeView = view
        } catch (e: Exception) {
            removeView = null
        }
    }

    private fun hideRemoveTarget() {
        removeView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {
            }
        }
        removeView = null
    }

    /** True if the button's centre is currently over the remove target. */
    private fun isOverRemoveZone(view: View, params: WindowManager.LayoutParams): Boolean {
        val dm = context.resources.displayMetrics
        val size = REMOVE_TARGET_SIZE_DP * dm.density
        val bottomMargin = REMOVE_TARGET_BOTTOM_MARGIN_DP * dm.density
        val targetCenterX = dm.widthPixels / 2f
        val targetCenterY = dm.heightPixels - bottomMargin - size / 2f
        val buttonCenterX = params.x + view.width / 2f
        val buttonCenterY = params.y + view.height / 2f
        val distance = hypot(buttonCenterX - targetCenterX, buttonCenterY - targetCenterY)
        return distance < (size / 2f + 44f * dm.density)
    }

    private fun removeTargetParams(): WindowManager.LayoutParams {
        val dm = context.resources.displayMetrics
        val size = (REMOVE_TARGET_SIZE_DP * dm.density).toInt()
        val bottomMargin = (REMOVE_TARGET_BOTTOM_MARGIN_DP * dm.density).toInt()
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (dm.widthPixels - size) / 2
            y = dm.heightPixels - size - bottomMargin
        }
    }

    private fun overlayType(): Int {
        @Suppress("DEPRECATION")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private fun buildLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType(),
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

    /**
     * Drag to move; drop on the ✕ target to dismiss; a tap (no real movement)
     * opens the quick-entry screen.
     */
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
                    if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                        if (!moved) showRemoveTarget() // first real movement
                        moved = true
                    }
                    params.x = initialX + dx.toInt()
                    params.y = initialY + dy.toInt()
                    try {
                        windowManager.updateViewLayout(view, params)
                    } catch (_: Exception) {
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    when {
                        !moved -> {
                            hideRemoveTarget()
                            v.performClick()
                            onButtonTapped()
                        }
                        isOverRemoveZone(view, params) -> {
                            // Dropped on the ✕ — remove the button.
                            hideAll()
                        }
                        else -> {
                            hideRemoveTarget()
                            resetAutoHide()
                        }
                    }
                    true
                }

                else -> false
            }
        }
    }

    companion object {
        private const val AUTO_HIDE_MS = 5000L
        private const val REMOVE_TARGET_SIZE_DP = 64f
        private const val REMOVE_TARGET_BOTTOM_MARGIN_DP = 48f
    }
}
