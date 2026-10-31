package internet.billing.manager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "isp_settings")
data class IspSettings(
    @PrimaryKey
    val id: Int = 1,
    val companyName: String = "U-turn Technologies",
    val tagline: String = "Connect People Better Tomorrow",
    val phone: String = "01700000000",
    val address: String = "Dhaka, Bangladesh",
    val currencySymbol: String = "৳",
    val smsTemplate: String = "Dear {NAME}, your ISP bill for {MONTH} is {DUE} ৳. Please pay on time. Thank you, {COMPANY}.",
    val bandwidthBill: Double = 0.0,
    val discount: Double = 0.0
)
