package com.leo.androidwidget.popup

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import androidx.annotation.IdRes
import androidx.annotation.IntRange
import androidx.annotation.Px
import androidx.annotation.UiThread
import com.leo.androidwidget.AndroidWidget
import com.leo.androidwidget.Utils

open class Popup protected constructor(val mBuilder: Builder) {

    protected val mAutoDismiss: Runnable
    protected val mRootLayout: PopupRootLayout
    protected val mOnKeyListener: View.OnKeyListener

    protected lateinit var mPopupView: View
    protected var mShowing: Boolean = false
    protected var mCanceled: Boolean = false


    companion object {
        private val FOCUSABLE_VIEW_LIST = ArrayList<View>()
        fun isPermanentPopup(popup: Popup): Boolean {
            return !popup.mBuilder.mCanceledOnTouchOutside && popup.mBuilder.mPenetrateOutsideTouchEvent
        }

        /**
         * 焦点的自动分发有时失效，可以调用此方法，相应的记得调用 [.removeFocusableView] 方法
         */
        fun addFocusableView(view: View) {
            if (!FOCUSABLE_VIEW_LIST.contains(view)) {
                FOCUSABLE_VIEW_LIST.add(view)
            }
        }

        fun removeFocusableView(view: View) {
            FOCUSABLE_VIEW_LIST.remove(view)
        }
    }

    init {
        mAutoDismiss = Runnable { dismiss(PopupInterface.CLOSE_TYPE_AUTO) }
        mRootLayout = PopupRootLayout(mBuilder.mActivity)
        mRootLayout.setChildMaxHeight(mBuilder.mMaxHeight).setChildMaxWidth(mBuilder.mMaxWidth)
            .setPadding(0, mBuilder.mTopPadding, 0, mBuilder.mBottomPadding)
        mRootLayout.background = mBuilder.mBackground
        mOnKeyListener = View.OnKeyListener { _, keyCode, event ->
            if (!mBuilder.mCancelable) {
                true
            } else {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_DOWN
                    && isShowing()
                ) {
                    cancelPopup(PopupInterface.CLOSE_TYPE_BACK)
                    true
                } else {
                    false
                }
            }
        }
    }


    fun getContext(): Activity {
        return mBuilder.mActivity
    }

    /**
     * 所有弹窗都可能会排队，而不是马上展示。
     * 当弹窗真正展示的时候，getPopupView 才有意义，需要在 onShowPopup 或者
     * [PopupInterface.OnVisibilityListener.onShow] 调用。
     *
     * @return popup view.
     */
    fun getPopupView(): View? {
        return mPopupView
    }

    fun getTag(): Any? {
        return mBuilder.mTag
    }

    fun getPopupType(): String {
        return mBuilder.mPopupType
    }

    fun getExcluded(): PopupInterface.Excluded {
        return mBuilder.mExcluded
    }

    fun isShowing(): Boolean {
        return mShowing
    }

    /**
     * 展示弹窗，不一定马上展示，可能排队.
     *
     * @param <T>
    </T> */
    @UiThread
    fun show(): Popup {
        checkLegality()
        if (mBuilder.mActivity.isFinishing) {
            discard()
            return this
        }
        if (isShowing()) {
            return this
        }
        if (getPopupManager().enableShowNow(mBuilder.mActivity, this)) {
            createPopup()
        } else {
            getPopupManager().onPopupPending(mBuilder.mActivity, this)
            if (mBuilder.mOnVisibilityListener != null) {
                mBuilder.mOnVisibilityListener!!.onPending(this)
            }
        }
        return this
    }

    /**
     * 关闭弹窗.
     * 和 show 的语义对称，show 展示或者入队，dismiss 消失或者从队列移除.
     *
     * @param dismissType
     */
    @UiThread
    fun dismiss(dismissType: Int) {
        if (!isShowing()) {
            discard()
            return
        }
        if (!Utils.isMainThread()) {
            throw RuntimeException("Must be called on the main thread!!!")
        }
        dismissPopup(dismissType)
    }

    fun discard() {
        if (isShowing()) {
            return
        }
        getPopupManager().onPopupDiscard(mBuilder.mActivity, this)
        if (mBuilder.mOnVisibilityListener != null) {
            mBuilder.mOnVisibilityListener!!.onDiscard(this)
        }
    }

    fun setCancelable(cancelable: Boolean) {
        mBuilder.mCancelable = cancelable
    }

    fun setCanceledOnTouchOutside(cancelable: Boolean) {
        if (cancelable && !mBuilder.mCancelable) {
            mBuilder.mCancelable = true
        }
        mBuilder.mCanceledOnTouchOutside = cancelable
    }

    /**
     * 如果有弹窗外的view抢占了焦点，需调用此方法，否则无法拦截back事件
     */
    fun interceptBackEvent(view: View) {
        if (view is ViewGroup) {
            setKeyListener(view)
        } else {
            view.setOnKeyListener(mOnKeyListener)
        }
    }

    protected open fun onShowPopup(bundle: Bundle?) {}

    protected open fun onDismissPopup(bundle: Bundle?) {}

    protected fun cancelPopup(cancelType: Int) {
        dismiss(cancelType)
        if (mBuilder.mOnCancelListener == null || mCanceled) {
            return
        }
        mCanceled = true
        mBuilder.mOnCancelListener!!.onCancel(this, cancelType)
    }

    protected fun <T : View> findViewById(@IdRes id: Int): T? {
        return mPopupView.findViewById(id)
    }

    private fun checkLegality() {
        require(!(mBuilder.mOnViewStateCallback == null)) { "mBuilder.mActivity and mBuilder.mOnViewStateCallback cannot be null!!!" }
        if (!Utils.isMainThread()) {
            throw RuntimeException("Must be called on the main thread!!!")
        }
    }

    private fun createPopup() {
        mShowing = true
        mCanceled = false
        mPopupView = mBuilder.mOnViewStateCallback!!.onCreateView(
            this,
            LayoutInflater.from(mBuilder.mActivity), mRootLayout, mBuilder.mBundle
        )
        if (mPopupView === mRootLayout) {
            if (mRootLayout.childCount !== 1) {
                throw RuntimeException("mRootLayout has one and only one child View!!!")
            }
            mPopupView = mRootLayout.getChildAt(0)
        } else {
            mRootLayout.addView(mPopupView)
        }

        if (!mBuilder.mIsAddToWindow) {
            (mBuilder.mActivity.window.decorView as ViewGroup).addView(
                mRootLayout,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(mBuilder.mActivity.window.attributes)
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION
            layoutParams.flags =
                layoutParams.flags or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            layoutParams.format = PixelFormat.TRANSLUCENT
            layoutParams.gravity = Gravity.CENTER
            mBuilder.mActivity.windowManager.addView(mRootLayout, layoutParams)
        }

        FOCUSABLE_VIEW_LIST.add(mRootLayout)
        getPopupManager().onPopupShow(mBuilder.mActivity, this)
        onShowPopup(mBuilder.mBundle)
        if (mBuilder.mOnVisibilityListener != null) {
            mBuilder.mOnVisibilityListener!!.onShow(this)
        }
        observerViewProperty()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun observerViewProperty() {
        mPopupView.viewTreeObserver
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mPopupView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    if (mBuilder.mInAnimatorCallback != null) {
                        mBuilder.mInAnimatorCallback!!.onStartAnimator(mPopupView,
                            object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    autoDismiss()
                                }
                            })
                    } else {
                        autoDismiss()
                    }
                }
            })
        mRootLayout.setOnTouchListener { _, event ->
            if (isPermanentPopup(this@Popup)) {
                mBuilder.mActivity.dispatchTouchEvent(event)
                false
            } else if (!mBuilder.mCancelable || !mBuilder.mCanceledOnTouchOutside) {
                true
            } else if (event.action == MotionEvent.ACTION_DOWN) {
                cancelPopup(PopupInterface.CLOSE_TYPE_OUTSIDE)
                !mBuilder.mPenetrateOutsideTouchEvent
            } else {
                false
            }
        }
        mRootLayout.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}

            override fun onViewDetachedFromWindow(v: View) {
                if (isShowing()) {
                    dismiss(PopupInterface.CLOSE_TYPE_AUTO)
                }
            }
        })
        mRootLayout.setFocusable(true)
        mRootLayout.setFocusableInTouchMode(true)
        mRootLayout.requestFocus()
        setKeyListener(mRootLayout)
    }

    private fun setKeyListener(viewGroup: ViewGroup) {
        viewGroup.setOnKeyListener(mOnKeyListener)
        val childCount = viewGroup.childCount
        for (i in 0 until childCount) {
            val view = viewGroup.getChildAt(i)
            if (view is ViewGroup) {
                setKeyListener(view)
            } else {
                view.setOnKeyListener(mOnKeyListener)
            }
        }
    }

    private fun autoDismiss() {
        if (mBuilder.mShowDuration > 0) {
            mPopupView.postDelayed(mAutoDismiss, mBuilder.mShowDuration)
        }
    }

    private fun dismissPopup(dismissType: Int) {
        mShowing = false
        getPopupManager().onPopupDismiss(mBuilder.mActivity, this)
        mPopupView.removeCallbacks(mAutoDismiss)
        if (mBuilder.mOutAnimatorCallback != null) {
            mBuilder.mOutAnimatorCallback!!.onStartAnimator(
                mPopupView,
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        removeView(dismissType)
                    }
                })
        } else {
            removeView(dismissType)
        }
    }

    private fun removeView(dismissType: Int) {
        if (mBuilder.mOnVisibilityListener != null) {
            mBuilder.mOnVisibilityListener!!.onDismiss(this, dismissType)
        }
        onDismissPopup(mBuilder.mBundle)
        mBuilder.mOnViewStateCallback?.onDestroyView(this)
        if (!mBuilder.mIsAddToWindow) {
            val parent = mRootLayout.parent
            if (parent is ViewGroup) {
                parent.removeView(mRootLayout)
            }
        } else {
            try {
                mBuilder.mActivity.windowManager.removeViewImmediate(mRootLayout)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        FOCUSABLE_VIEW_LIST.remove(mRootLayout)
        if (FOCUSABLE_VIEW_LIST.isNotEmpty()) {
            FOCUSABLE_VIEW_LIST[FOCUSABLE_VIEW_LIST.size - 1].requestFocus()
        }
    }

    private fun getPopupManager(): PopupInterface.PopupManager {
        return AndroidWidget.getPopupManager()
    }

    fun isAdded(): Boolean {
        return mRootLayout.parent != null
    }


    open class Builder(val mActivity: Activity) {

        var mCancelable = true
        var mCanceledOnTouchOutside = true
        var mPenetrateOutsideTouchEvent: Boolean = false
        var mIsAddToWindow: Boolean = false
        var mShowDuration = -1L

        var mMaxHeight = Integer.MAX_VALUE
        var mMaxWidth = Integer.MAX_VALUE
        var mTopPadding: Int = 0
        var mBottomPadding: Int = 0

        var mBackground: Drawable? = null
        var mBundle: Bundle? = null
        var mTag: Any? = null

        var mPopupType = PopupInterface.POPUP_TYPE_POPUP
        var mExcluded = PopupInterface.Excluded.NOT_AGAINST
        var mOnViewStateCallback: PopupInterface.OnViewStateCallback? = null
        var mOnVisibilityListener: PopupInterface.OnVisibilityListener? = null
        var mOnCancelListener: PopupInterface.OnCancelListener? = null
        var mInAnimatorCallback: PopupInterface.OnAnimatorCallback? = null
        var mOutAnimatorCallback: PopupInterface.OnAnimatorCallback? = null

        init {
            mTopPadding = Utils.getStatusBarHeight(mActivity)
            if (!Utils.isLandscape()) {
                mBottomPadding = Utils.getNavigationBarHeight(mActivity)
            }
        }

        open fun build(): Popup {
            return Popup(this)
        }

        @UiThread
        fun show(listener: PopupInterface.OnVisibilityListener): Popup {
            val popup = build()
            popup.mBuilder.mOnVisibilityListener = listener
            return popup.show()
        }

        /**
         * 必须设置,且必须在回调中返回有效的View
         */
        fun setOnViewStateCallback(
            onViewStateCallback: PopupInterface.OnViewStateCallback
        ): Builder {
            mOnViewStateCallback = onViewStateCallback
            return this
        }

        /**
         * 默认值：true
         * true：可back键销毁弹窗，是否可点击外部区域销毁弹窗需要看 [.mCanceledOnTouchOutside]
         * false：不可back键销毁弹窗，不可点击外部区域销毁弹窗
         */
        fun setCancelable(cancelable: Boolean): Builder {
            mCancelable = cancelable
            return this
        }

        /**
         * 默认值：true
         * true：可点击外部区域销毁弹窗,前提是 [.mCancelable] 必须为true
         * false：不可点击外部区域销毁弹窗
         */
        fun setCanceledOnTouchOutside(canceledOnTouchOutside: Boolean): Builder {
            mCanceledOnTouchOutside = canceledOnTouchOutside
            return this
        }

        /**
         * 展示时间
         * 默认值：-1L,表示必须由用户手动触发dismiss
         */
        fun setShowDuration(@IntRange(from = 1L) showDuration: Long): Builder {
            mShowDuration = showDuration
            return this
        }

        /**
         * 默认值：false
         * true：外部区域的触摸事件都将透传给下面的view,前提是 [.mCancelable] 和 [.mCanceledOnTouchOutside]
         * 必须为true
         * false：外部区域拦截一切触摸事件
         */
        fun setPenetrateOutsideTouchEvent(penetrateOutsideTouchEvent: Boolean): Builder {
            mPenetrateOutsideTouchEvent = penetrateOutsideTouchEvent
            return this
        }

        /**
         * 默认值：false
         * true：将View添加给Window
         * false：默认处理，将View添加给decorView
         */
        fun setAddToWindow(isAddToWindow: Boolean): Builder {
            mIsAddToWindow = isAddToWindow
            return this
        }

        /**
         * [.mRootLayout] 的顶部padding，默认值为状态栏高度
         */
        fun setTopPadding(@Px topPadding: Int): Builder {
            mTopPadding = topPadding
            return this
        }

        /**
         * [.mRootLayout] 的底部padding
         * 1.如果存在虚拟导航栏，默认值为虚拟导航栏的高度
         * 2.不存在虚拟导航栏，默认值为0
         */
        fun setBottomPadding(@Px bottomPadding: Int): Builder {
            mBottomPadding = bottomPadding
            return this
        }

        /**
         * 默认值：[Integer.MAX_VALUE]，表示不限制高度
         * 设置弹窗的最大高度
         */
        fun setMaxHeight(@Px maxHeight: Int): Builder {
            mMaxHeight = maxHeight
            return this
        }

        /**
         * 默认值：[Integer.MAX_VALUE]，表示不限制宽度
         * 设置弹窗的最大宽度
         */
        fun setMaxWidth(@Px maxWidth: Int): Builder {
            mMaxWidth = maxWidth
            return this
        }

        /**
         * 外部区域背景色，默认透明
         */
        fun setBackground(background: Drawable?): Builder {
            mBackground = background
            return this
        }

        /**
         * 用来传输一些额外的数据，将通过
         * [PopupInterface.OnViewStateCallback][.onShowPopup]
         * 回调给用户
         */
        fun setBundle(bundle: Bundle?): Builder {
            mBundle = bundle
            return this
        }

        /**
         * 用来标识一个Popup
         */
        fun setTag(tag: Any?): Builder {
            mTag = tag
            return this
        }

        /**
         * 弹窗类型，主要用来弹窗互斥，作者目前定义了三种类型，开发者可自由发挥
         */
        fun setPopupType(popupType: String): Builder {
            mPopupType = popupType
            return this
        }

        /**
         * 排斥类型
         */
        fun setExcluded(excluded: PopupInterface.Excluded): Builder {
            mExcluded = excluded
            return this
        }

        /**
         * 用户通过触摸外部区域或者back键等方式销毁弹窗的回调
         */
        fun setOnCancelListener(
            onCancelListener: PopupInterface.OnCancelListener?
        ): Builder {
            mOnCancelListener = onCancelListener
            return this
        }

        /**
         * 弹窗展示动画
         */
        fun setInAnimatorCallback(
            inAnimatorCallback: PopupInterface.OnAnimatorCallback?
        ): Builder {
            mInAnimatorCallback = inAnimatorCallback
            return this
        }

        /**
         * 弹窗销毁动画
         */
        fun setOutAnimatorCallback(
            outAnimatorCallback: PopupInterface.OnAnimatorCallback?
        ): Builder {
            mOutAnimatorCallback = outAnimatorCallback
            return this
        }
    }
}