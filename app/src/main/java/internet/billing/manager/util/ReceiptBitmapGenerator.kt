package internet.billing.manager.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import internet.billing.manager.data.model.Customer
import internet.billing.manager.data.model.IspSettings
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReceiptBitmapGenerator {

    fun generateReceiptBitmap(
        context: Context,
        ispSettings: IspSettings?,
        receiptSettings: ReceiptCustomizationSettings,
        customer: Customer,
        amount: Double,
        method: String,
        monthYear: String,
        totalDueRemaining: Double,
        dateMillis: Long = System.currentTimeMillis()
    ): Bitmap {
        val width = 900
        val height = 1250

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Outer Border
        val borderPaint = Paint().apply {
            color = Color.parseColor("#0284C7") // Cyan/Blue border
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRect(12f, 12f, width - 12f, height - 12f, borderPaint)

        // Inner Border
        val innerBorderPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRect(20f, 20f, width - 20f, height - 20f, innerBorderPaint)

        // 2. Header Section
        val companyName = ispSettings?.companyName?.ifBlank { "INTERNET SERVICE PROVIDER" } ?: "INTERNET SERVICE PROVIDER"
        val hotline = ispSettings?.phone ?: ""
        val email = ispSettings?.email ?: ""
        val address = ispSettings?.address ?: ""

        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F172A") // Slate 900
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(companyName, 40f, 65f, titlePaint)

        val subHeaderPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 15f
            isAntiAlias = true
        }

        var headerY = 90f
        if (hotline.isNotBlank()) {
            canvas.drawText("Hotline: $hotline", 40f, headerY, subHeaderPaint)
            headerY += 22f
        }
        if (email.isNotBlank()) {
            canvas.drawText("Email: $email", 40f, headerY, subHeaderPaint)
            headerY += 22f
        }
        if (address.isNotBlank()) {
            canvas.drawText("Address: $address", 40f, headerY, subHeaderPaint)
            headerY += 22f
        }

        // 3. Mobile Banking Box (Top Right)
        if (receiptSettings.showMobileBanking && (receiptSettings.bkashNumber.isNotBlank() || receiptSettings.nagadNumber.isNotBlank())) {
            val mbBoxPaint = Paint().apply {
                color = Color.parseColor("#FFF1F2") // Light Rose
                style = Paint.Style.FILL
            }
            val mbBorderPaint = Paint().apply {
                color = Color.parseColor("#E11D48") // Rose 600
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
            }
            val mbRect = RectF(width - 320f, 35f, width - 40f, 130f)
            canvas.drawRoundRect(mbRect, 12f, 12f, mbBoxPaint)
            canvas.drawRoundRect(mbRect, 12f, 12f, mbBorderPaint)

            val mbTitlePaint = Paint().apply {
                color = Color.parseColor("#BE123C")
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("Mobile Banking (বিকাশ / নগদ)", mbRect.centerX(), 62f, mbTitlePaint)

            val mbNumPaint = Paint().apply {
                color = Color.parseColor("#881337")
                textSize = 17f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val numberText = receiptSettings.bkashNumber.ifBlank { receiptSettings.nagadNumber }
            canvas.drawText(numberText, mbRect.centerX(), 94f, mbNumPaint)

            if (receiptSettings.nagadNumber.isNotBlank() && receiptSettings.bkashNumber.isNotBlank()) {
                val secNumPaint = Paint().apply {
                    color = Color.parseColor("#C2410C")
                    textSize = 13f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.drawText("Nagad: ${receiptSettings.nagadNumber}", mbRect.centerX(), 116f, secNumPaint)
            }
        }

        // 4. Center Title Pill: MONEY RECEIPT
        val pillY = 170f
        val pillPaint = Paint().apply {
            color = Color.parseColor("#F0F9FF") // Sky 50
            style = Paint.Style.FILL
        }
        val pillBorderPaint = Paint().apply {
            color = Color.parseColor("#0284C7")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val pillRect = RectF(width / 2f - 220f, pillY, width / 2f + 220f, pillY + 44f)
        canvas.drawRoundRect(pillRect, 22f, 22f, pillPaint)
        canvas.drawRoundRect(pillRect, 22f, 22f, pillBorderPaint)

        val pillTextPaint = Paint().apply {
            color = Color.parseColor("#0369A1")
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val titleText = receiptSettings.receiptTitle.ifBlank { "MONEY RECEIPT (মানি রসিদ)" }
        canvas.drawText(titleText, width / 2f, pillY + 29f, pillTextPaint)

        // 5. Receipt Metadata (Date, Receipt No)
        val dateStr = SimpleDateFormat("dd MMMM, yyyy  hh:mm a", Locale.getDefault()).format(Date(dateMillis))
        val metaPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 14f
            isAntiAlias = true
        }
        canvas.drawText("Receipt Date: $dateStr", 40f, 248f, metaPaint)

        val receiptNo = "REC-${dateMillis % 1000000}"
        val recNoPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("Voucher No: $receiptNo", width - 40f, 248f, recNoPaint)

        // Divider
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1.5f
            pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
        }
        canvas.drawLine(40f, 265f, width - 40f, 265f, dividerPaint)

        // 6. Customer Details Section
        var curY = 305f
        val labelPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 16f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // Received with thanks from
        canvas.drawText("Received with thanks from:", 40f, curY, labelPaint)
        val customerDisplay = customer.name.ifBlank { customer.pppoeUsername }
        canvas.drawText(customerDisplay, 275f, curY, valuePaint)

        curY += 36f

        // Amount in Taka
        canvas.drawText("Amount (টাকার পরিমাণ):", 40f, curY, labelPaint)
        val amountPaint = Paint().apply {
            color = Color.parseColor("#0369A1")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("৳ ${"%,.2f".format(amount)}", 275f, curY, amountPaint)

        curY += 36f

        // In word
        canvas.drawText("In Word (কথায়):", 40f, curY, labelPaint)
        val wordsText = NumberToWords.convertToWords(amount.toLong())
        val wordsPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawText(wordsText, 275f, curY, wordsPaint)

        curY += 36f

        // For Month & Branch
        canvas.drawText("For Billing Month:", 40f, curY, labelPaint)
        canvas.drawText(monthYear, 275f, curY, valuePaint)

        val branchText = "Branch: Main Office"
        canvas.drawText(branchText, width - 260f, curY, labelPaint)

        curY += 30f

        // 7. Grid of Details (Package, Method, Phone, Username, Remaining Due)
        val gridBgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val gridBorderPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val gridRect = RectF(40f, curY, width - 40f, curY + 200f)
        canvas.drawRoundRect(gridRect, 10f, 10f, gridBgPaint)
        canvas.drawRoundRect(gridRect, 10f, 10f, gridBorderPaint)

        var gRowY = curY + 36f
        val gCol1 = 60f
        val gCol2 = 470f

        // Item 1: PPPoE Username
        if (receiptSettings.showUsername) {
            canvas.drawText("PPPoE Username:", gCol1, gRowY, labelPaint)
            canvas.drawText(customer.pppoeUsername, gCol1 + 160f, gRowY, valuePaint)
        }

        // Item 2: Customer Code
        canvas.drawText("Customer ID:", gCol2, gRowY, labelPaint)
        canvas.drawText(customer.customerCode.ifBlank { "U-${customer.id}" }, gCol2 + 130f, gRowY, valuePaint)

        gRowY += 38f

        // Item 3: Package Name
        if (receiptSettings.showPackage) {
            canvas.drawText("Internet Package:", gCol1, gRowY, labelPaint)
            val pkg = customer.packageName.ifBlank { "${customer.speedMbps} Mbps Plan" }
            canvas.drawText(pkg, gCol1 + 160f, gRowY, valuePaint)
        }

        // Item 4: Payment Method
        if (receiptSettings.showPaymentMethod) {
            canvas.drawText("Payment Method:", gCol2, gRowY, labelPaint)
            canvas.drawText(method, gCol2 + 150f, gRowY, valuePaint)
        }

        gRowY += 38f

        // Item 5: Phone Number
        if (receiptSettings.showPhone && customer.phone.isNotBlank()) {
            canvas.drawText("Contact Phone:", gCol1, gRowY, labelPaint)
            canvas.drawText(customer.phone, gCol1 + 160f, gRowY, valuePaint)
        }

        // Item 6: Remaining Due
        if (receiptSettings.showRemainingDue) {
            canvas.drawText("Remaining Due:", gCol2, gRowY, labelPaint)
            val dueColor = if (totalDueRemaining > 0) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")
            val duePaint = Paint().apply {
                color = dueColor
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("৳ ${"%,.2f".format(totalDueRemaining)}", gCol2 + 150f, gRowY, duePaint)
        }

        gRowY += 38f

        // Item 7: Address (if enabled and present)
        if (receiptSettings.showAddress && customer.address.isNotBlank()) {
            canvas.drawText("Customer Address: ${customer.address}", gCol1, gRowY, labelPaint)
        }

        curY += 225f

        // 8. Thank you note & Special Notes
        if (receiptSettings.footerNote.isNotBlank()) {
            val thankPaint = Paint().apply {
                color = Color.parseColor("#047857") // Emerald 700
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(receiptSettings.footerNote, width / 2f, curY, thankPaint)
            curY += 26f
        }

        if (receiptSettings.specialNotes.isNotBlank()) {
            val termsPaint = Paint().apply {
                color = Color.parseColor("#64748B")
                textSize = 13.5f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(receiptSettings.specialNotes, width / 2f, curY, termsPaint)
            curY += 36f
        }

        // 9. Signature Row
        val sigY = height - 120f
        val sigLinePaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            strokeWidth = 1.8f
        }
        // Left: Received by
        canvas.drawLine(70f, sigY, 320f, sigY, sigLinePaint)
        val sigTextPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 14f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Received by", 195f, sigY + 24f, sigTextPaint)

        // Right: Authorized Signature
        canvas.drawLine(width - 340f, sigY, width - 70f, sigY, sigLinePaint)
        canvas.drawText("Authorized Signature", width - 205f, sigY + 24f, sigTextPaint)

        val authNamePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val sigName = receiptSettings.signatureName.ifBlank { "Authority" }
        canvas.drawText(sigName, width - 205f, sigY + 44f, authNamePaint)

        // 10. Bottom Corporate Ribbon / Banner
        val bannerHeight = 44f
        val bannerPaint = Paint().apply {
            color = Color.parseColor("#0F172A") // Navy Slate
            style = Paint.Style.FILL
        }
        canvas.drawRect(20f, height - 20f - bannerHeight, width - 20f, height - 20f, bannerPaint)

        val mottoPaint = Paint().apply {
            color = Color.WHITE
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val motto = receiptSettings.corporateMotto.ifBlank { "Stay Connected, Stay Ahead" }
        canvas.drawText(motto, width / 2f, height - 38f, mottoPaint)

        return bitmap
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
        val cachePath = File(context.cacheDir, "receipts")
        cachePath.mkdirs()
        val file = File(cachePath, "Money_Receipt_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun sendReceiptViaWhatsApp(
        context: Context,
        imageUri: Uri,
        phone: String,
        customerName: String,
        amount: Double
    ) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val targetPhone = when {
            cleanPhone.startsWith("880") -> cleanPhone
            cleanPhone.startsWith("0") -> "88$cleanPhone"
            cleanPhone.length == 10 -> "880$cleanPhone"
            else -> cleanPhone
        }

        val caption = "প্রিয় $customerName, আপনার ৳${amount.toInt()} টাকা বিল পরিশোধের মানি রসিদ পাঠানো হলো। ধন্যবাদ।"

        // Try direct intent targeting WhatsApp
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, caption)
                if (targetPhone.isNotBlank()) {
                    putExtra("jid", "$targetPhone@s.whatsapp.net")
                }
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Try WhatsApp Business fallback
            try {
                val wbIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    putExtra(Intent.EXTRA_TEXT, caption)
                    if (targetPhone.isNotBlank()) {
                        putExtra("jid", "$targetPhone@s.whatsapp.net")
                    }
                    setPackage("com.whatsapp.w4b")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(wbIntent)
            } catch (e2: Exception) {
                // Fallback to generic share chooser
                val chooserIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    putExtra(Intent.EXTRA_TEXT, caption)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(chooserIntent, "Share Receipt via WhatsApp"))
            }
        }
    }
}
