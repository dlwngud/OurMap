package com.wngud.ourmap.data.local

import androidx.room.*
import com.wngud.ourmap.core.model.Memory
import com.wngud.ourmap.core.model.Place
import com.wngud.ourmap.domain.memory.MemoryOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

@Entity(tableName = "memories", primaryKeys = ["userId", "spaceId", "id"],
    indices = [Index(value = ["userId", "spaceId", "visitedOn"])])
data class MemoryEntity(
    val userId: String, val spaceId: String, val id: String,
    @Embedded(prefix = "place_") val place: Place,
    val visitedOn: String, val note: String, val companion: String, val mood: String,
    val favorite: Boolean, val tagsJson: String, val stylesJson: String, val photosJson: String,
)

internal fun Memory.toEntity(owner: MemoryOwner) = MemoryEntity(owner.userId, owner.spaceId, id,
    place, visitedOn, note, companion, mood, favorite,
    Json.encodeToString(tags), Json.encodeToString(photoStyles), Json.encodeToString(photoUris))
internal fun MemoryEntity.toMemory() = Memory(id, place, visitedOn, note, companion,
    Json.decodeFromString(tagsJson), mood, Json.decodeFromString(stylesJson), favorite,
    Json.decodeFromString(photosJson))

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories WHERE userId = :user AND spaceId = :space ORDER BY visitedOn DESC, id DESC")
    fun observe(user: String, space: String): Flow<List<MemoryEntity>>
    @Query("SELECT * FROM memories WHERE userId = :user AND spaceId = :space AND id = :id")
    suspend fun find(user: String, space: String, id: String): MemoryEntity?
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(record: MemoryEntity)
    @Query("UPDATE memories SET favorite = NOT favorite WHERE userId = :user AND spaceId = :space AND id = :id")
    suspend fun toggleFavorite(user: String, space: String, id: String): Int
    @Query("DELETE FROM memories") suspend fun clear()
    @Query("SELECT * FROM memories") suspend fun all(): List<MemoryEntity>
}

@Database(entities = [MemoryEntity::class], version = 1, exportSchema = true)
abstract class MemoryDatabase : RoomDatabase() { abstract fun memories(): MemoryDao }
