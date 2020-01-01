/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.atharion.commons;

import com.atharion.commons.command.argument.ArgumentParserRegistry;
import com.atharion.commons.command.argument.SimpleParserRegistry;
import com.atharion.commons.command.functional.FunctionalCommandBuilder;
import com.atharion.commons.concurrent.promise.Promise;
import com.atharion.commons.utils.function.Numbers;
import com.atharion.commons.utils.player.Players;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

/**
 * A functional command handling utility.
*/
public final class Commands {

    // Global argument parsers
    private static final ArgumentParserRegistry PARSER_REGISTRY;

    @Nonnull
    public static ArgumentParserRegistry parserRegistry() {
        return PARSER_REGISTRY;
    }

    static {
        PARSER_REGISTRY = new SimpleParserRegistry();

        // setup default argument parsers
        PARSER_REGISTRY.register(String.class, Optional::of);
        PARSER_REGISTRY.register(Number.class, Numbers::parse);
        PARSER_REGISTRY.register(Integer.class, Numbers::parseIntegerOpt);
        PARSER_REGISTRY.register(Long.class, Numbers::parseLongOpt);
        PARSER_REGISTRY.register(Float.class, Numbers::parseFloatOpt);
        PARSER_REGISTRY.register(Double.class, Numbers::parseDoubleOpt);
        PARSER_REGISTRY.register(Byte.class, Numbers::parseByteOpt);
        PARSER_REGISTRY.register(Boolean.class, s -> s.equalsIgnoreCase("true") ? Optional.of(true) : s.equalsIgnoreCase("false") ? Optional.of(false) : Optional.empty());
        PARSER_REGISTRY.register(UUID.class, s -> {
            try {
                return Optional.of(UUID.fromString(s));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        });
        PARSER_REGISTRY.register(Player.class, s -> {
            try {
                return Players.get(UUID.fromString(s));
            } catch (IllegalArgumentException e) {
                return Players.get(s);
            }
        });
        PARSER_REGISTRY.register(Promise.class, s -> {
            try {
                return Optional.of(Promise.start());
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        });
        PARSER_REGISTRY.register(World.class, Atharion::world);
    }

    /**
     * Creates and returns a new command builder
     *
     * @return a command builder
     */
    public static FunctionalCommandBuilder<CommandSender> create() {
        return FunctionalCommandBuilder.newBuilder();
    }

    private Commands() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}