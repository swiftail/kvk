package ru.swiftail.kvk.vk.api.bot

import ru.swiftail.kvk.vk.api.VkContext
import javax.inject.Singleton

@Singleton
class BotVk(val vkContext: VkContext) {

    val messages = BotVkMessages(this)
    val upload = BotVkUpload(vkContext)

}
