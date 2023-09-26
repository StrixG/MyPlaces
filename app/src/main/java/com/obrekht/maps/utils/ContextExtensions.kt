package com.obrekht.maps.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

fun Context.createBitmapFromVector(
    @DrawableRes resourceId: Int,
    @ColorRes color: Int? = null,
    width: Int? = null,
    height: Int? = null,
): Bitmap? {
    val drawable = ContextCompat.getDrawable(this, resourceId) ?: return null

    val bitmap = Bitmap.createBitmap(
        width ?: drawable.intrinsicWidth,
        height ?: drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    if (color != null) {
        drawable.setTint(ContextCompat.getColor(this, color))
    }
    drawable.draw(canvas)

    return bitmap
}