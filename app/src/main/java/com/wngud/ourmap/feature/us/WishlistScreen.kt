package com.wngud.ourmap.feature.us

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wngud.ourmap.core.model.Place
import com.wngud.ourmap.feature.memory.PlacePicker
import com.wngud.ourmap.ui.components.*

@Composable
fun WishlistScreen(places: List<Place>, wishedIds: List<String>, onBack: () -> Unit,
    onPlace: (String) -> Unit, onToggle: (String) -> Unit) {
    var picker by rememberSaveable { mutableStateOf(false) }
    PrototypePage("가보고 싶은 곳", onBack = onBack, subtitle = "다음에 함께할 장소를 모아봐요.",
        footer = { OurMapButton({ picker = true }, Modifier.fillMaxWidth()) { Text("장소 추가하기") } }) {
        Text("전체 ${wishedIds.size}곳", style = MaterialTheme.typography.titleLarge)
        if (wishedIds.isEmpty()) Text("아직 저장한 장소가 없어요.")
        places.filter { it.id in wishedIds }.forEachIndexed { index, place ->
            OurMapCard(Modifier.fillMaxWidth()) {
                SamplePhoto(index, Modifier.fillMaxWidth().height(140.dp))
                FeatureRow(place.name, { onPlace(place.id) }, subtitle = place.address, emoji = "📍")
                OutlinedButton({ onToggle(place.id) }) { Text("저장 해제") }
            }
        }
    }
    if (picker) PlacePicker(places, { picker = false }, {
        if (it.id !in wishedIds) onToggle(it.id)
        picker = false
    })
}
