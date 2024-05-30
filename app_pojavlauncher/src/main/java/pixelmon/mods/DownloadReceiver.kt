package pixelmon.mods

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class DownloadReceiver : BroadcastReceiver() {

    private var downloadManager: DownloadManager? = null

    companion object {
        @JvmStatic
        private val TAG = "ModDownloadReceiver.kt"
    }

    @SuppressLint("Range")
    override fun onReceive(context: Context?, intent: Intent?) {
        downloadManager = context?.getSystemService(DownloadManager::class.java)
        if (intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (id != -1L) {
                Log.i(TAG, "Download with Id $id finished!")

            }
        }
    }
}