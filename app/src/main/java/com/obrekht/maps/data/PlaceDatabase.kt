package com.obrekht.maps.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.obrekht.maps.data.entity.PlaceEntity

@Database(entities = [PlaceEntity::class], version = 1)
abstract class PlaceDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
}