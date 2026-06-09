package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carpet_transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val transactionDate: String, // format YYYY-MM-DD
    val carpetType: String,      // product name e.g. "FELT GREEN"
    val size: String,            // size value e.g. "0.61" or "10*10"
    val direction: String,       // "INCOMING" (Stock In) or "OUTGOING" (Stock Out)
    val quantity: Int,           // quantity of rolls/pieces
    val challanNumber: String = "", // bill or gatepass reference
    val remarks: String = "",     // additional details (e.g. party name)
    val rollLengths: String = ""   // comma-separated roll lengths, e.g. "1.5,6,6,9"
)
