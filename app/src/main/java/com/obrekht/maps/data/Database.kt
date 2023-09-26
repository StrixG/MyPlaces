package com.obrekht.maps.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.obrekht.maps.data.entity.PlacemarkEntity

@Database(entities = [PlacemarkEntity::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun placemarkDao(): PlacemarkDao
}