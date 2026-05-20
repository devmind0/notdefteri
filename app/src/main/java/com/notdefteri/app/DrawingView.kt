package com.notdefteri.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paths = mutableListOf<PathWithPaint>()
    private var currentPath: Path? = null
    private var currentPaint: Paint = createPaint(Color.BLACK, 8f)
    private var backgroundBitmap: Bitmap? = null

    var strokeColor: Int = Color.BLACK
        set(value) {
            field = value
            currentPaint = createPaint(value, currentPaint.strokeWidth)
        }

    var strokeWidth: Float = 8f
        set(value) {
            field = value
            currentPaint = createPaint(currentPaint.color, value)
        }

    private fun createPaint(color: Int, width: Float): Paint {
        return Paint().apply {
            isAntiAlias = true
            this.color = color
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            this.strokeWidth = width
        }
    }

    fun undo() {
        if (paths.isNotEmpty()) {
            paths.removeAt(paths.size - 1)
            invalidate()
        }
    }

    fun clearCanvas() {
        paths.clear()
        backgroundBitmap = null
        invalidate()
    }

    fun loadFromBitmap(bitmap: Bitmap?) {
        if (bitmap == null) return
        paths.clear()
        backgroundBitmap = bitmap
        invalidate()
    }

    fun saveToBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        backgroundBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        for (pwp in paths) {
            canvas.drawPath(pwp.path, pwp.paint)
        }
        return bitmap
    }

    fun hasDrawing(): Boolean = paths.isNotEmpty() || backgroundBitmap != null

    fun isEmpty(): Boolean = paths.isEmpty() && backgroundBitmap == null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        backgroundBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        for (pwp in paths) {
            canvas.drawPath(pwp.path, pwp.paint)
        }
        currentPath?.let {
            canvas.drawPath(it, currentPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath = Path().apply { moveTo(x, y) }
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath?.lineTo(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                currentPath?.let { path ->
                    paths.add(PathWithPaint(path, createPaint(currentPaint.color, currentPaint.strokeWidth)))
                }
                currentPath = null
                invalidate()
            }
        }
        return super.onTouchEvent(event)
    }
}
