package internet.billing.manager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // Office Rent, Maintenance, Equipment, Internet, Other
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val monthYear: String = "", // e.g. "2026-10"
    val note: String = ""
) {
    companion object {
        const val CAT_OFFICE_RENT = "Office Rent"
        const val CAT_MAINTENANCE = "Maintenance"
        const val CAT_EQUIPMENT = "Equipment"
        const val CAT_INTERNET = "Internet"
        const val CAT_OTHER = "Other"

        val ALL_CATEGORIES = listOf(
            CAT_OFFICE_RENT,
            CAT_MAINTENANCE,
            CAT_EQUIPMENT,
            CAT_INTERNET,
            CAT_OTHER
        )
    }
}
