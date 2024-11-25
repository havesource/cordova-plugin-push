package com.adobe.phonegap.push

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable

object UiUtils {

    fun getTintDrawable(drawable: Drawable?, color: Int): Drawable? {
        return getTintDrawable(drawable, color, PorterDuff.Mode.SRC_IN)
    }

    private fun getTintDrawable(drawable: Drawable?, color: Int, mode: PorterDuff.Mode): Drawable? {
        val newDrawable = drawable?.mutate()
        newDrawable?.setColorFilter(color, mode)
        return newDrawable
    }
}
