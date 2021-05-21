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

import de.nycode.bankobot.utils.CODEBLOCK_REGEX
import de.nycode.bankobot.utils.CodeBlock
import de.nycode.bankobot.utils.asNullable
import dev.kord.x.commands.argument.Argument
import dev.kord.x.commands.argument.result.ArgumentResult

/**
 * Markdown codeblock argument.
 */
val CodeblockArgument: Argument<CodeBlock, Any?> =
    InternalCodeblockArgument(checkForLanguage = true)

/**
 * Creates a new [CodeBlock] argument with [name].
 * @param checkForLanguage whether a language is required for the markdown codeblock or not
 */
@Suppress("FunctionName")
fun CodeblockArgument(name: String = "code", checkForLanguage: Boolean): Argument<CodeBlock, Any?> =
    InternalCodeblockArgument(name, checkForLanguage)

private class InternalCodeblockArgument(
    override val name: String = "code",
    private val checkForLanguage: Boolean,
) :
    Argument<CodeBlock, Any?> {
    override suspend fun parse(
        text: String,
        fromIndex: Int,
        context: Any?,
    ): ArgumentResult<CodeBlock> {
        val input = text.substring(fromIndex)
        val codeBlockMatch =
            CODEBLOCK_REGEX.find(input) ?: return ArgumentResult.Failure("Expected codeblock",
                fromIndex)
        val (_, languageRaw, code) = codeBlockMatch.groupValues
        val language = languageRaw.asNullable()
        if (language == null && checkForLanguage) {
            return ArgumentResult.Failure("Expected codeblock with language",
                fromIndex)
        }

        return ArgumentResult.Success(CodeBlock(language, code),
            fromIndex + codeBlockMatch.range.last)
    }
}
