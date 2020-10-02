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

import ru.swiftail.kvk.command.api.command.CommandSource
import ru.swiftail.kvk.command.api.command.args.CommandContext

typealias ExecutorClosure = suspend ExecutorContext.(src: CommandSource, args: CommandContext) -> Unit

interface ICommandExecutor {
    suspend fun execute(execCtx: ExecutorContext, src: CommandSource, args: CommandContext)

    suspend operator fun invoke(execCtx: ExecutorContext, src: CommandSource, args: CommandContext) {
        execute(execCtx, src, args)
    }
}

class CommandExecutor(private val executorClosure: ExecutorClosure) : ICommandExecutor {
    override suspend fun execute(execCtx: ExecutorContext, src: CommandSource, args: CommandContext) {
        // We're already in the executor service
        executorClosure(execCtx, src, args)
    }
}
