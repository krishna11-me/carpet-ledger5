package com.example.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val dao: TransactionDao,
    private val customDao: CustomCarpetTypeDao,
    private val sizeDao: CustomCarpetSizeDao,
    private val categoryDao: CustomCategoryDao,
    private val imageDao: CarpetImageDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val allCustomTypes: Flow<List<CustomCarpetTypeEntity>> = customDao.getAllCustomTypes()
    val allCustomSizes: Flow<List<CustomCarpetSizeEntity>> = sizeDao.getAllCustomSizes()
    val allCustomCategories: Flow<List<CustomCategoryEntity>> = categoryDao.getAllCustomCategories()
    val allCarpetImages: Flow<List<CarpetImageEntity>> = imageDao.getAllCarpetImages()

    suspend fun insert(transaction: TransactionEntity) {
        dao.insertTransaction(transaction)
    }

    suspend fun update(transaction: TransactionEntity) {
        dao.updateTransaction(transaction)
    }

    suspend fun delete(transaction: TransactionEntity) {
        dao.deleteTransaction(transaction)
    }

    suspend fun deleteById(id: Int) {
        dao.deleteTransactionById(id)
    }

    fun getTransactionsForDate(date: String): Flow<List<TransactionEntity>> {
        return dao.getTransactionsForDate(date)
    }

    suspend fun insertCustomType(customType: CustomCarpetTypeEntity) {
        customDao.insertCustomType(customType)
    }

    suspend fun deleteCustomType(customType: CustomCarpetTypeEntity) {
        customDao.deleteCustomType(customType)
    }

    suspend fun insertCustomSize(customSize: CustomCarpetSizeEntity) {
        sizeDao.insertCustomSize(customSize)
    }

    suspend fun deleteCustomSize(customSize: CustomCarpetSizeEntity) {
        sizeDao.deleteCustomSize(customSize)
    }

    suspend fun insertCustomCategory(customCategory: CustomCategoryEntity) {
        categoryDao.insertCustomCategory(customCategory)
    }

    suspend fun deleteCustomCategory(customCategory: CustomCategoryEntity) {
        categoryDao.deleteCustomCategory(customCategory)
    }

    suspend fun insertCarpetImage(carpetImage: CarpetImageEntity) {
        imageDao.insertCarpetImage(carpetImage)
    }

    suspend fun deleteCarpetImage(styleName: String) {
        imageDao.deleteCarpetImage(styleName)
    }

    suspend fun deleteAllCarpetImages() {
        imageDao.deleteAllCarpetImages()
    }

    suspend fun deleteAllData() {
        dao.deleteAllTransactions()
        customDao.deleteAllCustomTypes()
        sizeDao.deleteAllCustomSizes()
        categoryDao.deleteAllCustomCategories()
        imageDao.deleteAllCarpetImages()
    }
}
