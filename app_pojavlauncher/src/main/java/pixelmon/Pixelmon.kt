package pixelmon

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import pixelmon.JRE.JREDownloader

class Pixelmon(private val context: Context, popStack: () -> Boolean) {
    private val jreInstaller = JREDownloader(context)
    private val forgerInstaller = ForgerInstaller(context, popStack = popStack)
//    private val jre
//        get() {
//
//        }

    fun start() {
//        jreInstaller.installRequiredJRE()
        forgerInstaller.install()
    }
}