package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.example.data.*
import com.example.util.PdfExporter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CarpetViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TransactionRepository(
        database.transactionDao, 
        database.customCarpetTypeDao,
        database.customCarpetSizeDao,
        database.customCategoryDao,
        database.carpetImageDao
    )

    // Flow of custom carpet types
    val customCarpetTypesFlow: StateFlow<List<CustomCarpetTypeEntity>> = repository.allCustomTypes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customCategoriesFlow: StateFlow<List<CustomCategoryEntity>> = repository.allCustomCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val carpetImagesFlow: StateFlow<List<CarpetImageEntity>> = repository.allCarpetImages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val carpetImagesMap: StateFlow<Map<String, String>> = carpetImagesFlow
        .map { list -> list.associate { it.styleName.uppercase() to it.imageUri } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val allCategories: StateFlow<List<String>> = repository.allCustomCategories
        .map { customs ->
            val defaults = listOf("FELT", "VELLORE", "DURRI", "OTHERS")
            val customNames = customs.map { it.name.trim().uppercase() }
            (defaults + customNames).distinct()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("FELT", "VELLORE", "DURRI", "OTHERS"))

    val categoryToTypesMap: StateFlow<Map<String, List<String>>> = repository.allCustomTypes
        .combine(allCategories) { customTypes, cats ->
            val result = mutableMapOf<String, List<String>>()
            cats.forEach { cat ->
                val hardcoded = when (cat) {
                    "FELT" -> CarpetConstants.FELT_TYPES
                    "VELLORE" -> CarpetConstants.VELLORE_TYPES
                    "DURRI" -> CarpetConstants.DURRI_TYPES
                    "OTHERS" -> CarpetConstants.OTHER_TYPES
                    else -> emptyList()
                }
                val customs = customTypes.filter { it.category.trim().uppercase() == cat }.map { it.name.trim().uppercase() }
                result[cat] = (hardcoded + customs).distinct()
            }
            result
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val feltTypes: StateFlow<List<String>> = categoryToTypesMap
        .map { it["FELT"] ?: CarpetConstants.FELT_TYPES }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CarpetConstants.FELT_TYPES)

    val velloreTypes: StateFlow<List<String>> = categoryToTypesMap
        .map { it["VELLORE"] ?: CarpetConstants.VELLORE_TYPES }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CarpetConstants.VELLORE_TYPES)

    val otherTypes: StateFlow<List<String>> = categoryToTypesMap
        .map { it["OTHERS"] ?: CarpetConstants.OTHER_TYPES }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CarpetConstants.OTHER_TYPES)

    val durriTypes: StateFlow<List<String>> = categoryToTypesMap
        .map { it["DURRI"] ?: CarpetConstants.DURRI_TYPES }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CarpetConstants.DURRI_TYPES)

    val allTypes: StateFlow<List<String>> = categoryToTypesMap
        .map { map -> map.values.flatten().distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CarpetConstants.ALL_TYPES)

    // Flow of custom carpet sizes
    val customCarpetSizesFlow: StateFlow<List<CustomCarpetSizeEntity>> = repository.allCustomSizes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val carpetSizes: StateFlow<List<String>> = repository.allCustomSizes
        .map { custom ->
            CarpetConstants.CARPET_SIZES + custom.filter { !it.isDurri }.map { it.sizeValue.trim().uppercase() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CarpetConstants.CARPET_SIZES)

    val durriSizes: StateFlow<List<String>> = repository.allCustomSizes
        .map { custom ->
            CarpetConstants.DURRI_SIZES + custom.filter { it.isDurri }.map { it.sizeValue.trim().uppercase() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CarpetConstants.DURRI_SIZES)

    // Flow of all transactions
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Dynamic aggregated stock Map: [CarpetType -> [Size -> BalanceRollsCount]]
    val stockMap: StateFlow<Map<String, Map<String, Int>>> = repository.allTransactions
        .map { transactions ->
            transactions.groupBy { it.carpetType }
                .mapValues { typeGroup ->
                    typeGroup.value.groupBy { it.size }
                        .mapValues { sizeGroup ->
                            sizeGroup.value.sumOf { tx ->
                                if (tx.direction == "INCOMING") tx.quantity else -tx.quantity
                            }
                        }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Form states for additions and removals
    var selectedType by mutableStateOf("FELT GREEN")
    var selectedSize by mutableStateOf("1.22")
    var transactionDirection by mutableStateOf("INCOMING") // "INCOMING" or "OUTGOING"
    var quantityText by mutableStateOf("")
    var challanNumber by mutableStateOf("")
    var remarks by mutableStateOf("")
    var rollLengthsText by mutableStateOf("")
    
    // Ledger filtering and date picker
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var selectedDate by mutableStateOf(sdf.format(Date()))

    // Info or error banner status message
    var messageText by mutableStateOf<String?>(null)
    var isMessageError by mutableStateOf(false)

    // Active Tab in Dashboard: "STOCK" (Grid layout), "LOG" (Input forms), "LEDGER" (Detailed lists)
    var currentDashboardTab by mutableStateOf("STOCK")

    // Ledger filters
    var ledgerSearchQuery by mutableStateOf("")
    var ledgerFilterDirection by mutableStateOf("ALL") // "ALL", "INCOMING", "OUTGOING"
    var ledgerTypeFilter by mutableStateOf("ALL") // "ALL", "FELT", "VELLORE", "DURRI", "OTHERS"

    init {
        updateDefaultSizeForType("FELT GREEN")
    }

    fun updateSelectedType(type: String) {
        selectedType = type
        updateDefaultSizeForType(type)
    }

    fun updateDefaultSizeForType(type: String) {
        val isDurri = type == "DURRI" || durriTypes.value.contains(type)
        if (isDurri) {
            if (selectedSize !in durriSizes.value) {
                selectedSize = durriSizes.value.firstOrNull() ?: "12*12"
            }
        } else {
            if (selectedSize !in carpetSizes.value) {
                selectedSize = carpetSizes.value.firstOrNull() ?: "1.22"
            }
        }
    }

    fun addQuantity(amount: Int) {
        val current = quantityText.toIntOrNull() ?: 0
        val newValue = (current + amount).coerceAtLeast(0)
        quantityText = if (newValue == 0) "" else newValue.toString()
    }

    fun submitTransaction() {
        val qty = quantityText.toIntOrNull() ?: 0
        if (qty <= 0) {
            showMessage("Please enter a valid quantity greater than 0.", true)
            return
        }

        // Validate stock before outgoing ledger confirmation
        if (transactionDirection == "OUTGOING") {
            val currentStockOfThisType = stockMap.value[selectedType]?.get(selectedSize) ?: 0
            if (qty > currentStockOfThisType) {
                showMessage("Insufficient stock! Available rolls of $selectedType at width ${selectedSize}: $currentStockOfThisType.", true)
                return
            }
        }

        viewModelScope.launch {
            try {
                val transaction = TransactionEntity(
                    transactionDate = selectedDate,
                    carpetType = selectedType,
                    size = selectedSize,
                    direction = transactionDirection,
                    quantity = qty,
                    challanNumber = challanNumber.trim(),
                    remarks = remarks.trim(),
                    rollLengths = rollLengthsText.trim()
                )
                repository.insert(transaction)
                showMessage("Stock ${if (transactionDirection == "INCOMING") "received" else "dispatched"} successfully!", false)
                
                // Clear quantity and remarks, keeping challan/date and product for rapid batch booking
                quantityText = ""
                remarks = ""
                rollLengthsText = ""
            } catch (e: Exception) {
                showMessage("Error adding trans: ${e.message}", true)
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                // If we delete an INCOMING transaction, it reduces the total stock.
                // We must ensure the stock doesn't go below 0 after deletion.
                if (transaction.direction == "INCOMING") {
                    val currentStock = stockMap.value[transaction.carpetType]?.get(transaction.size) ?: 0
                    if (currentStock - transaction.quantity < 0) {
                        showMessage("Cannot delete: This would cause negative stock balance.", true)
                        return@launch
                    }
                }
                repository.delete(transaction)
                showMessage("Transaction record deleted successfully.", false)
            } catch (e: Exception) {
                showMessage("Error deleting transaction: ${e.message}", true)
            }
        }
    }

    fun showMessage(msg: String, isErr: Boolean) {
        messageText = msg
        isMessageError = isErr
    }

    fun clearMessage() {
        messageText = null
    }

    fun addNewCategory(name: String) {
        val trimmedName = name.trim().uppercase()
        if (trimmedName.isEmpty()) {
            return
        }
        viewModelScope.launch {
            try {
                repository.insertCustomCategory(
                    CustomCategoryEntity(
                        name = trimmedName
                    )
                )
            } catch (e: Exception) {
                showMessage("Failed to save category group: ${e.message}", true)
            }
        }
    }

    fun addNewCarpetType(name: String, category: String) {
        val trimmedName = name.trim().uppercase()
        if (trimmedName.isEmpty()) {
            showMessage("Carpet name cannot be empty.", true)
            return
        }
        if (allTypes.value.contains(trimmedName)) {
            showMessage("Carpet style \"$trimmedName\" already exists!", true)
            return
        }
        viewModelScope.launch {
            try {
                repository.insertCustomType(
                    CustomCarpetTypeEntity(
                        name = trimmedName,
                        category = category.uppercase()
                    )
                )
                showMessage("New style \"$trimmedName\" added successfully!", false)
            } catch (e: Exception) {
                showMessage("Failed to save style: ${e.message}", true)
            }
        }
    }

    fun deleteCustomCarpetType(customType: CustomCarpetTypeEntity) {
        viewModelScope.launch {
            try {
                // Check if any transaction exists for this type
                val txCount = allTransactions.value.count { it.carpetType.uppercase() == customType.name.uppercase() }
                if (txCount > 0) {
                    showMessage("Cannot delete: Used in $txCount transaction(s).", true)
                    return@launch
                }
                repository.deleteCustomType(customType)
                showMessage("Custom style \"${customType.name}\" deleted.", false)
            } catch (e: Exception) {
                showMessage("Failed to delete style: ${e.message}", true)
            }
        }
    }

    fun addNewCarpetSize(sizeValue: String, isDurri: Boolean) {
        val trimmedSize = sizeValue.trim().uppercase()
        if (trimmedSize.isEmpty()) {
            showMessage("Size/width value cannot be empty.", true)
            return
        }
        val currentList = if (isDurri) durriSizes.value else carpetSizes.value
        if (currentList.contains(trimmedSize)) {
            showMessage("Size \"$trimmedSize\" already exists!", true)
            return
        }
        viewModelScope.launch {
            try {
                repository.insertCustomSize(
                    CustomCarpetSizeEntity(
                        sizeValue = trimmedSize,
                        isDurri = isDurri
                    )
                )
                showMessage("New size \"$trimmedSize\" added successfully!", false)
                selectedSize = trimmedSize
            } catch (e: Exception) {
                showMessage("Failed to save size: ${e.message}", true)
            }
        }
    }

    fun deleteCustomCarpetSize(customSize: CustomCarpetSizeEntity) {
        viewModelScope.launch {
            try {
                // Check if any transaction exists for this size
                val txCount = allTransactions.value.count { it.size.uppercase() == customSize.sizeValue.uppercase() }
                if (txCount > 0) {
                    showMessage("Cannot delete: Used in $txCount transaction(s).", true)
                    return@launch
                }
                repository.deleteCustomSize(customSize)
                showMessage("Custom size \"${customSize.sizeValue}\" deleted.", false)
            } catch (e: Exception) {
                showMessage("Failed to delete size: ${e.message}", true)
            }
        }
    }

    fun parseRollLengths(rollLengthsStr: String, quantity: Int): List<Double> {
        if (rollLengthsStr.trim().isEmpty()) {
            return List(quantity) { -1.0 }
        }
        val parts = rollLengthsStr.split(Regex("[,;\\s]+")).map { it.trim() }.filter { it.isNotEmpty() }
        val numbers = parts.mapNotNull { it.toDoubleOrNull() }
        
        if (numbers.isEmpty()) {
            return List(quantity) { -1.0 }
        }
        
        if (numbers.size == 1 && quantity > 1) {
            return List(quantity) { numbers[0] }
        }
        
        if (numbers.size < quantity) {
            return numbers + List(quantity - numbers.size) { -1.0 }
        }
        
        return numbers.take(quantity)
    }

    fun formatRollLength(length: Double): String {
        if (length <= 0) return "Unspecified"
        return if (length % 1.0 == 0.0) {
            "${length.toInt()}m"
        } else {
            "${length}m"
        }
    }

    fun getRollLengthsInStock(
        carpetType: String, 
        size: String, 
        allTransactionsList: List<TransactionEntity>
    ): List<Pair<Double, Int>> {
        val activeRolls = mutableListOf<Double>()
        
        val sortedTransactions = allTransactionsList
            .filter { it.carpetType == carpetType && it.size == size }
            .sortedBy { it.id } // simple chronological order based on auto-generated primary key ID
            
        for (tx in sortedTransactions) {
            val qty = tx.quantity
            val parsed = parseRollLengths(tx.rollLengths, qty)
            
            if (tx.direction == "INCOMING") {
                activeRolls.addAll(parsed)
            } else {
                for (len in parsed) {
                    val idx = activeRolls.indexOf(len)
                    if (idx != -1) {
                        activeRolls.removeAt(idx)
                    } else {
                        val unspecifiedIdx = activeRolls.indexOf(-1.0)
                        if (unspecifiedIdx != -1) {
                            activeRolls.removeAt(unspecifiedIdx)
                        } else if (activeRolls.isNotEmpty()) {
                            activeRolls.removeAt(0)
                        }
                    }
                }
            }
        }
        
        val counts = activeRolls.groupBy { it }.mapValues { it.value.size }
        
        return counts.toList().sortedWith { a, b ->
            if (a.first == -1.0) 1
            else if (b.first == -1.0) -1
            else a.first.compareTo(b.first)
        }
    }

    fun getBackupJsonString(): String {
        val root = org.json.JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        // 1. Categories
        val catsArray = org.json.JSONArray()
        customCategoriesFlow.value.forEach { cat ->
            val obj = org.json.JSONObject()
            obj.put("name", cat.name)
            catsArray.put(obj)
        }
        root.put("custom_categories", catsArray)

        // 2. Types
        val typesArray = org.json.JSONArray()
        customCarpetTypesFlow.value.forEach { type ->
            val obj = org.json.JSONObject()
            obj.put("name", type.name)
            obj.put("category", type.category)
            typesArray.put(obj)
        }
        root.put("custom_carpet_types", typesArray)

        // 3. Sizes
        val sizesArray = org.json.JSONArray()
        customCarpetSizesFlow.value.forEach { size ->
            val obj = org.json.JSONObject()
            obj.put("sizeValue", size.sizeValue)
            obj.put("isDurri", size.isDurri)
            sizesArray.put(obj)
        }
        root.put("custom_carpet_sizes", sizesArray)

        // 4. Transactions
        val txsArray = org.json.JSONArray()
        allTransactions.value.forEach { tx ->
            val obj = org.json.JSONObject()
            obj.put("timestamp", tx.timestamp)
            obj.put("transactionDate", tx.transactionDate)
            obj.put("carpetType", tx.carpetType)
            obj.put("size", tx.size)
            obj.put("direction", tx.direction)
            obj.put("quantity", tx.quantity)
            obj.put("challanNumber", tx.challanNumber)
            obj.put("remarks", tx.remarks)
            obj.put("rollLengths", tx.rollLengths)
            txsArray.put(obj)
        }
        root.put("carpet_transactions", txsArray)

        // 5. Carpet Images
        val imagesArray = org.json.JSONArray()
        carpetImagesFlow.value.forEach { img ->
            val obj = org.json.JSONObject()
            obj.put("styleName", img.styleName)
            obj.put("imageUri", img.imageUri)
            imagesArray.put(obj)
        }
        root.put("carpet_images", imagesArray)

        return root.toString(4)
    }

    fun restoreFromJsonString(jsonString: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val root = org.json.JSONObject(jsonString)
                
                // 1. Parse custom categories
                val categoriesList = mutableListOf<CustomCategoryEntity>()
                val catsArray = root.optJSONArray("custom_categories")
                if (catsArray != null) {
                    for (i in 0 until catsArray.length()) {
                        val obj = catsArray.getJSONObject(i)
                        categoriesList.add(CustomCategoryEntity(
                            name = obj.getString("name").trim().uppercase()
                        ))
                    }
                }

                // 2. Parse custom types
                val typesList = mutableListOf<CustomCarpetTypeEntity>()
                val typesArray = root.optJSONArray("custom_carpet_types")
                if (typesArray != null) {
                    for (i in 0 until typesArray.length()) {
                        val obj = typesArray.getJSONObject(i)
                        typesList.add(CustomCarpetTypeEntity(
                            name = obj.getString("name").trim().uppercase(),
                            category = obj.getString("category").trim().uppercase()
                        ))
                    }
                }

                // 3. Parse custom sizes
                val sizesList = mutableListOf<CustomCarpetSizeEntity>()
                val sizesArray = root.optJSONArray("custom_carpet_sizes")
                if (sizesArray != null) {
                    for (i in 0 until sizesArray.length()) {
                        val obj = sizesArray.getJSONObject(i)
                        sizesList.add(CustomCarpetSizeEntity(
                            sizeValue = obj.getString("sizeValue").trim().uppercase(),
                            isDurri = obj.getBoolean("isDurri")
                        ))
                    }
                }

                // 4. Parse transactions
                val txsList = mutableListOf<TransactionEntity>()
                val txsArray = root.optJSONArray("carpet_transactions")
                if (txsArray != null) {
                    for (i in 0 until txsArray.length()) {
                        val obj = txsArray.getJSONObject(i)
                        txsList.add(TransactionEntity(
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            transactionDate = obj.getString("transactionDate"),
                            carpetType = obj.getString("carpetType"),
                            size = obj.getString("size"),
                            direction = obj.getString("direction"),
                            quantity = obj.getInt("quantity"),
                            challanNumber = obj.optString("challanNumber", ""),
                            remarks = obj.optString("remarks", ""),
                            rollLengths = obj.optString("rollLengths", "")
                        ))
                    }
                }

                // 5. Parse carpet images
                val imagesList = mutableListOf<CarpetImageEntity>()
                val imagesArray = root.optJSONArray("carpet_images")
                if (imagesArray != null) {
                    for (i in 0 until imagesArray.length()) {
                        val obj = imagesArray.getJSONObject(i)
                        imagesList.add(CarpetImageEntity(
                            styleName = obj.getString("styleName").trim().uppercase(),
                            imageUri = obj.getString("imageUri")
                        ))
                    }
                }

                // Wipe and insert
                repository.deleteAllData()

                // Insert all categories
                categoriesList.forEach {
                    repository.insertCustomCategory(it)
                }

                // Insert all types
                typesList.forEach {
                    repository.insertCustomType(it)
                }

                // Insert all sizes
                sizesList.forEach {
                    repository.insertCustomSize(it)
                }

                // Insert all transactions
                txsList.forEach {
                    repository.insert(it)
                }

                // Insert all carpet images
                imagesList.forEach {
                    repository.insertCarpetImage(it)
                }

                showMessage("Database restored successfully!", false)
                onSuccess()

            } catch (e: Exception) {
                showMessage("Restore failed: ${e.message}", true)
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun exportToUri(uri: android.net.Uri, context: android.content.Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val jsonStr = getBackupJsonString()
                val outputStream = context.contentResolver.openOutputStream(uri)
                outputStream?.use { stream ->
                    stream.bufferedWriter().use { it.write(jsonStr) }
                } ?: throw Exception("Failed to open output destination")
                
                showMessage("Backup file exported successfully!", false)
                onSuccess()
            } catch (e: Exception) {
                showMessage("Failed to write backup file: ${e.message}", true)
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun restoreFromUri(uri: android.net.Uri, context: android.content.Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonString = inputStream?.use { stream ->
                    stream.bufferedReader().use { it.readText() }
                } ?: throw Exception("Failed to open backup file")
                
                restoreFromJsonString(jsonString, onSuccess, onError)
            } catch (e: Exception) {
                showMessage("Failed to read backup file: ${e.message}", true)
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun updateCarpetImage(styleName: String, imageUri: String) {
        viewModelScope.launch {
            repository.insertCarpetImage(CarpetImageEntity(styleName.trim().uppercase(), imageUri))
        }
    }

    fun deleteCarpetImage(styleName: String) {
        viewModelScope.launch {
            repository.deleteCarpetImage(styleName.trim().uppercase())
        }
    }

    fun handleAndSaveImageUri(context: android.content.Context, uri: android.net.Uri, styleName: String) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val imagesDir = java.io.File(context.filesDir, "carpet_images")
                    if (!imagesDir.exists()) {
                        imagesDir.mkdirs()
                    }
                    val cleanStyle = styleName.replace(Regex("[^A-Za-z0-9_]"), "_").uppercase()
                    val file = java.io.File(imagesDir, "style_${cleanStyle}_${System.currentTimeMillis()}.jpg")
                    val outputStream = java.io.FileOutputStream(file)
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    val savedPath = file.absolutePath
                    updateCarpetImage(styleName, savedPath)
                    showMessage("Photo uploaded successfully for $styleName!", false)
                } else {
                    showMessage("Failed to open selected photograph input stream", true)
                }
            } catch (e: Exception) {
                showMessage("Failed to import selected photograph: ${e.message}", true)
            }
        }
    }

    fun exportLedgerToPdf(uri: Uri, context: android.content.Context, filteredTransactions: List<TransactionEntity>) {
        viewModelScope.launch {
            PdfExporter.exportTransactionsToPdf(context, uri, filteredTransactions) { success, error ->
                if (success) {
                    showMessage("PDF Ledger exported successfully!", false)
                } else {
                    showMessage("Failed to export PDF: $error", true)
                }
            }
        }
    }
}
