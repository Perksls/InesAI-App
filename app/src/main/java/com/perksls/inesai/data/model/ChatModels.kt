package com.perksls.inesai.data.model

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val imageBase64: String? = null,
    val fileName: String? = null,       // nome do ficheiro anexado
    val fileContent: String? = null,    // conteúdo texto do ficheiro
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val model: String? = null
)

enum class MessageRole {
    USER, ASSISTANT, SYSTEM
}

data class Conversation(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val model: String,
    val provider: String,
    val messages: List<ChatMessage> = emptyList()
)

// OpenAI API Multimodal
sealed class OpenAIMessage {
    abstract val role: String

    data class TextMessage(
        override val role: String,
        val content: String
    ) : OpenAIMessage()

    data class VisionMessage(
        override val role: String,
        val content: List<ContentPart>
    ) : OpenAIMessage()
}

sealed class ContentPart {
    data class Text(val text: String) : ContentPart()
    data class ImageUrl(val image_url: ImageUrlData) : ContentPart()

    data class ImageUrlData(val url: String)
}

data class OpenAIRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int? = null,
    val stream: Boolean = true
)

data class OpenAIStreamResponse(
    val id: String?,
    val choices: List<StreamChoice>?
)

data class StreamChoice(
    val delta: Delta?,
    val finish_reason: String?
)

data class Delta(
    val content: String?,
    val role: String?
)
