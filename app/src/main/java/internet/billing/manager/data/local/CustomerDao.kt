package internet.billing.manager.data.local

import androidx.room.*
import internet.billing.manager.data.model.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY id DESC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE status = :status ORDER BY id DESC")
    fun getCustomersByStatus(status: String): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR pppoeUsername LIKE '%' || :query || '%' OR customerCode LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchCustomers(query: String): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("SELECT COUNT(*) FROM customers")
    fun getTotalCustomerCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM customers WHERE status = 'Active'")
    fun getActiveCustomerCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM customers WHERE status = 'Suspended'")
    fun getSuspendedCustomerCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM customers WHERE status = 'Inactive'")
    fun getInactiveCustomerCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("UPDATE customers SET dueAmount = :newDue WHERE id = :customerId")
    suspend fun updateDue(customerId: Long, newDue: Double)

    @Query("SELECT SUM(monthlyBill) FROM customers WHERE status = 'Active'")
    fun getTotalActiveMonthlyBill(): Flow<Double?>

    @Query("SELECT SUM(dueAmount) FROM customers")
    fun getTotalOutstandingDue(): Flow<Double?>

    @Query("SELECT * FROM customers WHERE status = :status ORDER BY id DESC")
    suspend fun getCustomersByStatusList(status: String): List<Customer>

    @Query("SELECT * FROM customers ORDER BY id DESC")
    suspend fun getAllCustomersList(): List<Customer>

    @Query("UPDATE customers SET status = :status WHERE id = :customerId")
    suspend fun updateStatus(customerId: Long, status: String)
}
