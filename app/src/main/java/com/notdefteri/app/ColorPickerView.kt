package com.notdefteri.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val colors = intArrayOf(
        Color.BLACK,
        Color.parseColor("#D32F2F"),
        Color.parseColor("#1976D2"),
        Color.parseColor("#388E3C"),
        Color.parseColor("#F57C00"),
        Color.parseColor("#7B1FA2"),
        Color.parseColor("#00838F"),
        Color.parseColor("#C2185B")
    )

    private var selectedIndex = 0
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.parseColor("#6200EE")
    }

    var onColorSelected: ((Int) -> Unit)? = null

    fun setSelectedColor(color: Int) {
        val idx = colors.indexOf(color)
        if (idx >= 0) {
            selectedIndex = idx
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val circleSize = (width.toFloat()) / colors.size
        val radius = circleSize / 2f - 12f

        for (i in colors.indices) {
            val cx = circleSize * i + circleSize / 2f
            val cy = height / 2f
            circlePaint.color = colors[i]
            canvas.drawCircle(cx, cy, radius, circlePaint)

            if (i == selectedIndex) {
                canvas.drawCircle(cx, cy, radius + 4f, strokePaint)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = 80
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val circleSize = width.toFloat() / colors.size
            val index = (event.x / circleSize).toInt().coerceIn(0, colors.size - 1)
            selectedIndex = index
            onColorSelected?.invoke(colors[index])
            invalidate()
            return true
        }
        return super.onTouchEvent(event)
    }
}
