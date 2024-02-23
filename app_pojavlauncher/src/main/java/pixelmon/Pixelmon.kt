package pixelmon

import android.content.Context
import android.util.Log
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.prefs.LauncherPreferences

class Pixelmon(private val context: Context, popStack: () -> Boolean) {
    companion object {
        @JvmStatic
        private val TAG = "Pixelmon.kt"
        fun launchGame() {
            Log.i(TAG, "Start the game")
            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true)
        }
    }

    private val forgerInstaller = ForgerInstaller(context, popStack = popStack)
    fun start() {
        Log.i(TAG, "the value of first_installation is ${LauncherPreferences.PREF_FIRST_INSTALLATION}")
        LauncherPreferences.loadPreferences(context)
        if (LauncherPreferences.PREF_FIRST_INSTALLATION) {
            Log.i(TAG, "start forge installation")
            forgerInstaller.install()
        } else {
            launchGame()
        }
//        changeProfile()
    }


//    fun changeProfile() {
//        Log.i(TAG, "select the correct option")
//        LauncherPreferences.DEFAULT_PREF.edit()
//            .putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "0")
//            .commit()
//        LauncherPreferences.loadPreferences(context)
//        val profile = LauncherPreferences.DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "")
//        Log.i(TAG, "The current profile is $profile")
//    }
}