package com.leo.androidwidget.popup

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.Px
import com.leo.androidwidget.R

class PopupRootLayout : FrameLayout {


    constructor(context: Context) : super(context) {
        initViewProperty(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initViewProperty(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initViewProperty(context, attrs, defStyleAttr)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initViewProperty(context, attrs, defStyleAttr)
    }

    private var mChildMaxHeight = Integer.MAX_VALUE
    private var mChildMaxWidth = Integer.MAX_VALUE

    override fun measureChildWithMargins(
        child: View, parentWidthMeasureSpec: Int, widthUsed: Int,
        parentHeightMeasureSpec: Int, heightUsed: Int
    ) {
        var widthUsed = widthUsed
        var heightUsed = heightUsed
        val width = View.MeasureSpec.getSize(parentWidthMeasureSpec)
        val height = View.MeasureSpec.getSize(parentHeightMeasureSpec)
        if (width > mChildMaxWidth) {
            widthUsed = width - mChildMaxWidth
        }
        if (height > mChildMaxHeight) {
            heightUsed = height - mChildMaxHeight
        }
        super.measureChildWithMargins(
            child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec,
            heightUsed
        )
    }

    fun setChildMaxHeight(@Px childMaxHeight: Int): PopupRootLayout {
        mChildMaxHeight = childMaxHeight
        return this
    }

    fun setChildMaxWidth(@Px childMaxWidth: Int): PopupRootLayout {
        mChildMaxWidth = childMaxWidth
        return this
    }

    private fun initViewProperty(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val array =
            context.obtainStyledAttributes(attrs, R.styleable.PopupRootLayout, defStyleAttr, 0)
        mChildMaxHeight = array.getDimensionPixelSize(
            R.styleable.PopupRootLayout_maxChildHeight,
            Integer.MAX_VALUE
        )
        mChildMaxWidth = array.getDimensionPixelSize(
            R.styleable.PopupRootLayout_maxChildWidth,
            Integer.MAX_VALUE
        )
        array.recycle()
    }
}