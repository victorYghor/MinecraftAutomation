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
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import com.kdt.mcgui.ProgressLayout
import com.kdt.mcgui.mcAccountSpinner
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.extra.ExtraListener
import net.kdt.pojavlaunch.fragments.MicrosoftLoginFragment
import net.kdt.pojavlaunch.fragments.PixelmonMenuFragment
import net.kdt.pojavlaunch.fragments.PixelmonWelcomeScreen
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
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftLauncherProfiles
import pixelmon.SocialMedia
import pixelmon.Tools.informativeAlertDialog
import timber.log.Timber
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
    private var mProgressLayout: ProgressLayout? = null
    private var mProgressServiceKeeper: ProgressServiceKeeper? = null
    private var mInstallTracker: ModloaderInstallTracker? = null
    private var mNotificationManager: NotificationManager? = null
    private var btnDiscord: ImageButton? = null
    private var btnOfficialSite: ImageButton? = null
    private var btnTiktok: ImageButton? = null
    private var btnSettings: ImageButton? = null
    private lateinit var btnPlay: Button


    //Pixelmon stuff
    private val mShowPlayButtonListener = ExtraListener { key: String?, value: Boolean ->
        // verificar qual o fragment atual na tela se não for PixelmonMenuFragment não mostrar o botão
        val pixelmonMenuFragment: PixelmonMenuFragment? =
            supportFragmentManager.findFragmentByTag("PixelmonMenuFragment") as PixelmonMenuFragment?
        if (pixelmonMenuFragment != null && pixelmonMenuFragment.isVisible()) {
            if(value) {
                btnPlay.visibility = View.VISIBLE
            } else {
                btnPlay.visibility = View.GONE
            }
        }
        false
    }

    /**
     * @param value in this list you put the string resource for title for index 0 and the string
     * resource for description for the index 1 the pop up.
     */
    private val mDialogAlertDownload = ExtraListener { key: String?, value: List<Int> ->
       informativeAlertDialog(this, value[0], value[1])
        false
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
        ) as? PixelmonMenuFragment ?: return@ExtraListener false
        // Allow starting the add account only from the main menu, should it be moved to fragment itself ?
        Tools.swapFragment(this, SelectAuthFragment::class.java, SelectAuthFragment.TAG, true, null)
        false
    }

    /* Listener for the settings fragment */
    private val mSettingButtonListener = View.OnClickListener { v: View? ->
        val fragment = supportFragmentManager.findFragmentById(
            mFragmentView!!.id
        )
        if (fragment is PixelmonMenuFragment) {
            Tools.swapFragment(
                this,
                LauncherPreferenceFragment::class.java,
                SETTING_FRAGMENT_TAG,
                true,
                null
            )
        } else {
            // The setting button doubles as a home button now
            while (supportFragmentManager.findFragmentById(mFragmentView!!.id) !is PixelmonMenuFragment) {
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
            Timber.d("tarefas em andamento")
            Toast.makeText(this, R.string.tasks_ongoing, Toast.LENGTH_LONG).show()
            return@ExtraListener false
        }
        // o profile é como se fosse a versão do minecraft ou do forge que vai ser instalada aqui
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
        Log.i(TAG, "The current profile is $prof, and the select profile is $selectedProfile")
        if (prof?.lastVersionId == null || "Unknown" == prof.lastVersionId) {
            Toast.makeText(this, R.string.error_no_version, Toast.LENGTH_LONG).show()
            return@ExtraListener false
        }

        if (mAccountSpinner?.selectedAccount == null) {
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
        val viewModel by viewModels<LauncherViewModel> {
            LauncherViewModel.provideFactory(this, this)
        }
        LauncherPreferences.loadPreferences(this)

        setContentView(R.layout.pixelmon_main_activity)
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

        // here you start the views stuff
        bindViews()
        val firsInstallation = LauncherPreferences.PREF_FIRST_INSTALLATION
        Log.d(PixelmonMenuFragment.TAG, "the first installation is $firsInstallation")
        if (firsInstallation) {
            LauncherPreferences.DEFAULT_PREF.edit().putBoolean("first_installation", false).commit()
            // Aqui entra dentro da tela de boas vindas
            setBottomButtonsVisibility(false)
            Tools.swapFragment(
                this,
                PixelmonWelcomeScreen::class.java,
                PixelmonWelcomeScreen.TAG,
                false,
                null
            )
        } else {
            Tools.swapFragment(
                this,
                PixelmonMenuFragment::class.java,
                PixelmonMenuFragment.TAG,
                false,
                null
            )
        }

        // change the visibility of the buttons
        val bottomButtonsVisibilityObserver = Observer<Boolean> { visible: Boolean ->
            setBottomButtonsVisibility(visible)
        }
        viewModel.bottomButtonsVisible.observe(this, bottomButtonsVisibilityObserver)
//        viewModel.bottomButtonsVisibility.value = true


        // pixelmon buttons
        btnDiscord?.setOnClickListener {
            startActivity(SocialMedia.DISCORD.open)
        } ?: Log.d(TAG, "btnDiscord is null")
        btnOfficialSite?.setOnClickListener {
            startActivity(SocialMedia.OFFICIAL_SITE.open)
        }
        btnTiktok?.setOnClickListener {
            startActivity(SocialMedia.TIK_TOK.open)
        }
        btnSettings?.setOnClickListener {
            Tools.swapFragment(
                this,
                LauncherPreferenceFragment::class.java,
                LauncherActivity.SETTING_FRAGMENT_TAG,
                true,
                null
            )
        }
        btnPlay.setOnClickListener {
            ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true)
        }

        checkNotificationPermission()
        // place for putting extra listener
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        ProgressKeeper.addTaskCountListener(mDoubleLaunchPreventionListener)
        ProgressKeeper.addTaskCountListener(ProgressServiceKeeper(this).also {
            mProgressServiceKeeper = it
        })
        ProgressKeeper.addTaskCountListener(mProgressLayout)

        //Pixelmon stuff
        ExtraCore.addExtraListener(ExtraConstants.ALERT_DIALOG_DOWNLOAD, mDialogAlertDownload)
        ExtraCore.addExtraListener(ExtraConstants.SHOW_PLAY_BUTTON, mShowPlayButtonListener)

        ExtraCore.addExtraListener(ExtraConstants.BACK_PREFERENCE, mBackPreferenceListener)
        ExtraCore.addExtraListener(ExtraConstants.SELECT_AUTH_METHOD, mSelectAuthMethod)
        ExtraCore.addExtraListener(ExtraConstants.LAUNCH_GAME, mLaunchGameListener)
        AsyncVersionList().getVersionList({ versions: JMinecraftVersionList? ->
            ExtraCore.setValue(
                ExtraConstants.RELEASE_TABLE,
                versions
            )
        }, false)
        // progressLayout
        mInstallTracker = ModloaderInstallTracker(this)
        mProgressLayout!!.observe(ProgressLayout.DOWNLOAD_MINECRAFT)
//        mProgressLayout!!.observe(ProgressLayout.UNPACK_RUNTIME)
        mProgressLayout!!.observe(ProgressLayout.INSTALL_MODPACK)
        mProgressLayout!!.observe(ProgressLayout.AUTHENTICATE_MICROSOFT)
        mProgressLayout!!.observe(ProgressLayout.DOWNLOAD_VERSION_LIST)
        mProgressLayout!!.observe(ProgressLayout.MOVING_FILES)
        mProgressLayout!!.observe(ProgressLayout.DOWNLOAD_MOD_ONE_DOT_TWELVE)
        LauncherPreferences.loadPreferences(this)

        insertProfiles()
    }

    /**
     * Insert the profiles from the assets to the game folder
     */
    private fun insertProfiles() {
        val profiles = this.assets.open("launcher_profiles.json").readBytes()
        Tools.write(
            Tools.DIR_GAME_NEW + "/" + "launcher_profiles.json", profiles
        )
        LauncherProfiles.mainProfileJson = Tools.GLOBAL_GSON.fromJson(
            Tools.read(LauncherProfiles.launcherProfilesFile.absolutePath),
            MinecraftLauncherProfiles::class.java
        )
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
        mAccountSpinner = findViewById(R.id.account_spinner)
        mProgressLayout = findViewById(R.id.progress_layout)

        // pixelmon buttons
        btnDiscord = findViewById(R.id.btn_discord)
        btnSettings = findViewById(R.id.btn_settings)
        btnTiktok = findViewById(R.id.btn_tiktok)
        btnOfficialSite = findViewById(R.id.btn_official_site)
        btnPlay = findViewById(R.id.btn_play)
    }
    fun setBottomButtonsVisibility(visible: Boolean) {
        // The btnPlay and the ProgressLayout is set automatically
        if(visible) {
            btnDiscord?.visibility = View.VISIBLE
            btnSettings?.visibility = View.VISIBLE
            btnTiktok?.visibility = View.VISIBLE
            btnOfficialSite?.visibility = View.VISIBLE
        } else {
            ExtraCore.setValue(ExtraConstants.SHOW_PLAY_BUTTON, false)
            btnDiscord?.visibility = View.GONE
            btnSettings?.visibility = View.GONE
            btnTiktok?.visibility = View.GONE
            btnOfficialSite?.visibility = View.GONE
        }
    }

    companion object {
        const val SETTING_FRAGMENT_TAG = "SETTINGS_FRAGMENT"
        private const val TAG = "LauncherActivity.java"
    }

}
