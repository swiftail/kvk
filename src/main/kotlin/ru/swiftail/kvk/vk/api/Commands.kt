package ru.swiftail.kvk.vk.api

import mu.KotlinLogging
import ru.swiftail.kvk.command.api.command.CommandException
import ru.swiftail.kvk.command.api.command.CommandSource
import ru.swiftail.kvk.command.api.command.args.ArgumentParseException
import ru.swiftail.kvk.command.api.command.dispatcher.SimpleDispatcher
import javax.inject.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class Commands(private val dispatcher: SimpleDispatcher) {

    // Ugly as fuck
    suspend fun execute(alias: String, args: String, source: CommandSource) {

        val maybeMapping = dispatcher.get(alias, source).orElse(null)

        maybeMapping?.apply {
            val mapping = this
            try {
                val commandLine = "$alias $args"
                dispatcher.process(source, commandLine)
            } catch (e: CommandException) {

                source.reply("Ошибка: ${e.message}").await()

                if (e.shouldIncludeUsage()) {
                    when (e) {
                        is ArgumentParseException.WithUsage -> {
                            source.reply("Использование: ${e.usage}").await()
                        }
                        else -> {
                            source.reply("Использование: ${mapping.primaryAlias} ${mapping.callable.getUsage(source)}").await()
                        }
                    }
                }

            } catch (e: Throwable) {
                logger.error(e) { "Server error while processing command" }
                source.reply("Ошибка сервера").await()
                source.reply(e.toString()).await()
            }
        } ?: run {
            source.reply("Команда не найдена")
        }
    }

}
