package internet.billing.manager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "packages")
data class InternetPackage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val speedMbps: Int,
    val monthlyPrice: Double,
    val description: String = "High-speed optical fiber connection",
    val colorHex: String = "#2979FF" // Default blue
)
