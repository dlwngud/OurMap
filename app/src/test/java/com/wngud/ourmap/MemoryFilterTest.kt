package com.wngud.ourmap

import com.wngud.ourmap.data.demo.DemoContent
import com.wngud.ourmap.domain.memory.*
import org.junit.Assert.*
import org.junit.Test

class MemoryFilterTest {
    private val memories = DemoContent().memories
    @Test fun filtersAreCombinedAndDatesSortDescending() {
        val saved = memories.map { it.copy(favorite = true) }
        assertEquals(listOf("forest"), MemoryFilter("산책", true, "2026.05", "수빈", "산책").apply(saved).map { it.id })
        assertTrue(MemoryFilter(month = "2026.04", tag = "산책").apply(saved).isEmpty())
        assertEquals(listOf("forest", "cafe", "river"), MemoryFilter().apply(saved.reversed()).map { it.id })
    }
    @Test fun searchesAddressTagsAndTrimsWhitespace() {
        assertEquals(1, MemoryFilter("  햇살좋음 ").apply(memories).size)
        assertEquals(1, MemoryFilter("영등포구").apply(memories).size)
        assertTrue(MemoryFilter(favoriteOnly = true).apply(memories).isEmpty())
    }
    @Test fun rejectsMalformedAndImpossibleDates() {
        assertTrue(validMemoryDate("2024.02.29"))
        assertFalse(validMemoryDate("2026.02.29"))
        assertFalse(validMemoryDate("2026.5.21"))
        assertFalse(validMemoryDate("2026.05.21 trailing"))
    }
}
