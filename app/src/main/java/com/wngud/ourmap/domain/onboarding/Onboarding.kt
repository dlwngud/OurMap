package com.wngud.ourmap.domain.onboarding

data class User(
    val id: String,
    val name: String = "",
    val introduction: String = "",
    val avatar: String = "🌿",
    val profileComplete: Boolean = false,
)

data class LocalSpace(
    val id: String,
    val name: String,
    val startedOn: String,
    val style: String,
    val introduction: String,
)

data class SpaceMember(val userId: String, val spaceId: String, val name: String, val owner: Boolean)
data class Invite(val code: String, val spaceId: String, val expiresAt: Long, val accepted: Boolean = false)
enum class SpaceStatus { Creating, WaitingForPartner, Active }

data class Onboarding(
    val signedIn: Boolean = false,
    val user: User? = null,
    val space: LocalSpace? = null,
    val members: List<SpaceMember> = emptyList(),
    val invite: Invite? = null,
) {
    val status: SpaceStatus get() = when {
        space == null -> SpaceStatus.Creating
        members.size < 2 -> SpaceStatus.WaitingForPartner
        else -> SpaceStatus.Active
    }
}

sealed interface OnboardingAction {
    data object SignIn : OnboardingAction
    data class SaveProfile(val name: String, val introduction: String, val avatar: String) : OnboardingAction
    data class CreateSpace(val name: String, val startedOn: String, val style: String, val introduction: String) : OnboardingAction
    data class JoinSample(val code: String, val name: String) : OnboardingAction
    data object RenewInvite : OnboardingAction
    data object SimulatePartner : OnboardingAction
    data object SignOut : OnboardingAction
    data object Reset : OnboardingAction
}

interface OnboardingRepository {
    suspend fun load(): Onboarding
    suspend fun perform(action: OnboardingAction): Onboarding
}
