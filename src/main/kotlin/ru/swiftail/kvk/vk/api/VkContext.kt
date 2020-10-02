package ru.swiftail.kvk.vk.api

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import javax.inject.Singleton

@Singleton
class VkContext (
    val actor: GroupActor,
    val llVk: VkApiClient
)
