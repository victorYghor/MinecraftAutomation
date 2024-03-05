package net.kdt.pojavlaunch.tasks;


import static net.kdt.pojavlaunch.Architecture.archAsString;
import static net.kdt.pojavlaunch.PojavApplication.sExecutorService;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AsyncAssetManager {

    private AsyncAssetManager(){}

    /**
     * Attempt to install the java 8 runtime, if necessary
     * @param am App context
     */
    public static void unpackRuntime(AssetManager am) {
        /* Check if JRE is included */
        String rt_version = null;
        String current_rt_version = MultiRTUtils.__internal__readBinpackVersion("Internal");
        try {
            rt_version = Tools.read(am.open("components/jre/version"));
        } catch (IOException e) {
            Log.e("JREAuto", "JRE was not included on this APK.", e);
        }
        String exactJREName = MultiRTUtils.getExactJreName(8);
        if(current_rt_version == null && exactJREName != null && !exactJREName.equals("Internal")/*this clause is for when the internal runtime is goofed*/) return;
        if(rt_version == null) return;
        if(rt_version.equals(current_rt_version)) return;

        // Install the runtime in an async manner, hope for the best
        String finalRt_version = rt_version;
        sExecutorService.execute(() -> {

            try {
                MultiRTUtils.installRuntimeNamedBinpack(
                        am.open("components/jre/universal.tar.xz"),
                        am.open("components/jre/bin-" + archAsString(Tools.DEVICE_ARCHITECTURE) + ".tar.xz"),
                        "Internal", finalRt_version);
                MultiRTUtils.postPrepare("Internal");
            }catch (IOException e) {
                Log.e("JREAuto", "Internal JRE unpack failed", e);
            }
        });
    }

    /** Unpack single files, with no regard to version tracking */
    public static void unpackSingleFiles(Context ctx){
        ProgressLayout.setProgress(ProgressLayout.EXTRACT_SINGLE_FILES, 0);
        sExecutorService.execute(() -> {
            try {
                Tools.copyAssetFile(ctx, "options.txt", Tools.DIR_GAME_NEW, false);
                Tools.copyAssetFile(ctx, "default.json", Tools.CTRLMAP_PATH, true);
                Tools.write(Tools.CTRLMAP_PATH, "{\n" +
                        "  \"mControlDataList\": [\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${margin} * 4 + ${width} * 3\",\n" +
                        "      \"dynamicY\": \"${margin}\",\n" +
                        "      \"height\": 29.629631,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": true,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        -1,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"Teclado\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 80\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${screen_width} - ${width}\",\n" +
                        "      \"dynamicY\": \"0.001853565 * ${screen_height}\",\n" +
                        "      \"height\": 29.629631,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": false,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        -2,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"GUI\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 80\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${right} - ${margin} * 2 - ${width} * 2.25\",\n" +
                        "      \"dynamicY\": \"${bottom} - ${margin} * 2 - ${height} * 2.25\",\n" +
                        "      \"height\": 49.10053,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": true,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        -3,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"PRI\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 49.10053\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${right} - ${margin} * 2 - ${width} * 2.25\",\n" +
                        "      \"dynamicY\": \"${bottom} - ${margin} * 2 - ${height} * 3.50\",\n" +
                        "      \"height\": 49.10053,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": true,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        -4,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"SEC\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 49.10053\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${screen_width} - ${width}\",\n" +
                        "      \"dynamicY\": \"${height} + ${margin}\",\n" +
                        "      \"height\": 29.629631,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": false,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        -5,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"Mouse\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 80\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${margin} * 2 + ${width}\",\n" +
                        "      \"dynamicY\": \"${margin}\",\n" +
                        "      \"height\": 29.629631,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": true,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        292,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"F3\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 80\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${margin}\",\n" +
                        "      \"dynamicY\": \"${margin}\",\n" +
                        "      \"height\": 29.629631,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": true,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        256,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"ESC\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 80\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${margin} * 3 + ${width} * 2\",\n" +
                        "      \"dynamicY\": \"${margin}\",\n" +
                        "      \"height\": 29.629631,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": true,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        84,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"Chat\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 80\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${margin} * 5 + ${width} * 4\",\n" +
                        "      \"dynamicY\": \"${margin}\",\n" +
                        "      \"height\": 29.629631,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": true,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        258,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"Tab\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 80\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${margin}\",\n" +
                        "      \"dynamicY\": \"${height} + ${margin}\",\n" +
                        "      \"height\": 29.629631,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": true,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        294,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"F5\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 80\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${margin} * 2 + ${width}\",\n" +
                        "      \"dynamicY\": \"${height} + ${margin}\",\n" +
                        "      \"height\": 29.629631,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": true,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        69,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"Inventário\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 80\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${right} - ${margin} * 2 - ${width} * 2.25\",\n" +
                        "      \"dynamicY\": \"${bottom} - ${margin} * 2 - ${height}\",\n" +
                        "      \"height\": 49.10053,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": true,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": true,\n" +
                        "      \"keycodes\": [\n" +
                        "        340,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"◇\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 49.10053\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${right} - ${margin} * 2 - ${width}\",\n" +
                        "      \"dynamicY\": \"${bottom} - ${margin} * 2 - ${height}\",\n" +
                        "      \"height\": 49.10053,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": true,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        32,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"⬛\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 49.10053\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${right} - ${margin} * 2 - ${width}\",\n" +
                        "      \"dynamicY\": \"${screen_height} - ${margin} * 4 - ${height} * 4\",\n" +
                        "      \"height\": 49.10053,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": true,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        264,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"▼\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 49.10053\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${right} - ${margin} * 2 - ${width}\",\n" +
                        "      \"dynamicY\": \"${screen_height} - ${margin} * 5 - ${height} * 5\",\n" +
                        "      \"height\": 49.10053,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": true,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        82,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"R\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 49.10053\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0,\n" +
                        "      \"dynamicX\": \"${right} - ${margin} * 2 - ${width}\",\n" +
                        "      \"dynamicY\": \"${screen_height} - ${margin} * 6 - ${height} * 6\",\n" +
                        "      \"height\": 49.10053,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": true,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        265,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"▲\",\n" +
                        "      \"opacity\": 1,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0,\n" +
                        "      \"width\": 49.10053\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"mDrawerDataList\": [\n" +
                        "    {\n" +
                        "      \"buttonProperties\": [\n" +
                        "        {\n" +
                        "          \"bgColor\": 1291845632,\n" +
                        "          \"cornerRadius\": 0,\n" +
                        "          \"dynamicX\": \"0.0832176 * ${screen_width}\",\n" +
                        "          \"dynamicY\": \"0.24814814 * ${screen_height}\",\n" +
                        "          \"height\": 49.10053,\n" +
                        "          \"isDynamicBtn\": false,\n" +
                        "          \"isHideable\": true,\n" +
                        "          \"isSwipeable\": false,\n" +
                        "          \"isToggle\": false,\n" +
                        "          \"keycodes\": [\n" +
                        "            90,\n" +
                        "            0,\n" +
                        "            0,\n" +
                        "            0\n" +
                        "          ],\n" +
                        "          \"name\": \"Waypoint\",\n" +
                        "          \"opacity\": 1,\n" +
                        "          \"passThruEnabled\": false,\n" +
                        "          \"strokeColor\": -1,\n" +
                        "          \"strokeWidth\": 0,\n" +
                        "          \"width\": 80\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"bgColor\": 1291845632,\n" +
                        "          \"cornerRadius\": 0,\n" +
                        "          \"dynamicX\": \"0.1647517 * ${screen_width}\",\n" +
                        "          \"dynamicY\": \"0.24814814 * ${screen_height}\",\n" +
                        "          \"height\": 49.10053,\n" +
                        "          \"isDynamicBtn\": false,\n" +
                        "          \"isHideable\": true,\n" +
                        "          \"isSwipeable\": false,\n" +
                        "          \"isToggle\": false,\n" +
                        "          \"keycodes\": [\n" +
                        "            74,\n" +
                        "            0,\n" +
                        "            0,\n" +
                        "            0\n" +
                        "          ],\n" +
                        "          \"name\": \"Mapa\",\n" +
                        "          \"opacity\": 1,\n" +
                        "          \"passThruEnabled\": false,\n" +
                        "          \"strokeColor\": -1,\n" +
                        "          \"strokeWidth\": 0,\n" +
                        "          \"width\": 80\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"bgColor\": 1291845632,\n" +
                        "          \"cornerRadius\": 0,\n" +
                        "          \"dynamicX\": \"0.2462858 * ${screen_width}\",\n" +
                        "          \"dynamicY\": \"0.24814814 * ${screen_height}\",\n" +
                        "          \"height\": 49.10053,\n" +
                        "          \"isDynamicBtn\": false,\n" +
                        "          \"isHideable\": true,\n" +
                        "          \"isSwipeable\": false,\n" +
                        "          \"isToggle\": false,\n" +
                        "          \"keycodes\": [\n" +
                        "            341,\n" +
                        "            74,\n" +
                        "            0,\n" +
                        "            0\n" +
                        "          ],\n" +
                        "          \"name\": \"Ativar\",\n" +
                        "          \"opacity\": 1,\n" +
                        "          \"passThruEnabled\": false,\n" +
                        "          \"strokeColor\": -1,\n" +
                        "          \"strokeWidth\": 0,\n" +
                        "          \"width\": 80\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"bgColor\": 1291845632,\n" +
                        "          \"cornerRadius\": 0,\n" +
                        "          \"dynamicX\": \"0.32781988 * ${screen_width}\",\n" +
                        "          \"dynamicY\": \"0.24814814 * ${screen_height}\",\n" +
                        "          \"height\": 49.10053,\n" +
                        "          \"isDynamicBtn\": false,\n" +
                        "          \"isHideable\": true,\n" +
                        "          \"isSwipeable\": false,\n" +
                        "          \"isToggle\": false,\n" +
                        "          \"keycodes\": [\n" +
                        "            334,\n" +
                        "            0,\n" +
                        "            0,\n" +
                        "            0\n" +
                        "          ],\n" +
                        "          \"name\": \"+ Zoom\",\n" +
                        "          \"opacity\": 1,\n" +
                        "          \"passThruEnabled\": false,\n" +
                        "          \"strokeColor\": -1,\n" +
                        "          \"strokeWidth\": 0,\n" +
                        "          \"width\": 80\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"bgColor\": 1291845632,\n" +
                        "          \"cornerRadius\": 0,\n" +
                        "          \"dynamicX\": \"0.40935394 * ${screen_width}\",\n" +
                        "          \"dynamicY\": \"0.24814814 * ${screen_height}\",\n" +
                        "          \"height\": 49.10053,\n" +
                        "          \"isDynamicBtn\": false,\n" +
                        "          \"isHideable\": true,\n" +
                        "          \"isSwipeable\": false,\n" +
                        "          \"isToggle\": false,\n" +
                        "          \"keycodes\": [\n" +
                        "            45,\n" +
                        "            0,\n" +
                        "            0,\n" +
                        "            0\n" +
                        "          ],\n" +
                        "          \"name\": \"- Zoom\",\n" +
                        "          \"opacity\": 1,\n" +
                        "          \"passThruEnabled\": false,\n" +
                        "          \"strokeColor\": -1,\n" +
                        "          \"strokeWidth\": 0,\n" +
                        "          \"width\": 80\n" +
                        "        }\n" +
                        "      ],\n" +
                        "      \"orientation\": \"RIGHT\",\n" +
                        "      \"properties\": {\n" +
                        "        \"bgColor\": 1291845632,\n" +
                        "        \"cornerRadius\": 0,\n" +
                        "        \"dynamicX\": \"${margin}\",\n" +
                        "        \"dynamicY\": \"${height} * 3.25 + ${margin}  - (${height} + ${margin})\",\n" +
                        "        \"height\": 49.10053,\n" +
                        "        \"isDynamicBtn\": false,\n" +
                        "        \"isHideable\": true,\n" +
                        "        \"isSwipeable\": false,\n" +
                        "        \"isToggle\": false,\n" +
                        "        \"keycodes\": [\n" +
                        "          0,\n" +
                        "          0,\n" +
                        "          0,\n" +
                        "          0\n" +
                        "        ],\n" +
                        "        \"name\": \"Minimapa ->\",\n" +
                        "        \"opacity\": 1,\n" +
                        "        \"passThruEnabled\": false,\n" +
                        "        \"strokeColor\": -1,\n" +
                        "        \"strokeWidth\": 0,\n" +
                        "        \"width\": 80\n" +
                        "      }\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"buttonProperties\": [\n" +
                        "        {\n" +
                        "          \"bgColor\": 1291845632,\n" +
                        "          \"cornerRadius\": 0,\n" +
                        "          \"dynamicX\": \"0.0832176 * ${screen_width}\",\n" +
                        "          \"dynamicY\": \"0.13703704 * ${screen_height}\",\n" +
                        "          \"height\": 49.10053,\n" +
                        "          \"isDynamicBtn\": false,\n" +
                        "          \"isHideable\": true,\n" +
                        "          \"isSwipeable\": false,\n" +
                        "          \"isToggle\": false,\n" +
                        "          \"keycodes\": [\n" +
                        "            66,\n" +
                        "            0,\n" +
                        "            0,\n" +
                        "            0\n" +
                        "          ],\n" +
                        "          \"name\": \"B\",\n" +
                        "          \"opacity\": 1,\n" +
                        "          \"passThruEnabled\": false,\n" +
                        "          \"strokeColor\": -1,\n" +
                        "          \"strokeWidth\": 0,\n" +
                        "          \"width\": 80\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"bgColor\": 1291845632,\n" +
                        "          \"cornerRadius\": 0,\n" +
                        "          \"dynamicX\": \"0.1647517 * ${screen_width}\",\n" +
                        "          \"dynamicY\": \"0.13703704 * ${screen_height}\",\n" +
                        "          \"height\": 49.10053,\n" +
                        "          \"isDynamicBtn\": false,\n" +
                        "          \"isHideable\": true,\n" +
                        "          \"isSwipeable\": false,\n" +
                        "          \"isToggle\": false,\n" +
                        "          \"keycodes\": [\n" +
                        "            71,\n" +
                        "            0,\n" +
                        "            0,\n" +
                        "            0\n" +
                        "          ],\n" +
                        "          \"name\": \"G\",\n" +
                        "          \"opacity\": 1,\n" +
                        "          \"passThruEnabled\": false,\n" +
                        "          \"strokeColor\": -1,\n" +
                        "          \"strokeWidth\": 0,\n" +
                        "          \"width\": 80\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"bgColor\": 1291845632,\n" +
                        "          \"cornerRadius\": 0,\n" +
                        "          \"dynamicX\": \"0.2462858 * ${screen_width}\",\n" +
                        "          \"dynamicY\": \"0.13703704 * ${screen_height}\",\n" +
                        "          \"height\": 49.10053,\n" +
                        "          \"isDynamicBtn\": false,\n" +
                        "          \"isHideable\": true,\n" +
                        "          \"isSwipeable\": false,\n" +
                        "          \"isToggle\": false,\n" +
                        "          \"keycodes\": [\n" +
                        "            73,\n" +
                        "            0,\n" +
                        "            0,\n" +
                        "            0\n" +
                        "          ],\n" +
                        "          \"name\": \"Pokedex\",\n" +
                        "          \"opacity\": 1,\n" +
                        "          \"passThruEnabled\": false,\n" +
                        "          \"strokeColor\": -1,\n" +
                        "          \"strokeWidth\": 0,\n" +
                        "          \"width\": 80\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"bgColor\": 1291845632,\n" +
                        "          \"cornerRadius\": 0,\n" +
                        "          \"dynamicX\": \"0.32781988 * ${screen_width}\",\n" +
                        "          \"dynamicY\": \"0.13703704 * ${screen_height}\",\n" +
                        "          \"height\": 49.10053,\n" +
                        "          \"isDynamicBtn\": false,\n" +
                        "          \"isHideable\": true,\n" +
                        "          \"isSwipeable\": false,\n" +
                        "          \"isToggle\": false,\n" +
                        "          \"keycodes\": [\n" +
                        "            298,\n" +
                        "            0,\n" +
                        "            0,\n" +
                        "            0\n" +
                        "          ],\n" +
                        "          \"name\": \"Encerrar\\nBatalha\",\n" +
                        "          \"opacity\": 1,\n" +
                        "          \"passThruEnabled\": false,\n" +
                        "          \"strokeColor\": -1,\n" +
                        "          \"strokeWidth\": 0,\n" +
                        "          \"width\": 80\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"bgColor\": 1291845632,\n" +
                        "          \"cornerRadius\": 0,\n" +
                        "          \"dynamicX\": \"0.40935394 * ${screen_width}\",\n" +
                        "          \"dynamicY\": \"0.13703704 * ${screen_height}\",\n" +
                        "          \"height\": 49.10053,\n" +
                        "          \"isDynamicBtn\": false,\n" +
                        "          \"isHideable\": true,\n" +
                        "          \"isSwipeable\": false,\n" +
                        "          \"isToggle\": false,\n" +
                        "          \"keycodes\": [\n" +
                        "            85,\n" +
                        "            0,\n" +
                        "            0,\n" +
                        "            0\n" +
                        "          ],\n" +
                        "          \"name\": \"Card\",\n" +
                        "          \"opacity\": 1,\n" +
                        "          \"passThruEnabled\": false,\n" +
                        "          \"strokeColor\": -1,\n" +
                        "          \"strokeWidth\": 0,\n" +
                        "          \"width\": 80\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"bgColor\": 1291845632,\n" +
                        "          \"cornerRadius\": 0,\n" +
                        "          \"dynamicX\": \"0.57043356 * ${screen_width} - ${width}\",\n" +
                        "          \"dynamicY\": \"0.13703704 * ${screen_height}\",\n" +
                        "          \"height\": 49.10053,\n" +
                        "          \"isDynamicBtn\": false,\n" +
                        "          \"isHideable\": true,\n" +
                        "          \"isSwipeable\": false,\n" +
                        "          \"isToggle\": false,\n" +
                        "          \"keycodes\": [\n" +
                        "            89,\n" +
                        "            0,\n" +
                        "            0,\n" +
                        "            0\n" +
                        "          ],\n" +
                        "          \"name\": \"Busca PC\",\n" +
                        "          \"opacity\": 1,\n" +
                        "          \"passThruEnabled\": false,\n" +
                        "          \"strokeColor\": -1,\n" +
                        "          \"strokeWidth\": 0,\n" +
                        "          \"width\": 80\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"bgColor\": 1291845632,\n" +
                        "          \"cornerRadius\": 0,\n" +
                        "          \"dynamicX\": \"0.65196764 * ${screen_width} - ${width}\",\n" +
                        "          \"dynamicY\": \"0.13703704 * ${screen_height}\",\n" +
                        "          \"height\": 49.10053,\n" +
                        "          \"isDynamicBtn\": false,\n" +
                        "          \"isHideable\": true,\n" +
                        "          \"isSwipeable\": false,\n" +
                        "          \"isToggle\": false,\n" +
                        "          \"keycodes\": [\n" +
                        "            79,\n" +
                        "            0,\n" +
                        "            0,\n" +
                        "            0\n" +
                        "          ],\n" +
                        "          \"name\": \"Esconder\",\n" +
                        "          \"opacity\": 1,\n" +
                        "          \"passThruEnabled\": false,\n" +
                        "          \"strokeColor\": -1,\n" +
                        "          \"strokeWidth\": 0,\n" +
                        "          \"width\": 80\n" +
                        "        }\n" +
                        "      ],\n" +
                        "      \"orientation\": \"RIGHT\",\n" +
                        "      \"properties\": {\n" +
                        "        \"bgColor\": 1291845632,\n" +
                        "        \"cornerRadius\": 0,\n" +
                        "        \"dynamicX\": \"${margin}\",\n" +
                        "        \"dynamicY\": \"${height} * 2.25 + ${margin} - (${height} + ${margin})\",\n" +
                        "        \"height\": 49.10053,\n" +
                        "        \"isDynamicBtn\": false,\n" +
                        "        \"isHideable\": true,\n" +
                        "        \"isSwipeable\": false,\n" +
                        "        \"isToggle\": false,\n" +
                        "        \"keycodes\": [\n" +
                        "          0,\n" +
                        "          0,\n" +
                        "          0,\n" +
                        "          0\n" +
                        "        ],\n" +
                        "        \"name\": \"Pokemon ->\",\n" +
                        "        \"opacity\": 1,\n" +
                        "        \"passThruEnabled\": false,\n" +
                        "        \"strokeColor\": -1,\n" +
                        "        \"strokeWidth\": 0,\n" +
                        "        \"width\": 80\n" +
                        "      }\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"mJoystickDataList\": [\n" +
                        "    {\n" +
                        "      \"bgColor\": 1291845632,\n" +
                        "      \"cornerRadius\": 0.0,\n" +
                        "      \"displayInGame\": true,\n" +
                        "      \"displayInMenu\": true,\n" +
                        "      \"dynamicX\": \"${screen_width} * 0.026\",\n" +
                        "      \"dynamicY\": \"0.94 * ${screen_height} - ${height}\",\n" +
                        "      \"height\": 208.38095,\n" +
                        "      \"isDynamicBtn\": false,\n" +
                        "      \"isHideable\": true,\n" +
                        "      \"isSwipeable\": false,\n" +
                        "      \"isToggle\": false,\n" +
                        "      \"keycodes\": [\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0,\n" +
                        "        0\n" +
                        "      ],\n" +
                        "      \"name\": \"button\",\n" +
                        "      \"opacity\": 1.0,\n" +
                        "      \"passThruEnabled\": false,\n" +
                        "      \"strokeColor\": -1,\n" +
                        "      \"strokeWidth\": 0.0,\n" +
                        "      \"width\": 208.38095\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"scaledAt\": 100,\n" +
                        "  \"version\": 4\n" +
                        "}");
                Tools.copyAssetFile(ctx, "launcher_profiles.json", Tools.DIR_GAME_NEW, true);
                Tools.write(Tools.DIR_GAME_NEW + "/" + "launcher_profiles.json", "{\n" +
                        "  \"profiles\": {\n" +
                        "    \"089641ed-d20c-4933-a366-13f85ca69d53\": {\n" +
                        "      \"icon\": \"data:image/png;base64,AAABAAMAMDAAAAEACACoDgAANgAAACAgAAABAAgAqAgAAN4OAAAQEAAAAQAIAGgFAACGFwAAKAAAADAAAABgAAAAAQAIAAAAAAAACQAAAAAAAAAAAAAAAQAAAAAAAPX08wBMOSoAvbaxANPOywD6+fkAPioZAK+noADW0s4AnpSMAP39/ADFv7oAtKymAGpaTQDa19QA29fUAEs4KACEd20A9fT0AOPg3QCropsATz0uAPn49wCJfHMA5+XjAD4qGgDo5eMAPyoaAGVVSACMgHYA/f39AMW/uwD+/f0As6ukAEMvIADa1tIAopiQAJCFfADv7uwAt7CqALiwqgBtXlEAzsjEAPTz8gBLOCkAcmNXAOPg3gBhUEMA5uThAD0pGACdk4sAxL65AEEuHgDr6ecAQi4eAGlZTADa1tMAj4R6AO/u7QDw7u0ApZyUAG5eUgDf29kAu7SuAIN2bACqoZoAX09BANDMyADRzMgA9/f2AE88LQD49/YAwLm0AD0pGQA+KRkAnJKJAGRURwCLf3UA/Pz8AEEtHACyqqMAemxhAOvp6ADZ1dEAoZePAEUyIgC3r6kA3drXAIJ1agBxYlYAYE9CAId6cAD49/cAnJKKAJ2SigD7+/oAQS0dAOro5gDZ1dIAV0U3AH5wZQBGMiMAbV1RAMvGwQDe2tgAXEo9AEk2JgC6s60A4d7bAKmgmQD39vUA5ePhAObj4QBkU0YA1dDNAPv7+wD8+/sAsamiANjU0ACglo4AaFhMAP///gCPg3oARDEhALauqADc2dYA3dnWAPLx8ABJNicAqJ+XALuzrgCXjIMA9/b2AK2knQDUz8sAm5GJAJyRiQD6+vkAUT8wAOnn5QBALBwAQSwcANjU0QDZ1NEAVkQ2AP///wDu7OsAo5qSAMrFwADd2dcA8fDuAEg1JQC5sqwAb2BTAODd2gCpn5gA5eLgAIl9cwA/KxoA+/r6AOrn5gBVQzQAjoJ5AO3r6QC1racAa1tOANvY1QDc2NUAy8XBAM7JxACWi4IATTosAFA+LwDBu7YA+vn4AMK7tgA/KxsA6ObkAEArGwBmVkkA/v7+AFVDNQD//v4Ae25jAHxuYwDt6+oARDAhANrX0wBrW08AycS/AEc0JAC5sasA39zZAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABfX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX1+MjIyMX19fX19fX19fX19fX4uMjIxfX19fX19fX19fX19fX19fX19fX19fX19frwyje3smtjUYi19fX19fi52vcCB7ewsbr19fX19fX19fX19fX19fX19fX19fX19fBT8dkJCQE0wsqjWvGq9ff0uhOIiQkHO3BV9fX19fX19fX19fX19fX19fX19fX19fBT8dkJCQQBA5jT6aUzt7QzqAEB2QkHK3BV9fX19fX19fX19fX19fX19fX19fX19fnaSikJCQN1hVkJCQkJCQkAc8ApCQkBlLr19fX19fX19fX19fX19fX19fX19fX19fX195RpCQkLBsv5CQkJCQJRMHkJCQAFqLX19fX19fX19fX19fX19fX19fX19fX19fX4tUklFekJBbm16QkJC1ipWQswAvhyFOX19fX19fX19fX19fX19fX19fX19fX19fX1+LVGhXZh2QeJCQkJCQs5CwdiiPeotfX19fX19fX19fX19fX19fX19fX19fX19fX19fX69Jq7yQkJCQkJCQkCpQGBixX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fMJyQkJCQkJCQkKe9i19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fSBaQkJCQkJCQkISxX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fTouxr69JKwqQkJCQkJCQkE+LX19fX19fX19fX19fX19fX19fX19fX19fX19fXxqLloliaEEsvnOQkJCQkJCQkIUBi19fX19fX19fX19fX19fX19fX19fX19fX1+dZFhAqL8XNJFEkJCQkJCQkJCQkIO3BV9fX19fX19fX19fX19fX19fX19fX19fi4uyaiqQkJCQkJCQkJCQkJCQkJCQkJADoK9fX19fX19fX19fX19fX19fX19fX1+vK6mfkJCQkJBbBJAJs5CQkJCQkJCQkJCQPhQaX19fX19fX19fX19fX19fX19fX68urnKQkJCQkJAtJZCKiJCQkJCQkJCQkJCQHzJZnYtfX19fX19fX19fX19fX1+LNWNrkJCQkJCQkJCTYLMpW5CQkJCQkJCQkJCQkJASJImvGotfX19fX19fX19fX69phn6QkJCQkJCQkLUGGa0LRpCQkJCQkJCQkJCQkJCQTQ4xdw+Li4uLX19fX19fi7Qnc5CQkJCQkJCQkASCsDoIFZCQs15tlVFvv3VCvKyXdBMjHDarRRQBNV9fX19fX2VdXUpKSkpKSkpKXIK7HkdMBJCQs54RlbiKbi1rPVZ8pg4OpZRnZ5mBlk5fX19fX4saGBgYGBgYGBgYGBg1D72YcY6NjVJhYSI3Nzc3ug0NDQ0NDQ0NDX0nll9fX19fX19fX19fX19fX19fX19fi4u5KwEBAQEBAQEBAQEBAQEBAQEBAQEBAQF/M19fX19fX19fX19fX19fX19fX19fX19fi4uLi4uLi4uLi4uLi4uLi4uLi4uLi4uLX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fX18AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAIAAAAEAAAAABAAgAAAAAAAAEAAAAAAAAAAAAAAABAAAAAAAAzsnFAPX08wBMOSoAc2RYAGJRRACJfHIA+vn5AD4qGQDs6ugA29fUAFhHOQBZRzkAbl9TAODc2gDNyMMAlYqBALy1rwDj4N0A+Pj3AE89LgD5+PcAUD0uAD4qGgA/KhoA1tLPANfSzwCMgHYAVEI0AP79/QDa1tIAkYV8AO/u7AC4sKoAXEs9AHJjVwBhUEMA0s3KAFA9LwCvpp8A1dHNAPz8+wDEvrkAQi4eANrW0wBYRjgAycO/AH5xZgBHMyQAlImAAPj39gDAubQA5uTiAD0pGQDn5OIAPikZAGRURwBlVEcA1tHOAPz8/ADFvroAemxhAOvp6ABWRTYARTIiALevqQB/cWcAbF1QAN3a1wDz8vEASjcoALu0rwCYjYQAh3pwAJ2SigBSQDEAw724AEEtHQDZ1dIAV0U3AEYyIwCkm5MAy8bBAN7a2ADh3tsA9vb1APf29QB0ZloA5uPhAEAsGwD8+/sA6+jnAMfBvABFMSEA7uzqAGtcTwDd2dYAWkk7APLx8ABJNicAcGFVAL63sQBOOy0AraSdAPr6+QBRPzAAQCwcAEEsHADY1NEA////AMfBvQB8b2QA3dnXAEg1JQCCdGoA4N3aAPb19AC+t7IAdGVZADwnFwCakIcAY1JFAPr6+gCwqKEA19PPAMbAuwBEMCAA29jVANzY1QDKxcEAWkg6AIBzaADy8O8A4N3bAJaLggBLOSkAvbawAKyjnAD5+fgAPysbAOjm5ABAKxsA19PQANjT0AD+/v4AVUM1AEQwIQCShn0AubGrAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMKj8/XGlqTExMTExpkT8/KkxMTExMTExMTExMTExMTIo4O4CTeC8WNjYHkYGIUUsEikxMTExMTExMTExMTExMFgMUbFoeh0kFSEdGME1sVQwWTExMTExMTExMTExMTEyMgX9seSZbbGyPbBhQRGyOPlhMTExMTExMTExMTExMTEyKVg1nWVMBbGwGhBJzfmOKTExMTExMTExMTExMTExMTExMkEEJbI9sbGw6QEJKTExMTExMTExMTExMTExMTExMTExYdnEobGxsbENKNGlMTExMTExMTExMTExMTExMTExpjIp2gihsbGxsKX1MTExMTExMTExMTExMTExMTGkXT2gsIXUnbGxsbGw5AmlMTExMTExMTExMTExMTEwXZRptcosIVGxsbGxsbDEuNkxMTExMTExMTExMTExpTCIAKGxsZ48cbGxsbGxsbF83B0xMTExMTExMTExMikV3H2xsbI8RiT2PbGxsbGxsbG88XBZpTExMTExMTGlgdDpsbGxsBi1hJGxsbGxsbGyPbIMgVoYWB4pMTExYLBCLNTMzMzUJZhFkbGw6c11XQycOfDJ6km5uI2lMTExFCk5OTk5OThsLXoUNUl8JHWuOjXt7GY1NK18PikxMTGmMjIyMjIyMaYwWcBUTExMVFRUVFRUVFRUVJWJMTExMTExMTExMTExMTExqaWlpaWlpaWlpaWlpaWlpaUxMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTEwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACgAAAAQAAAAIAAAAAEACAAAAAAAAAEAAAAAAAAAAAAAAAEAAAAAAADUz8sA5+TiAPr5+QDDvLcAVEIzAEczJADy8e8A0s3JAEMvHwDl4uAAVkQ2APj39wDQy8cAmY6FAEEtHQBUQjQA9vX1AFA+LwBDLyAAl4yDAD8rGwBAKxsA3drXAN7a1wCZjoYAQS0eAO3r6QCkm5MATDorAE06KwCTiH8AXEs9AP7+/gCnnZYA6eflAFZFNgD4+PcAoZePAOvp6ABKNicAfW9kAOfl4wDj4d4Av7mzAJ+VjQBINCUAQzAgAHdpXQBALBsAV0U3APn4+ACLfnQAnpOLAEYyIwBCLh4AraSdAFVDNQCIfHIAQCwcAHhpXgBzZVkAU0EzAN7b2ADOyMQAPioaAD8qGgBOOywAXUw+AP///wDb19QATDkqAFtKPADGwLsAbl9TAPn5+ADZ1dIASjcoAJGFfACNgXcAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADg4ODg4ODg4ODg4ODg4ODg4ODg4ODg4ODg4ODg4ODg4ODg4ODjoODg4ODjoODg4ODg4ODi4cBUBAQDVGEg4ODg4ODhRDBxs7SS80DEcUDg4ODg46QgMJASQiPisdOg4ODg4ODhQnTTJEGihMOg4ODg4OFQ4RMU4CRBYdFA4ODg4OQSMhFykQREQLOTAVDg4ONjxLREoBBkREICYTOAgZDkYzJSwNHkgqRQA/SDcYHxQOFEBAQDpCBD0EBA8KIy0ODg4ODg4OOjo6Ojo6OjoODg4ODg4ODg4ODg4ODg4ODg4ODg4ODg4ODg4ODg4ODg4ODg4ODg4ODg4ODg4ODg4ODgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\u003d\",\n" +
                        "      \"lastVersionId\": \"1.12.2-forge-14.23.5.2860\",\n" +
                        "      \"logConfigIsXML\": false,\n" +
                        "      \"name\": \"forge\",\n" +
                        "      \"type\": \"custom\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}\n");
                Tools.copyAssetFile(ctx,"resolv.conf",Tools.DIR_DATA, false);
            } catch (IOException e) {
                Log.e("AsyncAssetManager", "Failed to unpack critical components !");
            }
            ProgressLayout.clearProgress(ProgressLayout.EXTRACT_SINGLE_FILES);
        });
    }

    public static void unpackComponents(Context ctx){
        ProgressLayout.setProgress(ProgressLayout.EXTRACT_COMPONENTS, 0);
        sExecutorService.execute(() -> {
            try {
                unpackComponent(ctx, "caciocavallo", false);
                unpackComponent(ctx, "caciocavallo17", false);
                // Since the Java module system doesn't allow multiple JARs to declare the same module,
                // we repack them to a single file here
                unpackComponent(ctx, "lwjgl3", false);
                unpackComponent(ctx, "security", true);
                unpackComponent(ctx, "arc_dns_injector", true);
                unpackComponent(ctx, "forge_installer", true);
            } catch (IOException e) {
                Log.e("AsyncAssetManager", "Failed o unpack components !",e );
            }
            ProgressLayout.clearProgress(ProgressLayout.EXTRACT_COMPONENTS);
        });
    }

    private static void unpackComponent(Context ctx, String component, boolean privateDirectory) throws IOException {
        AssetManager am = ctx.getAssets();
        String rootDir = privateDirectory ? Tools.DIR_DATA : Tools.DIR_GAME_HOME;

        File versionFile = new File(rootDir + "/" + component + "/version");
        InputStream is = am.open("components/" + component + "/version");
        if(!versionFile.exists()) {
            if (versionFile.getParentFile().exists() && versionFile.getParentFile().isDirectory()) {
                FileUtils.deleteDirectory(versionFile.getParentFile());
            }
            versionFile.getParentFile().mkdir();

            Log.i("UnpackPrep", component + ": Pack was installed manually, or does not exist, unpacking new...");
            String[] fileList = am.list("components/" + component);
            for(String s : fileList) {
                Tools.copyAssetFile(ctx, "components/" + component + "/" + s, rootDir + "/" + component, true);
            }
        } else {
            FileInputStream fis = new FileInputStream(versionFile);
            String release1 = Tools.read(is);
            String release2 = Tools.read(fis);
            if (!release1.equals(release2)) {
                if (versionFile.getParentFile().exists() && versionFile.getParentFile().isDirectory()) {
                    FileUtils.deleteDirectory(versionFile.getParentFile());
                }
                versionFile.getParentFile().mkdir();

                String[] fileList = am.list("components/" + component);
                for (String fileName : fileList) {
                    Tools.copyAssetFile(ctx, "components/" + component + "/" + fileName, rootDir + "/" + component, true);
                }
            } else {
                Log.i("UnpackPrep", component + ": Pack is up-to-date with the launcher, continuing...");
            }
        }
    }
}
