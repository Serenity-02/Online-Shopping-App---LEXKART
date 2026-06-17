package com.example.data.firebase

import android.util.Log
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.User

object FirestoreService {
    private const val TAG = "FirestoreService"

    // --- User Collection Operations ---
    fun saveUser(user: User) {
        val firestore = FirebaseManager.firestore ?: return
        try {
            val userMap = hashMapOf(
                "id" to user.id,
                "username" to user.username,
                "email" to user.email,
                "passwordHash" to user.passwordHash,
                "is2FAEnabled" to user.is2FAEnabled,
                "twoFACode" to user.twoFACode,
                "isDarkMode" to user.isDarkMode,
                "avatarUrl" to user.avatarUrl,
                "address" to user.address,
                "cardNumber" to user.cardNumber,
                "cardHolder" to user.cardHolder,
                "phoneNumber" to user.phoneNumber
            )
            firestore.collection("users")
                .document(user.email)
                .set(userMap)
                .addOnSuccessListener {
                    Log.d(TAG, "User ${user.email} saved to Firestore successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to save user to Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user to Firestore: ${e.message}")
        }
    }

    fun fetchUserByEmail(email: String, onResult: (User?) -> Unit) {
        val firestore = FirebaseManager.firestore ?: run {
            onResult(null)
            return
        }
        try {
            firestore.collection("users")
                .document(email)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val user = User(
                            id = (doc.getLong("id") ?: 0L).toInt(),
                            username = doc.getString("username") ?: "",
                            email = doc.getString("email") ?: "",
                            passwordHash = doc.getString("passwordHash") ?: "",
                            is2FAEnabled = doc.getBoolean("is2FAEnabled") ?: false,
                            twoFACode = doc.getString("twoFACode") ?: "123456",
                            isDarkMode = doc.getBoolean("isDarkMode") ?: false,
                            avatarUrl = doc.getString("avatarUrl") ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=200",
                            address = doc.getString("address") ?: "",
                            cardNumber = doc.getString("cardNumber") ?: "",
                            cardHolder = doc.getString("cardHolder") ?: "",
                            phoneNumber = doc.getString("phoneNumber") ?: ""
                        )
                        onResult(user)
                    } else {
                        onResult(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to fetch user from Firestore: ${e.message}")
                    onResult(null)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user from Firestore: ${e.message}")
            onResult(null)
        }
    }

    // --- Product Collection Operations ---
    fun saveProduct(product: Product) {
        val firestore = FirebaseManager.firestore ?: return
        try {
            val productMap = hashMapOf(
                "id" to product.id,
                "name" to product.name,
                "description" to product.description,
                "price" to product.price,
                "category" to product.category,
                "imageUrl" to product.imageUrl,
                "stock" to product.stock,
                "rating" to product.rating,
                "ratingCount" to product.ratingCount,
                "isFeatured" to product.isFeatured
            )
            firestore.collection("products")
                .document(product.id.toString())
                .set(productMap)
                .addOnSuccessListener {
                    Log.d(TAG, "Product #${product.id} saved to Firestore successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to save product to Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving product to Firestore: ${e.message}")
        }
    }

    fun deleteProduct(productId: Int) {
        val firestore = FirebaseManager.firestore ?: return
        try {
            firestore.collection("products")
                .document(productId.toString())
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "Product #$productId deleted from Firestore successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to delete product from Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product from Firestore: ${e.message}")
        }
    }

    fun fetchAllProducts(onResult: (List<Product>) -> Unit) {
        val firestore = FirebaseManager.firestore ?: run {
            onResult(emptyList())
            return
        }
        try {
            firestore.collection("products")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val products = mutableListOf<Product>()
                    for (doc in querySnapshot.documents) {
                        try {
                            val product = Product(
                                id = (doc.getLong("id") ?: 0L).toInt(),
                                name = doc.getString("name") ?: "",
                                description = doc.getString("description") ?: "",
                                price = doc.getDouble("price") ?: 0.0,
                                category = doc.getString("category") ?: "",
                                imageUrl = doc.getString("imageUrl") ?: "",
                                stock = (doc.getLong("stock") ?: 10L).toInt(),
                                rating = doc.getDouble("rating") ?: 4.5,
                                ratingCount = (doc.getLong("ratingCount") ?: 12L).toInt(),
                                isFeatured = doc.getBoolean("isFeatured") ?: false
                            )
                            products.add(product)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error mapping doc to product: ${e.message}")
                        }
                    }
                    onResult(products)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to fetch products from Firestore: ${e.message}")
                    onResult(emptyList())
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching products from Firestore: ${e.message}")
            onResult(emptyList())
        }
    }

    // --- Order Collection Operations ---
    fun saveOrder(order: Order) {
        val firestore = FirebaseManager.firestore ?: return
        try {
            val orderMap = hashMapOf(
                "id" to order.id,
                "userId" to order.userId,
                "orderDate" to order.orderDate,
                "status" to order.status,
                "totalAmount" to order.totalAmount,
                "shippingAddress" to order.shippingAddress,
                "paymentId" to order.paymentId,
                "itemsJson" to order.itemsJson
            )
            firestore.collection("orders")
                .document(order.id.toString())
                .set(orderMap)
                .addOnSuccessListener {
                    Log.d(TAG, "Order #${order.id} saved to Firestore successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to save order to Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving order to Firestore: ${e.message}")
        }
    }

    fun fetchAllOrders(onResult: (List<Order>) -> Unit) {
        val firestore = FirebaseManager.firestore ?: run {
            onResult(emptyList())
            return
        }
        try {
            firestore.collection("orders")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val orders = mutableListOf<Order>()
                    for (doc in querySnapshot.documents) {
                        try {
                            val order = Order(
                                id = (doc.getLong("id") ?: 0L).toInt(),
                                userId = (doc.getLong("userId") ?: 0L).toInt(),
                                orderDate = doc.getLong("orderDate") ?: System.currentTimeMillis(),
                                status = doc.getString("status") ?: "Pending",
                                totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                                shippingAddress = doc.getString("shippingAddress") ?: "",
                                paymentId = doc.getString("paymentId") ?: "",
                                itemsJson = doc.getString("itemsJson") ?: ""
                            )
                            orders.add(order)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error mapping doc to order: ${e.message}")
                        }
                    }
                    onResult(orders)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to fetch orders from Firestore: ${e.message}")
                    onResult(emptyList())
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching orders from Firestore: ${e.message}")
            onResult(emptyList())
        }
    }
}
