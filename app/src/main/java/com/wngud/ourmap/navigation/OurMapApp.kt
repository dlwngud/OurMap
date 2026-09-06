package com.wngud.ourmap.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.wngud.ourmap.data.demo.DemoContent
import com.wngud.ourmap.feature.map.MapScreen
import com.wngud.ourmap.feature.records.RecordsScreen
import com.wngud.ourmap.feature.us.UsScreen
import com.wngud.ourmap.feature.my.MyScreen
import com.wngud.ourmap.ui.components.OurMapBottomSheet
import com.wngud.ourmap.ui.components.OurMapButton
import com.wngud.ourmap.ui.theme.OurMapTheme

@Composable
fun OurMapApp(viewModel: MainViewModel) {
    OurMapAppContent(demo = viewModel.demo)
}

@Composable
fun OurMapAppContent(demo: DemoContent, modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(MainDestination.Map)
    val selected = backStack.last() as MainDestination
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }
    // Tabs are peers. Replacing the root avoids accumulating tab history.
    val selectTab: (MainDestination) -> Unit = { destination ->
        if (selected != destination) {
            backStack.clear()
            backStack.add(destination)
        }
    }
    BackHandler(enabled = selected != MainDestination.Map && !showCreateSheet) {
        selectTab(MainDestination.Map)
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                MainDestination.entries.forEachIndexed { index, destination ->
                    if (index == 2) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            FloatingActionButton(onClick = { showCreateSheet = true }) {
                                Icon(Icons.Default.Add, contentDescription = "새 기록 작성")
                            }
                        }
                    }
                    NavigationBarItem(
                        selected = selected == destination,
                        onClick = { selectTab(destination) },
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            onBack = { selectTab(MainDestination.Map) },
            modifier = Modifier.padding(padding),
            entryProvider = { key ->
                NavEntry(key) {
                    when (key) {
                        MainDestination.Map -> MapScreen(demo.memories, onCreate = { showCreateSheet = true })
                        MainDestination.Records -> RecordsScreen(demo.memories)
                        MainDestination.Us -> UsScreen(demo.space, demo.memories, onCreate = { showCreateSheet = true })
                        MainDestination.My -> MyScreen(demo.space.memberNames.first())
                        else -> error("Unknown destination: $key")
                    }
                }
            },
        )
    }
    if (showCreateSheet) {
        OurMapBottomSheet(onDismiss = { showCreateSheet = false }) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("새로운 추억을 남겨요", style = MaterialTheme.typography.headlineSmall)
            Text("장소, 사진, 그날의 이야기를 담을 공간이에요.\n기록 작성 기능은 다음 단계에서 연결할 예정이에요.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OurMapButton(onClick = { showCreateSheet = false }, modifier = Modifier.fillMaxWidth()) { Text("둘러보기 계속하기") }
        }
    }
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
