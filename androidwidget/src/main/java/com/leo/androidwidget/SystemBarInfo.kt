package com.leo.androidwidget

import android.os.Parcel
import android.os.Parcelable

class SystemBarInfo() : Parcelable {

    var mIsExist: Boolean = false
    var mHeight: Int = 0

    constructor(parcel: Parcel) : this() {
        mIsExist = parcel.readByte() != 0.toByte()
        mHeight = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (mIsExist) 1 else 0)
        parcel.writeInt(mHeight)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SystemBarInfo> {
        override fun createFromParcel(parcel: Parcel): SystemBarInfo {
            return SystemBarInfo(parcel)
        }

        override fun newArray(size: Int): Array<SystemBarInfo?> {
            return arrayOfNulls(size)
        }
    }


}