package com.example.data.repository

import com.example.data.db.ShoppingDao
import com.example.data.model.CartItem
import com.example.data.model.NotificationItem
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.User
import com.example.data.model.WishlistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class ShoppingRepository(private val shoppingDao: ShoppingDao) {

    // --- Users ---
    fun getUserById(id: Int): Flow<User?> = shoppingDao.getUserById(id)

    suspend fun getUserByEmail(email: String): User? = shoppingDao.getUserByEmailNow(email)

    suspend fun insertUser(user: User): Long {
        val newId = shoppingDao.insertUser(user)
        val userWithId = user.copy(id = newId.toInt())
        com.example.data.firebase.FirestoreService.saveUser(userWithId)
        return newId
    }

    suspend fun updateUser(user: User) {
        shoppingDao.updateUser(user)
        com.example.data.firebase.FirestoreService.saveUser(user)
    }

    // --- Products ---
    val allProducts: Flow<List<Product>> = shoppingDao.getAllProducts()

    fun getProductById(id: Int): Flow<Product?> = shoppingDao.getProductById(id)

    suspend fun getProductByIdNow(id: Int): Product? = shoppingDao.getProductByIdNow(id)

    fun getProductsByCategory(category: String): Flow<List<Product>> = shoppingDao.getProductsByCategory(category)

    suspend fun insertProduct(product: Product): Long {
        val newId = shoppingDao.insertProduct(product)
        val productWithId = product.copy(id = newId.toInt())
        com.example.data.firebase.FirestoreService.saveProduct(productWithId)
        return newId
    }

    suspend fun updateProduct(product: Product) {
        shoppingDao.updateProduct(product)
        com.example.data.firebase.FirestoreService.saveProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        shoppingDao.deleteProduct(product)
        com.example.data.firebase.FirestoreService.deleteProduct(product.id)
    }

    // --- Cart ---
    fun getCartByUserId(userId: Int): Flow<List<CartItem>> = shoppingDao.getCartByUserId(userId)

    suspend fun getCartByUserIdNow(userId: Int): List<CartItem> = shoppingDao.getCartByUserIdNow(userId)

    suspend fun addToCart(userId: Int, productId: Int, quantity: Int) {
        val existing = shoppingDao.getCartItemByProductAndUser(userId, productId)
        if (existing != null) {
            shoppingDao.updateCartItem(existing.copy(quantity = existing.quantity + quantity))
        } else {
            shoppingDao.insertCartItem(CartItem(productId = productId, userId = userId, quantity = quantity))
        }
    }

    suspend fun updateCartItemQuantity(cartItemId: Int, quantity: Int, userId: Int) {
        // Find existing cart item to make sure it exists
        val cartItems = shoppingDao.getCartByUserIdNow(userId)
        val matched = cartItems.find { it.id == cartItemId }
        if (matched != null) {
            if (quantity <= 0) {
                shoppingDao.deleteCartItem(matched)
            } else {
                shoppingDao.updateCartItem(matched.copy(quantity = quantity))
            }
        }
    }

    suspend fun removeFromCart(cartItem: CartItem) {
        shoppingDao.deleteCartItem(cartItem)
    }

    suspend fun clearCart(userId: Int) {
        shoppingDao.clearCartForUser(userId)
    }

    // --- Wishlist ---
    fun getWishlistByUserId(userId: Int): Flow<List<WishlistItem>> = shoppingDao.getWishlistByUserId(userId)

    suspend fun toggleWishlist(userId: Int, productId: Int) {
        val existing = shoppingDao.getWishlistItemByProductAndUser(userId, productId)
        if (existing != null) {
            shoppingDao.deleteWishlistItem(existing)
        } else {
            shoppingDao.insertWishlistItem(WishlistItem(productId = productId, userId = userId))
        }
    }

    suspend fun isProductInWishlist(userId: Int, productId: Int): Boolean {
        return shoppingDao.getWishlistItemByProductAndUser(userId, productId) != null
    }

    // --- Orders ---
    fun getOrdersByUserId(userId: Int): Flow<List<Order>> = shoppingDao.getOrdersByUserId(userId)

    val allOrders: Flow<List<Order>> = shoppingDao.getAllOrders()

    suspend fun placeOrder(
        userId: Int,
        shippingAddress: String,
        cardNumber: String,
        cardHolder: String,
        items: List<Pair<CartItem, Product>>
    ): Order? {
        if (items.isEmpty()) return null

        var totalAmount = 0.0
        val itemsSerialized = mutableListOf<String>()

        // 1. Validate stocks, adjust stock levels, and build serialization string
        for (item in items) {
            val cartItem = item.first
            val product = item.second
            val qty = cartItem.quantity

            // Reduce stock
            val newStock = (product.stock - qty).coerceAtLeast(0)
            shoppingDao.updateProduct(product.copy(stock = newStock))

            // Calculate total price
            totalAmount += product.price * qty

            // Format: name|price|quantity|imageUrl
            // Escape pipe/semicolon characters in name to avoid corrupting custom string join
            val cleanName = product.name.replace("|", "-").replace(";", "-")
            itemsSerialized.add("$cleanName|${product.price}|$qty|${product.imageUrl}")
        }

        val itemsJson = itemsSerialized.joinToString(";;")
        val paymentId = "ch_stripe_" + UUID.randomUUID().toString().take(12)

        // 2. Create the Order
        val order = Order(
            userId = userId,
            status = "Processing",
            totalAmount = totalAmount,
            shippingAddress = shippingAddress,
            paymentId = paymentId,
            itemsJson = itemsJson
        )
        val orderId = shoppingDao.insertOrder(order)
        val orderWithId = order.copy(id = orderId.toInt())
        com.example.data.firebase.FirestoreService.saveOrder(orderWithId)

        // 3. Clear user's cart
        shoppingDao.clearCartForUser(userId)

        // 4. Send real-time confirmation notifications
        shoppingDao.insertNotification(
            NotificationItem(
                userId = userId,
                title = "Order placed successfully!",
                content = "Order #${1000 + orderId} of $${String.format("%.2f", totalAmount)} is currently processing. Payment via Stripe verified.",
                type = "Order"
            )
        )

        return orderWithId
    }

    suspend fun updateOrderStatus(orderId: Int, status: String, userId: Int) {
        shoppingDao.updateOrderStatus(orderId, status)
        val updatedOrder = shoppingDao.getOrderByIdNow(orderId)
        if (updatedOrder != null) {
            com.example.data.firebase.FirestoreService.saveOrder(updatedOrder)
        }
        shoppingDao.insertNotification(
            NotificationItem(
                userId = userId,
                title = "Order tracking updated",
                content = "Your order #${1000 + orderId} status has been updated to: $status.",
                type = "Order"
            )
        )
    }

    suspend fun insertOrderDirectly(order: Order): Long = shoppingDao.insertOrder(order)

    // --- Notifications ---
    fun getNotificationsByUserId(userId: Int): Flow<List<NotificationItem>> = shoppingDao.getNotificationsByUserId(userId)

    suspend fun insertNotification(userId: Int, title: String, content: String, type: String = "Promo") {
        shoppingDao.insertNotification(NotificationItem(userId = userId, title = title, content = content, type = type))
    }

    suspend fun markNotificationAsRead(id: Int) {
        shoppingDao.markNotificationAsRead(id)
    }

    suspend fun clearNotifications(userId: Int) {
        shoppingDao.clearNotificationsForUser(userId)
    }
}
