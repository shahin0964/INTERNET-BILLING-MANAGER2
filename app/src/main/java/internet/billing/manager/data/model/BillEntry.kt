package internet.billing.manager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_entries")
data class BillEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val customerCode: String,
    val monthYear: String, // e.g. "2026-10"
    val billAmount: Double,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val isPaid: Boolean = false,
    val billDateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)
