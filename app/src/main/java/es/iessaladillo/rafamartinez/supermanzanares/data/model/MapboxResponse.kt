package es.iessaladillo.rafamartinez.supermanzanares.data.model

import com.google.gson.annotations.SerializedName

data class MapboxResponse(
    val features: List<Feature>
)

data class Feature(
    @SerializedName("place_name")
    val placeName: String,
    val geometry: Geometry?
)

data class Geometry(
    val type: String,
    val coordinates: List<Double>
)

