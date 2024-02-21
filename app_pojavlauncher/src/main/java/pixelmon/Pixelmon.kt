package pixelmon

import android.content.Context
import android.nfc.Tag
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.runBlocking
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import kotlin.concurrent.thread

class Pixelmon(private val context: Context, popStack: () -> Boolean) {
    companion object {
        @JvmStatic
        private val TAG = "Pixelmon.kt"
    }
    private val forgerInstaller = ForgerInstaller(context, popStack = popStack)
    fun start() {
//        jreInstaller.installRequiredJRE()
        Thread {
            forgerInstaller.install()
            Log.i(TAG, "starting the download")
            Thread.sleep(12000)
            Log.i(TAG, "select the correct profile")
            LauncherPreferences.DEFAULT_PREF.edit()
                .putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "0")
                .apply()
            Thread.sleep(1000)
            Log.i(TAG, "launch the game")
            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true)
        }.start()
    }
}