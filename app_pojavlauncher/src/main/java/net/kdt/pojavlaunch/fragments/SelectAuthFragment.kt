package net.kdt.pojavlaunch.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.LauncherViewModel
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import pixelmon.SocialMedia

/**
 * Fragment for selecting the atuhentication method.
 */
class SelectAuthFragment : Fragment(R.layout.fragment_select_auth_method) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Hide the bottom UI elements
        (activity as LauncherActivity).setBottomButtonsVisibility(false)

        val mMicrosoftButton = view.findViewById<Button>(R.id.button_microsoft_authentication)
        val mLocalButton = view.findViewById<Button>(R.id.button_local_authentication)
        val tvIHaveProblems = view.findViewById<TextView>(R.id.tv_problems_with_login)

        tvIHaveProblems.setOnClickListener {
            startActivity(SocialMedia.DISCORD.open)
        }
        mMicrosoftButton.setOnClickListener {
            Tools.swapFragment(
                requireActivity(),
                MicrosoftLoginFragment::class.java,
                MicrosoftLoginFragment.TAG,
                false,
                null
            )
        }
        mLocalButton.setOnClickListener {
            Tools.swapFragment(
                requireActivity(),
                LocalLoginFragment::class.java,
                LocalLoginFragment.TAG,
                false,
                null
            )
        }
    }

    companion object {
        const val TAG = "AUTH_SELECT_FRAGMENT"
    }
}
