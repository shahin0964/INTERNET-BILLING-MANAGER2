package internet.billing.manager.data.repository

import internet.billing.manager.InternetBillingApp
import internet.billing.manager.data.local.*
import internet.billing.manager.data.model.*
import internet.billing.manager.util.AutoBackupManager
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

    suspend fun insertCustomer(customer: Customer): Long {
        val id = customerDao.insertCustomer(customer)
        AutoBackupManager.scheduleDebouncedBackup(InternetBillingApp.instance)
        return id
    }

    suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer)
        AutoBackupManager.scheduleDebouncedBackup(InternetBillingApp.instance)
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer)
        AutoBackupManager.scheduleDebouncedBackup(InternetBillingApp.instance)
    }

    suspend fun getCustomerById(id: Long): Customer? =
        customerDao.getCustomerById(id)

    suspend fun getCustomersByStatusList(status: String): List<Customer> =
        customerDao.getCustomersByStatusList(status)

    suspend fun getAllCustomersList(): List<Customer> =
        customerDao.getAllCustomersList()

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

    fun getTotalDiscountForMonth(monthYear: String): Flow<Double?> =
        paymentDao.getTotalDiscountForMonth(monthYear)

    suspend fun recordPayment(
        customerId: Long,
        amount: Double,
        discount: Double = 0.0,
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
            discount = discount,
            paymentMethod = method,
            dateMillis = System.currentTimeMillis(),
            monthYear = currentMonthYear,
            note = note
        )
        paymentDao.insertPayment(transaction)

        val totalDeduction = amount + discount

        // 1. Deduct from customer total due amount
        val newDue = (customer.dueAmount - totalDeduction).coerceAtLeast(0.0)
        customerDao.updateDue(customerId, newDue)

        // 2. Fetch all unpaid bills for this customer in chronological order (oldest first - FIFO)
        val unpaidBills = billDao.getUnpaidBillsForCustomer(customerId)
        val pastUnpaidBills = unpaidBills.filter { it.monthYear < currentMonthYear }
        val currentMonthBill = unpaidBills.firstOrNull { it.monthYear == currentMonthYear }

        var remainingPayment = totalDeduction

        // Step A: Pay past recorded bills first (oldest first)
        for (bill in pastUnpaidBills) {
            if (remainingPayment <= 0.0) break
            val billDue = bill.dueAmount
            if (billDue > 0.0) {
                val payThis = minOf(remainingPayment, billDue)
                val newPaid = bill.paidAmount + payThis
                val newBillDue = (bill.billAmount - newPaid).coerceAtLeast(0.0)
                billDao.updateBill(
                    bill.copy(
                        paidAmount = newPaid,
                        dueAmount = newBillDue,
                        isPaid = newBillDue <= 0.0
                    )
                )
                remainingPayment -= payThis
            }
        }

        // Step B: If customer had previous dues not recorded as separate bill_entries,
        // absorb payment towards that past due balance before touching the running bill!
        val currentBillDue = currentMonthBill?.dueAmount ?: customer.monthlyBill
        val initialPreviousDue = (customer.dueAmount - currentBillDue).coerceAtLeast(0.0)
        val pastRecordedDueSum = pastUnpaidBills.sumOf { it.dueAmount }
        val unrecordedPreviousDue = (initialPreviousDue - pastRecordedDueSum).coerceAtLeast(0.0)

        if (unrecordedPreviousDue > 0.0 && remainingPayment > 0.0) {
            val payUnrecorded = minOf(remainingPayment, unrecordedPreviousDue)
            remainingPayment -= payUnrecorded
        }

        // Step C: Only after all previous dues are satisfied, apply remaining payment to running month's bill!
        if (currentMonthBill != null && remainingPayment > 0.0) {
            val payThis = minOf(remainingPayment, currentMonthBill.dueAmount)
            val newPaid = currentMonthBill.paidAmount + payThis
            val newBillDue = (currentMonthBill.billAmount - newPaid).coerceAtLeast(0.0)
            billDao.updateBill(
                currentMonthBill.copy(
                    paidAmount = newPaid,
                    dueAmount = newBillDue,
                    isPaid = newBillDue <= 0.0
                )
            )
            remainingPayment -= payThis
        }
        AutoBackupManager.scheduleDebouncedBackup(InternetBillingApp.instance)
    }

    suspend fun deletePaymentAndReverse(payment: PaymentTransaction) {
        paymentDao.deletePayment(payment)

        val customer = customerDao.getCustomerById(payment.customerId) ?: return
        val totalToRestore = payment.amount + payment.discount
        val restoredDue = customer.dueAmount + totalToRestore
        customerDao.updateDue(customer.id, restoredDue)

        // Reverse bill payments in LIFO order (newest paid bills first)
        val customerBills = billDao.getAllBillsForCustomer(payment.customerId)
            .sortedWith(compareByDescending<BillEntry> { it.monthYear }.thenByDescending { it.id })

        var remainingToReverse = totalToRestore
        for (bill in customerBills) {
            if (remainingToReverse <= 0.0) break
            if (bill.paidAmount > 0.0) {
                val reverseThis = minOf(remainingToReverse, bill.paidAmount)
                val newPaid = (bill.paidAmount - reverseThis).coerceAtLeast(0.0)
                val newDue = (bill.billAmount - newPaid).coerceAtLeast(0.0)
                billDao.updateBill(
                    bill.copy(
                        paidAmount = newPaid,
                        dueAmount = newDue,
                        isPaid = newDue <= 0.0
                    )
                )
                remainingToReverse -= reverseThis
            }
        }
        AutoBackupManager.scheduleDebouncedBackup(InternetBillingApp.instance)
    }

    suspend fun addManualBill(customerId: Long, monthYear: String, amount: Double, notes: String): Long {
        val customer = customerDao.getCustomerById(customerId) ?: return 0L
        val existing = billDao.getBillForCustomerMonth(customerId, monthYear)
        val updatedDue = customer.dueAmount + amount
        customerDao.updateDue(customerId, updatedDue)
        val id = if (existing != null) {
            val updatedBill = existing.copy(
                billAmount = existing.billAmount + amount,
                dueAmount = existing.dueAmount + amount,
                isPaid = false,
                billDateMillis = System.currentTimeMillis(),
                notes = if (notes.isNotBlank()) notes else existing.notes.ifBlank { "Bill for $monthYear" }
            )
            billDao.updateBill(updatedBill)
            existing.id
        } else {
            billDao.insertBill(
                BillEntry(
                    customerId = customerId,
                    customerName = customer.name.ifBlank { customer.pppoeUsername },
                    customerCode = customer.customerCode,
                    monthYear = monthYear,
                    billAmount = amount,
                    paidAmount = 0.0,
                    dueAmount = amount,
                    isPaid = false,
                    billDateMillis = System.currentTimeMillis(),
                    notes = notes.ifBlank { "Bill for $monthYear" }
                )
            )
        }
        AutoBackupManager.scheduleDebouncedBackup(InternetBillingApp.instance)
        return id
    }

    suspend fun updateCustomerStatus(customerId: Long, status: String) {
        customerDao.updateStatus(customerId, status)
        AutoBackupManager.scheduleDebouncedBackup(InternetBillingApp.instance)
    }

    suspend fun getUnpaidBillsForCustomer(customerId: Long): List<BillEntry> =
        billDao.getUnpaidBillsForCustomer(customerId)

    // Bills
    val unpaidBills: Flow<List<BillEntry>> = billDao.getUnpaidBills()

    suspend fun insertBill(bill: BillEntry): Long {
        val id = billDao.insertBill(bill)
        AutoBackupManager.scheduleDebouncedBackup(InternetBillingApp.instance)
        return id
    }

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

    fun getAllPayments(): Flow<List<PaymentTransaction>> = paymentDao.getAllPayments()

    fun getPaymentsForToday(): Flow<List<PaymentTransaction>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        return paymentDao.getPaymentsBetween(start, end)
    }

    fun getPaymentsForYesterday(): Flow<List<PaymentTransaction>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        return paymentDao.getPaymentsBetween(start, end)
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
        AutoBackupManager.scheduleDebouncedBackup(InternetBillingApp.instance)
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

    suspend fun insertExpense(expense: Expense): Long {
        val id = expenseDao.insertExpense(expense)
        AutoBackupManager.scheduleDebouncedBackup(InternetBillingApp.instance)
        return id
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
        AutoBackupManager.scheduleDebouncedBackup(InternetBillingApp.instance)
    }

    // Packages
    suspend fun insertPackage(pkg: InternetPackage): Long {
        val id = packageDao.insertPackage(pkg)
        AutoBackupManager.scheduleDebouncedBackup(InternetBillingApp.instance)
        return id
    }

    suspend fun updatePackage(pkg: InternetPackage) {
        packageDao.updatePackage(pkg)
        AutoBackupManager.scheduleDebouncedBackup(InternetBillingApp.instance)
    }

    suspend fun deletePackage(pkg: InternetPackage) {
        packageDao.deletePackage(pkg)
        AutoBackupManager.scheduleDebouncedBackup(InternetBillingApp.instance)
    }

    // Settings
    suspend fun updateSettings(settings: IspSettings) {
        settingsDao.insertOrUpdate(settings)
        AutoBackupManager.scheduleDebouncedBackup(InternetBillingApp.instance)
    }
}
