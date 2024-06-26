package net.kdt.pojavlaunch.tasks

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import net.kdt.pojavlaunch.Architecture
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.multirt.MultiRTUtils
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException

object AsyncAssetManager {
    /**
     * Attempt to install the java 8 runtime, if necessary
     * @param am App context
     */
    @JvmStatic
    fun unpackRuntime(am: AssetManager) {
        /* Check if JRE is included */
        var rt_version: String? = null
        val current_rt_version = MultiRTUtils.__internal__readBinpackVersion("Internal")
        try {
            rt_version = Tools.read(am.open("components/jre/version"))
        } catch (e: IOException) {
            Log.e("JREAuto", "JRE was not included on this APK.", e)
        }
        val exactJREName = MultiRTUtils.getExactJreName(8)
        if (current_rt_version == null && exactJREName != null && exactJREName != "Internal" /*this clause is for when the internal runtime is goofed*/) return
        if (rt_version == null) return
        if (rt_version == current_rt_version) return

        // Install the runtime in an async manner, hope for the best
        val finalRt_version: String = rt_version
        PojavApplication.sExecutorService.execute {
            try {
                MultiRTUtils.installRuntimeNamedBinpack(
                    am.open("components/jre/universal.tar.xz"),
                    am.open("components/jre/bin-" + Architecture.archAsString(Tools.DEVICE_ARCHITECTURE) + ".tar.xz"),
                    "Internal", finalRt_version
                )
                MultiRTUtils.postPrepare("Internal")
            } catch (e: IOException) {
                Log.e("JREAuto", "Internal JRE unpack failed", e)
            }
        }
    }

    /** Unpack single files, with no regard to version tracking  */
    @JvmStatic
    fun unpackSingleFiles(ctx: Context) {
//        ProgressLayout.setProgress(ProgressLayout.EXTRACT_SINGLE_FILES, 0)
        PojavApplication.sExecutorService.execute {
            try {
                val controlMap = ctx.assets.open("default.json") .readBytes()
                Tools.copyAssetFile(ctx, "minecraft/options.txt", Tools.DIR_GAME_NEW, false)
                Tools.copyAssetFile(ctx, "minecraft/optionsof.txt", Tools.DIR_GAME_NEW, false)
                Tools.copyAssetFile(ctx, "minecraft/optionsOneDotSixteen.txt", Tools.DIR_GAME_NEW, false)
                Tools.copyAssetFile(ctx, "minecraft/optionsofOneDotSixteen.txt", Tools.DIR_GAME_NEW, false)

                Tools.copyAssetFile(ctx, "default.json", Tools.CTRLMAP_PATH, true)
                Tools.write(Tools.CTRLMAP_PATH, controlMap)
//                Tools.copyAssetFile(ctx, "resolv.conf", Tools.DIR_DATA, false)
//                Tools.copyAssetFile(ctx, "launcher_profiles.json", Tools.DIR_GAME_NEW, true)
            } catch (e: IOException) {
                Log.e("AsyncAssetManager", "Failed to unpack critical components !")
            }
//            ProgressLayout.clearProgress(ProgressLayout.EXTRACT_SINGLE_FILES)
        }
    }

    @JvmStatic
    fun unpackComponents(ctx: Context) {
//        ProgressLayout.setProgress(ProgressLayout.EXTRACT_COMPONENTS, 0)
        PojavApplication.sExecutorService.execute {
            try {
                unpackComponent(ctx, "caciocavallo", false)
                unpackComponent(ctx, "caciocavallo17", false)
                // Since the Java module system doesn't allow multiple JARs to declare the same module,
                // we repack them to a single file here
                unpackComponent(ctx, "lwjgl3", false)
                unpackComponent(ctx, "security", true)
                unpackComponent(ctx, "arc_dns_injector", true)
                unpackComponent(ctx, "forge_installer", true)
            } catch (e: IOException) {
                Log.e("AsyncAssetManager", "Failed o unpack components !", e)
            }
//            ProgressLayout.clearProgress(ProgressLayout.EXTRACT_COMPONENTS)
        }
    }

    @Throws(IOException::class)
    private fun unpackComponent(ctx: Context, component: String, privateDirectory: Boolean) {
        val am = ctx.assets
        val rootDir = if (privateDirectory) Tools.DIR_DATA else Tools.DIR_GAME_HOME
        val versionFile = File("$rootDir/$component/version")
        val `is` = am.open("components/$component/version")
        if (!versionFile.exists()) {
            if (versionFile.parentFile.exists() && versionFile.parentFile.isDirectory) {
                FileUtils.deleteDirectory(versionFile.parentFile)
            }
            versionFile.parentFile.mkdir()
            Log.i(
                "UnpackPrep",
                "$component: Pack was installed manually, or does not exist, unpacking new..."
            )
            val fileList = am.list("components/$component")
            for (s in fileList!!) {
                Tools.copyAssetFile(ctx, "components/$component/$s", "$rootDir/$component", true)
            }
        } else {
            val fis = FileInputStream(versionFile)
            val release1 = Tools.read(`is`)
            val release2 = Tools.read(fis)
            if (release1 != release2) {
                if (versionFile.parentFile.exists() && versionFile.parentFile.isDirectory) {
                    FileUtils.deleteDirectory(versionFile.parentFile)
                }
                versionFile.parentFile.mkdir()
                val fileList = am.list("components/$component")
                for (fileName in fileList!!) {
                    Tools.copyAssetFile(
                        ctx,
                        "components/$component/$fileName",
                        "$rootDir/$component",
                        true
                    )
                }
            } else {
                Log.i(
                    "UnpackPrep",
                    "$component: Pack is up-to-date with the launcher, continuing..."
                )
            }
        }
    }
}
