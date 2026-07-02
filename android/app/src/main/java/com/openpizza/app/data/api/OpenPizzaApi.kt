package com.openpizza.app.data.api

import com.openpizza.app.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface OpenPizzaApi {

    @POST("api/stores/nearby")
    suspend fun getNearbyStores(@Body request: NearbyRequest): NearbyResponse

    @GET("api/stores/{id}/menu")
    suspend fun getStoreMenu(@Path("id") storeId: String): MenuResponse

    @GET("api/stores/{id}/info")
    suspend fun getStoreInfo(@Path("id") storeId: String): StoreInfoResponse

    @POST("api/order/validate")
    suspend fun validateOrder(@Body request: ValidateRequest): ValidateResponse

    @POST("api/order/place")
    suspend fun placeOrder(@Body request: PlaceRequest): PlaceResponse

    @POST("api/tracking")
    suspend fun trackOrder(@Body request: TrackingRequest): TrackingResponse

    @GET("api/menu/categories")
    suspend fun getCategories(): CategoriesResponse

    companion object {
        fun create(baseUrl: String = "http://10.0.2.2:3000/"): OpenPizzaApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenPizzaApi::class.java)
        }
    }
}
