package com.wngud.ourmap.feature.memory

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wngud.ourmap.core.model.Memory
import com.wngud.ourmap.core.model.Place
import com.wngud.ourmap.data.demo.parseDate
import com.wngud.ourmap.data.demo.today
import com.wngud.ourmap.ui.components.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemoryEditorScreen(
    places: List<Place>,
    initialPlaceId: String?,
    companion: String,
    onBack: () -> Unit,
    onSave: (Memory) -> Unit,
) {
    var placeId by rememberSaveable { mutableStateOf(initialPlaceId) }
    val initialDate = rememberSaveable { today() }
    var date by rememberSaveable { mutableStateOf(initialDate) }
    var note by rememberSaveable { mutableStateOf("") }
    var mood by rememberSaveable { mutableStateOf("좋았어요") }
    var photos by rememberSaveable { mutableStateOf(listOf<Int>()) }
    var together by rememberSaveable { mutableStateOf(true) }
    var placePicker by rememberSaveable { mutableStateOf(false) }
    var photoPicker by rememberSaveable { mutableStateOf(false) }
    var discard by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val place = places.firstOrNull { it.id == placeId }
    val dirty = note.isNotEmpty() || photos.isNotEmpty() || placeId != initialPlaceId ||
        date != initialDate || mood != "좋았어요" || !together
    val exit: () -> Unit = { if (dirty) discard = true else onBack() }
    BackHandler(enabled = dirty && !placePicker && !photoPicker && !discard) { discard = true }

    PrototypePage("새 기록", onBack = exit, footer = {
        OurMapButton({
            if (place != null && !submitted) {
                submitted = true
                onSave(Memory(
                    id = "preview-" + java.util.UUID.randomUUID().toString(),
                    place = place,
                    visitedOn = date,
                    note = note.trim(),
                    companion = if (together) companion else "나",
                    tags = listOf(mood),
                    mood = mood,
                    photoStyles = photos,
                ))
            }
        }, Modifier.fillMaxWidth(), enabled = place != null && parseDate(date) != null && !submitted) {
            Text("기록 미리보기 저장")
        }
    }) {
        FeatureRow("장소 선택", { placePicker = true }, subtitle = place?.name ?: "어디에서 함께했나요?", emoji = "📍")
        place?.let {
            Text(it.address, style = MaterialTheme.typography.bodySmall)
        }
        DateField("방문 날짜", date, { date = it })
        OurMapCard(Modifier.fillMaxWidth()) {
            Text("함께한 사람", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OurMapChip(companion, together, { together = true })
                OurMapChip("나만", !together, { together = false })
            }
        }
        OurMapCard(Modifier.fillMaxWidth()) {
            Text("사진 추가", style = MaterialTheme.typography.titleMedium)
            if (photos.isEmpty()) Text("사진 없이도 기록할 수 있어요.")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                photos.forEach { style ->
                    Column {
                        SamplePhoto(style, Modifier.size(100.dp), label = false)
                        TextButton(onClick = { photos = photos - style }) { Text("사진 ${style + 1} 제거") }
                    }
                }
            }
            OutlinedButton(onClick = { photoPicker = true }) { Text("샘플 사진 선택") }
        }
        OurMapCard(Modifier.fillMaxWidth()) {
            Text("기분은 어땠나요?", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("최고였어요", "좋았어요", "평범했어요", "아쉬웠어요").forEach {
                    OurMapChip(it, mood == it, { mood = it })
                }
            }
        }
        OutlinedTextField(note, { note = it.take(200) }, Modifier.fillMaxWidth(),
            label = { Text("메모") }, placeholder = { Text("오래 기억하고 싶은 순간을 남겨요.") },
            minLines = 3, supportingText = { Text("${note.length}/200") })
        Text("저장 결과는 이 미리보기에서만 보여요. 기기 사진이나 서버에는 저장하지 않아요.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (placePicker) {
        PlacePicker(places, onDismiss = { placePicker = false }, onSelect = {
            placeId = it.id
            placePicker = false
        })
    }
    if (photoPicker) {
        OurMapBottomSheet(onDismiss = { photoPicker = false }) {
            Text("샘플 사진 고르기", style = MaterialTheme.typography.titleLarge)
            Text("기기 앨범 연결 전, 제공된 이미지로 구성을 확인해요.")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                (0..2).forEach { style ->
                    Column(Modifier.width(90.dp)) {
                        SamplePhoto(style, Modifier.size(90.dp), label = false)
                        OurMapChip("사진 ${style + 1}", style in photos, {
                            photos = if (style in photos) photos - style else photos + style
                        })
                    }
                }
            }
            OurMapButton({ photoPicker = false }, Modifier.fillMaxWidth()) { Text("선택 완료") }
        }
    }
    if (discard) {
        AlertDialog(onDismissRequest = { discard = false }, title = { Text("작성을 그만둘까요?") },
            text = { Text("아직 저장하지 않은 내용은 사라져요.") },
            confirmButton = { TextButton(onClick = onBack) { Text("그만두기") } },
            dismissButton = { TextButton(onClick = { discard = false }) { Text("계속 작성") } })
    }
}

@Composable
fun PlacePicker(places: List<Place>, onDismiss: () -> Unit, onSelect: (Place) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    OurMapBottomSheet(onDismiss = onDismiss) {
        Text("장소 검색", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("장소 이름 검색") }, singleLine = true)
        val filtered = places.filter { it.name.contains(query.trim()) || it.address.contains(query.trim()) }
        if (filtered.isEmpty()) Text("일치하는 샘플 장소가 없어요.")
        filtered.forEach { place ->
            FeatureRow(place.name, { onSelect(place) }, subtitle = place.address, emoji = "📍")
        }
        Text("제공된 샘플 장소에서 검색해요.", style = MaterialTheme.typography.bodySmall)
    }
}
