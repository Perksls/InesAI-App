package com.perksls.inesai.data.repository

import com.perksls.inesai.data.local.dao.ConversationDao
import com.perksls.inesai.data.local.dao.MessageDao
import com.perksls.inesai.data.local.entity.ConversationEntity
import com.perksls.inesai.data.local.entity.MessageEntity
import com.perksls.inesai.data.model.ChatMessage
import com.perksls.inesai.data.model.Conversation
import com.perksls.inesai.data.model.MessageRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ConversationRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) {

    fun getAllConversations(): Flow<List<Conversation>> {
        return conversationDao.getAllConversations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun createConversation(title: String = "Nova Conversa", model: String, provider: String): Conversation {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity = ConversationEntity(
            id = id,
            title = title,
            createdAt = now,
            updatedAt = now,
            model = model,
            provider = provider
        )
        conversationDao.insertConversation(entity)
        return entity.toDomain()
    }

    suspend fun updateConversationTitle(id: String, title: String) {
        conversationDao.updateTitle(id, title, System.currentTimeMillis())
    }

    suspend fun updateConversationTimestamp(id: String) {
        conversationDao.updateTimestamp(id, System.currentTimeMillis())
    }

    suspend fun deleteConversation(id: String) {
        conversationDao.deleteConversationById(id)
        messageDao.deleteMessagesForConversation(id)
    }

    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>> {
        return messageDao.getMessagesForConversation(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getMessagesForConversationSync(conversationId: String): List<ChatMessage> {
        return messageDao.getMessagesForConversationSync(conversationId).map { it.toDomain() }
    }

    suspend fun addMessage(conversationId: String, message: ChatMessage) {
        val entity = MessageEntity(
            id = message.id,
            conversationId = conversationId,
            role = message.role.name.lowercase(),
            content = message.content,
            imageBase64 = message.imageBase64,
            fileName = message.fileName,
            fileContent = message.fileContent,
            timestamp = message.timestamp,
            model = message.model,
            provider = null
        )
        messageDao.insertMessage(entity)
        updateConversationTimestamp(conversationId)
    }

    suspend fun addMessages(conversationId: String, messages: List<ChatMessage>) {
        val entities = messages.map { message ->
            MessageEntity(
                id = message.id,
                conversationId = conversationId,
                role = message.role.name.lowercase(),
                content = message.content,
                imageBase64 = message.imageBase64,
                fileName = message.fileName,
                fileContent = message.fileContent,
                timestamp = message.timestamp,
                model = message.model,
                provider = null
            )
        }
        messageDao.insertMessages(entities)
        updateConversationTimestamp(conversationId)
    }

    suspend fun generateTitle(conversationId: String): String {
        val messages = getMessagesForConversationSync(conversationId)
        val firstUserMessage = messages.find { it.role == MessageRole.USER }
        return firstUserMessage?.content?.take(50)?.let {
            if (it.length >= 50) "$it..." else it
        } ?: "Conversa ${System.currentTimeMillis()}"
    }

    private fun ConversationEntity.toDomain(): Conversation {
        return Conversation(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            model = model,
            provider = provider
        )
    }

    private fun MessageEntity.toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            role = when (role) {
                "user" -> MessageRole.USER
                "assistant" -> MessageRole.ASSISTANT
                "system" -> MessageRole.SYSTEM
                else -> MessageRole.USER
            },
            content = content,
            imageBase64 = imageBase64,
            fileName = fileName,
            fileContent = fileContent,
            timestamp = timestamp,
            model = model
        )
    }
}
