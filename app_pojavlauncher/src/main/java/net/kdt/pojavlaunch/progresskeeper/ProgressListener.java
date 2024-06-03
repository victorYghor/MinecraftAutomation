package net.kdt.pojavlaunch.progresskeeper;

/**
 *
 */
public interface ProgressListener {
    void onProgressStarted();
    void onProgressUpdated(int progress, String message, Object... va);
    void onProgressEnded();
}
