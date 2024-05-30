package net.kdt.pojavlaunch

import android.os.Bundle
import net.kdt.pojavlaunch.fragments.PixelmonWelcomeScreen

class WelcomePixelmonActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_pixelmon)
        Tools.swapWelcomeFragment(
            this,
            PixelmonWelcomeScreen::class.java,
            PixelmonWelcomeScreen.TAG,
            false,
            null
        )
    }
}