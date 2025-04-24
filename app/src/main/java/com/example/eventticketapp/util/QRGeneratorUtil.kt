package com.example.eventticketapp.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QRGeneratorUtil @Inject constructor() {

    fun generateQRCode(data: String, size: Int = 512): Bitmap {
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            put(EncodeHintType.MARGIN, 1)
        }

        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size, hints)

        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }

    fun generateTicketQRCodeData(ticketId: String, eventId: String, userId: String): String {
        // Create a JSON-like string to encode ticket information
        return "{\"ticketId\":\"$ticketId\",\"eventId\":\"$eventId\",\"userId\":\"$userId\"}"
    }

    fun parseQRCodeData(qrCodeData: String): Map<String, String>? {
        return try {
            // Simple JSON parser for our format - in a real app, use a proper JSON parser
            val regex = "\\{\"ticketId\":\"([^\"]+)\",\"eventId\":\"([^\"]+)\",\"userId\":\"([^\"]+)\"\\}"
            val matchResult = Regex(regex).find(qrCodeData)

            if (matchResult != null) {
                val (ticketId, eventId, userId) = matchResult.destructured
                mapOf(
                    "ticketId" to ticketId,
                    "eventId" to eventId,
                    "userId" to userId
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}