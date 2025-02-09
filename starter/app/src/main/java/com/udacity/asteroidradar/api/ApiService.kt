package com.udacity.asteroidradar.api

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.BuildConfig
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.PictureOfDay
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {
    @GET("neo/rest/v1/feed")
    fun getAsteroidsAsync(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Deferred<ResponseBody>

    @GET("planetary/apod")
    fun getPictureOfDayAsync(): Deferred<PictureOfDay>
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val interceptor =
    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

private val client: OkHttpClient =
    OkHttpClient.Builder()
        .addInterceptor { chain ->
        val url = chain
            .request()
            .url
            .newBuilder()
            .addQueryParameter("api_key", BuildConfig.API_KEY)
            .build()
        chain.proceed(chain.request().newBuilder().url(url).build())
    }
        .addInterceptor(interceptor)
        .build()

object Network {
    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val service: ApiService = retrofit.create(ApiService::class.java)
}
