package com.leo.androidwidget.dialog

import android.view.View
import androidx.annotation.IntRange

class SmartDialogInterface {

    interface ButtonCallback {
        fun onClick(dialog: SmartDialog, view: View)
    }

    interface InputCallback {
        fun onInput(dialog: SmartDialog, view: View, input: CharSequence)
    }

    interface ListCallback {
        fun onSelection(
            dialog: SmartDialog, itemView: View?,
            @IntRange(from = 0) position: Int
        )
    }

    interface ListCallbackMultiChoice {
        fun onSelection(
            dialog: SmartDialog,
            @IntRange(from = 0) position: List<Int>
        )
    }
}