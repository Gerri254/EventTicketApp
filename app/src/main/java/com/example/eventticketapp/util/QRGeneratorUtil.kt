package com.example.eventticketapp.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.EnumMap
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class QRCodeContent(
    val ticketId: String,
    val eventId: String,
    val userId: String
)

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
        val content = QRCodeContent(ticketId, eventId, userId)
        return Json.encodeToString(content)
    }

    fun parseQRCodeData(qrCodeData: String): Map<String, String>? {
        return try {
            val content = Json.decodeFromString<QRCodeContent>(qrCodeData)
            mapOf(
                "ticketId" to content.ticketId,
                "eventId" to content.eventId,
                "userId" to content.userId
            )
        } catch (e: Exception) {
            null
        }
    }
}