package com.leo.androidwidget.toast

interface Interceptor {
    fun intercept(chain: Chain): SmartToast.Builder

    interface Chain {
        fun request(): SmartToast.Builder

        fun proceed(request: SmartToast.Builder): SmartToast.Builder
    }
}