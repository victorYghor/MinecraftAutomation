package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.databinding.FragmentPixelmonWelcomeScreenBinding
import net.kdt.pojavlaunch.prefs.LauncherPreferences

/**
 * The first that show. When the user open the app
 */
class PixelmonWelcomeScreen : Fragment() {
    var _binging : FragmentPixelmonWelcomeScreenBinding? = null
    val b get() = _binging!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binging = FragmentPixelmonWelcomeScreenBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.btnContinue.setOnClickListener {
            LauncherPreferences.DEFAULT_PREF.edit().putBoolean("first_installation", false).commit()
            Tools.swapFragment(
                requireActivity(),
                SelectAuthFragment::class.java,
                SelectAuthFragment.TAG,
                false,
                null
            )
        }
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        @JvmStatic
        val TAG: String? = "PixelmonWelcomeScreen"
    }
}