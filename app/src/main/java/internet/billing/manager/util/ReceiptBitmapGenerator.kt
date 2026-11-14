package internet.billing.manager.util

import android.content.ClipData
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
        discount: Double = 0.0,
        dateMillis: Long = System.currentTimeMillis()
    ): Bitmap {
        val width = 1200
        val height = 750

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background (Pure Crisp White)
        canvas.drawColor(Color.WHITE)

        // 2. Company Info & Metadata
        val companyName = receiptSettings.companyName.ifBlank {
            ispSettings?.companyName?.takeIf { it.isNotBlank() && !it.equals("U-turn Technologies", ignoreCase = true) }
                ?: "Internet Service Provider"
        }
        val tagline = receiptSettings.companyTagline.ifBlank {
            ispSettings?.tagline?.ifBlank { "FAST • STABLE • CONNECTED" } ?: "FAST • STABLE • CONNECTED"
        }
        val hotline = ispSettings?.phone.orEmpty()
        val email = ispSettings?.email.orEmpty()
        val address = ispSettings?.address.orEmpty()
        val motto = receiptSettings.corporateMotto.ifBlank { "Stay Connected, Stay Ahead" }

        // Top Left: ISP Circular Logo
        val circlePaint = Paint().apply {
            color = Color.parseColor("#0284C7") // Sky Blue
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val circleX = 75f
        val circleY = 78f
        val circleR = 30f
        canvas.drawCircle(circleX, circleY, circleR, circlePaint)

        val logoAcronym = companyName.split(" ").filter { it.isNotBlank() }.take(3).map { it.first() }.joinToString("").uppercase().ifBlank { "ISP" }
        val ispTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = if (logoAcronym.length > 3) 14f else 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(logoAcronym.take(4), circleX, circleY + 6f, ispTextPaint)

        // Company Name & Tagline
        val companyPaint = Paint().apply {
            color = Color.parseColor("#0F172A") // Deep Slate
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(companyName, 118f, 72f, companyPaint)

        val taglinePaint = Paint().apply {
            color = Color.parseColor("#0284C7")
            textSize = 11.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(tagline, 118f, 96f, taglinePaint)

        // Top Center: MONEY RECEIPT
        val moneyPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 23f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val receiptWordPaint = Paint().apply {
            color = Color.parseColor("#0D9488") // Teal
            textSize = 23f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val thanksPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 12.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("MONEY", 600f, 58f, moneyPaint)
        canvas.drawText("RECEIPT", 600f, 85f, receiptWordPaint)
        canvas.drawText("Thank you for being with us!", 600f, 108f, thanksPaint)

        // Top Right: Mobile Banking Box (Optional based on settings)
        if (receiptSettings.showMobileBanking) {
            val mbBoxPaint = Paint().apply {
                color = Color.parseColor("#FFFFFF")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val mbBorderPaint = Paint().apply {
                color = Color.parseColor("#E11D48") // Rose 600
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            val mbRect = RectF(880f, 35f, 1160f, 125f)
            canvas.drawRoundRect(mbRect, 14f, 14f, mbBoxPaint)
            canvas.drawRoundRect(mbRect, 14f, 14f, mbBorderPaint)

            val mbTitlePaint = Paint().apply {
                color = Color.parseColor("#E11D48")
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val mbNumber = receiptSettings.bkashNumber.ifBlank {
                receiptSettings.nagadNumber.ifBlank { hotline }
            }
            val mbLabel = if (receiptSettings.bkashNumber.isNotBlank()) "bKash Mobile Banking"
                else if (receiptSettings.nagadNumber.isNotBlank()) "Nagad Mobile Banking"
                else "Mobile Banking"
            canvas.drawText(mbLabel, mbRect.centerX(), 66f, mbTitlePaint)

            val mbNumPaint = Paint().apply {
                color = Color.parseColor("#881337")
                textSize = 19f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(mbNumber.ifBlank { "01XXXXXXXXX" }, mbRect.centerX(), 102f, mbNumPaint)
        }

        // 3. Contact Info Capsule Bar (Dynamic based on settings & available data)
        val contactPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val contactItems = mutableListOf<String>()
        if (receiptSettings.showPhone && hotline.isNotBlank()) {
            contactItems.add("☎ Hotline: $hotline")
        }
        if (email.isNotBlank()) {
            contactItems.add("✉ Email: $email")
        }
        if (address.isNotBlank()) {
            contactItems.add("📍 Address: $address")
        }

        if (contactItems.isNotEmpty()) {
            val capsuleBgPaint = Paint().apply {
                color = Color.parseColor("#F1F5F9") // Slate 100
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val capsuleBorderPaint = Paint().apply {
                color = Color.parseColor("#E2E8F0") // Slate 200
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
                isAntiAlias = true
            }
            val capsuleRect = RectF(40f, 142f, 1160f, 186f)
            canvas.drawRoundRect(capsuleRect, 10f, 10f, capsuleBgPaint)
            canvas.drawRoundRect(capsuleRect, 10f, 10f, capsuleBorderPaint)

            when (contactItems.size) {
                1 -> {
                    canvas.drawText(contactItems[0], 65f, 169f, contactPaint)
                }
                2 -> {
                    canvas.drawText(contactItems[0], 65f, 169f, contactPaint)
                    canvas.drawText(contactItems[1], 620f, 169f, contactPaint)
                }
                else -> {
                    canvas.drawText(contactItems[0], 65f, 169f, contactPaint)
                    canvas.drawText(contactItems[1], 440f, 169f, contactPaint)
                    canvas.drawText(contactItems[2], 800f, 169f, contactPaint)
                }
            }
        }

        // 4. Voucher Body (Dashed Underlines)
        val dashPaint = Paint().apply {
            color = Color.parseColor("#94A3B8") // Slate 400
            strokeWidth = 1.6f
            pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val valPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val italicValPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            isAntiAlias = true
        }
        val tealValPaint = Paint().apply {
            color = Color.parseColor("#0D9488")
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val dueColor = if (totalDueRemaining > 0) Color.parseColor("#DC2626") else Color.parseColor("#0D9488")
        val dueValPaint = Paint().apply {
            color = dueColor
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val rightMargin = 1160f
        val leftMargin = 40f

        // Row 1: Received with thanks from
        val r1Y = 232f
        val r1Label = "Received with thanks from "
        canvas.drawText(r1Label, leftMargin, r1Y, labelPaint)
        val r1LWidth = labelPaint.measureText(r1Label)
        val customerNameStr = if (receiptSettings.showUsername && customer.pppoeUsername.isNotBlank() && customer.name.isNotBlank()) {
            "${customer.name} (${customer.pppoeUsername})"
        } else {
            customer.name.ifBlank { customer.pppoeUsername }
        }
        val details = mutableListOf<String>()
        if (receiptSettings.showPackage && customer.packageName.isNotBlank()) {
            details.add(customer.packageName)
        }
        if (receiptSettings.showAddress && customer.address.isNotBlank()) {
            details.add(customer.address)
        }
        if (receiptSettings.showPhone && customer.phone.isNotBlank()) {
            details.add(customer.phone)
        }
        val customerInfo = if (details.isNotEmpty()) {
            "$customerNameStr / ${details.joinToString(" / ")}"
        } else {
            customerNameStr
        }
        canvas.drawText(customerInfo, leftMargin + r1LWidth, r1Y, valPaint)
        canvas.drawLine(leftMargin + r1LWidth, r1Y + 6f, rightMargin, r1Y + 6f, dashPaint)

        // Row 2: Amount
        val r2Y = 278f
        val r2Label = "Amount "
        canvas.drawText(r2Label, leftMargin, r2Y, labelPaint)
        val r2LWidth = labelPaint.measureText(r2Label)
        val amountStr = "৳ ${"%,.2f".format(amount)}" + if (receiptSettings.showPaymentMethod) " ($method)" else ""
        canvas.drawText(amountStr, leftMargin + r2LWidth, r2Y, valPaint)
        canvas.drawLine(leftMargin + r2LWidth, r2Y + 6f, rightMargin, r2Y + 6f, dashPaint)

        // Row 3: In word
        val r3Y = 324f
        val r3Label = "In word "
        canvas.drawText(r3Label, leftMargin, r3Y, labelPaint)
        val r3LWidth = labelPaint.measureText(r3Label)
        val inWordStr = NumberToWords.convertToWords(amount.toLong())
        canvas.drawText(inWordStr, leftMargin + r3LWidth, r3Y, italicValPaint)
        canvas.drawLine(leftMargin + r3LWidth, r3Y + 6f, rightMargin, r3Y + 6f, dashPaint)

        // Row 4: For ... Branch ...
        val r4Y = 370f
        val r4Label = "For "
        canvas.drawText(r4Label, leftMargin, r4Y, labelPaint)
        val r4LWidth = labelPaint.measureText(r4Label)
        val monthStr = monthYear.ifBlank { SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(dateMillis)) }
        canvas.drawText(monthStr, leftMargin + r4LWidth, r4Y, valPaint)
        canvas.drawLine(leftMargin + r4LWidth, r4Y + 6f, 660f, r4Y + 6f, dashPaint)

        val branchX = 700f
        val branchLabel = "Branch "
        canvas.drawText(branchLabel, branchX, r4Y, labelPaint)
        val branchLWidth = labelPaint.measureText(branchLabel)
        val branchName = ispSettings?.address?.ifBlank { "Main Branch" } ?: "Main Branch"
        canvas.drawText(branchName, branchX + branchLWidth, r4Y, valPaint)
        canvas.drawLine(branchX + branchLWidth, r4Y + 6f, rightMargin, r4Y + 6f, dashPaint)

        // Row 5: ACCT. ... PAID ... DUE / METHOD
        val r5Y = 416f
        // ACCT.
        val acctLabel = "ACCT. "
        canvas.drawText(acctLabel, leftMargin, r5Y, labelPaint)
        val acctLWidth = labelPaint.measureText(acctLabel)
        val acctVal = customer.customerCode.ifBlank { "U-${customer.id}" }
        canvas.drawText(acctVal, leftMargin + acctLWidth, r5Y, valPaint)
        canvas.drawLine(leftMargin + acctLWidth, r5Y + 6f, 360f, r5Y + 6f, dashPaint)

        // PAID
        val paidX = 400f
        val paidLabel = "PAID "
        canvas.drawText(paidLabel, paidX, r5Y, labelPaint)
        val paidLWidth = labelPaint.measureText(paidLabel)
        val paidVal = "৳ ${"%,.2f".format(amount)}"
        canvas.drawText(paidVal, paidX + paidLWidth, r5Y, tealValPaint)
        canvas.drawLine(paidX + paidLWidth, r5Y + 6f, 720f, r5Y + 6f, dashPaint)

        // DUE (or METHOD if remaining due disabled)
        if (receiptSettings.showRemainingDue) {
            val dueX = 760f
            val dueLabel = "DUE "
            canvas.drawText(dueLabel, dueX, r5Y, labelPaint)
            val dueLWidth = labelPaint.measureText(dueLabel)
            val dueVal = "৳ ${"%,.2f".format(totalDueRemaining)}"
            canvas.drawText(dueVal, dueX + dueLWidth, r5Y, dueValPaint)
            canvas.drawLine(dueX + dueLWidth, r5Y + 6f, rightMargin, r5Y + 6f, dashPaint)
        } else if (receiptSettings.showPaymentMethod) {
            val methodX = 760f
            val methodLabel = "METHOD "
            canvas.drawText(methodLabel, methodX, r5Y, labelPaint)
            val methodLWidth = labelPaint.measureText(methodLabel)
            canvas.drawText(method, methodX + methodLWidth, r5Y, valPaint)
            canvas.drawLine(methodX + methodLWidth, r5Y + 6f, rightMargin, r5Y + 6f, dashPaint)
        }

        // 5. Lower Section: Amount Box & Signatures
        // Amount Box (Left)
        val amtOuterRect = RectF(40f, 512f, 380f, 582f)
        val amtOuterPaint = Paint().apply {
            color = Color.parseColor("#E0F2FE") // Sky 100
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val amtBorderPaint = Paint().apply {
            color = Color.parseColor("#38BDF8") // Sky 400
            style = Paint.Style.STROKE
            strokeWidth = 1.8f
            isAntiAlias = true
        }
        canvas.drawRoundRect(amtOuterRect, 14f, 14f, amtOuterPaint)
        canvas.drawRoundRect(amtOuterRect, 14f, 14f, amtBorderPaint)

        val amtLabelPaint = Paint().apply {
            color = Color.parseColor("#0369A1")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Amount =", 60f, 554f, amtLabelPaint)

        val amtInnerRect = RectF(175f, 522f, 365f, 572f)
        val amtInnerPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val amtInnerBorderPaint = Paint().apply {
            color = Color.parseColor("#BAE6FD")
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(amtInnerRect, 10f, 10f, amtInnerPaint)
        canvas.drawRoundRect(amtInnerRect, 10f, 10f, amtInnerBorderPaint)

        val amtNumPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 21f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("৳ ${"%,.2f".format(amount)}", amtInnerRect.centerX(), 555f, amtNumPaint)

        // Signatures (Right)
        val sigLineY = 555f
        val sigTextPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val sigNamePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        // Slot 1: Received by
        canvas.drawLine(620f, sigLineY, 820f, sigLineY, dashPaint)
        canvas.drawText("Received by", 720f, sigLineY + 22f, sigTextPaint)

        // Slot 2: Authorized Signature
        val authorityName = receiptSettings.signatureName.ifBlank { "Authority" }
        canvas.drawText(authorityName, 970f, sigLineY - 8f, sigNamePaint)
        canvas.drawLine(860f, sigLineY, 1080f, sigLineY, dashPaint)
        canvas.drawText("Authorized Signature", 970f, sigLineY + 22f, sigTextPaint)

        // 6. Bottom Corporate Ribbon / Banner
        val bannerHeight = 46f
        val bannerPaint = Paint().apply {
            color = Color.parseColor("#1E1B4B") // Deep Indigo / Navy
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(0f, height - bannerHeight, width.toFloat(), height.toFloat(), bannerPaint)

        val mottoPaint = Paint().apply {
            color = Color.WHITE
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("⚡ $motto", 40f, height - 17f, mottoPaint)

        val recNoStr = "Receipt #: PAY-${dateMillis % 1000000} • $monthStr"
        val recNoPaint = Paint().apply {
            color = Color.WHITE
            textSize = 13.5f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(recNoStr, width - 40f, height - 17f, recNoPaint)

        return bitmap
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        val cachePath = File(cacheDir, "receipts")
        if (!cachePath.exists()) {
            cachePath.mkdirs()
        }
        val file = File(cachePath, "Money_Receipt_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        file.setReadable(true, false)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun sendReceiptViaWhatsApp(
        context: Context,
        imageUri: Uri,
        phone: String,
        customerName: String,
        amount: Double
    ) {
        val caption = "প্রিয় $customerName, আপনার ৳${amount.toInt()} টাকা বিল পরিশোধের মানি রসিদ পাঠানো হলো। ধন্যবাদ।"

        // Check installed WhatsApp variants
        val pm = context.packageManager
        val isWhatsAppInstalled = try {
            pm.getPackageInfo("com.whatsapp", 0)
            true
        } catch (_: Exception) {
            false
        }
        val isWhatsAppBusinessInstalled = try {
            pm.getPackageInfo("com.whatsapp.w4b", 0)
            true
        } catch (_: Exception) {
            false
        }

        val targetPackage = when {
            isWhatsAppInstalled -> "com.whatsapp"
            isWhatsAppBusinessInstalled -> "com.whatsapp.w4b"
            else -> null
        }

        // Format phone for WhatsApp JID (e.g. 8801708732389@s.whatsapp.net)
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val targetPhone = when {
            cleanPhone.startsWith("880") -> cleanPhone
            cleanPhone.startsWith("0") -> "88$cleanPhone"
            cleanPhone.length == 10 -> "880$cleanPhone"
            else -> cleanPhone
        }

        // Grant explicit URI read permission to WhatsApp and WhatsApp Business
        listOf("com.whatsapp", "com.whatsapp.w4b").forEach { pkg ->
            try {
                context.grantUriPermission(pkg, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            clipData = ClipData.newRawUri("Money Receipt", imageUri)
            putExtra(Intent.EXTRA_TEXT, caption)
            if (targetPhone.isNotBlank()) {
                putExtra("jid", "$targetPhone@s.whatsapp.net")
            }
            if (targetPackage != null) {
                setPackage(targetPackage)
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                // Retry without jid
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    clipData = ClipData.newRawUri("Money Receipt", imageUri)
                    putExtra(Intent.EXTRA_TEXT, caption)
                    if (targetPackage != null) {
                        setPackage(targetPackage)
                    }
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                try {
                    val chooserIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        clipData = ClipData.newRawUri("Money Receipt", imageUri)
                        putExtra(Intent.EXTRA_TEXT, caption)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(chooserIntent, "Share Receipt").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } catch (ex2: Exception) {
                    Toast.makeText(context, "WhatsApp এ শেয়ার করা সম্ভব হয়নি: ${ex2.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun shareReceipt(
        context: Context,
        imageUri: Uri,
        customerName: String,
        amount: Double
    ) {
        val caption = "প্রিয় $customerName, আপনার ৳${amount.toInt()} টাকা বিল পরিশোধের মানি রসিদ পাঠানো হলো। ধন্যবাদ।"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            clipData = ClipData.newRawUri("Money Receipt", imageUri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserIntent = Intent.createChooser(intent, "মানি রসিদ শেয়ার করুন").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "শেয়ার করা সম্ভব হয়নি: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openDirectWhatsAppChat(
        context: Context,
        phone: String,
        message: String = ""
    ) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val targetPhone = when {
            cleanPhone.startsWith("880") -> cleanPhone
            cleanPhone.startsWith("0") -> "88$cleanPhone"
            cleanPhone.length == 10 -> "880$cleanPhone"
            else -> cleanPhone
        }
        if (targetPhone.isBlank()) {
            Toast.makeText(context, "সঠিক মোবাইল নম্বর পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://api.whatsapp.com/send?phone=$targetPhone&text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                if (context.packageManager.getPackageInfo("com.whatsapp", 0) != null) {
                    setPackage("com.whatsapp")
                }
            } catch (_: Exception) {}
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "WhatsApp ওপেন করা যাচ্ছে না: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
