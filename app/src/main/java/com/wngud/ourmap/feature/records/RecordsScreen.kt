package com.wngud.ourmap.feature.records

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wngud.ourmap.core.model.Memory
import com.wngud.ourmap.ui.components.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecordsScreen(memories: List<Memory>, modifier: Modifier = Modifier) {
    var selectedTag by rememberSaveable { mutableStateOf("전체") }
    ScreenContent("기록", "한 장면씩, 차곡차곡 쌓인 우리의 이야기", modifier) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("전체", "산책", "카페", "피크닉").forEach { tag ->
                OurMapChip(tag, selected = selectedTag == tag, onClick = { selectedTag = tag })
            }
        }
        val filtered = memories.filter { selectedTag == "전체" || selectedTag in it.tags }
        if (filtered.isEmpty()) Text("아직 기록이 없어요. 첫 번째 순간을 남겨 보세요.")
        filtered.forEach { MemoryCard(it) }
    }
}
