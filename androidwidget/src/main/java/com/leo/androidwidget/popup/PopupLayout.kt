package com.leo.androidwidget.popup

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.Px
import com.leo.androidwidget.R

class PopupLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {


    private var mDelayMeasureViewId: Int = 0
    private var mMaxHeight = Integer.MAX_VALUE
    private var mMaxWidth = Integer.MAX_VALUE
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        var widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        if (widthSize > mMaxWidth) {
            widthSize = mMaxWidth
        }
        var heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        if (heightSize > mMaxHeight) {
            heightSize = mMaxHeight
        }
        widthMeasureSpec =
            View.MeasureSpec.makeMeasureSpec(widthSize, View.MeasureSpec.getMode(widthMeasureSpec))
        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            heightSize,
            View.MeasureSpec.getMode(heightMeasureSpec)
        )
        if (mDelayMeasureViewId == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else if (orientation == LinearLayout.HORIZONTAL) {
            measureHorizontal(widthMeasureSpec, heightMeasureSpec)
        } else {
            measureVertical(widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun setMaxHeight(@Px maxHeight: Int): PopupLayout {
        mMaxHeight = maxHeight
        return this
    }

    fun setMaxWidth(@Px maxWidth: Int): PopupLayout {
        mMaxWidth = maxWidth
        return this
    }

    private fun measureHorizontal(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        var availableWidth = widthSize - paddingLeft - paddingRight

        var delayMeasureView: View? = null
        var layoutParams: ViewGroup.MarginLayoutParams
        val childCount = childCount
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if (view.id == mDelayMeasureViewId) {
                delayMeasureView = view
            } else if (view.visibility != View.GONE) {
                measureChildWithMargins(
                    view, View.MeasureSpec.makeMeasureSpec(availableWidth, widthMode), 0,
                    heightMeasureSpec, 0
                )
                layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
                availableWidth -= view.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
            }
        }
        if (delayMeasureView == null) {
            throw RuntimeException("PopupLayout_delay_measure_id is invalid!!!")
        }
        if (delayMeasureView.visibility != View.GONE) {
            measureChildWithMargins(
                delayMeasureView,
                View.MeasureSpec.makeMeasureSpec(availableWidth, widthMode), 0, heightMeasureSpec, 0
            )
            layoutParams = delayMeasureView.layoutParams as ViewGroup.MarginLayoutParams
            availableWidth -= delayMeasureView.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
        }
        setMeasuredDimension(
            View.MeasureSpec.makeMeasureSpec(widthSize - availableWidth, widthMode),
            heightMeasureSpec
        )
    }

    private fun measureVertical(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        var availableHeight = heightSize - paddingTop - paddingBottom

        var delayMeasureView: View? = null
        var layoutParams: ViewGroup.MarginLayoutParams
        val childCount = childCount
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if (view.id == mDelayMeasureViewId) {
                delayMeasureView = view
            } else if (view.visibility != View.GONE) {
                measureChildWithMargins(
                    view, widthMeasureSpec, 0,
                    View.MeasureSpec.makeMeasureSpec(availableHeight, heightMode), 0
                )
                layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
                availableHeight -= view.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
            }
        }
        if (delayMeasureView == null) {
            throw RuntimeException("PopupLayout_delay_measure_id is invalid!!!")
        }
        if (delayMeasureView.visibility != View.GONE) {
            measureChildWithMargins(
                delayMeasureView, widthMeasureSpec, 0,
                View.MeasureSpec.makeMeasureSpec(availableHeight, heightMode), 0
            )
            layoutParams = delayMeasureView.layoutParams as ViewGroup.MarginLayoutParams
            availableHeight -= delayMeasureView.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
        }
        setMeasuredDimension(
            widthMeasureSpec,
            View.MeasureSpec.makeMeasureSpec(heightSize - availableHeight, heightMode)
        )
    }

    private fun getDelayMeasureViewIds(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.PopupLayout, defStyleAttr, 0)
        mDelayMeasureViewId = array.getResourceId(R.styleable.PopupLayout_delay_measure_id, 0)
        mMaxHeight = array.getDimensionPixelSize(
            R.styleable.PopupLayout_android_maxHeight,
            Integer.MAX_VALUE
        )
        mMaxWidth =
            array.getDimensionPixelSize(R.styleable.PopupLayout_android_maxWidth, Integer.MAX_VALUE)
        array.recycle()
    }
}