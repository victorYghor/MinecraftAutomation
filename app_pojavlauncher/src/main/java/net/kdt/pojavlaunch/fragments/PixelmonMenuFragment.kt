package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.kdt.mcgui.ProgressLayout
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.databinding.PixelmonHomeBinding
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment
import pixelmon.Loading
import pixelmon.SocialMedia
import pixelmon.forge.ForgerDownload

class PixelmonMenuFragment() : Fragment(R.layout.pixelmon_home) {
    companion object {
        const val TAG = "PixelmonMenuFragment"
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

    fun toggleVersionSelectPreference(checked: Boolean) {
        LauncherPreferences.DEFAULT_PREF.edit().putBoolean(
            "select_version_is_one_dot_twelve",
            checked
        ).commit()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // handle changes between button play and progress bar
        // Handle the first fragment to show
        LauncherPreferences.loadPreferences(requireContext())
        ExtraCore.setValue(ExtraConstants.LOADING_INTERNAL, Loading.SHOW_PLAY_BUTTON)

//        ProgressLayout.setProgress(ProgressLayout.MOVING_FILES, 0);
//        var progress = 0
//        val handler = Handler(Looper.getMainLooper())
//        handler.post(object : Runnable {
//            override fun run() {
//                if(progress >= 100) return
//                Log.d(TAG, "progress is $progress")
//                handler.postDelayed(this, 100L)
//                ProgressLayout.setProgress(ProgressLayout.MOVING_FILES, ++progress)
//            }
//        })

//        val pixelmonProgressBar = view.findViewById<ProgressBar>(R.id.progress_bar_pixelmon_home)
//        pixelmonProgressBar.setProgress(100)


        val installOneDotSixTeenDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.install_one_dot_sixteen)
            setMessage(R.string.description_install_one_dot_sixteen)
            setNegativeButton(R.string.cancel) { dialog, witch ->
                // fecha o popup quando voce clica em cancelar
                dialog.cancel()
            }
            setPositiveButton(R.string.confirm) { dialog, witch ->
                // isso inicia o download do forge
                Thread {
                    Log.i(TAG, "tentar iniciar o download do pixelmon")
                    // é necessário colocar run para o codigo funcionar
                    ForgerDownload(requireContext()).run()
                }.start()
                dialog.cancel()
            }
        }
        b.radioBtnVersion112.setOnCheckedChangeListener { buttonView, checked ->
            toggleVersionSelectPreference(checked)
            Log.d(
                TAG,
                "the select_version_is_one_dot_twelve preference is ${LauncherPreferences.SELECT_VERSION_IS_ONE_DOT_TWELVE}"
            )
        }
        b.radioBtnVersion116.setOnCheckedChangeListener { buttonView, checked ->
            toggleVersionSelectPreference(!checked)
            Log.d(
                TAG,
                "the select_version_is_one_dot_twelve preference is ${LauncherPreferences.SELECT_VERSION_IS_ONE_DOT_TWELVE}"
            )
        }
        b.btnOpenSelectVersion.apply {
            text =
                if (LauncherPreferences.SELECT_VERSION_IS_ONE_DOT_TWELVE) getString(R.string.pixelmon_1_12_2) else getString(
                    R.string.pixelmon_1_16_5
                )
        }

        // temp way to create a progress bar
//        ExtraCore.setValue(ExtraConstants.LOADING_INTERNAL, LoadingType.MOVING_FILES)
        b.btnOpenSelectVersion.setOnClickListener {
            b.radioGroupSelectVersion.visibility =
                if (b.radioGroupSelectVersion.visibility == View.GONE) {
                    toggleArrowIcon()

                    View.VISIBLE
                } else {
                    toggleArrowIcon()

                    View.GONE
                }
        }


        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}