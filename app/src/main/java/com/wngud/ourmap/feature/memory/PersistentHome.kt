package com.wngud.ourmap.feature.memory

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wngud.ourmap.data.demo.DemoContent
import com.wngud.ourmap.domain.memory.MemoryOwner
import com.wngud.ourmap.navigation.*

@Composable
fun PersistentHome(demo: DemoContent, account: LocalAccountControls, owner: MemoryOwner,
    model: MemoryViewModel = viewModel()) {
    val state by model.state.collectAsStateWithLifecycle()
    var retry by remember { mutableIntStateOf(0) }
    LaunchedEffect(model, owner, retry) { model.observe(owner) }
    if (state.owner != owner || state.memories == null) {
        Scaffold { padding -> Column(Modifier.padding(padding)) {
            if (state.owner == owner && state.error != null) {
                Text(state.error.orEmpty()); Button({ retry++ }) { Text("기록 다시 불러오기") }
                TextButton(account.onAccount) { Text("로컬 계정 관리") }
            } else { CircularProgressIndicator(); Text("기록을 불러오고 있어요") }
        } }
    } else {
        OurMapAppContent(demo, account = account, stored = StoredMemoryControls(state.memories.orEmpty(),
            { model.save(owner, it) }, { model.toggleFavorite(owner, it) }))
        state.error?.let { message ->
            AlertDialog(onDismissRequest = {}, title = { Text("기록 읽기 오류") }, text = { Text(message) },
                confirmButton = { TextButton({ retry++ }) { Text("다시 불러오기") } })
        }
    }
}
