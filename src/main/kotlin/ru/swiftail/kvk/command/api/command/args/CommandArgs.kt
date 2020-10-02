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

import com.google.common.collect.ImmutableList
import ru.swiftail.kvk.command.api.command.args.parsing.SingleArg
import java.util.*
import java.util.stream.Collectors

class CommandArgs(
    val raw: String, args: List<SingleArg>?
) {
    private val args: MutableList<SingleArg>
    private var index = -1

    operator fun hasNext(): Boolean {
        return index + 1 < args.size
    }

    @Throws(ArgumentParseException::class)
    fun peek(): String {
        if (!hasNext()) {
            throw createError("Not enough arguments")
        }
        return args[index + 1].value
    }

    @Throws(ArgumentParseException::class)
    operator fun next(): String {
        if (!hasNext()) {
            throw createError(String.format("Not enough arguments!"))
        }
        return args[++index].value
    }

    fun nextIfPresent(): Optional<String> {
        return if (hasNext()) Optional.of(args[++index].value) else Optional.empty()
    }

    fun createError(message: String?): ArgumentParseException {
        return ArgumentParseException(
            message!!,
            raw,
            if (index < 0) 0 else args[index].startIdx
        )
    }

    val all: List<String>
        get() = Collections.unmodifiableList(
            args.stream()
                .map(SingleArg::value)
                .collect(Collectors.toList())
        )

    fun getArgs(): List<SingleArg> {
        return args
    }

    /**
     * Get an arg at the specified position.
     *
     * @param index index of the element to return
     */
    operator fun get(index: Int): String {
        return args[index].value
    }

    /**
     * Insert an arg as the next arg to be returned by [.next].
     *
     * @param value The argument to insert
     */
    fun insertArg(value: String?) {
        val index = if (index < 0) 0 else args[index].endIdx
        args.add(this.index + 1, SingleArg(value!!, index, index))
    }

    /**
     * Remove the arguments parsed between two snapshots.
     *
     * @param startSnapshot The starting state
     * @param endSnapshot The ending state
     */
    fun removeArgs(
        startSnapshot: Snapshot,
        endSnapshot: Snapshot
    ) {
        removeArgs(startSnapshot.index, endSnapshot.index)
    }

    private fun removeArgs(startIdx: Int, endIdx: Int) {
        if (index >= startIdx) {
            if (index < endIdx) {
                index = startIdx - 1
            } else {
                index -= endIdx - startIdx + 1
            }
        }
        for (i in startIdx..endIdx) {
            args.removeAt(startIdx)
        }
    }

    /**
     * Returns the number of arguments
     *
     * @return the number of arguments
     */
    fun size(): Int {
        return args.size
    }

    /**
     * Go back to the previous argument.
     */
    fun previous() {
        if (index > -1) {
            --index
        }
    }

    /**
     * Gets the current position in raw input.
     *
     * @return the raw position
     */
    val rawPosition: Int
        get() = if (index < 0) 0 else args[index].startIdx

    /**
     * Gets a snapshot of the data inside this context to allow it to be
     * restored later.
     *
     * @return The [CommandArgs.Snapshot] containing the current state of the
     * [CommandArgs]
     */
    val snapshot: Snapshot
        get() = Snapshot(index, args)
    /**
     * Resets a [CommandArgs] to a previous state using a previously
     * created [CommandArgs.Snapshot].
     *
     *
     * If resetArgs is set to false, this snapshot will not reset the
     * argument list to its previous state, only the index.
     *
     * @param snapshot The [CommandArgs.Snapshot] to restore this context
     * with
     * @param resetArgs Whether to restore the argument list
     */
    /**
     * Resets a [CommandArgs] to a previous state using a previously
     * created [CommandArgs.Snapshot].
     *
     * @param snapshot The [CommandArgs.Snapshot] to restore this context
     * with
     */
    @JvmOverloads
    fun applySnapshot(
        snapshot: Snapshot,
        resetArgs: Boolean = true
    ) {
        index = snapshot.index
        if (resetArgs) {
            args.clear()
            args.addAll(snapshot.args)
        }
    }

    /**
     * A snapshot of a [CommandArgs]. This object does not contain any
     * public API methods, a snapshot should be considered a black box.
     */
    inner class Snapshot internal constructor(val index: Int, args: List<SingleArg?>?) {
        val args: ImmutableList<SingleArg> = ImmutableList.copyOf(args)
        override fun equals(o: Any?): Boolean {
            if (this === o) {
                return true
            }
            if (o == null || javaClass != o.javaClass) {
                return false
            }
            val snapshot =
                o as Snapshot
            return this.index == snapshot.index &&
                    this.args == snapshot.args
        }

        override fun hashCode(): Int {
            return Objects.hash(this.index, this.args)
        }

    }

    /**
     * Create a new CommandArgs instance with the given raw input and arguments.
     *
     * @param rawInput Raw input
     * @param args Arguments extracted from the raw input
     */
    init {
        this.args = ArrayList(args)
    }
}
