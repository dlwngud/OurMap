package com.wngud.ourmap.core.model

/** A place can have multiple visits. Coordinates are independent of any map SDK. */
data class Place(val id: String, val name: String, val address: String, val latitude: Double, val longitude: Double)

data class Memory(
    val id: String,
    val place: Place,
    val visitedOn: String,
    val note: String,
    val companion: String,
    val tags: List<String>,
)

data class Space(val id: String, val name: String, val memberNames: List<String>)
