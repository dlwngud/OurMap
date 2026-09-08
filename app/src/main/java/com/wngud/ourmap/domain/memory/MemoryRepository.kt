package com.wngud.ourmap.domain.memory

import com.wngud.ourmap.core.model.Memory
import kotlinx.coroutines.flow.Flow

data class MemoryOwner(val userId: String, val spaceId: String)

interface MemoryRepository {
    fun observe(owner: MemoryOwner): Flow<List<Memory>>
    suspend fun save(owner: MemoryOwner, memory: Memory): Memory
    suspend fun toggleFavorite(owner: MemoryOwner, id: String)
    suspend fun clearLocalData()
}
