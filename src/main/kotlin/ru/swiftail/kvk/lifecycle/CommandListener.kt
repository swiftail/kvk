package ru.swiftail.kvk.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import mu.KotlinLogging
import ru.swiftail.kvk.command.api.command.CommandSource
import ru.swiftail.kvk.vk.api.Commands
import ru.swiftail.kvk.vk.api.message.MessageContext
import ru.swiftail.kvk.vk.api.message.MessageEvents
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class CommandListener(
    private val messageEvents: MessageEvents,
    private val commands: Commands
) {

    private val commandCoroutineScope =
        CoroutineScope(newFixedThreadPoolContext(4, "commands") + SupervisorJob())

    private suspend fun handleCommand(ctx: MessageContext) {
        val txt = ctx.text.removePrefix("-")
        if (txt.isBlank()) return

        val alias = txt.substringBefore(" ").trim()
        val args = txt.removePrefix(alias).trim()

        commands.execute(alias, args, CommandSource(ctx))
    }

    private fun handleMessage(ctx: MessageContext) {
        commandCoroutineScope.launch {
            if (ctx.text.startsWith("-")) {
                handleCommand(ctx)
            }
        }
    }

    fun run() {
        messageEvents.addEventListener(::handleMessage)
        logger.debug { "CommandListener is launched" }
    }

}
