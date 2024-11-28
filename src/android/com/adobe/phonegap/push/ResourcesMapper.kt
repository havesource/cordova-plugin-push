package com.adobe.phonegap.push

import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorRes
import androidx.annotation.StringRes

/*
* Resources mapper class to get resources id with variable project package name
* */
object ResourcesMapper {

    fun getId(context: Context, name: String): Int {
        return getResId(context, name, ResourcesKeys.RES_TYPE_ID)
    }

    fun getLayout(context: Context, name: String): Int {
        return getResId(context, name, ResourcesKeys.RES_TYPE_LAYOUT)
    }

    fun getString(context: Context, name: String): Int {
        return getResId(context, name, ResourcesKeys.RES_TYPE_STRING)
    }

    fun getDrawable(context: Context, name: String): Int {
        return getResId(context, name, ResourcesKeys.RES_TYPE_DRAWABLE)
    }

    fun getColor(context: Context, name: String): Int {
        return getResId(context, name, ResourcesKeys.RES_TYPE_COLOR)
    }

    fun getResId(context: Context, name: String, type: String): Int {
        val packageName = context.packageName
        return context.resources.getIdentifier(name, type, packageName)
    }

    fun getActionText(context: Context, @StringRes stringRes: Int, @ColorRes colorRes: Int): Spannable? {
        val spannable: Spannable = SpannableString(context.getText(stringRes))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            spannable.setSpan(
                ForegroundColorSpan(context.getColor(colorRes)), 0, spannable.length, 0
            )
        }
        return spannable
    }
}
