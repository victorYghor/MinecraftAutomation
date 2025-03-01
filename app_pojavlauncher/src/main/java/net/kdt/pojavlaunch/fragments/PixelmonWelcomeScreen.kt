package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.kdt.mcgui.ProgressLayout
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.LauncherViewModel
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.databinding.FragmentPixelmonWelcomeScreenBinding
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import pixelmon.Loading
import pixelmon.Tools.Timberly
import timber.log.Timber

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
        super.onViewCreated(view, savedInstanceState)
        (activity as LauncherActivity).setBottomButtonsVisibility(false)
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
    }

    companion object {
        @JvmStatic
        val TAG: String? = "PixelmonWelcomeScreen"
    }
}