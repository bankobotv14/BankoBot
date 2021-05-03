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

package me.schlaubi.autohelp.internal

import dev.schlaubi.forp.parser.stacktrace.CausedStackTrace
import dev.schlaubi.forp.parser.stacktrace.RootStackTrace
import dev.schlaubi.forp.parser.stacktrace.StackTrace

internal class ParentAwareChildren(
    private val delegate: CausedStackTrace,
    private val root: RootStackTrace
) : CausedStackTrace by delegate {
    override val parent: StackTrace
        get() = root.findParentOf(this) ?: this

    // this makes the indexOf() check work
    override fun equals(other: Any?): Boolean {
        // It is sus but there is no way around it
        @Suppress("SuspiciousEqualsCombination")
        return this === other || delegate == other
    }

    override fun hashCode(): Int = delegate.hashCode()
}

internal fun RootStackTrace.findParentOf(child: StackTrace): StackTrace? =
    when (val me = children.indexOf(child)) {
        0 -> this
        -1 -> null
        else -> children.getOrNull(me - 1)
    }
