package pixelmon

import android.content.Context
import android.content.res.Resources
import android.content.res.loader.AssetsProvider
import android.util.Log
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.profiles.ProfileAdapter
import net.kdt.pojavlaunch.profiles.ProfileAdapter.mProfileList
import net.kdt.pojavlaunch.profiles.ProfileAdapter.mProfiles
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import java.util.Arrays

class Pixelmon(private val context: Context, popStack: () -> Boolean) {
    companion object {
        @JvmStatic
        private val TAG = "Pixelmon.kt"
        fun launchGame() {
            Log.w(TAG, "Start the game")
            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true)
        }
    }

    private val forgerInstaller = ForgerInstaller(context, popStack = popStack)
    fun start() {
        LauncherPreferences.loadPreferences(context)
        val profile = LauncherProfiles.getCurrentProfile()
        Log.w(
            TAG, """
            The current profile have
            gameDIr: ${profile.gameDir}
            name : ${profile.name}
            last used: ${profile.lastUsed}
        """.trimIndent()
        )
        launchGame()
    }


    fun changeProfile() {
        Log.w(TAG, "select the correct option")
        LauncherPreferences.DEFAULT_PREF.edit()
            .putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "1")
            .commit()
        LauncherPreferences.loadPreferences(context)
        val profile = LauncherPreferences.DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "")
        Log.w(TAG, "The current profile is $profile")
        LauncherProfiles.load()
        mProfiles = HashMap(LauncherProfiles.mainProfileJson.profiles)
        mProfileList =
            ArrayList(Arrays.asList(*mProfiles.keys.toTypedArray<String>()))
    }
}