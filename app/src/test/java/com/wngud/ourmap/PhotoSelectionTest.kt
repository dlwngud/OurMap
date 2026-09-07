package com.wngud.ourmap

import com.wngud.ourmap.feature.memory.*
import org.junit.Assert.*
import org.junit.Test
import com.wngud.ourmap.core.model.Memory
import com.wngud.ourmap.data.demo.DemoContent
import kotlinx.serialization.json.Json

class PhotoSelectionTest {
    @Test fun photoReferencesAndCoverOrderSurviveSerialization() {
        val memory = DemoContent().memories.first().copy(photoStyles = emptyList(),
            photoUris = listOf("content://test/2", "content://test/1"))
        val restored = Json.decodeFromString<Memory>(Json.encodeToString(memory))
        assertEquals(memory, restored)
        assertEquals(2, restored.photoCount)
    }
    @Test fun cancellationKeepsPhotos() {
        assertEquals(listOf("a"), mergePhotoSelection(listOf("a"), emptyList()))
    }
    @Test fun mergesWithoutDuplicatesAndHonorsLimit() {
        assertEquals(listOf("a", "b"), mergePhotoSelection(listOf("a"), listOf("a", "b")))
        assertEquals(8, mergePhotoSelection(emptyList(), (0..20).map { "$it" }).size)
        assertEquals(5, mergePhotoSelection(emptyList(), (0..20).map { "$it" }, 3).size)
    }
    @Test fun coverChangesOrderWithoutDroppingPhotos() {
        assertEquals(listOf("b", "a", "c"), selectCover(listOf("a", "b", "c"), "b"))
        assertEquals(listOf("a"), selectCover(listOf("a"), "missing"))
    }
}
