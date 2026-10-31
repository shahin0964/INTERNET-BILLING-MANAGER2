package internet.billing.manager.data.local

import androidx.room.*
import internet.billing.manager.data.model.PaymentTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY dateMillis DESC")
    fun getAllPayments(): Flow<List<PaymentTransaction>>

    @Query("SELECT * FROM payments ORDER BY dateMillis DESC LIMIT :limit")
    fun getRecentPayments(limit: Int): Flow<List<PaymentTransaction>>

    @Query("SELECT * FROM payments WHERE monthYear = :monthYear ORDER BY dateMillis DESC")
    fun getPaymentsForMonth(monthYear: String): Flow<List<PaymentTransaction>>

    @Query("SELECT SUM(amount) FROM payments WHERE monthYear = :monthYear")
    fun getTotalCollectedForMonth(monthYear: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM payments WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis")
    fun getTotalCollectedBetween(startMillis: Long, endMillis: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentTransaction): Long

    @Delete
    suspend fun deletePayment(payment: PaymentTransaction)
}
