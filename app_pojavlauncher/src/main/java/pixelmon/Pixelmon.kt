package pixelmon

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.runBlocking
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.prefs.LauncherPreferences

class Pixelmon(private val context: Context, popStack: () -> Boolean) {
    private val forgerInstaller = ForgerInstaller(context, popStack = popStack)
    fun start() {
//        jreInstaller.installRequiredJRE()
        forgerInstaller.install()
        LauncherPreferences.DEFAULT_PREF.edit()
            .putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "1")
            .apply()
        ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true)
    }
}