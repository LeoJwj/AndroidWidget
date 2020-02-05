package com.leo.androidwidget.popup

import android.app.Activity
import android.text.TextUtils
import java.util.*
import kotlin.collections.ArrayList

class DefaultPopupManager : PopupInterface.PopupManager {
    private val POPUP_MAP_LIST = WeakHashMap<Activity, ArrayList<Popup>>()
    override fun enableShowNow(activity: Activity, popup: Popup): Boolean {
        return if (isEmpty(activity) || popup.getExcluded() === PopupInterface.Excluded.NOT_AGAINST) {
            true
        } else if (popup.getExcluded() === PopupInterface.Excluded.ALL_TYPE) {
            false
        } else {
            var existSameTypePopup = false
            for (popupItem in getPopupList(activity)) {
                if (TextUtils.equals(popupItem.getPopupType(), popup.getPopupType())) {
                    existSameTypePopup = true
                    break
                }
            }
            !existSameTypePopup
        }
    }

    override fun onPopupShow(activity: Activity, popup: Popup) {
        put(activity, popup)
    }

    override fun onPopupDismiss(activity: Activity, popup: Popup) {
        remove(activity, popup)
        val pendingPopup = getNextPendingPopup(activity)
        pendingPopup?.show()
    }

    override fun onPopupPending(activity: Activity, popup: Popup) {
        put(activity, popup)
    }

    override fun onPopupDiscard(activity: Activity, popup: Popup) {
        remove(activity, popup)
    }

    override fun onActivityDestroy(activity: Activity) {
        clear(activity)
    }

    fun getNextPendingPopup(activity: Activity): Popup? {
        var popupList: List<Popup>? = getShowingPopupList(activity)
        if (popupList!!.isNotEmpty()) {
            for (popup in popupList) {
                if (!Popup.isPermanentPopup(popup)) {
                    return null
                }
            }
        }
        popupList = POPUP_MAP_LIST[activity]
        if (popupList == null || popupList.isEmpty() || activity.isFinishing) {
            return null
        }
        for (popup in popupList) {
            if (!popup.isShowing()) {
                popupList.remove(popup)
                return popup
            }
        }
        return null
    }

    private fun put(activity: Activity, popup: Popup) {
        var popupList: MutableList<Popup>? = POPUP_MAP_LIST[activity]
        if (popupList == null) {
            popupList = ArrayList()
            POPUP_MAP_LIST[activity] = popupList
        }
        if (!popupList.contains(popup)) {
            popupList.add(popup)
        }
    }

    private fun remove(activity: Activity, popup: Popup) {
        val popupList = POPUP_MAP_LIST[activity]
        popupList?.remove(popup)
    }

    private fun clear(activity: Activity) {
        val popupList = POPUP_MAP_LIST.remove(activity)
        if (popupList != null) {
            for (popup in popupList) {
                if (popup.isShowing()) {
                    popup.dismiss(PopupInterface.CLOSE_TYPE_AUTO)
                } else {
                    popup.discard()
                }
            }
        }
    }

    fun isEmpty(activity: Activity): Boolean {
        val popupList = POPUP_MAP_LIST[activity]
        return popupList == null || popupList.isEmpty()
    }

    fun getPopupByTag(activity: Activity, tag: Any?): Popup? {
        val popupList = getPopupList(activity)
        if (popupList.isEmpty()) {
            return null
        }
        if (tag == null) {
            return popupList[0]
        }
        for (popup in popupList) {
            if (tag == popup.getTag()) {
                return popup
            }
        }
        return null
    }

    fun getPopupByType(activity: Activity, popupType: String): List<Popup> {
        val popupList = ArrayList<Popup>()
        if (isEmpty(activity)) {
            return popupList
        }
        for (popup in getPopupList(activity)) {
            if (TextUtils.equals(popupType, popup.getPopupType())) {
                popupList.add(popup)
            }
        }
        return Collections.unmodifiableList(popupList)
    }

    private fun getShowingPopupList(activity: Activity): List<Popup> {
        val popupList = ArrayList<Popup>()
        if (isEmpty(activity)) {
            return popupList
        }
        for (popup in getPopupList(activity)) {
            if (popup.isShowing()) {
                popupList.add(popup)
            }
        }
        return Collections.unmodifiableList(popupList)
    }

    fun getPopupList(activity: Activity): List<Popup> {
        var popupList: List<Popup>? = POPUP_MAP_LIST[activity]
        if (popupList == null) {
            popupList = ArrayList()
        }
        return Collections.unmodifiableList(popupList)
    }

}