package com.leo.androidwidget.dialog

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.*
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.*
import androidx.annotation.IntRange
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.leo.androidwidget.KeyboardVisibilityUtils
import com.leo.androidwidget.R
import com.leo.androidwidget.Utils
import com.leo.androidwidget.dialog.adapter.DialogViewHolder
import com.leo.androidwidget.dialog.adjust.AdjustStyle
import com.leo.androidwidget.popup.Popup
import com.leo.androidwidget.popup.PopupInterface
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.ArrayList

class SmartDialog(private val builder: Builder) : Popup(builder), View.OnClickListener {

    protected var mInputView: EditText? = null
    private var mKeyboardVisibilityListener: KeyboardVisibilityUtils.OnKeyboardVisibilityListener? =
        null

    override fun onShowPopup(bundle: Bundle?) {
        super.onShowPopup(bundle)
        initTitleView()
        initContentView()
        initDetailView()
        initButton()
        initIconView()
        initInputView()
        initRecyclerView()
        for (adjustStyle in getBuilder().mAdjustStyles) {
            adjustStyle.apply(this)
        }
    }

    override fun onDismissPopup(bundle: Bundle?) {
        super.onDismissPopup(bundle)
        if (mInputView != null) {
            KeyboardVisibilityUtils.unregisterListener(
                mRootLayout,
                mKeyboardVisibilityListener!!
            )
            Utils.hideSoftInput(mInputView?.getWindowToken())
        }
    }

    fun getBuilder(): Builder {
        return mBuilder as Builder
    }

    private fun sendSingleChoiceCallback(itemView: View?) {
        val builder = getBuilder()
        if (builder.mListCallback == null) {
            return
        }
        builder.mListCallback!!.onSelection(this, itemView, builder.mSelectedIndex)
    }

    private fun sendMultiChoiceCallback() {
        val builder = getBuilder()
        if (builder.mListCallbackMultiChoice == null) {
            return
        }
        Collections.sort<Int>(builder.mSelectedIndices)
        builder.mListCallbackMultiChoice!!.onSelection(this, builder.mSelectedIndices)
    }

    private fun sendInputCallback() {
        val builder = getBuilder()
        if (builder.mInputCallback == null || mInputView == null) {
            return
        }
        builder.mInputCallback!!.onInput(this, mInputView!!, mInputView?.getText()!!)
    }

    private fun initTitleView() {
        val titleView = findViewById<TextView>(R.id.title) ?: return
        val builder = getBuilder()
        if (!TextUtils.isEmpty(builder.mTitleText)) {
            titleView.text = builder.mTitleText
            titleView.visibility = View.VISIBLE
        } else {
            titleView.visibility =
                if (TextUtils.isEmpty(titleView.getText())) View.GONE else View.VISIBLE
        }
    }

    private fun initContentView() {
        val contentView = findViewById<TextView>(R.id.content) ?: return
        val builder = getBuilder()
        if (!TextUtils.isEmpty(builder.mContentText)) {
            contentView.text = builder.mContentText
            contentView.visibility = View.VISIBLE
        } else {
            contentView.visibility =
                if (TextUtils.isEmpty(contentView.text)) View.GONE else View.VISIBLE
        }
    }

    private fun initDetailView() {
        val detailView = findViewById<TextView>(R.id.detail) ?: return
        val builder = getBuilder()
        if (!TextUtils.isEmpty(builder.mDetailText)) {
            detailView.text = builder.mDetailText
            detailView.visibility = View.VISIBLE
        } else {
            detailView.visibility =
                if (TextUtils.isEmpty(detailView.getText())) View.GONE else View.VISIBLE
        }
    }

    private fun initButton() {
        val builder = getBuilder()
        val positiveView = findViewById<TextView>(R.id.positive)
        if (positiveView != null) {
            if (!TextUtils.isEmpty(builder.mPositiveText)) {
                positiveView.text = builder.mPositiveText
                positiveView.visibility = View.VISIBLE
            } else {
                positiveView.visibility =
                    if (TextUtils.isEmpty(positiveView.getText())) View.GONE else View.VISIBLE
            }
            if (positiveView.visibility == View.VISIBLE) {
                positiveView.setOnClickListener(this)
            }
        }
        val negativeView = findViewById<TextView>(R.id.negative)
        if (negativeView != null) {
            if (!TextUtils.isEmpty(builder.mNegativeText)) {
                negativeView.text = builder.mNegativeText
                negativeView.visibility = View.VISIBLE
            } else {
                negativeView.visibility =
                    if (TextUtils.isEmpty(negativeView.text)) View.GONE else View.VISIBLE
            }
            if (negativeView.visibility == View.VISIBLE) {
                negativeView.setOnClickListener(this)
            }
        }
        val closeView = findViewById<View>(R.id.close)
        closeView?.setOnClickListener(this)
    }

    private fun initIconView() {
        val iconView = findViewById<ImageView>(R.id.icon) ?: return
        val builder = getBuilder()
        if (builder.mIcon != null) {
            iconView.setImageDrawable(builder.mIcon)
            iconView.visibility = View.VISIBLE
        } else if (builder.mIconUri != null) {
            iconView.setImageURI(builder.mIconUri)
            iconView.visibility = View.VISIBLE
        } else {
            iconView.visibility = if (iconView.getDrawable() == null) View.GONE else View.VISIBLE
        }
    }

    private fun initInputView() {
        mInputView = findViewById(R.id.input)
        if (mInputView == null) {
            return
        }
        val builder = getBuilder()
        if (!TextUtils.isEmpty(builder.mInputHint)) {
            mInputView?.hint = builder.mInputHint
        }
        if (!TextUtils.isEmpty(builder.mInputPrefill)) {
            mInputView?.setText(builder.mInputPrefill)
            mInputView?.setSelection(builder.mInputPrefill!!.length)
        }
        mInputView?.maxLines = builder.mMaxLines
        if (builder.mInputType != -1) {
            mInputView?.inputType = builder.mInputType
            if (builder.mInputType != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD && builder.mInputType and InputType.TYPE_TEXT_VARIATION_PASSWORD == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                mInputView?.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }
        if (builder.mInputMinLength > 0 || builder.mInputMaxLength > 0) {
            invalidatePositiveViewForInput(mInputView?.text!!)
        }
        mInputView?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                invalidatePositiveViewForInput(s)
                if (builder.mAlwaysCallInputCallback) {
                    builder.mInputCallback?.onInput(this@SmartDialog, mInputView!!, s)
                }
            }
        })
        mKeyboardVisibilityListener =
            object : KeyboardVisibilityUtils.OnKeyboardVisibilityListener {
                override fun onKeyboardShow(height: Int) {
                    mPopupView.translationY = (-(height shr 1)).toFloat()
                }

                override fun onKeyboardHide(height: Int) {
                    mPopupView.translationY = 0F
                }
            }
        KeyboardVisibilityUtils.registerListener(mRootLayout, mKeyboardVisibilityListener!!)
        Utils.showKeyboard(mInputView!!)
    }

    private fun invalidatePositiveViewForInput(text: CharSequence) {
        val positiveView = findViewById<TextView>(R.id.positive) ?: return
        val builder = getBuilder()
        if (TextUtils.isEmpty(text) && !builder.mInputAllowEmpty) {
            positiveView.isEnabled = false
            return
        }
        if (builder.mInputMinLength > 0) {
            if (TextUtils.isEmpty(text) || text.length < builder.mInputMinLength) {
                positiveView.isEnabled = false
                return
            }
        }
        if (builder.mInputMaxLength > 0) {
            if (!TextUtils.isEmpty(text) && text.length > builder.mInputMaxLength) {
                positiveView.isEnabled = false
                return
            }
        }
        positiveView.isEnabled = true
    }

    private fun initRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view) ?: return
        val builder = getBuilder()
        if (builder.mLayoutManager != null) {
            recyclerView.layoutManager = builder.mLayoutManager
        } else {
            builder.mLayoutManager = LinearLayoutManager(getContext())
            recyclerView.layoutManager = builder.mLayoutManager
        }
        Collections.sort<Int>(builder.mSelectedIndices)
        recyclerView.adapter = builder.mAdapter
        recyclerView.viewTreeObserver
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    var selectedIndex = -1
                    if (builder.mSelectedIndex > -1) {
                        selectedIndex = builder.mSelectedIndex
                    } else if (builder.mSelectedIndices.isNotEmpty()) {
                        selectedIndex = builder.mSelectedIndices.get(0)
                    }
                    if (selectedIndex < 0) {
                        return
                    }
                    val finalSelectedIndex = selectedIndex
                    recyclerView.post { builder.mLayoutManager!!.scrollToPosition(finalSelectedIndex) }
                }
            })
    }


    override fun onClick(v: View?) {

        val builder = mBuilder as Builder
        val id = v?.id
        if (id == R.id.positive) {
            if (builder.mPositiveCallback != null) {
                builder.mPositiveCallback!!.onClick(this, v)
            }
            if (!builder.mAlwaysCallSingleChoiceCallback) {
                sendSingleChoiceCallback(null)
            }
            if (!builder.mAlwaysCallMultiChoiceCallback) {
                sendMultiChoiceCallback()
            }
            if (!builder.mAlwaysCallInputCallback) {
                sendInputCallback()
            }
            if (builder.mAutoDismiss) {
                dismiss(PopupInterface.CLOSE_TYPE_POSITIVE)
            }
        } else if (id == R.id.negative) {
            if (builder.mNegativeCallback != null) {
                builder.mNegativeCallback!!.onClick(this, v)
            }
            if (builder.mAutoDismiss) {
                cancelPopup(PopupInterface.CLOSE_TYPE_NEGATIVE)
            }
        } else if (id == R.id.close) {
            if (builder.mCloseCallback != null) {
                builder.mCloseCallback!!.onClick(this, v)
            }
            if (builder.mAutoDismiss) {
                cancelPopup(PopupInterface.CLOSE_TYPE_NEGATIVE)
            }
        }
    }

    class Builder(activity: Activity) : Popup.Builder(activity) {
        protected lateinit var mDialog: SmartDialog
        var mAutoDismiss = true
        var mAdjustStyles: ArrayList<AdjustStyle> = ArrayList()

        var mTitleText: CharSequence? = null
        var mContentText: CharSequence? = null
        var mDetailText: CharSequence? = null

        var mPositiveText: CharSequence? = null
        var mNegativeText: CharSequence? = null

        var mIconUri: Uri? = null
        var mIcon: Drawable? = null

        var mInputType = -1
        var mInputMinLength: Int = 0
        var mInputMaxLength: Int = 0
        var mMaxLines = 1
        var mInputAllowEmpty = true
        var mAlwaysCallInputCallback: Boolean = false
        var mInputPrefill: CharSequence? = null
        var mInputHint: CharSequence? = null
        var mInputCallback: SmartDialogInterface.InputCallback? = null

        protected var mListItemLayout: Int = 0
        var mSelectedIndex = -1
        var mAlwaysCallMultiChoiceCallback: Boolean = false
        var mAlwaysCallSingleChoiceCallback: Boolean = false
        var mSelectedIndices: List<Int> = ArrayList()
        protected var mListItems: ArrayList<CharSequence>? = null
        var mAdapter: RecyclerView.Adapter<DialogViewHolder>? = null
        var mLayoutManager: RecyclerView.LayoutManager? = null
        var mListCallback: SmartDialogInterface.ListCallback? = null
        protected var mListLongCallback: SmartDialogInterface.ListCallback? = null
        var mListCallbackMultiChoice: SmartDialogInterface.ListCallbackMultiChoice? = null

        var mPositiveCallback: SmartDialogInterface.ButtonCallback? = null
        var mNegativeCallback: SmartDialogInterface.ButtonCallback? = null
        var mCloseCallback: SmartDialogInterface.ButtonCallback? = null

        init {
            mPopupType = PopupInterface.POPUP_TYPE_DIALOG
            mExcluded = PopupInterface.Excluded.SAME_TYPE
            mBackground = ColorDrawable(-0x80000000)
            mInAnimatorCallback = DialogBuilderFactory.getDefaultInAnimator()
            mOutAnimatorCallback = DialogBuilderFactory.getDefaultOutAnimator()
        }

        override fun build(): SmartDialog {
            mDialog = SmartDialog(this)
            return mDialog
        }

        fun addAdjustStyles(adjustStyles: AdjustStyle): Builder {
            mAdjustStyles.add(adjustStyles)
            return this
        }

        fun addAdjustStyles(adjustStyles: List<AdjustStyle>): Builder {
            mAdjustStyles.addAll(adjustStyles)
            return this
        }

        fun setTitleText(@StringRes titleRes: Int, vararg formatArgs: Any): Builder {
            return setTitleText(mActivity.getString(titleRes, formatArgs))
        }

        fun setContentText(@StringRes contentRes: Int, isHtml: Boolean): Builder {
            var text = mActivity.getText(contentRes)
            if (isHtml) {
                text = Html.fromHtml(text.toString().replace("\n", "<br/>"))
            }
            return setContentText(text)
        }

        fun setContentText(@StringRes contentRes: Int, vararg formatArgs: Any): Builder {
            val contentText =
                String.format(mActivity.getString(contentRes), formatArgs).replace("\n", "<br/>")
            return setContentText(Html.fromHtml(contentText))
        }

        fun setDetailText(@StringRes detailRes: Int, vararg formatArgs: Any): Builder {
            return setDetailText(mActivity.getString(detailRes, formatArgs))
        }

        fun setInput(
            @StringRes inputHintRes: Int,
            @StringRes inputPrefillRes: Int,
            inputCallback: SmartDialogInterface.InputCallback
        ): Builder {
            return setInput(
                if (inputHintRes == 0) null else mActivity.getText(inputHintRes),
                if (inputPrefillRes == 0) null else mActivity.getText(inputPrefillRes),
                inputCallback
            )
        }

        fun setInput(
            inputHint: CharSequence?,
            inputPrefill: CharSequence?,
            inputCallback: SmartDialogInterface.InputCallback
        ): Builder {
            mInputHint = inputHint
            mInputPrefill = inputPrefill
            mInputCallback = inputCallback
            return this
        }

        fun inputRange(
            @IntRange(from = 0L, to = Integer.MAX_VALUE.toLong()) inputMinLength: Int,
            @IntRange(from = 1L, to = Integer.MAX_VALUE.toLong()) inputMaxLength: Int
        ): Builder {
            mInputMinLength = inputMinLength
            mInputMaxLength = inputMaxLength
            if (mInputMinLength > 0) {
                mInputAllowEmpty = false
            }
            return this
        }

        fun setItemsCallback(
            listCallback: SmartDialogInterface.ListCallback?
        ): Builder {
            mListCallback = listCallback
            return this
        }

        fun setItemsLongCallback(
            listLongCallback: SmartDialogInterface.ListCallback?
        ): Builder {
            mListLongCallback = listLongCallback
            return this
        }

        fun itemsCallbackMultiChoice(
            selectedIndices: List<Int>?,
            callback: SmartDialogInterface.ListCallbackMultiChoice
        ): Builder {
            if (selectedIndices != null) {
                mSelectedIndices = selectedIndices
            }
            mListCallbackMultiChoice = callback
            return this
        }

        fun onPositive(
            buttonCallback: SmartDialogInterface.ButtonCallback
        ): Builder {
            mPositiveCallback = buttonCallback
            return this
        }

        fun onNegative(
            buttonCallback: SmartDialogInterface.ButtonCallback
        ): Builder {
            mNegativeCallback = buttonCallback
            return this
        }

        fun onClose(
            buttonCallback: SmartDialogInterface.ButtonCallback
        ): Builder {
            mCloseCallback = buttonCallback
            return this
        }

        fun getDialog(): SmartDialog {
            return mDialog
        }

        fun isAutoDismiss(): Boolean {
            return mAutoDismiss
        }

        fun setAutoDismiss(autoDismiss: Boolean): Builder {
            mAutoDismiss = autoDismiss
            return this
        }

        fun getAdapter(): RecyclerView.Adapter<DialogViewHolder>? {
            return mAdapter
        }

        fun setAdapter(adapter: RecyclerView.Adapter<DialogViewHolder>): Builder {
            mAdapter = adapter
            return this
        }

        fun getListCallback(): SmartDialogInterface.ListCallback? {
            return mListCallback
        }

        fun getListLongCallback(): SmartDialogInterface.ListCallback? {
            return mListLongCallback
        }

        fun getListCallbackMultiChoice(): SmartDialogInterface.ListCallbackMultiChoice? {
            return mListCallbackMultiChoice
        }

        fun getListItemLayout(): Int {
            return mListItemLayout
        }

        fun setListItemLayout(@LayoutRes listItemLayout: Int): Builder {
            mListItemLayout = listItemLayout
            return this
        }

        fun getListItems(): List<CharSequence> {
            return mListItems!!
        }

        fun setListItems(@ArrayRes itemsRes: Int): Builder {
            return setListItems(Utils.getResources().getTextArray(itemsRes))
        }

        fun setListItems(items: Array<CharSequence>): Builder {
            mListItems = ArrayList()
            items.forEach {
                mListItems!!.add(it)
            }
            return this
        }

        fun setListItems(listItems: ArrayList<CharSequence>?): Builder {
            mListItems = listItems
            return this
        }

        fun getSelectedIndex(): Int {
            return mSelectedIndex
        }

        fun setSelectedIndex(selectedIndex: Int): Builder {
            mSelectedIndex = selectedIndex
            return this
        }

        fun getSelectedIndices(): List<Int> {
            return mSelectedIndices
        }

        fun isAlwaysCallMultiChoiceCallback(): Boolean {
            return mAlwaysCallMultiChoiceCallback
        }

        fun setAlwaysCallMultiChoiceCallback(
            alwaysCallMultiChoiceCallback: Boolean
        ): Builder {
            mAlwaysCallMultiChoiceCallback = alwaysCallMultiChoiceCallback
            return this
        }

        fun getAdjustStyles(): List<AdjustStyle> {
            return mAdjustStyles
        }

        fun getTitleText(): CharSequence? {
            return mTitleText
        }

        fun setTitleText(@StringRes titleRes: Int): Builder {
            return setTitleText(mActivity.getText(titleRes))
        }

        fun setTitleText(titleText: CharSequence?): Builder {
            mTitleText = titleText
            return this
        }

        fun getContentText(): CharSequence? {
            return mContentText
        }

        fun setContentText(@StringRes contentRes: Int): Builder {
            return setContentText(contentRes, false)
        }

        fun setContentText(contentText: CharSequence): Builder {
            mContentText = contentText
            return this
        }

        fun getDetailText(): CharSequence? {
            return mDetailText
        }

        fun setDetailText(@StringRes detailRes: Int): Builder {
            return setDetailText(mActivity.getText(detailRes))
        }

        fun setDetailText(detailText: CharSequence): Builder {
            mDetailText = detailText
            return this
        }

        fun getPositiveText(): CharSequence? {
            return mPositiveText
        }

        fun setPositiveText(@StringRes positiveRes: Int): Builder {
            return setPositiveText(mActivity.getText(positiveRes))
        }

        fun setPositiveText(positiveText: CharSequence): Builder {
            mPositiveText = positiveText
            return this
        }

        fun getNegativeText(): CharSequence? {
            return mNegativeText
        }

        fun setNegativeText(@StringRes negativeRes: Int): Builder {
            return setNegativeText(mActivity.getText(negativeRes))
        }

        fun setNegativeText(negativeText: CharSequence): Builder {
            mNegativeText = negativeText
            return this
        }

        fun getIconUri(): Uri? {
            return mIconUri
        }

        fun setIconUri(iconUri: Uri): Builder {
            mIconUri = iconUri
            return this
        }

        fun getIcon(): Drawable? {
            return mIcon
        }

        fun setIcon(@DrawableRes icon: Int): Builder {
            return setIcon(mActivity.resources.getDrawable(icon))
        }

        fun setIcon(icon: Drawable): Builder {
            mIcon = icon
            return this
        }

        fun getInputType(): Int {
            return mInputType
        }

        fun setInputType(inputType: Int): Builder {
            mInputType = inputType
            return this
        }

        fun getInputMinLength(): Int {
            return mInputMinLength
        }

        fun getInputMaxLength(): Int {
            return mInputMaxLength
        }

        fun getMaxLines(): Int {
            return mMaxLines
        }

        fun setMaxLines(@IntRange(from = 1L) maxLines: Int): Builder {
            mMaxLines = maxLines
            return this
        }

        fun isInputAllowEmpty(): Boolean {
            return mInputAllowEmpty
        }

        fun setInputAllowEmpty(inputAllowEmpty: Boolean): Builder {
            mInputAllowEmpty = inputAllowEmpty
            return this
        }

        fun isAlwaysCallInputCallback(): Boolean {
            return mAlwaysCallInputCallback
        }

        fun setAlwaysCallInputCallback(alwaysCallInputCallback: Boolean): Builder {
            mAlwaysCallInputCallback = alwaysCallInputCallback
            return this
        }

        fun getInputPrefill(): CharSequence? {
            return mInputPrefill
        }

        fun getInputHint(): CharSequence? {
            return mInputHint
        }

        fun getInputCallback(): SmartDialogInterface.InputCallback? {
            return mInputCallback
        }

        fun isAlwaysCallSingleChoiceCallback(): Boolean {
            return mAlwaysCallSingleChoiceCallback
        }

        fun setAlwaysCallSingleChoiceCallback(
            alwaysCallSingleChoiceCallback: Boolean
        ): Builder {
            mAlwaysCallSingleChoiceCallback = alwaysCallSingleChoiceCallback
            return this
        }

        fun getLayoutManager(): RecyclerView.LayoutManager? {
            return mLayoutManager
        }

        fun setLayoutManager(
            layoutManager: RecyclerView.LayoutManager?
        ): Builder {
            mLayoutManager = layoutManager
            return this
        }

        fun getPositiveCallback(): SmartDialogInterface.ButtonCallback? {
            return mPositiveCallback
        }

        fun getNegativeCallback(): SmartDialogInterface.ButtonCallback? {
            return mNegativeCallback
        }

        fun getCloseCallback(): SmartDialogInterface.ButtonCallback? {
            return mCloseCallback
        }
    }
}