package internet.billing.manager.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AutoBackupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        CoroutineScope(Dispatchers.IO).launch {
            if (action == GoogleDriveSyncManager.ACTION_CLOUD_BACKUP_1130PM) {
                // 11:30 PM Google Drive cloud backup trigger
                GoogleDriveSyncManager.checkAndRunScheduled1130PmBackup(context)
            } else {
                // Default local 24/7 backup trigger & boot reschedule
                AutoBackupManager.performBackup(context)
                AutoBackupManager.scheduleDailyBackupAlarm(context)
                GoogleDriveSyncManager.scheduleDaily1130PmDriveBackupAlarm(context)
                AutoBillingManager.scheduleNextMonthAlarm(context)
            }
        }
    }
}
