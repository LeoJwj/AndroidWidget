package com.leo.androidwidget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.TypedValue
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

object Utils {
    private val mHandler = Handler(Looper.getMainLooper())


    fun runOnUIThread(runnable: Runnable) {
        if (isMainThread()) {
            runnable.run()
        } else {
            mHandler.post(runnable)
        }
    }

    fun runOnUIThread(runnable: Runnable, delayed: Long) {
        mHandler.postDelayed(runnable, delayed)
    }

    fun removeCallbacks(runnable: Runnable) {
        mHandler.removeCallbacks(runnable)
    }

    fun showKeyboard(editText: EditText) {
        showKeyboard(editText, 0L)
    }

    fun showKeyboard(editText: EditText, delayed: Long) {
        runOnUIThread(Runnable {
            editText.requestFocus()
            val imm = AndroidWidget.getContext()
                ?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }, delayed)
    }

    fun hideSoftInput(): Boolean {
        val activity = AndroidWidget.getCurrentActivity()
        if (activity != null) {
            val focusView = activity!!.getCurrentFocus()
            return if (focusView != null) {
                hideSoftInput(focusView!!.getWindowToken())
            } else {
                false
            }
        }
        return false
    }

    fun hideSoftInput(window: Window): Boolean {
        val focusView = window.currentFocus
        return if (focusView != null) {
            hideSoftInput(focusView.windowToken)
        } else false
    }

    fun hideSoftInput(windowToken: IBinder?): Boolean {
        if (windowToken == null) {
            return false
        }
        val imm = AndroidWidget.getContext()
            ?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.hideSoftInputFromWindow(windowToken, 0) ?: false
    }

    fun getResources(): Resources {
        return AndroidWidget.getContext()!!.resources
    }

    fun getDimensionPixelSize(@DimenRes id: Int): Int {
        return getResources().getDimensionPixelSize(id)
    }

    fun isMainThread(): Boolean {
        return Looper.getMainLooper() == Looper.myLooper()
    }

    fun getFloatDimensionSize(@DimenRes id: Int): Float {
        val outValue = TypedValue()
        getResources().getValue(id, outValue, true)
        return outValue.float
    }

    fun getText(@StringRes resId: Int): CharSequence {
        return getResources().getText(resId)
    }

    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return getResources().getString(resId, *formatArgs)
    }

    fun getDrawable(@DrawableRes drawableId: Int): Drawable {
        return getResources().getDrawable(drawableId)
    }

    fun dip2px(dpValue: Float): Int {
        return (dpValue * getResources().displayMetrics.density + 0.5f).toInt()
    }

    /**
     * 需要在DecorView layout完成以后才能获取到正确的值
     */
    fun getWindowWidth(activity: Activity): Int {
        return activity.window.decorView.width
    }

    /**
     * 需要在DecorView layout完成以后才能获取到正确的值
     */
    fun getWindowHeight(activity: Activity): Int {
        return activity.window.decorView.height
    }

    /**
     * 某种情况下 getChildFragmentManager 在子线程会崩溃，所以建议在
     * UI线程调用，否则可能拿不到顶部的 DialogFragment
     */
    @UiThread
    fun getTopDialogFragment(): DialogFragment? {
        val context = AndroidWidget.getContext()
        if (context !is FragmentActivity) {
            return null
        }
        var fragmentList = context.supportFragmentManager.fragments
        var dialogFragment: DialogFragment? = null
        var fragment: Fragment?
        if (isMainThread()) {
            while (!fragmentList.isEmpty()) {
                fragment = fragmentList[fragmentList.size - 1]
                if (fragment is DialogFragment) {
                    dialogFragment = fragment
                }
                fragmentList = fragment!!.childFragmentManager.fragments
            }
        } else {
            fragment = if (fragmentList.isEmpty()) null else fragmentList[fragmentList.size - 1]
            if (fragment is DialogFragment) {
                dialogFragment = fragment
            }
        }
        return dialogFragment
    }

    fun getDialogFragmentContentView(dialogFragment: DialogFragment): ViewGroup? {
        val view = dialogFragment.view
        return if (view != null && view.parent != null) view.parent as ViewGroup else null
    }


    fun getStatusBarHeight(activity: Activity): Int {
        val statusBarInfo = getStatusBarInfo(activity)
        if (!statusBarInfo.mIsExist) {
            return 0
        }
        if (statusBarInfo.mHeight > 0) {
            return statusBarInfo.mHeight
        }
        var statusBarHeight = 0
        val resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = getDimensionPixelSize(resourceId)
        } else {
            try {
                @SuppressLint("PrivateApi")
                val c = Class.forName("com.android.internal.R\$dimen")
                val field = c.getField("status_bar_height")
                field.isAccessible = true
                statusBarHeight =
                    getDimensionPixelSize(Integer.parseInt(field.get(c.newInstance()).toString()))
            } catch (throwable: Throwable) {
            }

        }
        if (statusBarHeight <= 0) {
            statusBarHeight = dip2px(25f)
        }
        return statusBarHeight
    }

    fun getNavigationBarHeight(activity: Activity): Int {
        val navigationBarInfo = getNavigationBarInfo(activity)
        if (!navigationBarInfo.mIsExist) {
            return 0
        }
        return if (navigationBarInfo.mHeight > 0)
            navigationBarInfo.mHeight
        else
            getDimensionPixelSize(
                getResources().getIdentifier("navigation_bar_height", "dimen", "android")
            )
    }

    fun getNavigationBarInfo(activity: Activity): SystemBarInfo {
        val navigationBarInfo = SystemBarInfo()
        val decorView = activity.window.decorView as ViewGroup
        val childCount = decorView.childCount
        for (i in 0 until childCount) {
            val childView = decorView.getChildAt(i)
            if (childView.id == android.R.id.navigationBarBackground) {
                navigationBarInfo.mIsExist = true
                /**
                 * 横屏状态下虚拟导航栏的位置和竖屏是一样的，默认开发者依然想拿的是竖屏状态下导航栏的高度，
                 * 所以横屏状态下返回的是导航栏的宽度值
                 */
                if (isLandscape()) {
                    navigationBarInfo.mHeight = childView.width
                } else {
                    navigationBarInfo.mHeight = childView.height
                }
                break
            }
        }
        return navigationBarInfo
    }

    fun getStatusBarInfo(activity: Activity): SystemBarInfo {
        val statusBarInfo = SystemBarInfo()
        val decorView = activity.window.decorView as ViewGroup
        val childCount = decorView.childCount
        for (i in 0 until childCount) {
            val childView = decorView.getChildAt(i)
            if (childView.id == android.R.id.statusBarBackground) {
                statusBarInfo.mIsExist = true
                statusBarInfo.mHeight = childView.height
                break
            }
        }
        return statusBarInfo
    }

    fun isMIUI(): Boolean {
        return Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)
    }

    fun isFlyme(): Boolean {
        try {
            return Build::class.java.getMethod("hasSmartBar") != null
        } catch (e: Exception) {
        }

        return false
    }

    fun isFullScreen(window: Window): Boolean {
        return window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN == WindowManager.LayoutParams.FLAG_FULLSCREEN
    }

    fun isLandscape(): Boolean {
        return getResources().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

}