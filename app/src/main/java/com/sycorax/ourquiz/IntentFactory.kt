package com.sycorax.ourquiz

import android.content.Context
import android.content.Intent

open class IntentFactory {
    open fun <T>create(context: Context, activityClass: Class<T>): Intent {
        return Intent(context, activityClass)
    }
}
