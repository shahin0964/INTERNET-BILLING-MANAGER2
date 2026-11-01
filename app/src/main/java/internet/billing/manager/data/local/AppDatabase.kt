package internet.billing.manager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
    version = 4,
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

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE customers ADD COLUMN staticIp TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE customers ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE isp_settings ADD COLUMN email TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE isp_settings ADD COLUMN networkStatus TEXT NOT NULL DEFAULT 'Operational'")
                db.execSQL("ALTER TABLE isp_settings ADD COLUMN logoUri TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE customers ADD COLUMN billDueDay INTEGER NOT NULL DEFAULT 10")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "internet_billing_manager.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun resetDatabase() {
            synchronized(this) {
                try {
                    INSTANCE?.close()
                } catch (_: Exception) {}
                INSTANCE = null
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
