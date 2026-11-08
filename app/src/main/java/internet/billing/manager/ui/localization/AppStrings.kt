package internet.billing.manager.ui.localization

interface AppStrings {
    // Navigation
    val navDashboard: String
    val navCustomers: String
    val navBilling: String
    val navCollection: String
    val navMore: String

    // Dashboard
    val dashboardTitle: String
    val quickActions: String
    val addCustomer: String
    val collectPayment: String
    val generateBill: String
    val businessOverview: String
    val activeUsers: String
    val monthlyBill: String
    val monthlyCollection: String
    val todayCollection: String
    val billingAnalytics: String
    val monthlyCollectionOverview: String
    val totalGenerated: String
    val collected: String
    val outstanding: String
    val recentPayments: String
    val viewAll: String
    val noPaymentsRecorded: String
    val totalLabel: String
    val statusOperational: String
    val statusMaintenance: String
    val statusDegraded: String

    // Customers Screen
    val customersTitle: String
    val customersSubtitle: String
    val searchCustomerHint: String
    val filterAll: String
    val filterActive: String
    val filterSuspended: String
    val filterInactive: String
    val addCustomerBtn: String
    val noCustomersFound: String
    val monthlyLabel: String
    val dueLabel: String
    val packageLabel: String
    val phoneLabel: String
    val collectBtn: String
    val editBtn: String
    val deleteBtn: String
    val confirmDeleteCustomer: String

    // Customer Dialogs
    val addCustomerTitle: String
    val editCustomerTitle: String
    val fullName: String
    val phoneNumber: String
    val customerCode: String
    val pppoeUsername: String
    val staticIp: String
    val selectPackage: String
    val monthlyFee: String
    val address: String
    val notes: String
    val connectionStatus: String
    val statusActive: String
    val statusSuspended: String
    val statusInactive: String
    val previousDueTitle: String
    val addDueMonth: String
    val monthLabel: String
    val yearLabel: String
    val dueAmount: String
    val saveCustomer: String
    val cancel: String

    // Billing Screen
    val billingTitle: String
    val generateMonthlyBills: String
    val selectMonthYear: String
    val totalBillAmount: String
    val totalPaidAmount: String
    val totalDueAmount: String
    val searchBillHint: String
    val statusPaid: String
    val statusUnpaid: String
    val statusPartial: String
    val dueText: String
    val billGeneratedSuccess: String
    val noBillsFound: String

    // Collection & Accounts Screen
    val collectionTitle: String
    val dailyBillEntry: String
    val expenses: String
    val todayCollectedCard: String
    val todayExpenseCard: String
    val netBalanceCard: String
    val bandwidthBillTitle: String
    val monthlyProfitTitle: String
    val bandwidthBillSubtitle: String
    val monthlyProfitSubtitle: String
    val paymentHistory: String
    val noHistoryFound: String

    // Daily Bill Entry Screen
    val recordCollectionTitle: String
    val selectCustomer: String
    val paymentAmount: String
    val paymentMethod: String
    val noteRemarks: String
    val methodCash: String
    val methodBkash: String
    val methodNagad: String
    val methodRocket: String
    val methodBank: String
    val savePayment: String

    // Expense Screen
    val expenseTitle: String
    val addExpense: String
    val expenseName: String
    val expenseAmount: String
    val expenseCategory: String
    val catBandwidth: String
    val catFiber: String
    val catEquipment: String
    val catRent: String
    val catElectricity: String
    val catSalary: String
    val catSnacks: String
    val catOther: String
    val saveExpense: String
    val totalExpenseMonth: String

    // Settings Screen
    val settingsTitle: String
    val settingsSubtitle: String
    val ispBusinessInfo: String
    val internetPackages: String
    val advancedFeatures: String
    val advancedFeaturesSubtitle: String
    val language: String
    val theme: String
    val backupRestore: String
    val appUpdate: String
    val about: String
    val security: String
    val tapToManage: String
    val packagesCountSuffix: String

    // ISP Business Info Dialog
    val ispInfoTitle: String
    val companyLogo: String
    val changeLogo: String
    val removeLogo: String
    val ispNameLabel: String
    val supportHotline: String
    val companyEmail: String
    val officeAddress: String
    val currencySymbol: String
    val networkStatusLabel: String
    val saveBusinessInfo: String

    // Advanced Features & Auto SMS
    val autoSmsTitle: String
    val autoSmsSubtitle: String
    val masterAutoSms: String
    val masterAutoSmsDesc: String
    val eventAutomation: String
    val autoSmsOnBill: String
    val autoSmsOnBillDesc: String
    val autoSmsOnPayment: String
    val autoSmsOnPaymentDesc: String
    val antiSpamInterval: String
    val antiSpamIntervalDesc: String
    val smsTemplates: String
    val bulkSmsSend: String
    val selectTemplate: String
    val targetAudience: String
    val targetAll: String
    val targetActiveOnly: String
    val targetDueOnly: String
    val messagePreview: String
    val sendSmsBtn: String
    val sendingProgress: String
    val cancelSms: String
    val done: String

    // Packages Dialog & Management
    val packagesDialogTitle: String
    val addPackage: String
    val editPackage: String
    val packageName: String
    val speedMbps: String
    val monthlyPrice: String
    val packageDescription: String
    val accentColor: String
    val deletePackage: String
    val savePackage: String
    val noPackagesConfigured: String

    // Common Dialog Strings
    val confirmAndCollect: String
    val generateNow: String
    val estimatedTotalBill: String
    val activeCustomersToBill: String
    val usersCountSuffix: String
    val close: String

    // Language Dialog
    val selectLanguageTitle: String
    val selectLanguageSubtitle: String
    val englishName: String
    val banglaName: String
    val confirmBtn: String

    // App Update
    val appUpdateTitle: String
    val checkingForUpdate: String
    val updateAvailable: String
    val appUpToDate: String
    val currentVersion: String
    val latestVersion: String
    val releaseNotes: String
    val downloadUpdate: String
    val downloading: String
    val downloadComplete: String
    val installNow: String
    val checkAgain: String
    val updateError: String
    val updateSizeLabel: String
    val releaseDateLabel: String
    val allowUnknownSourcesPrompt: String
    val openInBrowser: String
    val pressAgainToExit: String
}

object EnglishStrings : AppStrings {
    // Navigation
    override val navDashboard = "Dashboard"
    override val navCustomers = "Customers"
    override val navBilling = "Billing"
    override val navCollection = "Collection"
    override val navMore = "Settings"

    // Dashboard
    override val dashboardTitle = "Dashboard"
    override val quickActions = "Quick Actions"
    override val addCustomer = "Add\nCustomer"
    override val collectPayment = "Collect\nPayment"
    override val generateBill = "Generate\nBill"
    override val businessOverview = "Business Overview"
    override val activeUsers = "Active Users"
    override val monthlyBill = "Monthly Bill"
    override val monthlyCollection = "Monthly Collection"
    override val todayCollection = "Today Collection"
    override val billingAnalytics = "Billing Analytics"
    override val monthlyCollectionOverview = "Monthly Collection Overview"
    override val totalGenerated = "Total Generated"
    override val collected = "Collected"
    override val outstanding = "Outstanding"
    override val recentPayments = "Recent Payments"
    override val viewAll = "View All"
    override val noPaymentsRecorded = "No payments recorded yet"
    override val totalLabel = "Total"
    override val statusOperational = "Operational"
    override val statusMaintenance = "Maintenance"
    override val statusDegraded = "Degraded"

    // Customers Screen
    override val customersTitle = "Customers"
    override val customersSubtitle = "Manage your internet subscribers"
    override val searchCustomerHint = "Search name, phone, code..."
    override val filterAll = "All"
    override val filterActive = "Active"
    override val filterSuspended = "Suspended"
    override val filterInactive = "Inactive"
    override val addCustomerBtn = "Add Customer"
    override val noCustomersFound = "No customers found"
    override val monthlyLabel = "Monthly"
    override val dueLabel = "Due"
    override val packageLabel = "Package"
    override val phoneLabel = "Phone"
    override val collectBtn = "Collect"
    override val editBtn = "Edit"
    override val deleteBtn = "Delete"
    override val confirmDeleteCustomer = "Are you sure you want to delete this customer?"

    // Customer Dialogs
    override val addCustomerTitle = "Add New Customer"
    override val editCustomerTitle = "Edit Customer"
    override val fullName = "Full Name"
    override val phoneNumber = "Phone Number"
    override val customerCode = "Customer Code"
    override val pppoeUsername = "PPPoE Username"
    override val staticIp = "Static IP (Optional)"
    override val selectPackage = "Select Internet Package"
    override val monthlyFee = "Monthly Fee (৳)"
    override val address = "Address / Location"
    override val notes = "Notes (Optional)"
    override val connectionStatus = "Connection Status"
    override val statusActive = "Active"
    override val statusSuspended = "Suspended"
    override val statusInactive = "Inactive"
    override val previousDueTitle = "Previous Due / Opening Due"
    override val addDueMonth = "+ Add Due Month"
    override val monthLabel = "Month"
    override val yearLabel = "Year"
    override val dueAmount = "Due Amount"
    override val saveCustomer = "Save Customer"
    override val cancel = "Cancel"

    // Billing Screen
    override val billingTitle = "Monthly Billing"
    override val generateMonthlyBills = "Generate Monthly Bills"
    override val selectMonthYear = "Select Month & Year"
    override val totalBillAmount = "Total Bill"
    override val totalPaidAmount = "Total Paid"
    override val totalDueAmount = "Total Due"
    override val searchBillHint = "Search customer bill..."
    override val statusPaid = "Paid"
    override val statusUnpaid = "Unpaid"
    override val statusPartial = "Partial"
    override val dueText = "Due"
    override val billGeneratedSuccess = "Monthly bills generated for all active subscribers"
    override val noBillsFound = "No bills found"

    // Collection & Accounts Screen
    override val collectionTitle = "Collection & Accounts"
    override val dailyBillEntry = "Daily Bill Entry"
    override val expenses = "Expenses"
    override val todayCollectedCard = "Today's Collection"
    override val todayExpenseCard = "Today's Expense"
    override val netBalanceCard = "Net Balance"
    override val bandwidthBillTitle = "Bandwidth Bill"
    override val monthlyProfitTitle = "Monthly Profit"
    override val bandwidthBillSubtitle = "Upstream cost"
    override val monthlyProfitSubtitle = "Total Paid - Expenses"
    override val paymentHistory = "Payment History"
    override val noHistoryFound = "No collection history found"

    // Daily Bill Entry Screen
    override val recordCollectionTitle = "Record Bill Collection"
    override val selectCustomer = "Select Customer"
    override val paymentAmount = "Amount (৳)"
    override val paymentMethod = "Payment Method"
    override val noteRemarks = "Note / Remarks"
    override val methodCash = "Cash"
    override val methodBkash = "bKash"
    override val methodNagad = "Nagad"
    override val methodRocket = "Rocket"
    override val methodBank = "Bank"
    override val savePayment = "Save Payment"

    // Expense Screen
    override val expenseTitle = "Expense Management"
    override val addExpense = "Add Expense"
    override val expenseName = "Expense Title / Reason"
    override val expenseAmount = "Amount (৳)"
    override val expenseCategory = "Category"
    override val catBandwidth = "Bandwidth Cost"
    override val catFiber = "Fiber / Cable"
    override val catEquipment = "Equipment / Router"
    override val catRent = "Office Rent"
    override val catElectricity = "Electricity Bill"
    override val catSalary = "Staff Salary"
    override val catSnacks = "Tea & Snacks"
    override val catOther = "Other Expenses"
    override val saveExpense = "Save Expense"
    override val totalExpenseMonth = "Total Expense This Month"

    // Settings Screen
    override val settingsTitle = "Settings"
    override val settingsSubtitle = "App settings and preferences"
    override val ispBusinessInfo = "ISP Business Information"
    override val internetPackages = "Internet Packages"
    override val advancedFeatures = "Advanced Features"
    override val advancedFeaturesSubtitle = "Auto SMS System, Tools & Automation"
    override val language = "Language"
    override val theme = "Theme"
    override val backupRestore = "Backup & Restore"
    override val appUpdate = "App Update"
    override val about = "About"
    override val security = "Security"
    override val tapToManage = "Tap to manage"
    override val packagesCountSuffix = "packages • Tap to manage"

    // ISP Business Info Dialog
    override val ispInfoTitle = "ISP Business Information"
    override val companyLogo = "Company Logo"
    override val changeLogo = "Change Logo"
    override val removeLogo = "Remove Logo"
    override val ispNameLabel = "ISP Name / Brand Name"
    override val supportHotline = "Support Hotline"
    override val companyEmail = "Company Email"
    override val officeAddress = "Office Address"
    override val currencySymbol = "Currency Symbol"
    override val networkStatusLabel = "Network Status"
    override val saveBusinessInfo = "Save Business Info"

    // Advanced Features & Auto SMS
    override val autoSmsTitle = "Auto SMS System (SIM)"
    override val autoSmsSubtitle = "SIM rate-limited dispatch & billing templates"
    override val masterAutoSms = "Master Auto SMS (SIM Dispatch)"
    override val masterAutoSmsDesc = "Enable or disable all automatic SMS notifications with one tap"
    override val eventAutomation = "Event Automation Triggers"
    override val autoSmsOnBill = "Auto SMS on Monthly Bill Generation"
    override val autoSmsOnBillDesc = "Automatically send bill notice when monthly bills are generated"
    override val autoSmsOnPayment = "Auto SMS on Payment Collection"
    override val autoSmsOnPaymentDesc = "Automatically send payment receipt SMS when payment is collected"
    override val antiSpamInterval = "SIM Anti-Spam Interval (Rate-Limit)"
    override val antiSpamIntervalDesc = "Safe pause between each SMS to protect your SIM from carrier blocks"
    override val smsTemplates = "SMS Template Library"
    override val bulkSmsSend = "Send Bulk SMS"
    override val selectTemplate = "Select Template"
    override val targetAudience = "Target Audience"
    override val targetAll = "All Customers"
    override val targetActiveOnly = "Active Customers Only"
    override val targetDueOnly = "Customers with Dues Only"
    override val messagePreview = "Message Preview"
    override val sendSmsBtn = "Send SMS"
    override val sendingProgress = "Sending SMS..."
    override val cancelSms = "Cancel"
    override val done = "Done"

    // Packages Dialog & Management
    override val packagesDialogTitle = "Internet Packages"
    override val addPackage = "Add Package"
    override val editPackage = "Edit Package"
    override val packageName = "Package Name"
    override val speedMbps = "Speed (Mbps)"
    override val monthlyPrice = "Monthly Price"
    override val packageDescription = "Description"
    override val accentColor = "Color Badge"
    override val deletePackage = "Delete Package"
    override val savePackage = "Save Package"
    override val noPackagesConfigured = "No packages configured yet"

    // Common Dialog Strings
    override val confirmAndCollect = "Confirm & Collect"
    override val generateNow = "Generate Now"
    override val estimatedTotalBill = "Estimated Total Bill"
    override val activeCustomersToBill = "Active Customers to Bill"
    override val usersCountSuffix = "users"
    override val close = "Close"

    // Language Dialog
    override val selectLanguageTitle = "Select Language / ভাষা নির্বাচন করুন"
    override val selectLanguageSubtitle = "Choose your preferred app display language"
    override val englishName = "English"
    override val banglaName = "বাংলা (Bangla)"
    override val confirmBtn = "Confirm"

    // App Update
    override val appUpdateTitle = "App Update"
    override val checkingForUpdate = "Checking GitHub for updates..."
    override val updateAvailable = "New Update Available!"
    override val appUpToDate = "App is up to date"
    override val currentVersion = "Current Version"
    override val latestVersion = "Latest Version"
    override val releaseNotes = "Release Notes"
    override val downloadUpdate = "Download Update"
    override val downloading = "Downloading Update..."
    override val downloadComplete = "Download Complete"
    override val installNow = "Install Update Now"
    override val checkAgain = "Check Again"
    override val updateError = "Failed to check update. Please check internet connection."
    override val updateSizeLabel = "Size"
    override val releaseDateLabel = "Release Date"
    override val allowUnknownSourcesPrompt = "Please enable 'Install unknown apps' permission to install the update."
    override val openInBrowser = "Open in Browser"
    override val pressAgainToExit = "Press back again to exit"
}

object BanglaStrings : AppStrings {
    // Navigation
    override val navDashboard = "ড্যাশবোর্ড"
    override val navCustomers = "গ্রাহক"
    override val navBilling = "বিলিং"
    override val navCollection = "কালেকশন"
    override val navMore = "সেটিংস"

    // Dashboard
    override val dashboardTitle = "ড্যাশবোর্ড"
    override val quickActions = "কুইক অ্যাকশন"
    override val addCustomer = "গ্রাহক\nযুক্ত করুন"
    override val collectPayment = "বিল\nআদায়"
    override val generateBill = "বিল\nজেনারেট"
    override val businessOverview = "সারসংক্ষেপ"
    override val activeUsers = "সক্রিয় গ্রাহক"
    override val monthlyBill = "মাসিক বিল"
    override val monthlyCollection = "মাসিক আদায়"
    override val todayCollection = "আজকের আদায়"
    override val billingAnalytics = "বিলিং বিশ্লেষণ"
    override val monthlyCollectionOverview = "মাসিক আদায়ের চিত্র"
    override val totalGenerated = "মোট বিল"
    override val collected = "আদায় হয়েছে"
    override val outstanding = "বকেয়া রয়েছে"
    override val recentPayments = "সাম্প্রতিক আদায়"
    override val viewAll = "সব দেখুন"
    override val noPaymentsRecorded = "এখনও কোনো পেমেন্ট রেকর্ড নেই"
    override val totalLabel = "মোট"
    override val statusOperational = "স্বাভাবিক"
    override val statusMaintenance = "রক্ষণাবেক্ষণ"
    override val statusDegraded = "ধীরগতি / সমস্যা"

    // Customers Screen
    override val customersTitle = "গ্রাহক তালিকা"
    override val customersSubtitle = "আপনার সকল ইন্টারনেট গ্রাহকদের পরিচালনা করুন"
    override val searchCustomerHint = "নাম, মোবাইল বা কোড খুঁজুন..."
    override val filterAll = "সকল"
    override val filterActive = "সক্রিয়"
    override val filterSuspended = "স্থগিত"
    override val filterInactive = "নিষ্ক্রিয়"
    override val addCustomerBtn = "নতুন গ্রাহক"
    override val noCustomersFound = "কোনো গ্রাহক পাওয়া যায়নি"
    override val monthlyLabel = "মাসিক"
    override val dueLabel = "বকেয়া"
    override val packageLabel = "প্যাকেজ"
    override val phoneLabel = "মোবাইল"
    override val collectBtn = "আদায়"
    override val editBtn = "এডিট"
    override val deleteBtn = "মুছুন"
    override val confirmDeleteCustomer = "আপনি কি নিশ্চিত এই গ্রাহককে মুছে ফেলতে চান?"

    // Customer Dialogs
    override val addCustomerTitle = "নতুন গ্রাহক যুক্ত করুন"
    override val editCustomerTitle = "গ্রাহকের তথ্য পরিবর্তন"
    override val fullName = "গ্রাহকের পুরো নাম"
    override val phoneNumber = "মোবাইল নম্বর"
    override val customerCode = "গ্রাহক কোড / আইডি"
    override val pppoeUsername = "পিপিপিওই ইউজারনেম"
    override val staticIp = "স্ট্যাটিক আইপি (ঐচ্ছিক)"
    override val selectPackage = "ইন্টারনেট প্যাকেজ নির্বাচন করুন"
    override val monthlyFee = "মাসিক ফি (৳)"
    override val address = "ঠিকানা / এলাকা"
    override val notes = "নোট / মন্তব্য (ঐচ্ছিক)"
    override val connectionStatus = "সংযোগের অবস্থা"
    override val statusActive = "সক্রিয় (Active)"
    override val statusSuspended = "স্থগিত (Suspended)"
    override val statusInactive = "নিষ্ক্রিয় (Inactive)"
    override val previousDueTitle = "পূর্বের বকেয়া বিল / ওপেনিং ডিউ"
    override val addDueMonth = "+ বকেয়া মাস যুক্ত করুন"
    override val monthLabel = "মাস"
    override val yearLabel = "বছর"
    override val dueAmount = "বকেয়া টাকা"
    override val saveCustomer = "গ্রাহক সংরক্ষণ করুন"
    override val cancel = "বাতিল"

    // Billing Screen
    override val billingTitle = "মাসিক বিলিং"
    override val generateMonthlyBills = "মাসিক বিল জেনারেট করুন"
    override val selectMonthYear = "মাস ও বছর নির্বাচন"
    override val totalBillAmount = "মোট বিল"
    override val totalPaidAmount = "মোট আদায়"
    override val totalDueAmount = "মোট বকেয়া"
    override val searchBillHint = "গ্রাহকের বিল খুঁজুন..."
    override val statusPaid = "পরিশোধিত"
    override val statusUnpaid = "অপরিশোধিত"
    override val statusPartial = "আংশিক"
    override val dueText = "বকেয়া"
    override val billGeneratedSuccess = "সকল সক্রিয় গ্রাহকের মাসিক বিল জেনারেট হয়েছে"
    override val noBillsFound = "কোনো বিল পাওয়া যায়নি"

    // Collection & Accounts Screen
    override val collectionTitle = "আদায় ও হিসাব"
    override val dailyBillEntry = "দৈনিক বিল এন্ট্রি"
    override val expenses = "খরচের হিসাব"
    override val todayCollectedCard = "আজকের আদায়"
    override val todayExpenseCard = "আজকের খরচ"
    override val netBalanceCard = "অবশিষ্ট ক্যাশ"
    override val bandwidthBillTitle = "ব্যান্ডউইথ বিল"
    override val monthlyProfitTitle = "মাসিক লাভ"
    override val bandwidthBillSubtitle = "আপস্ট্রিম খরচ"
    override val monthlyProfitSubtitle = "মোট আদায় - মোট খরচ"
    override val paymentHistory = "আদায়ের ইতিহাস"
    override val noHistoryFound = "কোনো আদায়ের ইতিহাস পাওয়া যায়নি"

    // Daily Bill Entry Screen
    override val recordCollectionTitle = "বিল আদায়ের এন্ট্রি"
    override val selectCustomer = "গ্রাহক নির্বাচন করুন"
    override val paymentAmount = "টাকার পরিমাণ (৳)"
    override val paymentMethod = "পেমেন্ট মাধ্যম"
    override val noteRemarks = "নোট / বিবরণ"
    override val methodCash = "ক্যাশ (নগদ)"
    override val methodBkash = "বিকাশ"
    override val methodNagad = "নগদ (Nagad)"
    override val methodRocket = "রকেট"
    override val methodBank = "ব্যাংক"
    override val savePayment = "পেমেন্ট জমা করুন"

    // Expense Screen
    override val expenseTitle = "খরচের হিসাব"
    override val addExpense = "নতুন খরচ"
    override val expenseName = "খরচের বিবরণ"
    override val expenseAmount = "টাকার পরিমাণ (৳)"
    override val expenseCategory = "ক্যাটাগরি"
    override val catBandwidth = "ব্যান্ডউইথ খরচ"
    override val catFiber = "ফাইবার / কেবল"
    override val catEquipment = "রাউটার / সুইচ / ইকুইপমেন্ট"
    override val catRent = "অফিস ভাড়া"
    override val catElectricity = "বিদ্যুৎ বিল"
    override val catSalary = "স্টাফদের বেতন"
    override val catSnacks = "চা ও নাস্তা"
    override val catOther = "অন্যান্য খরচ"
    override val saveExpense = "খরচ সংরক্ষণ করুন"
    override val totalExpenseMonth = "এই মাসের মোট খরচ"

    // Settings Screen
    override val settingsTitle = "সেটিংস"
    override val settingsSubtitle = "অ্যাপ সেটিংস ও কনফিগারেশন"
    override val ispBusinessInfo = "আইএসপি ব্যবসার তথ্য"
    override val internetPackages = "ইন্টারনেট প্যাকেজ"
    override val advancedFeatures = "অ্যাডভান্সড ফিচার"
    override val advancedFeaturesSubtitle = "অটো এসএমএস সিস্টেম ও অটোমেশন"
    override val language = "ভাষা / Language"
    override val theme = "থিম"
    override val backupRestore = "ব্যাকআপ ও রিস্টোর"
    override val appUpdate = "অ্যাপ আপডেট"
    override val about = "অ্যাপ পরিচিতি"
    override val security = "নিরাপত্তা"
    override val tapToManage = "ম্যানেজ করতে ট্যাপ করুন"
    override val packagesCountSuffix = "টি প্যাকেজ • ম্যানেজ করতে ট্যাপ করুন"

    // ISP Business Info Dialog
    override val ispInfoTitle = "আইএসপি ব্যবসার তথ্য"
    override val companyLogo = "কোম্পানির লোগো"
    override val changeLogo = "লোগো পরিবর্তন"
    override val removeLogo = "মুছুন"
    override val ispNameLabel = "আইএসপি বা ব্র্যান্ডের নাম"
    override val supportHotline = "সাপোর্ট হটলাইন"
    override val companyEmail = "কোম্পানির ইমেইল"
    override val officeAddress = "অফিস ঠিকানা"
    override val currencySymbol = "মুদ্রা প্রতীক"
    override val networkStatusLabel = "নেটওয়ার্কের অবস্থা"
    override val saveBusinessInfo = "তথ্য সংরক্ষণ করুন"

    // Advanced Features & Auto SMS
    override val autoSmsTitle = "অটো এসএমএস সিস্টেম (সিম)"
    override val autoSmsSubtitle = "সিম রেট-লিমিটেড ও ইন্টারনেট বিলিং টেমপ্লেট"
    override val masterAutoSms = "মাস্টার অটো এসএমএস (সিম)"
    override val masterAutoSmsDesc = "এক ক্লিকে সম্পূর্ণ অটো এসএমএস সুবিধা চালু বা বন্ধ রাখুন"
    override val eventAutomation = "অটোমেশন ইভেন্ট"
    override val autoSmsOnBill = "মাসিক বিল তৈরিতে অটো এসএমএস"
    override val autoSmsOnBillDesc = "বিল জেনারেট করার সাথে সাথে গ্রাহককে স্বয়ংক্রিয় এসএমএস পাঠানো হবে"
    override val autoSmsOnPayment = "বিল আদায়ে অটো মানি রিসিট এসএমএস"
    override val autoSmsOnPaymentDesc = "বিল কালেকশন করার সাথে সাথে গ্রাহককে পেমেন্ট রিসিট এসএমএস পাঠানো হবে"
    override val antiSpamInterval = "সিম স্প্যাম-রক্ষা বিরতি (রেট লিমিট)"
    override val antiSpamIntervalDesc = "সিম কার্ড স্প্যাম ব্লকিং থেকে সুরক্ষিত রাখতে এসএমএসের মাঝে নিরাপদ বিরতি"
    override val smsTemplates = "এসএমএস টেমপ্লেট লাইব্রেরি"
    override val bulkSmsSend = "একসাথে এসএমএস পাঠান"
    override val selectTemplate = "টেমপ্লেট নির্বাচন করুন"
    override val targetAudience = "গ্রাহক গ্রুপ"
    override val targetAll = "সকল গ্রাহক"
    override val targetActiveOnly = "শুধু সক্রিয় গ্রাহক"
    override val targetDueOnly = "শুধু বকেয়া গ্রাহক"
    override val messagePreview = "মেসেজ প্রিভিউ"
    override val sendSmsBtn = "এসএমএস পাঠান"
    override val sendingProgress = "এসএমএস পাঠানো হচ্ছে..."
    override val cancelSms = "বাতিল"
    override val done = "সম্পন্ন"

    // Packages Dialog & Management
    override val packagesDialogTitle = "ইন্টারনেট প্যাকেজসমূহ"
    override val addPackage = "প্যাকেজ যোগ করুন"
    override val editPackage = "প্যাকেজ এডিট করুন"
    override val packageName = "প্যাকেজের নাম"
    override val speedMbps = "স্পিড (Mbps)"
    override val monthlyPrice = "মাসিক মূল্য"
    override val packageDescription = "বিবরণ"
    override val accentColor = "কালার ব্যাজ"
    override val deletePackage = "প্যাকেজ মুছুন"
    override val savePackage = "প্যাকেজ সংরক্ষণ করুন"
    override val noPackagesConfigured = "কোনো প্যাকেজ তৈরি করা হয়নি"

    // Common Dialog Strings
    override val confirmAndCollect = "নিশ্চিত করুন ও গ্রহণ করুন"
    override val generateNow = "এখনই তৈরি করুন"
    override val estimatedTotalBill = "আনুমানিক মোট বিল"
    override val activeCustomersToBill = "সক্রিয় গ্রাহক সংখ্যা"
    override val usersCountSuffix = "জন গ্রাহক"
    override val close = "বন্ধ করুন"

    // Language Dialog
    override val selectLanguageTitle = "ভাষা নির্বাচন করুন / Select Language"
    override val selectLanguageSubtitle = "আপনার পছন্দের ভাষা বেছে নিন"
    override val englishName = "English"
    override val banglaName = "বাংলা (Bangla)"
    override val confirmBtn = "নিশ্চিত করুন"

    // App Update
    override val appUpdateTitle = "অ্যাপ আপডেট"
    override val checkingForUpdate = "গিটহাব থেকে নতুন আপডেট চেক করা হচ্ছে..."
    override val updateAvailable = "নতুন আপডেট উপলব্ধ!"
    override val appUpToDate = "আপনার অ্যাপটি সর্বশেষ সংস্করণে আছে"
    override val currentVersion = "বর্তমান ভার্সন"
    override val latestVersion = "সর্বশেষ ভার্সন"
    override val releaseNotes = "আপডেট বিবরণী"
    override val downloadUpdate = "আপডেট ডাউনলোড"
    override val downloading = "আপডেট ডাউনলোড হচ্ছে..."
    override val downloadComplete = "ডাউনলোড সম্পন্ন হয়েছে"
    override val installNow = "এখনই ইনস্টল করুন"
    override val checkAgain = "পুনরায় চেক করুন"
    override val updateError = "আপডেট চেক করতে ব্যর্থ হয়েছে। ইন্টারনেট সংযোগ পরীক্ষা করুন।"
    override val updateSizeLabel = "সাইজ"
    override val releaseDateLabel = "রিলিজের তারিখ"
    override val allowUnknownSourcesPrompt = "অ্যাপটি ইনস্টল করার জন্য অজানা অ্যাপ ইনস্টলের অনুমতি দিন।"
    override val openInBrowser = "ব্রাউজারে খুলুন"
    override val pressAgainToExit = "অ্যাপ থেকে বের হতে আবার ব্যাক প্রেস করুন"
}
