package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Product
import com.example.ui.viewmodel.ShoppingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingAppMain(
    viewModel: ShoppingViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("Shop") } // Shop, Cart, Wishlist, Alerts, Profile
    var currentAdminViewActive by remember { mutableStateOf(false) }

    // Selected product for showing detailed modal dialog
    var selectedDetailProduct by remember { mutableStateOf<Product?>(null) }
    var selectedDetailQty by remember { mutableStateOf(1) }

    // Listeners for badges
    val cartItems by viewModel.cartItemsState.collectAsState()
    val notifications by viewModel.notificationsState.collectAsState()

    val totalCartCount = cartItems.sumOf { it.cartItem.quantity }
    val unreadNotificationsCount = notifications.count { !it.isRead }

    if (currentAdminViewActive) {
        // --- Render Admin Panel ---
        AdminScreen(
            viewModel = viewModel,
            onBackNavigate = { currentAdminViewActive = false },
            modifier = modifier
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("app_bottom_nav"),
                    tonalElevation = 8.dp
                ) {
                    // 1. Shop Tab
                    NavigationBarItem(
                        selected = activeTab == "Shop",
                        onClick = { activeTab = "Shop" },
                        icon = { Icon(imageVector = Icons.Default.Store, contentDescription = "Market") },
                        label = { Text("Shop") },
                        modifier = Modifier.testTag("nav_tab_shop")
                    )

                    // 2. Cart Tab (with Badge)
                    NavigationBarItem(
                        selected = activeTab == "Cart",
                        onClick = { activeTab = "Cart" },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (totalCartCount > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text(totalCartCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Basket")
                            }
                        },
                        label = { Text("Cart") },
                        modifier = Modifier.testTag("nav_tab_cart")
                    )

                    // 3. Wishlist Tab
                    NavigationBarItem(
                        selected = activeTab == "Wishlist",
                        onClick = { activeTab = "Wishlist" },
                        icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "Saved") },
                        label = { Text("Wishlist") },
                        modifier = Modifier.testTag("nav_tab_wishlist")
                    )

                    // 4. Alerts Tab (with Badges)
                    NavigationBarItem(
                        selected = activeTab == "Alerts",
                        onClick = { activeTab = "Alerts" },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationsCount > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text(unreadNotificationsCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alerts")
                            }
                        },
                        label = { Text("Alerts") },
                        modifier = Modifier.testTag("nav_tab_alerts")
                    )

                    // 5. Profile Tab
                    NavigationBarItem(
                        selected = activeTab == "Profile",
                        onClick = { activeTab = "Profile" },
                        icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Account") },
                        label = { Text("Profile") },
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            },
            modifier = modifier
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main screen routing matching
                when (activeTab) {
                    "Shop" -> ShopScreen(
                        viewModel = viewModel,
                        onProductClick = { product ->
                            selectedDetailProduct = product
                            selectedDetailQty = 1
                        }
                    )
                    "Cart" -> CartScreen(
                        viewModel = viewModel,
                        onOrderPlacedCompleted = { 
                            activeTab = "Alerts" // Slide straight to orders and status notifications alert page
                        }
                    )
                    "Wishlist" -> WishlistScreen(viewModel = viewModel)
                    "Alerts" -> NotificationScreen(viewModel = viewModel)
                    "Profile" -> ProfileScreen(
                        viewModel = viewModel,
                        onAdminDashboardNavigate = { currentAdminViewActive = true }
                    )
                }
            }
        }
    }

    // --- Product Detail View Modal Dialog Sheet ---
    val targetProduct = selectedDetailProduct
    if (targetProduct != null) {
        val detailOutOfStock = targetProduct.stock <= 0
        AlertDialog(
            onDismissRequest = { selectedDetailProduct = null },
            title = {
                Text(targetProduct.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        AsyncImage(
                            model = targetProduct.imageUrl,
                            contentDescription = targetProduct.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$${String.format("%.2f", targetProduct.price)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Stock: ${targetProduct.stock} units",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (detailOutOfStock) Color.Red else Color.Gray
                        )
                    }

                    Text(
                        text = targetProduct.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    // Quantity selector stepper inside details sheet
                    if (!detailOutOfStock) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Desired Quantity", fontWeight = FontWeight.SemiBold)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = { if (selectedDetailQty > 1) selectedDetailQty-- },
                                    modifier = Modifier.size(28.dp).testTag("detail_qty_minus")
                                ) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrement", modifier = Modifier.size(16.dp))
                                }

                                Text(
                                    selectedDetailQty.toString(),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("detail_qty_number")
                                )

                                IconButton(
                                    onClick = { if (selectedDetailQty < targetProduct.stock) selectedDetailQty++ },
                                    enabled = selectedDetailQty < targetProduct.stock,
                                    modifier = Modifier.size(28.dp).testTag("detail_qty_plus")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increment", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addToCart(targetProduct.id, selectedDetailQty)
                        selectedDetailProduct = null
                    },
                    enabled = !detailOutOfStock,
                    modifier = Modifier.testTag("detail_add_to_cart_btn")
                ) {
                    Text(if (detailOutOfStock) "SOLD OUT" else "Add (${selectedDetailQty}) Items to Cart")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDetailProduct = null }) {
                    Text("Close")
                }
            }
        )
    }
}
