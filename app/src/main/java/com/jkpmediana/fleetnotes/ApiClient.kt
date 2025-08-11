package com.jkpmediana.fleetnotes

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor(BuildConfig.API_KEY))
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://po.jkpmediana.rs/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
}
