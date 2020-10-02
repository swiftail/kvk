package ru.swiftail.kvk.bootstrap

import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers

private lateinit var applicationContext: ApplicationContext

fun getApplicationContext(): ApplicationContext {
    return applicationContext
}

object KVKContextLauncher {

    private var launched = false
    private var lock = Any()

    private inline fun <reified T> defineNamedSingleton(name: String, value: T) {
        applicationContext.registerSingleton(T::class.java, value, Qualifiers.byName(name))
    }

    private fun synchronizeLaunch() {
        synchronized(lock) {
            if(launched) error("KVK entrypoint is already created")
            launched = true
        }
    }

    fun <T> createEntryPoint(
        entryPointClass: Class<T>,
        applicationPackage: String
    ): T {

        synchronizeLaunch()

        applicationContext = ApplicationContext.run()
        defineNamedSingleton("KVK_APPLICATION_PACKAGE", applicationPackage)

        return applicationContext.use { ctx ->
            ctx.getBean(entryPointClass)
        }
    }
}
