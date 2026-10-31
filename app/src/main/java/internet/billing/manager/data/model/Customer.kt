package internet.billing.manager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerCode: String = "",
    val name: String = "",
    val pppoeUsername: String = "",
    val phone: String = "",
    val packageId: Long = 0,
    val packageName: String = "",
    val speedMbps: Int = 0,
    val monthlyBill: Double = 0.0,
    val dueAmount: Double = 0.0,
    val billDueDay: Int = 10,
    val status: String = STATUS_ACTIVE, // "Active", "Suspended", "Inactive"
    val connectionType: String = "Fiber",
    val address: String = "",
    val staticIp: String = "",
    val notes: String = "",
    val createdDateMillis: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_ACTIVE = "Active"
        const val STATUS_SUSPENDED = "Suspended"
        const val STATUS_INACTIVE = "Inactive"
    }
}

data class PreviousDueItem(
    val month: String,
    val year: String,
    val amount: Double
)
