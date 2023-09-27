package com.obrekht.maps.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.obrekht.maps.model.Place

@Entity("place")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val description: String = "",
    val latitude: Double,
    val longitude: Double
)

fun PlaceEntity.toModel() = Place(id, name, description, latitude, longitude)
fun Place.toEntity() = PlaceEntity(id, name, description, latitude, longitude)

@JvmName("entityToModel")
fun List<PlaceEntity>.toModel() = map(PlaceEntity::toModel)
@JvmName("modelToEntity")
fun List<Place>.toEntity() = map(Place::toEntity)