package net.kdt.pojavlaunch

import android.Manifest
import android.app.NotificationManager
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.kdt.mcgui.ProgressLayout
import com.kdt.mcgui.mcAccountSpinner
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.extra.ExtraListener
import net.kdt.pojavlaunch.fragments.MainMenuFragment
import net.kdt.pojavlaunch.fragments.MicrosoftLoginFragment
import net.kdt.pojavlaunch.fragments.SelectAuthFragment
import net.kdt.pojavlaunch.lifecycle.ContextAwareDoneListener
import net.kdt.pojavlaunch.lifecycle.ContextExecutor
import net.kdt.pojavlaunch.modloaders.modpacks.ModloaderInstallTracker
import net.kdt.pojavlaunch.modloaders.modpacks.imagecache.IconCacheJanitor
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener
import net.kdt.pojavlaunch.services.ProgressServiceKeeper
import net.kdt.pojavlaunch.tasks.AsyncMinecraftDownloader
import net.kdt.pojavlaunch.tasks.AsyncVersionList
import net.kdt.pojavlaunch.tasks.MinecraftDownloader
import net.kdt.pojavlaunch.utils.NotificationUtils
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import pixelmon.MinecraftAssets
import java.lang.ref.WeakReference

class LauncherActivity : BaseActivity() {
    @JvmField
    val modInstallerLauncher =
        registerForActivityResult(OpenDocumentWithExtension("jar")) { data: Uri? ->
            if (data != null) Tools.launchModInstaller(
                this,
                data
            )
        }
    private var mAccountSpinner: mcAccountSpinner? = null
    private var mFragmentView: FragmentContainerView? = null
    private var mSettingsButton: ImageButton? = null
    private var mDeleteAccountButton: ImageButton? = null
    private var mProgressLayout: ProgressLayout? = null
    private var mProgressServiceKeeper: ProgressServiceKeeper? = null
    private var mInstallTracker: ModloaderInstallTracker? = null
    private var mNotificationManager: NotificationManager? = null

    /* Allows to switch from one button "type" to another */
    private val mFragmentCallbackListener: FragmentManager.FragmentLifecycleCallbacks =
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                mSettingsButton!!.setImageDrawable(
                    ContextCompat.getDrawable(
                        baseContext,
                        if (f is MainMenuFragment) R.drawable.ic_menu_settings else R.drawable.ic_menu_home
                    )
                )
            }
        }

    /* Listener for the back button in settings */
    private val mBackPreferenceListener = ExtraListener { key: String?, value: String ->
        if (value == "true") onBackPressed()
        false
    }

    /* Listener for the auth method selection screen */
    private val mSelectAuthMethod = ExtraListener { key: String?, value: Boolean? ->
        val fragment = supportFragmentManager.findFragmentById(
            mFragmentView!!.id
        ) as? MainMenuFragment ?: return@ExtraListener false
        // Allow starting the add account only from the main menu, should it be moved to fragment itself ?
        Tools.swapFragment(this, SelectAuthFragment::class.java, SelectAuthFragment.TAG, true, null)
        false
    }

    /* Listener for the settings fragment */
    private val mSettingButtonListener = View.OnClickListener { v: View? ->
        val fragment = supportFragmentManager.findFragmentById(
            mFragmentView!!.id
        )
        if (fragment is MainMenuFragment) {
            Tools.swapFragment(
                this,
                LauncherPreferenceFragment::class.java,
                SETTING_FRAGMENT_TAG,
                true,
                null
            )
        } else {
            // The setting button doubles as a home button now
            while (supportFragmentManager.findFragmentById(mFragmentView!!.id) !is MainMenuFragment) {
                supportFragmentManager.popBackStackImmediate()
            }
        }
    }

    /* Listener for account deletion */
    private val mAccountDeleteButtonListener = View.OnClickListener { v: View? ->
        AlertDialog.Builder(this)
            .setMessage(R.string.warning_remove_account)
            .setPositiveButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.global_delete) { dialog: DialogInterface?, which: Int -> mAccountSpinner!!.removeCurrentAccount() }
            .show()
    }

    /**
     * see for problems then start the minecraft game
     */
    private val mLaunchGameListener = ExtraListener { key: String?, value: Boolean? ->
        if (mProgressLayout!!.hasProcesses()) {
            Toast.makeText(this, R.string.tasks_ongoing, Toast.LENGTH_LONG).show()
            return@ExtraListener false
        }
        val selectedProfile = LauncherPreferences.DEFAULT_PREF.getString(
            LauncherPreferences.PREF_KEY_CURRENT_PROFILE,
            ""
        )
        if (LauncherProfiles.mainProfileJson == null || !LauncherProfiles.mainProfileJson.profiles.containsKey(
                selectedProfile
            )
        ) {
            Log.i(
                TAG, "LauncherProfiles.mainProfileJson == null = " +
                        (LauncherProfiles.mainProfileJson == null) +
                        "!LauncherProfiles.mainProfileJson.profiles.containsKey(selectedProfile) = "
                        + !LauncherProfiles.mainProfileJson.profiles.containsKey(selectedProfile)
            )
            Log.i(TAG, "the select profile is $selectedProfile")
            Toast.makeText(this, R.string.error_no_version, Toast.LENGTH_LONG).show()
            return@ExtraListener false
        }
        val prof = LauncherProfiles.mainProfileJson.profiles[selectedProfile]
        if (prof?.lastVersionId == null || "Unknown" == prof.lastVersionId) {
            Toast.makeText(this, R.string.error_no_version, Toast.LENGTH_LONG).show()
            return@ExtraListener false
        }
        if (mAccountSpinner!!.selectedAccount == null) {
            Toast.makeText(this, R.string.no_saved_accounts, Toast.LENGTH_LONG).show()
            ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true)
            return@ExtraListener false
        }
        val normalizedVersionId = AsyncMinecraftDownloader.normalizeVersionId(prof.lastVersionId)
        val mcVersion = AsyncMinecraftDownloader.getListedVersion(normalizedVersionId)
        MinecraftDownloader().start(
            this,
            mcVersion,
            normalizedVersionId,
            ContextAwareDoneListener(this, normalizedVersionId)
        )
        false
    }
    private val mDoubleLaunchPreventionListener = TaskCountListener { taskCount: Int ->
        // Hide the notification that starts the game if there are tasks executing.
        // Prevents the user from trying to launch the game with tasks ongoing.
        if (taskCount > 0) {
            Tools.runOnUiThread { mNotificationManager!!.cancel(NotificationUtils.NOTIFICATION_ID_GAME_START) }
        }
    }
    private var mRequestNotificationPermissionLauncher: ActivityResultLauncher<String>? = null
    private var mRequestNotificationPermissionRunnable: WeakReference<Runnable>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pojav_launcher)
        IconCacheJanitor.runJanitor()
        mRequestNotificationPermissionLauncher = registerForActivityResult(
            RequestPermission()
        ) { isAllowed: Boolean? ->
            if (!isAllowed!!) handleNoNotificationPermission() else {
                val runnable = Tools.getWeakReference(mRequestNotificationPermissionRunnable)
                runnable?.run()
            }
        }
        window.setBackgroundDrawable(null)
        bindViews()
        checkNotificationPermission()
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        ProgressKeeper.addTaskCountListener(mDoubleLaunchPreventionListener)
        ProgressKeeper.addTaskCountListener(ProgressServiceKeeper(this).also {
            mProgressServiceKeeper = it
        })
        mSettingsButton!!.setOnClickListener(mSettingButtonListener)
        mDeleteAccountButton!!.setOnClickListener(mAccountDeleteButtonListener)
        ProgressKeeper.addTaskCountListener(mProgressLayout)
        ExtraCore.addExtraListener(ExtraConstants.BACK_PREFERENCE, mBackPreferenceListener)
        ExtraCore.addExtraListener(ExtraConstants.SELECT_AUTH_METHOD, mSelectAuthMethod)
        ExtraCore.addExtraListener(ExtraConstants.LAUNCH_GAME, mLaunchGameListener)
        AsyncVersionList().getVersionList({ versions: JMinecraftVersionList? ->
            ExtraCore.setValue(
                ExtraConstants.RELEASE_TABLE,
                versions
            )
        }, false)
        mInstallTracker = ModloaderInstallTracker(this)
        mProgressLayout!!.observe(ProgressLayout.DOWNLOAD_MINECRAFT)
        mProgressLayout!!.observe(ProgressLayout.UNPACK_RUNTIME)
        mProgressLayout!!.observe(ProgressLayout.INSTALL_MODPACK)
        mProgressLayout!!.observe(ProgressLayout.AUTHENTICATE_MICROSOFT)
        mProgressLayout!!.observe(ProgressLayout.DOWNLOAD_VERSION_LIST)

        MinecraftAssets(this).moveFiles("minecraft")
    }

    override fun onResume() {
        super.onResume()
        ContextExecutor.setActivity(this)
        mInstallTracker!!.attach()
    }

    override fun onPause() {
        super.onPause()
        ContextExecutor.clearActivity()
        mInstallTracker!!.detach()
    }

    override fun setFullscreen(): Boolean {
        return false
    }

    override fun onStart() {
        super.onStart()
        supportFragmentManager.registerFragmentLifecycleCallbacks(mFragmentCallbackListener, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        mProgressLayout!!.cleanUpObservers()
        ProgressKeeper.removeTaskCountListener(mProgressLayout)
        ProgressKeeper.removeTaskCountListener(mProgressServiceKeeper)
        ExtraCore.removeExtraListenerFromValue(
            ExtraConstants.BACK_PREFERENCE,
            mBackPreferenceListener
        )
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.SELECT_AUTH_METHOD, mSelectAuthMethod)
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.LAUNCH_GAME, mLaunchGameListener)
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(mFragmentCallbackListener)
    }

    /** Custom implementation to feel more natural when a backstack isn't present  */
    override fun onBackPressed() {
        val fragment = getVisibleFragment(MicrosoftLoginFragment.TAG) as MicrosoftLoginFragment?
        if (fragment != null) {
            if (fragment.canGoBack()) {
                fragment.goBack()
                return
            }
        }
        super.onBackPressed()
    }

    override fun onAttachedToWindow() {
        LauncherPreferences.computeNotchSize(this)
    }

    private fun getVisibleFragment(tag: String): Fragment? {
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        return if (fragment != null && fragment.isVisible) {
            fragment
        } else null
    }

    @Suppress("unused")
    private fun getVisibleFragment(id: Int): Fragment? {
        val fragment = supportFragmentManager.findFragmentById(id)
        return if (fragment != null && fragment.isVisible) {
            fragment
        } else null
    }

    private fun checkNotificationPermission() {
        if (LauncherPreferences.PREF_SKIP_NOTIFICATION_PERMISSION_CHECK ||
            checkForNotificationPermission()
        ) {
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
        ) {
            showNotificationPermissionReasoning()
            return
        }
        askForNotificationPermission(null)
    }

    private fun showNotificationPermissionReasoning() {
        AlertDialog.Builder(this)
            .setTitle(R.string.notification_permission_dialog_title)
            .setMessage(R.string.notification_permission_dialog_text)
            .setPositiveButton(android.R.string.ok) { d: DialogInterface?, w: Int ->
                askForNotificationPermission(
                    null
                )
            }
            .setNegativeButton(android.R.string.cancel) { d: DialogInterface?, w: Int -> handleNoNotificationPermission() }
            .show()
    }

    private fun handleNoNotificationPermission() {
        LauncherPreferences.PREF_SKIP_NOTIFICATION_PERMISSION_CHECK = true
        LauncherPreferences.DEFAULT_PREF.edit()
            .putBoolean(LauncherPreferences.PREF_KEY_SKIP_NOTIFICATION_CHECK, true)
            .apply()
        Toast.makeText(this, R.string.notification_permission_toast, Toast.LENGTH_LONG).show()
    }

    fun checkForNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_DENIED
    }

    fun askForNotificationPermission(onSuccessRunnable: Runnable?) {
        if (Build.VERSION.SDK_INT < 33) return
        if (onSuccessRunnable != null) {
            mRequestNotificationPermissionRunnable = WeakReference(onSuccessRunnable)
        }
        mRequestNotificationPermissionLauncher!!.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    /** Stuff all the view boilerplate here  */
    private fun bindViews() {
        mFragmentView = findViewById(R.id.container_fragment)
        mSettingsButton = findViewById(R.id.setting_button)
        mDeleteAccountButton = findViewById(R.id.delete_account_button)
        mAccountSpinner = findViewById(R.id.account_spinner)
        mProgressLayout = findViewById(R.id.progress_layout)
    }

    companion object {
        const val SETTING_FRAGMENT_TAG = "SETTINGS_FRAGMENT"
        private const val TAG = "LauncherActivity.java"
    }
}
