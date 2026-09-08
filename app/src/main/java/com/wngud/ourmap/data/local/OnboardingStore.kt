package com.wngud.ourmap.data.local

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.wngud.ourmap.domain.onboarding.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Bounded onboarding data only. Memories and images belong in Room, not this file. */
@Serializable
data class OnboardingRecord(
    val version: Int = 1,
    val signedIn: Boolean = false,
    val user: UserRecord? = null,
    val space: SpaceRecord? = null,
    val members: List<MemberRecord> = emptyList(),
    val invite: InviteRecord? = null,
) {
    fun toDomain() = Onboarding(signedIn, user?.let { User(it.id, it.name, it.introduction, it.avatar, it.complete) },
        space?.let { LocalSpace(it.id, it.name, it.date, it.style, it.introduction) },
        members.map { SpaceMember(it.userId, it.spaceId, it.name, it.owner) },
        invite?.let { Invite(it.code, it.spaceId, it.expiresAt, it.accepted) })
}
@Serializable data class UserRecord(val id: String, val name: String, val introduction: String, val avatar: String, val complete: Boolean)
@Serializable data class SpaceRecord(val id: String, val name: String, val date: String, val style: String, val introduction: String)
@Serializable data class MemberRecord(val userId: String, val spaceId: String, val name: String, val owner: Boolean)
@Serializable data class InviteRecord(val code: String, val spaceId: String, val expiresAt: Long, val accepted: Boolean)

private fun Onboarding.toRecord() = OnboardingRecord(signedIn = signedIn,
    user = user?.let { UserRecord(it.id, it.name, it.introduction, it.avatar, it.profileComplete) },
    space = space?.let { SpaceRecord(it.id, it.name, it.startedOn, it.style, it.introduction) },
    members = members.map { MemberRecord(it.userId, it.spaceId, it.name, it.owner) },
    invite = invite?.let { InviteRecord(it.code, it.spaceId, it.expiresAt, it.accepted) })

object OnboardingSerializer : Serializer<OnboardingRecord> {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    override val defaultValue = OnboardingRecord()
    override suspend fun readFrom(input: InputStream): OnboardingRecord = try {
        json.decodeFromString<OnboardingRecord>(input.readBytes().decodeToString()).also {
            if (it.version != 1) throw CorruptionException("Unsupported onboarding version")
        }
    } catch (e: SerializationException) {
        // Never silently replace a damaged profile or Space with empty data.
        throw CorruptionException("Cannot read onboarding", e)
    }
    override suspend fun writeTo(t: OnboardingRecord, output: OutputStream) {
        output.write(json.encodeToString(OnboardingRecord.serializer(), t).encodeToByteArray())
    }
}

private val Context.onboardingStore by dataStore("onboarding.json", OnboardingSerializer)

class DataStoreOnboardingRepository(
    private val store: DataStore<OnboardingRecord>,
    private val now: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() },
    private val newCode: () -> String = ::generateInviteCode,
) : OnboardingRepository {
    override suspend fun load() = store.data.first().toDomain()
    override suspend fun perform(action: OnboardingAction): Onboarding {
        val timestamp = now()
        val id = newId()
        return store.updateData { record ->
            val current = record.toDomain()
            val needsCode = action is OnboardingAction.CreateSpace || action == OnboardingAction.RenewInvite
            val code = if (needsCode) (1..10).asSequence().map { newCode() }.firstOrNull {
                OnboardingRules.normalized(it) != current.invite?.code?.let(OnboardingRules::normalized) &&
                    OnboardingRules.normalized(it) != OnboardingRules.normalized(OnboardingRules.SAMPLE_CODE)
            } ?: throw java.io.IOException("Could not generate a new invite") else ""
            OnboardingRules.apply(current, action, timestamp, id, code).toRecord()
        }.toDomain()
    }
}

private fun generateInviteCode(): String {
    val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    val random = SecureRandom()
    return List(8) { alphabet[random.nextInt(alphabet.length)] }.joinToString("").chunked(4).joinToString("-")
}

@Singleton
class LocalOnboardingRepository @Inject constructor(@ApplicationContext context: Context,
    private val memories: com.wngud.ourmap.domain.memory.MemoryRepository) : OnboardingRepository {
    private val delegate = DataStoreOnboardingRepository(context.onboardingStore)
    override suspend fun load() = delegate.load()
    override suspend fun perform(action: OnboardingAction): Onboarding {
        if (action == OnboardingAction.Reset) memories.clearLocalData()
        return delegate.perform(action)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class OnboardingModule {
    @Binds abstract fun repository(impl: LocalOnboardingRepository): OnboardingRepository
}
