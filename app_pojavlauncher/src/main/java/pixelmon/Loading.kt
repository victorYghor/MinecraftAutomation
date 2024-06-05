package pixelmon


enum class Loading(val messageLoading: String) {
    MOVING_FILES(messageLoading = "Movendo arquivos para Minecraft 1.12"),
    DOWNLOAD_MOD_ONE_DOT_TWELVE(messageLoading = "Baixando mods para 1.12"),
    DOWNLOAD_MOD_ONE_DOT_SIXTEEN(messageLoading = "Baixando mods para 1.16"),
    DOWNLOAD_ONE_DOT_SIXTEEN(messageLoading = "Baixando Minecraft 1.16"),
    SHOW_PLAY_BUTTON(messageLoading = "Mostrando o botão de jogar"),
    DOWNLOAD_TEXTURE(messageLoading = "Baixando a textura do Pixelmon");
}