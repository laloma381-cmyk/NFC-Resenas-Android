package com.nfcresenas.app.ui

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

object QRCodeGenerator {
    fun generateQrBitmap(content: String, size: Int = 1024): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: WriterException) {
            null
        }
    }

    fun buildReviewMessage(name: String): String {
        return "Hola, nos gustaría conocer tu experiencia en $name. Si te ha parecido buena, te agradeceríamos mucho que dejaras una reseña honesta."
    }
}
