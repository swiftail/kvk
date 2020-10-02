package ru.swiftail.kvk.vk.api.message

import com.vk.api.sdk.objects.messages.Message
import ru.swiftail.kvk.vk.api.bot.BotVk

class MessageContext(
    private val botVk: BotVk,
    val messageObject: Message
) {

    val text = messageObject.text

    suspend fun reply(text: String) = reply {
        setText(text)
    }

    suspend fun reply(block: suspend MessageQueryBuilder.() -> Unit) =
        botVk.messages.send {
            setPeerId(messageObject.peerId)
            block()
        }
}
