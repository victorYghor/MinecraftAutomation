package pixelmon.Tools

import android.util.Log
import java.io.File

fun DeleteDirecoty(dir: File): Boolean {
    Log.d("delete directory", dir.name)
    dir.listFiles()?.let{
        for(file in it) {
            if(file.isDirectory) {
                DeleteDirecoty(file)
            }
            Log.d("delete directory", file.name)
            file.delete()
        }
    }
    return dir.delete()
}