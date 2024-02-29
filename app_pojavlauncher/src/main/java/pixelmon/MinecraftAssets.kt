package pixelmon

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import net.kdt.pojavlaunch.fragments.MainMenuFragment
import java.io.File

class MinecraftAssets(val context: Context) {
    fun moveFiles(directory: String) {
        if (context?.assets != null) {
            val assets = context?.assets!!
            assets.list(directory)?.let {
                try {
                    for (dir in it) {
                        if (dir.isFile() || directory.isObjectsDir()) {
                            putFilesInData(assets = assets, name =  directory + "/" + dir)
                        } else {
                            val newDirectory = "$directory/$dir"
                            File(context?.getExternalFilesDir(null), "." + newDirectory).mkdir()
                            moveFiles(newDirectory)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /** Object is a directory containing the assets need to run minecraft
     */
    fun String.isObjectsDir(): Boolean {
        return this.takeLast(2).matches(Regex("([a-f]|\\d){2}"))
    }
    fun String.isFile(): Boolean {
        return Regex("\\.([A-Za-z])+").containsMatchIn(this)
    }

    private fun putFilesInData(name: String, assets: AssetManager) {
        val outFile = File(context?.getExternalFilesDir(null), "." + name)
        Log.d(MainMenuFragment.TAG, "Attempting to write to: " + outFile.absolutePath)
        val copiedFile = assets.open(name)
        outFile.writeBytes(copiedFile.readBytes())
        copiedFile.close()
    }
}