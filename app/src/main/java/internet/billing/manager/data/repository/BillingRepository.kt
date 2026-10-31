package internet.billing.manager.data.repository

import internet.billing.manager.data.local.*
import internet.billing.manager.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class BillingRepository(
    private val customerDao: CustomerDao,
    private val packageDao: PackageDao,
    private val paymentDao: PaymentDao,
    private val billDao: BillDao,
    private val expenseDao: ExpenseDao,
    private val settingsDao: SettingsDao
) {
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val totalCustomerCount: Flow<Int> = customerDao.getTotalCustomerCount()
    val activeCustomerCount: Flow<Int> = customerDao.getActiveCustomerCount()
    val suspendedCustomerCount: Flow<Int> = customerDao.getSuspendedCustomerCount()
    val inactiveCustomerCount: Flow<Int> = customerDao.getInactiveCustomerCount()
    val totalActiveMonthlyBill: Flow<Double?> = customerDao.getTotalActiveMonthlyBill()
    val totalOutstandingDue: Flow<Double?> = customerDao.getTotalOutstandingDue()

    val allPackages: Flow<List<InternetPackage>> = packageDao.getAllPackages()
    val recentPayments: Flow<List<PaymentTransaction>> = paymentDao.getRecentPayments(10)
    val ispSettings: Flow<IspSettings?> = settingsDao.getSettings()

    fun getCustomersByStatus(status: String): Flow<List<Customer>> =
        customerDao.getCustomersByStatus(status)

    fun searchCustomers(query: String): Flow<List<Customer>> =
        customerDao.searchCustomers(query)

    suspend fun insertCustomer(customer: Customer): Long =
        customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: Customer) =
        customerDao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: Customer) =
        customerDao.deleteCustomer(customer)

    suspend fun getCustomerById(id: Long): Customer? =
        customerDao.getCustomerById(id)

    // Payments
    fun getPaymentsForMonth(monthYear: String): Flow<List<PaymentTransaction>> =
        paymentDao.getPaymentsForMonth(monthYear)

    fun getTotalCollectedForMonth(monthYear: String): Flow<Double?> =
        paymentDao.getTotalCollectedForMonth(monthYear)

    fun getTotalCollectedToday(): Flow<Double?> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis
        return paymentDao.getTotalCollectedBetween(startOfDay, endOfDay)
    }

    suspend fun recordPayment(
        customerId: Long,
        amount: Double,
        method: String,
        note: String
    ) {
        val customer = customerDao.getCustomerById(customerId) ?: return
        val currentMonthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        
        val transaction = PaymentTransaction(
            customerId = customerId,
            customerName = customer.name.ifBlank { customer.pppoeUsername },
            customerCode = customer.customerCode,
            amount = amount,
            paymentMethod = method,
            dateMillis = System.currentTimeMillis(),
            monthYear = currentMonthYear,
            note = note
        )
        paymentDao.insertPayment(transaction)

        // Deduct from customer due amount
        val newDue = (customer.dueAmount - amount).coerceAtLeast(0.0)
        customerDao.updateDue(customerId, newDue)

        // If customer has an unpaid monthly bill, update it
        val unpaidBill = billDao.getLatestUnpaidBillForCustomer(customerId)
        if (unpaidBill != null) {
            val newBillPaid = unpaidBill.paidAmount + amount
            val newBillDue = (unpaidBill.billAmount - newBillPaid).coerceAtLeast(0.0)
            billDao.updateBill(
                unpaidBill.copy(
                    paidAmount = newBillPaid,
                    dueAmount = newBillDue,
                    isPaid = newBillDue <= 0.0
                )
            )
        } else {
            // Also record a payment entry
            val billEntry = BillEntry(
                customerId = customerId,
                customerName = customer.name.ifBlank { customer.pppoeUsername },
                customerCode = customer.customerCode,
                monthYear = currentMonthYear,
                billAmount = amount,
                paidAmount = amount,
                dueAmount = 0.0,
                isPaid = true,
                billDateMillis = System.currentTimeMillis(),
                notes = "$method Payment"
            )
            billDao.insertBill(billEntry)
        }
    }

    // Bills
    fun getAllBills(): Flow<List<BillEntry>> = billDao.getAllBills()

    fun getBillsForMonth(monthYear: String): Flow<List<BillEntry>> =
        billDao.getBillsForMonth(monthYear)

    fun getBillsForToday(): Flow<List<BillEntry>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.timeInMillis
        return billDao.getBillsBetween(start, end)
    }

    fun getBillsForYesterday(): Flow<List<BillEntry>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.timeInMillis
        return billDao.getBillsBetween(start, end)
    }

    suspend fun generateMonthlyBills(monthYear: String, customers: List<Customer>) {
        val activeCustomers = customers.filter { it.status == Customer.STATUS_ACTIVE }
        val alreadyBilledIds = billDao.getBilledCustomerIdsForMonth(monthYear).toSet()
        val toBill = activeCustomers.filter { it.id !in alreadyBilledIds }
        val entries = mutableListOf<BillEntry>()
        val currentTime = System.currentTimeMillis()

        for (cust in toBill) {
            val updatedDue = cust.dueAmount + cust.monthlyBill
            customerDao.updateDue(cust.id, updatedDue)
            entries.add(
                BillEntry(
                    customerId = cust.id,
                    customerName = cust.name.ifBlank { cust.pppoeUsername },
                    customerCode = cust.customerCode,
                    monthYear = monthYear,
                    billAmount = cust.monthlyBill,
                    paidAmount = 0.0,
                    dueAmount = cust.monthlyBill,
                    isPaid = false,
                    billDateMillis = currentTime,
                    notes = "Monthly bill for $monthYear"
                )
            )
        }
        if (entries.isNotEmpty()) {
            billDao.insertAllBills(entries)
        }
    }

    // Expenses
    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getExpensesForMonth(monthYear: String): Flow<List<Expense>> =
        expenseDao.getExpensesForMonth(monthYear)

    fun getTotalOutflowForMonth(monthYear: String): Flow<Double?> =
        expenseDao.getTotalOutflowForMonth(monthYear)

    fun getHighestExpenseForMonth(monthYear: String): Flow<Double?> =
        expenseDao.getHighestExpenseForMonth(monthYear)

    fun getAverageExpenseForMonth(monthYear: String): Flow<Double?> =
        expenseDao.getAverageExpenseForMonth(monthYear)

    fun getVouchersCountForMonth(monthYear: String): Flow<Int> =
        expenseDao.getVouchersCountForMonth(monthYear)

    fun getCategorySumsForMonth(monthYear: String): Flow<List<CategorySum>> =
        expenseDao.getCategorySumsForMonth(monthYear)

    suspend fun insertExpense(expense: Expense): Long =
        expenseDao.insertExpense(expense)

    suspend fun deleteExpense(expense: Expense) =
        expenseDao.deleteExpense(expense)

    // Packages
    suspend fun insertPackage(pkg: InternetPackage): Long =
        packageDao.insertPackage(pkg)

    suspend fun updatePackage(pkg: InternetPackage) =
        packageDao.updatePackage(pkg)

    suspend fun deletePackage(pkg: InternetPackage) =
        packageDao.deletePackage(pkg)

    // Settings
    suspend fun updateSettings(settings: IspSettings) =
        settingsDao.insertOrUpdate(settings)
}
