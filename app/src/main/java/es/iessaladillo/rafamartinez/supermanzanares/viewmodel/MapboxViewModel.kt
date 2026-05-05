package es.iessaladillo.rafamartinez.supermanzanares.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.MapboxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapboxViewModel @Inject constructor(
    private val repository: MapboxRepository
) : ViewModel() {

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> get() = _suggestions

    fun fetchSuggestions(query: String, location: Pair<Double, Double>?) {
        viewModelScope.launch {
            try {
                val results = repository.getSuggestions(query, location)
                _suggestions.value = results
            } catch (_: Exception) {
                _suggestions.value = emptyList()
            }
        }
    }

    fun clearSuggestions() {
        _suggestions.value = emptyList()
    }

    suspend fun reverseGeocode(lat: Double, lon: Double): String? {
        return repository.reverseGeocode(lat, lon)
    }

    suspend fun geocode(query: String): Point? {
        return try {
            val response = repository.forwardGeocode(query)
            val coords = response.features.firstOrNull()?.geometry?.coordinates
            coords?.let { Point.fromLngLat(it[0], it[1]) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
