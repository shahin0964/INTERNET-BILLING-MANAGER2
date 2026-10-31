package internet.billing.manager.data.local

import androidx.room.*
import internet.billing.manager.data.model.IspSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM isp_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<IspSettings?>

    @Query("SELECT * FROM isp_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): IspSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: IspSettings)
}
