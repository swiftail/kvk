package ru.swiftail.kvk.command.api.command.dispatcher

import com.google.common.base.Preconditions
import com.google.common.collect.*
import ru.swiftail.kvk.command.api.command.*
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

/**
 * A simple implementation of a [Dispatcher].
 */
class SimpleDispatcher
/**
 * Creates a basic new dispatcher.
 */ @JvmOverloads constructor(private val disambiguatorFunc: Disambiguator = FIRST_DISAMBIGUATOR) :
    Dispatcher {
    private val commands: ListMultimap<String, CommandMapping> =
        ArrayListMultimap.create()

    fun register(callable: CommandCallable, vararg alias: String): Optional<CommandMapping> {
        return register(callable, alias.toList())
    }

    fun register(
        callable: CommandCallable,
        aliases: List<String>
    ): Optional<CommandMapping> {
        return register(
            callable,
            aliases,
            Function.identity()
        )
    }

    /**
     * Register a given command using a given list of aliases.
     *
     *
     * The provided callback function will be called with a list of aliases
     * that are not taken (from the list of aliases that were requested) and
     * it should return a list of aliases to actually register. Aliases may be
     * removed, and if no aliases remain, then the command will not be
     * registered. It may be possible that no aliases are available, and thus
     * the callback would receive an empty list. New aliases should not be added
     * to the list in the callback as this may cause
     * [IllegalArgumentException] to be thrown.
     *
     *
     * The first non-conflicted alias becomes the "primary alias."
     *
     * @param callable The command
     * @param aliases  A list of aliases
     * @param callback The callback
     * @return The registered command mapping, unless no aliases could
     * be registered
     */
    @Synchronized
    fun register(
        callable: CommandCallable,
        aliases: List<String>,
        callback: Function<List<String>, List<String>>
    ): Optional<CommandMapping> {
        var aliases = aliases

        // Invoke the callback with the commands that /can/ be registered
        // noinspection ConstantConditions
        aliases = ImmutableList.copyOf(callback.apply(aliases))
        if (aliases.isEmpty()) {
            return Optional.empty()
        }
        val primary = aliases[0]
        val secondary = aliases.subList(1, aliases.size)
        val mapping: CommandMapping = ImmutableCommandMapping(callable, primary, secondary.toMutableSet())
        for (alias in aliases) {
            commands.put(alias.toLowerCase(), mapping)
        }
        return Optional.of(mapping)
    }

    /**
     * Remove a mapping identified by the given alias.
     *
     * @param alias The alias
     * @return The previous mapping associated with the alias, if one was found
     */
    @Synchronized
    fun remove(alias: String): Collection<CommandMapping> {
        return commands.removeAll(alias.toLowerCase())
    }

    /**
     * Remove all mappings identified by the given aliases.
     *
     * @param aliases A collection of aliases
     * @return Whether any were found
     */
    @Synchronized
    fun removeAll(aliases: Collection<*>): Boolean {
        Preconditions.checkNotNull(aliases, "aliases")
        var found = false
        for (alias in aliases) {
            if (!commands.removeAll(alias.toString().toLowerCase()).isEmpty()) {
                found = true
            }
        }
        return found
    }

    /**
     * Remove a command identified by the given mapping.
     *
     * @param mapping The mapping
     * @return The previous mapping associated with the alias, if one was found
     */
    @Synchronized
    fun removeMapping(mapping: CommandMapping): Optional<CommandMapping> {
        Preconditions.checkNotNull(mapping, "mapping")
        var found: CommandMapping? = null
        val it = commands.values().iterator()
        while (it.hasNext()) {
            val current = it.next()
            if (current == mapping) {
                it.remove()
                found = current
            }
        }
        return Optional.ofNullable(found)
    }

    /**
     * Remove all mappings contained with the given collection.
     *
     * @param mappings The collection
     * @return Whether the at least one command was removed
     */
    @Synchronized
    fun removeMappings(mappings: Collection<*>): Boolean {
        Preconditions.checkNotNull(mappings, "mappings")
        var found = false
        val it = commands.values().iterator()
        while (it.hasNext()) {
            if (mappings.contains(it.next())) {
                it.remove()
                found = true
            }
        }
        return found
    }

    @Synchronized
    override fun getCommands(): Set<CommandMapping> {
        return ImmutableSet.copyOf(commands.values())
    }

    @Synchronized
    override fun getPrimaryAliases(): Set<String> {
        val aliases: MutableSet<String> = HashSet()
        for (mapping in commands.values()) {
            aliases.add(mapping.primaryAlias)
        }
        return Collections.unmodifiableSet(aliases)
    }

    @Synchronized
    override fun getAliases(): Set<String> {
        val aliases: MutableSet<String> = HashSet()
        for (mapping in commands.values()) {
            aliases.addAll(mapping.getAllAliases())
        }
        return Collections.unmodifiableSet(aliases)
    }

    override fun get(alias: String): Optional<CommandMapping> {
        return get(alias, null)
    }

    @Synchronized
    override fun get(alias: String, source: CommandSource?): Optional<CommandMapping> {
        val results = commands[alias.toLowerCase()]
        var result: Optional<CommandMapping> = Optional.empty()
        if (results.size == 1) {
            result = Optional.of(results[0])
        } else if (results.size > 1) {
            result = disambiguatorFunc.disambiguate(source, alias, results)
        }
        if (source != null) {
            result = result.filter { m: CommandMapping ->
                m.callable.testPermission(source)
            }
        }
        return result
    }

    @Synchronized
    override fun containsAlias(alias: String): Boolean {
        return commands.containsKey(alias.toLowerCase())
    }

    override fun containsMapping(mapping: CommandMapping): Boolean {
        Preconditions.checkNotNull(mapping, "mapping")
        for (test in commands.values()) {
            if (mapping == test) {
                return true
            }
        }
        return false
    }

    @Throws(CommandException::class)
    override suspend fun process(source: CommandSource, commandLine: String) {
        val argSplit = commandLine.split(Regex(" +"), 2).toTypedArray()
        val cmdOptional = get(argSplit[0], source)
        if (!cmdOptional.isPresent) {
            throw CommandNotFoundException(argSplit[0])
        }
        val arguments = if (argSplit.size > 1) argSplit[1] else ""
        val mapping = cmdOptional.get()
        val spec = mapping.callable
        try {
            spec.process(source, arguments)
        } catch (e: CommandNotFoundException) {
            throw CommandException(
                String.format(
                    "No such child command: %s",
                    e.command
                )
            )
        }
    }

    override fun testPermission(source: CommandSource): Boolean {
        for (mapping in commands.values()) {
            if (mapping.callable.testPermission(source)) {
                return true
            }
        }
        return false
    }

    override fun getShortDescription(source: CommandSource): Optional<String> {
        return Optional.empty()
    }

    override fun getHelp(source: CommandSource): Optional<String> {
        if (commands.isEmpty) {
            return Optional.empty()
        }
        val build = StringBuilder("Available commands:\n")
        val it = filterCommands(source).iterator()
        while (it.hasNext()) {
            val mappingOpt = get(it.next(), source)
            if (!mappingOpt.isPresent) {
                continue
            }
            val mapping = mappingOpt.get()
            val description = mapping.callable.getShortDescription(source)
            build.append(mapping.primaryAlias)
            if (it.hasNext()) {
                build.append("\n")
            }
        }
        return Optional.of(build.toString())
    }

    private fun filterCommands(src: CommandSource): Set<String> {
        return Multimaps.filterValues(
            commands
        ) { input: CommandMapping? ->
            input!!.callable.testPermission(src)
        }.keys().elementSet()
    }

    // Filter out commands by String first
    private fun filterCommands(src: CommandSource, start: String): Set<String> {
        val map =
            Multimaps.filterKeys(
                commands
            ) { input: String? ->
                input != null && input.toLowerCase().startsWith(start.toLowerCase())
            }
        return Multimaps.filterValues(
            map
        ) { input: CommandMapping? ->
            input!!.callable.testPermission(src)
        }.keys().elementSet()
    }

    /**
     * Gets the number of registered aliases.
     *
     * @return The number of aliases
     */
    @Synchronized
    fun size(): Int {
        return commands.size()
    }

    override fun getUsage(source: CommandSource): String {
        val build = StringBuilder()
        val filteredCommands: Iterable<String> = filterCommands(source).stream()
            .filter { input: String? ->
                if (input == null) {
                    return@filter false
                }
                val ret = get(input, source)
                ret.isPresent && ret.get().primaryAlias.equals(input)
            }
            .collect(Collectors.toList())
        val it = filteredCommands.iterator()
        while (it.hasNext()) {
            build.append(it.next())
            if (it.hasNext()) {
                build.append(CommandMessageFormatting.PIPE_TEXT)
            }
        }
        return build.toString()
    }

    @Synchronized
    override fun getAll(alias: String): Set<CommandMapping> {
        return ImmutableSet.copyOf(commands[alias])
    }

    override fun getAll(): Multimap<String, CommandMapping> {
        return ImmutableMultimap.copyOf(commands)
    }

    companion object {
        /**
         * This is a disambiguator function that returns the first matching command.
         */
        val FIRST_DISAMBIGUATOR =
            Disambiguator { source: CommandSource?, aliasUsed: String, availableOptions: List<CommandMapping> ->
                for (mapping in availableOptions) {
                    if (mapping.primaryAlias.toLowerCase() == aliasUsed.toLowerCase()) {
                        return@Disambiguator Optional.of(mapping)
                    }
                }
                Optional.of(availableOptions[0])
            }
    }
    /**
     * Creates a new dispatcher with a specific disambiguator.
     *
     * @param disambiguatorFunc Function that returns the preferred command if
     * multiple exist for a given alias
     */
}
