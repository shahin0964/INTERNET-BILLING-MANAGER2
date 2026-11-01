package internet.billing.manager

import android.app.Application
import internet.billing.manager.data.local.AppDatabase
import internet.billing.manager.data.repository.BillingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class InternetBillingApp : Application() {
    companion object {
        lateinit var instance: InternetBillingApp
            private set
    }

    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        instance = this
        internet.billing.manager.ui.theme.ThemeManager.initialize(this)
        internet.billing.manager.ui.localization.LanguageManager.initialize(this)
        internet.billing.manager.util.AutoSmsManager.initialize(this)
        internet.billing.manager.util.AutoBillingManager.checkAndRunAutoBilling(this)
        internet.billing.manager.util.AutoBackupManager.initialize(this)
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
