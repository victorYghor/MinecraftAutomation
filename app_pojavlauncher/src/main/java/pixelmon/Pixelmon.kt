package pixelmon

import android.content.Context
import android.util.Log
import com.kdt.mcgui.mcVersionSpinner
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import pixelmon.mods.ModDownloader

class Pixelmon(private val context: Context, private val versionSpinner: mcVersionSpinner, popStack: () -> Boolean) {

    companion object {
        @JvmStatic
        var state = State.MOVING_FILES
        @JvmStatic
        private val TAG = "Pixelmon.kt"
        fun launchGame() {
            Log.w(TAG, "Start the game")
            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true)
        }
    }
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
        val modDownloader = ModDownloader(context)
        modDownloader.downloadModOneDotSixteen()
        modDownloader.downloadModsOneDotTwelve(exclude = listOf("Pixelmon"))
        Log.i(TAG, "the quantity of files copied is " + MinecraftAssets.filesCount.size.toString())
        changeProfile(versionSpinner)
    }


    fun changeProfile(versionSpinner: mcVersionSpinner) {
        Log.w(TAG, "select the correct option")
        LauncherPreferences.DEFAULT_PREF.edit()
            .putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, idForgeOneDot16)
            .commit()
        LauncherPreferences.loadPreferences(context)
        val profile = LauncherPreferences.DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "")
        Log.w(TAG, "The current profile is $profile")
        versionSpinner.reloadProfiles()
        LauncherProfiles.load()
//        mProfiles = HashMap(LauncherProfiles.mainProfileJson.profiles)
//        mProfileList =
//            ArrayList(Arrays.asList(*mProfiles.keys.toTypedArray<String>()))
    }
}