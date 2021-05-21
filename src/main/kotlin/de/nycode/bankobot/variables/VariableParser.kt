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
