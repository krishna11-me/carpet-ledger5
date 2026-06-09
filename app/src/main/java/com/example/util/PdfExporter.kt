package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.data.TransactionEntity
import java.io.OutputStream

object PdfExporter {

    fun exportTransactionsToPdf(
        context: Context,
        uri: Uri,
        transactions: List<TransactionEntity>,
        onResult: (Boolean, String?) -> Unit
    ) {
        val document = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 24f
            color = Color.BLACK
        }

        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            color = Color.BLACK
        }

        val textPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 10f
            color = Color.BLACK
        }

        val linePaint = Paint().apply {
            color = Color.GRAY
            strokeWidth = 1f
        }

        var yPosition = 50f
        canvas.drawText("MANNONWOVEN Stock Ledger", 50f, yPosition, titlePaint)
        yPosition += 40f

        // Draw headers
        canvas.drawText("Date", 50f, yPosition, headerPaint)
        canvas.drawText("Product", 150f, yPosition, headerPaint)
        canvas.drawText("Size", 280f, yPosition, headerPaint)
        canvas.drawText("Type", 360f, yPosition, headerPaint)
        canvas.drawText("Qty", 440f, yPosition, headerPaint)
        canvas.drawText("Challan", 490f, yPosition, headerPaint)

        yPosition += 10f
        canvas.drawLine(50f, yPosition, 545f, yPosition, linePaint)
        yPosition += 20f

        for (tx in transactions) {
            if (yPosition > pageHeight - 50f) {
                document.finishPage(page)
                pageNumber++
                val newPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(newPageInfo)
                canvas = page.canvas
                yPosition = 50f
                canvas.drawText("MANNONWOVEN Stock Ledger (Cont.)", 50f, yPosition, titlePaint)
                yPosition += 40f
                canvas.drawText("Date", 50f, yPosition, headerPaint)
                canvas.drawText("Product", 150f, yPosition, headerPaint)
                canvas.drawText("Size", 280f, yPosition, headerPaint)
                canvas.drawText("Type", 360f, yPosition, headerPaint)
                canvas.drawText("Qty", 440f, yPosition, headerPaint)
                canvas.drawText("Challan", 490f, yPosition, headerPaint)
                yPosition += 10f
                canvas.drawLine(50f, yPosition, 545f, yPosition, linePaint)
                yPosition += 20f
            }

            canvas.drawText(tx.transactionDate, 50f, yPosition, textPaint)
            // Truncate product name if too long
            var prodName = tx.carpetType
            if (prodName.length > 20) prodName = prodName.substring(0, 18) + ".."
            canvas.drawText(prodName, 150f, yPosition, textPaint)
            canvas.drawText(tx.size, 280f, yPosition, textPaint)
            
            if (tx.direction == "INCOMING") {
                textPaint.color = Color.parseColor("#2E7D32")
            } else {
                textPaint.color = Color.parseColor("#C62828")
            }
            canvas.drawText(tx.direction, 360f, yPosition, textPaint)
            textPaint.color = Color.BLACK
            
            canvas.drawText("${tx.quantity}", 440f, yPosition, textPaint)
            
            var challan = tx.challanNumber
            if (challan.length > 10) challan = challan.substring(0, 8) + ".."
            canvas.drawText(challan, 490f, yPosition, textPaint)

            yPosition += 20f
        }

        document.finishPage(page)

        try {
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                document.writeTo(outputStream)
                outputStream.close()
                document.close()
                onResult(true, null)
            } else {
                document.close()
                onResult(false, "Failed to open output stream")
            }
        } catch (e: Exception) {
            document.close()
            onResult(false, e.message)
        }
    }
}
