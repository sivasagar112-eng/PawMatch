package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface DogApiService {
    @GET("https://dog.ceo/api/breeds/list/all")
    suspend fun getAllBreeds(): Response<DogBreedsResponse>

    @GET("https://dog.ceo/api/breed/{breed}/images/random")
    suspend fun getRandomBreedImage(@Path("breed") breed: String): Response<DogImageResponse>

    @GET("https://dog-api.kinduff.com/api/facts")
    suspend fun getDogFacts(@Query("number") count: Int): Response<DogFactsResponse>
}

data class DogBreedsResponse(
    val message: Map<String, List<String>>,
    val status: String
)

data class DogImageResponse(
    val message: String,
    val status: String
)

data class DogFactsResponse(
    val facts: List<String>,
    val success: Boolean
)

object RetrofitClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val dogApiService: DogApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://dog.ceo/api/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(DogApiService::class.java)
    }
}
