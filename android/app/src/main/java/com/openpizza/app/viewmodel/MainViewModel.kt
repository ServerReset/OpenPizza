package com.openpizza.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.openpizza.app.data.api.OpenPizzaApi
import com.openpizza.app.data.model.*
import com.openpizza.app.data.repository.OpenPizzaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = OpenPizzaRepository(OpenPizzaApi.create())

    // Store
    private val _stores = MutableStateFlow<List<StoreInfo>>(emptyList())
    val stores: StateFlow<List<StoreInfo>> = _stores.asStateFlow()

    private val _storesLoading = MutableStateFlow(false)
    val storesLoading: StateFlow<Boolean> = _storesLoading.asStateFlow()

    private val _storesError = MutableStateFlow<String?>(null)
    val storesError: StateFlow<String?> = _storesError.asStateFlow()

    // Selected store
    private val _selectedStore = MutableStateFlow<StoreInfo?>(null)
    val selectedStore: StateFlow<StoreInfo?> = _selectedStore.asStateFlow()

    // Menu
    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    private val _menuLoading = MutableStateFlow(false)
    val menuLoading: StateFlow<Boolean> = _menuLoading.asStateFlow()

    // Categories
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    // Pizza Builder
    private val _buildState = MutableStateFlow(PizzaBuildState())
    val buildState: StateFlow<PizzaBuildState> = _buildState.asStateFlow()

    // Cart
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    // Order
    private val _orderResult = MutableStateFlow<PlaceResponse?>(null)
    val orderResult: StateFlow<PlaceResponse?> = _orderResult.asStateFlow()

    private val _orderLoading = MutableStateFlow(false)
    val orderLoading: StateFlow<Boolean> = _orderLoading.asStateFlow()

    // Tracking
    private val _trackingResult = MutableStateFlow<Any?>(null)
    val trackingResult: StateFlow<Any?> = _trackingResult.asStateFlow()

    private val _trackingLoading = MutableStateFlow(false)
    val trackingLoading: StateFlow<Boolean> = _trackingLoading.asStateFlow()

    // Profile
    private val _profile = MutableStateFlow(CustomerRequest("", "", "", "", ""))
    val profile: StateFlow<CustomerRequest> = _profile.asStateFlow()

    init {
        loadCategories()
    }

    fun searchStores(address: String) {
        viewModelScope.launch {
            _storesLoading.value = true
            _storesError.value = null
            repository.getNearbyStores(address)
                .onSuccess { _stores.value = it }
                .onFailure { _storesError.value = it.message }
            _storesLoading.value = false
        }
    }

    fun selectStore(store: StoreInfo) {
        _selectedStore.value = store
        loadMenu(store.id)
    }

    private fun loadMenu(storeId: String) {
        viewModelScope.launch {
            _menuLoading.value = true
            repository.getStoreMenu(storeId)
                .onSuccess { _menuItems.value = it.products ?: emptyList() }
            _menuLoading.value = false
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories()
                .onSuccess { _categories.value = it }
        }
    }

    fun getMenuItemsByCategory(categoryId: String): List<MenuItem> {
        val keyword = when (categoryId) {
            "pizza" -> ""
            "sandwiches" -> "SANDWICH"
            "pasta" -> "PASTA"
            "sides" -> "BREAD"
            "desserts" -> "COOKIE"
            "drinks" -> "2L"
            "wings" -> "WINGS"
            "salads" -> "SALAD"
            else -> ""
        }
        return if (keyword.isEmpty()) _menuItems.value.take(20)
        else _menuItems.value.filter { it.ProductCode?.contains(keyword, ignoreCase = true) == true }.take(20)
    }

    fun updateBuilderCrust(crust: CrustType) {
        _buildState.value = _buildState.value.copy(crust = crust)
    }

    fun updateBuilderSize(size: PizzaSize) {
        _buildState.value = _buildState.value.copy(size = size)
    }

    fun updateBuilderSauce(sauce: SauceOption) {
        _buildState.value = _buildState.value.copy(sauce = sauce)
    }

    fun updateBuilderCheese(cheese: CheeseOption) {
        _buildState.value = _buildState.value.copy(cheese = cheese)
    }

    fun toggleTopping(topping: ToppingOption) {
        val current = _buildState.value.toppings
        _buildState.value = _buildState.value.copy(
            toppings = if (current.any { it.code == topping.code })
                current.filter { it.code != topping.code }
            else current + topping
        )
    }

    fun getBuilderPrice(): Double {
        val state = _buildState.value
        val base = state.size?.basePrice ?: 14.99
        return base + state.toppings.size * 1.5
    }

    fun addBuilderToCart() {
        val state = _buildState.value
        val build = state.crust ?: return
        val size = state.size ?: return
        val code = size.code + build.code

        val item = CartItem(
            code = code,
            name = "${size.displayName} ${build.displayName} Pizza",
            price = getBuilderPrice(),
            toppings = state.toppings.joinToString(", ") { it.name }
        )
        _cart.value = _cart.value + item
        _buildState.value = PizzaBuildState(storeId = state.storeId)
    }

    fun addItemToCart(item: CartItem) {
        _cart.value = _cart.value + item
    }

    fun updateCartItemQty(index: Int, delta: Int) {
        val list = _cart.value.toMutableList()
        if (index in list.indices) {
            val newQty = list[index].qty + delta
            if (newQty > 0) list[index] = list[index].copy(qty = newQty)
            else list.removeAt(index)
            _cart.value = list
        }
    }

    fun removeFromCart(index: Int) {
        val list = _cart.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _cart.value = list
        }
    }

    fun getCartTotal(): Double {
        return _cart.value.sumOf { it.price * it.qty }
    }

    fun getCartTax(): Double = getCartTotal() * 0.08

    fun getCartGrandTotal(): Double = getCartTotal() + getCartTax()

    fun placeOrder(customer: CustomerRequest, storeId: String, payment: PaymentInfo) {
        viewModelScope.launch {
            _orderLoading.value = true
            val items = _cart.value.map { OrderItem(code = it.code) }
            repository.placeOrder(customer, storeId, items, payment)
                .onSuccess {
                    _orderResult.value = it
                    _cart.value = emptyList()
                }
                .onFailure { _orderResult.value = null }
            _orderLoading.value = false
        }
    }

    fun validateOrder(customer: CustomerRequest, storeId: String) {
        viewModelScope.launch {
            val items = _cart.value.map { OrderItem(code = it.code) }
            repository.validateOrder(customer, storeId, items)
        }
    }

    fun trackOrder(phone: String) {
        viewModelScope.launch {
            _trackingLoading.value = true
            repository.trackOrder(phone)
                .onSuccess { _trackingResult.value = it }
                .onFailure { _trackingResult.value = null }
            _trackingLoading.value = false
        }
    }

    fun updateProfile(profile: CustomerRequest) {
        _profile.value = profile
    }

    val crustOptions = CrustType.entries
    val sizeOptions = PizzaSize.entries
    val sauceOptions = listOf(
        SauceOption("Normal", "1"),
        SauceOption("Light", "0.5"),
        SauceOption("Extra", "1.5"),
        SauceOption("None", "0")
    )
    val cheeseOptions = listOf(
        CheeseOption("Normal", "1"),
        CheeseOption("Light", "0.5"),
        CheeseOption("Extra", "1.5"),
        CheeseOption("None", "0")
    )
    val toppingOptions = listOf(
        ToppingOption("P", "Pepperoni"),
        ToppingOption("S", "Italian Sausage"),
        ToppingOption("B", "Beef"),
        ToppingOption("H", "Ham"),
        ToppingOption("BN", "Bacon"),
        ToppingOption("M", "Mushrooms"),
        ToppingOption("O", "Onions"),
        ToppingOption("G", "Green Peppers"),
        ToppingOption("J", "Jalapeños"),
        ToppingOption("C", "Black Olives"),
        ToppingOption("T", "Tomatoes"),
        ToppingOption("A", "Anchovies"),
        ToppingOption("PL", "Pineapple"),
        ToppingOption("D", "Banana Peppers"),
        ToppingOption("Z", "Spinach"),
        ToppingOption("F", "Feta Cheese"),
        ToppingOption("CX", "Cheddar"),
        ToppingOption("RC", "Ricotta")
    )
}
