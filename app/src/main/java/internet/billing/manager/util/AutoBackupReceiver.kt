package internet.billing.manager.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AutoBackupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            AutoBackupManager.performBackup(context)
            AutoBackupManager.scheduleDailyBackupAlarm(context)
            AutoBillingManager.scheduleNextMonthAlarm(context)
        }
    }
}
