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

import de.nycode.bankobot.variables.parsers.calc.OldCalcExpression
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.ceil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
internal class CalcExpressionTest {

    private infix fun String.shouldBe(result: Number) {
        val double = result.toDouble()
        val bigDecimal = if (double != ceil(double)) {
            double.toBigDecimal()
        } else {
            result.toInt().toBigDecimal()
        }
        assertEquals(bigDecimal, OldCalcExpression(this).getResult())
    }

    @Test
    fun `Parse simple addition`() {
        "1 + 2" shouldBe 3
    }

    @Test
    fun `Parse simple subtraction`() {
        "2 - 1" shouldBe 1
    }

    @Test
    fun `Parse simple multiplication`() {
        "2 * 2" shouldBe 4
    }

    @Test
    fun `Parse simple division`() {
        "10 / 2" shouldBe 5
    }

    @Test
    fun `Point before Line Calculation`() {
        "2 + 2 * 4" shouldBe 10
    }

    @Test
    fun `Calculation with Parentheses`() {
        "2 * (4 + 5)" shouldBe 18
    }

    @Test
    fun `Square number`() {
        "2^3" shouldBe 8
    }

    @Test
    fun `Square number throws exception when to high`() {
        assertFailsWith<InvalidExpressionException>("1000000000 is higher than the max of 999999999") {
            OldCalcExpression("2^1000000000").getResult()
        }
    }
}
