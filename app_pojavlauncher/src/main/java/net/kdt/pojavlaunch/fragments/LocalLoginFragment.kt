package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import net.kdt.pojavlaunch.LauncherViewModel
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import org.apache.commons.io.input.SwappedDataInputStream
import java.io.File

class LocalLoginFragment : Fragment(R.layout.fragment_pixelmon_local_login) {
    private lateinit var mUsernameEditText: EditText
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewModel by viewModels<LauncherViewModel> {
            LauncherViewModel.provideFactory(requireContext(), this)
        }
        viewModel.bottomButtonsVisible.value = false

        mUsernameEditText = view.findViewById(R.id.login_edit_email)
        view.findViewById<View>(R.id.login_button).setOnClickListener { v: View? ->
            ExtraCore.setValue(
                ExtraConstants.MOJANG_LOGIN_TODO, arrayOf(
                    mUsernameEditText.getText().toString(), ""
                )
            )
            Tools.swapFragment(
                requireActivity(),
                PixelmonMenuFragment::class.java,
                PixelmonMenuFragment.TAG,
                false,
                null
            )
        }
    }

    /** @return Whether the mail (and password) text are eligible to make an auth request
     */
    private fun checkEditText(): Boolean {
        val text = mUsernameEditText!!.text.toString()
        return !(text.isEmpty() || text.length < 3 || text.length > 16 || !text.matches("\\w+".toRegex())
                || File(Tools.DIR_ACCOUNT_NEW + "/" + text + ".json").exists())
    }

    companion object {
        const val TAG = "LOCAL_LOGIN_FRAGMENT"
    }
}
