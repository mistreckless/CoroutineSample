package com.mistreckless.coroutinesample.data

data class SearchVenuesResponse(val response: Response = Response())

data class Response(val venues: List<VenuesItem> = emptyList())

data class VenuesItem(
    val name: String = "",
    val location: Location? = null,
    val id: String = ""
)

data class Location(
    val cc: String = "",
    val country: String = "",
    val address: String = "",
    val lng: Double = 0.0,
    val distance: Int = 0,
    val formattedAddress: List<String> = emptyList(),
    val lat: Double = 0.0
)