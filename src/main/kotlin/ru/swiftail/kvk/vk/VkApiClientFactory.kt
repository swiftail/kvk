package ru.swiftail.kvk.vk

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.httpclient.HttpTransportClient
import io.micronaut.context.annotation.Factory
import javax.inject.Singleton

@Factory
class VkApiClientFactory {

    @Singleton
    fun createVkApiClient(): VkApiClient {
        val transportClient = HttpTransportClient()
        return VkApiClient(transportClient)
    }

}
