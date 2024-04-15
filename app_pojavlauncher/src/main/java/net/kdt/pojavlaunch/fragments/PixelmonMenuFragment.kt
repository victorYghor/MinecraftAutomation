package net.kdt.pojavlaunch.fragments

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.databinding.PixelmonHomeBinding
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment
import pixelmon.SocialMedia

class PixelmonMenuFragment(): Fragment(R.layout.pixelmon_home) {
    var _binding: PixelmonHomeBinding? = null
    val b get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PixelmonHomeBinding.inflate(layoutInflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.btnOpenSelectVersion.setOnClickListener {

            b.radioGroupSelectVersion.visibility =
                if(b.radioGroupSelectVersion.visibility == View.GONE) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        b.btnDiscord.setOnClickListener {
            startActivity(SocialMedia.DISCORD.open)
        }
        b.btnOfficialSite.setOnClickListener {
            startActivity(SocialMedia.OFFICIAL_SITE.open)
        }
        b.btnTiktok.setOnClickListener {
            startActivity(SocialMedia.TIK_TOK.open)
        }
        b.btnSettings.setOnClickListener {
            Tools.swapFragment(
                requireActivity(),
                LauncherPreferenceFragment::class.java,
                LauncherActivity.SETTING_FRAGMENT_TAG,
                true,
                null
            )
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}