package pixelmon.Tools

import android.app.DownloadManager
import android.app.DownloadManager.COLUMN_STATUS
import android.content.Context


fun checkDownloadStatus(downloadManager: DownloadManager, downloadId: Long): Int {
    return try {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        val statusColumnIndex = cursor.getColumnIndex(COLUMN_STATUS)
        cursor.getInt(statusColumnIndex)
    } catch(e: Exception) {
        -1
    }
}