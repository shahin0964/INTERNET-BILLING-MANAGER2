package internet.billing.manager.util

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.room.withTransaction
import internet.billing.manager.InternetBillingApp
import internet.billing.manager.data.model.BillEntry
import internet.billing.manager.data.model.Customer
import internet.billing.manager.data.model.PaymentTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object ServerSyncManager {
    private const val PREFS_NAME = "ibm_server_sync_prefs"
    private const val KEY_SERVER_URL = "server_api_url"
    private const val KEY_AUTH_TOKEN = "server_auth_token"
    private const val KEY_SYNC_ENABLED = "server_sync_enabled"
    private const val KEY_LAST_SYNC_TIME = "server_last_sync_time"

    var serverUrl by mutableStateOf("http://10.0.2.2:5000/api")
        private set

    var isSyncEnabled by mutableStateOf(false)
        private set

    var lastSyncTimestamp by mutableStateOf(0L)
        private set

    var isSyncing by mutableStateOf(false)
        private set

    var lastSyncStatus by mutableStateOf("")
        private set

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        serverUrl = prefs.getString(KEY_SERVER_URL, "http://10.0.2.2:5000/api") ?: "http://10.0.2.2:5000/api"
        isSyncEnabled = prefs.getBoolean(KEY_SYNC_ENABLED, false)
        lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC_TIME, 0L)
    }

    fun updateConfig(context: Context, url: String, enabled: Boolean) {
        serverUrl = url.trim().removeSuffix("/")
        isSyncEnabled = enabled
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_SERVER_URL, serverUrl)
            .putBoolean(KEY_SYNC_ENABLED, enabled)
            .apply()
    }

    /**
     * Tests connectivity to the central REST API server.
     */
    suspend fun testConnection(testUrl: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val cleanUrl = testUrl.trim().removeSuffix("/")
            val url = URL(if (cleanUrl.endsWith("/api")) cleanUrl.removeSuffix("/api") + "/health" else "$cleanUrl/health")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
            }
            val code = conn.responseCode
            conn.disconnect()
            if (code in 200..299) {
                Pair(true, "সার্ভারের সাথে সফলভাবে সংযোগ স্থাপিত হয়েছে! (HTTP $code)")
            } else {
                Pair(false, "সার্ভার রেসপন্স কোড: $code")
            }
        } catch (e: Exception) {
            Pair(false, "সংযোগ ব্যর্থ: ${e.localizedMessage}")
        }
    }

    /**
     * Performs a bidirectional sync between local Room DB and the Central PostgreSQL Server.
     */
    suspend fun performSync(context: Context): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!isSyncEnabled) {
            return@withContext Pair(false, "সার্ভার সিঙ্ক নিষ্ক্রিয় রয়েছে")
        }

        isSyncing = true
        lastSyncStatus = "সার্ভার থেকে ডেটা সিঙ্ক করা হচ্ছে..."

        return@withContext try {
            val app = context.applicationContext as InternetBillingApp
            val db = app.database
            val base = serverUrl.removeSuffix("/")

            // 1. PULL changes from Server
            val pullUrl = URL("$base/sync/pull?since=$lastSyncTimestamp")
            val pullConn = (pullUrl.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 15000
                requestMethod = "GET"
            }

            if (pullConn.responseCode !in 200..299) {
                pullConn.disconnect()
                isSyncing = false
                lastSyncStatus = "সার্ভার রেসপন্স এরর (HTTP ${pullConn.responseCode})"
                return@withContext Pair(false, lastSyncStatus)
            }

            val pullResponseStr = pullConn.inputStream.bufferedReader().use { it.readText() }
            pullConn.disconnect()

            val pullJson = JSONObject(pullResponseStr)
            val serverTs = pullJson.optLong("serverTimestamp", System.currentTimeMillis())
            val dataObj = pullJson.optJSONObject("data")

            var pulledCount = 0
            if (dataObj != null) {
                db.withTransaction {
                    // Merge customers
                    val custArr = dataObj.optJSONArray("customers") ?: JSONArray()
                    for (i in 0 until custArr.length()) {
                        val c = custArr.getJSONObject(i)
                        val cust = Customer(
                            id = c.optLong("id", 0L),
                            customerCode = c.optString("customer_code", ""),
                            name = c.optString("name", ""),
                            pppoeUsername = c.optString("pppoe_username", ""),
                            phone = c.optString("phone", ""),
                            monthlyBill = c.optDouble("monthly_bill", 0.0),
                            dueAmount = c.optDouble("due_amount", 0.0),
                            billDueDay = c.optInt("bill_due_day", 10),
                            status = c.optString("status", "Active"),
                            connectionType = c.optString("connection_type", "Fiber"),
                            address = c.optString("address", ""),
                            staticIp = c.optString("static_ip", ""),
                            notes = c.optString("notes", "")
                        )
                        db.customerDao().insertCustomer(cust)
                        pulledCount++
                    }

                    // Merge bills
                    val billsArr = dataObj.optJSONArray("bills") ?: JSONArray()
                    for (i in 0 until billsArr.length()) {
                        val b = billsArr.getJSONObject(i)
                        val bill = BillEntry(
                            id = b.optLong("id", 0L),
                            customerId = b.optLong("customer_id", 0L),
                            customerName = b.optString("customer_name", ""),
                            customerCode = b.optString("customer_code", ""),
                            monthYear = b.optString("month_year", ""),
                            billAmount = b.optDouble("bill_amount", 0.0),
                            paidAmount = b.optDouble("paid_amount", 0.0),
                            dueAmount = b.optDouble("due_amount", 0.0),
                            isPaid = b.optBoolean("is_paid", false),
                            billDateMillis = b.optLong("bill_date_millis", System.currentTimeMillis()),
                            notes = b.optString("notes", "")
                        )
                        db.billDao().insertBill(bill)
                    }

                    // Merge payments
                    val payArr = dataObj.optJSONArray("payments") ?: JSONArray()
                    for (i in 0 until payArr.length()) {
                        val p = payArr.getJSONObject(i)
                        val pay = PaymentTransaction(
                            id = p.optLong("id", 0L),
                            customerId = p.optLong("customer_id", 0L),
                            customerName = p.optString("customer_name", ""),
                            customerCode = p.optString("customer_code", ""),
                            amount = p.optDouble("amount", 0.0),
                            discount = p.optDouble("discount", 0.0),
                            paymentMethod = p.optString("payment_method", "Cash"),
                            dateMillis = p.optLong("date_millis", System.currentTimeMillis()),
                            monthYear = p.optString("month_year", ""),
                            note = p.optString("note", "")
                        )
                        db.paymentDao().insertPayment(pay)
                    }
                }
            }

            // 2. Save Last Sync Timestamp
            lastSyncTimestamp = serverTs
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putLong(KEY_LAST_SYNC_TIME, lastSyncTimestamp).apply()

            isSyncing = false
            lastSyncStatus = "সিঙ্ক সম্পন্ন হয়েছে ($pulledCount টি রেকর্ড আপডেট)"
            Pair(true, lastSyncStatus)
        } catch (e: Exception) {
            isSyncing = false
            lastSyncStatus = "সিঙ্ক ত্রুটি: ${e.localizedMessage}"
            Pair(false, lastSyncStatus)
        }
    }
}
