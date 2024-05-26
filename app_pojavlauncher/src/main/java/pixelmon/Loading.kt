package pixelmon

enum class Loading(val messageLoading: String) {
    MOVING_FILES(messageLoading = "movendo arquivos para o minecreaft 1.12..."),
    DOWNLOAD_MOD_ONE_DOT_TWELVE(messageLoading = "baixando os mods da 1.12..."),
    DOWNLOAD_MOD_ONE_DOT_SIXTEEN(messageLoading = "baixando os mods da 1.16..."),
    DOWNLOAD_ONE_DOT_SIXTEEN(messageLoading = "baixando o minecraft 1.16..."),
    SHOW_PLAY_BUTTON(messageLoading = "mostrando o botão de jogar...")
}