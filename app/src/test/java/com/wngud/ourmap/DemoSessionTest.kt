package com.wngud.ourmap

import com.wngud.ourmap.data.demo.*
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class DemoSessionTest {
    @Test fun inviteNormalizesWhitespaceCaseAndHyphen() {
        assertTrue(DemoSession.isValidInvite(" 8a2f-91b "))
        assertTrue(DemoSession.isValidInvite("8A2F91B"))
        assertFalse(DemoSession.isValidInvite("8A2F91"))
        assertFalse(DemoSession.isValidInvite(""))
    }

    @Test fun dateRejectsInvalidCalendarDay() {
        assertNull(parseDate("2026.02.30"))
        assertNull(parseDate("2026.2.1"))
        assertEquals("2024.02.29", formatDate(requireNotNull(parseDate("2024.02.29"))))
    }

    @Test fun restoredSessionKeepsNewMemoryAndFavorites() {
        val memory = DemoContent().memories.first()
        val session = DemoSession().withMemory(memory).toggleFavorite(memory.id)
        val restored = Json.decodeFromString(DemoSession.serializer(), Json.encodeToString(DemoSession.serializer(), session))
        assertEquals(1, restored.memories.size)
        assertTrue(restored.memories.single().favorite)
        assertEquals(memory.place, restored.memories.single().place)
    }

    @Test fun togglingWishNeverDuplicatesPlace() {
        val original = DemoSession(wishedPlaceIds = emptyList())
        assertEquals(listOf("forest"), original.toggleWish("forest").wishedPlaceIds)
        assertTrue(original.toggleWish("forest").toggleWish("forest").wishedPlaceIds.isEmpty())
    }
}
