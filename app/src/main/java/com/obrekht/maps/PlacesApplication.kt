package com.obrekht.maps

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class PlacesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAPS_API_KEY)
    }
}