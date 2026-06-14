package com.perksls.inesai.data.local.dao

import androidx.room.*
import com.perksls.inesai.data.local.entity.ProviderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderDao {

    @Query("SELECT * FROM providers ORDER BY sortOrder ASC, createdAt ASC")
    fun getAllProviders(): Flow<List<ProviderEntity>>

    @Query("UPDATE providers SET sortOrder = :order WHERE id = :id")
    suspend fun updateSortOrder(id: String, order: Int)

    @Query("SELECT * FROM providers WHERE id = :id")
    suspend fun getProviderById(id: String): ProviderEntity?

    @Query("SELECT * FROM providers WHERE isPrimary = 1 LIMIT 1")
    suspend fun getPrimaryProvider(): ProviderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderEntity)

    @Update
    suspend fun updateProvider(provider: ProviderEntity)

    @Query("DELETE FROM providers WHERE id = :id")
    suspend fun deleteProviderById(id: String)

    @Query("UPDATE providers SET isPrimary = 0")
    suspend fun clearPrimary()

    @Query("UPDATE providers SET isPrimary = 1 WHERE id = :id")
    suspend fun setPrimary(id: String)

    @Query("UPDATE providers SET activeModel = :model WHERE id = :id")
    suspend fun setActiveModel(id: String, model: String)
}
