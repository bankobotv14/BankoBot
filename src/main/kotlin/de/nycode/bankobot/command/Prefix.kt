/*
 * MIT License
 *
 * Copyright (c) 2021 BankoBot Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.nycode.bankobot.command

import dev.kord.x.commands.model.prefix.PrefixBuilder
import dev.kord.x.commands.model.prefix.PrefixRule

/**
 * [PrefixRule] which requires the literal string [prefix] to be present in front of the command.
 * This matches against the prefix being at the beginning of the line and ignores an unlimited amount
 * of spaces between the prefix and the command. (Regex "^$prefix\s*")
 */
@Suppress("unused") // it is supposed to be only invoked on PrefixBuilder even if PrefixBuilder is unused
fun PrefixBuilder.literal(prefix: String): PrefixRule<Any?> =
    LiteralPrefixRule(prefix)

private class LiteralPrefixRule(prefix: String) : PrefixRule<Any?> {
    private val regex = "^$prefix\\s*".toRegex()

    override suspend fun consume(message: String, context: Any?): PrefixRule.Result {
        val match = regex.find(message) ?: return PrefixRule.Result.Denied
        return PrefixRule.Result.Accepted(match.value)
    }
}
