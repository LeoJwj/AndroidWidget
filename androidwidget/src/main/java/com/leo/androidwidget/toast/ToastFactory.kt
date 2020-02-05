package com.leo.androidwidget.toast

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.StringRes
import com.leo.androidwidget.AndroidWidget
import com.leo.androidwidget.R
import com.leo.androidwidget.Utils
import com.leo.androidwidget.popup.PopupInterface

object ToastFactory {

    fun info(@StringRes textRes: Int): SmartToast {
        return info(Utils.getText(textRes))
    }

    fun info(@StringRes textRes: Int, vararg formatArgs: Any): SmartToast {
        return info(Utils.getString(textRes, formatArgs))
    }

    fun info(text: CharSequence): SmartToast {
        return show(text, null)
    }

    fun notify(@StringRes textRes: Int): SmartToast {
        return notify(Utils.getText(textRes))
    }

    fun notify(@StringRes textRes: Int, vararg formatArgs: Any): SmartToast {
        return notify(Utils.getString(textRes, formatArgs))
    }

    fun notify(text: CharSequence): SmartToast {
        return show(text, Utils.getDrawable(R.drawable.toast_success))
    }

    fun alert(@StringRes textRes: Int): SmartToast {
        return alert(Utils.getText(textRes))
    }

    fun alert(@StringRes textRes: Int, vararg formatArgs: Any): SmartToast {
        return alert(Utils.getString(textRes, formatArgs))
    }

    fun alert(text: CharSequence): SmartToast {
        return show(text, Utils.getDrawable(R.drawable.toast_error))
    }

    fun show(text: CharSequence, iconDrawable: Drawable?): SmartToast {
        return SmartToast.show(
            AndroidWidget.getBuilder().setText<SmartToast.Builder>(text).setIcon(
                iconDrawable
            )
        )
    }

    fun getDefaultInAnimator(): PopupInterface.OnAnimatorCallback {
        return object : PopupInterface.OnAnimatorCallback {
            override fun onStartAnimator(view: View, animatorListener: Animator.AnimatorListener?) {
                val alphaAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
                val scaleXAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.9f, 1.0f)
                val scaleYAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.9f, 1.0f)
                val animatorSet = AnimatorSet()
                animatorSet.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator)
                animatorSet.duration = 300L
                animatorSet.interpolator = DecelerateInterpolator(1.5f)
                animatorSet.addListener(animatorListener)
                animatorSet.start()
            }
        }
    }

    fun getDefaultOutAnimator(): PopupInterface.OnAnimatorCallback {
        return object : PopupInterface.OnAnimatorCallback {
            override fun onStartAnimator(view: View, animatorListener: Animator.AnimatorListener?) {
                val alphaAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
                val scaleXAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 0.9f)
                val scaleYAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 0.9f)
                val animatorSet = AnimatorSet()
                animatorSet.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator)
                animatorSet.duration = 240L
                animatorSet.interpolator = DecelerateInterpolator(1.5f)
                animatorSet.addListener(animatorListener)
                animatorSet.start()
            }

        }
    }
}