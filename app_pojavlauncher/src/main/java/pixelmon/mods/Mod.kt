package pixelmon.mods

data class Mod(
    val id: String,
    val name: String,
    val type: String,
    val artifact: Artifact
)

data class Artifact(
    val fileName: String,
    val size: String,
    val url: String,
    val MD5: String
)

data class ModFile(val mods: Array<Mod>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModFile

        return mods.contentEquals(other.mods)
    }

    override fun hashCode(): Int {
        return mods.contentHashCode()
    }
}
