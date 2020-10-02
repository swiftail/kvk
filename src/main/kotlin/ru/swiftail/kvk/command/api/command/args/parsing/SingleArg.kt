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

import com.google.common.base.MoreObjects
import com.google.common.base.Objects

class SingleArg(val value: String, val startIdx: Int, val endIdx: Int) {

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is SingleArg) {
            return false
        }
        return startIdx == o.startIdx && endIdx == o.endIdx && Objects.equal(value, o.value)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(value, startIdx, endIdx)
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("value", value)
            .add("startIdx", startIdx)
            .add("endIdx", endIdx)
            .toString()
    }

}
