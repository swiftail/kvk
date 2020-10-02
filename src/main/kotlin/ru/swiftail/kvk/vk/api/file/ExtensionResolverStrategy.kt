package ru.swiftail.kvk.vk.api.file

import java.net.URI

sealed class ExtensionResolverStrategy {

    abstract fun resolve(uri: URI): String

    class STATIC(private val extension: String) : ExtensionResolverStrategy() {
        override fun resolve(uri: URI): String {
            return ".$extension"
        }
    }

    object FROM_URI : ExtensionResolverStrategy() {
        override fun resolve(uri: URI): String {
            return "." + uri.toString().substringAfterLast(".")
        }
    }

    object EMPTY : ExtensionResolverStrategy() {
        override fun resolve(uri: URI): String {
            return ""
        }
    }

}
