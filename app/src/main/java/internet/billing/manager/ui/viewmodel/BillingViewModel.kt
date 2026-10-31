package internet.billing.manager.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import internet.billing.manager.InternetBillingApp
import internet.billing.manager.data.local.CategorySum
import internet.billing.manager.data.model.*
import internet.billing.manager.data.repository.BillingRepository
import internet.billing.manager.util.AutoSmsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class BillingViewModel(
    private val repository: BillingRepository
) : ViewModel() {

    private val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val displayMonthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    // Selected Month & Year for Billing & Expenses
    private val _selectedCalendar = MutableStateFlow(Calendar.getInstance())
    val selectedCalendar: StateFlow<Calendar> = _selectedCalendar.asStateFlow()

    val currentMonthYearKey: StateFlow<String> = _selectedCalendar.map {
        monthYearFormat.format(it.time)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, monthYearFormat.format(Date()))

    val currentMonthYearDisplay: StateFlow<String> = _selectedCalendar.map {
        displayMonthFormat.format(it.time)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, displayMonthFormat.format(Date()))

    // Customers and Filtering
    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCustomerCount: StateFlow<Int> = repository.totalCustomerCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeCustomerCount: StateFlow<Int> = repository.activeCustomerCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val suspendedCustomerCount: StateFlow<Int> = repository.suspendedCustomerCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val inactiveCustomerCount: StateFlow<Int> = repository.inactiveCustomerCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery: StateFlow<String> = _customerSearchQuery.asStateFlow()

    private val _customerStatusFilter = MutableStateFlow("All")
    val customerStatusFilter: StateFlow<String> = _customerStatusFilter.asStateFlow()

    val filteredCustomers: StateFlow<List<Customer>> = combine(
        allCustomers,
        customerSearchQuery,
        customerStatusFilter
    ) { customers, query, status ->
        customers.filter { customer ->
            val matchesQuery = query.isBlank() ||
                    customer.name.contains(query, ignoreCase = true) ||
                    customer.phone.contains(query, ignoreCase = true) ||
                    customer.pppoeUsername.contains(query, ignoreCase = true) ||
                    customer.customerCode.contains(query, ignoreCase = true)

            val matchesStatus = status == "All" || customer.status.equals(status, ignoreCase = true)
            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Overview / Stats
    val totalActiveMonthlyBill: StateFlow<Double> = repository.totalActiveMonthlyBill
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalOutstandingDue: StateFlow<Double> = repository.totalOutstandingDue
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalCollectedThisMonth: StateFlow<Double> = currentMonthYearKey.flatMapLatest { key ->
        repository.getTotalCollectedForMonth(key).map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalCollectedToday: StateFlow<Double> = repository.getTotalCollectedToday()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val recentPayments: StateFlow<List<PaymentTransaction>> = repository.recentPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPackages: StateFlow<List<InternetPackage>> = repository.allPackages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ispSettings: StateFlow<IspSettings?> = repository.ispSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Daily Bill Entries
    private val _dailyBillFilter = MutableStateFlow("Today") // "All", "Today", "Yesterday", "This Month"
    val dailyBillFilter: StateFlow<String> = _dailyBillFilter.asStateFlow()

    val dailyBillEntries: StateFlow<List<BillEntry>> = combine(
        _dailyBillFilter,
        currentMonthYearKey
    ) { filter, monthKey ->
        Pair(filter, monthKey)
    }.flatMapLatest { (filter, monthKey) ->
        when (filter) {
            "Today" -> repository.getBillsForToday()
            "Yesterday" -> repository.getBillsForYesterday()
            "This Month" -> repository.getBillsForMonth(monthKey)
            else -> repository.getAllBills()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Monthly Bills for selected month (User Generated Bills)
    val monthlyBills: StateFlow<List<BillEntry>> = currentMonthYearKey.flatMapLatest { monthKey ->
        repository.getBillsForMonth(monthKey)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Bills across all months
    val allBills: StateFlow<List<BillEntry>> = repository.getAllBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Aggregated Single-Card Customer Billing List (Excludes fully paid customers)
    val customerBillingItems: StateFlow<List<CustomerBillingItem>> = combine(
        allCustomers,
        allBills,
        currentMonthYearKey
    ) { customers, bills, monthKey ->
        val list = mutableListOf<CustomerBillingItem>()
        for (cust in customers) {
            val custBills = bills.filter { it.customerId == cust.id }
            val currentBill = custBills.firstOrNull { it.monthYear == monthKey }
            val unpaidBills = custBills.filter { !it.isPaid && it.dueAmount > 0 }

            val totalCustomerDue = cust.dueAmount
            val isCurrentPaid = currentBill != null && currentBill.isPaid && currentBill.dueAmount <= 0.0

            if (totalCustomerDue <= 0.0 && (currentBill == null || isCurrentPaid) && unpaidBills.isEmpty()) {
                // Customer has no dues - automatically leaves Billing screen!
                continue
            }

            // Running month's unpaid bill due
            val currentBillDue = when {
                currentBill != null -> currentBill.dueAmount
                cust.status == Customer.STATUS_ACTIVE -> cust.monthlyBill
                else -> 0.0
            }

            // Current due is capped at customer's total due
            val currentDue = minOf(currentBillDue, totalCustomerDue)
            // Previous due is strictly the remainder of customer's total due
            val previousDue = (totalCustomerDue - currentDue).coerceAtLeast(0.0)
            // Total payable STRICTLY equals totalCustomerDue!
            val totalPayable = totalCustomerDue

            if (totalPayable > 0.0) {
                val dueMonthsText = computeDueMonthsText(
                    unpaidBills = unpaidBills,
                    currentMonthKey = monthKey,
                    previousDue = previousDue,
                    monthlyBill = cust.monthlyBill
                )

                list.add(
                    CustomerBillingItem(
                        customer = cust,
                        currentMonthKey = monthKey,
                        currentMonthBill = currentDue,
                        previousDue = previousDue,
                        dueMonthsText = dueMonthsText,
                        totalPayable = totalPayable,
                        isPaid = false,
                        latestBillId = currentBill?.id ?: 0L
                    )
                )
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expenses
    val monthlyExpenses: StateFlow<List<Expense>> = currentMonthYearKey.flatMapLatest { monthKey ->
        repository.getExpensesForMonth(monthKey)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseOutflow: StateFlow<Double> = currentMonthYearKey.flatMapLatest { monthKey ->
        repository.getTotalOutflowForMonth(monthKey).map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val highestExpense: StateFlow<Double> = currentMonthYearKey.flatMapLatest { monthKey ->
        repository.getHighestExpenseForMonth(monthKey).map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val averageExpense: StateFlow<Double> = currentMonthYearKey.flatMapLatest { monthKey ->
        repository.getAverageExpenseForMonth(monthKey).map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val vouchersCount: StateFlow<Int> = currentMonthYearKey.flatMapLatest { monthKey ->
        repository.getVouchersCountForMonth(monthKey)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val categorySums: StateFlow<List<CategorySum>> = currentMonthYearKey.flatMapLatest { monthKey ->
        repository.getCategorySumsForMonth(monthKey)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyBandwidthBill: StateFlow<Double> = monthlyExpenses.map { expenses ->
        expenses.filter {
            it.category.equals(Expense.CAT_BANDWIDTH_BILL, ignoreCase = true) ||
            it.category.contains("bandwidth", ignoreCase = true) ||
            it.title.contains("bandwidth", ignoreCase = true)
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // UI Dialog States
    var showAddCustomerDialog by mutableStateOf(false)
    var customerToEdit by mutableStateOf<Customer?>(null)
    var selectedCustomerForDetails by mutableStateOf<Customer?>(null)
    var customerToDelete by mutableStateOf<Customer?>(null)

    var showCollectPaymentDialog by mutableStateOf(false)
    var customerForPayment by mutableStateOf<Customer?>(null)

    var showGenerateBillDialog by mutableStateOf(false)
    var showAddManualBillDialog by mutableStateOf(false)
    var paymentToDelete by mutableStateOf<PaymentTransaction?>(null)
    var showAddExpenseDialog by mutableStateOf(false)
    var defaultExpenseCategory by mutableStateOf(Expense.CAT_BANDWIDTH_BILL)
    var defaultExpenseTitle by mutableStateOf("")

    fun openAddExpense(category: String = Expense.CAT_BANDWIDTH_BILL, title: String = "") {
        defaultExpenseCategory = category
        defaultExpenseTitle = title
        showAddExpenseDialog = true
    }

    var showPackagesManagerDialog by mutableStateOf(false)
    var showAddPackageDialog by mutableStateOf(false)
    var packageToEdit by mutableStateOf<InternetPackage?>(null)

    var showEditBusinessInfoDialog by mutableStateOf(false)

    // User Actions
    fun setCustomerSearchQuery(query: String) {
        _customerSearchQuery.value = query
    }

    fun setCustomerStatusFilter(status: String) {
        _customerStatusFilter.value = status
    }

    fun setDailyBillFilter(filter: String) {
        _dailyBillFilter.value = filter
    }

    fun nextMonth() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _selectedCalendar.value.timeInMillis
            add(Calendar.MONTH, 1)
        }
        _selectedCalendar.value = cal
    }

    fun previousMonth() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _selectedCalendar.value.timeInMillis
            add(Calendar.MONTH, -1)
        }
        _selectedCalendar.value = cal
    }

    fun saveCustomer(customer: Customer, previousDues: List<PreviousDueItem> = emptyList()) {
        viewModelScope.launch {
            val customerId = if (customer.id == 0L) {
                repository.insertCustomer(customer)
            } else {
                repository.updateCustomer(customer)
                customer.id
            }

            if (previousDues.isNotEmpty()) {
                val monthMap = mapOf(
                    "January" to "01", "February" to "02", "March" to "03", "April" to "04",
                    "May" to "05", "June" to "06", "July" to "07", "August" to "08",
                    "September" to "09", "October" to "10", "November" to "11", "December" to "12"
                )
                for (item in previousDues) {
                    if (item.amount > 0) {
                        val mNum = monthMap[item.month] ?: "01"
                        val key = "${item.year}-$mNum"
                        repository.insertBill(
                            BillEntry(
                                customerId = customerId,
                                customerName = customer.name.ifBlank { customer.pppoeUsername },
                                customerCode = customer.customerCode,
                                monthYear = key,
                                billAmount = item.amount,
                                paidAmount = 0.0,
                                dueAmount = item.amount,
                                isPaid = false,
                                notes = "Previous Due (${item.month} ${item.year})"
                            )
                        )
                    }
                }
            }

            showAddCustomerDialog = false
            customerToEdit = null
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    fun collectPayment(customerId: Long, amount: Double, method: String, note: String) {
        viewModelScope.launch {
            repository.recordPayment(customerId, amount, method, note)
            // Trigger Auto SMS Receipt if enabled
            if (AutoSmsManager.isAutoSmsEnabled && AutoSmsManager.isAutoPaymentSmsEnabled) {
                val cust = allCustomers.value.firstOrNull { it.id == customerId }
                if (cust != null && cust.phone.isNotBlank()) {
                    val rawTpl = AutoSmsManager.getTemplateText("payment_received", InternetBillingApp.instance)
                    val msg = AutoSmsManager.formatMessage(rawTpl, cust, ispSettings.value, extraAmount = amount)
                    AutoSmsManager.sendSingleSms(InternetBillingApp.instance, cust.phone, msg)
                }
            }
            showCollectPaymentDialog = false
            customerForPayment = null
        }
    }

    fun generateBills() {
        viewModelScope.launch {
            val key = currentMonthYearKey.value
            val customers = allCustomers.value
            repository.generateMonthlyBills(key, customers)
            // Trigger Auto SMS Bill notification if enabled
            if (AutoSmsManager.isAutoSmsEnabled && AutoSmsManager.isAutoBillSmsEnabled) {
                val targets = customers.filter { it.status == "Active" && it.phone.isNotBlank() }
                if (targets.isNotEmpty()) {
                    val rawTpl = AutoSmsManager.getTemplateText("bill_generated", InternetBillingApp.instance)
                    AutoSmsManager.sendBatchSms(
                        context = InternetBillingApp.instance,
                        customers = targets,
                        template = rawTpl,
                        ispSettings = ispSettings.value,
                        onProgress = { _, _, _, _ -> }
                    )
                }
            }
            showGenerateBillDialog = false
        }
    }

    fun saveExpense(expense: Expense) {
        viewModelScope.launch {
            val toSave = if (expense.monthYear.isBlank()) {
                expense.copy(monthYear = currentMonthYearKey.value)
            } else {
                expense
            }
            repository.insertExpense(toSave)
            showAddExpenseDialog = false
            defaultExpenseTitle = ""
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun savePackage(pkg: InternetPackage) {
        viewModelScope.launch {
            if (pkg.id == 0L) {
                repository.insertPackage(pkg)
            } else {
                repository.updatePackage(pkg)
            }
            showAddPackageDialog = false
            packageToEdit = null
        }
    }

    fun deletePackage(pkg: InternetPackage) {
        viewModelScope.launch {
            repository.deletePackage(pkg)
        }
    }

    fun saveBusinessInfo(settings: IspSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            showEditBusinessInfoDialog = false
        }
    }

    fun updateCustomerStatus(customer: Customer, newStatus: String) {
        viewModelScope.launch {
            repository.updateCustomerStatus(customer.id, newStatus)
            if (selectedCustomerForDetails?.id == customer.id) {
                selectedCustomerForDetails = customer.copy(status = newStatus)
            }
        }
    }

    fun deletePayment(payment: PaymentTransaction) {
        viewModelScope.launch {
            repository.deletePaymentAndReverse(payment)
            paymentToDelete = null
        }
    }

    fun addManualBill(customerId: Long, monthYear: String, amount: Double, notes: String) {
        viewModelScope.launch {
            repository.addManualBill(customerId, monthYear, amount, notes)
            showAddManualBillDialog = false
        }
    }
}

data class CustomerBillingItem(
    val customer: Customer,
    val currentMonthKey: String,
    val currentMonthBill: Double,
    val previousDue: Double,
    val dueMonthsText: String,
    val totalPayable: Double,
    val isPaid: Boolean,
    val latestBillId: Long = 0L
)

private fun formatShortMonth(monthYear: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val formatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val date = parser.parse(monthYear)
        if (date != null) formatter.format(date) else monthYear
    } catch (e: Exception) {
        monthYear
    }
}

private fun computeDueMonthsText(
    unpaidBills: List<BillEntry>,
    currentMonthKey: String,
    previousDue: Double,
    monthlyBill: Double
): String {
    val recordedPastMonths = unpaidBills
        .filter { it.monthYear < currentMonthKey }
        .map { it.monthYear }
        .distinct()
        .sorted()

    val months = if (recordedPastMonths.isNotEmpty()) {
        recordedPastMonths.map { formatShortMonth(it) }
    } else if (previousDue > 0) {
        val count = if (monthlyBill > 0) {
            Math.ceil(previousDue / monthlyBill).toInt().coerceIn(1, 12)
        } else {
            1
        }
        val cal = Calendar.getInstance()
        try {
            val parser = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            cal.time = parser.parse(currentMonthKey) ?: Date()
        } catch (e: Exception) {
            cal.time = Date()
        }
        val generated = mutableListOf<String>()
        for (i in count downTo 1) {
            val pastCal = cal.clone() as Calendar
            pastCal.add(Calendar.MONTH, -i)
            val key = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(pastCal.time)
            generated.add(formatShortMonth(key))
        }
        generated
    } else {
        emptyList()
    }

    return if (months.isNotEmpty()) months.joinToString(", ") else ""
}

class BillingViewModelFactory(
    private val repository: BillingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BillingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
