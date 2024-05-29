package pixelmon.mods

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.concurrent.Executors

class ModDownloadReceiver: BroadcastReceiver() {
    // Indicate that we would like to update download progress
    private val UPDATE_DOWNLOAD_PROGRESS = 1

    // Use a background thread to check the progress of downloading
    private val executor = Executors.newFixedThreadPool(1)

    // Use a handler to update progress bar on the main thread
    private val mainHandler: Handler = Handler(Looper.getMainLooper()) { msg ->
        if (msg.what == UPDATE_DOWNLOAD_PROGRESS) {
            val downloadProgress: Int = msg.arg1

            // Update your progress bar here.
            Log.d(TAG, "Download progress: $downloadProgress")
        }
        true
    }
    private var downloadManager: DownloadManager? = null
    companion object {
        @JvmStatic
        private val TAG = "ModDownloadReceiver.kt"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        downloadManager = context?.getSystemService(DownloadManager::class.java)
        if(intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if(id != -1L) {
                Log.i(TAG, "Download with Id $id finished!")
            }
        }
    }
}