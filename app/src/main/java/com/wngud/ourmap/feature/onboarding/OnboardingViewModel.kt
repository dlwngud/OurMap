package com.wngud.ourmap.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wngud.ourmap.domain.onboarding.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val loading: Boolean = true,
    val data: Onboarding? = null,
    val saving: Boolean = false,
    val error: String? = null,
    val completed: OnboardingAction? = null,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(private val repository: OnboardingRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(OnboardingUiState())
    val state = mutableState.asStateFlow()

    /** Started by the screen's lifecycle effect; repeated composition does not reload. */
    fun load() {
        if (state.value.data != null || state.value.saving) return
        mutableState.value = state.value.copy(loading = true, saving = true, error = null)
        viewModelScope.launch {
            try {
                mutableState.value = OnboardingUiState(loading = false, data = repository.load())
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) {
                mutableState.value = OnboardingUiState(loading = false,
                    error = "저장된 정보를 읽지 못했어요. 데이터는 지우지 않았으니 다시 시도해 주세요.")
            }
        }
    }

    fun perform(action: OnboardingAction) {
        if (state.value.saving || state.value.data == null || state.value.completed != null) return
        mutableState.value = state.value.copy(saving = true, error = null)
        viewModelScope.launch {
            try {
                val saved = repository.perform(action)
                mutableState.value = state.value.copy(data = saved, saving = false, completed = action)
            } catch (e: CancellationException) { throw e
            } catch (e: IllegalArgumentException) {
                mutableState.value = state.value.copy(saving = false, error = e.message ?: "입력을 확인해 주세요.")
            } catch (_: Exception) {
                mutableState.value = state.value.copy(saving = false,
                    error = "저장하지 못했어요. 입력은 유지됩니다. 저장 공간을 확인하고 다시 시도해 주세요.")
            }
        }
    }

    fun acknowledge() { mutableState.value = state.value.copy(completed = null) }
    fun dismissError() { mutableState.value = state.value.copy(error = null) }
}
