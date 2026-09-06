package com.wngud.ourmap.data.demo

import com.wngud.ourmap.core.model.Memory
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Small, saved-instance-state prototype. No authentication or durable storage. */
@Serializable
data class DemoSession(
    val name: String = "주형",
    val introduction: String = "추억을 기록하는 중",
    val avatar: String = "🌿",
    val spaceName: String = "우리의 장소",
    val spaceIntroduction: String = "우리의 장소와 순간을 담는 공간",
    val startedOn: String = today(),
    val spaceStyle: String = "🧡",
    val partnerJoined: Boolean = false,
    val memories: List<Memory> = emptyList(),
    val wishedPlaceIds: List<String> = listOf("seoul-forest", "seongsu-cafe"),
) {
    fun withMemory(memory: Memory) = copy(memories = listOf(memory) + memories)

    fun toggleFavorite(id: String) = copy(
        memories = memories.map { if (it.id == id) it.copy(favorite = !it.favorite) else it },
    )

    fun toggleWish(placeId: String) = copy(
        wishedPlaceIds = if (placeId in wishedPlaceIds) wishedPlaceIds - placeId else wishedPlaceIds + placeId,
    )

    companion object {
        const val INVITE_CODE = "8A2F-91B"
        fun isValidInvite(code: String) = code.trim().replace("-", "").uppercase(Locale.ROOT) == "8A2F91B"
    }
}

// Calendar selection uses UTC milliseconds, but "today" follows the device's time zone.
fun today(): String = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date())

fun formatDate(millis: Long): String = dateFormat().format(Date(millis))

fun parseDate(value: String): Long? = runCatching {
    require(Regex("\\d{4}\\.\\d{2}\\.\\d{2}").matches(value))
    dateFormat().parse(value)?.time
}.getOrNull()

private fun dateFormat() = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).apply {
    isLenient = false
    timeZone = TimeZone.getTimeZone("UTC")
}
