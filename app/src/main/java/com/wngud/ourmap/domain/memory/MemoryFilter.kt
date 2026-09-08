package com.wngud.ourmap.domain.memory

import com.wngud.ourmap.core.model.Memory
import java.text.SimpleDateFormat
import java.util.Locale

fun validMemoryDate(value: String): Boolean = value.matches(Regex("\\d{4}\\.\\d{2}\\.\\d{2}")) &&
    try { SimpleDateFormat("yyyy.MM.dd", Locale.ROOT).apply { isLenient = false }.parse(value) != null }
    catch (_: java.text.ParseException) { false }

data class MemoryFilter(val query: String = "", val favoriteOnly: Boolean = false,
    val month: String? = null, val companion: String? = null, val tag: String? = null) {
    fun apply(memories: List<Memory>): List<Memory> = memories.filter { memory ->
        val term = query.trim()
        (!favoriteOnly || memory.favorite) &&
            (month == null || memory.visitedOn.startsWith(month)) &&
            (companion == null || memory.companion == companion) &&
            (tag == null || tag in memory.tags) &&
            (term.isEmpty() || listOf(memory.place.name, memory.place.address, memory.note, memory.companion)
                .plus(memory.tags).any { it.contains(term, ignoreCase = true) })
    }.sortedWith(compareByDescending<Memory> { it.visitedOn }.thenByDescending { it.id })
}
