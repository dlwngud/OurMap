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
    onPlace: () -> Unit, onPhoto: (Int) -> Unit, persisted: Boolean = false) {
    PrototypePage("추억 상세", onBack = onBack, footer = {
        OurMapButton(onPlace, Modifier.fillMaxWidth()) { Text("장소의 기록 모아보기") }
    }) {
        if (memory.photoCount > 0) {
            MemoryPhoto(memory, 0, Modifier.fillMaxWidth().height(220.dp)
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
        Text("사진 모아보기 · ${memory.photoCount}장", style = MaterialTheme.typography.titleMedium)
        if (memory.photoCount == 0) Text("이 기록에는 사진이 없어요.")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(memory.photoCount) { index ->
                MemoryPhoto(memory, index, Modifier.size(128.dp).clickable(onClickLabel = "사진 ${index + 1} 보기") { onPhoto(index) })
            }
        }
        Text(if (persisted) "기기에 저장된 기록 · 서버 백업 없음" else "미리보기 기록 · 실제 저장 기능은 다음 단계에서 연결해요.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PlaceDetailScreen(place: Place, memories: List<Memory>, wished: Boolean, onBack: () -> Unit,
    onWish: () -> Unit, onCreate: () -> Unit, onMemory: (String) -> Unit) {
    PrototypePage("장소 상세", onBack = onBack, footer = {
        OurMapButton(onCreate, Modifier.fillMaxWidth()) { Text("이 장소에 기록 남기기") }
    }) {
        memories.firstOrNull { it.photoCount > 0 }?.let {
            MemoryPhoto(it, 0, Modifier.fillMaxWidth().height(180.dp))
        }
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
    var index by rememberSaveable(memory.id) { mutableIntStateOf(initialIndex.coerceIn(0, (memory.photoCount - 1).coerceAtLeast(0))) }
    PrototypePage("사진 모아보기", onBack = onBack) {
        if (memory.photoCount == 0) {
            Text("아직 사진이 없어요.")
        } else {
            MemoryPhoto(memory, index, Modifier.fillMaxWidth().aspectRatio(.85f))
            Text("${index + 1} / ${memory.photoCount}", Modifier.align(Alignment.CenterHorizontally))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton({ index-- }, enabled = index > 0) { Text("이전 사진") }
                TextButton({ index++ }, enabled = index < memory.photoCount - 1) { Text("다음 사진") }
            }
        }
    }
}
