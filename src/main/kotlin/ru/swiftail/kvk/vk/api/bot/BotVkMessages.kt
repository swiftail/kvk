package ru.swiftail.kvk.vk.api.bot

import kotlinx.coroutines.Deferred
import ru.swiftail.kvk.vk.api.message.MessageQueryBuilder
import ru.swiftail.kvk.vk.lowlevel.runAsync

class BotVkMessages(private val botVk: BotVk) {

    suspend fun send(block: suspend MessageQueryBuilder.() -> Unit): Deferred<Int> {
        val builder = MessageQueryBuilder(botVk)
        block(builder)
        return builder.build().runAsync()
    }

}
