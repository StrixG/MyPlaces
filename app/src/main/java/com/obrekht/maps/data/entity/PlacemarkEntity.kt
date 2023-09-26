package com.obrekht.maps.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("placemark")
data class PlacemarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val description: String = "",
    val latitude: Double,
    val longitude: Double
)
