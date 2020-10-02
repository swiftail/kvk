package ru.swiftail.kvk.vk.lowlevel

import com.google.common.collect.Multimaps
import com.google.gson.JsonObject
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.exceptions.ApiException
import com.vk.api.sdk.exceptions.ClientException
import com.vk.api.sdk.exceptions.LongPollServerKeyExpiredException
import org.slf4j.LoggerFactory
import java.net.SocketTimeoutException
import java.util.function.Consumer
import javax.inject.Named
import javax.inject.Singleton

typealias JsonListener = (JsonObject) -> Unit

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
@Singleton
class LongPollingListener(
    private val vk: VkApiClient,
    private val actor: GroupActor,
    @Named("vk_group_id") private val groupId: Integer
) {
    private val handlers = Multimaps.newSetMultimap<String, JsonListener>(hashMapOf()) { hashSetOf() }
    private lateinit var server: String
    private lateinit var key: String
    private var ts = 0

    @Throws(ClientException::class, ApiException::class)
    fun connect() {
        val longPollServer = vk.groups()
            .getLongPollServer(actor, groupId as Int)
            .execute()
        server = longPollServer.server
        key = longPollServer.key
        ts = longPollServer.ts.toInt()
    }

    @Throws(ClientException::class, ApiException::class)
    fun startListening() {
        Thread({
            while (true) {
                try {
                    logger.debug("Making longpoll")
                    val response = vk.longPoll()
                        .getEvents(server, key, ts)
                        .waitTime(30)
                        .execute()
                    ts = response.ts
                    val updates = response.updates
                    updates.forEach(Consumer { jsonObject: JsonObject ->
                        val type = jsonObject["type"].asString
                        val handlers = handlers[type]
                        handlers.forEach { it.invoke(jsonObject) }
                    })
                } catch (e: LongPollServerKeyExpiredException) {
                    logger.info("Key expired. Regeneration...")
                    connect()
                } catch (e: ClientException) {
                    logger.error("Long poll error", e)
                    connect()
                } catch (e: SocketTimeoutException) {
                    logger.warn("Socket timeout exception", e)
                    continue
                }
            }
        }, "LongPoll").start()
    }

    fun addHandler(
        event: String,
        handler: JsonListener
    ): LongPollingListener {
        handlers.put(event, handler)
        return this
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LongPollingListener::class.java)
    }
}
