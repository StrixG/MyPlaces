package com.obrekht.maps

import android.app.Application
import androidx.room.Room
import com.obrekht.maps.data.DefaultPlaceRepository
import com.obrekht.maps.data.PlaceDatabase
import com.yandex.mapkit.MapKitFactory

class MyPlacesApplication : Application() {

    private val database by lazy {
        Room.databaseBuilder(
            this,
            PlaceDatabase::class.java,
            "my-places.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val placeRepository by lazy {
        DefaultPlaceRepository(database)
    }

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAPS_API_KEY)
    }
}