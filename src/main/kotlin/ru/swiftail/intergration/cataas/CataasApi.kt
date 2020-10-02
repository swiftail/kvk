package ru.swiftail.intergration.cataas

import java.awt.Color
import java.net.URI
import java.util.*
import javax.inject.Singleton

@Singleton
class CataasApi {

    private val root = "https://cataas.com/cat"

    fun getCute(): URI {
        return URI("$root/cute")
    }

    fun getSays(
        text: Optional<String>,
        color: Color,
        size: Int,
        filter: Optional<String>
    ): URI {

        var root = "/c"
        var query = ""

        if (text.isPresent) {
            root += "/s/${text.get()}"
            val colorString = "#" + Integer.toHexString(color.rgb).substring(2)
            query += "color=$colorString&size=$size"
        }

        filter.ifPresent {
                f -> query += "&filter=$f"
        }

        return URI(
            "https",
            "cataas.com",
            root,
            query,
            null
        )
    }

}
