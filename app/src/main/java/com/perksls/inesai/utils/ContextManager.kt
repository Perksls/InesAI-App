package com.perksls.inesai.utils

import com.perksls.inesai.data.model.ChatMessage
import com.perksls.inesai.data.model.MessageRole
import com.perksls.inesai.data.model.OpenAIMessage

class ContextManager {

    fun buildContext(
        messages: List<ChatMessage>,
        systemPrompt: String,
        contextWindow: Int
    ): List<OpenAIMessage> {
        val contextMessages = mutableListOf<OpenAIMessage>()
        contextMessages.add(OpenAIMessage.TextMessage("system", systemPrompt))

        val chatHistory = messages
            .filter { it.role == MessageRole.USER || it.role == MessageRole.ASSISTANT }
            .sortedBy { it.timestamp }
            .takeLast(contextWindow * 2)

        chatHistory.forEach { message ->
            val role = when (message.role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
                else -> "user"
            }
            contextMessages.add(OpenAIMessage.TextMessage(role, message.content))
        }

        return contextMessages
    }

    fun estimateTokens(messages: List<OpenAIMessage>): Int {
        return messages.sumOf { 
            when (it) {
                is OpenAIMessage.TextMessage -> it.content.length / 4 + 1
                is OpenAIMessage.VisionMessage -> 1000 // Estimativa para imagens
                else -> 1
            }
        }
    }

    fun shouldSummarize(messages: List<ChatMessage>, maxTokens: Int = 4000): Boolean {
        val totalChars = messages.sumOf { it.content.length }
        return totalChars / 4 > maxTokens
    }
}
