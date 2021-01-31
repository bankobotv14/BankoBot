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

package de.nycode.bankobot.variables.parsers

import de.nycode.bankobot.variables.CalculationLexer
import de.nycode.bankobot.variables.CalculationParser
import de.nycode.bankobot.variables.Expression
import org.antlr.v4.runtime.*

class CalcExpression(val input: String) : Expression<Int>() {

    private var result: Int? = null

    override fun getResult(): Int {
        if (result != null)
            return result as Int

        val input = CharStreams.fromString(input)
        val lexer = CalculationLexer(input).apply {
            removeErrorListeners()
            addErrorListener(ThrowingErrorListener)
        }

        val tokens = CommonTokenStream(lexer)
        val parser = CalculationParser(tokens)
        val tree = parser.root()
        result = CalcExpressionVisitor().visit(tree)
        return result as Int
    }
}

internal object ThrowingErrorListener : BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        throw InvalidExpressionException(msg, charPositionInLine)
    }
}
