package com.wngud.ourmap

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.wngud.ourmap.data.demo.DemoContent
import com.wngud.ourmap.data.local.*
import com.wngud.ourmap.domain.memory.MemoryOwner
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import java.io.File
import java.net.URI
import java.util.UUID

class MemoryRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "test-memory-${UUID.randomUUID()}.db"
    private val files = File(context.cacheDir, "test-memory-${UUID.randomUUID()}")
    private val isolated = object : ContextWrapper(context) { override fun getNoBackupFilesDir() = files }
    private lateinit var database: MemoryDatabase
    private lateinit var repository: RoomMemoryRepository
    private val owner = MemoryOwner("user-a", "space-a")
    private val memory = DemoContent().memories.first().copy(photoStyles = emptyList())

    @Before fun setup() { files.mkdirs(); reopen() }
    private fun reopen() {
        database = Room.databaseBuilder(context, MemoryDatabase::class.java, name).build()
        repository = RoomMemoryRepository(database, MemoryPhotoStore(isolated))
    }
    @After fun cleanup() { database.close(); context.deleteDatabase(name); files.deleteRecursively() }

    @Test fun recordsAndFavoriteSurviveDatabaseReopenAndStayScoped() = runTest {
        repository.save(owner, memory)
        repository.save(owner, memory)
        repository.toggleFavorite(owner, memory.id)
        repository.save(MemoryOwner("user-b", "space-a"), memory.copy(note = "다른 사용자"))
        repository.save(MemoryOwner("user-a", "space-b"), memory.copy(note = "다른 공간"))
        database.close(); reopen()
        val restored = repository.observe(owner).first()
        assertEquals(1, restored.size)
        assertTrue(restored.single().favorite)
        assertEquals(memory.note, restored.single().note)
        assertTrue(repository.observe(MemoryOwner("unknown", "space-a")).first().isEmpty())
    }

    private fun source(): File {
        val source = File(files, "original.png")
        val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return source
    }

    @Test fun copiedPhotoSurvivesOriginalRemovalAndDatabaseReopen() = runTest {
        val original = source()
        val saved = repository.save(owner, memory.copy(photoUris = listOf(original.toURI().toString())))
        val copy = File(URI(saved.photoUris.single()))
        assertNotEquals(original, copy)
        assertArrayEquals(original.readBytes(), copy.readBytes())
        original.delete()
        database.close(); reopen()
        val restored = repository.observe(owner).first().single()
        val decoded = BitmapFactory.decodeFile(File(URI(restored.photoUris.single())).path)
        assertNotNull(decoded); decoded.recycle()
        // Retrying the same committed ID does not need the now-deleted source.
        assertEquals(saved, repository.save(owner, memory.copy(photoUris = listOf(original.toURI().toString()))))
    }

    @Test fun failedCopyLeavesNoRecordAndNoPartialCopies() = runTest {
        val original = source()
        try {
            repository.save(owner, memory.copy(photoUris = listOf(original.toURI().toString(), "content://missing/photo")))
            fail("Expected copy failure")
        } catch (_: java.io.FileNotFoundException) { }
        assertTrue(repository.observe(owner).first().isEmpty())
        assertTrue(File(files, "memory-photos").listFiles().orEmpty().isEmpty())
        assertTrue(original.exists())
    }

    @Test fun resetRemovesDatabaseRowsAndCopiesButNotOriginal() = runTest {
        val original = source()
        repository.save(owner, memory.copy(photoUris = listOf(original.toURI().toString())))
        repository.clearLocalData()
        assertTrue(repository.observe(owner).first().isEmpty())
        assertTrue(File(files, "memory-photos").listFiles().orEmpty().isEmpty())
        assertTrue(original.exists())
    }
}
