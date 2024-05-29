package com.kdt.mcgui;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.ProgressListener;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;

import java.util.ArrayList;

import pixelmon.Loading;


/** This is a custom layout created with pure java code to show the progress of the app
 * Class staring at specific values and automatically show something if the progress is present
 * Since progress is posted in a specific way, The packing/unpacking is handheld by the class
 *
 * This class relies on ExtraCore for its behavior.
 * You need a listener with you want update the progress bar
 */
public class ProgressLayout extends ConstraintLayout implements TaskCountListener{
    // aqui é basicamento o tipo de carregamento que existe dentro do app
    // Vê se ficar muito complexo essa parte de carregamento o que pode fazer é fazer um carregamento fake

    // These are the progress keys are the key of the progress in the app
    public static final String UNPACK_RUNTIME = "unpack_runtime";
    public static final String DOWNLOAD_MINECRAFT = "download_minecraft";
    public static final String DOWNLOAD_VERSION_LIST = "download_verlist";
    public static final String AUTHENTICATE_MICROSOFT = "authenticate_microsoft";
    public static final String INSTALL_MODPACK = "install_modpack";
    public static final String EXTRACT_COMPONENTS = "extract_components";
    public static final String EXTRACT_SINGLE_FILES = "extract_single_files";
    public static final String MOVING_FILES = "moving_files";

    //overloading constroctors
    public ProgressLayout(@NonNull Context context) {
        super(context);
        init();
    }
    public ProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public ProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    /**
     *
     */
    private final ArrayList<LayoutProgressListener> mMap = new ArrayList<>();

    // pixelmon
    private ProgressBar mProgressBarPixelmonHome;
//    private TextView mTaskNumberDisplayer;
    private TextView mTextLoading;

    /**
     * Aqui ele coloca as strings do inicio da classe
     * @param progressKey
     */
    public void observe(String progressKey){
        mMap.add(new LayoutProgressListener(progressKey));
    }

    public void cleanUpObservers() {
        for(LayoutProgressListener progressListener : mMap) {
            ProgressKeeper.removeListener(progressListener.progressKey, progressListener);
        }
    }

    public boolean hasProcesses(){
        return ProgressKeeper.getTaskCount() > 0;
    }


    /**
     * Applying styles to the progress viewer
     */
    private void init(){
        inflate(getContext(), R.layout.fragment_pixelmon_progress_bar, this);
//        mTaskNumberDisplayer = findViewById(R.id.tv_progress_text);
        mProgressBarPixelmonHome = findViewById(R.id.progress_bar_pixelmon_home);
        mTextLoading = findViewById(R.id.tv_progress_text);
        setVisibility(View.GONE);
    }


    /**
     *  Update the progress bar content
     * @param progressKey the key of the progress all the strings are in the start of the file.
     * @param progress the progress of the bar
     * */
    public static void setProgress(String progressKey, int progress){

        ProgressKeeper.submitProgress(progressKey, progress, -1, (Object)null);
    }
    /** Update the text and progress content */
    public static void setProgress(String progressKey, int progress, String message){
        setProgress(progressKey,progress, -1, message);
    }
    /** Update the text and progress content */
    public static void setProgress(String progressKey, int progress, @StringRes int resource, Object... message){
            ProgressKeeper.submitProgress(progressKey, progress, resource, message);
    }

    /** Update the text and progress content */
    public static void clearProgress(String progressKey){
        setProgress(progressKey, -1, -1);
    }

    /**
     * Preciso saber como eu posso adaptar isso daqui
     */
    @Override
    public void onUpdateTaskCount(int tc) {
        post(()->{
        });
    }

    /**
     * Inside this class I have the view that I will update,
     */
    class LayoutProgressListener implements ProgressListener {
        /**
         * Is one of the strings in the beginning of the file
         */
        final String progressKey;
        final ProgressBar progressBarPixelmon;
        final TextView textLoading;
        public LayoutProgressListener(String progressKey) {
            this.progressKey = progressKey;
            progressBarPixelmon = mProgressBarPixelmonHome;
            textLoading = mTextLoading;
            ProgressKeeper.addListener(progressKey, this);
        }

        @Override
        public void onProgressStarted() {
            post(()-> {
                setVisibility(View.VISIBLE);
                Log.i("ProgressLayout", "onProgressStarted");
            });
        }

        /**
         * O textViw é uma palavra ruim para se referenciar ao progresslayout com o um text view
         * da questão do progresso da barra é colocado aqui, texto da barra de progresso
         * @param progress
         * @param resid
         * @param va
         */
        @Override
        public void onProgressUpdated(int progress, int resid, Object... va) {
            post(()-> {
                progressBarPixelmon.setProgress(progress);
                textLoading.setText(resid == -1 ? "" : getContext().getString(resid, va));
            });
        }

        @Override
        public void onProgressEnded() {
            post(()-> {
                Log.d("ProgressLayout", "onProgressEnded");
                setVisibility(View.GONE);
                // call some function or class that trigger change in the layout for another progress or button like button play
                // I can do this here or inside the ProgressKeeper
                // temporary
                ExtraCore.setValue(ExtraConstants.LOADING_INTERNAL, Loading.SHOW_PLAY_BUTTON);
            });
        }
    }
}
