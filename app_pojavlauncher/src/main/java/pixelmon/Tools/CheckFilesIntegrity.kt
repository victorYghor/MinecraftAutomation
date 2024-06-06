package pixelmon.Tools

import android.content.Context
import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.security.MessageDigest

fun File.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.readBytes())
    return digest.joinToString("").filter{ it != '-'}
}

/**
 * @param context the current context is call
 * @param copyFile th file copied
 * @param originalMd5 the code
 */
fun checkFileIntegrity(context: Context, copyPath: String, originalMd5: String?): Boolean {
    val TAG = "chekFileIntegrity.kt"
    try {
        val copyFile = File(context.getExternalFilesDir(null), copyPath)
        if(originalMd5 == null) {
            Timber.i("The copy file with this name was not found")
            return false
        }
        if(copyFile.md5() != originalMd5) return false
    } catch (e: FileNotFoundException) {
        Timber.e("File Not found")
        e.printStackTrace()
        return false
    } catch(e: Exception) {
        e.printStackTrace()
        return false
    }
    return true
}