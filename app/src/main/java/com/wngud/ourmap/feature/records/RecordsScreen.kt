package com.wngud.ourmap.feature.records

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.wngud.ourmap.core.model.Memory
import com.wngud.ourmap.domain.memory.MemoryFilter
import com.wngud.ourmap.ui.components.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecordsScreen(memories: List<Memory>, onMemory: (String) -> Unit, onPlace: (String) -> Unit,
    onPhoto: (String, Int) -> Unit, onCreate: () -> Unit) {
    var view by rememberSaveable { mutableStateOf("전체") }
    var query by rememberSaveable { mutableStateOf("") }
    var favoriteOnly by rememberSaveable { mutableStateOf(false) }
    var month by rememberSaveable { mutableStateOf<String?>(null) }
    var companion by rememberSaveable { mutableStateOf<String?>(null) }
    var tag by rememberSaveable { mutableStateOf<String?>(null) }
    var filtersOpen by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val filtered = MemoryFilter(query, favoriteOnly, month, companion, tag).apply(memories)
    PrototypePage("기록", subtitle = "한 장면씩, 차곡차곡 쌓인 우리의 이야기") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("전체", "사진", "장소").forEach { item -> OurMapChip(item, view == item, { view = item }) }
        }
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(),
            label = { Text("기록 검색") }, singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus(); keyboard?.hide() }))
        OurMapChip("찜한 추억만", favoriteOnly, { favoriteOnly = !favoriteOnly })
        TextButton({ filtersOpen = !filtersOpen }) { Text(if (filtersOpen) "상세 필터 접기" else "기간·사람·태그 필터") }
        if (filtersOpen) {
            Text("방문 월")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OurMapChip("전체 기간", month == null, { month = null })
                memories.map { it.visitedOn.take(7) }.distinct().sortedDescending().forEach {
                    OurMapChip(it, month == it, { month = it })
                }
            }
            Text("함께한 사람")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OurMapChip("전체 사람", companion == null, { companion = null })
                memories.map { it.companion }.distinct().forEach { OurMapChip(it, companion == it, { companion = it }) }
            }
            Text("태그")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OurMapChip("전체 태그", tag == null, { tag = null })
                memories.flatMap { it.tags }.distinct().forEach { OurMapChip("#$it", tag == it, { tag = it }) }
            }
        }
        if (query.isNotBlank() || favoriteOnly || month != null || companion != null || tag != null) {
            TextButton({ query = ""; favoriteOnly = false; month = null; companion = null; tag = null }) { Text("필터 초기화") }
        }
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
                if (filtered.all { it.photoCount == 0 }) Text("선택한 기록에는 사진이 없어요.")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filtered.forEach { memory ->
                        repeat(memory.photoCount) { index ->
                            MemoryPhoto(memory, index, Modifier.size(140.dp).clickable(onClickLabel = "${memory.place.name} 사진 ${index + 1}") {
                                onPhoto(memory.id, index)
                            })
                        }
                    }
                }
            }
        }
    }
}
