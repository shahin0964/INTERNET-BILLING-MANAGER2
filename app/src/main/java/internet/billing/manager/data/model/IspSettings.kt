package internet.billing.manager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "isp_settings")
data class IspSettings(
    @PrimaryKey
    val id: Int = 1,
    val companyName: String = "",
    val tagline: String = "",
    val phone: String = "",
    val address: String = "",
    val currencySymbol: String = "৳",
    val smsTemplate: String = "প্রিয় {NAME}, আপনার {MONTH} মাসের ইন্টারনেট বিল ৳{DUE} টাকা। সময়মতো পরিশোধের অনুরোধ রইল। ধন্যবাদ - {COMPANY}.",
    val bandwidthBill: Double = 0.0,
    val discount: Double = 0.0,
    val email: String = "",
    val networkStatus: String = "Operational", // Operational, Maintenance, Degraded
    val logoUri: String = ""
)
