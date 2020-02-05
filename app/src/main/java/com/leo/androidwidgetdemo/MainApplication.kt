package com.leo.androidwidgetdemo

import android.app.Application
import com.leo.androidwidget.AndroidWidget

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidWidget.init(AndroidWidget.Config(this))
    }
}