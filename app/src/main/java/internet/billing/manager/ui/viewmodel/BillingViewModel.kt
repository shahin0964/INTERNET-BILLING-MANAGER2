package internet.billing.manager.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import internet.billing.manager.data.local.CategorySum
import internet.billing.manager.data.model.*
import internet.billing.manager.data.repository.BillingRepository
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

    // UI Dialog States
    var showAddCustomerDialog by mutableStateOf(false)
    var customerToEdit by mutableStateOf<Customer?>(null)

    var showCollectPaymentDialog by mutableStateOf(false)
    var customerForPayment by mutableStateOf<Customer?>(null)

    var showGenerateBillDialog by mutableStateOf(false)
    var showAddExpenseDialog by mutableStateOf(false)

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

    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            if (customer.id == 0L) {
                repository.insertCustomer(customer)
            } else {
                repository.updateCustomer(customer)
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
            showCollectPaymentDialog = false
            customerForPayment = null
        }
    }

    fun generateBills() {
        viewModelScope.launch {
            val key = currentMonthYearKey.value
            val customers = allCustomers.value
            repository.generateMonthlyBills(key, customers)
            showGenerateBillDialog = false
        }
    }

    fun saveExpense(expense: Expense) {
        viewModelScope.launch {
            repository.insertExpense(expense)
            showAddExpenseDialog = false
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
