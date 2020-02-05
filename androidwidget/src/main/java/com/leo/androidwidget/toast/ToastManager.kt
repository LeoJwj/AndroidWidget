package com.leo.androidwidget.toast

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.RuntimeException
import java.lang.ref.WeakReference

object ToastManager {

    val LENGTH_INDEFINITE = -2
    val LENGTH_SHORT = -1
    val LENGTH_LONG = 0

    val SHORT_DURATION_MS = 1500L
    val LONG_DURATION_MS = 2000L

    private val MSG_TIMEOUT = 0

    private val mLock: Any = Any()
    private val mHandler: Handler

    private var mCurrentToast: ToastRecord? = null
    private var mNextToast: ToastRecord? = null

    init {
        mHandler = Handler(Looper.getMainLooper()) {
            when (it.what) {
                MSG_TIMEOUT -> {
                    handleTimeout(it.obj as ToastRecord)
                    true
                }
                else -> false
            }
        }
    }

    fun show(duration: Int, callback: Callback) {
        synchronized(mLock) {
            if (isCurrentToastLocked(callback)) {
                mCurrentToast?.mDuration = duration
                mHandler.removeCallbacksAndMessages(mCurrentToast)
                scheduleTimeoutLocked(mCurrentToast)
            } else {
                if (isNextToastLocked(callback)) {
                    mNextToast!!.mDuration = duration
                } else {
                    mNextToast = ToastRecord(duration, callback)
                }

                if (mCurrentToast == null || !cancelToastLocked(mCurrentToast)) {
                    mCurrentToast = null
                    showNextToastLocked()
                }
            }
        }
    }

    fun dismiss(callback: Callback) {
        synchronized(mLock) {
            when {
                isCurrentToastLocked(callback) -> cancelToastLocked(mCurrentToast)
                isNextToastLocked(callback) -> cancelToastLocked(mNextToast)
                else -> throw RuntimeException()
            }
        }
    }

    fun onDismissed(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentToastLocked(callback)) {
                mCurrentToast = null
                if (mNextToast != null) {
                    showNextToastLocked()
                }
            }
        }
    }

    fun onShown(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentToastLocked(callback)) {
                scheduleTimeoutLocked(mCurrentToast)
            }
        }
    }

    fun pauseTimeout(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentToastLocked(callback) && !mCurrentToast!!.mPaused) {
                mCurrentToast!!.mPaused = true
                mHandler.removeCallbacksAndMessages(mCurrentToast)
            }
        }
    }

    fun restoreTimeoutIfPaused(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentToastLocked(callback) && mCurrentToast!!.mPaused) {
                mCurrentToast!!.mPaused = false
                scheduleTimeoutLocked(mCurrentToast)
            }
        }
    }

    fun isCurrent(callback: Callback): Boolean {
        synchronized(mLock) {
            return isCurrentToastLocked(callback)
        }
    }

    fun isCurrentOrNext(callback: Callback): Boolean {
        synchronized(mLock) {
            return isCurrentToastLocked(callback) || isNextToastLocked(callback)
        }
    }

    private fun showNextToastLocked() {
        if (mNextToast != null) {
            mCurrentToast = mNextToast
            mNextToast = null
            val callback = mCurrentToast!!.mCallback.get()
            if (callback != null) {
                callback!!.show()
            } else {
                mCurrentToast = null
            }
        }
    }

    private fun cancelToastLocked(record: ToastRecord?): Boolean {
        val callback = record?.mCallback?.get()
        return if (callback != null) {
            mHandler.removeCallbacksAndMessages(record)
            callback.dismiss()
            true
        } else {
            false
        }
    }

    private fun isCurrentToastLocked(callback: Callback): Boolean {
        return mCurrentToast?.isToast(callback) ?: false
    }

    private fun isNextToastLocked(callback: Callback): Boolean {
        return mNextToast?.isToast(callback) ?: false
    }

    private fun scheduleTimeoutLocked(record: ToastRecord?) {
        if (record!!.mDuration != LENGTH_INDEFINITE) {
            var durationMs = LONG_DURATION_MS
            if (record.mDuration > 0) {
                durationMs = record.mDuration.toLong()
            } else if (record.mDuration == LENGTH_SHORT) {
                durationMs = SHORT_DURATION_MS
            }

            mHandler.removeCallbacksAndMessages(record)
            mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_TIMEOUT, record), durationMs)
        }
    }

    private fun handleTimeout(record: ToastRecord) {
        synchronized(mLock) {
            if (mCurrentToast === record || mNextToast === record) {
                cancelToastLocked(record)
            }
        }
    }

    interface Callback {
        fun show()

        fun dismiss()
    }

    private class ToastRecord internal constructor(var mDuration: Int, callback: Callback) {
        val mCallback: WeakReference<Callback>
        var mPaused: Boolean = false

        init {
            mCallback = WeakReference(callback)
        }

        fun isToast(callback: Callback?): Boolean {
            return callback != null && mCallback.get() === callback
        }
    }
}