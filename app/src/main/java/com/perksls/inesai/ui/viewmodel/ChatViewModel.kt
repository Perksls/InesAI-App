package com.perksls.inesai.ui.viewmodel

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perksls.inesai.data.local.PreferencesManager
import com.perksls.inesai.data.model.*
import com.perksls.inesai.data.repository.ConversationRepository
import com.perksls.inesai.data.repository.FallbackChatRepository
import com.perksls.inesai.data.repository.FallbackStreamEvent
import com.perksls.inesai.data.repository.ProviderRepository
import com.perksls.inesai.utils.ContextManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ChatViewModel(
    private val preferencesManager: PreferencesManager,
    private val conversationRepository: ConversationRepository,
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val repository = FallbackChatRepository()
    private val contextManager = ContextManager()

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> get() = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentProviderName = MutableStateFlow("–")
    val currentProvider: StateFlow<String> = _currentProviderName.asStateFlow()

    private val _currentModelName = MutableStateFlow("")
    val currentModel: StateFlow<String> = _currentModelName.asStateFlow()

    private val _fallbackActive = MutableStateFlow(false)
    val fallbackActive: StateFlow<Boolean> = _fallbackActive.asStateFlow()

    private val _estimatedTokens = MutableStateFlow(0)
    val estimatedTokens: StateFlow<Int> = _estimatedTokens.asStateFlow()

    private val _providerStatus = MutableStateFlow<Map<String, String>>(emptyMap())
    val providerStatus: StateFlow<Map<String, String>> = _providerStatus.asStateFlow()

    private val _currentImage = MutableStateFlow<Bitmap?>(null)
    val currentImage: StateFlow<Bitmap?> = _currentImage.asStateFlow()

    private val _attachedFileName = MutableStateFlow<String?>(null)
    val attachedFileName: StateFlow<String?> = _attachedFileName.asStateFlow()

    private val _attachedFileContent = MutableStateFlow<String?>(null)

    val conversations: StateFlow<List<Conversation>> = conversationRepository
        .getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val providers: StateFlow<List<AIProvider>> = providerRepository
        .getAllProviders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            providerRepository.getPrimaryProvider()?.let {
                _currentProviderName.value = it.name
                _currentModelName.value = it.effectiveModel
            }
        }
    }

    fun createNewConversation() {
        viewModelScope.launch {
            val provider = providerRepository.getPrimaryProvider()
            val conversation = conversationRepository.createConversation(
                model = provider?.effectiveModel ?: "unknown",
                provider = provider?.id ?: "unknown"
            )
            _currentConversationId.value = conversation.id
            _messages.clear()
            _currentImage.value = null
        }
    }

    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            _currentConversationId.value = conversationId
            _messages.clear()
            conversationRepository.getMessagesForConversation(conversationId)
                .collect { loadedMessages ->
                    _messages.clear()
                    _messages.addAll(loadedMessages)
                }
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            conversationRepository.deleteConversation(conversationId)
            if (_currentConversationId.value == conversationId) {
                _currentConversationId.value = null
                _messages.clear()
            }
        }
    }

    fun setImage(bitmap: Bitmap?) { _currentImage.value = bitmap }
    fun clearImage() { _currentImage.value = null }
    fun clearError() { _error.value = null }

    fun setAttachedFile(name: String, content: String) {
        _attachedFileName.value = name
        _attachedFileContent.value = content
        _currentImage.value = null // limpar imagem se houver ficheiro
    }
    fun clearAttachedFile() {
        _attachedFileName.value = null
        _attachedFileContent.value = null
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            try {
                _error.value = null
                _isLoading.value = true
                _fallbackActive.value = false
                _providerStatus.value = emptyMap()

                // Garantir conversa activa
                var conversationId = _currentConversationId.value
                if (conversationId == null) {
                    val primary = providerRepository.getPrimaryProvider()
                    val conversation = conversationRepository.createConversation(
                        model = primary?.effectiveModel ?: "unknown",
                        provider = primary?.id ?: "unknown"
                    )
                    conversationId = conversation.id
                    _currentConversationId.value = conversationId
                }

                val imageBase64 = _currentImage.value?.let { bitmapToBase64(it) }
                val fileContent = _attachedFileContent.value
                val fileName = _attachedFileName.value
                val messageContent = if (fileContent != null && fileName != null) {
                    "$content\n\n[Ficheiro: $fileName]\n```\n$fileContent\n```"
                } else content

                val userMessage = ChatMessage(
                    role = MessageRole.USER,
                    content = messageContent,
                    imageBase64 = imageBase64,
                    fileName = fileName,
                    fileContent = fileContent
                )
                _messages.add(userMessage)
                conversationRepository.addMessage(conversationId, userMessage)
                _currentImage.value = null
                clearAttachedFile()

                if (_messages.count { it.role == MessageRole.USER } == 1) {
                    val title = conversationRepository.generateTitle(conversationId)
                    conversationRepository.updateConversationTitle(conversationId, title)
                }

                // Carregar providers da BD
                // Providers já vêm ordenados por sortOrder do DAO
                val allProviders = providerRepository.getAllProviders().first()
                val primaryProvider = allProviders.firstOrNull() ?: providerRepository.getPrimaryProvider()

                if (primaryProvider == null || primaryProvider.apiKey.isBlank()) {
                    _error.value = "Nenhum provider configurado. Vai a Providers."
                    _isLoading.value = false
                    return@launch
                }

                val fallbackEnabled = preferencesManager.fallbackEnabled.first()
                val contextWindow = preferencesManager.contextWindow.first()
                val temperature = preferencesManager.temperature.first()
                val systemPrompt = preferencesManager.systemPrompt.first()
                val primaryModel = primaryProvider.effectiveModel.ifBlank { "gpt-4o" }
                _currentModelName.value = primaryModel

                val supportsVision = primaryModel.contains("vision") ||
                        primaryModel.contains("gpt-4o") ||
                        primaryModel.contains("claude")

                val contextMessages = if (imageBase64 != null && supportsVision) {
                    repository.buildMultimodalMessages(
                        messages = _messages.toList(),
                        systemPrompt = systemPrompt,
                        contextWindow = contextWindow
                    )
                } else {
                    contextManager.buildContext(
                        messages = _messages.toList(),
                        systemPrompt = systemPrompt,
                        contextWindow = contextWindow
                    ).map { msg ->
                        when (msg) {
                            is OpenAIMessage.TextMessage -> msg
                            else -> OpenAIMessage.TextMessage(msg.role, "")
                        }
                    }
                }

                _estimatedTokens.value = contextManager.estimateTokens(contextMessages)

                val primary = FallbackChatRepository.ProviderConfig(
                    provider = primaryProvider,
                    apiKey = primaryProvider.apiKey,
                    model = primaryModel
                )

                val fallbackProviders = if (fallbackEnabled) {
                    allProviders
                        .drop(1)  // já estão ordenados, o primeiro é o principal
                        .filter { it.apiKey.isNotBlank() }
                        .map { p ->
                            FallbackChatRepository.ProviderConfig(
                                provider = p,
                                apiKey = p.apiKey,
                                model = p.effectiveModel.ifBlank { "gpt-4o" }
                            )
                        }
                } else emptyList()

                val assistantMessage = ChatMessage(
                    role = MessageRole.ASSISTANT,
                    content = "",
                    isStreaming = true,
                    model = "${primaryProvider.name} • $primaryModel"
                )
                _messages.add(assistantMessage)

                repository.streamWithFallback(
                    primaryProvider = primary,
                    fallbackProviders = fallbackProviders,
                    messages = contextMessages,
                    temperature = temperature,
                    fallbackEnabled = fallbackEnabled
                ).collect { event ->
                    when (event) {
                        is FallbackStreamEvent.AttemptingProvider ->
                            _providerStatus.value = _providerStatus.value + (event.providerName to "⏳ A tentar...")
                        is FallbackStreamEvent.ProviderFailed -> {
                            _providerStatus.value = _providerStatus.value + (event.providerName to "❌ ${event.reason}")
                            if (fallbackEnabled && event.providerName == primary.provider.name)
                                _fallbackActive.value = true
                        }
                        is FallbackStreamEvent.SuccessProvider -> {
                            _providerStatus.value = _providerStatus.value + (event.providerName to "✅ Ativo")
                            _currentProviderName.value = event.providerName
                            // Actualizar label da mensagem com o provider que respondeu
                            val i = _messages.lastIndex
                            if (i >= 0 && _messages[i].role == MessageRole.ASSISTANT) {
                                val currentModel = _messages[i].model?.substringAfter(" • ") ?: primaryModel
                                _messages[i] = _messages[i].copy(model = "${event.providerName} • $currentModel")
                            }
                        }
                        is FallbackStreamEvent.ContentChunk -> {
                            val i = _messages.lastIndex
                            _messages[i] = _messages[i].copy(
                                content = _messages[i].content + event.content,
                                isStreaming = true,
                                model = _messages[i].model // manter o label já definido
                            )
                        }
                        is FallbackStreamEvent.Finished -> {
                            val i = _messages.lastIndex
                            _messages[i] = _messages[i].copy(isStreaming = false)
                        }
                        is FallbackStreamEvent.AllFailed -> {
                            _error.value = "Todos os providers falharam: ${event.lastError}"
                            if (_messages.lastOrNull()?.role == MessageRole.ASSISTANT &&
                                _messages.lastOrNull()?.content.isNullOrEmpty())
                                _messages.removeAt(_messages.lastIndex)
                        }
                    }
                }

                val lastIndex = _messages.lastIndex
                if (lastIndex >= 0) {
                    val finalMessage = _messages[lastIndex].copy(isStreaming = false)
                    _messages[lastIndex] = finalMessage
                    conversationRepository.addMessage(conversationId, finalMessage)
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Erro desconhecido"
                if (_messages.lastOrNull()?.role == MessageRole.ASSISTANT &&
                    _messages.lastOrNull()?.content.isNullOrEmpty())
                    _messages.removeAt(_messages.lastIndex)
            } finally {
                _isLoading.value = false
                _fallbackActive.value = false
            }
        }
    }

    fun clearChat() {
        _currentConversationId.value = null
        _messages.clear()
        _estimatedTokens.value = 0
        _providerStatus.value = emptyMap()
        _currentImage.value = null
    }

    fun regenerateLastResponse() {
        if (_messages.size >= 2) {
            val lastUserMessage = _messages.findLast { it.role == MessageRole.USER }
            _messages.removeAll {
                it.timestamp >= (lastUserMessage?.timestamp ?: 0) &&
                it.role == MessageRole.ASSISTANT
            }
            lastUserMessage?.let { sendMessage(it.content) }
        }
    }
}
