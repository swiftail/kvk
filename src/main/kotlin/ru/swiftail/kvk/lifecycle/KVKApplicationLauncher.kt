package ru.swiftail.kvk.lifecycle

import mu.KotlinLogging
import ru.swiftail.kvk.reflection.ReflectionScanners
import ru.swiftail.kvk.vk.lowlevel.LongPollingListener
import javax.inject.Singleton

;

private val logger = KotlinLogging.logger {}

@Singleton
class KVKApplicationLauncher(
    private val reflectionScanners: ReflectionScanners,
    private val commandListener: CommandListener,
    private val longPollingListener: LongPollingListener
) {

    private fun launchCommandListener() {
        commandListener.run()
    }

    private fun startLongPoll() {
        longPollingListener.connect()
        longPollingListener.startListening()
    }

    private fun runReflectionScanners() {
        reflectionScanners.performAllReflectionScans()
    }

    fun runApp() {
        runReflectionScanners()
        startLongPoll()
        launchCommandListener()

        logger.info { "KVK Application is launched" }
    }

}
