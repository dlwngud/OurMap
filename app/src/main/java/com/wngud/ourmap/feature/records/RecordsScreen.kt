package com.wngud.ourmap.feature.records

import androidx.compose.foundation.clickable
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
fun RecordsScreen(memories: List<Memory>, onMemory: (String) -> Unit, onPlace: (String) -> Unit,
    onPhoto: (String, Int) -> Unit, onCreate: () -> Unit) {
    var view by rememberSaveable { mutableStateOf("전체") }
    var query by rememberSaveable { mutableStateOf("") }
    var favoriteOnly by rememberSaveable { mutableStateOf(false) }
    val filtered = memories.filter {
        (!favoriteOnly || it.favorite) && (it.place.name.contains(query.trim()) || it.note.contains(query.trim()))
    }.sortedByDescending { it.visitedOn }
    PrototypePage("기록", subtitle = "한 장면씩, 차곡차곡 쌓인 우리의 이야기") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("전체", "사진", "장소").forEach { item -> OurMapChip(item, view == item, { view = item }) }
        }
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(),
            label = { Text("기록 검색") }, singleLine = true)
        OurMapChip("찜한 추억만", favoriteOnly, { favoriteOnly = !favoriteOnly })
        if (filtered.isEmpty()) {
            OurMapCard(Modifier.fillMaxWidth()) {
                Text(if (memories.isEmpty()) "아직 기록이 없어요" else "조건에 맞는 기록이 없어요",
                    style = MaterialTheme.typography.titleLarge)
                OurMapButton(onCreate) { Text("첫 장소 기록하기") }
            }
        } else when (view) {
            "전체" -> filtered.forEach { MemoryCard(it, onClick = { onMemory(it.id) }) }
            "장소" -> filtered.map { it.place }.distinctBy { it.id }.forEach { place ->
                FeatureRow(place.name, { onPlace(place.id) }, subtitle = "${filtered.count { it.place.id == place.id }}개의 추억", emoji = "📍")
            }
            else -> {
                if (filtered.all { it.photoStyles.isEmpty() }) Text("선택한 기록에는 사진이 없어요.")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filtered.forEach { memory ->
                        memory.photoStyles.forEachIndexed { index, style ->
                            SamplePhoto(style, Modifier.size(140.dp).clickable(onClickLabel = "${memory.place.name} 사진 ${index + 1}") {
                                onPhoto(memory.id, index)
                            })
                        }
                    }
                }
            }
        }
    }
}
