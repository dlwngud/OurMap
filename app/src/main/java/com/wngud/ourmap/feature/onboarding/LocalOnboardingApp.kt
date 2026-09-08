package com.wngud.ourmap.feature.onboarding

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.wngud.ourmap.data.demo.DemoContent
import com.wngud.ourmap.data.demo.DemoSession
import com.wngud.ourmap.data.demo.formatDate
import com.wngud.ourmap.domain.onboarding.*
import com.wngud.ourmap.domain.memory.MemoryOwner
import com.wngud.ourmap.feature.memory.PersistentHome
import com.wngud.ourmap.navigation.LocalAccountControls
import com.wngud.ourmap.navigation.OurMapAppContent
import com.wngud.ourmap.ui.components.*
import kotlinx.serialization.Serializable

@Serializable
private enum class LocalRoute : NavKey { Login, Profile, Choice, Create, Join, Invite, Waiting, Home, Account }

@Composable
fun LocalOnboardingApp(demo: DemoContent, model: OnboardingViewModel = viewModel()) {
    val state by model.state.collectAsStateWithLifecycle()
    LaunchedEffect(model) { model.load() }
    LocalOnboardingContent(state, demo, model::perform, model::load, model::acknowledge, model::dismissError,
        home = { controls ->
            val data = state.data
            if (data?.user != null && data.space != null) {
                PersistentHome(demo, controls, MemoryOwner(data.user.id, data.space.id))
            }
        })
}

@Composable
fun LocalOnboardingContent(
    state: OnboardingUiState,
    demo: DemoContent,
    onAction: (OnboardingAction) -> Unit,
    onRetry: () -> Unit,
    onAcknowledge: () -> Unit,
    onDismissError: () -> Unit,
    home: (@Composable (LocalAccountControls) -> Unit)? = null,
) {
    if (state.data == null) {
        Scaffold { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                if (state.loading) { CircularProgressIndicator(); Text("저장된 공간을 불러오고 있어요") }
                else { Text(state.error ?: "정보를 불러오지 못했어요."); Button(onRetry) { Text("다시 시도") } }
            }
        }
        return
    }
    val data = state.data
    val stack = rememberNavBackStack(initialRoute(data))
    val mainState = rememberSaveableStateHolder()
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    var renewConfirmation by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val session = data.toDemoSession()
    fun root(route: LocalRoute) { stack.clear(); stack.add(route) }
    fun push(route: LocalRoute) { if (stack.lastOrNull() != route) stack.add(route) }
    fun back() { if (stack.size > 1) stack.removeAt(stack.lastIndex) }

    // Navigation follows a successful disk commit, never a button press alone.
    LaunchedEffect(state.completed) {
        when (val action = state.completed) {
            null -> return@LaunchedEffect
            OnboardingAction.SignIn -> root(initialRoute(data))
            is OnboardingAction.SaveProfile -> if (data.space == null) root(LocalRoute.Choice) else back()
            is OnboardingAction.CreateSpace -> { root(LocalRoute.Home); push(LocalRoute.Invite) }
            is OnboardingAction.JoinSample, OnboardingAction.SimulatePartner -> root(LocalRoute.Home)
            OnboardingAction.SignOut, OnboardingAction.Reset -> {
                mainState.removeState("main")
                root(LocalRoute.Login)
            }
            OnboardingAction.RenewInvite -> Unit
        }
        onAcknowledge()
    }
    // Local login is a device profile, not proof of identity. Never expose home after logout.
    val validRoute = when {
        !data.signedIn -> LocalRoute.Login
        data.user?.profileComplete != true -> LocalRoute.Profile
        data.space == null && stack.last() in listOf(LocalRoute.Home, LocalRoute.Invite, LocalRoute.Waiting, LocalRoute.Account) -> LocalRoute.Choice
        else -> stack.last() as LocalRoute
    }
    LaunchedEffect(validRoute) { if (stack.last() != validRoute) root(validRoute) }

    Box(Modifier.fillMaxSize()) {
        NavDisplay(backStack = stack, onBack = { back() }, entryProvider = { route ->
            NavEntry(route) {
                if (route == LocalRoute.Home && data.space != null && data.signedIn) {
                    mainState.SaveableStateProvider("main") {
                        val controls = LocalAccountControls(session,
                            { push(LocalRoute.Profile) }, { push(LocalRoute.Invite) }, { push(LocalRoute.Account) },
                            companion = data.members.firstOrNull { it.userId != data.user?.id }?.name ?: "나")
                        if (home != null) home(controls) else OurMapAppContent(demo, account = controls)
                    }
                } else Scaffold { padding ->
                    Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
                        when (route) {
                            LocalRoute.Login -> PrototypePage("우리의 장소") {
                                Text("둘만의 장소와 순간을 기록해요", style = MaterialTheme.typography.headlineLarge)
                                Text("3주차 · 로컬 온보딩")
                                Text("이 기기에 프로필과 Space를 저장합니다. 실제 카카오·Google·이메일 인증은 아직 연결되지 않았어요.")
                                OurMapButton({ onAction(OnboardingAction.SignIn) }, Modifier.fillMaxWidth()) {
                                    Text(if (data.user == null) "로컬 체험 시작하기" else "저장된 프로필로 계속하기")
                                }
                                Text("이 기기의 사용자만 구분하는 체험 기능이며, 비밀번호나 토큰은 저장하지 않아요.")
                            }
                            LocalRoute.Profile -> ProfileScreen(session, data.space != null,
                                { if (data.space != null) back() else onAction(OnboardingAction.SignOut) }) { name, intro, avatar ->
                                onAction(OnboardingAction.SaveProfile(name, intro, avatar))
                            }
                            LocalRoute.Choice -> SpaceEntryScreen({ onAction(OnboardingAction.SignOut) },
                                { push(LocalRoute.Create) }, { push(LocalRoute.Join) })
                            LocalRoute.Create -> CreateSpaceScreen(session, ::back) { name, date, style, intro ->
                                onAction(OnboardingAction.CreateSpace(name, date, style, intro))
                            }
                            LocalRoute.Join -> LocalJoinScreen(session.name, ::back) { code, name ->
                                onAction(OnboardingAction.JoinSample(code, name))
                            }
                            LocalRoute.Invite, LocalRoute.Waiting -> LocalInviteContent(data,
                                waiting = route == LocalRoute.Waiting, onBack = ::back,
                                onHome = { root(LocalRoute.Home) },
                                onWaiting = { push(LocalRoute.Waiting) },
                                onCopy = {
                                    data.invite?.let {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("로컬 체험 초대 코드", it.code))
                                    }
                                }, onRenew = { renewConfirmation = true },
                                onSimulate = { onAction(OnboardingAction.SimulatePartner) })
                            LocalRoute.Account -> PrototypePage("로컬 계정 관리", onBack = ::back) {
                                Text("프로필·Space·초대·저장한 기록과 사진은 이 기기에 보관돼요. 서버 백업은 아직 없어요.")
                                OurMapButton({ onAction(OnboardingAction.SignOut) }, Modifier.fillMaxWidth()) { Text("로컬 로그아웃") }
                                Text("로그아웃해도 프로필과 Space는 남아 있어요. 같은 기기에서 다시 계속할 수 있어요.")
                                OutlinedButton({ confirmReset = true }) { Text("로컬 데이터 초기화") }
                            }
                            else -> Unit
                        }
                    }
                }
            }
        })
    }
    if (state.saving) {
        BackHandler { }
        Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
            Surface(shape = MaterialTheme.shapes.large) {
                Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator(Modifier.size(28.dp)); Text("저장 중이에요")
                }
            }
        }
    }
    state.error?.let { message ->
        AlertDialog(onDismissRequest = onDismissError, title = { Text("작업을 완료하지 못했어요") },
            text = { Text(message) }, confirmButton = { TextButton(onDismissError) { Text("입력으로 돌아가기") } })
    }
    if (confirmReset) AlertDialog(onDismissRequest = { confirmReset = false },
        title = { Text("로컬 데이터를 초기화할까요?") },
        text = { Text("이 기기에 저장된 프로필·Space·초대·기록과 앱에 복사한 사진이 삭제돼요. 원본 앨범은 유지되며 삭제된 앱 데이터는 되돌릴 수 없어요.") },
        confirmButton = { TextButton({ confirmReset = false; onAction(OnboardingAction.Reset) }) { Text("초기화") } },
        dismissButton = { TextButton({ confirmReset = false }) { Text("취소") } })
    if (renewConfirmation) AlertDialog(onDismissRequest = { renewConfirmation = false },
        title = { Text("초대 코드를 재발급할까요?") }, text = { Text("기존 코드는 더 이상 사용할 수 없어요.") },
        confirmButton = { TextButton({ renewConfirmation = false; onAction(OnboardingAction.RenewInvite) }) { Text("재발급") } },
        dismissButton = { TextButton({ renewConfirmation = false }) { Text("취소") } })
}

private fun initialRoute(data: Onboarding) = when {
    !data.signedIn -> LocalRoute.Login
    data.user?.profileComplete != true -> LocalRoute.Profile
    data.space == null -> LocalRoute.Choice
    else -> LocalRoute.Home
}

internal fun Onboarding.toDemoSession() = DemoSession(
    name = user?.name.orEmpty(), introduction = user?.introduction.orEmpty(), avatar = user?.avatar ?: "🌿",
    spaceName = space?.name ?: "우리의 장소", startedOn = space?.startedOn ?: com.wngud.ourmap.data.demo.today(),
    spaceStyle = space?.style ?: "🧡", spaceIntroduction = space?.introduction.orEmpty(),
    partnerJoined = status == SpaceStatus.Active, wishedPlaceIds = emptyList(),
)

@Composable
private fun LocalJoinScreen(initialName: String, onBack: () -> Unit, onJoin: (String, String) -> Unit) {
    var code by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf(initialName) }
    PrototypePage("Space 참여 체험", onBack = onBack, footer = {
        OurMapButton({ onJoin(code, name) }, Modifier.fillMaxWidth(), enabled = code.isNotBlank() && name.isNotBlank()) {
            Text("참여하고 시작하기")
        }
    }) {
        Text("서버 연결 전에는 다른 기기에서 만든 Space에 참여할 수 없어요.")
        Text("샘플 코드 ${OnboardingRules.SAMPLE_CODE}로 참여 흐름을 체험하세요. 생성 결과는 이 기기에 저장돼요.")
        OutlinedTextField(code, { code = it.take(12) }, Modifier.fillMaxWidth(), label = { Text("초대 코드") }, singleLine = true)
        OutlinedTextField(name, { name = it.take(20) }, Modifier.fillMaxWidth(), label = { Text("이름 또는 닉네임") }, singleLine = true)
    }
}

@Composable
private fun LocalInviteContent(data: Onboarding, waiting: Boolean, onBack: () -> Unit, onHome: () -> Unit,
    onWaiting: () -> Unit, onCopy: () -> Unit, onRenew: () -> Unit, onSimulate: () -> Unit) {
    PrototypePage(if (waiting) "상대 참여 대기" else "상대 초대", onBack = onBack,
        footer = { OurMapButton(onHome, Modifier.fillMaxWidth()) { Text("우리 공간으로 이동") } }) {
        Text(data.space?.name.orEmpty(), style = MaterialTheme.typography.headlineMedium)
        Text("프로필과 Space는 이 기기에 저장됐어요. 상대를 기다리는 동안에도 기록을 시작할 수 있어요.")
        if (data.status == SpaceStatus.Active) {
            Text("두 사람이 참여한 공간이에요")
            data.members.forEach { Text("${it.name} · ${if (it.owner) "만든 사람" else "참여자"}") }
        } else data.invite?.let { invite ->
            OurMapCard(Modifier.fillMaxWidth()) {
                Text("로컬 체험 초대 코드")
                Text(invite.code, style = MaterialTheme.typography.headlineMedium)
                Text("만료일: ${formatDate(invite.expiresAt)} · 7일 유효")
                Text("다른 기기에서는 사용할 수 없어요. 서버 연결 후 실제 공유를 지원합니다.")
                OutlinedButton(onCopy) { Text("체험 코드 복사") }
                TextButton(onRenew) { Text("초대 코드 재발급") }
            }
            if (!waiting) OurMapButton(onWaiting, Modifier.fillMaxWidth()) { Text("초대 대기 화면 보기") }
            OutlinedButton(onSimulate, Modifier.fillMaxWidth()) { Text("이 기기에서 상대 참여 시뮬레이션") }
        }
    }
}
