# INTERNET BILLING MANAGER (IBM)

An ultra-modern, professional Android ISP Management & Billing application developed with **Kotlin**, **Jetpack Compose (Material 3)**, and **Room Database**.

- **App Name**: INTERNET BILLING MANAGER
- **Launcher Short Name**: `IBM`
- **Package Name**: `internet.billing.manager`
- **UI Design**: Pixel-accurate dark obsidian glassmorphism theme (`#090E1A`, `#131D31`, `#00E5FF`, `#00E676`, `#7C4DFF`, `#FF9100`, `#FF5252`, `৳` Currency)

---

## 📱 Implemented Screens & Features

### 1. Dashboard (`DashboardScreen`)
- **Header**: Dynamic greeting ("Good Morning 👋"), customizable ISP Business Name, current date, notification bell with unread badge.
- **Brand Card**: Wi-Fi status banner with direct shortcut to settings.
- **Quick Actions (Gradient Buttons)**:
  - ➕ **Add Customer** (Cyan gradient)
  - 💳 **Collect Payment** (Emerald Green gradient)
  - 🧾 **Generate Bill** (Neon Purple gradient)
- **Business Overview (2x2 Grid)**:
  - Active Users & Total count
  - Monthly Bill with current month
  - Monthly Collection
  - Today Collection
- **Billing Analytics Card**:
  - Two-tone progress bar (Collected vs. Outstanding Due)
  - Detailed percentage and amount metrics
- **Recent Collections Card**:
  - Live list of recent payment transactions (Customer, Date, Payment method, and `+৳` amount)

---

### 2. Customers Management (`CustomersScreen`)
- **Top Bar**: Screen title and '+' quick add customer button.
- **Real-Time Search**: Search by name, phone number, PPPoE username, or customer code.
- **Status Filter Pills**: `All`, `Active`, `Suspended`, `Inactive` with live item counts.
- **Customer Cards**:
  - Avatar initial with status color
  - Customer name, code (`U001`, `U012`...)
  - PPPoE connection detail (`username/fiber`)
  - Speed plan & monthly rate (`10 Mbps • ৳600/mo`)
  - Real-time due indicator (`Due: ৳600` or `Due: ৳0`)
  - **Direct One-Tap Actions**:
    - 📞 **Call**: Launches native phone dialer with customer number
    - 💬 **SMS**: Opens native messaging app with pre-filled bill reminder
    - 🟢 **WhatsApp**: Directly opens WhatsApp chat with Bangladesh/international phone number format
    - ✏️ **Edit**: Edit customer details, speed, status, or bill
    - 🗑️ **Delete**: Deletes customer with confirmation prompt

---

### 3. Billing & Collection (`BillingScreen`)
- **Month Navigation**: `< Month Year >` selector for browsing historical or current billing cycles.
- **Financial Metric Cards (4 Glow Cards)**:
  - Total Bill (`৳...`)
  - Total Collected (`৳...`)
  - Previous Due (`৳...`)
  - Total Due (`৳...`)
- **Secondary KPIs**:
  - Bandwidth Bill (Upstream cost)
  - Monthly Profit (Total collected - Bandwidth - Expenses)
  - Discounts
- **Navigation Shortcuts**:
  - **Daily Bill Entry** (Daily collection logs)
  - **Expense Management** (Office, maintenance, and fiber costs)
- **Quick Record Payment Bar**: Fast payment collection dialog trigger.

---

### 4. Daily Bill Entry (`DailyBillEntryScreen`)
- **Time Filters**: `All`, `Today`, `Yesterday`, `This Month`.
- **Date Group Header**: Formatted date with entry counter and total daily collection.
- **Bill Entries List**:
  - Customer Avatar, Name, and Code
  - Billed/Collected Amount
  - Status Badges: `PAID` (Green) or `DUE` (Red)

---

### 5. Expense Management (`ExpenseScreen`)
- **Header**: Expense title, subtitle, and `+ Add` button.
- **Sub-Tabs**: `Dashboard`, `Details`, `Analytics`.
- **Expense KPIs**:
  - This Month Outflow (`৳...`)
  - Vouchers Logged
  - Highest Single Expense
  - Average Expense per Transaction
- **Expense Breakdown Donut Chart**:
  - Dynamic canvas donut ring rendering category shares:
    - 🏢 Office Rent (Orange)
    - 🔧 Maintenance (Blue)
    - 📦 Equipment / Fiber (Purple)
    - 🌐 Internet Bandwidth (Green)
    - 📋 Other (Grey)
- **Recent Expenses List**: Shows title, category, date, amount, and delete option.

---

### 6. Settings & Packages (`SettingsScreen`)
- **Settings Menu**:
  - **ISP Business Information**: Configure ISP name (e.g., U-turn Technologies), helpline phone, office address, and upstream bills.
  - **Bulk SMS Template**: Customizable billing SMS with placeholders (`{NAME}`, `{DUE}`, `{MONTH}`, `{COMPANY}`).
  - **Internet Packages**: Add or edit plans (e.g., 3 Mbps, 5 Mbps, 10 Mbps, 15 Mbps).
  - **Advanced Features**: Extended tools and network configurations.
  - **Language & Theme**: English / Bengali ready, Dark AMOLED palette.
  - **Backup & Restore**: Room SQLite database persistence.
  - **About & Security**: Version and database encryption info.
- **Horizontal Packages Carousel**:
  - Color-coded Wi-Fi speed cards with prices (`3 Mbps ৳300/mo`, `5 Mbps ৳400/mo`, `10 Mbps ৳600/mo`, `15 Mbps ৳800/mo`).

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.0.21
- **UI Toolkit**: Jetpack Compose (BOM 2024.10.01), Material 3
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Repository Pattern
- **Local Persistence**: Android Room Database (SQLite) + Kotlin Coroutines & `Flow`
- **Async & Reactive**: Kotlin Coroutines (`Dispatchers.IO`), `StateFlow`, `SharingStarted.WhileSubscribed`
- **Gradle**: Gradle 8.10.2 + AGP 8.7.2 + Version Catalog (`libs.versions.toml`)
- **Compile SDK**: Android 35, Min SDK: 26
