package internet.billing.manager.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import internet.billing.manager.data.model.Customer
import internet.billing.manager.ui.components.*
import internet.billing.manager.ui.localization.appStrings
import internet.billing.manager.ui.theme.*
import internet.billing.manager.ui.viewmodel.BillingViewModel

enum class ActiveScreen {
    DASHBOARD,
    CUSTOMERS,
    BILLING,
    COLLECTION,
    DAILY_ENTRY,
    EXPENSE,
    SETTINGS,
    ADVANCED_FEATURES,
    AUTO_SMS,
    NETWORK_TOOLS,
    RECEIPT_CUSTOMIZATION
}

@Composable
fun MainScreen(
    viewModel: BillingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(ActiveScreen.DASHBOARD) }
    var selectedNavTab by remember { mutableStateOf(BottomNavScreen.DASHBOARD) }
    var lastBackPressedTime by remember { mutableLongStateOf(0L) }

    val allCustomers by viewModel.allCustomers.collectAsState()
    val allPackages by viewModel.allPackages.collectAsState()
    val ispSettings by viewModel.ispSettings.collectAsState()
    val activeCount by viewModel.activeCustomerCount.collectAsState()
    val monthlyBill by viewModel.totalActiveMonthlyBill.collectAsState()
    val monthYearKey by viewModel.currentMonthYearKey.collectAsState()

    // 1. Handle back button when inside nested sub-screens
    BackHandler(enabled = currentScreen == ActiveScreen.NETWORK_TOOLS) {
        currentScreen = ActiveScreen.ADVANCED_FEATURES
    }

    BackHandler(enabled = currentScreen == ActiveScreen.RECEIPT_CUSTOMIZATION) {
        currentScreen = ActiveScreen.ADVANCED_FEATURES
    }

    BackHandler(enabled = currentScreen == ActiveScreen.AUTO_SMS) {
        currentScreen = ActiveScreen.ADVANCED_FEATURES
    }

    BackHandler(enabled = currentScreen == ActiveScreen.ADVANCED_FEATURES) {
        currentScreen = ActiveScreen.SETTINGS
        selectedNavTab = BottomNavScreen.MORE
    }

    BackHandler(enabled = currentScreen == ActiveScreen.DAILY_ENTRY || currentScreen == ActiveScreen.EXPENSE) {
        currentScreen = ActiveScreen.COLLECTION
        selectedNavTab = BottomNavScreen.COLLECTION
    }

    // 2. Handle back button when on main navigation tabs (not Dashboard) -> go back to Dashboard
    val isNonDashboardNavScreen = currentScreen == ActiveScreen.CUSTOMERS ||
            currentScreen == ActiveScreen.BILLING ||
            currentScreen == ActiveScreen.COLLECTION ||
            currentScreen == ActiveScreen.SETTINGS

    BackHandler(enabled = isNonDashboardNavScreen) {
        currentScreen = ActiveScreen.DASHBOARD
        selectedNavTab = BottomNavScreen.DASHBOARD
    }

    // 3. Double-back-to-exit when on Dashboard screen
    BackHandler(enabled = currentScreen == ActiveScreen.DASHBOARD) {
        val now = System.currentTimeMillis()
        if (now - lastBackPressedTime < 2000L) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressedTime = now
            Toast.makeText(context, appStrings.pressAgainToExit, Toast.LENGTH_SHORT).show()
        }
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
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToAdvancedFeatures = {
                            currentScreen = ActiveScreen.ADVANCED_FEATURES
                        }
                    )
                }

                ActiveScreen.ADVANCED_FEATURES -> {
                    AdvancedFeaturesScreen(
                        onBack = {
                            currentScreen = ActiveScreen.SETTINGS
                            selectedNavTab = BottomNavScreen.MORE
                        },
                        onNavigateToAutoSms = {
                            currentScreen = ActiveScreen.AUTO_SMS
                        },
                        onNavigateToNetworkTools = {
                            currentScreen = ActiveScreen.NETWORK_TOOLS
                        },
                        onNavigateToReceiptCustomization = {
                            currentScreen = ActiveScreen.RECEIPT_CUSTOMIZATION
                        }
                    )
                }

                ActiveScreen.RECEIPT_CUSTOMIZATION -> {
                    ReceiptCustomizationScreen(
                        ispSettings = ispSettings,
                        onBack = {
                            currentScreen = ActiveScreen.ADVANCED_FEATURES
                        }
                    )
                }

                ActiveScreen.AUTO_SMS -> {
                    AutoSmsScreen(
                        viewModel = viewModel,
                        onBack = {
                            currentScreen = ActiveScreen.ADVANCED_FEATURES
                        }
                    )
                }

                ActiveScreen.NETWORK_TOOLS -> {
                    NetworkToolsScreen(
                        onBack = {
                            currentScreen = ActiveScreen.ADVANCED_FEATURES
                        }
                    )
                }
            }
        }
    }

    // Interactive Dialogs
    if (viewModel.showAddCustomerDialog) {
        AddEditCustomerDialog(
            customerToEdit = viewModel.customerToEdit,
            existingCustomers = allCustomers,
            packages = allPackages,
            onDismiss = {
                viewModel.showAddCustomerDialog = false
                viewModel.customerToEdit = null
            },
            onSave = { customer, previousDues ->
                viewModel.saveCustomer(customer, previousDues)
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
            onCollect = { customerId, amount, discount, method, note ->
                viewModel.collectPayment(customerId, amount, discount, method, note)
            }
        )
    }

    val monthlyBills by viewModel.monthlyBills.collectAsState()
    val billedCustomerIds = remember(monthlyBills) {
        monthlyBills.map { it.customerId }.toSet()
    }

    if (viewModel.showGenerateBillDialog) {
        GenerateBillDialog(
            allCustomers = allCustomers,
            billedCustomerIds = billedCustomerIds,
            activeCustomerCount = activeCount,
            totalMonthlyBill = monthlyBill,
            monthYear = monthYearKey,
            onDismiss = { viewModel.showGenerateBillDialog = false },
            onConfirm = { viewModel.generateBills() },
            onConfirmSingle = { customerId, amount, notes ->
                viewModel.addManualBill(customerId, monthYearKey, amount, notes)
                viewModel.showGenerateBillDialog = false
            }
        )
    }

    if (viewModel.showAddExpenseDialog) {
        val currentMonthKey by viewModel.currentMonthYearKey.collectAsState()
        AddExpenseDialog(
            initialCategory = viewModel.defaultExpenseCategory,
            initialTitle = viewModel.defaultExpenseTitle,
            currentMonthKey = currentMonthKey,
            onDismiss = {
                viewModel.showAddExpenseDialog = false
                viewModel.defaultExpenseTitle = ""
            },
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

    // 1. Customer Details Dialog (Global across all screens)
    val customerForDetails = viewModel.selectedCustomerForDetails
    if (customerForDetails != null) {
        CustomerDetailsDialog(
            customer = customerForDetails,
            onDismiss = { viewModel.selectedCustomerForDetails = null },
            onEdit = {
                viewModel.customerToEdit = customerForDetails
                viewModel.showAddCustomerDialog = true
            },
            onStatusChange = { newStatus ->
                viewModel.updateCustomerStatus(customerForDetails, newStatus)
            },
            onDueDayChange = { newDueDay ->
                viewModel.updateCustomerDueDay(customerForDetails, newDueDay)
            },
            onCollectPayment = {
                viewModel.customerForPayment = customerForDetails
                viewModel.showCollectPaymentDialog = true
            },
            onDelete = {
                viewModel.customerToDelete = customerForDetails
            }
        )
    }

    // 2. Customer Delete Confirmation Dialog
    val custToDelete = viewModel.customerToDelete
    if (custToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.customerToDelete = null },
            containerColor = BgCard,
            title = {
                Text(
                    text = appStrings.deleteBtn,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${appStrings.confirmDeleteCustomer}\n\n${custToDelete.name.ifBlank { custToDelete.pppoeUsername }} (${custToDelete.customerCode})",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(custToDelete)
                        viewModel.customerToDelete = null
                        if (viewModel.selectedCustomerForDetails?.id == custToDelete.id) {
                            viewModel.selectedCustomerForDetails = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(appStrings.deleteBtn, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.customerToDelete = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(appStrings.cancel, color = TextSecondary)
                }
            }
        )
    }

    // 3. Manual Add Bill to Billing Dialog
    if (viewModel.showAddManualBillDialog) {
        ManualAddBillDialog(
            allCustomers = allCustomers,
            currentMonthKey = monthYearKey,
            onDismiss = { viewModel.showAddManualBillDialog = false },
            onAddBill = { customerId, monthYear, amount, notes ->
                viewModel.addManualBill(customerId, monthYear, amount, notes)
            }
        )
    }

    // 4. Delete Payment Transaction Dialog (with reversal)
    val txToDelete = viewModel.paymentToDelete
    if (txToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.paymentToDelete = null },
            containerColor = BgCard,
            title = {
                Text(
                    text = "বিল কালেকশন ডিলিট / Delete Payment",
                    color = AccentRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "আপনি কি নিশ্চিতভাবে এই পেমেন্ট এন্ট্রিটি ডিলিট করতে চান?",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "গ্রাহক: ${txToDelete.customerName}\nপরিমাণ: ৳${txToDelete.amount.toInt()}\n\n⚠️ এটি ডিলিট করলে সাথে সাথে গ্রাহকের পেইড টাকা মাইনাস হয়ে বকেয়ায় (Due) যোগ হবে এবং কাস্টমার পুনরায় বিলিং তালিকায় যুক্ত হবে।",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deletePayment(txToDelete) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ডিলিট করুন / Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.paymentToDelete = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("বাতিল / Cancel", color = TextSecondary)
                }
            }
        )
    }

    // 5. Payment Receipt Popup (with WhatsApp Money Receipt sharing)
    val receiptData = viewModel.lastPaymentReceiptData
    if (viewModel.showPaymentReceiptPopup && receiptData != null) {
        PaymentReceiptPopup(
            customer = receiptData.customer,
            amount = receiptData.amount,
            discount = receiptData.discount,
            method = receiptData.method,
            note = receiptData.note,
            ispSettings = ispSettings,
            onDismiss = {
                viewModel.showPaymentReceiptPopup = false
                viewModel.lastPaymentReceiptData = null
            }
        )
    }
}
