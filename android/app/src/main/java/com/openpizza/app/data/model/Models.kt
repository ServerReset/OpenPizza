package com.openpizza.app.data.model

import com.google.gson.annotations.SerializedName

data class NearbyRequest(val address: String)

data class NearbyResponse(val stores: List<StoreInfo>)

data class StoreInfo(
    val id: String,
    val name: String,
    val address: String?,
    val city: String?,
    val state: String?,
    val zip: String?,
    val phone: String?,
    val hours: String?,
    val isDelivery: Boolean,
    val isOpen: Boolean,
    val isOnlineCapable: Boolean,
    val serviceOpen: ServiceOpen?,
    val distance: Double?,
    val lat: Double?,
    val lng: Double?
)

data class ServiceOpen(
    val Delivery: Boolean?,
    val Carryout: Boolean?
)

data class MenuResponse(
    val products: List<MenuItem>?,
    val coupons: List<Any>?
)

data class MenuItem(
    val ProductCode: String?,
    val Name: String?,
    val Description: String?,
    val Price: String?,
    val ProductType: String?
)

data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val description: String
)

data class CategoriesResponse(val categories: List<Category>)

data class PizzaOptions(
    val X: Map<String, String> = emptyMap(),
    val C: Map<String, String> = emptyMap(),
    val P: Map<String, String> = emptyMap(),
    val S: Map<String, String> = emptyMap(),
    val M: Map<String, String> = emptyMap(),
    val O: Map<String, String> = emptyMap(),
    val G: Map<String, String> = emptyMap(),
    val J: Map<String, String> = emptyMap(),
    val T: Map<String, String> = emptyMap(),
    val A: Map<String, String> = emptyMap(),
    val PL: Map<String, String> = emptyMap(),
    val D: Map<String, String> = emptyMap(),
    val Z: Map<String, String> = emptyMap(),
    val F: Map<String, String> = emptyMap(),
    val B: Map<String, String> = emptyMap(),
    val H: Map<String, String> = emptyMap(),
    val BN: Map<String, String> = emptyMap(),
    val CX: Map<String, String> = emptyMap(),
    val RC: Map<String, String> = emptyMap()
)

data class CustomerRequest(
    val address: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String
)

data class OrderItem(
    val code: String,
    val options: PizzaOptions = PizzaOptions()
)

data class ValidateRequest(
    val customer: CustomerRequest,
    val storeID: String,
    val items: List<OrderItem>
)

data class PlaceRequest(
    val customer: CustomerRequest,
    val storeID: String,
    val items: List<OrderItem>,
    val payment: PaymentInfo
)

data class PaymentInfo(
    val number: String,
    val expiration: String,
    val securityCode: String,
    val postalCode: String,
    val tipAmount: Double = 0.0
)

data class ValidateResponse(
    val validated: Boolean?,
    val amounts: AmountsBreakdown?,
    val order: Any?
)

data class AmountsBreakdown(
    val customer: Double?,
    val tax: Double?,
    val total: Double?
)

data class PlaceResponse(
    val placed: Boolean?,
    val amounts: AmountsBreakdown?,
    val order: Any?
)

data class TrackingRequest(val phone: String)

data class TrackingResponse(val tracking: Any?)

data class CartItem(
    val code: String,
    val name: String,
    val price: Double,
    var qty: Int = 1,
    val description: String = "",
    val toppings: String = ""
)

data class StoreInfoResponse(
    val id: Int?,
    val name: String?
)

enum class CrustType(val code: String, val displayName: String) {
    HAND_TOSSED("SCREEN", "Hand Tossed"),
    THIN("THIN", "Thin Crust"),
    PAN("PAN", "Pan"),
    BROOKLYN("BROOKLYN", "Brooklyn Style"),
    GLUTEN_FREE("GLUTENF", "Gluten Free")
}

enum class PizzaSize(val code: String, val displayName: String, val basePrice: Double) {
    SMALL("10", "Small (10\")", 9.99),
    MEDIUM("12", "Medium (12\")", 12.49),
    LARGE("14", "Large (14\")", 14.99),
    XLARGE("16", "X-Large (16\")", 17.49)
}

data class SauceOption(val name: String, val value: String)
data class CheeseOption(val name: String, val value: String)

data class ToppingOption(val code: String, val name: String)

data class PizzaBuildState(
    val storeId: String = "",
    val crust: CrustType? = null,
    val size: PizzaSize? = null,
    val sauce: SauceOption = SauceOption("Normal", "1"),
    val cheese: CheeseOption = CheeseOption("Normal", "1"),
    val toppings: List<ToppingOption> = emptyList()
)
