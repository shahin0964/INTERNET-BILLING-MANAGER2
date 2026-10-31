package internet.billing.manager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import internet.billing.manager.ui.components.*
import internet.billing.manager.ui.theme.BgDark
import internet.billing.manager.ui.viewmodel.BillingViewModel

enum class ActiveScreen {
    DASHBOARD,
    CUSTOMERS,
    BILLING,
    COLLECTION,
    DAILY_ENTRY,
    EXPENSE,
    SETTINGS
}

@Composable
fun MainScreen(
    viewModel: BillingViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(ActiveScreen.DASHBOARD) }
    var selectedNavTab by remember { mutableStateOf(BottomNavScreen.DASHBOARD) }

    val allCustomers by viewModel.allCustomers.collectAsState()
    val allPackages by viewModel.allPackages.collectAsState()
    val ispSettings by viewModel.ispSettings.collectAsState()
    val activeCount by viewModel.activeCustomerCount.collectAsState()
    val monthlyBill by viewModel.totalActiveMonthlyBill.collectAsState()
    val monthYearKey by viewModel.currentMonthYearKey.collectAsState()

    // Handle back button when inside nested sub-screens
    BackHandler(enabled = currentScreen == ActiveScreen.DAILY_ENTRY || currentScreen == ActiveScreen.EXPENSE) {
        currentScreen = ActiveScreen.COLLECTION
        selectedNavTab = BottomNavScreen.COLLECTION
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgDark,
        bottomBar = {
            BottomNavBar(
                currentScreen = selectedNavTab,
                onScreenSelected = { tab ->
                    selectedNavTab = tab
                    currentScreen = when (tab) {
                        BottomNavScreen.DASHBOARD -> ActiveScreen.DASHBOARD
                        BottomNavScreen.CUSTOMERS -> ActiveScreen.CUSTOMERS
                        BottomNavScreen.BILLING -> ActiveScreen.BILLING
                        BottomNavScreen.COLLECTION -> ActiveScreen.COLLECTION
                        BottomNavScreen.MORE -> ActiveScreen.SETTINGS
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                ActiveScreen.DASHBOARD -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToCustomers = {
                            selectedNavTab = BottomNavScreen.CUSTOMERS
                            currentScreen = ActiveScreen.CUSTOMERS
                        },
                        onNavigateToBilling = {
                            selectedNavTab = BottomNavScreen.BILLING
                            currentScreen = ActiveScreen.BILLING
                        },
                        onNavigateToCollection = {
                            selectedNavTab = BottomNavScreen.COLLECTION
                            currentScreen = ActiveScreen.COLLECTION
                        },
                        onNavigateToMore = {
                            selectedNavTab = BottomNavScreen.MORE
                            currentScreen = ActiveScreen.SETTINGS
                        }
                    )
                }

                ActiveScreen.CUSTOMERS -> {
                    CustomersScreen(viewModel = viewModel)
                }

                ActiveScreen.BILLING -> {
                    BillingScreen(viewModel = viewModel)
                }

                ActiveScreen.COLLECTION -> {
                    CollectionScreen(
                        viewModel = viewModel,
                        onNavigateToDailyEntry = {
                            currentScreen = ActiveScreen.DAILY_ENTRY
                        },
                        onNavigateToExpense = {
                            currentScreen = ActiveScreen.EXPENSE
                        }
                    )
                }

                ActiveScreen.DAILY_ENTRY -> {
                    DailyBillEntryScreen(
                        viewModel = viewModel,
                        onBack = {
                            selectedNavTab = BottomNavScreen.COLLECTION
                            currentScreen = ActiveScreen.COLLECTION
                        }
                    )
                }

                ActiveScreen.EXPENSE -> {
                    ExpenseScreen(
                        viewModel = viewModel,
                        onBack = {
                            selectedNavTab = BottomNavScreen.COLLECTION
                            currentScreen = ActiveScreen.COLLECTION
                        }
                    )
                }

                ActiveScreen.SETTINGS -> {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Interactive Dialogs
    if (viewModel.showAddCustomerDialog) {
        AddEditCustomerDialog(
            customerToEdit = viewModel.customerToEdit,
            packages = allPackages,
            onDismiss = {
                viewModel.showAddCustomerDialog = false
                viewModel.customerToEdit = null
            },
            onSave = { customer ->
                viewModel.saveCustomer(customer)
            }
        )
    }

    if (viewModel.showCollectPaymentDialog) {
        CollectPaymentDialog(
            customers = allCustomers,
            initialCustomer = viewModel.customerForPayment,
            onDismiss = {
                viewModel.showCollectPaymentDialog = false
                viewModel.customerForPayment = null
            },
            onCollect = { customerId, amount, method, note ->
                viewModel.collectPayment(customerId, amount, method, note)
            }
        )
    }

    if (viewModel.showGenerateBillDialog) {
        GenerateBillDialog(
            activeCustomerCount = activeCount,
            totalMonthlyBill = monthlyBill,
            monthYear = monthYearKey,
            onDismiss = { viewModel.showGenerateBillDialog = false },
            onConfirm = { viewModel.generateBills() }
        )
    }

    if (viewModel.showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { viewModel.showAddExpenseDialog = false },
            onSave = { expense -> viewModel.saveExpense(expense) }
        )
    }

    if (viewModel.showPackagesManagerDialog) {
        InternetPackagesManagerDialog(
            packages = allPackages,
            onDismiss = { viewModel.showPackagesManagerDialog = false },
            onAddPackage = {
                viewModel.packageToEdit = null
                viewModel.showAddPackageDialog = true
            },
            onEditPackage = { pkg ->
                viewModel.packageToEdit = pkg
                viewModel.showAddPackageDialog = true
            }
        )
    }

    if (viewModel.showAddPackageDialog) {
        AddPackageDialog(
            packageToEdit = viewModel.packageToEdit,
            onDismiss = {
                viewModel.showAddPackageDialog = false
                viewModel.packageToEdit = null
            },
            onSave = { pkg ->
                viewModel.savePackage(pkg)
                viewModel.showAddPackageDialog = false
                viewModel.packageToEdit = null
            },
            onDelete = { pkg ->
                viewModel.deletePackage(pkg)
                viewModel.showAddPackageDialog = false
                viewModel.packageToEdit = null
            }
        )
    }

    if (viewModel.showEditBusinessInfoDialog) {
        EditBusinessInfoDialog(
            currentSettings = ispSettings,
            onDismiss = { viewModel.showEditBusinessInfoDialog = false },
            onSave = { settings -> viewModel.saveBusinessInfo(settings) }
        )
    }
}
