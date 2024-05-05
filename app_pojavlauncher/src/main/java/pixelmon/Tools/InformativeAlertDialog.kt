package pixelmon.Tools

import android.content.Context
import androidx.appcompat.app.AlertDialog
import net.kdt.pojavlaunch.R

fun InformativeAlertDialog(ctx: Context, title: Int, message: Int) {
    AlertDialog.Builder(ctx).apply {
        setTitle(title)
        setMessage(message)
        setPositiveButton(R.string.ok) { dialog, witch ->
            dialog.cancel()
        }
    }
}