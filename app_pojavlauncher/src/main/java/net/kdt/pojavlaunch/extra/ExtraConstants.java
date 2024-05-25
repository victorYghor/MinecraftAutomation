package net.kdt.pojavlaunch.extra;

public class ExtraConstants {
    // pixelmon
    /* ExtraCore constant: a HashMap for converting values such as latest-snapshot or latest-release to actual game version names */
    /** for trigger the alert dialog about some download problem **/
    public static final String ALERT_DIALOG_DOWNLOAD = "alert_dialog_download";
    public static final String LOADING_INTERNAL = "loading_internal";
    public static final String ONE_DOT_SIXTEEN_DOWNLOAD_RESULT  = "one_dot_sixteen_download_result";
    public static final String RELEASE_TABLE = "release_table";
    /** ExtraCore constant: Serpent's back button tracking thing */
    public static final String BACK_PREFERENCE = "back_preference";
    /** ExtraCore constant: The OPENGL version that should be exposed */
    public static final String OPEN_GL_VERSION = "open_gl_version";
    /** ExtraCore constant: When the microsoft authentication via webview is done */
    public static final String MICROSOFT_LOGIN_TODO = "webview_login_done";
    /** ExtraCore constant: Mojang or "local" authentication to perform */
    public static final String MOJANG_LOGIN_TODO = "mojang_login_todo";
    /** ExtraCore constant: Add minecraft account procedure, the user has to select between mojang or microsoft */
    public static final String SELECT_AUTH_METHOD = "start_login_procedure";
    /** ExtraCore constant: Selected file or folder, as a String */
    public static final String FILE_SELECTOR = "file_selector";
    /** ExtraCore constant: Need to refresh the version spinner, selecting the uuid at the same time. Can be DELETED_PROFILE */
    public static final String REFRESH_VERSION_SPINNER = "refresh_version";
    /** ExtraCore Constant: When we want to launch the game */
    public static final String LAUNCH_GAME = "launch_game";
}
