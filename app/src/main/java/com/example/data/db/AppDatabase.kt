package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CartItem
import com.example.data.model.NotificationItem
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.User
import com.example.data.model.WishlistItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Product::class,
        CartItem::class,
        WishlistItem::class,
        Order::class,
        NotificationItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingDao(): ShoppingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val existingInstance = INSTANCE
                if (existingInstance != null) {
                    existingInstance
                } else {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "shopping_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    INSTANCE = instance

                    // Robust fire-and-forget seeding check on initialization
                    scope.launch(Dispatchers.IO) {
                        val dao = instance.shoppingDao()
                        val existing = dao.getProductByIdNow(1)
                        if (existing == null) {
                            populateDatabase(dao)
                        }
                    }

                    instance
                }
            }
        }

        suspend fun populateDatabase(dao: ShoppingDao) {
            // Seed sample user (John Doe)
            val user = User(
                id = 1,
                username = "John Doe",
                email = "user@example.com",
                passwordHash = "password123",
                is2FAEnabled = false,
                isDarkMode = false,
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=200",
                address = "123 Main St, New York, NY 10001",
                cardNumber = "4111 2222 3333 4444",
                cardHolder = "John Doe",
                phoneNumber = "+1 (555) 019-2834"
            )
            dao.insertUser(user)

            // Seed sample products
            val products = listOf(
                Product(
                    id = 1,
                    name = "Premium Wireless Headphones",
                    description = "Experience rich, immersive audio with active hybrid noise cancellation. Cozy over-ear design with 40-hour battery life and quick charge technology.",
                    price = 199.99,
                    category = "Electronics",
                    imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&q=80&w=600",
                    stock = 15,
                    rating = 4.8,
                    ratingCount = 128,
                    isFeatured = true
                ),
                Product(
                    id = 2,
                    name = "Vapor Breathable Running Shoes",
                    description = "Ultra-lightweight mesh knit shoes optimized for endurance running and daily training. Features responsive energy-return foam soles.",
                    price = 129.99,
                    category = "Footwear",
                    imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&q=80&w=600",
                    stock = 8,
                    rating = 4.7,
                    ratingCount = 84,
                    isFeatured = true
                ),
                Product(
                    id = 3,
                    name = "Waterproof Smart Sport Watch",
                    description = "Modern fitness tracker with high contrast AMOLED screen, continuous heart rate sensor, built-in GPS, and comprehensive workout modes.",
                    price = 89.99,
                    category = "Electronics",
                    imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&q=80&w=600",
                    stock = 25,
                    rating = 4.4,
                    ratingCount = 205,
                    isFeatured = false
                ),
                Product(
                    id = 4,
                    name = "Classic Leather Journal Notebook",
                    description = "Handcrafted genuine leather journal with 200 pages of thick, ink-resistant unruled cream paper. Secure elastic band and premium ribbon bookmark.",
                    price = 24.50,
                    category = "Accessories",
                    imageUrl = "https://images.unsplash.com/photo-1544816155-12df9643f363?auto=format&fit=crop&q=80&w=600",
                    stock = 40,
                    rating = 4.9,
                    ratingCount = 52,
                    isFeatured = false
                ),
                Product(
                    id = 5,
                    name = "Minimalist Waterproof Backpack",
                    description = "Sleek weather-resistant commute bag containing padded sleeves for 16-inch laptops, quick access utility pockets, and reinforced dual chest straps.",
                    price = 75.00,
                    category = "Accessories",
                    imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&q=80&w=600",
                    stock = 12,
                    rating = 4.6,
                    ratingCount = 93,
                    isFeatured = true
                ),
                Product(
                    id = 6,
                    name = "Heavyweight Oversized Cotton Hoodie",
                    description = "100% organic heavy brush cotton comfort hoodie. Clean loose drop-shoulder style with durable ribbed cuffs and cozy kangaroo pocket.",
                    price = 59.99,
                    category = "Apparel",
                    imageUrl = "https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&q=80&w=600",
                    stock = 20,
                    rating = 4.5,
                    ratingCount = 67,
                    isFeatured = false
                ),
                Product(
                    id = 7,
                    name = "Retro Dynamic Mechanical Keyboard",
                    description = "Compact 75% mechanical layout utilizing tactile brown switches, bright RGB backlighting options, with multi-device bluetooth pairing capability.",
                    price = 110.00,
                    category = "Electronics",
                    imageUrl = "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&q=80&w=600",
                    stock = 6,
                    rating = 4.7,
                    ratingCount = 42,
                    isFeatured = false
                ),
                Product(
                    id = 8,
                    name = "Heavyweight Denim Chore Jacket",
                    description = "Sturdy vintage-wash denim outerwear. Features heavy triple stitching, triple front utility pockets, and elegant brushed copper buttons.",
                    price = 85.00,
                    category = "Apparel",
                    imageUrl = "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?auto=format&fit=crop&q=80&w=600",
                    stock = 9,
                    rating = 4.3,
                    ratingCount = 31,
                    isFeatured = false
                )
            )
            for (p in products) {
                dao.insertProduct(p)
            }

            // Seed initial greeting notification
            dao.insertNotification(
                NotificationItem(
                    userId = 1,
                    title = "Welcome to your active storefront!",
                    content = "Explore trending items, try managing settings, add to cart, and test out checking out with simulated Stripe payment panels.",
                    type = "Promo"
                )
            )
        }
    }
}
