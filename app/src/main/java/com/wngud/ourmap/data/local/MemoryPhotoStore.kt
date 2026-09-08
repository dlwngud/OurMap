package com.wngud.ourmap.data.local

import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Only app-created copies live here. Original picker media is never changed. */
class MemoryPhotoStore @Inject constructor(@param:ApplicationContext private val context: Context) {
    private val directory get() = File(context.noBackupFilesDir, "memory-photos")

    suspend fun copy(source: String): String {
        check(directory.isDirectory || directory.mkdirs()) { "사진 저장 폴더를 만들 수 없어요." }
        val target = File(directory, UUID.randomUUID().toString() + ".image")
        try {
            val input = context.contentResolver.openInputStream(source.toUri()) ?: throw IOException("사진을 읽을 수 없어요.")
            input.use { from -> target.outputStream().use { to ->
                val buffer = ByteArray(64 * 1024)
                var total = 0L
                while (true) {
                    currentCoroutineContext().ensureActive()
                    val size = from.read(buffer)
                    if (size < 0) break
                    total += size
                    if (total > 20L * 1024 * 1024) throw IOException("사진 한 장은 20MB 이하여야 해요.")
                    to.write(buffer, 0, size)
                }
                if (total == 0L) throw IOException("빈 사진 파일이에요.")
                to.fd.sync()
            } }
            return target.toURI().toString()
        } catch (error: Exception) { target.delete(); throw error }
    }

    fun remove(uri: String) {
        val parsed = uri.toUri()
        if (parsed.scheme != "file") return
        val file = File(parsed.path ?: return)
        if (file.parentFile?.canonicalFile == directory.canonicalFile && file.exists() && !file.delete()) {
            throw IOException("사진 파일을 지우지 못했어요.")
        }
    }

    fun cleanExcept(referenced: Set<String>) {
        directory.listFiles()?.forEach { if (it.toURI().toString() !in referenced) remove(it.toURI().toString()) }
    }
}
