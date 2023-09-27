package com.obrekht.maps.data

import com.obrekht.maps.data.entity.toEntity
import com.obrekht.maps.data.entity.toModel
import com.obrekht.maps.model.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultPlaceRepository(
    private val database: PlaceDatabase
) : PlaceRepository {

    private val placeDao = database.placeDao()

    override fun getPlaceListStream(): Flow<List<Place>> {
        return placeDao.observeAll().map { it.toModel() }
    }

    override suspend fun getAll(): List<Place> {
        return placeDao.getAll().toModel()
    }

    override suspend fun getById(placeId: Long): Place? {
        return placeDao.getById(placeId)?.toModel()
    }

    override suspend fun save(place: Place): Place? {
        val id = placeDao.upsert(place.toEntity())
        return placeDao.getById(id)?.toModel()
    }

    override suspend fun deleteById(placeId: Long) = placeDao.deleteById(placeId)

    override suspend fun delete(place: Place) = placeDao.deleteById(place.id)
}