package com.perksls.inesai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val baseUrl: String,        // base URL até ao /v1 (ex: https://api.openai.com/v1)
    val apiKey: String,
    val models: String,         // CSV: "gpt-4o,gpt-4o-mini"
    val activeModel: String,
    val isOpenAICompatible: Boolean,
    val sortOrder: Int,          // ordem de fallback (0 = primeiro/principal)
    val isPrimary: Boolean,
    val createdAt: Long
)
