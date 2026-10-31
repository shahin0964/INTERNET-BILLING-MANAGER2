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
    val packageName: String = "10 Mbps",
    val speedMbps: Int = 10,
    val monthlyBill: Double = 600.0,
    val dueAmount: Double = 0.0,
    val status: String = STATUS_ACTIVE, // "Active", "Suspended", "Inactive"
    val connectionType: String = "Fiber",
    val address: String = "",
    val createdDateMillis: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_ACTIVE = "Active"
        const val STATUS_SUSPENDED = "Suspended"
        const val STATUS_INACTIVE = "Inactive"
    }
}
