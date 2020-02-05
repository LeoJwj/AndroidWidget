package com.leo.androidwidget.toast

class RealInterceptorChain(val mInterceptors: List<Interceptor>, var mRequest: SmartToast.Builder) :
    Interceptor.Chain {

    private var mIndex = 0

    override fun request(): SmartToast.Builder {
        return mRequest
    }

    override fun proceed(request: SmartToast.Builder): SmartToast.Builder {
        if (mIndex >= mInterceptors.size) {
            return request
        }
        mRequest = request
        val interceptor = mInterceptors[mIndex++]
        check(mIndex == mInterceptors.size) { "interceptor $interceptor must call proceed() exactly once" }
        return interceptor.intercept(this)
    }

}