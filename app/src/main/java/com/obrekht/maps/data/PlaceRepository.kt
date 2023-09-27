package com.obrekht.maps.data

import com.obrekht.maps.model.Place
import kotlinx.coroutines.flow.Flow

interface PlaceRepository {

    fun getPlaceListStream(): Flow<List<Place>>
    suspend fun getAll(): List<Place>
    suspend fun getById(placeId: Long): Place?
    suspend fun save(place: Place): Place?
    suspend fun deleteById(placeId: Long)
    suspend fun delete(place: Place)
}