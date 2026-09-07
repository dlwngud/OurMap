package com.wngud.ourmap

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import com.wngud.ourmap.data.demo.DemoContent
import com.wngud.ourmap.feature.memory.MemoryEditorContent
import com.wngud.ourmap.ui.theme.OurMapTheme
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MemoryEditorTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val places = DemoContent().memories.map { it.place }

    @Test fun failedSaveKeepsInputAndRetryUsesSameId() {
        val ids = mutableListOf<String>()
        var saved = 0
        compose.setContent { OurMapTheme {
            MemoryEditorContent(places, places.first().id, "수빈", {}, { saved++ },
                prepareSave = {
                    ids += it.id
                    if (ids.size == 1) throw java.io.IOException("test failure")
                })
        } }
        compose.onNodeWithText("메모").performScrollTo().performTextInput("유지할 메모")
        compose.onNodeWithText("기록 미리보기 저장").performClick()
        compose.onNodeWithText("저장 실패").assertIsDisplayed()
        compose.onNodeWithText("다시 작성 화면으로").performClick()
        compose.onNodeWithText("메모").assertTextContains("유지할 메모")
        compose.onNodeWithText("기록 미리보기 저장").performClick()
        compose.runOnIdle { assertEquals(1, saved); assertEquals(ids.first(), ids.last()) }
    }

    @Test fun savingBlocksFurtherInteractionUntilComplete() {
        val gate = CompletableDeferred<Unit>()
        var attempts = 0
        var saved = 0
        compose.setContent { OurMapTheme {
            MemoryEditorContent(places, places.first().id, "수빈", {}, { saved++ },
                prepareSave = { attempts++; gate.await() })
        } }
        compose.onNodeWithText("기록 미리보기 저장").performClick()
        compose.onNodeWithText("사진 확인·기록 처리 중…").assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, attempts); assertEquals(0, saved); gate.complete(Unit) }
        compose.waitUntil(5_000) { saved == 1 }
    }

    @Test fun draftSurvivesSavedStateRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { OurMapTheme {
            MemoryEditorContent(places, places.first().id, "수빈", {}, {})
        } }
        compose.onNodeWithText("메모").performScrollTo().performTextInput("회전 후에도 남겨요")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("메모").performScrollTo().assertTextContains("회전 후에도 남겨요")
        compose.onNodeWithText("기록 미리보기 저장").assertIsEnabled()
    }
}
