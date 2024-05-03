package net.kdt.pojavlaunch.fragments

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.databinding.PixelmonHomeBinding
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment
import pixelmon.SocialMedia
import pixelmon.forge.ForgerDownload

class PixelmonMenuFragment(): Fragment(R.layout.pixelmon_home) {
    val installOneDotSixTeenDialog = AlertDialog.Builder(requireContext()).apply {
        setTitle(R.string.install_one_dot_sixteen)
        setMessage(R.string.description_install_one_dot_sixteen)
        setNegativeButton(R.string.cancel) { dialog, witch ->
            // fecha o pop up quando voce clica em cancelar
            dialog.cancel()
        }
        setPositiveButton(R.string.confirm) { dialog, witch ->
            // isso inicia o download do forge
            Thread {
                ForgerDownload(requireContext()).run()
            }
            dialog.cancel()
        }
    }
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

    private fun toggleArrowIcon() {
        b.imgDownloadIcon.visibility = View.GONE
        val upVis = b.imgArrowUp.visibility
        val downVis = b.imgArrowDown.visibility
        when {
            upVis == View.GONE && downVis == View.GONE -> b.imgArrowDown.visibility = View.VISIBLE
            upVis == View.GONE -> {
                b.imgArrowUp.visibility = View.VISIBLE
                b.imgArrowDown.visibility = View.GONE
            }
            downVis == View.GONE -> {
                b.imgArrowUp.visibility = View.GONE
                b.imgArrowDown.visibility = View.VISIBLE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.btnOpenSelectVersion.setOnClickListener {
            if(LauncherPreferences.DOWNLOAD_ONE_DOT_SIXTEEN) {
                b.radioGroupSelectVersion.visibility =
                    if(b.radioGroupSelectVersion.visibility == View.GONE) {
                        toggleArrowIcon()
                        View.VISIBLE
                    } else {
                        toggleArrowIcon()
                        View.GONE
                    }
            } else {
                installOneDotSixTeenDialog.create().show()
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