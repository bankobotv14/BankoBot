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
