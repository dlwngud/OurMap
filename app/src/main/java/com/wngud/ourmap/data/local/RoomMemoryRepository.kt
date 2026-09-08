package com.wngud.ourmap.data.local

import android.content.Context
import androidx.room.Room
import com.wngud.ourmap.core.model.Memory
import com.wngud.ourmap.domain.memory.*
import dagger.*
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class RoomMemoryRepository @Inject constructor(
    private val database: MemoryDatabase, private val photos: MemoryPhotoStore,
) : MemoryRepository {
    private val mutex = Mutex()
    override fun observe(owner: MemoryOwner) = database.memories().observe(owner.userId, owner.spaceId)
        .map { rows -> rows.map { it.toMemory() } }

    override suspend fun save(owner: MemoryOwner, memory: Memory): Memory = withContext(Dispatchers.IO) {
        mutex.withLock {
            require(owner.userId.isNotBlank() && owner.spaceId.isNotBlank())
            require(memory.id.isNotBlank() && memory.place.id.isNotBlank() && memory.note.length <= 200)
            require(validMemoryDate(memory.visitedOn)) { "방문 날짜를 확인해 주세요." }
            require(memory.photoCount <= 8)
            val dao = database.memories()
            // A retry after a committed write must not create another record or copy photos again.
            dao.find(owner.userId, owner.spaceId, memory.id)?.let { return@withLock it.toMemory() }
            photos.cleanExcept(dao.all().flatMap { it.toMemory().photoUris }.toSet())
            val copied = mutableListOf<String>()
            var committed = false
            try {
                memory.photoUris.forEach { copied += photos.copy(it) }
                val saved = memory.copy(photoUris = copied.toList())
                // Do not remove committed files if cancellation arrives just after the SQL commit.
                withContext(NonCancellable) { dao.insert(saved.toEntity(owner)); committed = true }
                saved
            } catch (error: Exception) {
                if (!committed) withContext(NonCancellable) {
                    copied.forEach {
                        try { photos.remove(it) } catch (cleanup: Exception) { error.addSuppressed(cleanup) }
                    }
                }
                throw error
            }
        }
    }

    override suspend fun toggleFavorite(owner: MemoryOwner, id: String) = mutex.withLock {
        check(database.memories().toggleFavorite(owner.userId, owner.spaceId, id) == 1) { "기록을 찾을 수 없어요." }
    }

    override suspend fun clearLocalData() = withContext(Dispatchers.IO) {
        mutex.withLock {
            database.memories().clear()
            photos.cleanExcept(emptySet())
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object MemoryModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): MemoryDatabase =
        Room.databaseBuilder(context, MemoryDatabase::class.java, "memories.db").build()
    @Provides fun repository(impl: RoomMemoryRepository): MemoryRepository = impl
}
