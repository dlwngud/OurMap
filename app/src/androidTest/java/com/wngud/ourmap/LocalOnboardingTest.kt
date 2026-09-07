package com.wngud.ourmap

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wngud.ourmap.data.demo.DemoContent
import com.wngud.ourmap.domain.onboarding.*
import com.wngud.ourmap.feature.onboarding.*
import com.wngud.ourmap.ui.theme.OurMapTheme
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class LocalOnboardingTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val repository = FakeLocalRepository()

    private fun start() {
        val model = OnboardingViewModel(repository)
        model.load()
        compose.setContent {
            val state by model.state.collectAsStateWithLifecycle()
            OurMapTheme { LocalOnboardingContent(state, DemoContent(), model::perform, model::load,
                model::acknowledge, model::dismissError) }
        }
    }
    private fun click(text: String) {
        compose.waitUntil(5_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        val matcher = hasText(text) and hasClickAction()
        if (compose.onAllNodes(matcher and hasAnyAncestor(hasScrollAction())).fetchSemanticsNodes().isNotEmpty()) {
            compose.onNode(matcher).performScrollTo()
        }
        compose.onNode(matcher).performClick()
    }
    private fun profile() {
        click("로컬 체험 시작하기")
        compose.onNodeWithText("이름 또는 닉네임").performTextInput("테스터")
        click("다음")
    }
    private fun awaitDisplayed(text: String) {
        compose.waitUntil(5_000) {
            try {
                compose.onNodeWithText(text).assertIsDisplayed()
                true
            } catch (_: AssertionError) { false }
        }
    }

    @Test fun createSpaceLogoutResumeAndEditProfile() {
        start(); profile()
        click("새 Space 만들기")
        compose.onNodeWithText("Space 이름").performTextReplacement("로컬 여행")
        click("Space 만들기")
        compose.onNodeWithText("ABCD-2345").performScrollTo().assertIsDisplayed()
        click("우리 공간으로 이동")
        compose.onNodeWithText("로컬 여행").assertIsDisplayed()
        click("마이"); click("로컬 계정 관리"); click("로컬 로그아웃")
        click("저장된 프로필로 계속하기")
        compose.onNodeWithText("로컬 여행").assertIsDisplayed()
        click("마이"); click("프로필 편집")
        compose.onNodeWithText("이름 또는 닉네임").performTextReplacement("새이름")
        click("프로필 저장")
        compose.onNodeWithText("새이름").assertIsDisplayed()
    }

    @Test fun failedProfileSaveKeepsInputUntilRetrySucceeds() {
        start(); click("로컬 체험 시작하기")
        compose.onNodeWithText("이름 또는 닉네임").performTextInput("유지할 이름")
        repository.fail = true
        click("다음")
        click("입력으로 돌아가기")
        compose.onNodeWithText("이름 또는 닉네임").assertTextContains("유지할 이름")
        repository.fail = false
        click("다음")
        awaitDisplayed("Space 시작하기")
    }

    @Test fun invalidInviteDoesNotNavigateAndValidSampleJoins() {
        start(); profile(); click("초대 코드 입력")
        compose.onNodeWithText("초대 코드").performTextInput("INVALID")
        click("참여하고 시작하기")
        compose.onNodeWithText("초대 코드를 확인해 주세요.").assertIsDisplayed()
        click("입력으로 돌아가기")
        compose.onNodeWithText("초대 코드").performTextReplacement("8a2f-91b")
        click("참여하고 시작하기")
        awaitDisplayed("주형 · 테스터")
    }

    private class FakeLocalRepository : OnboardingRepository {
        var data = Onboarding()
        var fail = false
        private var sequence = 0
        override suspend fun load() = data
        override suspend fun perform(action: OnboardingAction): Onboarding {
            if (fail) throw IOException()
            data = OnboardingRules.apply(data, action, 1_000, "id-${sequence++}", "ABCD-2345")
            return data
        }
    }
}
