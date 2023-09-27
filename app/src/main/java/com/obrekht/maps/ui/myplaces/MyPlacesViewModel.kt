package com.obrekht.maps.ui.myplaces

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

class MyPlacesViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: PlaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPlacesUiState())
    val uiState: StateFlow<MyPlacesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getPlaceListStream().collect { placeList ->
                _uiState.update { it.copy(placeList = placeList) }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val repository = (this[APPLICATION_KEY] as MyPlacesApplication).placeRepository
                MyPlacesViewModel(savedStateHandle, repository)
            }
        }
    }
}

data class MyPlacesUiState(
    val placeList: List<Place> = emptyList()
)