package com.wngud.ourmap

import com.wngud.ourmap.domain.onboarding.*
import com.wngud.ourmap.feature.onboarding.OnboardingViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    @Test fun failedSaveKeepsPreviousStateAndCanBeRetried() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeRepository()
            val model = OnboardingViewModel(repository)
            model.load(); runCurrent()
            repository.fail = true
            model.perform(OnboardingAction.SignIn); runCurrent()
            assertEquals(Onboarding(), model.state.value.data)
            assertNotNull(model.state.value.error)
            assertNull(model.state.value.completed)
            repository.fail = false
            model.perform(OnboardingAction.SignIn); runCurrent()
            assertTrue(model.state.value.data!!.signedIn)
            assertEquals(OnboardingAction.SignIn, model.state.value.completed)
            model.acknowledge()
            assertNull(model.state.value.completed)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun duplicateClicksWhileSavingAreIgnored() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeRepository()
            val model = OnboardingViewModel(repository)
            model.load(); runCurrent()
            repository.gate = CompletableDeferred()
            model.perform(OnboardingAction.SignIn)
            model.perform(OnboardingAction.SignIn)
            runCurrent()
            assertTrue(model.state.value.saving)
            assertEquals(1, repository.calls)
            repository.gate!!.complete(Unit); runCurrent()
            assertFalse(model.state.value.saving)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun loadFailureCanBeRetriedWithoutResettingData() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeRepository().apply { fail = true }
            val model = OnboardingViewModel(repository)
            model.load(); runCurrent()
            assertNull(model.state.value.data)
            assertNotNull(model.state.value.error)
            repository.fail = false
            model.load(); runCurrent()
            assertEquals(Onboarding(), model.state.value.data)
        } finally { Dispatchers.resetMain() }
    }

    private class FakeRepository : OnboardingRepository {
        var fail = false
        var calls = 0
        var gate: CompletableDeferred<Unit>? = null
        override suspend fun load(): Onboarding {
            if (fail) throw IOException()
            return Onboarding()
        }
        override suspend fun perform(action: OnboardingAction): Onboarding {
            calls++
            gate?.await()
            if (fail) throw IOException()
            return Onboarding(signedIn = true, user = User("local"))
        }
    }
}
