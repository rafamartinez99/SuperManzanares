package es.iessaladillo.rafamartinez.supermanzanares.data.remote

import es.iessaladillo.rafamartinez.supermanzanares.data.model.MapboxResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MapboxService {
    @GET("geocoding/v5/mapbox.places/{query}.json")
    suspend fun getSuggestions(
        @Path("query") query: String,
        @Query("access_token") accessToken: String,
        @Query("proximity") proximity: String?,
        @Query("autocomplete") autocomplete: Boolean = true,
        @Query("limit") limit: Int = 5,
        @Query("language") language: String = "es"
    ): MapboxResponse

    @GET("geocoding/v5/mapbox.places/{lon},{lat}.json")
    suspend fun reverseGeocode(
        @Path("lon") longitude: Double,
        @Path("lat") latitude: Double,
        @Query("access_token") accessToken: String,
        @Query("limit") limit: Int = 1,
        @Query("language") language: String = "es"
    ): MapboxResponse
}