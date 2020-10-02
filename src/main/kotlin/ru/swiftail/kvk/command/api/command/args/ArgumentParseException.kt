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

import com.google.common.base.Strings
import ru.swiftail.kvk.command.api.command.CommandException

/**
 * Exception thrown when an error occurs while parsing arguments.
 */
open class ArgumentParseException : CommandException {

    val sourceString: String

    val position: Int

    constructor(message: String, source: String, position: Int) : super(message, true) {
        sourceString = source
        this.position = position
    }

    constructor(message: String?, cause: Throwable?, source: String, position: Int) : super(
        message,
        cause,
        true
    ) {
        sourceString = source
        this.position = position
    }

    private val superMessage: String?
        get() = super.message

    val annotatedPosition: String
        get() {
            var source = sourceString
            var position = position
            if (source.length > 80) {
                if (position >= 37) {
                    val startPos = position - 37
                    val endPos = Math.min(source.length, position + 37)
                    source = if (endPos < source.length) {
                        "..." + source.substring(startPos, endPos) + "..."
                    } else {
                        "..." + source.substring(startPos, endPos)
                    }
                    position -= 40
                } else {
                    source = source.substring(0, 77) + "..."
                }
            }
            return """
        $source
        ${Strings.repeat(" ", position)}^
        """.trimIndent()
        }

    class WithUsage(
        wrapped: ArgumentParseException,
        val usage: String
    ) : ArgumentParseException(
        wrapped.superMessage,
        wrapped.cause,
        wrapped.sourceString,
        wrapped.position
    ) {

        companion object {
            private const val serialVersionUID = -786214501012293475L
        }

    }

    companion object {
        private const val serialVersionUID = -8555316116315990226L
    }
}
