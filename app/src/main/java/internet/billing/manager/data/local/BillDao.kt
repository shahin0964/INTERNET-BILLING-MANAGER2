package internet.billing.manager.data.local

import androidx.room.*
import internet.billing.manager.data.model.BillEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bill_entries ORDER BY billDateMillis DESC")
    fun getAllBills(): Flow<List<BillEntry>>

    @Query("SELECT * FROM bill_entries WHERE monthYear = :monthYear ORDER BY billDateMillis DESC")
    fun getBillsForMonth(monthYear: String): Flow<List<BillEntry>>

    @Query("SELECT * FROM bill_entries WHERE billDateMillis >= :startMillis AND billDateMillis <= :endMillis ORDER BY billDateMillis DESC")
    fun getBillsBetween(startMillis: Long, endMillis: Long): Flow<List<BillEntry>>

    @Query("SELECT SUM(billAmount) FROM bill_entries WHERE monthYear = :monthYear")
    fun getTotalBilledForMonth(monthYear: String): Flow<Double?>

    @Query("SELECT SUM(dueAmount) FROM bill_entries WHERE monthYear = :monthYear")
    fun getTotalDueForMonth(monthYear: String): Flow<Double?>

    @Query("SELECT customerId FROM bill_entries WHERE monthYear = :monthYear")
    suspend fun getBilledCustomerIdsForMonth(monthYear: String): List<Long>

    @Query("SELECT * FROM bill_entries WHERE customerId = :customerId AND isPaid = 0 ORDER BY billDateMillis DESC LIMIT 1")
    suspend fun getLatestUnpaidBillForCustomer(customerId: Long): BillEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBills(bills: List<BillEntry>)

    @Update
    suspend fun updateBill(bill: BillEntry)

    @Delete
    suspend fun deleteBill(bill: BillEntry)

    @Query("UPDATE bill_entries SET isPaid = 1, paidAmount = billAmount, dueAmount = 0 WHERE id = :billId")
    suspend fun markBillAsPaid(billId: Long)
}
