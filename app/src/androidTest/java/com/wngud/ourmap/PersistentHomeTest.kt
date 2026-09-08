package com.wngud.ourmap

import androidx.activity.ComponentActivity
import android.content.ContextWrapper
import java.io.File
import java.util.UUID
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.room.Room
import com.wngud.ourmap.data.demo.*
import com.wngud.ourmap.data.local.*
import com.wngud.ourmap.domain.memory.MemoryOwner
import com.wngud.ourmap.feature.memory.*
import com.wngud.ourmap.navigation.LocalAccountControls
import com.wngud.ourmap.ui.theme.OurMapTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*

class PersistentHomeTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var db: MemoryDatabase
    private lateinit var files: File
    @After fun close() { if (::db.isInitialized) db.close(); if (::files.isInitialized) files.deleteRecursively() }

    @Test fun saveNavigatesToDatabaseRecordAndFavoriteIsPersisted() = runTest {
        db = Room.inMemoryDatabaseBuilder(compose.activity, MemoryDatabase::class.java).build()
        files = File(compose.activity.cacheDir, "home-test-${UUID.randomUUID()}")
        val isolated = object : ContextWrapper(compose.activity) { override fun getNoBackupFilesDir() = files }
        val repo = RoomMemoryRepository(db, MemoryPhotoStore(isolated))
        val model = MemoryViewModel(repo)
        val owner = MemoryOwner("test", "test-space")
        compose.setContent { OurMapTheme {
            PersistentHome(DemoContent(), LocalAccountControls(DemoSession(spaceName = "저장 테스트"), {}, {}, {}), owner, model)
        } }
        fun click(text: String) {
            compose.waitUntil(5_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
            val node = compose.onNodeWithText(text)
            if (node.fetchSemanticsNode().parent != null &&
                compose.onAllNodes(hasText(text) and hasAnyAncestor(hasScrollAction())).fetchSemanticsNodes().isNotEmpty()) node.performScrollTo()
            node.performClick()
        }
        click("첫 장소 기록하기")
        click("장소 선택")
        click("서울숲")
        compose.onNodeWithText("메모").performScrollTo().performTextInput("DB에 남기는 기록")
        click("기록 저장")
        compose.waitUntil(5_000) { compose.onAllNodesWithText("기기에 저장된 기록 · 서버 백업 없음").fetchSemanticsNodes().isNotEmpty() }
        assertEquals("DB에 남기는 기록", repo.observe(owner).first().single().note)
        click("♡ 추억 찜")
        assertTrue(repo.observe(owner).first { it.single().favorite }.single().favorite)
        compose.onNodeWithContentDescription("뒤로").performClick()
        compose.onNode(hasText("기록") and hasClickAction()).performClick()
        compose.onNodeWithText("기록 검색").performTextInput("없는 기록")
        compose.onNodeWithText("기록 검색").performImeAction()
        compose.onNodeWithText("조건에 맞는 기록이 없어요").performScrollTo().assertIsDisplayed()
        click("필터 초기화")
        compose.onNodeWithText("DB에 남기는 기록").performScrollTo().assertIsDisplayed()
    }
}
