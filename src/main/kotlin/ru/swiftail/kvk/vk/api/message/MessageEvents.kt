package ru.swiftail.kvk.vk.api.message

import com.google.gson.JsonObject
import com.vk.api.sdk.objects.messages.Message
import ru.swiftail.kvk.vk.api.bot.BotVk
import ru.swiftail.kvk.vk.lowlevel.JsonListener
import ru.swiftail.kvk.vk.lowlevel.LongPollingListener
import javax.inject.Singleton

typealias MessageListener = (MessageContext) -> Unit

@Singleton
class MessageEvents(
    private val longPollingListener: LongPollingListener,
    private val botVk: BotVk
) {

    private fun getMessage(json: JsonObject): Message {
        val messageJson = json["object"].asJsonObject
        return botVk.vkContext.llVk.gson.fromJson(messageJson, Message::class.java)
    }

    private fun createJsonListener(messageListener: MessageListener): JsonListener = { json ->
        messageListener(MessageContext(botVk, getMessage(json)))
    }

    fun addEventListener(listener: MessageListener) {
        longPollingListener.addHandler("message_new", createJsonListener(listener))
    }

}
