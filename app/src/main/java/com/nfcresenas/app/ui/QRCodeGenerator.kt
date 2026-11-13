package com.nfcresenas.app.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlin.math.min

/** Ready-to-share color themes. Finder patterns remain standard for reliable scanning. */
data class QrPalette(
    val id: String,
    val name: String,
    val primary: Int,
    val background: Int,
    val ink: Int
)

object QRCodeGenerator {
    val palettes = listOf(
        QrPalette("ocean", "Azul", 0xFF0756C9.toInt(), 0xFFF1F6FF.toInt(), 0xFF152238.toInt()),
        QrPalette("midnight", "Noche", 0xFF172554.toInt(), 0xFFF4F3FF.toInt(), 0xFF211A38.toInt()),
        QrPalette("forest", "Verde", 0xFF176B52.toInt(), 0xFFF0F8F3.toInt(), 0xFF172C25.toInt())
    )

    /** Standard black-and-white QR helper, retained for any existing callers. */
    fun generateQrBitmap(content: String, size: Int = 1024): Bitmap? =
        createQrBitmap(content, size, android.graphics.Color.BLACK)

    /** Creates a branded poster containing a high-contrast, scannable QR code. */
    fun generateDesignedQrBitmap(content: String, businessName: String, palette: QrPalette): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val posterWidth = 1080
            val posterHeight = 1350
            val bitmap = Bitmap.createBitmap(posterWidth, posterHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(palette.background)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
            val card = RectF(44f, 44f, 1036f, 1306f)
            paint.color = android.graphics.Color.WHITE
            canvas.drawRoundRect(card, 52f, 52f, paint)

            paint.color = palette.primary
            canvas.drawRoundRect(RectF(44f, 44f, 1036f, 270f), 52f, 52f, paint)
            canvas.drawRect(44f, 210f, 1036f, 270f, paint)

            paint.color = android.graphics.Color.WHITE
            paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 42f
            canvas.drawText("REVIEWQR MANAGER", posterWidth / 2f, 132f, paint)
            paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            paint.textSize = 27f
            canvas.drawText("Una opinión puede hacer la diferencia", posterWidth / 2f, 190f, paint)

            paint.color = palette.ink
            paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
            paint.textSize = fittedTextSize(businessName, paint, 850f, 56f, 34f)
            canvas.drawText(businessName, posterWidth / 2f, 348f, paint)
            paint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            paint.textSize = 30f
            canvas.drawText("ESCANEA Y DEJA TU RESEÑA", posterWidth / 2f, 410f, paint)

            val qrSize = 780f
            val qrLeft = (posterWidth - qrSize) / 2f
            val qrTop = 458f
            paint.color = android.graphics.Color.WHITE
            canvas.drawRoundRect(RectF(qrLeft - 24f, qrTop - 24f, qrLeft + qrSize + 24f, qrTop + qrSize + 24f), 32f, 32f, paint)
            val qr = createQrBitmap(content, qrSize.toInt(), palette.primary) ?: return null
            canvas.drawBitmap(qr, null, RectF(qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize), null)
            qr.recycle()

            paint.color = palette.primary
            paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
            paint.textSize = 31f
            canvas.drawText("Gracias por ayudar a crecer a", posterWidth / 2f, 1285f, paint)
            paint.textSize = fittedTextSize(businessName, paint, 850f, 29f, 22f)
            canvas.drawText(businessName, posterWidth / 2f, 1325f, paint)
            bitmap
        } catch (_: WriterException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun createQrBitmap(content: String, size: Int, foreground: Int): Bitmap? {
        return try {
            val hints = mapOf<EncodeHintType, Any>(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 4,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val pixels = IntArray(size * size) { android.graphics.Color.WHITE }
            val moduleWidth = matrix.width
            val moduleHeight = matrix.height
            for (y in 0 until size) {
                val matrixY = y * moduleHeight / size
                for (x in 0 until size) {
                    if (matrix[x * moduleWidth / size, matrixY]) {
                        pixels[y * size + x] = foreground
                    }
                }
            }
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
            bitmap
        } catch (_: WriterException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun fittedTextSize(text: String, paint: Paint, maxWidth: Float, preferred: Float, minimum: Float): Float {
        var size = preferred
        while (size > minimum) {
            paint.textSize = size
            if (paint.measureText(text) <= maxWidth) break
            size -= 2f
        }
        return min(size, preferred)
    }

    fun buildReviewMessage(name: String): String =
        "Hola, nos gustaría conocer tu experiencia en $name. Comparte una reseña honesta: nos ayuda tanto lo que ha ido bien como aquello que podemos mejorar."
}
