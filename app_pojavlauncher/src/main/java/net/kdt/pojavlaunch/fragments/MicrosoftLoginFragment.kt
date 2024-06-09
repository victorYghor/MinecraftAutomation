package net.kdt.pojavlaunch.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import net.kdt.pojavlaunch.LauncherViewModel
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore

class MicrosoftLoginFragment : Fragment() {
    private var mWebview: WebView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mWebview = inflater.inflate(R.layout.fragment_microsoft_login, container, false) as WebView
        setWebViewSettings()
        val viewModel by viewModels<LauncherViewModel> {
            LauncherViewModel.provideFactory(requireContext(), this)
        }
        viewModel.bottomButtonsVisible.value = false

        if (savedInstanceState == null) startNewSession() else restoreWebViewState(
            savedInstanceState
        )
        return mWebview
    }

    // WebView.restoreState() does not restore the WebSettings or the client, so set them there
    // separately. Note that general state should not be altered here (aka no loading pages, no manipulating back/front lists),
    // to avoid "undesirable side-effects"
    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebViewSettings() {
        val settings = mWebview!!.settings
        settings.javaScriptEnabled = true
        mWebview!!.webViewClient = WebViewTrackClient()
    }

    private fun startNewSession() {
        CookieManager.getInstance().removeAllCookies { b: Boolean? ->
            mWebview!!.clearHistory()
            mWebview!!.clearCache(true)
            mWebview!!.clearFormData()
            mWebview!!.clearHistory()
            mWebview!!.loadUrl(
                "https://login.live.com/oauth20_authorize.srf" +
                        "?client_id=00000000402b5328" +
                        "&response_type=code" +
                        "&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL" +
                        "&redirect_url=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf"
            )
        }
    }

    private fun restoreWebViewState(savedInstanceState: Bundle) {
        Log.i("MSAuthFragment", "Restoring state...")
        if (mWebview!!.restoreState(savedInstanceState) == null) {
            Log.w("MSAuthFragment", "Failed to restore state, starting afresh")
            // if, for some reason, we failed to restore our session,
            // just start afresh
            startNewSession()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Since the value cannot be null, just creaqte a "blank" client. This is done to not let Android
        // kill us if something happens after onSaveInstanceState
        mWebview!!.webViewClient = WebViewClient()
        mWebview!!.saveState(outState)
        super.onSaveInstanceState(outState)
        // if something happens after this, well, too bad
    }

    /* Expose webview actions to others */
    fun canGoBack(): Boolean {
        return mWebview!!.canGoBack()
    }

    fun goBack() {
        mWebview!!.goBack()
    }

    /** Client to track when to sent the data to the launcher  */
    internal inner class WebViewTrackClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.startsWith("ms-xal-00000000402b5328")) {
                // Should be captured by the activity to kill the fragment and get
                ExtraCore.setValue(ExtraConstants.MICROSOFT_LOGIN_TODO, Uri.parse(url))
                Toast.makeText(view.context, "Login started !", Toast.LENGTH_SHORT).show()
                Tools.removeCurrentFragment(requireActivity())
                return true
            }

            // Sometimes, the user just clicked cancel
            if (url.contains("res=cancel")) {
                requireActivity().onBackPressed()
                return true
            }
            return super.shouldOverrideUrlLoading(view, url)
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {}
        override fun onPageFinished(view: WebView, url: String) {}
    }

    companion object {
        const val TAG = "MICROSOFT_LOGIN_FRAGMENT"
    }
}
