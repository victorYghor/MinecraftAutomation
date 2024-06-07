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


    }
}