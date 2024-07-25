package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.LauncherViewModel
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.databinding.PixelmonHomeBinding
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import pixelmon.Loading
import pixelmon.mods.PixelmonVersion
import timber.log.Timber

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class PixelmonMenuFragment() : Fragment(R.layout.pixelmon_home) {
    companion object {
        const val TAG = "PixelmonMenuFragment"
    }

    var _binding: PixelmonHomeBinding? = null
    val b get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
        super.onViewCreated(view, savedInstanceState)

        // handle changes between button play and progress bar
        // Handle the first fragment to show
        val viewModel by viewModels<LauncherViewModel> {
            LauncherViewModel.provideFactory(requireContext(), this)
        }
        val bottomButtonsVisibilityObserver = Observer<Boolean> { visible: Boolean ->
            (activity as LauncherActivity).setBottomButtonsVisibility(visible)
        }
        viewModel.bottomButtonsVisible.observe(requireActivity(), bottomButtonsVisibilityObserver)
        viewModel.bottomButtonsVisible.value = true

        LauncherPreferences.loadPreferences(requireContext())
        viewModel.setupPixelmonLoading()

        val installOneDotSixTeenDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.install_one_dot_sixteen)
            setMessage(R.string.description_install_one_dot_sixteen)
            setNegativeButton(R.string.cancel) { dialog, witch ->
                // fecha o popup quando voce clica em cancelar
                dialog.cancel()
            }
            setPositiveButton(R.string.confirm) { dialog, witch ->
                // começar o download das bilbliotecas do minecraft 1.16
                viewModel.loadingState.value = Loading.DOWNLOAD_ONE_DOT_SIXTEEN
                dialog.cancel()
            }
        }

        b.radioBtnVersion112.setOnCheckedChangeListener { buttonView, checked ->
            if(checked) {
                viewModel.selectedPixelmonVersion.value = PixelmonVersion.OneDotTwelve
                b.btnOpenSelectVersion.text = getString(R.string.pixelmon_1_12_2)
                Timber.d("one dot twelve select select_version_is_one_dot_twelve preference is " + LauncherPreferences.SELECT_VERSION_IS_ONE_DOT_TWELVE)
            }
        }
        b.radioBtnVersion116.setOnCheckedChangeListener { buttonView, checked ->
            if(checked) {
                viewModel.selectedPixelmonVersion.value = PixelmonVersion.OneDotSixteen
                b.btnOpenSelectVersion.text = getString(R.string.pixelmon_1_16_5)
                Timber.d("one dot sixteen select select_version_is_one_dot_twelve preference is " + LauncherPreferences.SELECT_VERSION_IS_ONE_DOT_TWELVE)
            }
        }
        if (viewModel.downloadModOneDotSixteen.value == false) {
            b.btnOpenSelectVersion.text = context?.getString(R.string.baixar_a_vers_o_1_16)
        } else {
            toggleArrowIcon()
            when(viewModel.selectedPixelmonVersion.value) {
                PixelmonVersion.OneDotTwelve -> {
                    b.btnOpenSelectVersion.text = getString(R.string.pixelmon_1_12_2)
                }
                PixelmonVersion.OneDotSixteen -> {
                    b.btnOpenSelectVersion.text = getString(R.string.pixelmon_1_16_5)
                }
                else -> {}
            }
        }

        // temp way to create a progress bar
//        ExtraCore.setValue(ExtraConstants.LOADING_INTERNAL, LoadingType.MOVING_FILES)
        b.btnOpenSelectVersion.setOnClickListener {
            if (ProgressKeeper.hasOngoingTasks()) {
                Toast.makeText(
                    requireContext(),
                    "Existem processo em andamento por favor espere",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (viewModel.downloadOneDotSixteen.value == true) {
                    b.radioGroupSelectVersion.visibility =
                        if (b.radioGroupSelectVersion.visibility == View.GONE) {
                            toggleArrowIcon()
                            View.VISIBLE
                        } else {
                            toggleArrowIcon()
                            View.GONE
                        }
                } else {
                    if(viewModel.dialogDownloadOneSixteenIsShowing.value == false) {
                        installOneDotSixTeenDialog.show()
                        viewModel.dialogDownloadOneSixteenIsShowing.value = true
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}