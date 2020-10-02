package ru.swiftail.kvk.reflection

import mu.KotlinLogging
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class ReflectionScanners (
    private val commandScanner: CommandScanner
) {

    fun performAllReflectionScans() {

        logger.info { "Performing reflection scans" }
        commandScanner.scanAndRegister()
        logger.debug { "Done all reflection scans" }

    }

}
