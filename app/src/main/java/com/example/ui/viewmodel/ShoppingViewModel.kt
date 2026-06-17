package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CartItem
import com.example.data.model.NotificationItem
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.User
import com.example.data.model.WishlistItem
import com.example.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShoppingRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ShoppingRepository(database.shoppingDao())
    }

    // --- Authentication State ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _registerError = MutableStateFlow<String?>(null)
    val registerError: StateFlow<String?> = _registerError.asStateFlow()

    private val _show2FAVerification = MutableStateFlow<User?>(null) // Holds user waiting for 2FA verification
    val show2FAVerification: StateFlow<User?> = _show2FAVerification.asStateFlow()

    private val _twoFAError = MutableStateFlow<String?>(null)
    val twoFAError: StateFlow<String?> = _twoFAError.asStateFlow()

    // --- Search & Filters State ---
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null)
    val priceFilterMax = MutableStateFlow(300.0)
    val sortBy = MutableStateFlow("Featured") // Featured, Price: Low-High, Price: High-Low, Highest Rated
    val currentPage = MutableStateFlow(1)
    val pageSize = 4 // Let's paginate 4 items per page to make pagination clear and functional

    // --- Products Flow ---
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Filtered and Paginated Products Flow (Optimal Rendering & Search) ---
    val filteredProductsState: StateFlow<FilteredProductsResult> = combine(
        combine(allProducts, searchQuery, selectedCategory) { p, q, c -> Triple(p, q, c) },
        combine(priceFilterMax, sortBy, currentPage) { pMax, s, cp -> Triple(pMax, s, cp) }
    ) { first, second ->
        val (products, query, category) = first
        val (maxPrice, sortOption, page) = second
        
        var list = products.filter { product ->
            val matchQuery = query.isEmpty() || product.name.contains(query, ignoreCase = true) || 
                             product.description.contains(query, ignoreCase = true)
            val matchCategory = category == null || product.category == category
            val matchPrice = product.price <= maxPrice
            matchQuery && matchCategory && matchPrice
        }

        // Apply sorting
        list = when (sortOption) {
            "Price: Low-High" -> list.sortedBy { it.price }
            "Price: High-Low" -> list.sortedByDescending { it.price }
            "Highest Rated" -> list.sortedByDescending { it.rating }
            else -> list.sortedByDescending { it.isFeatured } // Featured
        }

        val totalItems = list.size
        val totalPages = ((totalItems - 1) / pageSize + 1).coerceAtLeast(1)
        val adjustedPage = page.coerceIn(1, totalPages)
        
        // Paginate
        val startIdx = (adjustedPage - 1) * pageSize
        val paginatedList = if (startIdx < list.size) {
            list.subList(startIdx, (startIdx + pageSize).coerceAtMost(list.size))
        } else {
            emptyList()
        }

        FilteredProductsResult(
            products = paginatedList,
            totalItems = totalItems,
            totalPages = totalPages,
            currentPage = adjustedPage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FilteredProductsResult())

    // --- Active User Cart & Wishlist ---
    val cartItemsState: StateFlow<List<CartProductItem>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else {
                repository.getCartByUserId(user.id).flatMapLatest { cartList ->
                    repository.allProducts.map { products ->
                        cartList.mapNotNull { cartItem ->
                            val match = products.find { it.id == cartItem.productId }
                            match?.let { CartProductItem(cartItem, it) }
                        }
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistState: StateFlow<List<Product>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else {
                repository.getWishlistByUserId(user.id).flatMapLatest { wishlist ->
                    repository.allProducts.map { products ->
                        wishlist.mapNotNull { wishItem ->
                            products.find { it.id == wishItem.productId }
                        }
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active User Orders ---
    val userOrdersState: StateFlow<List<Order>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getOrdersByUserId(user.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ALL Orders (For Admin Dashboard Analytics) ---
    val allOrdersState: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Notifications State ---
    val notificationsState: StateFlow<List<NotificationItem>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getNotificationsByUserId(user.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Store Analytics State ---
    val analyticsState: StateFlow<StoreAnalytics> = combine(
        allOrdersState,
        allProducts
    ) { orders, products ->
        var totalSales = 0.0
        val categorySales = mutableMapOf<String, Double>()
        val productStockCount = products.sumOf { it.stock }
        val categoryStockCount = products.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.stock } }

        orders.forEach { order ->
            totalSales += order.totalAmount
            // Parse custom formatted items string: name|price|quantity|imageUrl
            if (order.itemsJson.isNotEmpty()) {
                order.itemsJson.split(";;").forEach { itemStr ->
                    val parts = itemStr.split("|")
                    if (parts.size >= 3) {
                        val name = parts[0]
                        val price = parts[1].toDoubleOrNull() ?: 0.0
                        val qty = parts[2].toIntOrNull() ?: 0
                        val itemRevenue = price * qty

                        // Try finding product to estimate category
                        val productMatch = products.find { it.name.equals(name, ignoreCase = true) }
                        val category = productMatch?.category ?: "Other"
                        categorySales[category] = (categorySales[category] ?: 0.0) + itemRevenue
                    }
                }
            }
        }

        val totalOrders = orders.size
        val avgOrderValue = if (totalOrders > 0) totalSales / totalOrders else 0.0

        StoreAnalytics(
            totalOrders = totalOrders,
            totalRevenue = totalSales,
            averageOrderValue = avgOrderValue,
            totalStockInStore = productStockCount,
            categoryRevenueShare = categorySales,
            categoryStockDistribution = categoryStockCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StoreAnalytics())


    // --- Auth Actions ---
    fun login(email: String, word: String) {
        viewModelScope.launch {
            _loginError.value = null
            if (email.isEmpty() || word.isEmpty()) {
                _loginError.value = "Email and Password cannot be empty."
                return@launch
            }

            var user = repository.getUserByEmail(email)
            if (user == null && email == "user@example.com" && word == "password123") {
                // Fail-safe synchronous database seeding on first-login fallback
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val db = com.example.data.db.AppDatabase.getDatabase(getApplication(), this)
                    val dao = db.shoppingDao()
                    val existing = dao.getProductByIdNow(1)
                    if (existing == null) {
                        com.example.data.db.AppDatabase.populateDatabase(dao)
                    }
                }
                user = repository.getUserByEmail(email)
            }

            if (user == null || user.passwordHash != word) {
                _loginError.value = "Invalid email or password."
            } else {
                if (user.is2FAEnabled) {
                    // Prompt 2FA
                    _show2FAVerification.value = user
                } else {
                    // Standard Login Successful
                    _currentUser.value = user
                    _show2FAVerification.value = null
                    repository.insertNotification(
                        userId = user.id,
                        title = "Secure Login",
                        content = "You successfully logged into your account.",
                        type = "Security"
                    )
                }
            }
        }
    }

    fun verify2FA(code: String) {
        viewModelScope.launch {
            _twoFAError.value = null
            val pendingUser = _show2FAVerification.value
            if (pendingUser != null) {
                if (code == pendingUser.twoFACode) {
                    _currentUser.value = pendingUser
                    _show2FAVerification.value = null
                    repository.insertNotification(
                        userId = pendingUser.id,
                        title = "Two-Factor Auth Passed",
                        content = "Second factor verification confirmed. Safe session activated.",
                        type = "Security"
                    )
                } else {
                    _twoFAError.value = "Incorrect verification code. Please try again."
                }
            }
        }
    }

    fun cancel2FA() {
        _show2FAVerification.value = null
        _twoFAError.value = null
    }

    fun register(username: String, email: String, word: String) {
        viewModelScope.launch {
            _registerError.value = null
            if (username.isEmpty() || email.isEmpty() || word.isEmpty()) {
                _registerError.value = "Please fill in all requested fields."
                return@launch
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _registerError.value = "Please enter a valid email format."
                return@launch
            }

            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                _registerError.value = "An account with this email already exists."
                return@launch
            }

            val newUser = User(
                username = username,
                email = email,
                passwordHash = word,
                is2FAEnabled = false
            )
            val newId = repository.insertUser(newUser)

            // Auto log in newly registered user & send warm onboarding greeting notification
            val registeredUser = newUser.copy(id = newId.toInt())
            _currentUser.value = registeredUser
            repository.insertNotification(
                userId = registeredUser.id,
                title = "Welcome Boarding Active!",
                content = "Congratulations on register. Explore products, set your preferences in details tab and test purchasing.",
                type = "Promo"
            )
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginError.value = null
        _registerError.value = null
        _show2FAVerification.value = null
    }

    // --- Profile Customization Actions ---
    fun updateProfile(
        username: String,
        address: String,
        phoneNumber: String,
        cardNumber: String,
        cardHolder: String,
        is2FAEnabled: Boolean,
        isDarkMode: Boolean
    ) {
        val active = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = active.copy(
                username = username,
                address = address,
                phoneNumber = phoneNumber,
                cardNumber = cardNumber,
                cardHolder = cardHolder,
                is2FAEnabled = is2FAEnabled,
                isDarkMode = isDarkMode
            )
            repository.updateUser(updated)
            _currentUser.value = updated
            repository.insertNotification(
                userId = updated.id,
                title = "Preferences Updated",
                content = "Your profile changes and settings preferences were saved successfully.",
                type = "Security"
            )
        }
    }

    // --- Cart Actions ---
    fun addToCart(productId: Int, quantity: Int = 1) {
        val active = _currentUser.value ?: return
        viewModelScope.launch {
            repository.addToCart(active.id, productId, quantity)
        }
    }

    fun updateCartQuantity(cartItemId: Int, quantity: Int) {
        val active = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateCartItemQuantity(cartItemId, quantity, active.id)
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        viewModelScope.launch {
            repository.removeFromCart(cartItem)
        }
    }

    // --- Wishlist Actions ---
    fun toggleWishlist(productId: Int) {
        val active = _currentUser.value ?: return
        viewModelScope.launch {
            repository.toggleWishlist(active.id, productId)
        }
    }

    // --- Order Placement Action ---
    fun checkoutCurrentCart(
        shippingAddress: String,
        cardNumber: String,
        cardHolder: String,
        onComplete: (Boolean) -> Unit
    ) {
        val active = _currentUser.value ?: return
        val currentItems = cartItemsState.value
        if (currentItems.isEmpty()) {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            val itemsPair = currentItems.map { it.cartItem to it.product }
            val processedOrder = repository.placeOrder(
                userId = active.id,
                shippingAddress = shippingAddress,
                cardNumber = cardNumber,
                cardHolder = cardHolder,
                items = itemsPair
            )
            onComplete(processedOrder != null)
        }
    }

    // --- Admin Product CRUD Actions ---
    fun addProduct(
        name: String,
        description: String,
        price: Double,
        category: String,
        imageUrl: String,
        stock: Int
    ) {
        viewModelScope.launch {
            val newProduct = Product(
                name = name,
                description = description,
                price = price,
                category = category,
                imageUrl = imageUrl,
                stock = stock
            )
            repository.insertProduct(newProduct)

            // Trigger real-time broadcast and notify logged in user of new arrival!
            _currentUser.value?.let { activeUser ->
                repository.insertNotification(
                    userId = activeUser.id,
                    title = "New Arrival Added!",
                    content = "Check out $name in the $category section. Available now.",
                    type = "Promo"
                )
            }
        }
    }

    fun editProduct(
        id: Int,
        name: String,
        description: String,
        price: Double,
        category: String,
        imageUrl: String,
        stock: Int
    ) {
        viewModelScope.launch {
            val updated = Product(
                id = id,
                name = name,
                description = description,
                price = price,
                category = category,
                imageUrl = imageUrl,
                stock = stock
            )
            repository.updateProduct(updated)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun changeOrderStatusByAdmin(orderId: Int, status: String, orderUserId: Int) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status, orderUserId)
        }
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    // --- Clear Notifications ---
    fun clearNotifications() {
        val active = _currentUser.value ?: return
        viewModelScope.launch {
            repository.clearNotifications(active.id)
        }
    }

    // --- Firestore Synchronization Actions ---
    private val _firestoreSyncing = MutableStateFlow(false)
    val firestoreSyncing: StateFlow<Boolean> = _firestoreSyncing.asStateFlow()

    private val _firestoreSyncMessage = MutableStateFlow<String?>(null)
    val firestoreSyncMessage: StateFlow<String?> = _firestoreSyncMessage.asStateFlow()

    fun syncWithFirestore() {
        if (!com.example.data.firebase.FirebaseManager.isInitialized) {
            _firestoreSyncMessage.value = "Firestore is uninitialized. Please add 'google-services.json' configuration to connect."
            return
        }
        viewModelScope.launch {
            _firestoreSyncing.value = true
            _firestoreSyncMessage.value = "Starting secure data sync with Firestore..."
            
            try {
                // 1. Sync active User profile
                _currentUser.value?.let { activeUser ->
                    com.example.data.firebase.FirestoreService.saveUser(activeUser)
                }

                // 2. Fetch remote Products and cache locally
                com.example.data.firebase.FirestoreService.fetchAllProducts { remoteProducts ->
                    if (remoteProducts.isNotEmpty()) {
                        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            for (product in remoteProducts) {
                                repository.insertProduct(product)
                            }
                        }
                    }
                }

                // 3. Fetch remote Orders and cache locally
                com.example.data.firebase.FirestoreService.fetchAllOrders { remoteOrders ->
                    if (remoteOrders.isNotEmpty()) {
                        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            for (order in remoteOrders) {
                                repository.insertOrderDirectly(order)
                            }
                        }
                    }
                }

                _firestoreSyncMessage.value = "Sync successful! Products and orders synced with Firestore."
            } catch (e: Exception) {
                _firestoreSyncMessage.value = "Sync error: ${e.message}"
            } finally {
                _firestoreSyncing.value = false
            }
        }
    }

    fun clearSyncMessage() {
        _firestoreSyncMessage.value = null
    }
}

// Helper POJOs for clean Compose reactive flow state structures
data class FilteredProductsResult(
    val products: List<Product> = emptyList(),
    val totalItems: Int = 0,
    val totalPages: Int = 1,
    val currentPage: Int = 1
)

data class CartProductItem(
    val cartItem: CartItem,
    val product: Product
)

data class StoreAnalytics(
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val totalStockInStore: Int = 0,
    val categoryRevenueShare: Map<String, Double> = emptyMap(),
    val categoryStockDistribution: Map<String, Int> = emptyMap()
)
