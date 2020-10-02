package ru.swiftail.kvk.bootstrap

import mu.KotlinLogging
import ru.swiftail.kvk.lifecycle.KVKApplicationLauncher

private val logger = KotlinLogging.logger {}

object KVKBootstrap {

    fun run(applicationPackage: String) {

        logger.debug { "Bootstrapping KVK application from $applicationPackage" }

        KVKContextLauncher
            .createEntryPoint(KVKApplicationLauncher::class.java, applicationPackage)
            .runApp()

    }

}
