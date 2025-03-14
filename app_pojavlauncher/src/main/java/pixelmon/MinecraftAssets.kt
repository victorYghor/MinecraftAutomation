package pixelmon

import android.content.Context

import android.content.res.AssetManager
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.kdt.mcgui.ProgressLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kdt.pojavlaunch.LauncherViewModel
import java.io.File

class MinecraftAssets(val context: Context, val viewModel: LauncherViewModel) {
    private val directoryTreeFile = File(context.getExternalFilesDir(null), "directoryTree.txt")
    init {
        directoryTreeFile.createNewFile()
    }
    companion object {
        val TAG = "MinecraftAssets.kt"
        val filesCount = mutableListOf<Boolean>()
    }
    fun moveFiles(directory: String) {
//        Log.i(TAG, "start moving files")
        if (context.assets != null) {
            val assets = context.assets
            assets.list(directory)?.let {
                try {
                    for (dir in it) {
                        val newDirectory = "$directory/$dir"
                        if (isFile(newDirectory)) {
                            putFilesInData(assets = assets, name =  newDirectory)
                            directoryTreeFile.appendText(
                                "\t|".repeat(newDirectory.count{ it == '/' }) + newDirectory + "\n"
                            )
                        } else {
                            File(context.getExternalFilesDir(null), "." + newDirectory).mkdirs()
                            directoryTreeFile.appendText("\t|".repeat(newDirectory.count { it == '/' }) + newDirectory + "/\n")
                            moveFiles(newDirectory)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
//        LauncherPreferences.DEFAULT_PREF.edit().putBoolean("first_installation", false).commit()
//        Log.w(TAG, "the value of first_installation is ${LauncherPreferences.PREF_FIRST_INSTALLATION}")
    }

    fun isFile(path: String): Boolean {
        val file = context.assets.list(path)
        return file?.isEmpty() ?: throw Exception("Não existe arquivo nesse path $path")
    }

    private fun putFilesInData(name: String, assets: AssetManager) {
        val outFile = File(context.getExternalFilesDir(null), "." + name)
        filesCount.add(outFile.exists())
        // set the progress when moving files
        val progress = (filesCount.size * 100) / 2928
        ProgressLayout.setProgress(ProgressLayout.MOVING_FILES, progress, Loading.MOVING_FILES.messageLoading)
        assets.open(name).use { copiedFile ->
            outFile.writeBytes(copiedFile.readBytes())
        }
    }
//    fun checkFileIntegrity(directory: String): Boolean {
//        val rootDirectory = File(context.getExternalFilesDir(null), directory)
//        for(file in rootDirectory.list()) {
//            if(context.assets.open(directory.drop(1) + "/" + file))
//        }
//    }

    fun moveImportantAssets() = CoroutineScope(Dispatchers.IO).launch {
        try {
            if(directoryTreeFile.exists() && directoryTreeFile.readBytes()
                    .contentEquals(context.assets.open("directoryTree.txt").readBytes())) {
//                Log.i(TAG, "All files was transferred")
            } else {
                directoryTreeFile.writeText("")
                File(context.getExternalFilesDir(null), ".minecraft/mods").mkdirs()
                moveFiles("minecraft")
            }
//            Log.i(TAG, "the quantity of files copied is " + MinecraftAssets.filesCount.size.toString())
        } catch(e: Exception) {
            Log.e(TAG, e.message.toString())
            e.printStackTrace()
        }
    }
}