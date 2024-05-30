package pixelmon

import net.kdt.pojavlaunch.R

enum class Loading(val messageLoading: Int) {
    MOVING_FILES(messageLoading = R.string.movendo_arquivos_para_o_minecreaft_1_12),
    DOWNLOAD_MOD_ONE_DOT_TWELVE(messageLoading = R.string.baixando_os_mods_da_1_12),
    DOWNLOAD_MOD_ONE_DOT_SIXTEEN(messageLoading = R.string.baixando_os_mods_da_1_16),
    DOWNLOAD_ONE_DOT_SIXTEEN(messageLoading = R.string.baixando_o_minecraft_1_16),
    SHOW_PLAY_BUTTON(messageLoading = R.string.mostrando_o_bot_o_de_jogar),
    DOWNLOAD_TEXTURE(messageLoading = R.string.baixando_a_textura_do_pixelmon);
}