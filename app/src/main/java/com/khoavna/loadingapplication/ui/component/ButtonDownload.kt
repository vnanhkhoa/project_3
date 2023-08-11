package com.khoavna.loadingapplication.ui.component

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getStringOrThrow
import androidx.core.content.withStyledAttributes
import com.khoavna.loadingapplication.R

class ButtonDownload @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var state = State.DEFAULT
        private set
    private var backgroundLoading = Color.parseColor("#918302")
    private var progressColor = Color.WHITE
    private var backgroundDefault = Color.parseColor("#03A9F4")
    private var textColor = Color.WHITE
    private var text = "Download"
    private var textLoading = "Downloading"
    private val textPaint = TextPaint().apply {
        color = textColor
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.sp_18)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val paintProgress: Paint
    private val oval = RectF()
    private val radiusProgress by lazy {
        resources.getDimensionPixelSize(R.dimen.dp_10)
    }
    private var progress = 0f

    private val progressAnimation by lazy {
        ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 3000
            interpolator = LinearInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            doOnStart { this@ButtonDownload.isEnabled = false }
            doOnEnd { this@ButtonDownload.isEnabled = true }
        }
    }

    init {
        try {
            context.withStyledAttributes(attrs, R.styleable.ButtonDownload) {
                backgroundDefault = getColorOrThrow(R.styleable.ButtonDownload_backgroundButton)
                backgroundLoading = getColorOrThrow(R.styleable.ButtonDownload_loadingBackground)
                progressColor = getColorOrThrow(R.styleable.ButtonDownload_progressColor)
                textColor = getColorOrThrow(R.styleable.ButtonDownload_textColor)
                text = getStringOrThrow(R.styleable.ButtonDownload_text)
                textLoading = getStringOrThrow(R.styleable.ButtonDownload_textLoading)
            }
        } catch (e: Exception) {
            Log.e("ButtonDownload", "${e.message}")
        }

        paintProgress = Paint().apply {
            color = progressColor
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSizeAndState(
            paddingLeft + paddingRight + suggestedMinimumWidth,
            widthMeasureSpec,
            1
        )
        val height = resolveSizeAndState(
            resources.getDimensionPixelSize(R.dimen.dp_60),
            heightMeasureSpec,
            0
        )
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.also {
            when (state) {
                State.DEFAULT -> drawWithStateDefault(it)
                State.LOADING -> drawWithStateLoading(it)
            }
        }
    }

    private fun drawWithStateDefault(canvas: Canvas) {
        canvas.drawColor(backgroundDefault)
        val rectText = Rect().also {
            textPaint.getTextBounds(text.uppercase(), 0, text.length, it)
        }
        canvas.drawText(
            text.uppercase(), width / 2f, height / 2f - rectText.centerY(), textPaint
        )
    }

    private fun drawWithStateLoading(canvas: Canvas) {
        canvas.drawRect(
            0f, 0f, (progress / 360) * width, height.toFloat(), Paint().apply {
                color = backgroundLoading
            }
        )

        canvas.drawRect(
            (progress / 360) * width, 0f, width.toFloat(), height.toFloat(), Paint().apply {
                color = backgroundDefault
            }
        )
        val rectText = Rect().also {
            textPaint.getTextBounds(textLoading.uppercase(), 0, textLoading.length, it)
        }

        canvas.drawText(
            textLoading.uppercase(), width / 2f, height / 2f - rectText.centerY(), textPaint
        )

        oval.set(
            width / 2f + rectText.centerX() + resources.getDimension(R.dimen.dp_5),
            (height / 2) - radiusProgress.toFloat(),
            (width / 2f) + rectText.centerX() + (radiusProgress * 2) + resources.getDimension(R.dimen.dp_5),
            (height / 2) + radiusProgress.toFloat()
        )

        canvas.drawArc(oval, 0f, progress, true, paintProgress)

    }

    fun updateState(state: State) {
        if (this.state == state) return
        this.state = state
        progressAnimation.start()
        invalidate()
    }
}

enum class State {
    DEFAULT, LOADING
}