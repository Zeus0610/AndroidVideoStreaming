package com.zeus.videostreaming.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity

internal fun Context.findActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) {
            return context
        }
        context = context.baseContext
    }
    throw IllegalStateException()
}
