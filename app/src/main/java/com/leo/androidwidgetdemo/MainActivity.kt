package com.leo.androidwidgetdemo

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.leo.androidwidget.Utils
import com.leo.androidwidget.dialog.DialogBuilderFactory
import com.leo.androidwidget.dialog.SmartDialog
import com.leo.androidwidget.dialog.SmartDialogInterface
import com.leo.androidwidget.popup.Popup
import com.leo.androidwidget.popup.PopupInterface
import com.leo.androidwidget.toast.ToastFactory
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testSmartToast(this)
        testSmartDialog(this)
    }

    companion object {

        val TAG = "widget"

        private val KEY_BACKGROUND = "background"
        fun testSmartToast(activity: Activity) {
            val title = activity.intent.getStringExtra(KEY_BACKGROUND)
            if (!TextUtils.isEmpty(title)) {
                activity.findViewById<LinearLayout>(R.id.container).setBackgroundColor(Color.BLUE)
            }

            activity.findViewById<Button>(R.id.info)
                .setOnClickListener { ToastFactory.info("一般toast") }
            activity.findViewById<Button>(R.id.notify)
                .setOnClickListener { ToastFactory.notify("成功toast") }
            activity.findViewById<Button>(R.id.alert)
                .setOnClickListener { ToastFactory.alert("失败toast") }

            activity.findViewById<Button>(R.id.sub_thread)
                .setOnClickListener { Thread { ToastFactory.info("子线程发出的toast") }.start() }

            activity.findViewById<Button>(R.id.next_activity).setOnClickListener {
                ToastFactory.info("下一个Activity立即展示的toast")
                val intent = Intent(activity, MainActivity::class.java)
                intent.putExtra(KEY_BACKGROUND, "下一个")
                activity.startActivity(intent)
            }
            activity.findViewById<Button>(R.id.next_activity_delay).setOnClickListener {
                ToastFactory.info("下一个Activity延迟展示的toast")
                val intent = Intent(activity, MainActivity::class.java)
                intent.putExtra(KEY_BACKGROUND, "下一个")
                Utils.runOnUIThread(Runnable { activity.startActivity(intent) }, 500L)
            }
            activity.findViewById<Button>(R.id.prev_activity).setOnClickListener {
                ToastFactory.info("前一个Activity立即展示的toast")
                activity.finish()
            }
            activity.findViewById<Button>(R.id.prev_activity_delay).setOnClickListener {
                ToastFactory.info("前一个Activity延迟展示的toast")
                Utils.runOnUIThread(Runnable { activity.finish() }, 500L)
            }
        }

        fun testSmartDialog(activity: Activity) {
            activity.findViewById<View>(R.id.succession_two_simple_dialog).setOnClickListener({ v ->
                showSimpleDialog(activity, "第一个弹窗")
                showSimpleDialog(activity, "第二个弹窗")
            })
            activity.findViewById<View>(R.id.simple_dialog)
                .setOnClickListener({ v -> showSimpleDialog(activity, "OnePiece") })
            activity.findViewById<View>(R.id.simple_dialog_content_multi_line)
                .setOnClickListener({ v -> showSimpleMultiContentDialog(activity) })
            activity.findViewById<View>(R.id.simple_dialog_title_content_multi_line)
                .setOnClickListener({ v -> showSimpleMultiTitleContentDialog(activity) })
            activity.findViewById<View>(R.id.simple_dialog_two_button)
                .setOnClickListener({ v -> showSimpleTwoButtonDialog(activity) })
            activity.findViewById<View>(R.id.simple_dialog_only_title)
                .setOnClickListener({ v -> showSimpleNoContentDialog(activity) })
            activity.findViewById<View>(R.id.simple_dialog_only_title_multi_line)
                .setOnClickListener({ v -> showSimpleMultiTitleNoContentDialog(activity) })
            activity.findViewById<View>(R.id.title_content_detail_dialog)
                .setOnClickListener({ v -> showSimpleTitleContentDetailDialog(activity) })

            activity.findViewById<View>(R.id.input_dialog)
                .setOnClickListener({ v -> showInputDialog(activity) })

            activity.findViewById<View>(R.id.small_icon_dialog)
                .setOnClickListener({ v -> showSmallIconDialog(activity) })
            activity.findViewById<View>(R.id.net_small_icon_dialog)
                .setOnClickListener({ v -> showNetSmallIconDialog(activity) })
            activity.findViewById<View>(R.id.big_icon_dialog)
                .setOnClickListener({ v -> showBigIconDialog(activity) })
            activity.findViewById<View>(R.id.big_icon_dialog_two_button)
                .setOnClickListener({ v -> showBigIconTwoButtonDialog(activity) })

            activity.findViewById<View>(R.id.list_dialog)
                .setOnClickListener({ v -> showSimpleListDialog(activity) })
            activity.findViewById<View>(R.id.list_dialog_multi_line)
                .setOnClickListener({ v -> showSimpleListMultiLineDialog(activity) })
            activity.findViewById<View>(R.id.multi_dialog)
                .setOnClickListener({ v -> showMultiDialog(activity) })

            activity.findViewById<View>(R.id.list_button_dialog)
                .setOnClickListener({ v -> showListButtonDialog(activity) })
            activity.findViewById<View>(R.id.list_button_content_dialog)
                .setOnClickListener({ v -> showListButtonContentDialog(activity) })

            activity.findViewById<View>(R.id.list_single_dialog)
                .setOnClickListener({ v -> showListSingleDialog(activity) })
            activity.findViewById<View>(R.id.list_single_button_dialog)
                .setOnClickListener({ v -> showListSingleButtonDialog(activity) })
        }

        /**
         * 小图标 + 标题 + 内容 + 单按钮
         */
        fun showSmallIconDialog(context: Activity) {
            DialogBuilderFactory.applySmallIconDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("这是标题文字")
                    .setContentText("告知当前状态，信息和解决方法")
                    .setIcon(R.drawable.dialog_small_icon_background)
                    .setPositiveText("确定")
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * 网络图标 + 标题 + 内容 + 单按钮
         */
        fun showNetSmallIconDialog(context: Activity) {
            DialogBuilderFactory.applySmallIconDialogStyle(
                SmartDialog.Builder(context)
                    .setIconUri(
                        Uri.parse(
                            "https://raw.githubusercontent.com/3HJack/plugin/master/dialog_net_icon_background.png"
                        )
                    )
                    .setTitleText("这是标题文字")
                    .setContentText("告知当前状态，信息和解决方法")
                    .setPositiveText("确定")
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * 大图标 + 标题 + 内容 + 单按钮
         */
        fun showBigIconDialog(context: Activity) {
            DialogBuilderFactory.applyBigIconDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("这是标题文字")
                    .setContentText("告知当前状态，信息和解决方法")
                    .setIcon(R.drawable.dialog_big_icon_background)
                    .setPositiveText("确定")
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * 大图标 + 标题 + 内容 + 双按钮
         */
        fun showBigIconTwoButtonDialog(context: Activity) {
            DialogBuilderFactory.applyBigIconDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("这是标题文字")
                    .setContentText("告知当前状态，信息和解决方法")
                    .setIcon(R.drawable.dialog_big_icon_background)
                    .setPositiveText("确定")
                    .setNegativeText("取消")
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * 简单list对话框
         */
        fun showSimpleListDialog(context: Activity) {
            val stringList = ArrayList<CharSequence>()
            stringList.add("告知当前状态，信息和解决方")
            stringList.add("法信息和解决方法")
            stringList.add("告知当前状态，信息和解决方法")
            stringList.add("告知当前状态，信息和解决方法")
            stringList.add("告知当前状态，信息和解决方法")
            DialogBuilderFactory.applyListDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("这是标题文字")
                    .setListItems(stringList)
                    .setPositiveText("确定")
                    .setNegativeText("取消")
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * 简单list对话框（内容可能多行），内容不可点
         */
        fun showSimpleListMultiLineDialog(context: Activity) {
            val stringList = ArrayList<CharSequence>()
            stringList.add("告知当前状态，信息和解决方法两行这样显示")
            stringList.add("告知当前状态，信息和解决方法两行这样显示显示")
            stringList.add("告知当前状态，信息和解决方法")
            stringList.add("告知当前状态，信息和解决方法")
            stringList.add("告知当前状态，信息和解决方法")
            stringList.add("告知当前状态，信息和解决方法两行这样显示")
            stringList.add("告知当前状态，信息和解决方法两行这样显示")
            stringList.add("告知当前状态，信息和解决方法两行这样显示")
            DialogBuilderFactory.applyListDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("这是标题文字")
                    .setListItems(stringList)
                    .setPositiveText("确定")
                    .setNegativeText("取消")
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * list多选框
         */
        fun showMultiDialog(context: Activity) {
            val stringList = ArrayList<CharSequence>()
            stringList.add("选项一")
            stringList.add("选项二")
            stringList.add("选项三")
            stringList.add("选项四")
            DialogBuilderFactory.applyListMultiDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("这是标题文字")
                    .setListItems(stringList)
                    .itemsCallbackMultiChoice(null,
                        object : SmartDialogInterface.ListCallbackMultiChoice {
                            override fun onSelection(dialog: SmartDialog, position: List<Int>) {
                                for (integer in position) {
                                    Log.e(TAG, "position $integer")
                                }
                            }
                        }).setPositiveText("确定")
                    .setNegativeText("取消")
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * list多button对话框，没有图标，上下布局，无内容
         */
        fun showListButtonDialog(context: Activity) {
            val stringList = ArrayList<CharSequence>()
            stringList.add("选项一")
            stringList.add("选项二")
            stringList.add("选项三")
            DialogBuilderFactory.applyListButtonDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("告知当前状态，信息和解决方案如果文字换行的情况")
                    .setListItems(stringList)
                    .setSelectedIndex(1)
                    .setItemsCallback(
                        object : SmartDialogInterface.ListCallback {
                            override fun onSelection(
                                dialog: SmartDialog,
                                itemView: View?,
                                position: Int
                            ) {
                                Log.e(
                                    TAG,
                                    "showListButtonDialog $position"
                                )
                            }

                        })
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * list多button对话框，没有图标，上下布局，有内容
         */
        fun showListButtonContentDialog(context: Activity) {
            val stringList = ArrayList<CharSequence>()
            stringList.add("选项一")
            stringList.add("选项二")
            stringList.add("选项三")
            DialogBuilderFactory.applyListButtonDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("这是标题文字")
                    .setContentText("告知当前状态，信息和解决方法如果文字换行的情况")
                    .setListItems(stringList)
                    .setSelectedIndex(1)
                    .setItemsCallback(
                        object : SmartDialogInterface.ListCallback {
                            override fun onSelection(
                                dialog: SmartDialog,
                                itemView: View?,
                                position: Int
                            ) {
                                Log.e(
                                    TAG,
                                    "showListButtonContentDialog $position"
                                )
                            }

                        })
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * list单选对话框，无按钮
         */
        fun showListSingleDialog(context: Activity) {
            val stringList = ArrayList<CharSequence>()
            stringList.add("user03@gmail.com")
            stringList.add("user03@gmail.com")
            stringList.add("user03@gmail.com")
            DialogBuilderFactory.applyListSingleDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("这是标题文字")
                    .setListItems(stringList)
                    .setSelectedIndex(1)
                    .setItemsCallback(
                        object : SmartDialogInterface.ListCallback {
                            override fun onSelection(
                                dialog: SmartDialog,
                                itemView: View?,
                                position: Int
                            ) {
                                Log.e(
                                    TAG,
                                    "showListButtonContentDialog $position"
                                )
                            }
                        })
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * list单选对话框，有按钮，图标在右边
         */
        fun showListSingleButtonDialog(context: Activity) {
            val stringList = ArrayList<CharSequence>()
            stringList.add("选项一")
            stringList.add("选项二")
            stringList.add("选项三")
            stringList.add("选项四")
            DialogBuilderFactory.applyListSingleButtonDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("这是标题文字")
                    .setListItems(stringList)
                    .setSelectedIndex(1)
                    .setItemsCallback(
                        object : SmartDialogInterface.ListCallback {
                            override fun onSelection(
                                dialog: SmartDialog,
                                itemView: View?,
                                position: Int
                            ) {
                                Log.e(
                                    TAG,
                                    "showListSingleButtonDialog $position"
                                )
                            }

                        })
                    .setPositiveText("确定")
                    .setNegativeText("取消")
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * 单行标题 + 单按钮
         */
        fun showSimpleNoContentDialog(context: Activity) {
            DialogBuilderFactory.applySimpleDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("告知当前状态和解决方案")
                    .setPositiveText("确定")
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * 多行标题 + 单按钮
         */
        fun showSimpleMultiTitleNoContentDialog(context: Activity) {
            DialogBuilderFactory.applySimpleDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("告知当前状态，信息和解决方案如果文字换行的情况")
                    .setPositiveText("确定")
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * 标题 + 内容 + 细节 + 双按钮
         */
        fun showSimpleTitleContentDetailDialog(context: Activity) {
            DialogBuilderFactory.applySimpleDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("这是标题文字")
                    .setContentText("告知当前状态，信息和解决方法")
                    .setDetailText("告知当前状态，信息和解决方法告知当前状态，信息和解决方法告知当前状态，信息和解决方法")
                    .setPositiveText("确定")
                    .setNegativeText("取消")
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * 标题 + 内容 + 输入框 + 双按钮
         */
        fun showInputDialog(context: Activity) {
            DialogBuilderFactory.applyInputDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("这是标题文字")
                    .setContentText("告知当前状态，信息和解决方法")
                    .setPositiveText("确定")
                    .setNegativeText("取消")
                    .setInput("默认文案", null, object : SmartDialogInterface.InputCallback {
                        override fun onInput(dialog: SmartDialog, view: View, input: CharSequence) {
                            Log.e(TAG, input.toString())
                        }
                    })
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * 多行标题 + 多行内容 + 单按钮
         */
        fun showSimpleMultiTitleContentDialog(context: Activity) {
            DialogBuilderFactory.applySimpleDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("这是标题文字标题文字如果两行这样显示")
                    .setContentText("告知当前状态，信息和解决方法如果文字换行的情况")
                    .setPositiveText("确定")
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * 单行标题 + 单行内容 + 双按钮
         */
        fun showSimpleTwoButtonDialog(context: Activity) {
            DialogBuilderFactory.applySimpleDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("这是标题文字")
                    .setContentText("告知当前状态，信息和解决方法")
                    .setPositiveText("确定")
                    .setNegativeText("取消")
                    .onPositive(object : SmartDialogInterface.ButtonCallback {
                        override fun onClick(dialog: SmartDialog, view: View) {
                            Log.e(TAG, "onPositive")
                        }
                    })
                    .onNegative(object : SmartDialogInterface.ButtonCallback {
                        override fun onClick(dialog: SmartDialog, view: View) {
                            Log.e(TAG, "onNegative")
                        }
                    })
                    .setOnCancelListener(object : PopupInterface.OnCancelListener {
                        override fun onCancel(popup: Popup, cancelType: Int) {
                            Log.e(TAG, "onCancel")
                        }
                    }) as SmartDialog.Builder
            )
                .show(object : PopupInterface.OnVisibilityListener {
                    override fun onShow(popup: Popup) {
                        Log.e(TAG, "onShow")
                    }

                    override fun onDismiss(popup: Popup, dismissType: Int) {
                        Log.e(TAG, "onDismiss $dismissType")
                    }
                })
        }

        /**
         * 单行标题 + 多行内容 + 单按钮
         */
        fun showSimpleMultiContentDialog(context: Activity) {
            DialogBuilderFactory.applySimpleDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("这是标题文字")
                    .setContentText("告知当前状态，信息和解决方法如果文字换行的情况")
                    .setPositiveText("确定")
            )
                .show(PopupInterface.EMPTY_VISIBILITY_LISTENER)
        }

        /**
         * 单行标题 + 单行内容 + 单按钮
         */
        fun showSimpleDialog(context: Activity, extra: String) {
            DialogBuilderFactory.applySimpleDialogStyle(
                SmartDialog.Builder(context)
                    .setTitleText("(无 id）标题")
                    .setContentText(extra)
                    .setPositiveText("确定")
                    .onPositive(object : SmartDialogInterface.ButtonCallback {
                        override fun onClick(dialog: SmartDialog, view: View) {
                            Log.e(TAG, "onPositive")
                        }
                    })
                    .setOnCancelListener(object : PopupInterface.OnCancelListener {
                        override fun onCancel(popup: Popup, cancelType: Int) {
                            Log.e(
                                TAG,
                                "onCancel $cancelType"
                            )
                        }
                    }) as SmartDialog.Builder
            )
                .show(object : PopupInterface.OnVisibilityListener {
                    override fun onDismiss(popup: Popup, dismissType: Int) {
                        Log.e(TAG, "onDismiss $dismissType")
                    }
                })
        }
    }


}
