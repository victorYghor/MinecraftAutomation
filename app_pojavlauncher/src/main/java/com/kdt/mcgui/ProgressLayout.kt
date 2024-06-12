package com.kdt.mcgui

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.progresskeeper.ProgressListener
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener
import pixelmon.Tools.Timberly
import timber.log.Timber

/**
 * This is a custom layout created with pure java code to show the progress of the app
 * Class staring at specific values and automatically show something if the progress is present
 * Since progress is posted in a specific way, The packing/unpacking is handheld by the class
 *
 *
 * This class relies on ExtraCore for its behavior.
 * You need a listener with you want update the progress bar
 */
class ProgressLayout : ConstraintLayout, TaskCountListener {
    //overloading constroctors
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    /**
     *
     */
    private val mMap = ArrayList<LayoutProgressListener>()

    // pixelmon
    private var mProgressBarPixelmonHome: ProgressBar? = null

    //    private TextView mTaskNumberDisplayer;
    private var mTextLoading: TextView? = null

    /**
     * Aqui ele coloca as strings do inicio da classe
     *
     * @param progressKey
     */
    fun observe(progressKey: String?) {
        mMap.add(LayoutProgressListener(progressKey))
    }

    fun cleanUpObservers() {
        for (progressListener in mMap) {
            ProgressKeeper.removeListener(progressListener.progressKey, progressListener)
        }
    }

    fun hasProcesses(): Boolean {
        return ProgressKeeper.getTaskCount() > 0
    }

    /**
     * Applying styles to the progress viewer
     */
    private fun init() {
        inflate(context, R.layout.fragment_pixelmon_progress_bar, this)
        //        mTaskNumberDisplayer = findViewById(R.id.tv_progress_text);
        mProgressBarPixelmonHome = findViewById(R.id.progress_bar_pixelmon_home)
        mTextLoading = findViewById(R.id.tv_progress_text)
        visibility = GONE
    }

    /**
     * Preciso saber como eu posso adaptar isso daqui
     */
    override fun onUpdateTaskCount(tc: Int) {
        post {}
    }

    /**
     * Inside this class I have the view that I will update,
     * @property progressKey is the key of the progress
     */
    internal inner class LayoutProgressListener(
        val progressKey: String?
    ) : ProgressListener {
        private val progressBarPixelmon = mProgressBarPixelmonHome
        private val textLoading: TextView? = mTextLoading

        init {
            ProgressKeeper.addListener(progressKey, this)
        }

        override fun onProgressStarted() {
            post {
                ExtraCore.setValue(ExtraConstants.SHOW_PLAY_BUTTON, false)
                visibility = VISIBLE
                Timber.tag(Timberly.downloadProblem).d("onProgressStarted")
            }
        }

        /**
         *
         * @param progress
         * @param message
         * @param va
         */
        override fun onProgressUpdated(progress: Int, message: String, vararg va: Any) {
            Timber.tag(Timberly.downloadProblem).d("the progress is $progress with the message $message ")
            post {
                progressBarPixelmon!!.progress = progress
                textLoading!!.text = message
            }
        }

        override fun onProgressEnded() {
            post {
                Timber.tag(Timberly.downloadProblem).d("onProgressEnded")
                visibility = GONE
                // call some function or class that trigger change in the layout for another progress or button like button play
                // I can do this here or inside the ProgressKeeper
                // temporary

                ExtraCore.setValue(ExtraConstants.SHOW_PLAY_BUTTON, true)
            }
        }
    }

    companion object {
        // aqui é basicamento o tipo de carregamento que existe dentro do app
        // These are the progress keys are the key of the progress in the app
        const val UNPACK_RUNTIME = "unpack_runtime"
        const val EXTRACT_COMPONENTS = "extract_components"
        const val EXTRACT_SINGLE_FILES = "extract_single_files"

        const val DOWNLOAD_MINECRAFT = "download_minecraft"
        const val DOWNLOAD_VERSION_LIST = "download_verlist"
        const val AUTHENTICATE_MICROSOFT = "authenticate_microsoft"
        const val INSTALL_MODPACK = "install_modpack"
        const val MOVING_FILES = "moving_files"
        const val DOWNLOAD_MOD_ONE_DOT_TWELVE = "download_mods"

        /**
         * Update the progress bar content
         *
         * @param progressKey the key of the progress all the strings are in the start of the file.
         * @param progress    the progress of the bar
         */
        @JvmStatic
        fun setProgress(progressKey: String?, progress: Int) {
            ProgressKeeper.submitProgress(progressKey, progress, "", null as Any?)
        }

        /**
         * Update the text and progress content
         */
        @JvmStatic
        fun setProgress(progressKey: String?, progress: Int, message: String?) {
            setProgress(progressKey, progress, message, "")
        }

        /**
         * Update the text and progress content
         */
        @JvmStatic
        fun setProgress(
            progressKey: String?,
            progress: Int,
            messageString: String?,
            vararg message: Any?
        ) {
            Timber.tag(Timberly.downloadProblem).d("setProgress was called with $progressKey, $progress, $messageString, $message")
            ProgressKeeper.submitProgress(progressKey, progress, messageString, *message)
        }

        /**
         * Update the text and progress content
         */
        @JvmStatic
        fun clearProgress(progressKey: String?) {
            Timber.tag(Timberly.downloadProblem).d("clearing progress")
            setProgress(progressKey, 100, "")
        }
    }
}
