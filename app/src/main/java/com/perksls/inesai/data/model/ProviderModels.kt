package com.perksls.inesai.data.model

data class AIProvider(
    val id: String,
    val name: String,
    val baseUrl: String,
    val models: List<String>,
    val activeModel: String = "",
    val apiKey: String = "",
    val isOpenAICompatible: Boolean = true,
    val sortOrder: Int = 0,
    val isPrimary: Boolean = false
) {
    val effectiveModel: String
        get() = if (activeModel.isNotBlank() && activeModel in models) activeModel
                else models.firstOrNull() ?: ""

    /** URL final a usar nas chamadas à API */
    val chatEndpoint: String
        get() = if (isOpenAICompatible)
            baseUrl.trimEnd('/') + "/chat/completions"
        else
            baseUrl  // URL completo introduzido pelo utilizador
}

enum class ProviderStatus {
    ACTIVE, FAILED, RATE_LIMITED, DISABLED
}
