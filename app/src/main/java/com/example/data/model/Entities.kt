package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val is2FAEnabled: Boolean = false,
    val twoFACode: String = "123456", // Mock OTP password
    val isDarkMode: Boolean = false,
    val avatarUrl: String = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=200",
    val address: String = "",
    val cardNumber: String = "",
    val cardHolder: String = "",
    val phoneNumber: String = ""
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String,
    val stock: Int = 10,
    val rating: Double = 4.5,
    val ratingCount: Int = 12,
    val isFeatured: Boolean = false
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val userId: Int,
    val quantity: Int
)

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val userId: Int
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val orderDate: Long = System.currentTimeMillis(),
    val status: String = "Pending", // Pending, Processing, Shipped, Delivered
    val totalAmount: Double,
    val shippingAddress: String,
    val paymentId: String,
    val itemsJson: String // Stringified representation of order list
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "Order" // Order, Promo, Security
)
