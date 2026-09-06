package com.wngud.ourmap.feature.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wngud.ourmap.core.model.Memory
import com.wngud.ourmap.ui.components.*

@Composable
fun MapScreen(memories: List<Memory>, onCreate: () -> Unit, modifier: Modifier = Modifier) {
    ScreenContent("우리의 지도", "함께한 순간이 장소가 되는 곳", modifier) {
        OurMapCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().heightIn(min = 180.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Text("우리의 장소가 여기에 모여요", style = MaterialTheme.typography.titleMedium)
                Text("지도 연결 준비 중", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OurMapButton(onClick = onCreate, modifier = Modifier.fillMaxWidth()) { Text("첫 장소 기록하기") }
        }
        Text("최근 함께한 장소", style = MaterialTheme.typography.titleMedium)
        memories.firstOrNull()?.let { MemoryCard(it) }
    }
}
