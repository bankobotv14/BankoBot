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

package de.nycode.bankobot.variables.parsers.calc

import de.nycode.bankobot.variables.CalculationBaseVisitor
import de.nycode.bankobot.variables.CalculationParser
import de.nycode.bankobot.variables.parsers.InvalidExpressionException
import java.math.BigDecimal

const val MAXIMUM_POW_VALUE = 999999999

class CalcExpressionVisitor : CalculationBaseVisitor<BigDecimal>() {

    override fun visitDivide(ctx: CalculationParser.DivideContext): BigDecimal {
        return visit(ctx.left) / visit(ctx.right)
    }

    override fun visitNumber(ctx: CalculationParser.NumberContext): BigDecimal {
        return ctx.NUMBER().symbol.text.toBigDecimal()
    }

    override fun visitMultiply(ctx: CalculationParser.MultiplyContext): BigDecimal {
        return visit(ctx.left) * visit(ctx.right)
    }

    override fun visitPlus(ctx: CalculationParser.PlusContext): BigDecimal {
        return visit(ctx.left) + visit(ctx.right)
    }

    override fun visitMinus(ctx: CalculationParser.MinusContext): BigDecimal {
        return visit(ctx.left) - visit(ctx.right)
    }

    override fun visitParentheses(ctx: CalculationParser.ParenthesesContext): BigDecimal {
        return visit(ctx.expression())
    }

    override fun visitSquared(ctx: CalculationParser.SquaredContext): BigDecimal {
        val left = visit(ctx.left)
        val right = visit(ctx.right)

        if (right > MAXIMUM_POW_VALUE.toBigDecimal()) {
            throw InvalidExpressionException("$right is higher than the max of 999999999", ctx.right.ruleIndex)
        }

        return left.pow(right.intValueExact())
    }
}
