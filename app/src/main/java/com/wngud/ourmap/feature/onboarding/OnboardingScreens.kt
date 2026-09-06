package com.wngud.ourmap.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wngud.ourmap.data.demo.DemoSession
import com.wngud.ourmap.data.demo.parseDate
import com.wngud.ourmap.ui.components.*

@Composable
fun LoginScreen(onStart: () -> Unit, onBrowse: () -> Unit, onJoin: () -> Unit) {
    PrototypePage("우리의 장소", footer = {
        Text("UI 체험용입니다. 실제 로그인·계정 생성은 하지 않아요.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }) {
        Spacer(Modifier.height(12.dp))
        OurMapCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("📍", fontSize = 64.sp)
                Text("🧡", Modifier.padding(top = 36.dp), fontSize = 40.sp)
                Text("📍", fontSize = 64.sp)
            }
            Text("둘만의 장소와\n순간을 기록해요", style = MaterialTheme.typography.headlineLarge)
            Text("지도로 추억을 남기고,\n가보고 싶은 곳도 함께 모아보세요.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OurMapButton(onStart, Modifier.fillMaxWidth()) { Text("카카오로 시작하기") }
        OutlinedButton(onClick = onStart, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Google로 계속하기") }
        TextButton(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("이메일로 시작하기") }
        HorizontalDivider()
        TextButton(onClick = onJoin, modifier = Modifier.fillMaxWidth()) { Text("초대 코드로 참여") }
        TextButton(onClick = onBrowse, modifier = Modifier.fillMaxWidth()) { Text("샘플 Space 둘러보기") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(session: DemoSession, editing: Boolean, onBack: () -> Unit, onNext: (String, String, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf(session.name) }
    var intro by rememberSaveable { mutableStateOf(session.introduction) }
    var avatar by rememberSaveable { mutableStateOf(session.avatar) }
    PrototypePage(if (editing) "프로필 편집" else "프로필 설정", onBack = onBack,
        subtitle = if (editing) "우리 공간에서 보여줄 프로필이에요." else "1 / 4 · 상대와 함께 볼 프로필이에요.",
        footer = {
            OurMapButton({ onNext(name.trim(), intro.trim(), avatar) }, Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()) { Text(if (editing) "프로필 저장" else "다음") }
        }) {
        OurMapCard(Modifier.fillMaxWidth()) {
            Text(avatar, Modifier.align(Alignment.CenterHorizontally), fontSize = 72.sp)
            OutlinedTextField(name, { if (it.length <= 20) name = it }, Modifier.fillMaxWidth(),
                label = { Text("이름 또는 닉네임") }, singleLine = true,
                supportingText = { Text("${name.length}/20") })
            OutlinedTextField(intro, { if (it.length <= 60) intro = it }, Modifier.fillMaxWidth(),
                label = { Text("한 줄 소개") }, minLines = 2)
            Text("대표 아이콘", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("🌿", "🌷", "☕", "📷", "🧡").forEach {
                    OurMapChip(it, it == avatar, { avatar = it })
                }
            }
        }
    }
}

@Composable
fun SpaceEntryScreen(onBack: () -> Unit, onCreate: () -> Unit, onJoin: () -> Unit) {
    PrototypePage("Space 시작하기", onBack = onBack, subtitle = "2 / 4 · 어떻게 시작할까요?") {
        Text("소중한 사람과\n하나의 지도를 만들어요", style = MaterialTheme.typography.headlineMedium)
        OurMapCard(Modifier.fillMaxWidth()) {
            Text("🧡", fontSize = 48.sp)
            Text("새 Space 만들기", style = MaterialTheme.typography.titleLarge)
            Text("상대를 초대해 둘만의 공간을 시작해요.")
            OurMapButton(onCreate, Modifier.fillMaxWidth()) { Text("새 Space 만들기") }
        }
        OurMapCard(Modifier.fillMaxWidth()) {
            Text("💌", fontSize = 48.sp)
            Text("초대받은 Space 참여하기", style = MaterialTheme.typography.titleLarge)
            Text("받은 초대 코드를 입력해 함께 시작해요.")
            OutlinedButton(onClick = onJoin, modifier = Modifier.fillMaxWidth()) { Text("초대 코드 입력") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateSpaceScreen(session: DemoSession, onBack: () -> Unit, onCreate: (String, String, String, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("${session.name}의 공간") }
    var date by rememberSaveable { mutableStateOf(session.startedOn) }
    var style by rememberSaveable { mutableStateOf(session.spaceStyle) }
    var intro by rememberSaveable { mutableStateOf(session.spaceIntroduction) }
    PrototypePage("새 Space 만들기", onBack = onBack, subtitle = "3 / 4 · 둘만의 기록이 쌓일 공간이에요.",
        footer = {
            OurMapButton({ onCreate(name.trim(), date, style, intro.trim()) }, Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && parseDate(date) != null) { Text("Space 만들기") }
        }) {
        Text(style, Modifier.align(Alignment.CenterHorizontally), fontSize = 64.sp)
        OurMapCard(Modifier.fillMaxWidth()) {
            OutlinedTextField(name, { if (it.length <= 30) name = it }, Modifier.fillMaxWidth(),
                label = { Text("Space 이름") }, singleLine = true)
            DateField("함께한 시작일", date, { date = it })
            Text("대표 스타일", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("🧡", "🌷", "📷", "☕", "📍").forEach { OurMapChip(it, it == style, { style = it }) }
            }
            OutlinedTextField(intro, { intro = it.take(60) }, Modifier.fillMaxWidth(),
                label = { Text("Space 소개 (선택)") }, minLines = 2,
                supportingText = { Text("${intro.length}/60") })
        }
        Text("상대가 참여하기 전에도 기록을 시작할 수 있어요.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun InviteScreen(session: DemoSession, onBack: () -> Unit, onCopy: () -> Unit, onWaiting: () -> Unit, onSkip: () -> Unit) {
    PrototypePage("상대 초대", onBack = onBack, subtitle = "4 / 4 · 함께할 사람을 초대해요.",
        footer = { OurMapButton(onSkip, Modifier.fillMaxWidth()) {
            Text(if (session.partnerJoined) "우리 공간으로 이동" else "나중에 하고 시작하기")
        } }) {
        OurMapCard(Modifier.fillMaxWidth()) {
            ProfilePair(session.spaceName, session.partnerJoined, avatar = session.avatar)
            Text("우리만의 Space가 만들어졌어요.", style = MaterialTheme.typography.titleMedium)
        }
        if (session.partnerJoined) {
            Text("이미 함께하고 있는 Space예요. 우리의 다음 추억을 남겨 보세요.")
        } else OurMapCard(Modifier.fillMaxWidth()) {
            Text("체험용 초대 코드", Modifier.align(Alignment.CenterHorizontally))
            Text(DemoSession.INVITE_CODE, Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Text("이 코드는 미리보기에서만 사용할 수 있어요.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = onCopy, modifier = Modifier.fillMaxWidth()) { Text("체험 코드 복사") }
            OurMapButton(onWaiting, Modifier.fillMaxWidth()) { Text("초대 대기 화면 보기") }
        }
    }
}

@Composable
fun WaitingScreen(session: DemoSession, onBack: () -> Unit, onResend: () -> Unit, onHome: () -> Unit, onSimulateJoin: () -> Unit) {
    PrototypePage("상대 참여 대기", onBack = onBack, footer = {
        OurMapButton(onHome, Modifier.fillMaxWidth()) { Text("우리 공간으로 이동") }
    }) {
        OurMapCard(Modifier.fillMaxWidth()) {
            ProfilePair(session.spaceName, false, avatar = session.avatar)
            Text("함께할 순간을 기다려요", style = MaterialTheme.typography.headlineSmall)
            Text("✓ Space 준비 완료")
            Text("○ 상대 참여 대기 중", color = MaterialTheme.colorScheme.primary)
            Text("미리보기에서는 초대 메시지를 실제로 보내지 않아요.",
                style = MaterialTheme.typography.bodySmall)
        }
        FeatureRow("초대 코드 다시 보기", onResend, emoji = "💌")
        OutlinedButton(onClick = onSimulateJoin, modifier = Modifier.fillMaxWidth()) { Text("상대 참여 시뮬레이션") }
    }
}

@Composable
fun JoinSpaceScreen(session: DemoSession, onBack: () -> Unit, onJoin: (String) -> Unit) {
    var code by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf(session.name) }
    var error by rememberSaveable { mutableStateOf(false) }
    PrototypePage("Space 초대", onBack = onBack, subtitle = "소중한 사람과 더 많은 추억을 만들어봐요.",
        footer = {
            OurMapButton({
                error = !DemoSession.isValidInvite(code)
                if (!error) onJoin(name.trim())
            }, Modifier.fillMaxWidth(), enabled = code.isNotBlank() && name.isNotBlank()) { Text("참여하고 시작하기") }
        }) {
        OurMapCard(Modifier.fillMaxWidth()) {
            Text("💌", fontSize = 56.sp)
            Text("주형님의 Space에 초대됐어요", style = MaterialTheme.typography.headlineSmall)
            Text("참여하면 이 공간의 지도와 기록을 함께 볼 수 있어요.")
        }
        OutlinedTextField(code, { code = it.take(12); error = false }, Modifier.fillMaxWidth(),
            label = { Text("초대 코드") }, singleLine = true, isError = error,
            supportingText = { Text(if (error) "초대 코드를 확인해 주세요." else "체험 코드: ${DemoSession.INVITE_CODE}") })
        OutlinedTextField(name, { name = it.take(20) }, Modifier.fillMaxWidth(),
            label = { Text("이름 또는 닉네임") }, singleLine = true)
        Text("참여를 누르면 샘플 Space가 열립니다. 실제 계정은 연결되지 않아요.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
