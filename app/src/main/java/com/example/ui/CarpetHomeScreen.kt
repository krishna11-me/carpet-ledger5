package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CarpetConstants
import com.example.data.TransactionEntity
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CarpetHomeScreen(viewModel: CarpetViewModel) {
    val context = LocalContext.current
    val allTransactions by viewModel.allTransactions.collectAsState()
    val stockMap by viewModel.stockMap.collectAsState()

    // Screen States
    var showDeleteConfirmDialog by remember { mutableStateOf<TransactionEntity?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showConfirmRestoreDialog by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportToUri(uri, context, onSuccess = {}, onError = {})
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.restoreFromUri(uri, context, onSuccess = {}, onError = {})
        }
    }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "MANNONWOVEN",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Stock Ledger & Godown Manager",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Options & Version Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier
                                    .clickable { showBackupDialog = true }
                                    .testTag("backup_settings_chip")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share, 
                                        contentDescription = "Backup/Restore",
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Sync",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Dashboard Modes Selector / Navigation Tiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TabTileButton(
                            label = "Stock",
                            icon = Icons.Default.ShoppingCart,
                            isActive = viewModel.currentDashboardTab == "STOCK",
                            activeColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_stock_view"),
                            onClick = { viewModel.currentDashboardTab = "STOCK" }
                        )
                        TabTileButton(
                            label = "Log Entry",
                            icon = Icons.Default.Add,
                            isActive = viewModel.currentDashboardTab == "LOG",
                            activeColor = Color(0xFF2E7D32), // Emerald Green
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("tab_log_entry"),
                            onClick = { viewModel.currentDashboardTab = "LOG" }
                        )
                        TabTileButton(
                            label = "Ledger",
                            icon = Icons.Default.List,
                            isActive = viewModel.currentDashboardTab == "LEDGER",
                            activeColor = Color(0xFFC62828), // Crimson Red
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_ledger"),
                            onClick = { viewModel.currentDashboardTab = "LEDGER" }
                        )
                        TabTileButton(
                            label = "Styles",
                            icon = Icons.Default.Edit,
                            isActive = viewModel.currentDashboardTab == "STYLES",
                            activeColor = Color(0xFFE65100), // Amber
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_styles_catalog"),
                            onClick = { viewModel.currentDashboardTab = "STYLES" }
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Info / Success Banner
                viewModel.messageText?.let { msg ->
                    val isError = viewModel.isMessageError
                    Surface(
                        color = if (isError) MaterialTheme.colorScheme.errorContainer else Color(0xFFE8F5E9),
                        contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF1B5E20),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.clearMessage() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = if (isError) Icons.Default.Warning else Icons.Default.Check,
                                contentDescription = if (isError) "Error" else "Success"
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearMessage() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = if (isError) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF1B5E20)
                                )
                            }
                        }
                    }
                    
                    // Auto dismiss the message after 4 seconds
                    LaunchedEffect(msg) {
                        delayMessageDismissal { viewModel.clearMessage() }
                    }
                }

                // Show active tab
                when (viewModel.currentDashboardTab) {
                    "STOCK" -> StockStatusScreen(viewModel, stockMap, allTransactions)
                    "LOG" -> DailyLogScreen(viewModel)
                    "LEDGER" -> TxLedgerScreen(
                        allTransactions = allTransactions,
                        viewModel = viewModel,
                        onDeleteClick = { showDeleteConfirmDialog = it }
                    )
                    "STYLES" -> StylesCatalogScreen(viewModel = viewModel)
                }
            }

            // Confirm Delete Dialog
            showDeleteConfirmDialog?.let { transaction ->
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = null },
                    title = { Text("Delete Transaction Record?") },
                    text = {
                        Text(
                            "Are you sure you want to delete this record?\n\n" +
                            "Date: ${transaction.transactionDate}\n" +
                            "Product: ${transaction.carpetType} (${transaction.size})\n" +
                            "Flow: ${transaction.direction}\n" +
                            "Quantity: ${transaction.quantity} roll(s)"
                        )
                    },
                    confirmButton = {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            onClick = {
                                viewModel.deleteTransaction(transaction)
                                showDeleteConfirmDialog = null
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showBackupDialog) {
                AlertDialog(
                    onDismissRequest = { showBackupDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share, 
                                contentDescription = "Backup", 
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Device Backup & Restore",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Transfer your ledger data when changing devices or keep a secure offline backup copy.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            
                            // Create Backup Option
                            Surface(
                                onClick = {
                                    showBackupDialog = false
                                    val sdf = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                                    val currentDateTime = sdf.format(java.util.Date())
                                    createDocumentLauncher.launch("mannonwoven_ledger_backup_$currentDateTime.json")
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth().testTag("backup_button_export")
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Save file",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Create & Save Backup",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "Creates a .json backup file of all transactions, custom styles, custom size lists, and categorical groups.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Upload/Restore Option
                            Surface(
                                onClick = {
                                    showConfirmRestoreDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth().testTag("backup_button_import")
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Upload restore file",
                                            tint = MaterialTheme.colorScheme.onTertiary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Upload & Restore",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = "Upload a previously saved .json backup file. This will restore custom catalogs and full transaction histories.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            
                            Text(
                                text = "⚠️ WARNING: Restoring backup data replaces your current database completely. It should be performed mainly when setting up a new device.",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showBackupDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }

            if (showConfirmRestoreDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmRestoreDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning, 
                                contentDescription = "Warning", 
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Are you absolutely sure?")
                        }
                    },
                    text = {
                        Text("This action will FORMAT AND REWRITE your entire local stock ledger with the content of the selected backup file. Any changes made since your last backup will be lost forever.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showConfirmRestoreDialog = false
                                showBackupDialog = false
                                openDocumentLauncher.launch("application/json")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Confirm Import")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmRestoreDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

// Staggered trigger helper
private suspend fun delayMessageDismissal(onDismiss: () -> Unit) {
    kotlinx.coroutines.delay(5000)
    onDismiss()
}

@Composable
fun TabTileButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) activeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) activeColor else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier.height(60.dp),
        tonalElevation = if (isActive) 2.dp else 0.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ------------------ SECTION 1: STOCK STATUS BALANCE VIEWER ------------------
@Composable
fun StockStatusScreen(
    viewModel: CarpetViewModel,
    stockMap: Map<String, Map<String, Int>>,
    allTransactions: List<TransactionEntity>
) {
    var stockFilterCategory by remember { mutableStateOf("ALL") }
    var stockSearchQuery by remember { mutableStateOf("") }

    val allCategories by viewModel.allCategories.collectAsState()
    val categoryToTypesMap by viewModel.categoryToTypesMap.collectAsState()
    val allTypes by viewModel.allTypes.collectAsState()

    val carpetSizes by viewModel.carpetSizes.collectAsState()
    val durriSizes by viewModel.durriSizes.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Stats Overview Row
        StockOverviewStatsRow(allTransactions, stockMap)

        Spacer(modifier = Modifier.height(10.dp))

        // Filters Container
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Search Input
                OutlinedTextField(
                    value = stockSearchQuery,
                    onValueChange = { stockSearchQuery = it },
                    placeholder = { Text("Search carpet or fabric style...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (stockSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { stockSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stock_search_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable category filter row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val categories = listOf("ALL") + allCategories
                    categories.forEach { cat ->
                        FilterChip(
                            selected = stockFilterCategory == cat,
                            onClick = { stockFilterCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Stock Grid Table Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (stockFilterCategory == "DURRI") "DURRI STOCK STATUS" else "ROLLS STOCK BALANCES",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Scroll horizontal for widths ➔",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.secondary),
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Render frozen column table
        val isDurriSection = stockFilterCategory == "DURRI"
        val sizes = if (isDurriSection) durriSizes else carpetSizes
        
        val rowTypes = remember(stockFilterCategory, stockSearchQuery, categoryToTypesMap, allTypes) {
            val list = if (stockFilterCategory == "ALL") {
                allTypes
            } else {
                categoryToTypesMap[stockFilterCategory] ?: emptyList()
            }
            if (stockSearchQuery.isEmpty()) {
                list
            } else {
                list.filter { it.contains(stockSearchQuery, ignoreCase = true) }
            }
        }

        if (rowTypes.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1.5f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "No matching carpets found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        } else {
            // Dynamic frozen spreadsheet grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                InteractiveStockGrid(
                    rowTypes = rowTypes,
                    isDurri = isDurriSection,
                    sizes = sizes,
                    stockMap = stockMap,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun StockOverviewStatsRow(
    allTransactions: List<TransactionEntity>,
    stockMap: Map<String, Map<String, Int>>
) {
    // Total rolls currently in stock
    val totalRolls = remember(stockMap) {
        stockMap.values.sumOf { sizeMap -> sizeMap.values.sumOf { if (it > 0) it else 0 } }
    }
    
    // Counter of unique product combinations in stock
    val activeSkusInStock = remember(stockMap) {
        var count = 0
        stockMap.forEach { (_, sizeMap) ->
            sizeMap.forEach { (_, balance) ->
                if (balance > 0) count++
            }
        }
        count
    }

    // Daily activity counters
    val todayDateString = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayReceived = remember(allTransactions) {
        allTransactions.filter { it.transactionDate == todayDateString && it.direction == "INCOMING" }.sumOf { it.quantity }
    }
    val todayDispatched = remember(allTransactions) {
        allTransactions.filter { it.transactionDate == todayDateString && it.direction == "OUTGOING" }.sumOf { it.quantity }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = "Total Stock",
            value = "$totalRolls Rolls",
            subtitle = "$activeSkusInStock active items",
            icon = Icons.Default.ShoppingCart,
            iconColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Today Received",
            value = "+$todayReceived",
            subtitle = "Daily Inflow",
            icon = Icons.Default.KeyboardArrowDown,
            iconColor = Color(0xFF2E7D32),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Today Dispatched",
            value = "-$todayDispatched",
            subtitle = "Daily Outflow",
            icon = Icons.Default.KeyboardArrowUp,
            iconColor = Color(0xFFE65100),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, maxLines = 1)
        }
    }
}

@Composable
fun InteractiveStockGrid(
    rowTypes: List<String>,
    isDurri: Boolean,
    sizes: List<String>,
    stockMap: Map<String, Map<String, Int>>,
    viewModel: CarpetViewModel
) {
    val horizontalScrollState = rememberScrollState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    var tooltipCellDetails by remember { mutableStateOf<Triple<String, String, List<Pair<Double, Int>>>?>(null) }
    var showImageDialogForStyle by remember { mutableStateOf<String?>(null) }
    val carpetImagesMap by viewModel.carpetImagesMap.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Frozen Left: Title Cell
            Text(
                text = "Carpet Style (Tap 📷)",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .width(135.dp)
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    .padding(8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Scrollable columns header
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
            ) {
                sizes.forEach { size ->
                    Text(
                        text = if (isDurri) size else "${size}m",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .width(62.dp)
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            .padding(8.dp)
                    )
                }

                // Total calculation col
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(65.dp)
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        .padding(8.dp)
                )
            }
        }

        // Table Rows
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(rowTypes) { carpetType ->
                // Gather quantities
                val sizesMapForType = stockMap[carpetType] ?: emptyMap()
                val totalQty = sizesMapForType.values.filter { it > 0 }.sum()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (totalQty > 0) MaterialTheme.colorScheme.surface 
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                ) {
                    // Frozen style label
                    val hasImage = !carpetImagesMap[carpetType.uppercase()].isNullOrEmpty()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(135.dp)
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            .background(
                                if (totalQty > 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                showImageDialogForStyle = carpetType
                            }
                            .padding(horizontal = 6.dp, vertical = 8.dp)
                            .testTag("style_col_${carpetType.replace(" ", "_")}")
                    ) {
                        Text(
                            text = (if (hasImage) "📷 " else "") + carpetType,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (totalQty > 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                color = if (hasImage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Scrollable cells
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        sizes.forEach { size ->
                            val qty = sizesMapForType[size] ?: 0
                            val quantityDisplay = if (qty > 0) qty.toString() else "-"
                            
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .width(62.dp)
                                    .height(34.dp)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                    .background(
                                        when {
                                            qty > 15 -> Color(0xFFE8F5E9) // High stock (light green)
                                            qty in 1..5 -> Color(0xFFFFF3E0) // Low stock (light amber)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable(enabled = qty > 0) {
                                        val lengthsList = viewModel.getRollLengthsInStock(carpetType, size, allTransactions)
                                        tooltipCellDetails = Triple(carpetType, size, lengthsList)
                                    }
                                    .testTag("stock_cell_${carpetType.replace(" ", "_")}_${size.replace(".", "_")}")
                            ) {
                                Text(
                                    text = quantityDisplay,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (qty > 0) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = when {
                                            qty > 15 -> Color(0xFF1B5E20)
                                            qty in 1..5 -> Color(0xFFE65100)
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        }
                                    )
                                )
                            }
                        }

                        // Aggregate total row item
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .width(65.dp)
                                .height(34.dp)
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                .background(
                                    if (totalQty > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    else Color.Transparent
                                )
                        ) {
                            Text(
                                text = if (totalQty > 0) totalQty.toString() else "-",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = if (totalQty > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }
                }
            }
        }

        if (tooltipCellDetails != null) {
            val (styleName, widthValue, lengthsList) = tooltipCellDetails!!
            AlertDialog(
                onDismissRequest = { tooltipCellDetails = null },
                title = {
                    Column {
                        Text(
                            text = styleName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Width / Roll Size: ${if (isDurri) widthValue else "${widthValue}m"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Active Rolls Distribution (${lengthsList.sumOf { it.second }} rolls):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (lengthsList.isEmpty()) {
                            Text(
                                text = "No recorded length data available for these rolls.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                lengthsList.forEach { (length, count) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = androidx.compose.foundation.shape.CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = viewModel.formatRollLength(length),
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                        Text(
                                            text = if (count == 1) "1 roll" else "$count rolls",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = "💡 Tap outside or click Close to dismiss.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { tooltipCellDetails = null }) {
                        Text("Close")
                    }
                }
            )
        }

        showImageDialogForStyle?.let { styleName ->
            val imageUri = carpetImagesMap[styleName.uppercase()]
            AlertDialog(
                onDismissRequest = { showImageDialogForStyle = null },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = styleName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { showImageDialogForStyle = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!imageUri.isNullOrEmpty() && java.io.File(imageUri).exists()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                            ) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Photo of $styleName",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "No image info",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No photograph uploaded for this carpet style yet.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Button(
                                onClick = {
                                    showImageDialogForStyle = null
                                    viewModel.currentDashboardTab = "STYLES"
                                },
                                modifier = Modifier.fillMaxWidth().testTag("go_to_catalog_upload_btn")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload Photo in Catalog")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showImageDialogForStyle = null }) {
                        Text("Dismiss")
                    }
                }
            )
        }
    }
}


// ------------------ SECTION 2: INTEGRATED DAILY LOGGER ENTRY FORM ------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyLogScreen(viewModel: CarpetViewModel) {
    val context = LocalContext.current
    var groupSelectedTab by remember { mutableStateOf("FELT") }
    val allCategories by viewModel.allCategories.collectAsState()
    val categoryToTypesMap by viewModel.categoryToTypesMap.collectAsState()

    val carpetSizes by viewModel.carpetSizes.collectAsState()
    val durriSizes by viewModel.durriSizes.collectAsState()

    var showAddStyleDialog by remember { mutableStateOf(false) }
    var newStyleName by remember { mutableStateOf("") }
    var newStyleCategory by remember { mutableStateOf("FELT") }
    var isCustomCategorySelected by remember { mutableStateOf(false) }
    var customCategoryInput by remember { mutableStateOf("") }

    var showAddSizeDialog by remember { mutableStateOf(false) }
    var newSizeValue by remember { mutableStateOf("") }
    var newSizeIsDurri by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        // TABS FOR INCOMING VS OUTGOING FLOWS
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Incoming Roll Option
                Button(
                    onClick = { viewModel.transactionDirection = "INCOMING" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.transactionDirection == "INCOMING") Color(0xFF2E7D32) else Color.Transparent,
                        contentColor = if (viewModel.transactionDirection == "INCOMING") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Incoming Inflow", fontSize = 13.sp)
                }

                // Outgoing Roll Option
                Button(
                    onClick = { viewModel.transactionDirection = "OUTGOING" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.transactionDirection == "OUTGOING") Color(0xFFD84315) else Color.Transparent,
                        contentColor = if (viewModel.transactionDirection == "OUTGOING") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Outgoing Dispatch", fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CARD MODULE 1: PRODUCT SELECTION GRID
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. Carpet Catalog Category",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(
                        onClick = {
                            newStyleName = ""
                            newStyleCategory = if (allCategories.contains(groupSelectedTab)) groupSelectedTab else "FELT"
                            isCustomCategorySelected = false
                            customCategoryInput = ""
                            showAddStyleDialog = true
                        },
                        modifier = Modifier.testTag("add_new_style_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add New Style", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add New Style", fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable selector for product group tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allCategories.forEach { cat ->
                        val isSelected = groupSelectedTab == cat
                        Button(
                            shape = RoundedCornerShape(20.dp),
                            onClick = {
                                groupSelectedTab = cat
                                // Auto set first product of that category
                                val firstVal = categoryToTypesMap[cat]?.firstOrNull() ?: ""
                                viewModel.updateSelectedType(firstVal)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Filter Selected: $groupSelectedTab",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Wrap layout chips for specific types
                val targetProductList = categoryToTypesMap[groupSelectedTab] ?: emptyList()
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    targetProductList.forEach { p ->
                        val isSelected = viewModel.selectedType == p
                        Surface(
                            onClick = { viewModel.updateSelectedType(p) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                1.dp, 
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.testTag("carpet_chip_${p.replace(" ", "_")}")
                        ) {
                            Text(
                                text = p,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CARD MODULE 2: SIZE CHIPS SELECTION
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                val durriTypes = categoryToTypesMap["DURRI"] ?: emptyList()
                val isSelectedTypeDurri = viewModel.selectedType == "DURRI" || durriTypes.contains(viewModel.selectedType)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSelectedTypeDurri) "2. Select Durri Dimension" else "2. Select Carpet Width Roll size",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(
                        onClick = {
                            newSizeValue = ""
                            newSizeIsDurri = isSelectedTypeDurri
                            showAddSizeDialog = true
                        },
                        modifier = Modifier.testTag("add_new_size_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add New Width", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isSelectedTypeDurri) "Add New Dimension" else "Add New Width", fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                val sizesToRender = if (isSelectedTypeDurri) {
                    durriSizes
                } else {
                    carpetSizes
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sizesToRender.forEach { s ->
                        val isSelected = viewModel.selectedSize == s
                        Surface(
                            onClick = { viewModel.selectedSize = s },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.testTag("size_chip_${s.replace("*", "_")}")
                        ) {
                            Text(
                                text = if (isSelectedTypeDurri) s else "${s}m",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CARD MODULE 3: QUANTITY COUNTER & REFERENCES
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "3. Ledger Particulars",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Quantity Row with Easy click modifiers
                Text("Quantity (No. of Rolls / Pieces):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Manual typing value field
                    OutlinedTextField(
                        value = viewModel.quantityText,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                viewModel.quantityText = input
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quantity_input_field"),
                        shape = RoundedCornerShape(8.dp),
                        placeholder = { Text("0") }
                    )

                    // Reset roll quantity
                    IconButton(
                        onClick = { viewModel.quantityText = "" },
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .size(54.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset quantity", tint = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Easy numeric increments/decrements chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IncrementChip(label = "-10", onClick = { viewModel.addQuantity(-10) }, modifier = Modifier.weight(1f))
                    IncrementChip(label = "-5", onClick = { viewModel.addQuantity(-5) }, modifier = Modifier.weight(1f))
                    IncrementChip(label = "-1", onClick = { viewModel.addQuantity(-1) }, modifier = Modifier.weight(1f))
                    IncrementChip(label = "+1", onClick = { viewModel.addQuantity(1) }, modifier = Modifier.weight(1f), isAdd = true)
                    IncrementChip(label = "+5", onClick = { viewModel.addQuantity(5) }, modifier = Modifier.weight(1f), isAdd = true)
                    IncrementChip(label = "+10", onClick = { viewModel.addQuantity(10) }, modifier = Modifier.weight(1f), isAdd = true)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Meter of Roll length input
                Text("Roll Lengths (Meter/Length of each roll):", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = viewModel.rollLengthsText,
                    onValueChange = { viewModel.rollLengthsText = it },
                    placeholder = { Text("e.g. 1, 6, 6, 6, 9 or enter a single value like 6") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("roll_lengths_input_field"),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                Text(
                    text = "Tip: Separate individual roll lengths with commas. Entering '6' when quantity is 3 defaults to 3 rolls of 6 meters length each.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Challan fields and Date Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Challan input
                    OutlinedTextField(
                        value = viewModel.challanNumber,
                        onValueChange = { viewModel.challanNumber = it },
                        label = { Text("Challan No / Bill Ref", fontSize = 11.sp) },
                        placeholder = { Text("e.g. #3045", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("challan_input_field"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Date Launcher
                    val dateCalendar = Calendar.getInstance()
                    val picker = DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val c = Calendar.getInstance().apply {
                                set(year, month, day)
                            }
                            viewModel.selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.time)
                        },
                        dateCalendar.get(Calendar.YEAR),
                        dateCalendar.get(Calendar.MONTH),
                        dateCalendar.get(Calendar.DAY_OF_MONTH)
                    )

                    OutlinedTextField(
                        value = viewModel.selectedDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Log Entry Date", fontSize = 11.sp) },
                        trailingIcon = {
                            IconButton(onClick = { picker.show() }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.5f)
                            .clickable { picker.show() }
                            .testTag("date_input_view"),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Party/Supplier/Customer Name Remarks
                OutlinedTextField(
                    value = viewModel.remarks,
                    onValueChange = { viewModel.remarks = it },
                    label = { Text("Customer Name / Supplier Supplier Details") },
                    placeholder = { Text("e.g. Ramesh Kumar, Sohan Lal and Sons, Damaged replacement") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("remarks_input_field"),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BIG SUBMIT ACTION BUTTON
        val btnColor = if (viewModel.transactionDirection == "INCOMING") Color(0xFF2E7D32) else Color(0xFFD84315)
        Button(
            onClick = { viewModel.submitTransaction() },
            colors = ButtonDefaults.buttonColors(containerColor = btnColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("submit_entry_button")
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (viewModel.transactionDirection == "INCOMING") "RECORD STOCK INCOMING (+)" else "RECORD STOCK OUTGOING (-)",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        if (showAddStyleDialog) {
            AlertDialog(
                onDismissRequest = { showAddStyleDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Carpet / Fabric", style = MaterialTheme.typography.titleMedium)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Register a brand new carpet style or catalog product. It will instantly show up in stock sheets and logging.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        OutlinedTextField(
                            value = newStyleName,
                            onValueChange = { newStyleName = it },
                            label = { Text("Carpet Style Name") },
                            placeholder = { Text("e.g. FELT PINK") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("new_style_name_input"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        Text("Select Catalog Category Group:", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allCategories.forEach { cat ->
                                val isSelected = !isCustomCategorySelected && newStyleCategory == cat
                                Surface(
                                    onClick = {
                                        isCustomCategorySelected = false
                                        newStyleCategory = cat
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                            
                            // + Custom Category Group Chip
                            val isCustomChipSelected = isCustomCategorySelected
                            Surface(
                                onClick = {
                                    isCustomCategorySelected = true
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isCustomChipSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (isCustomChipSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(10.dp), tint = if (isCustomChipSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+ CUSTOM GROUP",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCustomChipSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        if (isCustomCategorySelected) {
                            OutlinedTextField(
                                value = customCategoryInput,
                                onValueChange = { customCategoryInput = it },
                                label = { Text("New Custom Category Group") },
                                placeholder = { Text("e.g. PRINT, VELORANT, FLANNEL") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("custom_category_input"),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val styleName = newStyleName.trim()
                            if (styleName.isEmpty()) {
                                viewModel.showMessage("Style name cannot be empty.", true)
                                return@Button
                            }
                            if (isCustomCategorySelected) {
                                val catName = customCategoryInput.trim().uppercase()
                                if (catName.isEmpty()) {
                                    viewModel.showMessage("Custom category name cannot be empty.", true)
                                    return@Button
                                }
                                viewModel.addNewCategory(catName)
                                viewModel.addNewCarpetType(styleName, catName)
                                showAddStyleDialog = false
                            } else {
                                viewModel.addNewCarpetType(styleName, newStyleCategory)
                                showAddStyleDialog = false
                            }
                        },
                        modifier = Modifier.testTag("confirm_add_style")
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddStyleDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showAddSizeDialog) {
            AlertDialog(
                onDismissRequest = { showAddSizeDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Width / Size", style = MaterialTheme.typography.titleMedium)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Insert custom widths (in meters, e.g. 1.50 or m sizes) or custom Durri dimensions (e.g. 8*10). It will instantly update stock grids and drop-downs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        OutlinedTextField(
                            value = newSizeValue,
                            onValueChange = { newSizeValue = it },
                            label = { Text("Size / Width value") },
                            placeholder = { Text(if (newSizeIsDurri) "e.g. 8*10" else "e.g. 1.50") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("new_size_value_input"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = newSizeIsDurri,
                                onCheckedChange = { newSizeIsDurri = it },
                                modifier = Modifier.testTag("new_size_durri_checkbox")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("This is a Durri Dimension", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newSizeValue.trim().isNotEmpty()) {
                                viewModel.addNewCarpetSize(newSizeValue, newSizeIsDurri)
                                showAddSizeDialog = false
                            } else {
                                viewModel.showMessage("Size/width value cannot be empty.", true)
                            }
                        },
                        modifier = Modifier.testTag("confirm_add_size")
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddSizeDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun IncrementChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAdd: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = if (isAdd) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        border = BorderStroke(0.5.dp, if (isAdd) Color(0xFF81C784) else Color(0xFFE57373)),
        modifier = modifier.height(30.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = if (isAdd) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}


// ------------------ SECTION 3: TRANSACTION LEDGER LOG ------------------
@Composable
fun TxLedgerScreen(
    allTransactions: List<TransactionEntity>,
    viewModel: CarpetViewModel,
    onDeleteClick: (TransactionEntity) -> Unit
) {
    val categoryToTypesMap by viewModel.categoryToTypesMap.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()

    val filteredTransactions = remember(
        allTransactions, 
        viewModel.ledgerSearchQuery, 
        viewModel.ledgerFilterDirection, 
        viewModel.ledgerTypeFilter,
        categoryToTypesMap
    ) {
        allTransactions.filter { tx ->
            val matchesSearch = tx.carpetType.contains(viewModel.ledgerSearchQuery, ignoreCase = true) ||
                    tx.remarks.contains(viewModel.ledgerSearchQuery, ignoreCase = true) ||
                    tx.challanNumber.contains(viewModel.ledgerSearchQuery, ignoreCase = true)
                    
            val matchesDirection = when (viewModel.ledgerFilterDirection) {
                "INCOMING" -> tx.direction == "INCOMING"
                "OUTGOING" -> tx.direction == "OUTGOING"
                else -> true
            }

            val matchesCategory = if (viewModel.ledgerTypeFilter == "ALL") {
                true
            } else {
                val typesInCat = categoryToTypesMap[viewModel.ledgerTypeFilter] ?: emptyList()
                typesInCat.contains(tx.carpetType)
            }

            matchesSearch && matchesDirection && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Floating header controls
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Search Input Ledgers
                OutlinedTextField(
                    value = viewModel.ledgerSearchQuery,
                    onValueChange = { viewModel.ledgerSearchQuery = it },
                    placeholder = { Text("Search comments, names, challans...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (viewModel.ledgerSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.ledgerSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ledger_search_box"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable filters toggles row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Flow In/Out toggle chips
                    Column(verticalArrangement = Arrangement.Center) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val dirs = listOf("ALL", "INCOMING", "OUTGOING")
                            dirs.forEach { dir ->
                                FilterChip(
                                    selected = viewModel.ledgerFilterDirection == dir,
                                    onClick = { viewModel.ledgerFilterDirection = dir },
                                    label = {
                                        Text(
                                            text = when (dir) {
                                                "INCOMING" -> "IN 📥"
                                                "OUTGOING" -> "OUT 📤"
                                                else -> "All Flows"
                                            },
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (dir == "INCOMING") Color(0xFFE8F5E9) else if (dir == "OUTGOING") Color(0xFFFFE0B2) else MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = if (dir == "INCOMING") Color(0xFF2E7D32) else if (dir == "OUTGOING") Color(0xFFE65100) else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    // Separation line spacer
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    // Type catalogs filter row
                    val catFilters = listOf("ALL") + allCategories
                    catFilters.forEach { cat ->
                        FilterChip(
                            selected = viewModel.ledgerTypeFilter == cat,
                            onClick = { viewModel.ledgerTypeFilter = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Ledger total matched indicators
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Ledger Records (${filteredTransactions.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Row {
                val context = LocalContext.current
                val exportPdfLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("application/pdf")
                ) { uri ->
                    if (uri != null) {
                        viewModel.exportLedgerToPdf(uri, context, filteredTransactions)
                    }
                }
                TextButton(
                    onClick = {
                        val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
                        exportPdfLauncher.launch("ledger_export_${sdf.format(Date())}.pdf")
                    }
                ) {
                    Text("Export PDF", fontSize = 12.sp)
                }
                
                if (filteredTransactions.size != allTransactions.size) {
                    TextButton(
                        onClick = {
                            viewModel.ledgerSearchQuery = ""
                            viewModel.ledgerFilterDirection = "ALL"
                            viewModel.ledgerTypeFilter = "ALL"
                        }
                    ) {
                        Text("Clear Filter", fontSize = 12.sp)
                    }
                }
            }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // List container
        if (filteredTransactions.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Ledger is empty",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Adjust filters or log a daily carpet trans.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { tx ->
                    LedgerTxRowCard(tx = tx, onDelete = { onDeleteClick(tx) })
                }
            }
        }
    }
}

@Composable
fun LedgerTxRowCard(
    tx: TransactionEntity,
    onDelete: () -> Unit
) {
    val isInflow = tx.direction == "INCOMING"
    val flowColor = if (isInflow) Color(0xFF2E7D32) else Color(0xFFC62828)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ledger_item_${tx.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flow visual direction indicator badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = flowColor.copy(alpha = 0.1f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isInflow) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = if (isInflow) "Inflow Rec" else "Outflow Disp",
                        tint = flowColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text info column block
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tx.carpetType,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${if (isInflow) "+" else "-"}${tx.quantity} Rolls",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = flowColor
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Size: ${tx.size}${if (tx.carpetType == "DURRI") "" else "m"}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tx.transactionDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Conditionally show Challan Reference, Remarks, and Roll Lengths
                if (tx.challanNumber.isNotEmpty() || tx.remarks.isNotEmpty() || tx.rollLengths.trim().isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        if (tx.challanNumber.isNotEmpty()) {
                            Text(
                                text = "Ref/Challan: ${tx.challanNumber}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (tx.remarks.isNotEmpty()) {
                            Text(
                                text = "Note: ${tx.remarks}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (tx.rollLengths.trim().isNotEmpty()) {
                            Text(
                                text = "Roll Length(s): ${tx.rollLengths}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Delete Record button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Record",
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun StylesCatalogScreen(viewModel: CarpetViewModel) {
    val context = LocalContext.current
    val allCategories by viewModel.allCategories.collectAsState()
    val categoryToTypesMap by viewModel.categoryToTypesMap.collectAsState()
    val carpetImagesMap by viewModel.carpetImagesMap.collectAsState()

    var stylesFilterCategory by remember { mutableStateOf("ALL") }
    var stylesSearchQuery by remember { mutableStateOf("") }
    
    // Track which style is being uploaded
    var selectedStyleForUpload by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        val style = selectedStyleForUpload
        if (uri != null && !style.isNullOrEmpty()) {
            viewModel.handleAndSaveImageUri(context, uri, style)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Card Title & Header
        Text(
            text = "Carpet Styles Catalog",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Configure and manage photographs for each carpet and fabric style in your stock ledger.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filters card
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Search Input
                OutlinedTextField(
                    value = stylesSearchQuery,
                    onValueChange = { stylesSearchQuery = it },
                    placeholder = { Text("Filter styles by keyword...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (stylesSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { stylesSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("catalog_search_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable category filter row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val categories = listOf("ALL") + allCategories
                    categories.forEach { cat ->
                        FilterChip(
                            selected = stylesFilterCategory == cat,
                            onClick = { stylesFilterCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Compute Styles
        val filteredStyles = remember(stylesFilterCategory, stylesSearchQuery, categoryToTypesMap) {
            // First get the raw items list in the selected category
            val list = if (stylesFilterCategory == "ALL") {
                categoryToTypesMap.values.flatten().distinct()
            } else {
                categoryToTypesMap[stylesFilterCategory] ?: emptyList()
            }
            
            // Search filter
            if (stylesSearchQuery.isEmpty()) {
                list
            } else {
                list.filter { it.contains(stylesSearchQuery, ignoreCase = true) }
            }
        }

        if (filteredStyles.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No styles match your search criteria.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredStyles) { styleName ->
                    val imageUri = carpetImagesMap[styleName.uppercase()]
                    val styleCategory = remember(styleName, categoryToTypesMap) {
                        categoryToTypesMap.entries.firstOrNull { it.value.contains(styleName) }?.key ?: "OTHERS"
                    }

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("style_card_${styleName.replace(" ", "_")}"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column for Texts & upload/delete controls
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.padding(2.dp)
                                    ) {
                                        Text(
                                            text = styleCategory,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = styleName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            selectedStyleForUpload = styleName
                                            imagePickerLauncher.launch("image/*")
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("upload_btn_${styleName.replace(" ", "_")}"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share, // Upload icon
                                            contentDescription = "Pick Photo",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (imageUri.isNullOrEmpty()) "Add Photo" else "Change",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }

                                    if (!imageUri.isNullOrEmpty()) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.deleteCarpetImage(styleName)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("delete_photo_btn_${styleName.replace(" ", "_")}"),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove Photo",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Delete",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                }
                            }

                            // Block side image preview
                            Surface(
                                modifier = Modifier.size(80.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                if (!imageUri.isNullOrEmpty() && java.io.File(imageUri).exists()) {
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = "Preview of $styleName",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "No photograph icon",
                                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
