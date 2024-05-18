package pixelmon.Tools

import android.util.Log
import java.io.File

fun deleteDirecoty(dir: File): Boolean {
    Log.d("delete directory", dir.name)
    dir.listFiles()?.let{
        for(file in it) {
            if(file.isDirectory) {
                deleteDirecoty(file)
            }
            Log.d("delete directory", file.name)
            file.delete()
        }
    }
    return dir.delete()
}