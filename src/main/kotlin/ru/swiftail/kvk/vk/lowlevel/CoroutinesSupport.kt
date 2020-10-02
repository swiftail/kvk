package ru.swiftail.kvk.vk.lowlevel

import com.vk.api.sdk.client.ApiRequest
import kotlinx.coroutines.*


private val coroutinesContext = newFixedThreadPoolContext(12, "api") + SupervisorJob()

private val scope = CoroutineScope(coroutinesContext)

fun <T> ApiRequest<T>.runAsync(): Deferred<T> {
    val request = this
    return scope.async {
        try {
            request.execute()
        } catch (e: Throwable) {
            throw RuntimeException(
                """
                API error.
                Method: $request
                Error: $e
            """.trimIndent(), e
            )
        }
    }
}
