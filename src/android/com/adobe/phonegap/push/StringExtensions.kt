package com.adobe.phonegap.push

import android.text.Spanned
import androidx.core.text.HtmlCompat

fun String.fromHtml(): Spanned {
    return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun String.convertToTypedArray(): Array<String> {
    return this.replace("\\[".toRegex(), "")
        .replace("]".toRegex(), "")
        .split(",")
        .toTypedArray()
}
