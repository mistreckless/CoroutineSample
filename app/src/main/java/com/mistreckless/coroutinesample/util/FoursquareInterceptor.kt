package com.mistreckless.coroutinesample.util

import com.mistreckless.coroutinesample.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class FoursquareInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val clientId = BuildConfig.ClientId
        val clientSecret = BuildConfig.ClientSecret
        val apiVersion = BuildConfig.ApiVersion
        val request = chain.request()
        val originalUrl = request.url()
        val url = originalUrl.newBuilder()
            .addQueryParameter("client_id", clientId)
            .addQueryParameter("client_secret", clientSecret)
            .addQueryParameter("v", apiVersion.toString())
            .build()

        val newRequest = request.newBuilder()
            .url(url)
            .build()

        return chain.proceed(newRequest)
    }
}