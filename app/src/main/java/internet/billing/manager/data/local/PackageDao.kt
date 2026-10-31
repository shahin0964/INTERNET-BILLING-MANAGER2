package internet.billing.manager.data.local

import androidx.room.*
import internet.billing.manager.data.model.InternetPackage
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageDao {
    @Query("SELECT * FROM packages ORDER BY speedMbps ASC")
    fun getAllPackages(): Flow<List<InternetPackage>>

    @Query("SELECT * FROM packages WHERE id = :id LIMIT 1")
    suspend fun getPackageById(id: Long): InternetPackage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: InternetPackage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(packages: List<InternetPackage>)

    @Update
    suspend fun updatePackage(pkg: InternetPackage)

    @Delete
    suspend fun deletePackage(pkg: InternetPackage)

    @Query("SELECT COUNT(*) FROM packages")
    suspend fun getPackageCount(): Int
}
