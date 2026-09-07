package com.wngud.ourmap.navigation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.wngud.ourmap.data.demo.DemoContent
import com.wngud.ourmap.data.demo.DemoSession
import com.wngud.ourmap.feature.map.MapScreen
import com.wngud.ourmap.feature.records.RecordsScreen
import com.wngud.ourmap.feature.us.UsScreen
import com.wngud.ourmap.feature.us.WishlistScreen
import com.wngud.ourmap.feature.my.MyScreen
import com.wngud.ourmap.feature.memory.*
import com.wngud.ourmap.feature.onboarding.*
import com.wngud.ourmap.ui.components.*
import com.wngud.ourmap.ui.theme.OurMapTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private val SessionSaver = Saver<DemoSession, String>(
    save = { Json.encodeToString(DemoSession.serializer(), it) },
    restore = { Json.decodeFromString(DemoSession.serializer(), it) },
)

@Composable
fun OurMapApp(viewModel: MainViewModel) {
    LocalOnboardingApp(demo = viewModel.demo)
}

data class LocalAccountControls(
    val session: DemoSession,
    val onProfile: () -> Unit,
    val onInvite: () -> Unit,
    val onAccount: () -> Unit,
    val companion: String? = null,
)

@Composable
fun OurMapAppContent(demo: DemoContent, modifier: Modifier = Modifier, account: LocalAccountControls? = null) {
    val backStack = rememberNavBackStack(if (account == null) AppRoute.Login else MainDestination.Us)
    var session by rememberSaveable(stateSaver = SessionSaver) { mutableStateOf(account?.session ?: DemoSession()) }
    LaunchedEffect(account?.session) {
        account?.let { session = it.session.copy(memories = session.memories, wishedPlaceIds = session.wishedPlaceIds) }
    }
    var resetConfirmation by rememberSaveable { mutableStateOf(false) }
    val tabStates = rememberSaveableStateHolder()
    val current = backStack.last()
    val selected = backStack.firstOrNull() as? MainDestination
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val places = demo.memories.map { it.place }.distinctBy { it.id }

    fun replaceRoot(key: NavKey) {
        backStack.clear()
        backStack.add(key)
    }
    fun push(key: NavKey) {
        if (backStack.lastOrNull() != key) backStack.add(key)
    }
    fun back() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }
    fun info(title: String, message: String) { push(AppRoute.Info(title, message)) }
    fun openMemory(id: String) { push(AppRoute.MemoryDetail(id)) }
    fun openPlace(id: String) { push(AppRoute.PlaceDetail(id)) }
    fun browse() {
        session = DemoSession(
            spaceName = demo.space.name,
            startedOn = "2025.05.10",
            partnerJoined = true,
            memories = demo.memories,
        )
        replaceRoot(MainDestination.Map)
    }
    val create: () -> Unit = { push(AppRoute.Editor()) }
    BackHandler(enabled = backStack.size == 1 && selected != null && selected != MainDestination.Map) {
        replaceRoot(MainDestination.Map)
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (current is MainDestination) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    MainDestination.entries.forEachIndexed { index, destination ->
                        if (index == 2) {
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                FloatingActionButton(onClick = create) {
                                    Icon(Icons.Default.Add, contentDescription = "새 기록 작성")
                                }
                            }
                        }
                        NavigationBarItem(
                            selected = selected == destination,
                            onClick = { if (selected != destination) replaceRoot(destination) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            onBack = { back() },
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            entryProvider = { key ->
                NavEntry(key) {
                    when (key) {
                        is MainDestination -> tabStates.SaveableStateProvider(key) {
                            when (key) {
                                MainDestination.Map -> MapScreen(session.memories, places, create, ::openPlace)
                                MainDestination.Records -> RecordsScreen(session.memories, ::openMemory, ::openPlace,
                                    { id, index -> push(AppRoute.Gallery(id, index)) }, create)
                                MainDestination.Us -> UsScreen(session, create, { account?.onInvite?.invoke() ?: push(AppRoute.Invite) },
                                    { push(AppRoute.Wishlist) }, ::openMemory, { replaceRoot(MainDestination.Records) })
                                MainDestination.My -> MyScreen(session, { account?.onProfile?.invoke() ?: push(AppRoute.Profile(editing = true)) },
                                    { replaceRoot(MainDestination.Records) }, { push(AppRoute.Wishlist) },
                                    { account?.onInvite?.invoke() ?: push(AppRoute.Invite) }, ::info,
                                    { account?.onAccount?.invoke() ?: run { resetConfirmation = true } },
                                    accountLabel = if (account == null) "처음부터 다시 체험" else "로컬 계정 관리")
                            }
                        }
                        AppRoute.Login -> LoginScreen(
                            onStart = { push(AppRoute.Profile()) },
                            onBrowse = ::browse,
                            onJoin = { session = session.copy(name = "수빈"); push(AppRoute.JoinSpace) },
                        )
                        is AppRoute.Profile -> ProfileScreen(session, key.editing, ::back) { name, intro, avatar ->
                            session = session.copy(name = name, introduction = intro, avatar = avatar)
                            if (key.editing) back() else push(AppRoute.SpaceEntry)
                        }
                        AppRoute.SpaceEntry -> SpaceEntryScreen(::back,
                            { push(AppRoute.CreateSpace) }, { push(AppRoute.JoinSpace) })
                        AppRoute.CreateSpace -> CreateSpaceScreen(session, ::back) { name, date, style, intro ->
                            session = session.copy(spaceName = name, startedOn = date, spaceStyle = style,
                                spaceIntroduction = intro,
                                memories = emptyList(), partnerJoined = false, wishedPlaceIds = emptyList())
                            // Completed setup is removed from history so Back cannot create another Space.
                            replaceRoot(MainDestination.Us)
                            push(AppRoute.Invite)
                        }
                        AppRoute.Invite -> InviteScreen(session, ::back, onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("OurMap 체험 코드", DemoSession.INVITE_CODE))
                            scope.launch { snackbar.showSnackbar("체험 코드를 복사했어요") }
                        }, onWaiting = { push(AppRoute.Waiting) }, onSkip = { replaceRoot(MainDestination.Us) })
                        AppRoute.Waiting -> WaitingScreen(session, ::back, onResend = ::back,
                            onHome = { replaceRoot(MainDestination.Us) },
                            onSimulateJoin = {
                                session = session.copy(partnerJoined = true)
                                replaceRoot(MainDestination.Us)
                                scope.launch { snackbar.showSnackbar("상대 참여를 시뮬레이션했어요") }
                            })
                        AppRoute.JoinSpace -> JoinSpaceScreen(session, ::back) { name ->
                            session = session.copy(name = name, spaceName = "주형 · $name",
                                partnerJoined = true, memories = demo.memories)
                            replaceRoot(MainDestination.Us)
                        }
                        is AppRoute.Editor -> MemoryEditorScreen(places, key.placeId,
                            companion = account?.companion ?: if (session.partnerJoined) {
                                if (session.name == "수빈") "주형" else "수빈"
                            } else session.name,
                            onBack = ::back,
                            onSave = { memory ->
                                session = session.withMemory(memory)
                                backStack[backStack.lastIndex] = AppRoute.MemoryDetail(memory.id)
                            },
                        )
                        is AppRoute.MemoryDetail -> {
                            val memory = session.memories.firstOrNull { it.id == key.id }
                            if (memory == null) MissingContent(::back) else MemoryDetailScreen(memory, ::back,
                                { session = session.toggleFavorite(memory.id) },
                                { openPlace(memory.place.id) },
                                { push(AppRoute.Gallery(memory.id, it)) })
                        }
                        is AppRoute.PlaceDetail -> {
                            val place = places.firstOrNull { it.id == key.id }
                            if (place == null) MissingContent(::back) else PlaceDetailScreen(place,
                                session.memories.filter { it.place.id == key.id }, place.id in session.wishedPlaceIds,
                                ::back, { session = session.toggleWish(place.id) },
                                { push(AppRoute.Editor(place.id)) }, ::openMemory)
                        }
                        is AppRoute.Gallery -> {
                            val memory = session.memories.firstOrNull { it.id == key.memoryId }
                            if (memory == null) MissingContent(::back) else GalleryScreen(memory, key.index, ::back)
                        }
                        AppRoute.Wishlist -> WishlistScreen(places, session.wishedPlaceIds, ::back,
                            ::openPlace, { session = session.toggleWish(it) })
                        is AppRoute.Info -> PrototypePage(key.title, onBack = ::back) {
                            OurMapCard(Modifier.fillMaxWidth()) { Text(key.message) }
                        }
                    }
                }
            },
        )
    }
    if (resetConfirmation) AlertDialog(
        onDismissRequest = { resetConfirmation = false },
        title = { Text("처음부터 다시 체험할까요?") },
        text = { Text("미리보기에서 작성한 기록과 설정이 초기화돼요.") },
        confirmButton = {
            TextButton(onClick = {
                resetConfirmation = false
                session = DemoSession()
                replaceRoot(AppRoute.Login)
                MainDestination.entries.forEach(tabStates::removeState)
            }) { Text("초기화") }
        },
        dismissButton = { TextButton(onClick = { resetConfirmation = false }) { Text("취소") } },
    )
}

@Composable
private fun MissingContent(onBack: () -> Unit) {
    PrototypePage("기록을 찾을 수 없어요", onBack = onBack) { Text("뒤로 돌아가 다른 기록을 선택해 주세요.") }
}

private val MainDestination.icon: ImageVector
    get() = when (this) {
        MainDestination.Map -> Icons.Default.LocationOn
        MainDestination.Records -> Icons.Default.DateRange
        MainDestination.Us -> Icons.Default.Favorite
        MainDestination.My -> Icons.Default.Person
    }

@Preview(showBackground = true, locale = "ko", widthDp = 390, heightDp = 844)
@Composable
private fun AppPreview() { OurMapTheme { OurMapAppContent(DemoContent()) } }

@Preview(showBackground = true, locale = "ko", widthDp = 390, heightDp = 844)
@Composable
private fun DarkAppPreview() { OurMapTheme(darkTheme = true) { OurMapAppContent(DemoContent()) } }
