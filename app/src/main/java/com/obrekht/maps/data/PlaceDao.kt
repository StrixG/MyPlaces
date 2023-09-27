package com.obrekht.maps.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.obrekht.maps.data.entity.PlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Query("SELECT * FROM place")
    fun observeAll(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM place")
    suspend fun getAll(): List<PlaceEntity>

    @Query("SELECT * FROM place WHERE id = :placeId")
    suspend fun getById(placeId: Long): PlaceEntity?

    @Upsert
    suspend fun upsert(place: PlaceEntity): Long

    @Upsert
    suspend fun upsert(placeList: List<PlaceEntity>)

    @Insert
    suspend fun insert(place: PlaceEntity): Long

    @Query("DELETE FROM place WHERE id = :placeId")
    suspend fun deleteById(placeId: Long)

    @Query("DELETE FROM place")
    suspend fun deleteAll()
}