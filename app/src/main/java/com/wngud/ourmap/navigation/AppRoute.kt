package com.wngud.ourmap.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable data object Login : AppRoute
    @Serializable data class Profile(val editing: Boolean = false) : AppRoute
    @Serializable data object SpaceEntry : AppRoute
    @Serializable data object CreateSpace : AppRoute
    @Serializable data object Invite : AppRoute
    @Serializable data object Waiting : AppRoute
    @Serializable data object JoinSpace : AppRoute
    @Serializable data class Editor(val placeId: String? = null) : AppRoute
    @Serializable data class MemoryDetail(val id: String) : AppRoute
    @Serializable data class PlaceDetail(val id: String) : AppRoute
    @Serializable data class Gallery(val memoryId: String, val index: Int = 0) : AppRoute
    @Serializable data object Wishlist : AppRoute
    @Serializable data class Info(val title: String, val message: String) : AppRoute
}
