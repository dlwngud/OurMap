package com.wngud.ourmap

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import com.wngud.ourmap.data.demo.DemoContent
import com.wngud.ourmap.navigation.OurMapAppContent
import com.wngud.ourmap.ui.theme.OurMapTheme
import org.junit.Rule
import org.junit.Test

class MainNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun start() { compose.setContent { OurMapTheme { OurMapAppContent(DemoContent()) } } }
    private fun browse() { compose.onNodeWithText("샘플 Space 둘러보기").performScrollTo().performClick() }
    private fun tab(name: String) = compose.onNode(hasText(name) and hasClickAction())
    private fun click(text: String) {
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(text).performScrollTo().performClick()
    }
    private fun back() { compose.onNodeWithContentDescription("뒤로").performClick() }

    @Test
    fun tabsAndEditorCancellationReturnToOriginalTab() {
        start()
        browse()
        tab("기록").performClick()
        compose.onNodeWithContentDescription("새 기록 작성").performClick()
        compose.onNodeWithText("기록 미리보기 저장").assertIsNotEnabled()
        compose.onNodeWithText("메모").performScrollTo().performTextInput("취소할 기록")
        back()
        compose.onNodeWithText("작성을 그만둘까요?").assertIsDisplayed()
        compose.onNodeWithText("계속 작성").performClick()
        compose.onNodeWithText("메모").assertTextContains("취소할 기록")
        back()
        compose.onNodeWithText("그만두기").performClick()
        tab("기록").assertIsSelected()
        tab("우리").performClick()
        compose.onNodeWithText("주형 · 수빈").assertIsDisplayed()
        tab("마이").performClick()
        compose.onNodeWithText("나의 기록과 취향을 담는 곳").assertIsDisplayed()
    }

    @Test
    fun selectedTabAndSearchSurviveTabSwitchAndRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { OurMapTheme { OurMapAppContent(DemoContent()) } }
        browse()
        tab("기록").performClick()
        compose.onNodeWithText("기록 검색").performTextInput("카페")
        tab("우리").performClick()
        tab("기록").performClick()
        restoration.emulateSavedInstanceStateRestore()
        tab("기록").assertIsSelected()
        compose.onNodeWithText("기록 검색").assertTextContains("카페")
        compose.onNodeWithText("성수 카페거리").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("서울숲").assertDoesNotExist()
    }

    @Test
    fun newSpaceAndFirstMemoryAppearInRecords() {
        start()
        click("카카오로 시작하기")
        compose.onNodeWithText("이름 또는 닉네임").performTextReplacement("테스터")
        compose.onNodeWithText("다음").performClick()
        compose.onNode(hasText("새 Space 만들기") and hasClickAction()).performClick()
        compose.onNodeWithText("Space 이름").performTextReplacement("우리 여행")
        compose.onNodeWithText("Space 만들기").performClick()
        compose.onNodeWithText("나중에 하고 시작하기").performClick()
        compose.onNodeWithText("우리 여행").assertIsDisplayed()
        click("첫 장소 기록하기")
        click("장소 선택")
        compose.onNodeWithText("서울숲").performClick()
        compose.onNodeWithText("메모").performScrollTo().performTextInput("우리의 첫 산책")
        compose.onNodeWithText("기록 미리보기 저장").performClick()
        compose.onNodeWithText("우리의 첫 산책").performScrollTo().assertIsDisplayed()
        back()
        tab("기록").performClick()
        compose.onNodeWithText("우리의 첫 산책").performScrollTo().assertIsDisplayed()
        tab("지도").performClick()
        compose.onNodeWithText("서울숲").performScrollTo().performClick()
        click("장소 자세히 보기")
        compose.onNodeWithText("함께 남긴 기록 1개").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun inviteRejectsInvalidCodeThenJoinsDemoSpace() {
        start()
        click("초대 코드로 참여")
        compose.onNodeWithText("이름 또는 닉네임").performScrollTo().performTextReplacement("수빈")
        compose.onNodeWithText("초대 코드").performScrollTo().performTextInput("INVALID")
        compose.onNodeWithText("참여하고 시작하기").performClick()
        compose.onNodeWithText("초대 코드를 확인해 주세요.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("초대 코드").performTextReplacement("8a2f-91b")
        compose.onNodeWithText("참여하고 시작하기").performClick()
        compose.onNodeWithText("주형 · 수빈").assertIsDisplayed()
    }

    @Test
    fun placeDetailOpensRecordAndGallery() {
        start()
        browse()
        compose.onNodeWithText("서울숲").performScrollTo().performClick()
        click("장소 자세히 보기")
        compose.onNodeWithText("날씨가 좋아 오래 걸었던 날").performScrollTo().performClick()
        compose.onNodeWithText("추억 상세").assertIsDisplayed()
        compose.onNodeWithText("♡ 추억 찜").performScrollTo().performClick()
        compose.onNodeWithText("♥ 찜 해제").performScrollTo().assertIsDisplayed()
        // The illustration's click label is an accessibility action, so match its descendant label.
        compose.onAllNodesWithText("샘플 이미지").onFirst().performScrollTo().performClick()
        compose.onNodeWithText("1 / 3").performScrollTo().assertIsDisplayed()
        click("다음 사진")
        compose.onNodeWithText("2 / 3").assertIsDisplayed()
    }
}
