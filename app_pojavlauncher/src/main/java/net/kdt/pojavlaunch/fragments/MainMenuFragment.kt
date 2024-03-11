package net.kdt.pojavlaunch.fragments

import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.kdt.mcgui.mcVersionSpinner
import net.kdt.pojavlaunch.CustomControlsActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import pixelmon.MinecraftAssets
import pixelmon.Pixelmon
import java.io.File

class MainMenuFragment : Fragment(R.layout.fragment_launcher) {
    private lateinit var mVersionSpinner: mcVersionSpinner
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mNewsButton = view.findViewById<Button>(R.id.news_button)
        val mCustomControlButton = view.findViewById<Button>(R.id.custom_control_button)
        val mInstallJarButton = view.findViewById<Button>(R.id.install_jar_button)
        val mShareLogsButton = view.findViewById<Button>(R.id.share_logs_button)
        val mTestButton = view.findViewById<Button>(R.id.test_button)
        val mEditProfileButton = view.findViewById<ImageButton>(R.id.edit_profile_button)
        val mPlayButton = view.findViewById<Button>(R.id.play_button)
        mVersionSpinner = view.findViewById(R.id.mc_version_spinner)
        mNewsButton.setOnClickListener { v: View? ->
            Tools.openURL(
                requireActivity(),
                Tools.URL_HOME
            )
        }
        mCustomControlButton.setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    requireContext(),
                    CustomControlsActivity::class.java
                )
            )
        }
        mInstallJarButton.setOnClickListener { v: View? -> runInstallerWithConfirmation(false) }
        mInstallJarButton.setOnLongClickListener { v: View? ->
            runInstallerWithConfirmation(true)
            true
        }
        mEditProfileButton.setOnClickListener { v: View? ->
            mVersionSpinner.openProfileEditor(
                requireActivity()
            )
        }
        mPlayButton.setOnClickListener { v: View? ->
            ExtraCore.setValue(
                ExtraConstants.LAUNCH_GAME,
                true
            )
        }
        mShareLogsButton.setOnClickListener { v: View? -> Tools.shareLog(requireContext()) }
        val pixelmon = Pixelmon(requireContext(), mVersionSpinner) { parentFragmentManager.popBackStackImmediate() }
        mTestButton.setOnClickListener { v: View? -> pixelmon.start() }
        mNewsButton.setOnLongClickListener { v: View? ->
            Tools.swapFragment(
                requireActivity(),
                SearchModFragment::class.java,
                SearchModFragment.TAG,
                true,
                null
            )
            true
        }

    }

    override fun onResume() {
        super.onResume()
        mVersionSpinner.reloadProfiles()
    }

    private fun runInstallerWithConfirmation(isCustomArgs: Boolean) {
        if (ProgressKeeper.getTaskCount() == 0) Tools.installMod(
            requireActivity(),
            isCustomArgs
        ) else Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val TAG = "MainMenuFragment"
    }
}
