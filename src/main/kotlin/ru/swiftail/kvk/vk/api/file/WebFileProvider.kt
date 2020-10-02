package ru.swiftail.kvk.vk.api.file

import java.io.BufferedInputStream
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class WebFileProvider(
    private val uri: URI,
    private val extensionResolverStrategy: ExtensionResolverStrategy
) : FileProvider() {

    private lateinit var location: Path

    override fun getFile(): File {
        val inputStream = BufferedInputStream(uri.toURL().openStream())
        location = Files.createTempFile("vkbot_upload_", extensionResolverStrategy.resolve(uri))
        // println(location)
        Files.copy(inputStream, location, StandardCopyOption.REPLACE_EXISTING)
        return location.toFile()
    }

    override fun close() {
        Files.delete(location)
    }

}

