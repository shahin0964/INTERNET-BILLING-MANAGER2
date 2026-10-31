package internet.billing.manager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import internet.billing.manager.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Customer::class,
        InternetPackage::class,
        PaymentTransaction::class,
        BillEntry::class,
        Expense::class,
        IspSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun packageDao(): PackageDao
    abstract fun paymentDao(): PaymentDao
    abstract fun billDao(): BillDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "internet_billing_manager.db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialConfig(database.settingsDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        // Purge legacy fake settings & packages if present in existing database
                        val settings = database.settingsDao().getSettingsSync()
                        if (settings != null && (settings.companyName.equals("U-turn Technologies", ignoreCase = true) || settings.phone == "01700000000")) {
                            database.settingsDao().insertOrUpdate(
                                settings.copy(
                                    companyName = "",
                                    tagline = "",
                                    phone = "",
                                    address = ""
                                )
                            )
                        }
                        database.packageDao().deleteLegacyDefaultPackages()
                    }
                }
            }

            suspend fun populateInitialConfig(
                settingsDao: SettingsDao
            ) {
                // Initialize clean ISP Settings (Customizable by user in Settings)
                settingsDao.insertOrUpdate(
                    IspSettings(
                        id = 1,
                        companyName = "",
                        tagline = "",
                        phone = "",
                        address = "",
                        currencySymbol = "৳"
                    )
                )
            }
        }
    }
}
