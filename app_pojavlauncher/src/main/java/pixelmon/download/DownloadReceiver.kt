package pixelmon.download

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kdt.mcgui.ProgressLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class DownloadReceiver : BroadcastReceiver() {

    private var downloadManager: DownloadManager? = null

    companion object {
        @JvmStatic
        private val TAG = "ModDownloadReceiver.kt"
    }

    @SuppressLint("Range")
    override fun onReceive(context: Context?, intent: Intent?) {
        val scopeUpdateUI = CoroutineScope(Dispatchers.Main)
        downloadManager = context?.getSystemService(DownloadManager::class.java)
        if (intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (id != -1L) {
                Log.i(TAG, "Download with Id $id finished!")
                scopeUpdateUI.launch {
                    ProgressLayout.setProgress(ProgressLayout.DOWNLOAD_MOD_ONE_DOT_TWELVE, 100)
                }
            } else {
                //todo tell the user that download was not successful
                ProgressLayout.setProgress(ProgressLayout.DOWNLOAD_MOD_ONE_DOT_TWELVE, 100)
            }
        }
    }
}