package pixelmon.JRE

interface Downloader {
    fun download(url: String): Long
}