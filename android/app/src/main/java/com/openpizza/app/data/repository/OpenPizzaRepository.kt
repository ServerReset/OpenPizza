package com.openpizza.app.data.repository

import com.openpizza.app.data.api.OpenPizzaApi
import com.openpizza.app.data.model.*

class OpenPizzaRepository(private val api: OpenPizzaApi) {

    suspend fun getNearbyStores(address: String): Result<List<StoreInfo>> = runCatching {
        api.getNearbyStores(NearbyRequest(address)).stores
    }

    suspend fun getStoreMenu(storeId: String): Result<MenuResponse> = runCatching {
        api.getStoreMenu(storeId)
    }

    suspend fun getCategories(): Result<List<Category>> = runCatching {
        api.getCategories().categories
    }

    suspend fun validateOrder(
        customer: CustomerRequest,
        storeID: String,
        items: List<OrderItem>
    ): Result<ValidateResponse> = runCatching {
        api.validateOrder(ValidateRequest(customer, storeID, items))
    }

    suspend fun placeOrder(
        customer: CustomerRequest,
        storeID: String,
        items: List<OrderItem>,
        payment: PaymentInfo
    ): Result<PlaceResponse> = runCatching {
        api.placeOrder(PlaceRequest(customer, storeID, items, payment))
    }

    suspend fun trackOrder(phone: String): Result<Any?> = runCatching {
        api.trackOrder(TrackingRequest(phone)).tracking
    }
}
