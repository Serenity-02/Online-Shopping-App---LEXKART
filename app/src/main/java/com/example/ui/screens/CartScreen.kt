package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.viewmodel.CartProductItem
import com.example.ui.viewmodel.ShoppingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: ShoppingViewModel,
    onOrderPlacedCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartItemsState.collectAsState()
    val activeUser by viewModel.currentUser.collectAsState()

    var showStripeCheckoutSheet by remember { mutableStateOf(false) }
    var isCheckingOutInStripe by remember { mutableStateOf(false) }
    var checkoutSuccess by remember { mutableStateOf(false) }

    // Stripe checkout inputs (prefilled from profile if configured)
    var shippingAddress by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }

    // Autofill values once active user updates
    LaunchedEffect(activeUser) {
        activeUser?.let {
            shippingAddress = it.address
            cardNumber = it.cardNumber
            cardHolder = it.cardHolder
        }
    }

    val subtotal = cartItems.sumOf { it.product.price * it.cartItem.quantity }
    val shippingFee = if (subtotal > 0.0 && subtotal < 100.0) 9.99 else 0.0
    val totalEstimated = subtotal + shippingFee

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveShoppingCart,
                        contentDescription = "Empty Basket",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your shopping cart is empty!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Browse outstanding items in the marketplace and add them to enjoy high-quality shipping.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "My Shopping Basket (${cartItems.size} items)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // List of cart items
                cartItems.forEach { item ->
                    CartItemRow(
                        item = item,
                        onAddQuantity = { viewModel.updateCartQuantity(item.cartItem.id, item.cartItem.quantity + 1) },
                        onMinusQuantity = { viewModel.updateCartQuantity(item.cartItem.id, item.cartItem.quantity - 1) },
                        onRemove = { viewModel.removeFromCart(item.cartItem) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Order summary ticket
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Estimated Order Summary",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal Items", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$${String.format("%.2f", subtotal)}", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Shipping Handling", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (shippingFee == 0.0) "FREE" else "$${String.format("%.2f", shippingFee)}",
                                color = if (shippingFee == 0.0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (shippingFee > 0.0) {
                            Text(
                                text = "💡 Shop $${String.format("%.2f", 100.0 - subtotal)} more to activate FREE SHIPPING!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total Estimated",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$${String.format("%.2f", totalEstimated)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Checkout Button
                Button(
                    onClick = { showStripeCheckoutSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("checkout_trigger_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pay $${String.format("%.2f", totalEstimated)} via Stripe",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // --- Simulated Stripe Payment Sheet / Dialog ---
    if (showStripeCheckoutSheet) {
        AlertDialog(
            onDismissRequest = { if (!isCheckingOutInStripe) showStripeCheckoutSheet = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Secure Checkout (Stripe)")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Your transactions are encrypted and processed safely through standard client-side Stripe integrations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    OutlinedTextField(
                        value = shippingAddress,
                        onValueChange = { shippingAddress = it },
                        label = { Text("Delivery Shipping Address") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stripe_address_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = cardHolder,
                        onValueChange = { cardHolder = it },
                        label = { Text("Cardholder Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stripe_holder_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("Card Number (Stripe Card)") },
                        leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                        placeholder = { Text("4111 2222 3333 4444") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stripe_card_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cardExpiry,
                            onValueChange = { if (it.length <= 5) cardExpiry = it },
                            label = { Text("Expiry (MM/YY)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stripe_expiry_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            placeholder = { Text("12/28") }
                        )

                        OutlinedTextField(
                            value = cardCvv,
                            onValueChange = { if (it.length <= 3) cardCvv = it },
                            label = { Text("CVV") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stripe_cvv_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            placeholder = { Text("321") }
                        )
                    }

                    if (isCheckingOutInStripe) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Authorizing merchant payment...")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (shippingAddress.isEmpty() || cardNumber.isEmpty() || cardHolder.isEmpty()) {
                            // Quick simple local field check
                            return@Button
                        }
                        isCheckingOutInStripe = true
                        viewModel.checkoutCurrentCart(
                            shippingAddress = shippingAddress,
                            cardNumber = cardNumber,
                            cardHolder = cardHolder,
                            onComplete = { success ->
                                isCheckingOutInStripe = false
                                if (success) {
                                    showStripeCheckoutSheet = false
                                    checkoutSuccess = true
                                }
                            }
                        )
                    },
                    enabled = !isCheckingOutInStripe && shippingAddress.isNotEmpty() && cardNumber.isNotEmpty(),
                    modifier = Modifier.testTag("stripe_pay_button")
                ) {
                    Text("Pay $${String.format("%.2f", totalEstimated)}")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStripeCheckoutSheet = false },
                    enabled = !isCheckingOutInStripe
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Order Placed Success Alert Screen ---
    if (checkoutSuccess) {
        AlertDialog(
            onDismissRequest = { 
                checkoutSuccess = false
                onOrderPlacedCompleted() 
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(54.dp)
                )
            },
            title = {
                Text("Stripe Payment Authorized", textAlign = TextAlign.Center)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Awesome! Your payment has cleared, client inventory limits adjusted, and cart cleared.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A real-time notification alert was sent. You can track status in the Orders view.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        checkoutSuccess = false
                        onOrderPlacedCompleted() // callback to navigate and show history
                    },
                    modifier = Modifier.testTag("success_confirm_btn")
                ) {
                    Text("Track My Order")
                }
            }
        )
    }
}

@Composable
fun CartItemRow(
    item: CartProductItem,
    onAddQuantity: () -> Unit,
    onMinusQuantity: () -> Unit,
    onRemove: () -> Unit
) {
    val totalLinePrice = item.product.price * item.cartItem.quantity

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_row_${item.cartItem.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image
            AsyncImage(
                model = item.product.imageUrl,
                contentDescription = item.product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Category: ${item.product.category}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$${String.format("%.2f", item.product.price)} each",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quantity adjusters Column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                // Garbage bin quick delete
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("cart_item_remove_${item.cartItem.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Item",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Quantity Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onMinusQuantity,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("cart_item_minus_${item.cartItem.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrement",
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = item.cartItem.quantity.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.testTag("cart_item_qty_label_${item.cartItem.id}")
                    )

                    IconButton(
                        onClick = onAddQuantity,
                        enabled = item.cartItem.quantity < item.product.stock,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("cart_item_plus_${item.cartItem.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increment",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
