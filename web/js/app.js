// ==========================================================================
// INTERNET BILLING MANAGER 2 (IBM 2) - FRONTEND SPA CONTROLLER
// ==========================================================================

const API_BASE = '/api';

const state = {
    activeTab: 'dashboard',
    stats: null,
    customers: [],
    filteredCustomers: [],
    packages: [],
    bills: [],
    payments: [],
    expenses: [],
    settings: {
        company_name: 'Internet Service Provider',
        tagline: 'Fast • Stable • Connected',
        phone: '01700000000',
        address: 'Dhaka, Bangladesh',
        currency_symbol: '৳',
        network_status: 'Operational'
    },
    currentMonthYear: getMonthYearString(new Date()),
    selectedCustomerForPayment: null,
    activeReceiptData: null
};

// Utilities
function getMonthYearString(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    return `${y}-${m}`;
}

function formatMonthYearDisplay(monthYearStr) {
    if (!monthYearStr) return '';
    const [y, m] = monthYearStr.split('-');
    const months = [
        'জানুয়ারি', 'ফেব্রুয়ারি', 'মার্চ', 'এপ্রিল', 'মে', 'জুন',
        'জুলাই', 'আগস্ট', 'সেপ্টেম্বর', 'অক্টোবর', 'নভেম্বর', 'ডিসেম্বর'
    ];
    const idx = parseInt(m, 10) - 1;
    return `${months[idx] || m} ${y}`;
}

function formatCurrency(amount) {
    const val = parseFloat(amount) || 0;
    return `${state.settings.currency_symbol || '৳'}${Math.round(val).toLocaleString()}`;
}

function formatDate(millis) {
    if (!millis) return '-';
    const d = new Date(parseInt(millis, 10));
    return d.toLocaleDateString('bn-BD', { day: 'numeric', month: 'short', year: 'numeric' });
}

// Number to Words Converter
function numberToWords(amount) {
    const n = Math.round(Math.abs(amount));
    if (n === 0) return 'Zero Taka Only';

    const units = ['', 'One', 'Two', 'Three', 'Four', 'Five', 'Six', 'Seven', 'Eight', 'Nine',
                   'Ten', 'Eleven', 'Twelve', 'Thirteen', 'Fourteen', 'Fifteen', 'Sixteen',
                   'Seventeen', 'Eighteen', 'Nineteen'];
    const tens = ['', '', 'Twenty', 'Thirty', 'Forty', 'Fifty', 'Sixty', 'Seventy', 'Eighty', 'Ninety'];

    function convertSection(num) {
        let str = '';
        if (num >= 100) {
            str += units[Math.floor(num / 100)] + ' Hundred ';
            num %= 100;
        }
        if (num > 0) {
            if (num < 20) {
                str += units[num];
            } else {
                str += tens[Math.floor(num / 10)];
                if (num % 10 > 0) str += ' ' + units[num % 10];
            }
        }
        return str.trim();
    }

    let result = '';
    let rem = n;

    if (rem >= 10000000) {
        result += convertSection(Math.floor(rem / 10000000)) + ' Crore ';
        rem %= 10000000;
    }
    if (rem >= 100000) {
        result += convertSection(Math.floor(rem / 100000)) + ' Lakh ';
        rem %= 100000;
    }
    if (rem >= 1000) {
        result += convertSection(Math.floor(rem / 1000)) + ' Thousand ';
        rem %= 1000;
    }
    if (rem > 0) {
        result += convertSection(rem);
    }

    return `${result.trim()} Taka Only`;
}

// API Client Helper
async function apiCall(endpoint, options = {}) {
    try {
        const res = await fetch(`${API_BASE}${endpoint}`, {
            headers: {
                'Content-Type': 'application/json',
                ...(options.headers || {})
            },
            ...options
        });
        const data = await res.json();
        if (!res.ok) {
            throw new Error(data.error || 'Server error occurred');
        }
        return data;
    } catch (err) {
        console.error(`API Error on ${endpoint}:`, err);
        throw err;
    }
}

// App Initialization
document.addEventListener('DOMContentLoaded', async () => {
    setupNavigation();
    setupEventListeners();
    updateDateDisplay();

    // Initial Data Fetch
    try {
        await Promise.all([
            loadSettings(),
            loadDashboard(),
            loadPackages()
        ]);
    } catch (e) {
        console.warn('Initial load handled with local defaults:', e.message);
    }
});

function updateDateDisplay() {
    const now = new Date();
    const options = { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' };
    const dateStr = now.toLocaleDateString('en-US', options);
    const dateEl = document.getElementById('currentDatePill');
    if (dateEl) dateEl.innerText = dateStr;
}

// Navigation Controller
function setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const tab = item.getAttribute('data-tab');
            switchTab(tab);
        });
    });
}

function switchTab(tabName) {
    state.activeTab = tabName;

    // Update Sidebar Active Class
    document.querySelectorAll('.nav-item').forEach(item => {
        if (item.getAttribute('data-tab') === tabName) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    // Update Topbar Title
    const titles = {
        dashboard: 'ড্যাশবোর্ড ওভারভিউ (Dashboard)',
        customers: 'গ্রাহক ব্যবস্থাপনা (Customers Management)',
        billing: 'মাসিক বিল ও কালেকশন (Billing & Invoicing)',
        collections: 'দৈনিক বিল আদায় হিস্টোরি (Payment Records)',
        expenses: 'খরচ ব্যবস্থাপনা (Expense Management)',
        packages: 'ইন্টারনেট প্যাকেজ (Internet Packages)',
        mikrotik: 'মাইক্রোটিক রাউটার নিয়ন্ত্রণ (MikroTik RouterOS)',
        settings: 'আইএসপি সেটিংস (ISP Business Settings)'
    };
    const titleEl = document.getElementById('pageTitle');
    if (titleEl) titleEl.innerText = titles[tabName] || 'Dashboard';

    // Toggle Tab Views
    document.querySelectorAll('.tab-view').forEach(view => {
        if (view.id === `view-${tabName}`) {
            view.style.display = 'block';
        } else {
            view.style.display = 'none';
        }
    });

    // Lazy load tab data
    if (tabName === 'dashboard') loadDashboard();
    else if (tabName === 'customers') loadCustomers();
    else if (tabName === 'billing') loadBilling();
    else if (tabName === 'collections') loadPayments();
    else if (tabName === 'expenses') loadExpenses();
    else if (tabName === 'packages') loadPackages();
    else if (tabName === 'mikrotik') loadMikrotik();
    else if (tabName === 'settings') loadSettings();
}

// 1. DASHBOARD
async function loadDashboard() {
    try {
        const res = await apiCall('/dashboard/stats');
        if (res.success && res.data) {
            state.stats = res.data;
            renderDashboard();
        }
    } catch (err) {
        console.error('Failed to load dashboard:', err);
    }
}

function renderDashboard() {
    if (!state.stats) return;
    const { customers, financials, recentPayments } = state.stats;

    document.getElementById('statActiveCustomers').innerText = customers.active;
    document.getElementById('statTotalCustomers').innerText = `মোট: ${customers.total} জন`;

    document.getElementById('statMonthlyBill').innerText = formatCurrency(financials.billedThisMonth);
    document.getElementById('statMonthlyCollection').innerText = formatCurrency(financials.collectedThisMonth);
    document.getElementById('statTodayCollection').innerText = formatCurrency(financials.collectedToday);
    document.getElementById('statTotalDue').innerText = formatCurrency(customers.totalOutstandingDue);
    document.getElementById('statMonthlyProfit').innerText = formatCurrency(financials.monthlyProfit);

    // Render Recent Payments Table
    const tbody = document.getElementById('recentPaymentsTableBody');
    if (tbody) {
        if (!recentPayments || recentPayments.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color: var(--text-muted); padding: 24px;">এখনো কোনো পেমেন্ট রেকর্ড নেই।</td></tr>`;
        } else {
            tbody.innerHTML = recentPayments.map(p => `
                <tr>
                    <td>
                        <div style="font-weight: 600;">${p.customer_name}</div>
                        <div style="font-size: 11px; color: var(--text-muted);">${p.customer_code}</div>
                    </td>
                    <td><span class="badge badge-paid">${formatCurrency(p.amount)}</span></td>
                    <td><span class="badge" style="background: rgba(0,229,255,0.1); color: var(--accent-cyan);">${p.payment_method}</span></td>
                    <td>${formatDate(p.date_millis)}</td>
                    <td>
                        <button class="btn btn-outline" style="padding: 4px 8px; font-size: 11px;" onclick="openReceiptFromPayment(${JSON.stringify(p).replace(/"/g, '&quot;')})">
                            🧾 রসিদ
                        </button>
                    </td>
                </tr>
            `).join('');
        }
    }
}

// 2. CUSTOMERS
async function loadCustomers() {
    try {
        const res = await apiCall('/customers');
        if (res.success) {
            state.customers = res.data;
            state.filteredCustomers = [...res.data];
            renderCustomers();
        }
    } catch (err) {
        console.error('Failed to load customers:', err);
    }
}

function renderCustomers() {
    const tbody = document.getElementById('customersTableBody');
    if (!tbody) return;

    if (state.filteredCustomers.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-muted); padding: 30px;">কোনো গ্রাহক পাওয়া যায়নি।</td></tr>`;
        return;
    }

    tbody.innerHTML = state.filteredCustomers.map(c => `
        <tr>
            <td>
                <span style="font-weight: 700; color: var(--accent-cyan);">${c.customer_code}</span>
            </td>
            <td>
                <div style="font-weight: 600; font-size: 14px;">${c.name}</div>
                <div style="font-size: 11.5px; color: var(--text-muted);">PPPoE: ${c.pppoe_username} • ${c.connection_type || 'Fiber'}</div>
            </td>
            <td>
                <div>📞 ${c.phone || '-'}</div>
                <div style="font-size: 11px; color: var(--text-muted);">${c.address || ''}</div>
            </td>
            <td>
                <span class="badge" style="background: rgba(41,121,255,0.15); color: var(--accent-blue);">
                    ${c.package_name || `${c.speed_mbps} Mbps`}
                </span>
                <div style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">${formatCurrency(c.monthly_bill)}/মাস</div>
            </td>
            <td>
                <span class="badge ${parseFloat(c.due_amount) > 0 ? 'badge-due' : 'badge-paid'}">
                    বকেয়া: ${formatCurrency(c.due_amount)}
                </span>
            </td>
            <td>
                <span class="badge badge-${c.status ? c.status.toLowerCase() : 'active'}">${c.status || 'Active'}</span>
            </td>
            <td>
                <div style="display: flex; gap: 6px;">
                    <button class="btn btn-green" style="padding: 5px 9px; font-size: 11px;" onclick="openCollectPaymentModal(${c.id})">💳 আদায়</button>
                    <button class="btn btn-outline" style="padding: 5px 9px; font-size: 11px;" onclick="openWhatsAppChat('${c.phone}', '${c.name}', ${c.due_amount})">🟢 চ্যাট</button>
                    <button class="btn btn-outline" style="padding: 5px 9px; font-size: 11px;" onclick="editCustomer(${c.id})">✏️</button>
                    <button class="btn btn-danger" style="padding: 5px 9px; font-size: 11px;" onclick="deleteCustomer(${c.id})">🗑️</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function filterCustomers(query, statusFilter) {
    state.filteredCustomers = state.customers.filter(c => {
        const matchesQuery = !query || 
            (c.name && c.name.toLowerCase().includes(query.toLowerCase())) ||
            (c.customer_code && c.customer_code.toLowerCase().includes(query.toLowerCase())) ||
            (c.pppoe_username && c.pppoe_username.toLowerCase().includes(query.toLowerCase())) ||
            (c.phone && c.phone.includes(query));

        const matchesStatus = !statusFilter || statusFilter === 'All' || c.status === statusFilter;
        return matchesQuery && matchesStatus;
    });
    renderCustomers();
}

// 3. BILLING
async function loadBilling() {
    try {
        const monthYear = state.currentMonthYear;
        const res = await apiCall(`/bills?monthYear=${monthYear}`);
        if (res.success) {
            state.bills = res.data;
            renderBilling();
        }
    } catch (err) {
        console.error('Failed to load billing:', err);
    }
}

function renderBilling() {
    document.getElementById('billingMonthDisplay').innerText = formatMonthYearDisplay(state.currentMonthYear);
    
    // Compute totals for this month
    const totalBilled = state.bills.reduce((acc, b) => acc + parseFloat(b.bill_amount), 0);
    const totalPaid = state.bills.reduce((acc, b) => acc + parseFloat(b.paid_amount), 0);
    const totalDue = state.bills.reduce((acc, b) => acc + parseFloat(b.due_amount), 0);

    document.getElementById('billingTotalBill').innerText = formatCurrency(totalBilled);
    document.getElementById('billingTotalPaid').innerText = formatCurrency(totalPaid);
    document.getElementById('billingTotalDue').innerText = formatCurrency(totalDue);

    const tbody = document.getElementById('billingTableBody');
    if (!tbody) return;

    if (state.bills.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-muted); padding: 30px;">এই মাসের জন্য এখনো বিল তৈরি করা হয়নি। উপরে "মাসিক বিল জেনারেট" বাটনে ক্লিক করুন।</td></tr>`;
        return;
    }

    tbody.innerHTML = state.bills.map(b => `
        <tr>
            <td><strong style="color: var(--accent-cyan);">${b.customer_code}</strong></td>
            <td>${b.customer_name}</td>
            <td>${b.month_year}</td>
            <td>${formatCurrency(b.bill_amount)}</td>
            <td><span class="badge badge-paid">${formatCurrency(b.paid_amount)}</span></td>
            <td><span class="badge ${parseFloat(b.due_amount) > 0 ? 'badge-due' : 'badge-paid'}">${formatCurrency(b.due_amount)}</span></td>
            <td>
                <span class="badge ${b.is_paid ? 'badge-paid' : 'badge-due'}">
                    ${b.is_paid ? 'পরিশোধিত' : 'বকেয়া'}
                </span>
            </td>
        </tr>
    `).join('');
}

function changeBillingMonth(delta) {
    const [y, m] = state.currentMonthYear.split('-').map(Number);
    const d = new Date(y, m - 1 + delta, 1);
    state.currentMonthYear = getMonthYearString(d);
    loadBilling();
}

async function triggerGenerateMonthlyBills() {
    if (!confirm(`${formatMonthYearDisplay(state.currentMonthYear)} মাসের জন্য সকল সক্রিয় গ্রাহকের বিল তৈরি করতে চান?`)) return;

    try {
        const res = await apiCall('/bills/generate-monthly', {
            method: 'POST',
            body: JSON.stringify({ monthYear: state.currentMonthYear })
        });
        alert(res.message);
        loadBilling();
        loadDashboard();
    } catch (err) {
        alert(`বিল তৈরি ব্যর্থ হয়েছে: ${err.message}`);
    }
}

// 4. PAYMENTS & COLLECTIONS
async function loadPayments() {
    try {
        const res = await apiCall('/payments?limit=100');
        if (res.success) {
            state.payments = res.data;
            renderPayments();
        }
    } catch (err) {
        console.error('Failed to load payments:', err);
    }
}

function renderPayments() {
    const tbody = document.getElementById('paymentsTableBody');
    if (!tbody) return;

    if (state.payments.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: var(--text-muted); padding: 30px;">কোনো আদায়ের রেকর্ড নেই।</td></tr>`;
        return;
    }

    tbody.innerHTML = state.payments.map(p => `
        <tr>
            <td><span style="font-weight: 700; color: var(--accent-cyan);">${p.customer_code}</span></td>
            <td>${p.customer_name}</td>
            <td><strong style="color: var(--accent-green); font-size: 14px;">${formatCurrency(p.amount)}</strong></td>
            <td><span class="badge" style="background: rgba(0,229,255,0.1); color: var(--accent-cyan);">${p.payment_method}</span></td>
            <td>${formatDate(p.date_millis)}</td>
            <td>
                <button class="btn btn-outline" style="padding: 4px 8px; font-size: 11px;" onclick="openReceiptFromPayment(${JSON.stringify(p).replace(/"/g, '&quot;')})">
                    🧾 মানি রসিদ
                </button>
                <button class="btn btn-danger" style="padding: 4px 8px; font-size: 11px; margin-left: 6px;" onclick="deletePayment(${p.id})">
                    বাতিল
                </button>
            </td>
        </tr>
    `).join('');
}

// 5. EXPENSES
async function loadExpenses() {
    try {
        const res = await apiCall(`/expenses?monthYear=${state.currentMonthYear}`);
        const sumRes = await apiCall(`/expenses/summary?monthYear=${state.currentMonthYear}`);
        if (res.success) {
            state.expenses = res.data;
            renderExpenses(sumRes.data);
        }
    } catch (err) {
        console.error('Failed to load expenses:', err);
    }
}

function renderExpenses(summaryData) {
    const totalOutflow = state.expenses.reduce((acc, e) => acc + parseFloat(e.amount), 0);
    document.getElementById('expensesTotalOutflow').innerText = formatCurrency(totalOutflow);

    const tbody = document.getElementById('expensesTableBody');
    if (!tbody) return;

    if (state.expenses.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color: var(--text-muted); padding: 30px;">এই মাসে কোনো খরচের এন্ট্রি নেই।</td></tr>`;
        return;
    }

    tbody.innerHTML = state.expenses.map(e => `
        <tr>
            <td><strong>${e.title}</strong></td>
            <td><span class="badge" style="background: rgba(255,145,0,0.15); color: var(--accent-orange);">${e.category}</span></td>
            <td><span style="color: var(--accent-red); font-weight: 700;">${formatCurrency(e.amount)}</span></td>
            <td>${formatDate(e.date_millis)}</td>
            <td>
                <button class="btn btn-danger" style="padding: 4px 8px; font-size: 11px;" onclick="deleteExpense(${e.id})">মুছুন</button>
            </td>
        </tr>
    `).join('');
}

// 6. PACKAGES
async function loadPackages() {
    try {
        const res = await apiCall('/packages');
        if (res.success) {
            state.packages = res.data;
            renderPackages();
            populatePackageDropdowns();
        }
    } catch (err) {
        console.error('Failed to load packages:', err);
    }
}

function renderPackages() {
    const grid = document.getElementById('packagesGrid');
    if (!grid) return;

    grid.innerHTML = state.packages.map(p => `
        <div class="glass-card" style="border-top: 3px solid ${p.color_hex || 'var(--accent-cyan)'};">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                <h3 style="font-size: 17px; font-weight: 700;">${p.name}</h3>
                <span class="badge" style="background: rgba(0,229,255,0.15); color: var(--accent-cyan); font-size: 12px;">
                    ${p.speed_mbps} Mbps
                </span>
            </div>
            <div style="font-size: 24px; font-weight: 800; color: var(--accent-green); margin-bottom: 8px;">
                ${formatCurrency(p.monthly_price)}<span style="font-size: 13px; color: var(--text-muted);">/মাস</span>
            </div>
            <p style="font-size: 12.5px; color: var(--text-secondary); margin-bottom: 16px;">${p.description || 'অপটিক্যাল ফাইবার সংযোগ'}</p>
            <div style="display: flex; justify-content: flex-end; gap: 8px;">
                <button class="btn btn-danger" style="padding: 4px 8px; font-size: 11px;" onclick="deletePackage(${p.id})">মুছুন</button>
            </div>
        </div>
    `).join('');
}

function populatePackageDropdowns() {
    const select = document.getElementById('custPackageSelect');
    if (select) {
        select.innerHTML = '<option value="">কাস্টম প্যাকেজ বা সিলেক্ট করুন</option>' +
            state.packages.map(p => `<option value="${p.id}" data-price="${p.monthly_price}" data-speed="${p.speed_mbps}">${p.name} (${p.speed_mbps} Mbps - ৳${p.monthly_price})</option>`).join('');
    }
}

// 7. MIKROTIK
async function loadMikrotik() {
    try {
        const res = await apiCall('/mikrotik/config');
        if (res.success && res.data) {
            document.getElementById('mkHost').value = res.data.host || '192.168.88.1';
            document.getElementById('mkPort').value = res.data.port || 8728;
            document.getElementById('mkUser').value = res.data.username || 'admin';
        }
    } catch (e) {}
}

async function testMikrotikConnection() {
    const host = document.getElementById('mkHost').value;
    const port = document.getElementById('mkPort').value;
    const statusDiv = document.getElementById('mkPingStatus');
    statusDiv.innerHTML = `<span style="color: var(--accent-cyan);">Testing connection to ${host}:${port}...</span>`;

    try {
        const res = await apiCall(`/mikrotik/status?host=${host}&port=${port}`);
        if (res.data.success) {
            statusDiv.innerHTML = `<span style="color: var(--accent-green); font-weight: 700;">✅ ${res.data.message}</span>`;
        } else {
            statusDiv.innerHTML = `<span style="color: var(--accent-red); font-weight: 700;">❌ ${res.data.message}</span>`;
        }
    } catch (e) {
        statusDiv.innerHTML = `<span style="color: var(--accent-red);">❌ Error: ${e.message}</span>`;
    }
}

// 8. SETTINGS
async function loadSettings() {
    try {
        const res = await apiCall('/settings');
        if (res.success && res.data) {
            state.settings = res.data;
            document.getElementById('topbarCompanyName').innerText = res.data.company_name || 'Internet Billing Manager';
            document.getElementById('sidebarCompanyName').innerText = res.data.company_name || 'Internet Billing Manager';
            document.getElementById('setCompanyName').value = res.data.company_name || '';
            document.getElementById('setTagline').value = res.data.tagline || '';
            document.getElementById('setPhone').value = res.data.phone || '';
            document.getElementById('setAddress').value = res.data.address || '';
        }
    } catch (e) {}
}

async function saveSettings(e) {
    if (e) e.preventDefault();
    const payload = {
        companyName: document.getElementById('setCompanyName').value,
        tagline: document.getElementById('setTagline').value,
        phone: document.getElementById('setPhone').value,
        address: document.getElementById('setAddress').value
    };
    try {
        await apiCall('/settings', {
            method: 'PUT',
            body: JSON.stringify(payload)
        });
        alert('সেটিংস সফলভাবে সংরক্ষিত হয়েছে!');
        loadSettings();
    } catch (err) {
        alert(`সেটিংস সংরক্ষণ ব্যর্থ: ${err.message}`);
    }
}

// ==========================================================================
// MODALS & ACTIONS
// ==========================================================================

function openModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('active');
}

function closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('active');
}

// Add / Edit Customer Modal
function openAddCustomerModal() {
    document.getElementById('customerModalTitle').innerText = 'নতুন গ্রাহক যোগ করুন';
    document.getElementById('customerForm').reset();
    document.getElementById('custId').value = '';
    
    // Auto customer code
    const nextCode = `U${String(state.customers.length + 1).padStart(3, '0')}`;
    document.getElementById('custCode').value = nextCode;

    openModal('customerModal');
}

function editCustomer(id) {
    const cust = state.customers.find(c => c.id == id);
    if (!cust) return;

    document.getElementById('customerModalTitle').innerText = 'গ্রাহক তথ্য সম্পাদন';
    document.getElementById('custId').value = cust.id;
    document.getElementById('custCode').value = cust.customer_code;
    document.getElementById('custName').value = cust.name;
    document.getElementById('custPppoe').value = cust.pppoe_username;
    document.getElementById('custPhone').value = cust.phone;
    document.getElementById('custBill').value = cust.monthly_bill;
    document.getElementById('custDue').value = cust.due_amount;
    document.getElementById('custDueDay').value = cust.bill_due_day || 10;
    document.getElementById('custStatus').value = cust.status || 'Active';
    document.getElementById('custAddress').value = cust.address || '';

    if (cust.package_id) {
        document.getElementById('custPackageSelect').value = cust.package_id;
    }

    openModal('customerModal');
}

async function handleCustomerFormSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('custId').value;
    const pkgSelect = document.getElementById('custPackageSelect');
    const selectedPkg = pkgSelect.options[pkgSelect.selectedIndex];

    const payload = {
        customerCode: document.getElementById('custCode').value,
        name: document.getElementById('custName').value,
        pppoeUsername: document.getElementById('custPppoe').value,
        phone: document.getElementById('custPhone').value,
        monthlyBill: parseFloat(document.getElementById('custBill').value) || 0,
        dueAmount: parseFloat(document.getElementById('custDue').value) || 0,
        billDueDay: parseInt(document.getElementById('custDueDay').value, 10) || 10,
        status: document.getElementById('custStatus').value,
        address: document.getElementById('custAddress').value,
        packageId: pkgSelect.value ? parseInt(pkgSelect.value, 10) : null,
        packageName: selectedPkg && selectedPkg.value ? selectedPkg.text.split(' (')[0] : '',
        speedMbps: selectedPkg ? parseInt(selectedPkg.getAttribute('data-speed'), 10) || 10 : 10
    };

    try {
        if (id) {
            await apiCall(`/customers/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
        } else {
            await apiCall('/customers', { method: 'POST', body: JSON.stringify(payload) });
        }
        closeModal('customerModal');
        loadCustomers();
        loadDashboard();
    } catch (err) {
        alert(`গ্রাহক সংরক্ষণ ব্যর্থ: ${err.message}`);
    }
}

async function deleteCustomer(id) {
    if (!confirm('আপনি কি নিশ্চিত যে এই গ্রাহকটি মুছে ফেলতে চান? গ্রাহকের সাথে যুক্ত সকল বিল ও পেমেন্ট হিস্টোরিও মুছে যাবে!')) return;
    try {
        await apiCall(`/customers/${id}`, { method: 'DELETE' });
        loadCustomers();
        loadDashboard();
    } catch (err) {
        alert(`গ্রাহক ডিলিট ব্যর্থ: ${err.message}`);
    }
}

// Collect Payment Modal
function openCollectPaymentModal(customerId) {
    const cust = state.customers.find(c => c.id == customerId);
    if (!cust) return;

    state.selectedCustomerForPayment = cust;

    document.getElementById('payCustName').innerText = cust.name;
    document.getElementById('payCustDetails').innerText = `${cust.customer_code} • PPPoE: ${c => cust.pppoe_username} • ${formatCurrency(cust.monthly_bill)}/মাস`;
    document.getElementById('payCustDue').innerText = `বকেয়া: ${formatCurrency(cust.due_amount)}`;

    const amountInput = document.getElementById('payAmount');
    amountInput.value = cust.due_amount > 0 ? Math.round(cust.due_amount) : Math.round(cust.monthly_bill);
    document.getElementById('payDiscount').value = '0';
    document.getElementById('payNote').value = '';

    updatePaymentRemainCalculation();
    openModal('collectPaymentModal');
}

function updatePaymentRemainCalculation() {
    if (!state.selectedCustomerForPayment) return;
    const cust = state.selectedCustomerForPayment;
    const amt = parseFloat(document.getElementById('payAmount').value) || 0;
    const disc = parseFloat(document.getElementById('payDiscount').value) || 0;
    const curDue = parseFloat(cust.due_amount) || 0;

    const remain = Math.max(0, curDue - (amt + disc));
    document.getElementById('payRemainDue').innerText = `অবশিষ্ট বকেয়া: ${formatCurrency(remain)}`;
}

async function submitCollectPayment(e) {
    e.preventDefault();
    if (!state.selectedCustomerForPayment) return;

    const cust = state.selectedCustomerForPayment;
    const amount = parseFloat(document.getElementById('payAmount').value) || 0;
    const discount = parseFloat(document.getElementById('payDiscount').value) || 0;
    const method = document.getElementById('payMethod').value;
    const note = document.getElementById('payNote').value;

    if (amount <= 0 && discount <= 0) {
        alert('আদায়ের অংক বা ছাড় উল্লেখ করুন।');
        return;
    }

    try {
        const res = await apiCall('/payments/collect', {
            method: 'POST',
            body: JSON.stringify({
                customerId: cust.id,
                amount,
                discount,
                method,
                note
            })
        });

        closeModal('collectPaymentModal');
        loadDashboard();
        loadCustomers();
        loadPayments();

        // Automatically open money receipt
        openReceipt({
            customerName: cust.name,
            customerCode: cust.customer_code,
            pppoeUsername: cust.pppoe_username,
            phone: cust.phone,
            packageName: cust.package_name || `${cust.speed_mbps} Mbps`,
            amount,
            discount,
            method,
            totalDueRemaining: res.data.newDue,
            voucherNo: res.data.transaction.id,
            dateMillis: res.data.transaction.date_millis
        });
    } catch (err) {
        alert(`পেমেন্ট রেকর্ড ব্যর্থ: ${err.message}`);
    }
}

async function deletePayment(id) {
    if (!confirm('আপনি কি এই পেমেন্ট বাতিল করতে চান? কাস্টমারের বকেয়া স্বয়ংক্রিয়ভাবে পুনরায় যোগ হবে।')) return;
    try {
        await apiCall(`/payments/${id}`, { method: 'DELETE' });
        loadPayments();
        loadDashboard();
        loadCustomers();
    } catch (err) {
        alert(`পেমেন্ট ডিলিট ব্যর্থ: ${err.message}`);
    }
}

// Money Receipt Voucher Rendering
function openReceipt(data) {
    state.activeReceiptData = data;

    document.getElementById('recCompanyName').innerText = state.settings.company_name || 'Internet Service Provider';
    document.getElementById('recTagline').innerText = state.settings.tagline || 'Fast • Stable • Connected';
    document.getElementById('recHotline').innerText = state.settings.phone ? `হেল্পলাইন: ${state.settings.phone}` : '';
    document.getElementById('recVoucherNo').innerText = `#IBM-${String(data.voucherNo || Date.now()).slice(-6)}`;
    document.getElementById('recDate').innerText = formatDate(data.dateMillis || Date.now());

    document.getElementById('recCustName').innerText = data.customerName || '-';
    document.getElementById('recCustCode').innerText = data.customerCode || '-';
    document.getElementById('recPppoe').innerText = data.pppoeUsername || '-';
    document.getElementById('recCustPhone').innerText = data.phone || '-';
    document.getElementById('recPlan').innerText = data.packageName || '-';
    document.getElementById('recMethod').innerText = data.method || 'Cash';

    document.getElementById('recAmountNum').innerText = `${state.settings.currency_symbol || '৳'}${Math.round(data.amount).toLocaleString()}`;
    document.getElementById('recAmountWords').innerText = numberToWords(data.amount);
    document.getElementById('recDueRemaining').innerText = `${state.settings.currency_symbol || '৳'}${Math.round(data.totalDueRemaining || 0).toLocaleString()}`;

    openModal('receiptModal');
}

function openReceiptFromPayment(p) {
    openReceipt({
        customerName: p.customer_name,
        customerCode: p.customer_code,
        pppoeUsername: p.customer_code,
        amount: parseFloat(p.amount),
        discount: parseFloat(p.discount || 0),
        method: p.payment_method,
        voucherNo: p.id,
        dateMillis: p.date_millis
    });
}

function printReceipt() {
    window.print();
}

function openWhatsAppChat(phone, name, due) {
    if (!phone) {
        alert('গ্রাহকের ফোন নম্বর পাওয়া যায়নি।');
        return;
    }
    const cleanPhone = phone.replace(/[^0-9]/g, '');
    const intlPhone = cleanPhone.startsWith('880') ? cleanPhone : (cleanPhone.startsWith('0') ? '88' + cleanPhone : '880' + cleanPhone);
    const msg = `প্রিয় ${name}, ${state.settings.company_name || 'ইন্টারনেট সার্ভিস'}-এর বিল পরিশোধ সংক্রান্ত বিষয়ে যোগাযোগ করা হচ্ছে। মোট বকেয়া: ৳${Math.round(due)} টাকা।`;
    window.open(`https://api.whatsapp.com/send?phone=${intlPhone}&text=${encodeURIComponent(msg)}`, '_blank');
}

// Setup Event Listeners
function setupEventListeners() {
    // Search customers
    const searchInput = document.getElementById('customerSearchInput');
    const statusFilter = document.getElementById('customerStatusFilter');

    if (searchInput) {
        searchInput.addEventListener('input', () => {
            filterCustomers(searchInput.value, statusFilter ? statusFilter.value : 'All');
        });
    }
    if (statusFilter) {
        statusFilter.addEventListener('change', () => {
            filterCustomers(searchInput ? searchInput.value : '', statusFilter.value);
        });
    }

    // Forms
    const custForm = document.getElementById('customerForm');
    if (custForm) custForm.addEventListener('submit', handleCustomerFormSubmit);

    const payForm = document.getElementById('collectPaymentForm');
    if (payForm) payForm.addEventListener('submit', submitCollectPayment);

    const payAmtInput = document.getElementById('payAmount');
    const payDiscInput = document.getElementById('payDiscount');
    if (payAmtInput) payAmtInput.addEventListener('input', updatePaymentRemainCalculation);
    if (payDiscInput) payDiscInput.addEventListener('input', updatePaymentRemainCalculation);

    const setForm = document.getElementById('settingsForm');
    if (setForm) setForm.addEventListener('submit', saveSettings);
}
