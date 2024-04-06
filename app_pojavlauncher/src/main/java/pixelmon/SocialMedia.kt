package pixelmon

import android.content.Intent
import android.net.Uri

enum class SocialMedia(open: Intent) {
    OFFICIAL_SITE(Intent(Intent.ACTION_VIEW, Uri.parse("https://pixelmonbrasil.com.br/"))),
    TIK_TOK(Intent(Intent.ACTION_VIEW, Uri.parse("https://vm.tiktok.com/ZMM2cHDE1/"))),
    INSTAGRAM(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/reel/C4OqydYu1kk/?igsh=cG91bm8wY2o3Y3A3"))),
    YOUTUBE(Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/shorts/6tyP-nHk8c8?si=ODdigTDCdPoA77IC")))
}