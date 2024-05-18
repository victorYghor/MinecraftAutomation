package pixelmon.forge

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.Tools.read
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import pixelmon.SupportFile
import pixelmon.Tools.deleteDirecoty
import pixelmon.Tools.DownloadsIds
import pixelmon.Tools.checkDownloadStatus
import pixelmon.Tools.checkFileIntegrity
import pixelmon.Tools.md5
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
                    Log.d(TAG, "download failed")
                    ExtraCore.setValue(
                        ExtraConstants.ALERT_DIALOG_DOWNLOAD,
                        listOf(
                            R.string.error_download_libraries,
                            R.string.message_error_download_libraries
                        )
                    )
                } else if (downloadsIds.isNotEmpty() && id == downloadsIds.last()) {
                    Log.d(TAG, "the download was complete the forge will initiate")
                    // aqui é necessário verificar integridade do download
                    // se estiver tudo bem com o download ele deve continuar se não um pop up deve aparecer para visar isso ao usuário
                    val librariesZipFile = File(context.getExternalFilesDir(null), ".minecraft/libraries.zip")

                    // pegando a referencia da bliblioteca no formato json.
                    val libraryFile = Tools.GLOBAL_GSON.fromJson(
                        read(context.assets.open("support-files.json")),
                        SupportFile::class.java
                        )
                    val integrity = checkFileIntegrity(context, librariesZipFile.md5(), libraryFile.md5)
                    Log.d(TAG, "integrity of the file is $integrity")
                    // se o arquivo não tiver intregidade eu preciso avisar para o usuário que houve um problema
                    // com um arquivo deletar ele e perdir para ele fazer o download novamente
                    //
                    deleteDirecoty(File(context?.getExternalFilesDir(null), ".minecraft/libraries"))
                    ForgerDownload(context!!).unpackLibraries(librariesZipFile)
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