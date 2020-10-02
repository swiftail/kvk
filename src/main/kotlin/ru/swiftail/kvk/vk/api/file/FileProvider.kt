package ru.swiftail.kvk.vk.api.file

import java.io.File

abstract class FileProvider {

    protected abstract fun getFile(): File
    protected abstract fun close(): Unit

    private var closed = false
    fun assertOk() = assert(!closed) { "Attempt to use closed file provider" }

    inline fun <T> use(block: (File) -> T): T {

        assertOk()

        val file = `access$getFile`()
        val result = block(file)

        `access$close`()

        return result
    }

    fun get() {

    }

    @PublishedApi
    internal fun `access$getFile`() = getFile()

    @PublishedApi
    internal fun `access$close`() = close()

}
