package com.wngud.ourmap.feature.my

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wngud.ourmap.ui.components.*

@Composable
fun MyScreen(name: String, modifier: Modifier = Modifier) {
    ScreenContent("마이", "나의 기록과 취향을 담는 곳", modifier) {
        OurMapCard(Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary)
            Text(name, style = MaterialTheme.typography.headlineMedium)
            Text("추억을 기록하는 중", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OurMapCard(Modifier.fillMaxWidth()) {
            Text("앱 안내", style = MaterialTheme.typography.titleLarge)
            Text("OurMap · 우리의 장소")
            Text("시스템 설정에 맞춰 밝은 테마와 어두운 테마가 적용돼요.")
            Text("프로필 편집과 계정 설정은 다음 단계에서 만나요.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
