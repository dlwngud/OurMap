package com.wngud.ourmap.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.wngud.ourmap.core.model.Memory

@Composable
fun DevicePhoto(uri: String, modifier: Modifier = Modifier) {
    var failed by remember(uri) { mutableStateOf(false) }
    Box(modifier) {
        AsyncImage(model = uri, contentDescription = "선택한 사진",
            modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop,
            onError = { failed = true }, onSuccess = { failed = false })
        if (failed) Text("사진을 불러올 수 없어요")
    }
}

@Composable
fun MemoryPhoto(memory: Memory, index: Int, modifier: Modifier = Modifier) {
    if (index < memory.photoUris.size) DevicePhoto(memory.photoUris[index], modifier)
    else memory.photoStyles.getOrNull(index - memory.photoUris.size)?.let { SamplePhoto(it, modifier) }
}
