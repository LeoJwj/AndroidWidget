package com.leo.androidwidget.toast

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.leo.androidwidget.AndroidWidget
import com.leo.androidwidget.R
import com.leo.androidwidget.Utils
import com.leo.androidwidget.popup.PopupInterface
import java.lang.ref.WeakReference
import java.util.*

class SmartToast(private val mBuilder: Builder) {
    companion object {
        private val MSG_SHOW = 0
        private val MSG_DISMISS = 1
        private val sHandler: Handler
        private val sInterceptors = ArrayList<Interceptor>()
        private var sCurrentToast: WeakReference<SmartToast>? = null
        private var sResidueDuration = 1000L

        init {
            sHandler = Handler(Looper.getMainLooper()) { message ->
                when (message.what) {
                    MSG_SHOW -> {
                        (message.obj as SmartToast).showView()
                        true
                    }
                    MSG_DISMISS -> {
                        (message.obj as SmartToast).dismissView()
                        true
                    }
                    else -> false
                }
            }
        }

        /**
         * ArrayList 是非线程安全的，开发者自己注意多线程问题
         */
        fun addInterceptor(interceptor: Interceptor): Boolean {
            return if (sInterceptors.contains(interceptor)) {
                false
            } else sInterceptors.add(interceptor)
        }

        /**
         * ArrayList 是非线程安全的，开发者自己注意多线程问题
         */
        fun removeInterceptor(interceptor: Interceptor): Boolean {
            return sInterceptors.remove(interceptor)
        }

        fun setResidueDuration(residueDuration: Long) {
            sResidueDuration = residueDuration
        }

        fun showPendingToast(activity: Activity) {
            val toast = getCurrentToast() ?: return
            val residueDuration = toast.getNeedShowDuration() - toast.getShownDuration()
            // 差值需要大于sResidueDuration，是觉得如果展示时间差的不多的话，也没必要在下个Activity继续展示
            if (toast.getContext() !== activity && residueDuration > sResidueDuration) {
                val builder = toast.getBuilder()
                toast.forbidOutAnimator()
                SmartToast.show<SmartToast>(
                    builder.setInAnimatorCallback<Builder>(null).setDuration(
                        residueDuration.toInt()
                    )
                )
            }
        }

        fun getCurrentToast(): SmartToast? {
            return sCurrentToast?.get()
        }

        fun <T : SmartToast> show(builder: Builder): T {
            return RealInterceptorChain(Collections.unmodifiableList(sInterceptors), builder)
                .proceed(builder).build().show<T>()
        }
    }

    protected val mManagerCallback: ToastManager.Callback
    protected lateinit var mToastView: View
    protected lateinit var mRootLayout: ViewGroup
    protected var mStartShowTime: Long = 0

    init {
        mManagerCallback = object : ToastManager.Callback {
            override fun show() {
                sHandler.sendMessage(sHandler.obtainMessage(MSG_SHOW, this@SmartToast))
            }

            override fun dismiss() {
                sHandler.sendMessage(sHandler.obtainMessage(MSG_DISMISS, this@SmartToast))
            }
        }
        initView()
    }

    fun dismiss() {
        ToastManager.dismiss(mManagerCallback)
    }

    fun getView(): View {
        return mToastView
    }

    fun isShown(): Boolean {
        return ToastManager.isCurrent(mManagerCallback)
    }

    fun isShownOrQueued(): Boolean {
        return ToastManager.isCurrentOrNext(mManagerCallback)
    }

    fun getBuilder(): Builder {
        return mBuilder.clone()
    }

    fun getContext(): Context {
        return mToastView.context
    }

    fun getMessage(): CharSequence {
        return mBuilder.text
    }

    fun forbidOutAnimator() {
        mBuilder.outAnimatorCallback = null
    }

    fun forbidInAnimator() {
        mBuilder.inAnimatorCallback = null
    }

    fun getNeedShowDuration(): Long {
        return if (mBuilder.duration == ToastManager.LENGTH_SHORT) {
            ToastManager.SHORT_DURATION_MS
        } else if (mBuilder.duration == ToastManager.LENGTH_LONG) {
            ToastManager.LONG_DURATION_MS
        } else {
            mBuilder.duration.toLong()
        }
    }

    fun getShownDuration(): Long {
        return SystemClock.elapsedRealtime() - mStartShowTime
    }

    private fun <T : SmartToast> show(): T {
        if (!TextUtils.isEmpty(mBuilder.text)) {
            Utils.runOnUIThread(Runnable
            { ToastManager.show(mBuilder.duration, mManagerCallback) })
        }
        return this as T
    }

    private fun initView() {
        val context = AndroidWidget.getContext()
        mRootLayout = FrameLayout(context!!)
        mToastView = LayoutInflater.from(context).inflate(mBuilder.layoutRes, mRootLayout, false)
    }

    private fun showView() {
        val context = AndroidWidget.getContext()
        if (context !is Activity) {
            val toast = Toast.makeText(context, mBuilder.text, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
            onViewShown()
            return
        }

        mStartShowTime = SystemClock.elapsedRealtime()
        sCurrentToast = WeakReference(this)

        val activity = context as Activity
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(activity.window.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION
        layoutParams.flags =
            layoutParams.flags or (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        layoutParams.format = PixelFormat.TRANSLUCENT
        layoutParams.gravity = Gravity.CENTER
        activity.windowManager.addView(mRootLayout, layoutParams)

        observerViewProperty()
        mRootLayout.addView(mToastView)
        if (mBuilder.toastBackground != null) {
            mToastView.background = mBuilder.toastBackground
        }
        val iconView = mToastView.findViewById<ImageView>(R.id.toast_icon)
        if (iconView != null && mBuilder.icon != null) {
            iconView.setImageDrawable(mBuilder.icon)
            iconView.visibility = View.VISIBLE
        }
        val textView = mToastView.findViewById<TextView>(R.id.toast_text)
        if (textView != null) {
            textView.text = mBuilder.text
            textView.visibility = View.VISIBLE
        }
        if (mBuilder.viewAddListener != null) {
            mBuilder.viewAddListener?.onViewAdded(mToastView, mBuilder)
        }
    }

    private fun observerViewProperty() {
        mToastView.viewTreeObserver
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mToastView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    if (mBuilder.inAnimatorCallback != null) {
                        animateViewIn()
                    } else {
                        onViewShown()
                    }
                }
            })
        mToastView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}

            override fun onViewDetachedFromWindow(v: View) {
                if (isShownOrQueued()) {
                    sHandler.post { onViewHidden() }
                }
            }
        })
    }

    private fun dismissView() {
        sCurrentToast = null
        if (mBuilder.outAnimatorCallback != null) {
            animateViewOut()
        } else {
            onViewHidden()
        }
    }

    private fun animateViewIn() {
        mBuilder.inAnimatorCallback?.onStartAnimator(
            mToastView,
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onViewShown()
                }
            })
    }

    private fun animateViewOut() {
        mBuilder.outAnimatorCallback?.onStartAnimator(
            mToastView,
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onViewHidden()
                }
            })
    }

    private fun onViewShown() {
        ToastManager.onShown(mManagerCallback)
    }

    private fun onViewHidden() {
        ToastManager.onDismissed(mManagerCallback)
        if (mRootLayout.parent != null) {
            try {
                (mRootLayout.context as Activity).windowManager.removeViewImmediate(mRootLayout)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        if (mBuilder.viewRemoveListener != null) {
            mBuilder.viewRemoveListener?.onViewRemoved(mToastView)
        }
    }

    interface ViewAddListener {
        fun onViewAdded(toastView: View, builder: Builder)
    }

    interface ViewRemoveListener {
        fun onViewRemoved(toastView: View)
    }


    open class Builder : Cloneable {
        var layoutRes = R.layout.toast_layout
        var duration = ToastManager.LENGTH_LONG
        var text: CharSequence = ""
        var icon: Drawable? = null
        var toastBackground: Drawable? = null
        var tag: Any? = null
        var viewRemoveListener: ViewRemoveListener? = null
        var viewAddListener: ViewAddListener? = null
        var inAnimatorCallback: PopupInterface.OnAnimatorCallback? =
            ToastFactory.getDefaultInAnimator()
        var outAnimatorCallback: PopupInterface.OnAnimatorCallback? =
            ToastFactory.getDefaultOutAnimator()

        public override fun clone(): Builder {
            try {
                return super.clone() as Builder
            } catch (e: Exception) {
            }

            return Builder()
        }

        fun build(): SmartToast {
            return SmartToast(this)
        }

        fun <T : Builder> setText(@StringRes resId: Int): T {
            return setText(Utils.getString(resId))
        }

        fun <T : Builder> setText(text: CharSequence): T {
            this.text = text
            return this as T
        }

        fun <T : Builder> setTag(tag: Any?): T {
            this.tag = tag
            return this as T
        }

        fun <T : Builder> setLayoutRes(@LayoutRes layoutRes: Int): T {
            this.layoutRes = layoutRes
            return this as T
        }

        fun <T : Builder> setDuration(@IntRange(from = -2) duration: Int): T {
            this.duration = duration
            return this as T
        }

        fun <T : Builder> setIcon(@DrawableRes drawableId: Int): T {
            return setIcon(Utils.getDrawable(drawableId))
        }

        fun <T : Builder> setIcon(iconDrawable: Drawable?): T {
            icon = iconDrawable
            return this as T
        }

        fun <T : Builder> setToastBackground(@DrawableRes drawableId: Int): T {
            return setToastBackground(Utils.getDrawable(drawableId))
        }

        fun <T : Builder> setToastBackground(toastBackground: Drawable?): T {
            this.toastBackground = toastBackground
            return this as T
        }

        fun <T : Builder> setViewRemoveListener(
            viewRemoveListener: ViewRemoveListener?
        ): T {
            this.viewRemoveListener = viewRemoveListener
            return this as T
        }

        fun <T : Builder> setViewAddListener(viewAddListener: ViewAddListener?): T {
            this.viewAddListener = viewAddListener
            return this as T
        }

        fun <T : Builder> setInAnimatorCallback(
            inAnimatorCallback: PopupInterface.OnAnimatorCallback?
        ): T {
            this.inAnimatorCallback = inAnimatorCallback
            return this as T
        }

        fun <T : Builder> setOutAnimatorCallback(
            outAnimatorCallback: PopupInterface.OnAnimatorCallback?
        ): T {
            this.outAnimatorCallback = outAnimatorCallback
            return this as T
        }
    }
}