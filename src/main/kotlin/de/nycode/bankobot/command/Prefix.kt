/*
 *     This file is part of the BankoBot Project.
 *     Copyright (C) 2021  BankoBot Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Also add information on how to contact you by electronic and paper mail.
 *
 *   If your software can interact with users remotely through a computer
 * network, you should also make sure that it provides a way for users to
 * get its source.  For example, if your program is a web application, its
 * interface could display a "Source" link that leads users to an archive
 * of the code.  There are many ways you could offer source, and different
 * solutions will be better for different programs; see section 13 for the
 * specific requirements.
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU AGPL, see
 * <https://www.gnu.org/licenses/>.
 *
 */

package de.nycode.bankobot.command

import dev.kord.core.event.message.MessageCreateEvent
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
    private val regex = "^$prefix\\s*".toRegex(RegexOption.IGNORE_CASE)

    override suspend fun consume(message: String, context: Any?): PrefixRule.Result {
        val match = regex.find(message) ?: return PrefixRule.Result.Denied
        return PrefixRule.Result.Accepted(match.value)
    }
}

inline fun PrefixBuilder.bankoBot(supplier: () -> PrefixRule<MessageCreateEvent>) =
    add(BankoBotContext, supplier())
