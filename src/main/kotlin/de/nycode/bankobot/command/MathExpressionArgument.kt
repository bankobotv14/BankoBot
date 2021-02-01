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

import de.nycode.bankobot.variables.parsers.InvalidExpressionException
import de.nycode.bankobot.variables.parsers.calc.CalcExpression
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.x.commands.argument.Argument
import dev.kord.x.commands.argument.result.ArgumentResult

val MathExpressionArgument: Argument<CalcExpression, MessageCreateEvent> =
    InternalMathExpressionArgument()

internal class InternalMathExpressionArgument(override val name: String = "expression") :
    Argument<CalcExpression, MessageCreateEvent> {
    override suspend fun parse(
        text: String,
        fromIndex: Int,
        context: MessageCreateEvent
    ): ArgumentResult<CalcExpression> {
        val expression = CalcExpression(text.substring(fromIndex))
        return try {
            expression.getResult()
            ArgumentResult.Success(expression, text.length - fromIndex)
        } catch (exception: InvalidExpressionException) {
            ArgumentResult.Failure(exception.message ?: "Unknown error", exception.position)
        } catch (exception: Exception) {
            ArgumentResult.Failure(exception.message ?: "Unknown error", 0)
        }
    }
}
