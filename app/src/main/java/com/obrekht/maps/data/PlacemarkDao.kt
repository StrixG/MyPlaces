package com.obrekht.maps.data

import androidx.room.Query
import androidx.room.Upsert
import com.obrekht.maps.data.entity.PlacemarkEntity

interface PlacemarkDao {

    @Query("SELECT * FROM placemark")
    suspend fun getAll()

    @Query("SELECT * FROM placemark WHERE id = :placemarkId")
    suspend fun getById(placemarkId: Long): PlacemarkEntity?

    @Upsert
    suspend fun upsert(placemark: PlacemarkEntity)

    @Upsert
    suspend fun upsert(placemarkList: List<PlacemarkEntity>)

    @Query("DELETE FROM placemark WHERE id = :placemarkId")
    suspend fun deleteById(placemarkId: Long)

    @Query("DELETE FROM placemark")
    suspend fun deleteAll()
}