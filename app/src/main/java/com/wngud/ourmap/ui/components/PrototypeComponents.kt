package com.wngud.ourmap.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wngud.ourmap.data.demo.formatDate
import com.wngud.ourmap.data.demo.parseDate

@Composable
fun PrototypePage(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxSize().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로")
            }
            Text(title, Modifier.weight(1f).padding(12.dp), style = MaterialTheme.typography.titleLarge)
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(50)) {
                Text("미리보기", Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            subtitle?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            content()
        }
        footer?.let { Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) { it() } }
    }
}

@Composable
fun FeatureRow(title: String, onClick: () -> Unit, modifier: Modifier = Modifier, subtitle: String? = null, emoji: String = "✦") {
    Surface(modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(emoji, fontSize = 24.sp)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Local illustrations stand in for photos; no network or media permissions required. */
@Composable
fun SamplePhoto(style: Int, modifier: Modifier = Modifier, label: Boolean = true) {
    val variant = Math.floorMod(style, 3)
    Box(modifier.clip(MaterialTheme.shapes.medium)) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            drawRect(listOf(Color(0xFFDDE8D2), Color(0xFFF1D4B4), Color(0xFFCEE4EE))[variant])
            when (variant) {
                0 -> {
                    drawCircle(Color(0xFFFFF1BA), w * .16f, Offset(w * .75f, h * .22f))
                    repeat(5) { i ->
                        val x = w * (i * .24f)
                        drawLine(Color(0xFF9C8060), Offset(x, h), Offset(x, h * .25f), w * .035f)
                        drawCircle(Color(0xFF6D9465), w * .20f, Offset(x, h * .25f))
                        drawCircle(Color(0xFF98B580), w * .16f, Offset(x + w * .06f, h * .13f))
                    }
                }
                1 -> {
                    drawRect(Color(0xFFBA8C6C), topLeft = Offset(0f, h * .65f))
                    drawCircle(Color(0xFFFFFAEF), w * .25f, Offset(w * .5f, h * .52f))
                    drawCircle(Color(0xFF926749), w * .17f, Offset(w * .5f, h * .52f))
                    drawCircle(Color(0xFFD2A77C), w * .11f, Offset(w * .5f, h * .52f))
                }
                else -> {
                    drawCircle(Color(0xFFFFD9A7), w * .19f, Offset(w * .72f, h * .29f))
                    drawRect(Color(0xFF91B9C8), topLeft = Offset(0f, h * .55f))
                    repeat(4) { i ->
                        val y = h * (.63f + i * .09f)
                        drawLine(Color(0xFFCAE4E6), Offset(w * .12f, y), Offset(w * .82f, y), 3.dp.toPx())
                    }
                }
            }
        }
        if (label) Text("샘플 이미지", Modifier.align(Alignment.BottomStart).padding(10.dp)
            .background(Color(0xBBFFFFFF), RoundedCornerShape(8.dp)).padding(6.dp),
            color = Color(0xFF403A31), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ProfilePair(name: String, joined: Boolean, modifier: Modifier = Modifier, avatar: String = "🌿") {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
            Text(avatar, Modifier.padding(18.dp), fontSize = 32.sp)
        }
        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
            Text(if (joined) "🌷" else "+", Modifier.padding(18.dp), fontSize = 32.sp)
        }
        Column(Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold)
            Text(if (joined) "함께하는 공간" else "상대를 기다리고 있어요",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(label: String, value: String, onChange: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    FeatureRow(label, onClick = { open = true }, subtitle = value, emoji = "🗓")
    if (open) {
        val state = rememberDatePickerState(initialSelectedDateMillis = parseDate(value))
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = { TextButton(onClick = {
                state.selectedDateMillis?.let { onChange(formatDate(it)) }
                open = false
            }, enabled = state.selectedDateMillis != null) { Text("선택") } },
            dismissButton = { TextButton(onClick = { open = false }) { Text("취소") } },
        ) { DatePicker(state = state) }
    }
}
