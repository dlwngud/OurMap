package com.wngud.ourmap.feature.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wngud.ourmap.core.model.Memory
import com.wngud.ourmap.core.model.Place
import com.wngud.ourmap.feature.memory.PlacePicker
import com.wngud.ourmap.ui.components.*

@Composable
fun MapScreen(memories: List<Memory>, places: List<Place>, onCreate: () -> Unit, onPlace: (String) -> Unit) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var search by rememberSaveable { mutableStateOf(false) }
    val visited = memories.map { it.place }.distinctBy { it.id }
    val selected = places.firstOrNull { it.id == selectedId }
    PrototypePage("우리의 지도", subtitle = "함께한 순간을 한눈에") {
        OutlinedButton({ search = true }, Modifier.fillMaxWidth()) { Text("⌕  장소 검색") }
        OurMapCard(Modifier.fillMaxWidth()) {
            val surface = MaterialTheme.colorScheme.secondaryContainer
            val road = MaterialTheme.colorScheme.surface
            val river = MaterialTheme.colorScheme.tertiaryContainer
            BoxWithConstraints(Modifier.fillMaxWidth().height(310.dp)) {
                Canvas(Modifier.matchParentSize()) {
                    drawRect(surface)
                    repeat(9) { i ->
                        drawLine(road, Offset(size.width * i / 8, 0f), Offset(size.width * i / 8 + 60f, size.height), 10.dp.toPx())
                        drawLine(road, Offset(0f, size.height * i / 8), Offset(size.width, size.height * i / 8 - 40f), 8.dp.toPx())
                    }
                    val path = Path().apply {
                        moveTo(0f, size.height * .72f)
                        cubicTo(size.width * .35f, size.height * .3f, size.width * .6f, size.height * .9f, size.width, size.height * .42f)
                    }
                    drawPath(path, river, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 45.dp.toPx()))
                }
                visited.forEachIndexed { index, place ->
                    val x = listOf(.06f, .50f, .20f)[index % 3]
                    val y = listOf(.13f, .37f, .67f)[index % 3]
                    Surface(
                        modifier = Modifier.offset(x = maxWidth * x, y = maxHeight * y)
                            .widthIn(max = maxWidth * .48f).clickable { selectedId = place.id },
                        shape = MaterialTheme.shapes.medium,
                        color = if (selectedId == place.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp,
                    ) {
                        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(listOf("🌳", "☕", "🌊")[index % 3], fontSize = 24.sp)
                            Text(place.name, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                if (visited.isEmpty()) Text("첫 장소를 기록해 보세요", Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleMedium)
                Text("샘플 지도 · 실제 위치와 다릅니다", Modifier.align(Alignment.BottomStart).padding(8.dp),
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        if (selected != null) {
            OurMapCard(Modifier.fillMaxWidth()) {
                Text(selected.name, style = MaterialTheme.typography.titleLarge)
                Text("함께 남긴 기록 ${memories.count { it.place.id == selected.id }}개")
                OurMapButton({ onPlace(selected.id) }, Modifier.fillMaxWidth()) { Text("장소 자세히 보기") }
            }
        } else {
            Text("함께한 장소 ${visited.size}곳", style = MaterialTheme.typography.titleLarge)
            Text("지도 위 장소를 누르면 우리의 기록을 볼 수 있어요.")
        }
        OurMapButton(onCreate, Modifier.fillMaxWidth()) { Text("새로운 추억 남기기") }
    }
    if (search) PlacePicker(places, { search = false }, {
        search = false
        onPlace(it.id)
    })
}
