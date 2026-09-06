package com.wngud.ourmap.core.model

import kotlinx.serialization.Serializable

/** A place can have multiple visits. Coordinates are independent of any map SDK. */
@Serializable
data class Place(val id: String, val name: String, val address: String, val latitude: Double, val longitude: Double)

@Serializable
data class Memory(
    val id: String,
    val place: Place,
    val visitedOn: String,
    val note: String,
    val companion: String,
    val tags: List<String>,
    val mood: String = "좋았어요",
    val photoStyles: List<Int> = listOf(0, 1, 2),
    val favorite: Boolean = false,
)

@Serializable
data class Space(val id: String, val name: String, val memberNames: List<String>)
