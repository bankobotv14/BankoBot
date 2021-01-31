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

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class CalcExpressionTest {

    private suspend fun assertExpressionEquals(input: String, result: Int) {
        assertThat(CalcExpression(input).getResult()).isEqualTo(result)
    }

    @Test
    fun `Parse simple addition`() = runBlockingTest {
        assertExpressionEquals("1 + 2", 3)
    }

    @Test
    fun `Parse simple subtraction`() = runBlockingTest {
        assertExpressionEquals("2 - 1", 1)
    }

    @Test
    fun `Parse simple multiplication`() = runBlockingTest {
        assertExpressionEquals("2 * 2", 4)
    }

    @Test
    fun `Parse simple division`() = runBlockingTest {
        assertExpressionEquals("10 / 2", 5)
    }

    @Test
    fun `Point before Line Calculation`() = runBlockingTest {
        assertExpressionEquals("2 + 2 * 4", 10)
    }

    @Test
    fun `Calculation with Parentheses`() = runBlockingTest {
        assertExpressionEquals("2 * (4 + 5)", 18)
    }
}
