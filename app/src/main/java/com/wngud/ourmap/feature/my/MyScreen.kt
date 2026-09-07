package com.wngud.ourmap.feature.my

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.wngud.ourmap.data.demo.DemoSession
import com.wngud.ourmap.ui.components.*

@Composable
fun MyScreen(session: DemoSession, onProfile: () -> Unit, onRecords: () -> Unit, onWishlist: () -> Unit,
    onInvite: () -> Unit, onInfo: (String, String) -> Unit, onRestart: () -> Unit,
    accountLabel: String = "처음부터 다시 체험") {
    PrototypePage("마이", subtitle = "나의 기록과 취향을 담는 곳") {
        OurMapCard(Modifier.fillMaxWidth()) {
            Text(session.avatar, fontSize = 48.sp)
            Text(session.name, style = MaterialTheme.typography.headlineMedium)
            Text(session.introduction)
            OutlinedButton(onProfile) { Text("프로필 편집") }
        }
        Text("내 활동", style = MaterialTheme.typography.titleLarge)
        FeatureRow("기록 모아보기", onRecords, emoji = "📒")
        FeatureRow("저장한 장소", onWishlist, emoji = "📍")
        Text("설정 및 관리", style = MaterialTheme.typography.titleLarge)
        FeatureRow("알림 설정", { onInfo("알림 설정", "상대 참여와 새로운 추억 소식을 받아볼 수 있는 설정이에요. 실제 알림 전송과 권한 요청은 후속 단계에서 연결합니다.") }, emoji = "🔔")
        FeatureRow("기념일 관리", { onInfo("기념일 관리", "함께한 시작일은 ${session.startedOn}이에요. 추가 기념일을 관리하는 기능은 후속 단계에서 연결합니다.") }, emoji = "🗓")
        FeatureRow("초대 관리", onInvite, emoji = "💌")
        FeatureRow("앱 설정", { onInfo("앱 설정", "테마는 시스템의 밝은 모드·어두운 모드를 따라갑니다. 현재 앱은 샘플 데이터 기반 UI 미리보기입니다.") }, emoji = "⚙")
        FeatureRow("도움말", { onInfo("도움말", "지도에서 장소를 누르거나 + 버튼을 눌러 추억을 작성해 보세요. 샘플 이미지만 사용하며 실제 계정·서버에는 저장하지 않습니다.") }, emoji = "❔")
        TextButton(onRestart, Modifier.fillMaxWidth()) { Text(accountLabel) }
    }
}
