@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package ru.swiftail.kvk.config.env

import io.github.cdimascio.dotenv.dotenv
import io.micronaut.context.annotation.Factory
import javax.inject.Named
import javax.inject.Singleton

@Factory
class EnvFactory {

    private val env = dotenv()

    @Named("vk_token")
    @Singleton
    fun getToken(): String {
        return env["VK_TOKEN"] ?: error("Failed to load token")
    }

    @Named("vk_group_id")
    @Singleton
    fun getGroupId(): Integer {
        return env["VK_GROUP_ID"]?.toIntOrNull() as Integer? ?: error("Failed to load group id")
    }

}
