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

import com.google.common.collect.ImmutableList
import ru.swiftail.kvk.command.api.command.args.ArgumentParseException
import java.util.*
import java.util.regex.Pattern

internal class SpaceSplitInputTokenizer private constructor() : InputTokenizer {
    @Throws(ArgumentParseException::class)
    override fun tokenize(arguments: String, lenient: Boolean): List<SingleArg> {

        var arguments = arguments
        if (SPACE_REGEX.matcher(arguments).matches()) {
            return ImmutableList.of()
        }
        val ret: MutableList<SingleArg> = ArrayList()
        var lastIndex = 0
        var spaceIndex: Int
        while (arguments.indexOf(" ").also { spaceIndex = it } != -1) {
            arguments = if (spaceIndex != 0) {
                ret.add(SingleArg(arguments.substring(0, spaceIndex), lastIndex, lastIndex + spaceIndex))
                arguments.substring(spaceIndex)
            } else {
                arguments.substring(1)
            }
            lastIndex += spaceIndex + 1
        }
        ret.add(SingleArg(arguments, lastIndex, lastIndex + arguments.length))
        return ret
    }

    companion object {
        val INSTANCE = SpaceSplitInputTokenizer()
        private val SPACE_REGEX = Pattern.compile("^[ ]*$")
    }
}
