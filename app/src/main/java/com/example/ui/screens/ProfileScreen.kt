package com.example.ui.screens

import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.viewmodel.ShoppingViewModel

@Composable
fun ProfileScreen(
    viewModel: ShoppingViewModel,
    onAdminDashboardNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeUser by viewModel.currentUser.collectAsState()
    val orders by viewModel.userOrdersState.collectAsState()

    // Preferences states
    var displayName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var code2factor by remember { mutableStateOf(false) }
    var preferDark by remember { mutableStateOf(false) }

    var isEditState by remember { mutableStateOf(false) }

    // Synchronize initial configuration
    LaunchedEffect(activeUser) {
        activeUser?.let {
            displayName = it.username
            address = it.address
            phone = it.phoneNumber
            cardNumber = it.cardNumber
            cardHolder = it.cardHolder
            code2factor = it.is2FAEnabled
            preferDark = it.isDarkMode
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // User Avatar Banner
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .padding(4.dp)
            ) {
                AsyncImage(
                    model = activeUser?.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=200",
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = activeUser?.username ?: "Guest Member",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = activeUser?.email ?: "guest@example.com",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Points & Orders Summary Boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Loyalty Points Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Active Points", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "240 pts",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Completed orders counter card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Past Orders", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${orders.size} orders",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Customization form card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Profile Preferences",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        IconButton(
                            onClick = { 
                                if (isEditState) {
                                    // Save changes
                                    viewModel.updateProfile(
                                        username = displayName,
                                        address = address,
                                        phoneNumber = phone,
                                        cardNumber = cardNumber,
                                        cardHolder = cardHolder,
                                        is2FAEnabled = code2factor,
                                        isDarkMode = preferDark
                                    )
                                }
                                isEditState = !isEditState
                            },
                            modifier = Modifier.testTag("toggle_profile_edit")
                        ) {
                            Icon(
                                imageVector = if (isEditState) Icons.Default.Save else Icons.Default.Edit,
                                contentDescription = "Edit Preferences",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isEditState) {
                        // Read-only Details list
                        ProfileDetailRow(Icons.Default.Person, "Display Name", displayName)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        ProfileDetailRow(Icons.Default.Home, "Shipping Address", address.ifEmpty { "Not set" })
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        ProfileDetailRow(Icons.Default.Phone, "Phone Contact", phone.ifEmpty { "Not set" })
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        ProfileDetailRow(
                            Icons.Default.CreditCard, 
                            "Pre-saved Stripe Card", 
                            if (cardNumber.isNotEmpty()) "•••• •••• •••• " + cardNumber.takeLast(4) else "Not set"
                        )
                    } else {
                        // Editable Form Fields
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("field_name"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Default Shipping Address") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("field_address"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("field_phone"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it },
                            label = { Text("Simulated Credit Card Number") },
                            placeholder = { Text("4111 2222 3333 4444") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("field_card_num"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = cardHolder,
                            onValueChange = { cardHolder = it },
                            label = { Text("Cardholder Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("field_card_holder"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security Preference & App Preferences
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Access Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2FA Security Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Two-Factor Authentication", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Prompts security OTP code 123456", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Switch(
                            checked = code2factor,
                            onCheckedChange = {
                                code2factor = it
                                viewModel.updateProfile(displayName, address, phone, cardNumber, cardHolder, it, preferDark)
                            },
                            modifier = Modifier.testTag("2fa_switch_toggle")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Light/Dark Theme Preference Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Brightness4, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Dark Mode Toggle", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Swap the visual storefront palette", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Switch(
                            checked = preferDark,
                            onCheckedChange = {
                                preferDark = it
                                viewModel.updateProfile(displayName, address, phone, cardNumber, cardHolder, code2factor, it)
                            },
                            modifier = Modifier.testTag("dark_mode_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Firebase Firestore Database Sync Card ---
            val firestoreSyncing by viewModel.firestoreSyncing.collectAsState()
            val firestoreSyncMessage by viewModel.firestoreSyncMessage.collectAsState()
            val isFirebaseReady = com.example.data.firebase.FirebaseManager.isInitialized

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFirebaseReady) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) 
                                     else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isFirebaseReady) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                            contentDescription = "Cloud Sync Status",
                            tint = if (isFirebaseReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Firebase Firestore Database",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isFirebaseReady) "Status: Connected" else "Status: Local Sandbox (Rooms fallback)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isFirebaseReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "To fully enable cloud remote database backups, place your standard 'google-services.json' in '/app'. Real-time write mirroring to Firestore on user, product, or order creation is fully automated.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    firestoreSyncMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.clearSyncMessage() },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss Sync Message",
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.syncWithFirestore() },
                        enabled = !firestoreSyncing && isFirebaseReady,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("firestore_sync_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (firestoreSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Syncing Active...")
                        } else {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Perform Manual Cloud Sync")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Admin / Developer Portal Button ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAdminDashboardNavigate() }
                    .testTag("admin_portal_trigger"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Developer Portal & Admin Console", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Logout Session button ---
            OutlinedButton(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log out Session")
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun ProfileDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}
