package com.leo.androidwidget

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.leo.androidwidget.popup.DefaultPopupManager
import com.leo.androidwidget.popup.PopupInterface
import com.leo.androidwidget.toast.SmartToast
import java.lang.ref.WeakReference

object AndroidWidget {
    private var sConfig: Config? = null
    private var sCurrentActivity: WeakReference<Activity>? = null

    fun init(config: Config) {
        if (sConfig != null) {
            return
        }
        sConfig = config
        config.mApplication
            .registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    sCurrentActivity = WeakReference(activity)
                    SmartToast.showPendingToast(activity)
                }

                override fun onActivityStarted(activity: Activity) {}

                override fun onActivityResumed(activity: Activity) {
                    if (sCurrentActivity?.get() !== activity) {
                        sCurrentActivity = WeakReference(activity)
                    }
                    SmartToast.showPendingToast(activity)
                }

                override fun onActivityPaused(activity: Activity) {}

                override fun onActivityStopped(activity: Activity) {}

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

                override fun onActivityDestroyed(activity: Activity) {
                    if (sCurrentActivity?.get() == activity) {
                        sCurrentActivity = null
                    }
                    getPopupManager().onActivityDestroy(activity)
                }
            })
    }

    fun getContext(): Context? {
        return if (sCurrentActivity?.get() != null) {
            sCurrentActivity?.get()
        } else sConfig?.mApplication
    }

    fun getApplication(): Application {
        return sConfig?.mApplication!!
    }

    fun getCurrentActivity(): Activity? {
        return sCurrentActivity?.get()
    }

    fun getPopupManager(): PopupInterface.PopupManager {
        return sConfig?.mPopupManager!!
    }

    fun getBuilder(): SmartToast.Builder {
        return sConfig?.mBuilder?.clone()!!
    }

    class Config(internal val mApplication: Application) {
        internal var mPopupManager: PopupInterface.PopupManager = DefaultPopupManager()
        internal var mBuilder = SmartToast.Builder()

        fun setPopupManager(popupManager: PopupInterface.PopupManager): Config {
            mPopupManager = popupManager
            return this
        }

        fun setBuilder(builder: SmartToast.Builder): Config {
            mBuilder = builder
            return this
        }
    }
}