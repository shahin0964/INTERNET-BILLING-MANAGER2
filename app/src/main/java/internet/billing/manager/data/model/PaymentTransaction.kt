package internet.billing.manager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val customerCode: String,
    val amount: Double,
    val paymentMethod: String = "Cash", // Cash, bKash, Nagad, Bank
    val dateMillis: Long = System.currentTimeMillis(),
    val monthYear: String = "", // e.g. "2026-10"
    val note: String = ""
)
