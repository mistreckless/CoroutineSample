package com.mistreckless.coroutinesample.service

import com.mistreckless.coroutinesample.data.SearchVenuesResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface FoursquareApiService {

    @GET("/v2/venues/search")
    fun fetchVenues(@Query("ll") ll: String, @Query("radius") radius: Int, @Query("categoryId") categoryId: String): Deferred<SearchVenuesResponse>
}