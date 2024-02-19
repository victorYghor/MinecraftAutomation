package pixelmon.JRE

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

class JREDownloadReceiver: BroadcastReceiver() {
    private var downloadManager: DownloadManager? = null
    companion object {
        @JvmStatic
        private val TAG = "JREDownloadReceiver.kt"
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context?, intent: Intent?) {
        downloadManager = context?.getSystemService(DownloadManager::class.java)
        if(intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            val query = DownloadManager.Query()
                .setFilterById(id)
            if(id != -1L) {
                Log.i(TAG, "Download with Id $id finished!")
            }
        }
    }
}