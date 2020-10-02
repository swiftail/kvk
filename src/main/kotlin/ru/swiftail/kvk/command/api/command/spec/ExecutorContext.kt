package ru.swiftail.kvk.command.api.command.spec

import io.micronaut.inject.qualifiers.Qualifiers
import ru.swiftail.kvk.bootstrap.getApplicationContext
import ru.swiftail.kvk.vk.api.bot.BotVk

class ExecutorContext private constructor() {

    companion object {
        val INSTANCE = ExecutorContext()
    }

    inline fun <reified T> wire(): T {
        return getApplicationContext().getBean(T::class.java)
    }

    inline fun <reified T> wire(name: String): T {
        return getApplicationContext().getBean(T::class.java, Qualifiers.byName(name))
    }

    val botVk: BotVk = wire()

}
