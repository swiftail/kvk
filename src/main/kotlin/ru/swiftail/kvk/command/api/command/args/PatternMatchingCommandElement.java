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
package ru.swiftail.kvk.command.api.command.args;

import org.jetbrains.annotations.NotNull;
import ru.swiftail.kvk.command.api.command.CommandSource;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Abstract command element that matches values based on pattern.
 */
public abstract class PatternMatchingCommandElement extends CommandElement {
    private static final String nullKeyArg = "argument";
    final boolean useRegex;

    /**
     * @param yesIWantRegex Specify if you want to allow regex for users of
     *                      the command element. Note that this will open up for DoS attacks.
     */
    protected PatternMatchingCommandElement(@Nullable String key, boolean yesIWantRegex) {
        super(key);
        this.useRegex = yesIWantRegex;
    }

    protected PatternMatchingCommandElement(@Nullable String key) {
        this(key, false);
    }

    @Nullable
    @Override
    protected Object parseValue(@NotNull CommandSource source, CommandArgs args) throws ArgumentParseException {
        Iterable<String> choices = getChoices(source);
        Iterable<Object> ret;
        String arg = args.next();

        // Check to see if we have an exact match first
        Optional<Object> exactMatch = getExactMatch(choices, arg);
        if (exactMatch.isPresent()) {
            // Return this as a collection as this can get transformed by the subclass.
            return Collections.singleton(exactMatch.get());
        }

        if (this.useRegex) {
            Pattern pattern = getFormattedPattern(arg);
            ret = StreamSupport.stream(choices.spliterator(), false).filter(element -> pattern.matcher(element).find()).collect(Collectors.toList()).stream().map(this::getValue).collect(Collectors.toList());
        } else {
            Iterable<String> startsWith = StreamSupport.stream(choices.spliterator(), false).filter(element -> element.regionMatches(true, 0, arg, 0, arg.length())).collect(Collectors.toList());
            ret = StreamSupport.stream(startsWith.spliterator(), false).map(this::getValue).collect(Collectors.toList());
        }

        if (!ret.iterator().hasNext()) {
            throw args.createError(String.format("No values matching pattern '%s' present for %s!", arg, getKey() == null
                    ? nullKeyArg : getKey()));
        }
        return ret;
    }

    Pattern getFormattedPattern(String input) {
        if (!input.startsWith("^")) { // Anchor matches to the beginning -- this lets us use find()
            input = "^" + input;
        }
        return Pattern.compile(input, Pattern.CASE_INSENSITIVE);

    }

    /**
     * Tests a string against a set of valid choices to see if it is a
     * case-insensitive match.
     *
     * @param choices         The choices available to match against
     * @param potentialChoice The potential choice
     * @return If matched, an {@link Optional} containing the matched value
     */
    protected Optional<Object> getExactMatch(final Iterable<String> choices, final String potentialChoice) {
        return StreamSupport.stream(choices.spliterator(), false)
                .filter(potentialChoice::equalsIgnoreCase)
                .findFirst()
                .map(this::getValue);
    }

    /**
     * Gets the available choices for this command source.
     *
     * @param source The source requesting choices
     * @return the possible choices
     */
    protected abstract Iterable<String> getChoices(CommandSource source);

    /**
     * Gets the value for a given choice. For any result in
     * {@link #getChoices(CommandSource)}, this must return a non-null value.
     * Otherwise, an {@link IllegalArgumentException} may be throw.
     *
     * @param choice The specified choice
     * @return the choice's value
     * @throws IllegalArgumentException if the input string is not any return
     *                                  value of {@link #getChoices(CommandSource)}
     */
    protected abstract Object getValue(String choice) throws IllegalArgumentException;
}
