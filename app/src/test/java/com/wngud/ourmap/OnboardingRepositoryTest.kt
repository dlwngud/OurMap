package com.wngud.ourmap

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.CorruptionException
import com.wngud.ourmap.data.local.*
import com.wngud.ourmap.domain.onboarding.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream

class OnboardingRepositoryTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun profileSpaceAndInviteSurviveStoreReopening() = runTest {
        val file = folder.root.resolve("onboarding.json")
        val firstJob = SupervisorJob()
        val first = DataStoreOnboardingRepository(DataStoreFactory.create(OnboardingSerializer,
            scope = CoroutineScope(firstJob + Dispatchers.IO), produceFile = { file }))
        first.perform(OnboardingAction.SignIn)
        first.perform(OnboardingAction.SaveProfile("테스터", "소개", "🌿"))
        val created = first.perform(OnboardingAction.CreateSpace("우리 여행", "2026.09.07", "🧡", "소중한 순간"))
        assertEquals(SpaceStatus.WaitingForPartner, created.status)
        assertEquals(1, created.members.size)
        assertTrue(created.invite!!.code.matches(Regex("[A-Z2-9]{4}-[A-Z2-9]{4}")))
        firstJob.cancelAndJoin()
        val secondJob = SupervisorJob()
        try {
            val second = DataStoreOnboardingRepository(DataStoreFactory.create(OnboardingSerializer,
                scope = CoroutineScope(secondJob + Dispatchers.IO), produceFile = { file }))
            assertEquals(created, second.load())
            second.perform(OnboardingAction.SignOut)
            assertEquals(created, second.perform(OnboardingAction.SignIn))
            second.perform(OnboardingAction.Reset)
            assertEquals(Onboarding(), second.load())
        } finally { secondJob.cancelAndJoin() }
    }

    @Test fun duplicateCreateCannotOverwriteExistingSpace() = runTest {
        val store = DataStoreFactory.create(OnboardingSerializer, scope = backgroundScope,
            produceFile = { folder.root.resolve("duplicate.json") })
        val repository = DataStoreOnboardingRepository(store)
        repository.perform(OnboardingAction.SignIn)
        repository.perform(OnboardingAction.SaveProfile("테스터", "", "🌷"))
        val action = OnboardingAction.CreateSpace("우리", "2026.09.07", "📷", "")
        val created = repository.perform(action)
        try { repository.perform(action); fail("duplicate must fail") } catch (_: IllegalArgumentException) { }
        assertEquals(created, repository.load())
    }

    @Test fun corruptedJsonIsReportedRatherThanSilentlyReset() = runTest {
        try {
            OnboardingSerializer.readFrom(ByteArrayInputStream("broken".toByteArray()))
            fail("corruption must be reported")
        } catch (_: CorruptionException) { }
    }

    @Test fun inviteExpiresAndRenewalInvalidatesOldCode() {
        var state = profile()
        state = apply(state, OnboardingAction.CreateSpace("우리", "2026.09.07", "🧡", ""), 0)
        val old = state.invite!!
        try {
            apply(state, OnboardingAction.SimulatePartner, old.expiresAt)
            fail("expired invite must fail")
        } catch (_: IllegalArgumentException) { }
        state = OnboardingRules.apply(state, OnboardingAction.RenewInvite, old.expiresAt, "another-id", "EFGH-5678")
        assertNotEquals(old.code, state.invite!!.code)
        assertFalse(state.invite!!.accepted)
        state = apply(state, OnboardingAction.SimulatePartner, old.expiresAt + 1)
        assertEquals(SpaceStatus.Active, state.status)
        assertEquals(2, state.members.size)
        try { apply(state, OnboardingAction.SimulatePartner, 1); fail() } catch (_: IllegalArgumentException) { }
    }

    @Test fun sampleJoinValidatesCodeAndDoesNotCreateDuplicateMembership() {
        val initial = profile()
        try { apply(initial, OnboardingAction.JoinSample("BAD", "수빈")); fail() } catch (_: IllegalArgumentException) { }
        val joined = apply(initial, OnboardingAction.JoinSample(" 8a2f91b ", "수빈"))
        assertEquals(SpaceStatus.Active, joined.status)
        assertEquals(2, joined.members.size)
        try { apply(joined, OnboardingAction.JoinSample("8A2F-91B", "수빈")); fail() } catch (_: IllegalArgumentException) { }
    }

    @Test fun invalidProfileAndDateAreRejectedAtRepositoryBoundary() {
        val user = apply(Onboarding(), OnboardingAction.SignIn)
        try { apply(user, OnboardingAction.SaveProfile(" ", "", "🌿")); fail() } catch (_: IllegalArgumentException) { }
        try { apply(profile(), OnboardingAction.CreateSpace("우리", "2026.02.30", "🧡", "")); fail() } catch (_: IllegalArgumentException) { }
    }

    private fun profile() = apply(apply(Onboarding(), OnboardingAction.SignIn),
        OnboardingAction.SaveProfile("테스터", "소개", "🌿"))
    private fun apply(state: Onboarding, action: OnboardingAction, now: Long = 0) =
        OnboardingRules.apply(state, action, now, "id-$now", "ABCD-2345")
}
