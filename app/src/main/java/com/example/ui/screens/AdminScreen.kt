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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Product
import com.example.ui.viewmodel.ShoppingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: ShoppingViewModel,
    onBackNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var adminActiveTab by remember { mutableStateOf("Analytics") } // "Analytics" or "Products"

    // Dialog state for product add/edit
    var showProductFormDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) } // null means "Add Product"

    // Form inputs
    var productName by remember { mutableStateOf("") }
    var productDesc by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var productCategory by remember { mutableStateOf("Electronics") }
    var productImgUrl by remember { mutableStateOf("") }
    var productStock by remember { mutableStateOf("") }

    val categoriesList = listOf("Electronics", "Apparel", "Footwear", "Accessories")

    // Dynamic state listeners
    val analytics by viewModel.analyticsState.collectAsState()
    val products by viewModel.allProducts.collectAsState()

    // Trigger forms helper
    fun openProductForm(product: Product?) {
        editingProduct = product
        if (product != null) {
            productName = product.name
            productDesc = product.description
            productPrice = product.price.toString()
            productCategory = product.category
            productImgUrl = product.imageUrl
            productStock = product.stock.toString()
        } else {
            productName = ""
            productDesc = ""
            productPrice = ""
            productCategory = "Electronics"
            productImgUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&q=80&w=600"
            productStock = "10"
        }
        showProductFormDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Admin Suite") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackNavigate,
                        modifier = Modifier.testTag("admin_back_btn")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (adminActiveTab == "Products") {
                        IconButton(
                            onClick = { openProductForm(null) },
                            modifier = Modifier.testTag("admin_add_product_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Toggler Bar
            TabRow(
                selectedTabIndex = if (adminActiveTab == "Analytics") 0 else 1,
                modifier = Modifier.testTag("admin_tabs")
            ) {
                Tab(
                    selected = adminActiveTab == "Analytics",
                    onClick = { adminActiveTab = "Analytics" },
                    modifier = Modifier.testTag("tab_analytics")
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.BarChart, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Store Analytics", fontWeight = FontWeight.SemiBold)
                    }
                }

                Tab(
                    selected = adminActiveTab == "Products",
                    onClick = { adminActiveTab = "Products" },
                    modifier = Modifier.testTag("tab_products_crud")
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.SettingsSuggest, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Manage Inventory", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (adminActiveTab == "Analytics") {
                // --- Part 1: Comprehensive performance analytics dashboard ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // KPI stats widgets row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnalyticsKpiCard(
                            title = "Store Revenue",
                            value = "$${String.format("%.2f", analytics.totalRevenue)}",
                            icon = Icons.Default.AttachMoney,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )

                        AnalyticsKpiCard(
                            title = "Total Orders",
                            value = "${analytics.totalOrders}",
                            icon = Icons.Default.LocalShipping,
                            color = Color(0xFF1565C0),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnalyticsKpiCard(
                            title = "Avg Ticket Value",
                            value = "$${String.format("%.2f", analytics.averageOrderValue)}",
                            icon = Icons.Default.ShoppingBag,
                            color = Color(0xFFAD1457),
                            modifier = Modifier.weight(1f)
                        )

                        AnalyticsKpiCard(
                            title = "Items in Stock",
                            value = "${analytics.totalStockInStore} units",
                            icon = Icons.Default.Cabin,
                            color = Color(0xFFE65100),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // --- Custom Jetpack Compose Canvas draw Chart ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_custom_chart_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Category Sales Share ($)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Let's draw a stunning customized Canvas Pie Ring showing shares!
                            val revenueBreakdown = analytics.categoryRevenueShare
                            val totalRevenueSum = revenueBreakdown.values.sum().coerceAtLeast(1.0)

                            val apparelShare = (revenueBreakdown["Apparel"] ?: 0.0) / totalRevenueSum
                            val footwearShare = (revenueBreakdown["Footwear"] ?: 0.0) / totalRevenueSum
                            val accessoriesShare = (revenueBreakdown["Accessories"] ?: 0.0) / totalRevenueSum
                            val electronicsShare = (revenueBreakdown["Electronics"] ?: 0.0) / totalRevenueSum

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(
                                    modifier = Modifier
                                        .size(150.dp)
                                        .testTag("sales_canvas_ring")
                                ) {
                                    val strokeWidth = 32.dp.toPx()
                                    val radiusSize = size.width - strokeWidth
                                    val boundingBox = Size(radiusSize, radiusSize)
                                    val centerOffset = Offset(strokeWidth / 2, strokeWidth / 2)

                                    var curAngle = -90f

                                    // Let's color categories: Electronics = blue, Footwear = red, Accessories = orange, Apparel = purple
                                    val angleElectronics = (electronicsShare * 360f).toFloat()
                                    val angleApparel = (apparelShare * 360f).toFloat()
                                    val angleFootwear = (footwearShare * 360f).toFloat()
                                    val angleAccessories = (accessoriesShare * 360f).toFloat()

                                    // 1. Electronics Arc
                                    if (angleElectronics > 0f) {
                                        drawArc(
                                            color = Color(0xFF1976D2),
                                            startAngle = curAngle,
                                            sweepAngle = angleElectronics,
                                            useCenter = false,
                                            topLeft = centerOffset,
                                            size = boundingBox,
                                            style = Stroke(width = strokeWidth)
                                        )
                                        curAngle += angleElectronics
                                    }

                                    // 2. Apparel Arc
                                    if (angleApparel > 0f) {
                                        drawArc(
                                            color = Color(0xFF8E24AA),
                                            startAngle = curAngle,
                                            sweepAngle = angleApparel,
                                            useCenter = false,
                                            topLeft = centerOffset,
                                            size = boundingBox,
                                            style = Stroke(width = strokeWidth)
                                        )
                                        curAngle += angleApparel
                                    }

                                    // 3. Footwear Arc
                                    if (angleFootwear > 0f) {
                                        drawArc(
                                            color = Color(0xFFD32F2F),
                                            startAngle = curAngle,
                                            sweepAngle = angleFootwear,
                                            useCenter = false,
                                            topLeft = centerOffset,
                                            size = boundingBox,
                                            style = Stroke(width = strokeWidth)
                                        )
                                        curAngle += angleFootwear
                                    }

                                    // 4. Accessories Arc
                                    if (angleAccessories > 0f) {
                                        drawArc(
                                            color = Color(0xFFF57C00),
                                            startAngle = curAngle,
                                            sweepAngle = angleAccessories,
                                            useCenter = false,
                                            topLeft = centerOffset,
                                            size = boundingBox,
                                            style = Stroke(width = strokeWidth)
                                        )
                                        curAngle += angleAccessories
                                    }

                                    // Fallback when there are no purchases yet
                                    if (totalRevenueSum <= 1.0) {
                                        drawArc(
                                            color = Color.LightGray.copy(alpha = 0.5f),
                                            startAngle = 0f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            topLeft = centerOffset,
                                            size = boundingBox,
                                            style = Stroke(width = strokeWidth)
                                        )
                                    }
                                }

                                if (totalRevenueSum <= 1.0) {
                                    Text(
                                        text = "No analytics data\nyet",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                } else {
                                    Text(
                                        text = "$${String.format("%.0f", totalRevenueSum)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Legends list representation
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                CategoryLegend(Color(0xFF1976D2), "Electronics", "${(electronicsShare * 100).toInt()}%")
                                CategoryLegend(Color(0xFF8E24AA), "Apparel", "${(apparelShare * 100).toInt()}%")
                                CategoryLegend(Color(0xFFD32F2F), "Footwear", "${(footwearShare * 100).toInt()}%")
                                CategoryLegend(Color(0xFFF57C00), "Accessories", "${(accessoriesShare * 100).toInt()}%")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            } else {
                // --- Part 2: Product inventory manager (Full manual products CRUD) ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Warehouse Stock List (${products.size} Products)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        products.forEach { product ->
                            AdminProductRow(
                                product = product,
                                onEditTouch = { openProductForm(product) },
                                onDeleteTouch = { viewModel.deleteProduct(product) }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Add/Edit Product manual dialog forms ---
    if (showProductFormDialog) {
        AlertDialog(
            onDismissRequest = { showProductFormDialog = false },
            title = {
                Text(if (editingProduct != null) "Edit Product Specifications" else "Add New Store Product")
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Product Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_field_pname"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = productDesc,
                        onValueChange = { productDesc = it },
                        label = { Text("Description detailed") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_field_pdesc"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = productPrice,
                            onValueChange = { productPrice = it },
                            label = { Text("Price ($)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_field_pprice"),
                            shape = RoundedCornerShape(10.dp),
                            placeholder = { Text("99.99") }
                        )

                        OutlinedTextField(
                            value = productStock,
                            onValueChange = { productStock = it },
                            label = { Text("Stock Level") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_field_pstock"),
                            shape = RoundedCornerShape(10.dp),
                            placeholder = { Text("15") }
                        )
                    }

                    // Category choosing row dropdown simulation
                    Text("Department Category", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoriesList.forEach { cat ->
                            val activeSelected = productCategory == cat
                            FilterChip(
                                selected = activeSelected,
                                onClick = { productCategory = cat },
                                label = { Text(cat) },
                                modifier = Modifier.testTag("admin_cat_chip_$cat")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = productImgUrl,
                        onValueChange = { productImgUrl = it },
                        label = { Text("Product Image URL") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_field_pimg"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = productName
                        val priceVal = productPrice.toDoubleOrNull() ?: 0.0
                        val stockVal = productStock.toIntOrNull() ?: 0
                        val desc = productDesc
                        val img = productImgUrl

                        if (name.isNotEmpty() && img.isNotEmpty()) {
                            val activeProd = editingProduct
                            if (activeProd != null) {
                                viewModel.editProduct(activeProd.id, name, desc, priceVal, productCategory, img, stockVal)
                            } else {
                                viewModel.addProduct(name, desc, priceVal, productCategory, img, stockVal)
                            }
                            showProductFormDialog = false
                        }
                    },
                    modifier = Modifier.testTag("admin_spec_save_btn")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProductFormDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AnalyticsKpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("analytics_kpi_${title.replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun CategoryLegend(color: Color, name: String, percentage: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(percentage, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun AdminProductRow(
    product: Product,
    onEditTouch: () -> Unit,
    onDeleteTouch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_item_row_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.name, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$${String.format("%.2f", product.price)}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    Text("|", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), style = MaterialTheme.typography.bodySmall)
                    Text("Stock: ${product.stock} pcs", style = MaterialTheme.typography.bodySmall, color = if (product.stock < 5) Color.Red else Color.Gray)
                }
            }

            Row {
                IconButton(onClick = onEditTouch, modifier = Modifier.testTag("admin_edit_row_btn_${product.id}")) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit specs", tint = MaterialTheme.colorScheme.primary)
                }

                IconButton(onClick = onDeleteTouch, modifier = Modifier.testTag("admin_delete_row_btn_${product.id}")) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete product", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
