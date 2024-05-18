package pixelmon.Tools

import android.app.DownloadManager
import android.app.DownloadManager.COLUMN_STATUS
import android.content.Context
import android.util.Log


fun checkDownloadStatus(downloadManager: DownloadManager, downloadId: Long): Int {
    return try {
        
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        if (cursor.count == 0) {
            Log.e("checkDownloadStatus", "Download not found with ID: $downloadId")
            return -1
        }
        val statusColumnIndex = cursor.getColumnIndex(COLUMN_STATUS)
        cursor.getInt(statusColumnIndex)
    } catch(e: Exception) {
        e.printStackTrace()
        Log.e("checkDownloadStatus", e.message ?: "")
        -1
    }
}