package pixelmon

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.runBlocking

class Pixelmon(private val context: Context, popStack: () -> Boolean) {
    private val forgerInstaller = ForgerInstaller(context, popStack = popStack)
    fun start() {
//        jreInstaller.installRequiredJRE()
        runBlocking {
            forgerInstaller.install()
        }
    }
}