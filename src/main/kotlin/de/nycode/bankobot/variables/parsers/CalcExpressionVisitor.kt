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

import de.nycode.bankobot.variables.CalculationBaseVisitor
import de.nycode.bankobot.variables.CalculationParser

class CalcExpressionVisitor : CalculationBaseVisitor<Int>() {
    override fun visitDivide(ctx: CalculationParser.DivideContext): Int {
        return visit(ctx.left) / visit(ctx.right)
    }

    override fun visitNumber(ctx: CalculationParser.NumberContext): Int {
        return ctx.NUMBER().symbol.text.toInt()
    }

    override fun visitMultiply(ctx: CalculationParser.MultiplyContext): Int {
        return visit(ctx.left) * visit(ctx.right)
    }

    override fun visitPlus(ctx: CalculationParser.PlusContext): Int {
        return visit(ctx.left) + visit(ctx.right)
    }

    override fun visitMinus(ctx: CalculationParser.MinusContext): Int {
        return visit(ctx.left) - visit(ctx.right)
    }

    override fun visitParentheses(ctx: CalculationParser.ParenthesesContext): Int {
        return visit(ctx.expression())
    }
}
