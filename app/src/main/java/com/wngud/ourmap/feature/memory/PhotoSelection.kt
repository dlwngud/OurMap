package com.wngud.ourmap.feature.memory

const val MAX_MEMORY_PHOTOS = 8

/** Picker fallbacks may ignore their limit; enforce it again at the app boundary. */
fun mergePhotoSelection(current: List<String>, selected: List<String>, sampleCount: Int = 0): List<String> =
    (current + selected).distinct().take((MAX_MEMORY_PHOTOS - sampleCount).coerceAtLeast(0))

fun selectCover(photos: List<String>, uri: String): List<String> =
    if (uri in photos) listOf(uri) + (photos - uri) else photos
