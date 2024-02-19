package pixelmon

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import net.kdt.pojavlaunch.JavaGUILauncherActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.ForgeDownloadTask
import net.kdt.pojavlaunch.modloaders.ForgeUtils
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import java.io.File

class ForgerInstaller(private val context: Context, val popStack: () -> Boolean): ModloaderDownloadListener {
    private val TAG = "ForgerInstaller.kt"
    private var proxy: ModloaderListenerProxy? = ModloaderListenerProxy()
    private val version = "1.12.2-14.23.5.2860"
    fun install() {
        if(ProgressKeeper.hasOngoingTasks()) {
            Toast.makeText(context, R.string.tasks_ongoing, Toast.LENGTH_LONG).show()
        }
        val taskProxy = ModloaderListenerProxy()
        val downloadTask = ForgeDownloadTask(taskProxy, version)
        // set the proxy to the ui in the implementation on the fragment install forge
        taskProxy.attachListener(this)
        // todo use coroutines here
        Log.i(TAG, "Starting the download")
        Thread(downloadTask).start()
        Log.i(TAG, "Download finished")
    }

    override fun onDownloadFinished(downloadedFile: File?) {
        Tools.runOnUiThread {
            proxy?.detachListener()
            proxy = null
            popStack()
            onDownloadFinished(context, downloadedFile)
        }
    }

    fun onDownloadFinished(context: Context, downloadedFile: File?) {
        val modInstallerStartIntent = Intent(context, JavaGUILauncherActivity::class.java)
        ForgeUtils.addAutoInstallArgs(modInstallerStartIntent, downloadedFile, true)
        context.startActivity(modInstallerStartIntent)
    }

    override fun onDataNotAvailable() {
        Tools.runOnUiThread {
            proxy?.detachListener()
            proxy = null
            Tools.dialog(context,
                context.getString(R.string.global_error),
                context.getString(R.string.forge_dl_no_installer))
        }
    }

    override fun onDownloadError(e: Exception?) {
        Tools.runOnUiThread {
            proxy?.detachListener()
            proxy = null
            Tools.showError(context, e)
        }
    }


}