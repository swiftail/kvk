/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ru.swiftail.kvk.command.api.command.spec

import com.google.common.base.MoreObjects
import com.google.common.base.Objects
import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import ru.swiftail.kvk.command.api.command.CommandCallable
import ru.swiftail.kvk.command.api.command.CommandException
import ru.swiftail.kvk.command.api.command.CommandPermissionException
import ru.swiftail.kvk.command.api.command.CommandSource
import ru.swiftail.kvk.command.api.command.args.*
import ru.swiftail.kvk.command.api.command.args.parsing.InputTokenizer
import java.util.*

/**
 * Specification for how command arguments should be parsed.
 */
class CommandSpec internal constructor(
    private val args: CommandElement,
    val executor: ICommandExecutor,
    description: String?,
    extendedDescription: String?,
    private val permission: String?,
    parser: InputTokenizer
) : CommandCallable {

    private val description: Optional<String>
    private val extendedDescription: Optional<String>

    private val inputTokenizer: InputTokenizer

    @Throws(CommandException::class)
    fun checkPermission(source: CommandSource) {
        if (!testPermission(source)) {
            throw CommandPermissionException()
        }
    }

    @Throws(ArgumentParseException::class)
    fun populateContext(source: CommandSource, args: CommandArgs, context: CommandContext) {
        this.args.parse(source, args, context)
        if (args.hasNext()) {
            args.next()
            throw args.createError(String.format("Too many arguments!"))
        }
    }

    @Throws(CommandException::class)
    override suspend fun process(source: CommandSource, arguments: String) {
        checkPermission(source)
        val args = CommandArgs(arguments, inputTokenizer.tokenize(arguments, false))
        val context = CommandContext()
        populateContext(source, args, context)
        executor(ExecutorContext.INSTANCE, source, context)
    }

    override fun testPermission(source: CommandSource): Boolean {
        return permission == null || source.hasPermission(permission)
    }

    override fun getShortDescription(source: CommandSource): Optional<String> {
        return description
    }

    fun getExtendedDescription(source: CommandSource?): Optional<String> {
        return extendedDescription
    }

    override fun getUsage(source: CommandSource): String {
        return args.getUsage(source)
    }

    override fun getHelp(source: CommandSource): Optional<String> {
        val builder = StringBuilder()
        getShortDescription(source).ifPresent { a: String? ->
            builder.append(a).append("\n")
        }
        builder.append(getUsage(source))
        getExtendedDescription(source).ifPresent { a: String? ->
            builder.append("\n").append(a)
        }
        return Optional.of(builder.toString())
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val that =
            o as CommandSpec
        return (Objects.equal(args, that.args)
                && Objects.equal(executor, that.executor)
                && Objects.equal(description, that.description)
                && Objects.equal(extendedDescription, that.extendedDescription)
                && Objects.equal(permission, that.permission)
                && Objects.equal(inputTokenizer, that.inputTokenizer))
    }

    override fun hashCode(): Int {
        return Objects.hashCode(
            args,
            executor,
            description,
            extendedDescription,
            permission,
            inputTokenizer
        )
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("args", args)
            .add("executor", executor)
            .add("description", description)
            .add("extendedDescription", extendedDescription)
            .add("permission", permission)
            .add("argumentParser", inputTokenizer)
            .toString()
    }

    /**
     * Builder for command specs.
     */
    class Builder internal constructor() {
        private var args =
            DEFAULT_ARG
        private var description: String? = null
        private var extendedDescription: String? = null
        private var permission: String? = null
        private var executor: ICommandExecutor? = null
        private var childCommandMap: MutableMap<List<String>, CommandCallable>? =
            null
        private var childCommandFallback = true
        private var argumentParser = InputTokenizer.quotedStrings(false)

        /**
         * Sets the permission that will be checked before using this command.
         *
         * @param permission The permission to check
         * @return this
         */
        fun permission(permission: String?): Builder {
            this.permission = permission
            return this
        }

        fun executor(closure: ExecutorClosure): Builder {
            this.executor = CommandExecutor(closure)
            return this
        }

        /**
         * Sets the callback that will handle this command's execution.
         *
         * @param executor The executor that will be called with this command's
         * parsed arguments
         * @return this
         */
        fun executor(executor: ICommandExecutor): Builder {
            this.executor = executor
            return this
        }

        /**
         * Adds more child arguments for this command.
         * If an executor or arguments are set, they are used as fallbacks.
         *
         * @param children The children to use
         * @return this
         */
        fun children(children: Map<List<String>, CommandCallable>): Builder {
            if (childCommandMap == null) {
                childCommandMap = HashMap()
            }
            childCommandMap!!.putAll(children!!)
            return this
        }

        /**
         * Add a single child command to this command.
         *
         * @param child   The child to add
         * @param aliases Aliases to make the child available under. First
         * one is primary and is the only one guaranteed to be listed in
         * usage outputs.
         * @return this
         */
        fun child(
            child: CommandCallable,
            vararg aliases: String
        ): Builder {
            if (childCommandMap == null) {
                childCommandMap = HashMap()
            }
            childCommandMap!![ImmutableList.copyOf(aliases)] = child
            return this
        }

        /**
         * Add a single child command to this command.
         *
         * @param child   The child to add.
         * @param aliases Aliases to make the child available under. First
         * one is primary and is the only one guaranteed to be listed in
         * usage outputs.
         * @return this
         */
        fun child(
            child: CommandCallable,
            aliases: Collection<String>?
        ): Builder {
            if (childCommandMap == null) {
                childCommandMap = HashMap()
            }
            childCommandMap!![ImmutableList.copyOf(aliases)] = child
            return this
        }

        /**
         * A short, one-line description of this command's purpose.
         *
         * @param description The description to set
         * @return this
         */
        fun description(description: String?): Builder {
            this.description = description
            return this
        }

        /**
         * Sets an extended description to use in longer help listings for this
         * command. Will be appended to the short description and the command's
         * usage.
         *
         * @param extendedDescription The description to set
         * @return this
         */
        fun extendedDescription(extendedDescription: String?): Builder {
            this.extendedDescription = extendedDescription
            return this
        }

        /**
         * If a child command is selected but fails to parse arguments passed to
         * it, the following determines the behavior.
         *
         *
         *  * If this is set to **false**, this command (the
         * parent) will not attempt to parse the command, and will send back
         * the error from the child.
         *  * If this is set to **true**, the error from the
         * child will simply be discarded, and the parent command will
         * execute.
         *
         *
         *
         * The default for this is **true**, which emulates the
         * behavior from previous API revisions.
         *
         * @param childCommandFallback Whether to fallback on argument parse
         * failure
         * @return this
         */
        fun childArgumentParseExceptionFallback(childCommandFallback: Boolean): Builder {
            this.childCommandFallback = childCommandFallback
            return this
        }

        /**
         * Sets the argument specification for this command. Generally, for a
         * multi-argument command the [GenericArguments.seq]
         * method is used to parse a sequence of args.
         *
         * @param args The arguments object to use
         * @return this
         * @see GenericArguments
         */
        fun arguments(args: CommandElement?): Builder {
            Preconditions.checkNotNull(args, "args")
            this.args = GenericArguments.seq(args)
            return this
        }

        /**
         * Sets the argument specification for this command. This method accepts
         * a sequence of arguments. This is equivalent to calling `arguments(seq(args))`.
         *
         * @param args The arguments object to use
         * @return this
         * @see GenericArguments
         */
        fun arguments(vararg args: CommandElement?): Builder {
            Preconditions.checkNotNull(args, "args")
            this.args = GenericArguments.seq(*args)
            return this
        }

        /**
         * Sets the input tokenizer to be used to convert input from a string
         * into a list of argument tokens.
         *
         * @param parser The parser to use
         * @return this
         * @see InputTokenizer for common input parser implementations
         */
        fun inputTokenizer(parser: InputTokenizer): Builder {
            Preconditions.checkNotNull(parser, "parser")
            argumentParser = parser
            return this
        }

        /**
         * Create a new [CommandSpec] based on the data provided in this
         * builder.
         *
         * @return the new spec
         */
        fun build(): CommandSpec {
            if (childCommandMap == null || childCommandMap!!.isEmpty()) {
                Preconditions.checkNotNull(executor, "An executor is required")
            } else if (executor == null) {
                val childCommandElementExecutor =
                    registerInDispatcher(ChildCommandElementExecutor(null, null, false))
                if (args === DEFAULT_ARG) {
                    arguments(childCommandElementExecutor)
                } else {
                    arguments(args, childCommandElementExecutor)
                }
            } else {
                arguments(
                    registerInDispatcher(
                        ChildCommandElementExecutor(
                            executor,
                            args,
                            childCommandFallback
                        )
                    )
                )
            }
            return CommandSpec(
                args,
                executor!!,
                description,
                extendedDescription,
                permission,
                argumentParser
            )
        }

        private fun registerInDispatcher(childDispatcher: ChildCommandElementExecutor): ChildCommandElementExecutor {
            for ((key, value) in childCommandMap!!) {
                childDispatcher.register(value, key)
            }
            executor(childDispatcher)
            return childDispatcher
        }

        companion object {
            private val DEFAULT_ARG = GenericArguments.none()
        }
    }

    companion object {
        /**
         * Return a new builder for a CommandSpec.
         *
         * @return a new builder
         */
        fun builder(): Builder {
            return Builder()
        }
    }

    init {
        this.description = Optional.ofNullable(description)
        this.extendedDescription = Optional.ofNullable(extendedDescription)
        inputTokenizer = parser
    }
}
