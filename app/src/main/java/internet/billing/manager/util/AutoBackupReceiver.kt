package internet.billing.manager.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AutoBackupReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_LOCAL_BACKUP_DAILY = "internet.billing.manager.ACTION_LOCAL_BACKUP_DAILY"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "IBM:AutoBackupReceiverWakeLock"
        )?.apply {
            setReferenceCounted(false)
            acquire(5 * 60 * 1000L) // 5 minutes max
        }

        val action = intent?.action
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (action == GoogleDriveSyncManager.ACTION_CLOUD_BACKUP_1130PM) {
                    // 11:30 PM Google Drive cloud backup trigger
                    GoogleDriveSyncManager.checkAndRunScheduled1130PmBackup(context)
                } else {
                    // Default local 24/7 backup trigger & boot/upgrade reschedule
                    AutoBackupManager.performBackup(context)
                    AutoBackupManager.scheduleDailyBackupAlarm(context)
                    GoogleDriveSyncManager.scheduleDaily1130PmDriveBackupAlarm(context)
                    AutoBillingManager.scheduleNextMonthAlarm(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    if (wakeLock != null && wakeLock.isHeld) {
                        wakeLock.release()
                    }
                } catch (_: Exception) {}
                pendingResult.finish()
            }
        }
    }
}
