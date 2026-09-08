package com.wngud.ourmap

import com.wngud.ourmap.core.model.Memory
import com.wngud.ourmap.data.demo.DemoContent
import com.wngud.ourmap.domain.memory.*
import com.wngud.ourmap.feature.memory.MemoryViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class MemoryViewModelTest {
    private val owner = MemoryOwner("user", "space")
    @Test fun readFailureIsVisibleAndObservationCanRestart() = runTest {
        val repo = Fake().apply { failRead = true }
        val model = MemoryViewModel(repo)
        model.observe(owner)
        assertNotNull(model.state.value.error)
        assertNull(model.state.value.memories)
        repo.failRead = false
        backgroundScope.launch { model.observe(owner) }; runCurrent()
        assertEquals(emptyList<Memory>(), model.state.value.memories)
        assertNull(model.state.value.error)
    }
    @Test fun failedSaveDoesNotChangeSnapshotAndRetryReadsCommittedRecord() = runTest {
        val repo = Fake()
        val model = MemoryViewModel(repo)
        backgroundScope.launch { model.observe(owner) }; runCurrent()
        val memory = DemoContent().memories.first()
        repo.failSave = true
        try { model.save(owner, memory); fail("Expected failure") } catch (_: IOException) { }
        assertTrue(model.state.value.memories!!.isEmpty())
        repo.failSave = false
        assertEquals(memory, model.save(owner, memory))
        assertEquals(listOf(memory), model.state.value.memories)
    }
    @Test fun cancellationIsNotConvertedToReadError() = runTest {
        val repo = Fake()
        val model = MemoryViewModel(repo)
        val job = backgroundScope.launch { model.observe(owner) }
        runCurrent(); job.cancelAndJoin()
        assertNull(model.state.value.error)
    }
    private class Fake : MemoryRepository {
        val rows = MutableStateFlow(emptyList<Memory>())
        var failRead = false
        var failSave = false
        override fun observe(owner: MemoryOwner): Flow<List<Memory>> = flow {
            if (failRead) throw IOException()
            emitAll(rows)
        }
        override suspend fun save(owner: MemoryOwner, memory: Memory): Memory {
            if (failSave) throw IOException()
            rows.value = listOf(memory)
            return memory
        }
        override suspend fun toggleFavorite(owner: MemoryOwner, id: String) = Unit
        override suspend fun clearLocalData() { rows.value = emptyList() }
    }
}
