package internet.billing.manager

import android.app.Application
import internet.billing.manager.data.local.AppDatabase
import internet.billing.manager.data.repository.BillingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class InternetBillingApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        internet.billing.manager.ui.theme.ThemeManager.initialize(this)
    }

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy {
        BillingRepository(
            database.customerDao(),
            database.packageDao(),
            database.paymentDao(),
            database.billDao(),
            database.expenseDao(),
            database.settingsDao()
        )
    }
}
