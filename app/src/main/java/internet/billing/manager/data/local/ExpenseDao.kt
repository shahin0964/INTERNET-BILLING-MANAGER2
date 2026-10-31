package internet.billing.manager.data.local

import androidx.room.*
import internet.billing.manager.data.model.Expense
import kotlinx.coroutines.flow.Flow

data class CategorySum(
    val category: String,
    val total: Double
)

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE monthYear = :monthYear ORDER BY dateMillis DESC")
    fun getExpensesForMonth(monthYear: String): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE monthYear = :monthYear")
    fun getTotalOutflowForMonth(monthYear: String): Flow<Double?>

    @Query("SELECT MAX(amount) FROM expenses WHERE monthYear = :monthYear")
    fun getHighestExpenseForMonth(monthYear: String): Flow<Double?>

    @Query("SELECT AVG(amount) FROM expenses WHERE monthYear = :monthYear")
    fun getAverageExpenseForMonth(monthYear: String): Flow<Double?>

    @Query("SELECT COUNT(*) FROM expenses WHERE monthYear = :monthYear")
    fun getVouchersCountForMonth(monthYear: String): Flow<Int>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE monthYear = :monthYear GROUP BY category")
    fun getCategorySumsForMonth(monthYear: String): Flow<List<CategorySum>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}
