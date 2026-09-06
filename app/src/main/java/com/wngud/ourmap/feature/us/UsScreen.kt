package com.wngud.ourmap.feature.us

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wngud.ourmap.core.model.Memory
import com.wngud.ourmap.core.model.Space
import com.wngud.ourmap.ui.components.*

@Composable
fun UsScreen(space: Space, memories: List<Memory>, onCreate: () -> Unit, modifier: Modifier = Modifier) {
    ScreenContent("우리", "소중한 사람과 함께 만드는 공간", modifier) {
        OurMapCard(Modifier.fillMaxWidth()) {
            Text(space.name, style = MaterialTheme.typography.headlineMedium)
            Text("둘만의 장소와 순간을 기록해요", color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider()
            Text("함께한 장소 ${memories.map { it.place.id }.distinct().size}곳 · 기록 ${memories.size}개")
        }
        OurMapCard(Modifier.fillMaxWidth()) {
            Text("우리의 시작", style = MaterialTheme.typography.titleLarge)
            Text("작은 산책부터 특별한 여행까지, 오래 기억하고 싶은 순간을 남겨 보세요.")
            OurMapButton(onClick = onCreate, modifier = Modifier.fillMaxWidth()) { Text("새로운 추억 남기기") }
        }
    }
}
