package com.wngud.ourmap.feature.memory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wngud.ourmap.core.model.Memory
import com.wngud.ourmap.core.model.Place
import com.wngud.ourmap.ui.components.*

@Composable
fun MemoryDetailScreen(memory: Memory, onBack: () -> Unit, onFavorite: () -> Unit,
    onPlace: () -> Unit, onPhoto: (Int) -> Unit) {
    PrototypePage("추억 상세", onBack = onBack, footer = {
        OurMapButton(onPlace, Modifier.fillMaxWidth()) { Text("장소의 기록 모아보기") }
    }) {
        if (memory.photoStyles.isNotEmpty()) {
            SamplePhoto(memory.photoStyles.first(), Modifier.fillMaxWidth().height(220.dp)
                .clickable(onClickLabel = "대표 사진 보기") { onPhoto(0) })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(memory.place.name, Modifier.weight(1f), style = MaterialTheme.typography.headlineLarge)
            TextButton(onClick = onFavorite) { Text(if (memory.favorite) "♥ 찜 해제" else "♡ 추억 찜") }
        }
        Text("${memory.visitedOn} · ${memory.companion}과 함께", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OurMapCard(Modifier.fillMaxWidth()) {
            Text("그날의 기분 · ${memory.mood}", style = MaterialTheme.typography.titleMedium)
            Text(memory.note.ifBlank { "사진과 장소로 남긴 소중한 순간이에요." })
        }
        Text("사진 모아보기 · ${memory.photoStyles.size}장", style = MaterialTheme.typography.titleMedium)
        if (memory.photoStyles.isEmpty()) Text("이 기록에는 사진이 없어요.")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            memory.photoStyles.forEachIndexed { index, style ->
                SamplePhoto(style, Modifier.size(128.dp).clickable(onClickLabel = "사진 ${index + 1} 보기") { onPhoto(index) })
            }
        }
        Text("미리보기 기록 · 실제 저장 기능은 다음 단계에서 연결해요.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PlaceDetailScreen(place: Place, memories: List<Memory>, wished: Boolean, onBack: () -> Unit,
    onWish: () -> Unit, onCreate: () -> Unit, onMemory: (String) -> Unit) {
    PrototypePage("장소 상세", onBack = onBack, footer = {
        OurMapButton(onCreate, Modifier.fillMaxWidth()) { Text("이 장소에 기록 남기기") }
    }) {
        SamplePhoto(memories.firstOrNull()?.photoStyles?.firstOrNull() ?: 0, Modifier.fillMaxWidth().height(180.dp))
        Text(place.name, style = MaterialTheme.typography.headlineLarge)
        Text(place.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(onClick = onWish) { Text(if (wished) "가보고 싶은 곳에서 해제" else "가보고 싶은 곳에 저장") }
        Text("함께 남긴 기록 ${memories.size}개", style = MaterialTheme.typography.titleLarge)
        if (memories.isEmpty()) Text("아직 이 장소의 기록이 없어요.")
        memories.forEach { MemoryCard(it, onClick = { onMemory(it.id) }) }
    }
}

@Composable
fun GalleryScreen(memory: Memory, initialIndex: Int, onBack: () -> Unit) {
    var index by rememberSaveable { mutableIntStateOf(initialIndex.coerceIn(0, (memory.photoStyles.size - 1).coerceAtLeast(0))) }
    PrototypePage("사진 모아보기", onBack = onBack) {
        if (memory.photoStyles.isEmpty()) {
            Text("아직 사진이 없어요.")
        } else {
            SamplePhoto(memory.photoStyles[index], Modifier.fillMaxWidth().aspectRatio(.85f))
            Text("${index + 1} / ${memory.photoStyles.size}", Modifier.align(Alignment.CenterHorizontally))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton({ index-- }, enabled = index > 0) { Text("이전 사진") }
                TextButton({ index++ }, enabled = index < memory.photoStyles.lastIndex) { Text("다음 사진") }
            }
        }
    }
}
