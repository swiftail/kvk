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
package ru.swiftail.kvk.command.api.command.dispatcher;

import com.google.common.collect.Multimap;
import ru.swiftail.kvk.command.api.command.CommandCallable;
import ru.swiftail.kvk.command.api.command.CommandMapping;
import ru.swiftail.kvk.command.api.command.CommandSource;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

/**
 * Executes a command based on user input.
 */
public interface Dispatcher extends CommandCallable {

    Set<? extends CommandMapping> getCommands();

    Set<String> getPrimaryAliases();

    Set<String> getAliases();

    Optional<? extends CommandMapping> get(String alias);

    Optional<? extends CommandMapping> get(String alias, @Nullable CommandSource source);

    Set<? extends CommandMapping> getAll(String alias);

    Multimap<String, CommandMapping> getAll();

    boolean containsAlias(String alias);

    boolean containsMapping(CommandMapping mapping);
}
