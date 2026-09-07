package com.wngud.ourmap.domain.onboarding

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Pure state transition. IDs, time and randomness are supplied by the data boundary. */
object OnboardingRules {
    const val SAMPLE_CODE = "8A2F-91B"
    const val INVITE_LIFETIME = 7L * 24 * 60 * 60 * 1000
    fun normalized(code: String) = code.trim().replace("-", "").uppercase(Locale.ROOT)

    fun apply(current: Onboarding, action: OnboardingAction, now: Long, id: String, code: String): Onboarding {
        if (action == OnboardingAction.SignIn) {
            return current.copy(signedIn = true, user = current.user ?: User(id))
        }
        if (action == OnboardingAction.Reset) return Onboarding()
        require(current.signedIn) { "먼저 로컬 체험을 시작해 주세요." }
        val user = requireNotNull(current.user)
        return when (action) {
            is OnboardingAction.SaveProfile -> {
                require(action.name.trim().length in 1..20) { "이름은 1~20자로 입력해 주세요." }
                require(action.introduction.length <= 60) { "소개는 60자까지 입력할 수 있어요." }
                require(action.avatar in listOf("🌿", "🌷", "☕", "📷", "🧡")) { "대표 아이콘을 선택해 주세요." }
                current.copy(user = user.copy(name = action.name.trim(), introduction = action.introduction.trim(),
                    avatar = action.avatar, profileComplete = true),
                    members = current.members.map { if (it.userId == user.id) it.copy(name = action.name.trim()) else it })
            }
            is OnboardingAction.CreateSpace -> {
                require(user.profileComplete) { "프로필을 먼저 저장해 주세요." }
                require(current.space == null) { "이미 참여 중인 Space가 있어요." }
                require(action.name.trim().length in 1..30) { "Space 이름은 1~30자로 입력해 주세요." }
                require(action.introduction.length <= 60) { "소개는 60자까지 입력할 수 있어요." }
                require(validDate(action.startedOn)) { "시작일을 확인해 주세요." }
                require(action.style in listOf("🧡", "🌷", "📷", "☕", "📍")) { "대표 스타일을 선택해 주세요." }
                val space = LocalSpace(id, action.name.trim(), action.startedOn, action.style, action.introduction.trim())
                current.copy(space = space, members = listOf(SpaceMember(user.id, id, user.name, true)),
                    invite = Invite(code, id, now + INVITE_LIFETIME))
            }
            OnboardingAction.RenewInvite -> {
                val space = requireNotNull(current.space) { "Space를 먼저 만들어 주세요." }
                require(current.members.any { it.userId == user.id && it.owner }) { "초대는 Space를 만든 사람이 관리해요." }
                require(current.status != SpaceStatus.Active) { "이미 두 사람이 참여한 Space예요." }
                current.copy(invite = Invite(code, space.id, now + INVITE_LIFETIME))
            }
            OnboardingAction.SimulatePartner -> {
                val space = requireNotNull(current.space)
                val invite = requireNotNull(current.invite)
                require(current.status != SpaceStatus.Active && !invite.accepted) { "이미 참여가 완료됐어요." }
                require(now < invite.expiresAt) { "초대가 만료됐어요. 코드를 재발급해 주세요." }
                current.copy(members = current.members + SpaceMember(id, space.id, "체험 상대", false),
                    invite = invite.copy(accepted = true))
            }
            is OnboardingAction.JoinSample -> {
                require(current.space == null) { "이미 참여 중인 Space가 있어요." }
                require(normalized(action.code) == normalized(SAMPLE_CODE)) { "초대 코드를 확인해 주세요." }
                require(action.name.trim().length in 1..20) { "이름은 1~20자로 입력해 주세요." }
                val name = action.name.trim()
                current.copy(user = user.copy(name = name, profileComplete = true),
                    space = LocalSpace(id, "주형 · $name", "2026.05.21", "🧡", "로컬 참여 체험 공간"),
                    members = listOf(SpaceMember("sample-owner", id, "주형", true), SpaceMember(user.id, id, name, false)),
                    invite = Invite(SAMPLE_CODE, id, now + INVITE_LIFETIME, accepted = true))
            }
            OnboardingAction.SignOut -> current.copy(signedIn = false)
            else -> error("Handled above")
        }
    }

    private fun validDate(value: String): Boolean {
        if (!Regex("\\d{4}\\.\\d{2}\\.\\d{2}").matches(value)) return false
        return try {
            SimpleDateFormat("yyyy.MM.dd", Locale.ROOT).apply {
                isLenient = false
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(value) != null
        } catch (_: java.text.ParseException) { false }
    }
}
