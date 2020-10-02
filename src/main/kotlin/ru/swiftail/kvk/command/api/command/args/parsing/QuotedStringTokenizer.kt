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
package ru.swiftail.kvk.command.api.command.args.parsing

import ru.swiftail.kvk.command.api.command.args.ArgumentParseException
import java.util.*

internal class QuotedStringTokenizer(
    private val handleQuotedStrings: Boolean,
    private val forceLenient: Boolean,
    private val trimTrailingSpace: Boolean
) : InputTokenizer {
    @Throws(ArgumentParseException::class)
    override fun tokenize(arguments: String, lenient: Boolean): List<SingleArg> {
        if (arguments.isEmpty()) {
            return emptyList()
        }
        val state =
            TokenizerState(arguments, lenient)
        val returnedArgs: MutableList<SingleArg> =
            ArrayList(arguments.length / 4)
        if (trimTrailingSpace) {
            skipWhiteSpace(state)
        }
        while (state.hasMore()) {
            if (!trimTrailingSpace) {
                skipWhiteSpace(state)
            }
            val startIdx = state.index + 1
            val arg = nextArg(state)
            returnedArgs.add(SingleArg(arg, startIdx, state.index))
            if (trimTrailingSpace) {
                skipWhiteSpace(state)
            }
        }
        return returnedArgs
    }

    // Parsing methods
    @Throws(ArgumentParseException::class)
    private fun skipWhiteSpace(state: TokenizerState) {
        if (!state.hasMore()) {
            return
        }
        while (state.hasMore() && Character.isWhitespace(state.peek())) {
            state.next()
        }
    }

    @Throws(ArgumentParseException::class)
    private fun nextArg(state: TokenizerState): String {
        val argBuilder = StringBuilder()
        if (state.hasMore()) {
            val codePoint = state.peek()
            if (handleQuotedStrings && (codePoint == CHAR_DOUBLE_QUOTE || codePoint == CHAR_SINGLE_QUOTE)) {
                // quoted string
                parseQuotedString(state, codePoint, argBuilder)
            } else {
                parseUnquotedString(state, argBuilder)
            }
        }
        return argBuilder.toString()
    }

    @Throws(ArgumentParseException::class)
    private fun parseQuotedString(
        state: TokenizerState,
        startQuotation: Int,
        builder: StringBuilder
    ) {
        // Consume the start quotation character
        var nextCodePoint = state.next()
        if (nextCodePoint != startQuotation) {
            throw state.createException(
                String.format(
                    "Actual next character '%c' did not match expected quotation character '%c'",
                    nextCodePoint, startQuotation
                )
            )
        }
        while (true) {
            if (!state.hasMore()) {
                if (state.isLenient || forceLenient) {
                    return
                }
                throw state.createException("Unterminated quoted string found")
            }
            nextCodePoint = state.peek()
            when (nextCodePoint) {
                startQuotation -> {
                    state.next()
                    return
                }
                CHAR_BACKSLASH -> {
                    parseEscape(state, builder)
                }
                else -> {
                    builder.appendCodePoint(state.next())
                }
            }
        }
    }

    @Throws(ArgumentParseException::class)
    private fun parseUnquotedString(
        state: TokenizerState,
        builder: StringBuilder
    ) {
        while (state.hasMore()) {
            val nextCodePoint = state.peek()
            when {
                Character.isWhitespace(nextCodePoint) -> {
                    return
                }
                nextCodePoint == CHAR_BACKSLASH -> {
                    parseEscape(state, builder)
                }
                else -> {
                    builder.appendCodePoint(state.next())
                }
            }
        }
    }

    @Throws(ArgumentParseException::class)
    private fun parseEscape(
        state: TokenizerState,
        builder: StringBuilder
    ) {
        state.next() // Consume \
        builder.appendCodePoint(state.next()) // TODO: Unicode character escapes (\u00A7 type thing)?
    }

    companion object {
        private const val CHAR_BACKSLASH = '\\'.toInt()
        private const val CHAR_SINGLE_QUOTE = '\''.toInt()
        private const val CHAR_DOUBLE_QUOTE = '"'.toInt()
    }

}
