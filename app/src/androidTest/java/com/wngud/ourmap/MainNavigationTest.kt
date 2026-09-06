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

    @Test
    fun tabsAndCreateSheetReturnToOriginalTab() {
        compose.setContent { OurMapTheme { OurMapAppContent(DemoContent()) } }
        compose.onNodeWithText("우리의 지도").assertIsDisplayed()
        compose.onNode(hasText("기록") and hasClickAction()).performClick()
        compose.onNodeWithText("한 장면씩, 차곡차곡 쌓인 우리의 이야기").assertIsDisplayed()
        compose.onNodeWithContentDescription("새 기록 작성").performClick()
        compose.onNodeWithText("새로운 추억을 남겨요").assertIsDisplayed()
        compose.onNodeWithText("둘러보기 계속하기").performClick()
        compose.onNode(hasText("기록") and hasClickAction()).assertIsSelected()
        compose.onNode(hasText("우리") and hasClickAction()).performClick()
        compose.onNodeWithText("주형 · 수빈").assertIsDisplayed()
        compose.onNode(hasText("마이") and hasClickAction()).performClick()
        compose.onNodeWithText("나의 기록과 취향을 담는 곳").assertIsDisplayed()
    }

    @Test
    fun selectedTabAndFilterSurviveSavedStateRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { OurMapTheme { OurMapAppContent(DemoContent()) } }
        compose.onNode(hasText("기록") and hasClickAction()).performClick()
        compose.onNodeWithText("카페").performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNode(hasText("기록") and hasClickAction()).assertIsSelected()
        compose.onNodeWithText("카페").assertIsSelected()
        compose.onNodeWithText("성수 카페거리").assertIsDisplayed()
        compose.onNodeWithText("서울숲").assertDoesNotExist()
    }
}
