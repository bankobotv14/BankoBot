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

package de.nycode.bankobot.variables

import de.nycode.bankobot.variables.parsers.calc.CalcExpressionParser
import de.nycode.bankobot.variables.parsers.http.HttpExpressionParser

object VariableParser {

    private val expressionRegex = "(?<!\\\\)\\\$\\(([a-zA-Z0-9_-]+) ?(.+)?\\)".toRegex()
    private val expressions = listOf<ExpressionParser<*>>(CalcExpressionParser, HttpExpressionParser)

    /**
     * Parses an [Expression] out of a string matching the following form
     * $(<type> <optional arguments splitten by spaces>)
     * @return either the parsed expression or null if the [input] is not a valid expression
     * @param input the input to parse the expression form
     */
    @Suppress("UNCHECKED_CAST")
    fun <R> parseExpression(input: String): Expression<R>? {
        val result = expressionRegex.find(input)
            ?: return null

        val (command, arguments) = result.destructured

        val parser = expressions.find { it.isMatching(command) } ?: return null

        return parser.parseExpression(arguments) as Expression<R>
    }

    suspend fun String.replaceVariables(): String {
        val replacements = mutableMapOf<String, String>()
        expressionRegex.findAll(this)
            .forEach {
                val expression = parseExpression<Any>(it.value) ?: return@forEach
                replacements[it.value] = expression.getResult().toString()
            }
        var replacementString = this
        replacements.forEach { (key, value) ->
            replacementString = replacementString.replace(key, value)
        }
        return replacementString
    }
}
