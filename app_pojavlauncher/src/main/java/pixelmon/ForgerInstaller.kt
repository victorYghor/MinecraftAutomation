package pixelmon

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.kdt.pojavlaunch.JavaGUILauncherActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.ForgeDownloadTask
import net.kdt.pojavlaunch.modloaders.ForgeUtils
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import pixelmon.Pixelmon.Companion.startGame
import pixelmon.Version.FORGE
import java.io.File

class ForgerInstaller(private val context: Context, val popStack: () -> Boolean) :
    ModloaderDownloadListener {
    private val TAG = "ForgerInstaller.kt"
    private var proxy: ModloaderListenerProxy? = ModloaderListenerProxy()
    fun install() {
        if (ProgressKeeper.hasOngoingTasks()) {
            Toast.makeText(context, R.string.tasks_ongoing, Toast.LENGTH_LONG).show()
        }
        val taskProxy = ModloaderListenerProxy()
        val downloadTask = ForgeDownloadTask(taskProxy, Version.FORGE)
        // set the proxy to the ui in the implementation on the fragment install forge
        taskProxy.attachListener(this)
        // todo use coroutines here
        Log.i(TAG, "Starting the download")
        Thread {
            downloadTask.run()
            Pixelmon.startGame()
        }.start()
        Log.i(TAG, "Download finished")
    }

    override fun onDownloadFinished(downloadedFile: File?) {
        // faz com que a thread que estava baixando o forge, desapareça quando terminar o download
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
        LauncherPreferences.DEFAULT_PREF.edit().putBoolean("first_installation", false).commit()
        Log.i(TAG, "the value of first_installation is ${LauncherPreferences.PREF_FIRST_INSTALLATION}")
    }

    override fun onDataNotAvailable() {
        Tools.runOnUiThread {
            proxy?.detachListener()
            proxy = null
            Tools.dialog(
                context,
                context.getString(R.string.global_error),
                context.getString(R.string.forge_dl_no_installer)
            )
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