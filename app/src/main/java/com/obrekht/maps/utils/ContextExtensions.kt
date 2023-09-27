package com.obrekht.maps.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

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

fun Context.hasLocationPermission(): Boolean {
    return (ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED)
}

fun Fragment.hasLocationPermission(): Boolean = requireContext().hasLocationPermission()