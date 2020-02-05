package com.leo.androidwidget.popup

import android.animation.Animator
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

class PopupInterface private constructor() {
    companion object{
        val POPUP_TYPE_POPUP = "popup_type_popup"
        val POPUP_TYPE_DIALOG = "popup_type_dialog"
        val POPUP_TYPE_BUBBLE = "popup_type_bubble"
        val POPUP_TYPE_INPUT = "popup_type_input"

        val CLOSE_TYPE_AUTO = 0 // 程序自动关闭
        val CLOSE_TYPE_BACK = 1 // back键关闭
        val CLOSE_TYPE_OUTSIDE = 2 // 点击外部区域关闭
        val CLOSE_TYPE_NEGATIVE = 3 // 其它一切消极关闭
        const val CLOSE_TYPE_POSITIVE = 4 // 其它一切积极关闭
        val EMPTY_VISIBILITY_LISTENER: OnVisibilityListener = object : OnVisibilityListener {}
    }

    enum class Excluded {
        NOT_AGAINST, // 不被任何弹窗排斥
        SAME_TYPE, // 被同类型弹窗排斥
        ALL_TYPE // 被所有弹窗排斥
    }

    interface OnViewStateCallback {
        fun onCreateView(
            popup: Popup, inflater: LayoutInflater,
            container: ViewGroup, bundle: Bundle?
        ): View

        fun onDestroyView(popup: Popup) {}
    }

    interface OnVisibilityListener {
        fun onShow(popup: Popup) {}

        fun onDismiss(popup: Popup, dismissType: Int) {}

        /**
         * 由于优先级限制，弹窗排队等待展示.
         */
        fun onPending(popup: Popup) {}

        /**
         * 没有展示就被扔掉，可能发生在页面销毁时，或者还没有展示就调用 dismiss.
         */
        fun onDiscard(popup: Popup) {}
    }

    interface OnCancelListener {
        fun onCancel(popup: Popup, cancelType: Int)
    }

    interface OnAnimatorCallback {
        fun onStartAnimator(view: View, animatorListener: Animator.AnimatorListener?)
    }

    /**
     * 处理弹窗优先级，Popup 本身不处理优先级和排队.
     *
     * [DefaultPopupManager] 提供了默认的排队策略.
     */
    interface PopupManager {

        /**
         * popup 是否可以展示，所有弹窗在展示前都会调用该方法进行判断.
         *
         * @param activity
         * @param popup
         * @return true or false
         */
        fun enableShowNow(activity: Activity, popup: Popup): Boolean

        /**
         * 弹窗展示的回调，和 [.onPopupDismiss] 成对.
         */
        fun onPopupShow(activity: Activity, popup: Popup)

        /**
         * 弹窗消失的回调，和 [.onPopupShow] 成对.
         */
        fun onPopupDismiss(activity: Activity, popup: Popup)

        /**
         * 进入队列回调
         *
         * @see .enableShowNow
         */
        fun onPopupPending(activity: Activity, popup: Popup)

        /**
         * 排队中的弹窗调用 dismiss 取消展示
         *
         * @see .enableShowNow
         */
        fun onPopupDiscard(activity: Activity, popup: Popup)

        fun onActivityDestroy(activity: Activity)
    }

    class OnViewStateCallbackInflateAdapter(@param:LayoutRes @field:LayoutRes private val mLayoutRes: Int) :
        OnViewStateCallback {

        override fun onCreateView(
            popup: Popup, inflater: LayoutInflater,
            container: ViewGroup, bundle: Bundle?
        ): View {
            val view = inflater.inflate(mLayoutRes, container, false)
            onViewCreated(popup, view)
            return view
        }

        protected fun onViewCreated(popup: Popup, view: View) {}
    }
}