package com.wngud.ourmap.feature.memory

import androidx.lifecycle.ViewModel
import com.wngud.ourmap.core.model.Memory
import com.wngud.ourmap.domain.memory.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*

data class MemoryUiState(val owner: MemoryOwner? = null, val memories: List<Memory>? = null, val error: String? = null)

@HiltViewModel
class MemoryViewModel @Inject constructor(private val repository: MemoryRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(MemoryUiState())
    val state = mutableState.asStateFlow()

    /** Collection belongs to the visible home and stops when that home leaves composition. */
    suspend fun observe(owner: MemoryOwner) {
        mutableState.value = if (state.value.owner == owner) state.value.copy(error = null) else MemoryUiState(owner)
        try {
            repository.observe(owner).collect { mutableState.value = MemoryUiState(owner, it) }
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (_: Exception) {
            mutableState.value = state.value.copy(error = "기록을 읽지 못했어요. 데이터를 지우지 않았으니 다시 시도해 주세요.")
        }
    }

    suspend fun save(owner: MemoryOwner, memory: Memory): Memory {
        val result = repository.save(owner, memory)
        // Wait for a DB-backed snapshot before navigating to its detail.
        val records = repository.observe(owner).first { rows -> rows.any { it.id == result.id } }
        if (state.value.owner == owner) mutableState.value = MemoryUiState(owner, records)
        return result
    }
    suspend fun toggleFavorite(owner: MemoryOwner, id: String) = repository.toggleFavorite(owner, id)
}
