package com.obrekht.maps.ui.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.obrekht.maps.MyPlacesApplication
import com.obrekht.maps.data.PlaceRepository
import com.obrekht.maps.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val args = MapFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    init {
        loadPlace(args.placeId)
        viewModelScope.launch {
            placeRepository.getPlaceListStream().collect { placeList ->
                _uiState.update { uiState ->
                    uiState.copy(placeList = placeList)
                }
            }
        }
    }

    fun onCameraTargetChange(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }

    fun cameraMoved() {
        _uiState.update { it.copy(isCameraMoved = true) }
    }

    fun loadPlace(placeId: Long) {
        viewModelScope.launch {
            val place = placeRepository.getById(placeId)
            _uiState.update { it.copy(isCameraMoved = false, selectedPlace = place) }
        }
    }

    fun clearPlace() {
        _uiState.update { it.copy(isCameraMoved = false, selectedPlace = null) }
    }

    fun savePlace(name: String, description: String) {
        if (name.isBlank()) return

        val trimmedName = name.trim()
        val trimmedDescription = description.trim()

        viewModelScope.launch {
            val place = _uiState.value.selectedPlace?.copy(
                name = trimmedName,
                description = trimmedDescription
            ) ?: Place(0, trimmedName, trimmedDescription, latitude, longitude)

            placeRepository.save(place)
        }
    }

    fun deletePlace(placeId: Long) {
        viewModelScope.launch {
            placeRepository.deleteById(placeId)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val repository = (this[APPLICATION_KEY] as MyPlacesApplication).placeRepository
                MapViewModel(savedStateHandle, repository)
            }
        }
    }
}

data class MapUiState(
    val selectedPlace: Place? = null,
    val placeList: List<Place> = emptyList(),
    val isCameraMoved: Boolean = false
)
