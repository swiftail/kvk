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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.beryx.awt.color.ColorFactory;
import org.jetbrains.annotations.NotNull;
import ru.swiftail.kvk.command.api.command.CommandMessageFormatting;
import ru.swiftail.kvk.command.api.command.CommandSource;
import ru.swiftail.kvk.command.api.command.Tristate;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Class containing factory methods for common command elements.
 */
@SuppressWarnings("unused")
public final class GenericArguments {

    private static final CommandElement NONE = new SequenceCommandElement(ImmutableList.of());
    private static final Map<String, Boolean> BOOLEAN_CHOICES = ImmutableMap.<String, Boolean>builder()
            .put("true", true)
            .put("t", true)
            .put("y", true)
            .put("yes", true)
            .put("verymuchso", true)
            .put("1", true)
            .put("false", false)
            .put("f", false)
            .put("n", false)
            .put("no", false)
            .put("notatall", false)
            .put("0", false)
            .build();

    private GenericArguments() {
    }

    /**
     * Expects no arguments, returns no values.
     *
     * @return An expectation of no arguments
     */
    public static CommandElement none() {
        return NONE;
    }

    /**
     * Expects no arguments. Adds 'true' to the context when parsed.
     *
     * <p>This will return only one value.</p>
     *
     * @param key the key to store 'true' under
     * @return the argument
     */
    public static CommandElement markTrue(String key) {
        return new MarkTrueCommandElement(key);
    }

    /**
     * Gets a builder to create a command element that parses flags.
     *
     * <p>There should only be ONE of these in a command element sequence if you
     * wish to use flags. A {@link CommandFlags.Builder} can handle multiple
     * flags that have different behaviors. Using multiple builders in the same
     * sequence may cause unexpected behavior.</p>
     *
     * <p>Any command elements that are not associated with flags should be
     * placed into the {@link CommandFlags.Builder#buildWith(CommandElement)}
     * parameter, allowing the flags to be used throughout the argument string.
     * </p>
     *
     * @return the newly created builder
     */
    public static CommandFlags.Builder flags() {
        return new CommandFlags.Builder();
    }

    /**
     * Consumes a series of arguments. Usage is the elements concatenated
     *
     * @param elements The series of arguments to expect
     * @return the element to match the input
     */
    public static CommandElement seq(CommandElement... elements) {
        return new SequenceCommandElement(ImmutableList.copyOf(elements));
    }

    /**
     * Return an argument that allows selecting from a limited set of values.
     *
     * <p>If there are 5 or fewer choices available, the choices will be shown
     * in the command usage. Otherwise, the usage will only display only the
     * key.</p>
     *
     * <p>Choices are <strong>case sensitive</strong>. If you do not require
     * case sensitivity, see {@link #choicesInsensitive(String, Map)}.</p>
     *
     * <p>To override this behavior, see
     * {@link #choices(String, Map, boolean, boolean)}.</p>
     *
     * <p>When parsing, only one choice may be selected, returning its
     * associated value.</p>
     *
     * @param key     The key to store the resulting value under
     * @param choices The choices users can choose from
     * @return the element to match the input
     */
    public static CommandElement choices(String key, Map<String, ?> choices) {
        return choices(key, choices, choices.size() <= ChoicesCommandElement.CUTOFF, true);
    }

    /**
     * Return an argument that allows selecting from a limited set of values.
     *
     * <p>If there are 5 or fewer choices available, the choices will be shown
     * in the command usage. Otherwise, the usage will only display only the
     * key.</p>
     *
     * <p>Choices are <strong>not case sensitive</strong>. If you require
     * case sensitivity, see {@link #choices(String, Map)}</p>
     *
     * <p>To override this behavior, see
     * {@link #choices(String, Map, boolean, boolean)}.</p>
     *
     * <p>When parsing, only one choice may be selected, returning its
     * associated value.</p>
     *
     * @param key     The key to store the resulting value under
     * @param choices The choices users can choose from
     * @return the element to match the input
     */
    public static CommandElement choicesInsensitive(String key, Map<String, ?> choices) {
        return choices(key, choices, choices.size() <= ChoicesCommandElement.CUTOFF, false);
    }

    /**
     * Return an argument that allows selecting from a limited set of values.
     *
     * <p>Unless {@code choicesInUsage} is true, general command usage will only
     * display the provided key.</p>
     *
     * <p>Choices are <strong>case sensitive</strong>. If you do not require
     * case sensitivity, see {@link #choices(String, Map, boolean, boolean)}</p>
     *
     * <p>When parsing, only one choice may be selected, returning its
     * associated value.</p>
     *
     * @param key            The key to store the resulting value under
     * @param choices        The choices users can choose from
     * @param choicesInUsage Whether to display the available choices, or simply
     *                       the provided key, as part of usage
     * @return the element to match the input
     */
    public static CommandElement choices(String key, Map<String, ?> choices, boolean choicesInUsage) {
        return choices(key, choices, choicesInUsage, true);
    }

    /**
     * Return an argument that allows selecting from a limited set of values.
     *
     * <p>Unless {@code choicesInUsage} is true, general command usage will only
     * display the provided key.</p>
     *
     * <p>When parsing, only one choice may be selected, returning its
     * associated value.</p>
     *
     * @param key            The key to store the resulting value under
     * @param choices        The choices users can choose from
     * @param choicesInUsage Whether to display the available choices, or simply
     *                       the provided key, as part of usage
     * @param caseSensitive  Whether the matches should be case sensitive
     * @return the element to match the input
     */
    public static CommandElement choices(String key, Map<String, ?> choices, boolean choicesInUsage, boolean caseSensitive) {
        if (!caseSensitive) {
            Map<String, Object> immChoices = choices.entrySet().stream()
                    .collect(Collectors.toMap(x -> x.getKey().toLowerCase(), Map.Entry::getValue));
            return choices(key, immChoices::keySet, selection -> immChoices.get(selection.toLowerCase()), choicesInUsage);
        }
        Map<String, Object> immChoices = ImmutableMap.copyOf(choices);
        return choices(key, immChoices::keySet, immChoices::get, choicesInUsage);
    }

    /**
     * Return an argument that allows selecting from a limited set of values.
     *
     * <p>If there are 5 or fewer choices available, the choices will be shown
     * in the command usage. Otherwise, the usage will only display only the
     * key.</p>
     *
     * <p>To override this behavior, see {@link #choices(String, Map, boolean)}.
     * </p>
     *
     * <p>Only one choice may be selected, returning its associated value.</p>
     *
     * @param key    The key to store the resulting value under
     * @param keys   The function that will supply available keys
     * @param values The function that maps an element of {@code key} to a value
     *               and any other key to {@code null}
     * @return the element to match the input
     */
    public static CommandElement choices(String key, Supplier<Collection<String>> keys, Function<String, ?> values) {
        return new ChoicesCommandElement(key, keys, values, Tristate.UNDEFINED);
    }

    /**
     * Return an argument that allows selecting from a limited set of values.
     * Unless {@code choicesInUsage} is true, general command usage will only
     * display the provided key.
     *
     * <p>Only one choice may be selected, returning its associated value.</p>
     *
     * @param key            The key to store the resulting value under
     * @param keys           The function that will supply available keys
     * @param values         The function that maps an element of {@code key} to a value
     *                       and any other key to {@code null}
     * @param choicesInUsage Whether to display the available choices, or simply
     *                       the provided key, as part of usage
     * @return the element to match the input
     */
    public static CommandElement choices(String key, Supplier<Collection<String>> keys, Function<String, ?> values, boolean choicesInUsage) {
        return new ChoicesCommandElement(key, keys, values, choicesInUsage ? Tristate.TRUE : Tristate.FALSE);
    }

    /**
     * Returns a command element that matches the first of the provided elements
     * that parses tab completion matches from all options.
     *
     * @param elements The elements to check against
     * @return The command element matching the first passing of the elements
     * provided
     */
    public static CommandElement firstParsing(CommandElement... elements) {
        return new FirstParsingCommandElement(ImmutableList.copyOf(elements));
    }

    /**
     * Make the provided command element optional.
     *
     * <p>This means the command element is not required. However, if the
     * element is provided with invalid format and there are no more args
     * specified, any errors will still be passed on.</p>
     *
     * @param element The element to optionally require
     * @return the element to match the input
     */
    public static CommandElement optional(CommandElement element) {
        return new OptionalCommandElement(element, null, false);
    }

    /**
     * Make the provided command element optional.
     *
     * <p>This means the command element is not required. However, if the
     * element is provided with invalid format and there are no more args
     * specified, any errors will still be passed on. If the given element's key
     * and {@code value} are not null and this element is not provided the
     * element's key will be set to the given value.</p>
     *
     * @param element The element to optionally require
     * @param value   The default value to set
     * @return the element to match the input
     */
    public static CommandElement optional(CommandElement element, Object value) {
        return new OptionalCommandElement(element, value, false);
    }

    /**
     * Make the provided command element optional
     * This means the command element is not required.
     * If the argument is provided but of invalid format, it will be skipped.
     *
     * @param element The element to optionally require
     * @return the element to match the input
     */
    public static CommandElement optionalWeak(CommandElement element) {
        return new OptionalCommandElement(element, null, true);
    }

    /**
     * <p>Make the provided command element optional.</p>
     *
     * <p>This means the command element is not required.</p>
     *
     * <ul>
     *     <li>If the argument is provided but of invalid format, it will be
     *     skipped.</li>
     *     <li>If the given element's key and {@code value} are not null and
     *     this element is not provided the element's key will be set to the
     *     given value.</li>
     * </ul>
     *
     * @param element The element to optionally require
     * @param value   The default value to set
     * @return the element to match the input
     */
    public static CommandElement optionalWeak(CommandElement element, Object value) {
        return new OptionalCommandElement(element, value, true);
    }

    /**
     * Require all remaining args to match as many instances of
     * {@link CommandElement} as will fit. Command element values will be stored
     * under their provided keys in the CommandContext.
     *
     * @param element The element to repeat
     * @return the element to match the input
     */
    public static CommandElement allOf(CommandElement element) {
        return new AllOfCommandElement(element);
    }

    /**
     * Require an argument to be a string. Any provided argument will fit in
     * under this argument.
     *
     * <p>Gives values of type {@link String}. This will return only one value.
     * </p>
     *
     * @param key The key to store the parsed argument under
     * @return the element to match the input
     */
    public static CommandElement string(String key) {
        return new StringElement(key);
    }

    /**
     * Require an argument to be an integer (base 10).
     *
     * <p>Gives values of type {@link Integer}. This will return only one value.
     * </p>
     *
     * @param key The key to store the parsed argument under
     * @return the element to match the input
     */
    public static CommandElement integer(String key) {
        return new NumericElement<>(key, Integer::parseInt, Integer::parseInt, input -> String.format("Expected an integer, but input '%s' was not", input));
    }

    /**
     * Require an argument to be a long (base 10).
     *
     * <p>Gives values of type {@link Long}. This will return only one value.
     * </p>
     *
     * @param key The key to store the parsed argument under
     * @return the element to match the input
     */
    public static CommandElement longNum(String key) {
        return new NumericElement<>(key, Long::parseLong, Long::parseLong, input -> String.format("Expected a long, but input '%s' was not", input));
    }

    /**
     * Require an argument to be an double-precision floating point number.
     *
     * <p>Gives values of type {@link Double}. This will return only one value.
     * </p>
     *
     * @param key The key to store the parsed argument under
     * @return the element to match the input
     */
    public static CommandElement doubleNum(String key) {
        return new NumericElement<>(key, Double::parseDouble, null, input -> String.format("Expected a number, but input '%s' was not", input));
    }

    /**
     * Require an argument to be a boolean.
     *
     * <p>The recognized true values are:</p>
     *
     * <ul>
     *     <li>true</li>
     *     <li>t</li>
     *     <li>yes</li>
     *     <li>y</li>
     *     <li>verymuchso</li>
     * </ul>
     *
     *
     * <p>The recognized false values are:</p>
     *
     * <ul>
     *     <li>false</li>
     *     <li>f</li>
     *     <li>no</li>
     *     <li>n</li>
     *     <li>notatall</li>
     * </ul>
     *
     * <p>Gives values of type {@link Boolean}. This will return only one value.
     * </p>
     *
     * @param key The key to store the parsed argument under
     * @return the element to match the input
     */
    public static CommandElement bool(String key) {
        return GenericArguments.choices(key, BOOLEAN_CHOICES);
    }

    // -- Argument types for basic java types

    /*
     * Parent class that specifies elements as having no tab completions.
     * Useful for inputs with a very large domain, like strings and integers.
     */

    /**
     * Require the argument to be a key under the provided enum.
     *
     * <p>Gives values of type <tt>T</tt>. This will return only one value.</p>
     *
     * @param key  The key to store the matched enum value under
     * @param type The enum class to get enum constants from
     * @param <T>  The type of enum
     * @return the element to match the input
     */
    public static <T extends Enum<T>> CommandElement enumValue(String key, Class<T> type) {
        return new EnumValueElement<>(key, type);
    }

    /**
     * Require one or more strings, which are combined into a single,
     * space-separated string.
     *
     * <p>Gives values of type {@link String}. This will return only one value.
     * </p>
     *
     * @param key The key to store the parsed argument under
     * @return the element to match the input
     */
    public static CommandElement remainingJoinedStrings(String key) {
        return new RemainingJoinedStringsCommandElement(key, false);
    }

    /**
     * Require one or more strings, without any processing, which are combined
     * into a single, space-separated string.
     *
     * <p>Gives values of type {@link String}. This will return only one value.
     * </p>
     *
     * @param key The key to store the parsed argument under
     * @return the element to match the input
     */
    public static CommandElement remainingRawJoinedStrings(String key) {
        return new RemainingJoinedStringsCommandElement(key, true);
    }

    /**
     * Restricts the given command element to only insert one value into the
     * context at the provided key.
     *
     * <p>If more than one value is returned by an element, or the target key
     * already contains a value, this will throw an
     * {@link ArgumentParseException}</p>
     *
     * @param element The element to restrict
     * @return the restricted element
     */
    public static CommandElement onlyOne(CommandElement element) {
        return new OnlyOneCommandElement(element);
    }

    /**
     * Checks a permission for a given command argument to be used.
     *
     * <p>If the element attempts to parse an argument and the user does not
     * have the permission, an {@link ArgumentParseException} will be thrown.</p>
     *
     * @param element    The element to wrap
     * @param permission The permission to check
     * @return the element
     */
    public static CommandElement requiringPermission(CommandElement element, String permission) {
        return new PermissionCommandElement(element, permission, false);
    }

    /**
     * Checks a permission for a given command argument to be used.
     *
     * <p>If the element attempts to parse an argument and the user does not
     * have the permission, the element will be skipped over.</p>
     *
     * <p>If the invoking {@link CommandSource} has permission to use the
     * element, but the wrapped element fails to parse, an exception will
     * be reported in the normal way. If you require this element to be
     * truly optional, wrap this element in either
     * {@link #optional(CommandElement)} or
     * {@link #optionalWeak(CommandElement)}, as required.</p>
     *
     * @param element    The element to wrap
     * @param permission The permission to check
     * @return the element
     */
    public static CommandElement requiringPermissionWeak(CommandElement element, String permission) {
        return new PermissionCommandElement(element, permission, true);
    }

    /**
     * Expect an argument to represent a {@link URL}.
     *
     * <p>This will return only one value.</p>
     *
     * @param key The key to store under
     * @return the argument
     */
    public static CommandElement url(String key) {
        return new UrlElement(key);
    }

    /**
     * Expect an argument to return a {@link BigDecimal}.
     *
     * @param key The key to store under
     * @return the argument
     */
    public static CommandElement bigDecimal(String key) {
        return new BigDecimalElement(key);
    }

    /**
     * Expect an argument to return a {@link BigInteger}.
     *
     * <p>This will return only one value.</p>
     *
     * @param key The key to store under
     * @return the argument
     */
    public static CommandElement bigInteger(String key) {
        return new BigIntegerElement(key);
    }


    /**
     * Expect an argument to be a {@link UUID}.
     *
     * <p>This will return only one value.</p>
     *
     * @param key The key to store under
     * @return the argument
     */
    public static CommandElement uuid(String key) {
        return new UuidElement(key);
    }

    /**
     * Expect an argument to be a date-time, in the form of a
     * {@link LocalDateTime}. If no date is specified, {@link LocalDate#now()}
     * is used; if no time is specified, {@link LocalTime#MIDNIGHT} is used.
     *
     * <p>Date-times are expected in the ISO-8601 format.</p>
     *
     * <p>This will return only one value.</p>
     *
     * @param key The key to store under
     * @return the argument
     * @see <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO-8601</a>
     */
    public static CommandElement dateTime(String key) {
        return new DateTimeElement(key, false);
    }

    /**
     * Expect an argument to be a date-time, in the form of a
     * {@link LocalDateTime}. If no date is specified, {@link LocalDate#now()}
     * is used; if no time is specified, {@link LocalTime#MIDNIGHT} is used.
     *
     * <p>If no argument at all is specified, defaults to
     * {@link LocalDateTime#now()}.</p>
     *
     * <p>Date-times are expected in the ISO-8601 format.</p>
     *
     * <p>This will return only one value.</p>
     *
     * @param key The key to store under
     * @return the argument
     */
    public static CommandElement dateTimeOrNow(String key) {
        return new DateTimeElement(key, true);
    }

    /**
     * Expect an argument to be a {@link Duration}.
     *
     * <p>Durations are expected in the following format: {@code #D#H#M#S}.
     * This is not case sensitive.</p>
     *
     * <p>This will return only one value.</p>
     *
     * @param key The key to store under
     * @return the argument
     */
    public static CommandElement duration(String key) {
        return new DurationElement(key);
    }

    public static CommandElement color(String key) {
        return new ColorElement(key);
    }

    static class MarkTrueCommandElement extends CommandElement {

        MarkTrueCommandElement(String key) {
            super(key);
        }

        @Override
        protected Object parseValue(@NotNull CommandSource source, @NotNull CommandArgs args) {
            return true;
        }

        @NotNull
        @Override
        public String getUsage(CommandSource src) {
            return "";
        }
    }

    private static class SequenceCommandElement extends CommandElement {
        private final List<CommandElement> elements;

        SequenceCommandElement(List<CommandElement> elements) {
            super(null);
            this.elements = elements;
        }

        @Override
        public void parse(@NotNull CommandSource source, @NotNull CommandArgs args, @NotNull CommandContext context) throws ArgumentParseException {
            for (CommandElement element : this.elements) {
                element.parse(source, args, context);
            }
        }

        @Override
        protected Object parseValue(@NotNull CommandSource source, @NotNull CommandArgs args) {
            return null;
        }

        @NotNull
        @Override
        public String getUsage(CommandSource commander) {
            final StringBuilder build = new StringBuilder();
            for (Iterator<CommandElement> it = this.elements.iterator(); it.hasNext(); ) {
                String usage = it.next().getUsage(commander);
                if (!usage.isEmpty()) {
                    build.append(usage);
                    if (it.hasNext()) {
                        build.append(CommandMessageFormatting.SPACE_TEXT);
                    }
                }
            }
            return build.toString();
        }
    }

    private static class ChoicesCommandElement extends CommandElement {
        private static final int CUTOFF = 5;
        private final Supplier<Collection<String>> keySupplier;
        private final Function<String, ?> valueSupplier;
        private final Tristate choicesInUsage;

        ChoicesCommandElement(String key, Supplier<Collection<String>> keySupplier, Function<String, ?> valueSupplier, Tristate choicesInUsage) {
            super(key);
            this.keySupplier = keySupplier;
            this.valueSupplier = valueSupplier;
            this.choicesInUsage = choicesInUsage;
        }

        @Override
        public Object parseValue(@NotNull CommandSource source, CommandArgs args) throws ArgumentParseException {
            Object value = this.valueSupplier.apply(args.next());
            if (value == null) {
                throw args.createError(String.format("Argument was not a valid choice. Valid choices: %s", this.keySupplier.get().toString()));
            }
            return value;
        }

        @NotNull
        @Override
        public String getUsage(CommandSource commander) {
            Collection<String> keys = this.keySupplier.get();
            if (this.choicesInUsage == Tristate.TRUE || (this.choicesInUsage == Tristate.UNDEFINED && keys.size() <= CUTOFF)) {
                final StringBuilder build = new StringBuilder();
                build.append(CommandMessageFormatting.LT_TEXT);
                for (Iterator<String> it = keys.iterator(); it.hasNext(); ) {
                    build.append((it.next()));
                    if (it.hasNext()) {
                        build.append(CommandMessageFormatting.PIPE_TEXT);
                    }
                }
                build.append(CommandMessageFormatting.GT_TEXT);
                return build.toString();
            }
            return super.getUsage(commander);
        }
    }

    private static class FirstParsingCommandElement extends CommandElement {
        private final List<CommandElement> elements;

        FirstParsingCommandElement(List<CommandElement> elements) {
            super(null);
            this.elements = elements;
        }

        @Override
        public void parse(@NotNull CommandSource source, @NotNull CommandArgs args, @NotNull CommandContext context) throws ArgumentParseException {
            ArgumentParseException lastException = null;
            for (CommandElement element : this.elements) {
                CommandArgs.Snapshot startState = args.getSnapshot();
                CommandContext.Snapshot contextSnapshot = context.createSnapshot();
                try {
                    element.parse(source, args, context);
                    return;
                } catch (ArgumentParseException ex) {
                    lastException = ex;
                    args.applySnapshot(startState);
                    context.applySnapshot(contextSnapshot);
                }
            }
            if (lastException != null) {
                throw lastException;
            }
        }

        @Override
        protected Object parseValue(@NotNull CommandSource source, @NotNull CommandArgs args) {
            return null;
        }

        @NotNull
        @Override
        public String getUsage(CommandSource commander) {
            final StringBuilder ret = new StringBuilder();
            for (Iterator<CommandElement> it = this.elements.iterator(); it.hasNext(); ) {
                ret.append(it.next().getUsage(commander));
                if (it.hasNext()) {
                    ret.append(CommandMessageFormatting.PIPE_TEXT);
                }
            }
            return ret.toString();
        }
    }

    private static class OptionalCommandElement extends CommandElement {
        private final CommandElement element;
        @Nullable
        private final Object value;
        private final boolean considerInvalidFormatEmpty;

        OptionalCommandElement(CommandElement element, @Nullable Object value, boolean considerInvalidFormatEmpty) {
            super(null);
            this.element = element;
            this.value = value;
            this.considerInvalidFormatEmpty = considerInvalidFormatEmpty;
        }

        @Override
        public void parse(@NotNull CommandSource source, CommandArgs args, @NotNull CommandContext context) throws ArgumentParseException {
            if (!args.hasNext()) {
                String key = this.element.getKey();
                if (key != null && this.value != null) {
                    context.putArg(key, this.value);
                }
                return;
            }
            CommandArgs.Snapshot startState = args.getSnapshot();
            try {
                this.element.parse(source, args, context);
            } catch (ArgumentParseException ex) {
                if (this.considerInvalidFormatEmpty || args.hasNext()) { // If there are more args, suppress. Otherwise, throw the error
                    args.applySnapshot(startState);
                    if (this.element.getKey() != null && this.value != null) {
                        context.putArg(this.element.getKey(), this.value);
                    }
                } else {
                    throw ex;
                }
            }
        }

        @Override
        protected Object parseValue(@NotNull CommandSource source, CommandArgs args) throws ArgumentParseException {
            return args.hasNext() ? null : this.element.parseValue(source, args);
        }

        @NotNull
        @Override
        public String getUsage(CommandSource src) {
            final String containingUsage = this.element.getUsage(src);
            if (containingUsage.isEmpty()) {
                return "";
            }
            return ("[" + this.element.getUsage(src) + "]");
        }
    }

    private static class AllOfCommandElement extends CommandElement {
        private final CommandElement element;


        protected AllOfCommandElement(CommandElement element) {
            super(null);
            this.element = element;
        }

        @Override
        public void parse(@NotNull CommandSource source, CommandArgs args, @NotNull CommandContext context) throws ArgumentParseException {
            while (args.hasNext()) {
                this.element.parse(source, args, context);
            }
        }

        @Override
        protected Object parseValue(@NotNull CommandSource source, @NotNull CommandArgs args) {
            return null;
        }

        @NotNull
        @Override
        public String getUsage(CommandSource context) {
            return (this.element.getUsage(context) + CommandMessageFormatting.STAR_TEXT);
        }
    }

    private static class StringElement extends CommandElement {

        StringElement(String key) {
            super(key);
        }

        @Override
        public Object parseValue(@NotNull CommandSource source, CommandArgs args) throws ArgumentParseException {
            return args.next();
        }
    }

    private static class NumericElement<T extends Number> extends CommandElement {
        private final Function<String, T> parseFunc;
        @Nullable
        private final BiFunction<String, Integer, T> parseRadixFunction;
        private final Function<String, String> errorSupplier;

        protected NumericElement(String key, Function<String, T> parseFunc, @Nullable BiFunction<String, Integer, T> parseRadixFunction,
                                 Function<String, String> errorSupplier) {
            super(key);
            this.parseFunc = parseFunc;
            this.parseRadixFunction = parseRadixFunction;
            this.errorSupplier = errorSupplier;
        }

        @Override
        public Object parseValue(@NotNull CommandSource source, CommandArgs args) throws ArgumentParseException {
            final String input = args.next();
            try {
                if (this.parseRadixFunction != null) {
                    if (input.startsWith("0x")) {
                        return this.parseRadixFunction.apply(input.substring(2), 16);
                    } else if (input.startsWith("0b")) {
                        return this.parseRadixFunction.apply(input.substring(2), 2);
                    }
                }
                return this.parseFunc.apply(input);
            } catch (NumberFormatException ex) {
                throw args.createError(this.errorSupplier.apply(input));
            }
        }
    }

    private static class EnumValueElement<T extends Enum<T>> extends PatternMatchingCommandElement {
        private final Class<T> type;
        private final Map<String, T> values;

        EnumValueElement(String key, Class<T> type) {
            super(key);
            this.type = type;
            this.values = Arrays.stream(type.getEnumConstants())
                    .collect(Collectors.toMap(value -> value.name().toLowerCase(),
                            Function.identity(), (value, value2) -> {
                                throw new UnsupportedOperationException(type.getCanonicalName() + " contains more than one enum constant "
                                        + "with the same name, only differing by capitalization, which is unsupported.");
                            }
                    ));
        }

        @Override
        protected Iterable<String> getChoices(CommandSource source) {
            return this.values.keySet();
        }

        @Override
        protected Object getValue(String choice) throws IllegalArgumentException {
            T value = this.values.get(choice.toLowerCase());
            if (value == null) {
                throw new IllegalArgumentException("No enum constant " + this.type.getCanonicalName() + "." + choice);
            }

            return value;
        }
    }

    private static class RemainingJoinedStringsCommandElement extends CommandElement {
        private final boolean raw;

        RemainingJoinedStringsCommandElement(String key, boolean raw) {
            super(key);
            this.raw = raw;
        }

        @Override
        protected Object parseValue(@NotNull CommandSource source, @NotNull CommandArgs args) throws ArgumentParseException {
            if (this.raw) {
                args.next();
                String ret = args.getRaw().substring(args.getRawPosition());
                while (args.hasNext()) { // Consume remaining args
                    args.next();
                }
                return ret;
            }
            final StringBuilder ret = new StringBuilder(args.next());
            while (args.hasNext()) {
                ret.append(' ').append(args.next());
            }
            return ret.toString();
        }

        @NotNull
        @Override
        public String getUsage(CommandSource src) {
            return (CommandMessageFormatting.LT_TEXT + getKey() +
                    CommandMessageFormatting.ELLIPSIS_TEXT + CommandMessageFormatting.GT_TEXT);
        }
    }

    private static class OnlyOneCommandElement extends CommandElement {
        private final CommandElement element;

        protected OnlyOneCommandElement(CommandElement element) {
            super(element.getKey());
            this.element = element;
        }

        @Override
        public void parse(@NotNull CommandSource source, @NotNull CommandArgs args, @NotNull CommandContext context) throws ArgumentParseException {
            this.element.parse(source, args, context);
            if (context.getAll(this.element.getKey()).size() > 1) {
                String key = this.element.getKey();
                throw args.createError(String.format("Argument %s may have only one value!", key != null ? key : "unknown"));
            }
        }

        @NotNull
        @Override
        public String getUsage(CommandSource src) {
            return this.element.getUsage(src);
        }

        @Nullable
        @Override
        protected Object parseValue(@NotNull CommandSource source, @NotNull CommandArgs args) throws ArgumentParseException {
            return this.element.parseValue(source, args);
        }

    }

    private static class PermissionCommandElement extends CommandElement {
        private final CommandElement element;
        private final String permission;
        private final boolean isOptional;

        protected PermissionCommandElement(CommandElement element, String permission, boolean isOptional) {
            super(element.getKey());
            this.element = element;
            this.permission = permission;
            this.isOptional = isOptional;
        }

        @Nullable
        @Override
        protected Object parseValue(@NotNull CommandSource source, @NotNull CommandArgs args) throws ArgumentParseException {
            if (checkPermission(source, args)) {
                return this.element.parseValue(source, args);
            }

            return null;
        }

        private boolean checkPermission(CommandSource source, CommandArgs args) throws ArgumentParseException {
            boolean hasPermission = source.hasPermission(this.permission);
            if (!hasPermission && !this.isOptional) {
                String key = getKey();
                throw args.createError(String.format("You do not have permission to use the %s argument", key != null ? key : "unknown"));
            }

            return hasPermission;
        }

        @Override
        public void parse(@NotNull CommandSource source, @NotNull CommandArgs args, @NotNull CommandContext context) throws ArgumentParseException {
            if (checkPermission(source, args)) {
                this.element.parse(source, args, context);
            }
        }

        @NotNull
        @Override
        public String getUsage(CommandSource src) {
            if (this.isOptional && !src.hasPermission(this.permission)) {
                return "";
            }
            return this.element.getUsage(src);
        }
    }

    private static class UrlElement extends CommandElement {

        protected UrlElement(String key) {
            super(key);
        }

        @Nullable
        @Override
        protected Object parseValue(@NotNull CommandSource source, CommandArgs args) throws ArgumentParseException {
            String str = args.next();
            URL url;
            try {
                url = new URL(str);
            } catch (MalformedURLException ex) {
                throw new ArgumentParseException(("Invalid URL!"), ex, str, 0);
            }
            try {
                url.toURI();
            } catch (URISyntaxException ex) {
                throw new ArgumentParseException(("Invalid URL!"), ex, str, 0);
            }
            return url;
        }
    }

    private static class BigDecimalElement extends CommandElement {

        protected BigDecimalElement(String key) {
            super(key);
        }

        @Nullable
        @Override
        protected Object parseValue(@NotNull CommandSource source, CommandArgs args) throws ArgumentParseException {
            String next = args.next();
            try {
                return new BigDecimal(next);
            } catch (NumberFormatException ex) {
                throw args.createError(("Expected a number, but input " + next + " was not"));
            }
        }
    }

    private static class BigIntegerElement extends CommandElement {

        protected BigIntegerElement(String key) {
            super(key);
        }

        @Nullable
        @Override
        protected Object parseValue(@NotNull CommandSource source, CommandArgs args) throws ArgumentParseException {
            String integerString = args.next();
            try {
                return new BigInteger(integerString);
            } catch (NumberFormatException ex) {
                throw args.createError(("Expected an integer, but input " + integerString + " was not"));
            }
        }
    }

    private static class UuidElement extends CommandElement {

        protected UuidElement(String key) {
            super(key);
        }

        @Nullable
        @Override
        protected Object parseValue(@NotNull CommandSource source, CommandArgs args) throws ArgumentParseException {
            try {
                return UUID.fromString(args.next());
            } catch (IllegalArgumentException ex) {
                throw args.createError(("Invalid UUID!"));
            }
        }

    }

    private static class DateTimeElement extends CommandElement {

        private final boolean returnNow;

        protected DateTimeElement(String key, boolean returnNow) {
            super(key);
            this.returnNow = returnNow;
        }

        @Nullable
        @Override
        protected Object parseValue(@NotNull CommandSource source, CommandArgs args) throws ArgumentParseException {
            if (!args.hasNext() && this.returnNow) {
                return LocalDateTime.now();
            }
            CommandArgs.Snapshot state = args.getSnapshot();
            String date = args.next();
            try {
                return LocalDateTime.parse(date);
            } catch (DateTimeParseException ex) {
                try {
                    return LocalDateTime.of(LocalDate.now(), LocalTime.parse(date));
                } catch (DateTimeParseException ex2) {
                    try {
                        return LocalDateTime.of(LocalDate.parse(date), LocalTime.MIDNIGHT);
                    } catch (DateTimeParseException ex3) {
                        if (this.returnNow) {
                            args.applySnapshot(state);
                            return LocalDateTime.now();
                        }
                        throw args.createError(("Invalid date-time!"));
                    }
                }
            }
        }

        @NotNull
        @Override
        public String getUsage(CommandSource src) {
            if (!this.returnNow) {
                return super.getUsage(src);
            } else {
                return ("[" + this.getKey() + "]");
            }
        }
    }

    private static class DurationElement extends CommandElement {

        protected DurationElement(String key) {
            super(key);
        }

        @Nullable
        @Override
        protected Object parseValue(@NotNull CommandSource source, CommandArgs args) throws ArgumentParseException {
            String s = args.next().toUpperCase();
            if (!s.contains("T")) {
                if (s.contains("D")) {
                    if (s.contains("H") || s.contains("M") || s.contains("S")) {
                        s = s.replace("D", "DT");
                    }
                } else {
                    if (s.startsWith("P")) {
                        s = "PT" + s.substring(1);
                    } else {
                        s = "T" + s;
                    }
                }
            }
            if (!s.startsWith("P")) {
                s = "P" + s;
            }
            try {
                return Duration.parse(s);
            } catch (DateTimeParseException ex) {
                throw args.createError(("Invalid duration!"));
            }
        }
    }

    private static class ColorElement extends CommandElement {
        protected ColorElement(String key) {
            super(key);
        }

        @Nullable
        @Override
        protected Object parseValue(@NotNull CommandSource source, @NotNull CommandArgs args) throws ArgumentParseException {

            try {
                return ColorFactory.valueOf(args.next());
            } catch (Exception e) {
                throw args.createError(e.getMessage());
            }
        }
    }

}
