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
package ru.swiftail.kvk.command.api.command.args

import com.google.common.base.Preconditions
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import ru.swiftail.kvk.command.api.command.CommandException
import ru.swiftail.kvk.command.api.command.CommandSource
import java.util.*

/**
 * Context that a command is executed in.
 * This object stores parsed arguments from other commands
 */
class CommandContext {
    private val parsedArgs: Multimap<String, Any>

    fun <T> getAll(key: String?): Collection<T> {
        return Collections.unmodifiableCollection(parsedArgs[key] as Collection<T>)
    }

    fun <T> getOne(key: String?): Optional<T> {
        val values = parsedArgs[key]
        return if (values.size != 1) {
            Optional.empty()
        } else Optional.ofNullable(values.iterator().next() as T)
    }

    @Throws(NoSuchElementException::class, IllegalArgumentException::class, ClassCastException::class)
    fun <T> requireOne(key: String?): T {
        val values = parsedArgs[key]
        if (values.size == 1) {
            return values.iterator().next() as T
        } else if (values.isEmpty()) {
            throw NoSuchElementException()
        }
        throw IllegalArgumentException()
    }

    fun putArg(key: String?, value: Any) {
        Preconditions.checkNotNull(value, "value")
        parsedArgs.put(key, value)
    }


    @Throws(CommandException::class)
    fun checkPermission(commander: CommandSource, permission: String?) {
        if (!commander.hasPermission(permission)) {
            throw CommandException(String.format("You do not have permission to use this command!"))
        }
    }

    fun hasAny(key: String?): Boolean {
        return parsedArgs.containsKey(key)
    }

    fun createSnapshot(): Snapshot {
        return Snapshot(parsedArgs)
    }

    fun applySnapshot(snapshot: Snapshot) {
        parsedArgs.clear()
        parsedArgs.putAll(snapshot.args)
    }

    inner class Snapshot internal constructor(args: Multimap<String, Any>?) {
        val args: Multimap<String, Any>

        init {
            this.args = ArrayListMultimap.create(args)
        }
    }

    init {
        parsedArgs = ArrayListMultimap.create()
    }
}
