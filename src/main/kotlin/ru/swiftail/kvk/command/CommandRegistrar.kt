package ru.swiftail.kvk.command

import ru.swiftail.kvk.command.api.command.CommandCallable
import ru.swiftail.kvk.command.api.command.dispatcher.SimpleDispatcher
import javax.inject.Singleton

@Singleton
class CommandRegistrar(private val dispatcher: SimpleDispatcher) {

    fun register(aliases: List<String>, command: CommandCallable) {
        dispatcher.register(command, aliases)
    }

    fun getRegisteredCommandsSize(): Int {
        return dispatcher.commands.size
    }

}
