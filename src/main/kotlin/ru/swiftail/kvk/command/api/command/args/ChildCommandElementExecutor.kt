package ru.swiftail.kvk.command.api.command.args

import ru.swiftail.kvk.command.api.command.CommandCallable
import ru.swiftail.kvk.command.api.command.CommandException
import ru.swiftail.kvk.command.api.command.CommandMapping
import ru.swiftail.kvk.command.api.command.CommandMessageFormatting
import ru.swiftail.kvk.command.api.command.CommandSource
import ru.swiftail.kvk.command.api.command.args.ArgumentParseException.WithUsage
import ru.swiftail.kvk.command.api.command.spec.CommandSpec
import ru.swiftail.kvk.command.api.command.dispatcher.SimpleDispatcher
import ru.swiftail.kvk.command.api.command.spec.ExecutorContext
import ru.swiftail.kvk.command.api.command.spec.ICommandExecutor
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class ChildCommandElementExecutor(
    private val fallbackExecutor: ICommandExecutor?,
    fallbackElements: CommandElement?,
    fallbackOnFail: Boolean
) : CommandElement("child" + COUNTER.getAndIncrement()), ICommandExecutor {
    private val fallbackElements: CommandElement?
    private val dispatcher = SimpleDispatcher(SimpleDispatcher.FIRST_DISAMBIGUATOR)
    private val fallbackOnFail: Boolean


    fun register(
        callable: CommandCallable,
        aliases: List<String>
    ): Optional<CommandMapping> {
        return dispatcher.register(callable, aliases)
    }

    fun register(callable: CommandCallable, vararg aliases: String): Optional<CommandMapping> {
        return dispatcher.register(callable, *aliases)
    }

    private fun filterCommands(src: CommandSource): Set<String> {
        return com.google.common.collect.Multimaps.filterValues(
            dispatcher.all
        ) { input: CommandMapping? ->
            input != null && input.callable.testPermission(src)
        }
            .keys()
            .elementSet()
    }

    @Throws(ArgumentParseException::class)
    override fun parse(source: CommandSource, args: CommandArgs, context: CommandContext) {
        if (fallbackExecutor != null && !args.hasNext()) {
            fallbackElements?.parse(source, args, context)
            return  // execute the fallback regardless in this scenario.
        }
        val state = args.snapshot
        val key = args.next()
        val optionalCommandMapping = dispatcher[key, source]
        if (optionalCommandMapping.isPresent) {
            val mapping = optionalCommandMapping.get()
            try {
                if (mapping.callable is CommandSpec) {
                    val spec =
                        mapping.callable as CommandSpec
                    spec.populateContext(source, args, context)
                } else {
                    if (args.hasNext()) {
                        args.next()
                        context.putArg(this.key + "_args", args.raw.substring(args.rawPosition))
                    }
                    while (args.hasNext()) {
                        args.next()
                    }
                }

                // Success, add to context now so that we don't execute the wrong executor in the first place.
                context.putArg(this.key, mapping)
            } catch (ex: ArgumentParseException) {
                // If we get here, fallback to the elements, if they exist.
                args.applySnapshot(state)
                if (fallbackOnFail && fallbackElements != null) {
                    fallbackElements.parse(source, args, context)
                    return
                }

                // Get the usage
                args.next()
                if (ex is WithUsage) {
                    // This indicates a previous child failed, so we just prepend our child
                    throw WithUsage(ex, key + " " + ex.usage)
                }
                throw WithUsage(ex, key + " " + mapping.callable.getUsage(source))
            }
        } else {
            // Not a child, so let's continue with the fallback.
            if (fallbackExecutor != null && fallbackElements != null) {
                args.applySnapshot(state)
                fallbackElements.parse(source, args, context)
            } else {
                // If we have no elements to parse, then we throw this error - this is the only element
                // so specifying it implicitly means we have a child command to execute.
                throw args.createError(String.format("Input command %s was not a valid subcommand!", key))
            }
        }
    }

    @Throws(ArgumentParseException::class)
    override fun parseValue(source: CommandSource, args: CommandArgs): Any? {
        return null
    }

    @Throws(CommandException::class)
    override suspend fun execute(execCtx: ExecutorContext, src: CommandSource, args: CommandContext) {
        val mapping = args.getOne<CommandMapping?>(this.key).orElse(null)
        if (mapping == null) {
            if (fallbackExecutor == null) {
                throw CommandException(
                    String.format(
                        "Invalid subcommand state -- no more than one mapping may be provided for child arg %s",
                        key
                    )
                )
            }
            fallbackExecutor.execute(execCtx, src, args)
            return
        }
        if (mapping.callable is CommandSpec) {
            val spec =
                mapping.callable as CommandSpec
            spec.checkPermission(src)
            spec.executor.execute(execCtx, src, args)
            return
        }
        val arguments = args.getOne<String>(this.key + "_args").orElse("")
        mapping.callable.process(src, arguments)
    }

    override fun getUsage(src: CommandSource): String {
        val usage = dispatcher.getUsage(src)
        if (fallbackElements == null) {
            return usage
        }
        val elementUsage = fallbackElements.getUsage(src)
        return if (elementUsage.isEmpty()) {
            usage
        } else usage + CommandMessageFormatting.PIPE_TEXT + elementUsage
    }

    companion object {
        private val COUNTER = AtomicInteger()
        private val NONE = GenericArguments.none()
    }

    init {
        this.fallbackElements =
            if (NONE === fallbackElements) null else fallbackElements
        this.fallbackOnFail = fallbackOnFail
    }
}
