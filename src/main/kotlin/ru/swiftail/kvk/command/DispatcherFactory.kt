package ru.swiftail.kvk.command

import io.micronaut.context.annotation.Factory
import ru.swiftail.kvk.command.api.command.dispatcher.SimpleDispatcher
import javax.inject.Singleton

@Factory
class DispatcherFactory {

    @Singleton
    fun getDispatcher(): SimpleDispatcher {
        return SimpleDispatcher()
    }

}
