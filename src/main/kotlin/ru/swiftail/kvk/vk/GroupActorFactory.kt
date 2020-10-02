package ru.swiftail.kvk.vk

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import io.micronaut.context.annotation.Factory
import javax.inject.Named
import javax.inject.Singleton

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
@Factory
class GroupActorFactory(
    @Named("vk_token") private val token: String,
    @Named("vk_group_id") private val groupId: Integer,
    private val vk: VkApiClient
)  {

    @Singleton
    fun createGroupActor(): GroupActor {
        return GroupActor(groupId as Int, token)
    }

}
