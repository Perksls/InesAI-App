package com.perksls.inesai.data.repository

import android.util.Log
import com.google.gson.Gson
import com.perksls.inesai.data.api.MultimodalRequestConverter
import com.perksls.inesai.data.api.ProviderClientFactory
import com.perksls.inesai.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class FallbackChatRepository {

    companion object {
        private const val TAG = "FallbackChatRepository"
    }

    private val gson = Gson()
    private val requestConverter = MultimodalRequestConverter()

    data class ProviderConfig(
        val provider: AIProvider,
        val apiKey: String,
        val model: String,
        val status: ProviderStatus = ProviderStatus.ACTIVE
    )

    fun buildMultimodalMessages(
        messages: List<ChatMessage>,
        systemPrompt: String,
        contextWindow: Int
    ): List<OpenAIMessage> {
        val result = mutableListOf<OpenAIMessage>()
        result.add(OpenAIMessage.TextMessage("system", systemPrompt))

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

            if (message.imageBase64 != null && message.role == MessageRole.USER) {
                val contentParts = mutableListOf<ContentPart>()
                if (message.content.isNotBlank()) {
                    contentParts.add(ContentPart.Text(message.content))
                }
                contentParts.add(ContentPart.ImageUrl(
                    ContentPart.ImageUrlData("data:image/jpeg;base64,${message.imageBase64}")
                ))
                result.add(OpenAIMessage.VisionMessage(role, contentParts))
            } else {
                result.add(OpenAIMessage.TextMessage(role, message.content))
            }
        }
        return result
    }

    fun streamWithFallback(
        primaryProvider: ProviderConfig,
        fallbackProviders: List<ProviderConfig>,
        messages: List<OpenAIMessage>,
        temperature: Double = 0.7,
        maxTokens: Int? = null,
        fallbackEnabled: Boolean = true
    ): Flow<FallbackStreamEvent> = flow {

        val allProviders = listOf(primaryProvider) + fallbackProviders
        val attemptedProviders = mutableListOf<String>()
        var lastError: String? = null

        for (config in allProviders) {
            if (config.status == ProviderStatus.DISABLED) continue

            attemptedProviders.add(config.provider.name)
            emit(FallbackStreamEvent.AttemptingProvider(config.provider.name))

            try {
                val service = ProviderClientFactory.getService(config.provider)
                val request = OpenAIRequest(
                    model = config.model,
                    messages = messages,
                    temperature = temperature,
                    max_tokens = maxTokens,
                    stream = true
                )

                val requestBody = requestConverter.convert(request)
                val authHeader = "Bearer ${config.apiKey}"

                val response = service.streamChatCompletion(config.provider.chatEndpoint, authHeader, requestBody)

                if (!response.isSuccessful) {
                    val error = parseError(response.errorBody()?.string())
                    lastError = error

                    when {
                        response.code() == 429 -> {
                            emit(FallbackStreamEvent.ProviderFailed(config.provider.name, "Rate limit: $error"))
                            if (!fallbackEnabled) throw Exception("Rate limited e fallback desativado")
                            continue
                        }
                        response.code() in 500..599 -> {
                            emit(FallbackStreamEvent.ProviderFailed(config.provider.name, "Server error: $error"))
                            if (!fallbackEnabled) throw Exception("Server error e fallback desativado")
                            continue
                        }
                        else -> {
                            emit(FallbackStreamEvent.ProviderFailed(config.provider.name, "Error ${response.code()}: $error"))
                            if (!fallbackEnabled) throw Exception(error)
                            continue
                        }
                    }
                }

                emit(FallbackStreamEvent.SuccessProvider(config.provider.name))

                val reader = BufferedReader(InputStreamReader(response.body()?.byteStream()))
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    val trimmed = line!!.trim()
                    if (trimmed.startsWith("data: ")) {
                        val data = trimmed.substring(6)
                        if (data == "[DONE]") break

                        try {
                            val json = JSONObject(data)
                            val choices = json.getJSONArray("choices")
                            if (choices.length() > 0) {
                                val delta = choices.getJSONObject(0).optJSONObject("delta")
                                val content = delta?.optString("content", null)
                                val finishReason = choices.getJSONObject(0).optString("finish_reason", null)

                                if (!content.isNullOrEmpty()) {
                                    emit(FallbackStreamEvent.ContentChunk(content))
                                }
                                if (finishReason != null && finishReason != "null") {
                                    emit(FallbackStreamEvent.Finished(finishReason))
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Linha mal formatada: $data")
                        }
                    }
                }
                reader.close()
                return@flow

            } catch (e: Exception) {
                lastError = e.message
                emit(FallbackStreamEvent.ProviderFailed(config.provider.name, e.message ?: "Erro desconhecido"))
                if (!fallbackEnabled) throw e
            }
        }

        emit(FallbackStreamEvent.AllFailed(attemptedProviders, lastError ?: "Todos os providers falharam"))
        throw Exception("Fallback esgotado. Tentados: ${attemptedProviders.joinToString()}")

    }.flowOn(Dispatchers.IO)

    private fun parseError(errorBody: String?): String? {
        return try {
            val json = JSONObject(errorBody ?: return null)
            when {
                json.has("error") -> json.getJSONObject("error").optString("message", "Erro desconhecido")
                json.has("message") -> json.getString("message")
                else -> errorBody
            }
        } catch (e: Exception) {
            errorBody
        }
    }
}

sealed class FallbackStreamEvent {
    data class AttemptingProvider(val providerName: String) : FallbackStreamEvent()
    data class ProviderFailed(val providerName: String, val reason: String) : FallbackStreamEvent()
    data class SuccessProvider(val providerName: String) : FallbackStreamEvent()
    data class ContentChunk(val content: String) : FallbackStreamEvent()
    data class Finished(val reason: String) : FallbackStreamEvent()
    data class AllFailed(val attempted: List<String>, val lastError: String) : FallbackStreamEvent()
}
