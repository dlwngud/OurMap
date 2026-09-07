package com.wngud.ourmap.feature.us

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wngud.ourmap.data.demo.DemoSession
import com.wngud.ourmap.ui.components.*

@Composable
fun UsScreen(session: DemoSession, onCreate: () -> Unit, onInvite: () -> Unit, onWishlist: () -> Unit,
    onMemory: (String) -> Unit, onRecords: () -> Unit) {
    PrototypePage("우리", subtitle = "소중한 사람과 함께 만드는 공간") {
        OurMapCard(Modifier.fillMaxWidth()) {
            ProfilePair(session.spaceName, session.partnerJoined, avatar = session.avatar)
            Text("${session.spaceStyle} ${session.spaceIntroduction}")
            Text("${session.startedOn}부터 함께 기록해요")
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Stat("장소", session.memories.map { it.place.id }.distinct().size)
                Stat("사진", session.memories.sumOf { it.photoCount })
                Stat("기록", session.memories.size)
            }
        }
        if (!session.partnerJoined) FeatureRow("상대 초대하기", onInvite, subtitle = "혼자 먼저 기록하고, 나중에 함께 볼 수 있어요.", emoji = "💌")
        if (session.memories.isEmpty()) {
            OurMapCard(Modifier.fillMaxWidth()) {
                Text("🎉 우리의 Space가 준비됐어요", style = MaterialTheme.typography.titleLarge)
                Text("첫 순간이 쌓이면 우리만의 지도가 완성돼요.")
                OurMapButton(onCreate, Modifier.fillMaxWidth()) { Text("첫 장소 기록하기") }
            }
        }
        FeatureRow("가보고 싶은 곳", onWishlist, subtitle = "${session.wishedPlaceIds.size}곳을 함께 모았어요", emoji = "🔖")
        Text("최근 함께한 순간", style = MaterialTheme.typography.titleLarge)
        session.memories.take(2).forEach { memory ->
            MemoryCard(memory, onClick = { onMemory(memory.id) })
        }
        if (session.memories.isNotEmpty()) TextButton(onRecords) { Text("기록 전체 보기") }
    }
}

@Composable
private fun Stat(label: String, count: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(count.toString(), style = MaterialTheme.typography.headlineMedium)
    }
}
