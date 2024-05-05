package pixelmon.forge

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import pixelmon.Tools.DeleteDirecoty
import pixelmon.Tools.DownloadsIds
import pixelmon.Tools.checkDownloadStatus
import java.io.File

class ForgeDownloadReceiver : BroadcastReceiver() {
    private val TAG = "ForgeDownloadReceiver.kt"
    override fun onReceive(context: Context?, intent: Intent?) {
        val downloadManager = context?.getSystemService(DownloadManager::class.java)?.let { dm ->
            if (intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                val downloadsIds = DownloadsIds.forge
                val downloadStatus = checkDownloadStatus(dm, id)
                Log.d(TAG, "downloadStatus = $downloadStatus")
                if (downloadStatus == DownloadManager.STATUS_FAILED || downloadStatus == -1) {
                    ExtraCore.setValue(
                        ExtraConstants.ALERT_DIALOG_DOWNLOAD,
                        listOf(
                            R.string.error_download_libraries,
                            R.string.message_error_download_libraries
                        )
                    )
                } else if (downloadsIds.isNotEmpty() && id == downloadsIds.last()) {
                    Log.d(TAG, "the download was complete the forge will initiate")
                    DeleteDirecoty(File(context?.getExternalFilesDir(null), ".minecraft/libraries"))
                    ForgerDownload(context!!).unpackLibraries()
                    // this seems do not start the game
                    ExtraCore.setValue(
                        ExtraConstants.LAUNCH_GAME,
                        true
                    )
                }
            }
        } ?: Log.d(TAG, "the download manager is null something is wrong")
    }
}