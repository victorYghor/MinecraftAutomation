package pixelmon.mods

data class Mod(
    val id: String,
    val name: String,
    val type: String,
    val artifact: Artifact
)

data class Artifact(
    val size: String,
    val url: String,
    val MD5: String
)
