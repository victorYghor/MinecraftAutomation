package pixelmon.JRE

import android.app.DownloadManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import net.kdt.pojavlaunch.R

class JREDownloader(private val context: Context): Downloader {
    private val requiredJreUrl = "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre17-ca01427/jre17-arm64-20220817-release.tar.xz"
    private val TAG = "JREDownloader.kt"

    @RequiresApi(Build.VERSION_CODES.M)
    private val downloadManager = context.getSystemService(DownloadManager::class.java)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun download(url: String): Long {
        val title = context.getString(R.string.instaling_java_envoriment)
        // todo put this in a different directory
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("application/gzip")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(title)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title)
        return downloadManager.enqueue(request)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun installRequiredJRE() {
        try {
            Log.i(TAG, "starting the jre download")
            download(requiredJreUrl)
            Log.i(TAG, "finish the jre download")
        }catch (e: Exception) {
            Log.e(TAG, e.message?: e.toString())
        }
    }

}