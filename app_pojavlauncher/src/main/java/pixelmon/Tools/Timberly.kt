package pixelmon.Tools

import timber.log.Timber

object Timberly {
    const val downloadProblem = "DownloadProblem"
    @JvmStatic
    fun hideLogs(hide: Boolean) {
        if(!hide) {
            Timber.plant(Timber.DebugTree())
        }
    }
}