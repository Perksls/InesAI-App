package com.perksls.inesai.data.repository

import com.perksls.inesai.data.local.dao.ProviderDao
import com.perksls.inesai.data.local.entity.ProviderEntity
import com.perksls.inesai.data.model.AIProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ProviderRepository(private val dao: ProviderDao) {

    fun getAllProviders(): Flow<List<AIProvider>> =
        dao.getAllProviders().map { list -> list.map { it.toModel() } }

    suspend fun getProviderById(id: String): AIProvider? =
        dao.getProviderById(id)?.toModel()

    suspend fun getPrimaryProvider(): AIProvider? =
        dao.getPrimaryProvider()?.toModel()

    suspend fun saveProvider(
        name: String,
        baseUrl: String,
        apiKey: String,
        models: List<String>,
        isOpenAICompatible: Boolean = true,
        id: String = UUID.randomUUID().toString()
    ): AIProvider {
        val entity = ProviderEntity(
            id = id,
            name = name.trim(),
            baseUrl = baseUrl.trim().trimEnd('/'),
            apiKey = apiKey.trim(),
            models = models.joinToString(","),
            activeModel = models.firstOrNull() ?: "",
            isOpenAICompatible = isOpenAICompatible,
            sortOrder = 0,
            isPrimary = false,
            createdAt = System.currentTimeMillis()
        )
        dao.insertProvider(entity)
        return entity.toModel()
    }

    suspend fun updateProvider(
        id: String,
        name: String,
        baseUrl: String,
        apiKey: String,
        models: List<String>,
        isOpenAICompatible: Boolean = true
    ) {
        val existing = dao.getProviderById(id) ?: return
        val newActiveModel = if (existing.activeModel in models) existing.activeModel
                             else models.firstOrNull() ?: ""
        dao.updateProvider(
            existing.copy(
                name = name.trim(),
                baseUrl = baseUrl.trim().trimEnd('/'),
                apiKey = apiKey.trim(),
                models = models.joinToString(","),
                activeModel = newActiveModel,
                isOpenAICompatible = isOpenAICompatible
            )
        )
    }

    suspend fun setActiveModel(providerId: String, model: String) {
        dao.setActiveModel(providerId, model)
    }

    suspend fun deleteProvider(id: String) {
        dao.deleteProviderById(id)
    }

    suspend fun setPrimary(id: String) {
        dao.clearPrimary()
        dao.setPrimary(id)
    }

    suspend fun reorderProviders(orderedIds: List<String>) {
        orderedIds.forEachIndexed { index, id ->
            dao.updateSortOrder(id, index)
        }
    }

    // --- Mappers ---

    private fun ProviderEntity.toModel() = AIProvider(
        id = id,
        name = name,
        baseUrl = baseUrl,
        apiKey = apiKey,
        models = models.split(",").map { it.trim() }.filter { it.isNotBlank() },
        activeModel = activeModel,
        isOpenAICompatible = isOpenAICompatible,
        sortOrder = sortOrder,
        isPrimary = isPrimary
    )

}
