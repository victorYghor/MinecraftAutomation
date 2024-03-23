package pixelmon.forge

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import pixelmon.Tools.DownloadsIds

class ForgeDownloadReceiver: BroadcastReceiver() {
    private val TAG = "ForgeDownloadReceiver.kt"
    override fun onReceive(context: Context?, intent: Intent?) {
        val downloadManager = context?.getSystemService(DownloadManager::class.java)
        if(intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val downloadsIds = DownloadsIds.forge
            if(downloadsIds.isNotEmpty() && id == downloadsIds.last()) {
                Log.i(TAG, "the download was complete the forge will initiate")
                ForgerDownload(context!!).unpackLibraries()
                ExtraCore.setValue(
                    ExtraConstants.LAUNCH_GAME,
                    true
                )
            }
        }
    }
}