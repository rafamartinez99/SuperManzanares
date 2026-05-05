package es.iessaladillo.rafamartinez.supermanzanares.data.repository

import es.iessaladillo.rafamartinez.supermanzanares.data.model.MapboxResponse
import es.iessaladillo.rafamartinez.supermanzanares.data.remote.MapboxService
import javax.inject.Inject

class MapboxRepository @Inject constructor(
    private val api: MapboxService
) {
    suspend fun getSuggestions(query: String, location: Pair<Double, Double>?): List<String> {
        val proximity = location?.let { "${it.second},${it.first}" } // lng,lat
        val response = api.getSuggestions(query, MAPBOX_TOKEN, proximity)
        return response.features.map { it.placeName }
    }

    suspend fun reverseGeocode(lat: Double, lon: Double): String? {
        return try {
            val response = api.reverseGeocode(
                latitude = lat,
                longitude = lon,
                accessToken = MAPBOX_TOKEN
            )
            response.features.firstOrNull()?.placeName
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun forwardGeocode(query: String): MapboxResponse = api.getSuggestions(query, MAPBOX_TOKEN, null)


    companion object {
        private const val MAPBOX_TOKEN =
            "pk.eyJ1IjoibGVhZmFyaXRvIiwiYSI6ImNtYWR1dnZoMDAwdWoyanNnM2kxM2p2b3oifQ.obquiFMyrZA8lW5qXqXoxA" // reemplaza con el tuyo
    }
}