package ru.swiftail.kvk.vk.api.message

import com.vk.api.sdk.objects.photos.Photo
import com.vk.api.sdk.queries.messages.MessagesSendQuery
import mu.KotlinLogging
import ru.swiftail.kvk.vk.api.bot.BotVk
import ru.swiftail.kvk.vk.api.file.ExtensionResolverStrategy
import ru.swiftail.kvk.vk.api.file.WebFileProvider
import java.net.URI
import kotlin.random.Random

private val logger = KotlinLogging.logger {}
class MessageQueryBuilder(private val botVk: BotVk) {

    val vkContext = botVk.vkContext

    private var text: String? = null
    fun setText(v: String) = this.apply { text = v }

    private var peerId: Int? = null
    fun setPeerId(v: Int) = this.apply { peerId = v }

    private val attachments = mutableListOf<String>()
    fun attachPhoto(photo: Photo) = this.apply {
        attachments += "photo${photo.ownerId}_${photo.id}"
    }

    suspend fun uploadPhoto(uri: URI, extensionResolverStrategy: ExtensionResolverStrategy)
            = attachPhoto(botVk.upload.uploadMessagesPhoto(WebFileProvider(uri, extensionResolverStrategy)).first())

    fun build(): MessagesSendQuery {
        val query = vkContext.llVk
            .messages()
            .send(vkContext.actor)

        text?.let(query::message)
        peerId?.let(query::peerId)

        if(attachments.isNotEmpty()) query.attachment(attachments.joinToString(","))

        query.randomId(Random.nextInt(Int.MAX_VALUE))

        return query
    }

}
