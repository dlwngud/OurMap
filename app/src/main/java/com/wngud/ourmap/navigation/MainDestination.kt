package com.wngud.ourmap.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
enum class MainDestination(val label: String) : NavKey {
    Map("지도"), Records("기록"), Us("우리"), My("마이"),
}
