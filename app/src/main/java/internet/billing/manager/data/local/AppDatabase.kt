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
                        populateInitialConfig(
                            database.packageDao(),
                            database.settingsDao()
                        )
                    }
                }
            }

            suspend fun populateInitialConfig(
                packageDao: PackageDao,
                settingsDao: SettingsDao
            ) {
                // Initialize default ISP Internet packages as shown in UI design
                val defaultPackages = listOf(
                    InternetPackage(
                        name = "3 Mbps",
                        speedMbps = 3,
                        monthlyPrice = 300.0,
                        description = "Starter fiber connection",
                        colorHex = "#2979FF" // Blue
                    ),
                    InternetPackage(
                        name = "5 Mbps",
                        speedMbps = 5,
                        monthlyPrice = 400.0,
                        description = "Standard home internet",
                        colorHex = "#00E676" // Green
                    ),
                    InternetPackage(
                        name = "10 Mbps",
                        speedMbps = 10,
                        monthlyPrice = 600.0,
                        description = "Ultra-fast streaming fiber",
                        colorHex = "#7C4DFF" // Purple
                    ),
                    InternetPackage(
                        name = "15 Mbps",
                        speedMbps = 15,
                        monthlyPrice = 800.0,
                        description = "Gigabit pro business/gaming",
                        colorHex = "#FF9100" // Orange
                    )
                )
                packageDao.insertAll(defaultPackages)

                // Initialize default ISP Settings (Customizable by user in Settings)
                settingsDao.insertOrUpdate(
                    IspSettings(
                        id = 1,
                        companyName = "U-turn Technologies",
                        tagline = "Connect People Better Tomorrow",
                        phone = "01700000000",
                        address = "Dhaka, Bangladesh",
                        currencySymbol = "৳"
                    )
                )
            }
        }
    }
}
