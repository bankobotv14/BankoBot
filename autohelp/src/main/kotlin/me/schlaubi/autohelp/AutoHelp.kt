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

package me.schlaubi.autohelp

import dev.schlaubi.forp.analyze.StackTraceAnalyzer
import me.schlaubi.autohelp.help.HtmlRenderer
import me.schlaubi.autohelp.help.MessageRenderer
import me.schlaubi.autohelp.tags.TagSupplier
import kotlin.time.Duration

/**
 * Autohelp instance.
 *
 * @see AutoHelpBuilder
 * @see autoHelp
 */
public interface AutoHelp {
    /**
     * [StackTraceAnalyzer] used to power analysis of exceptions.
     */
    public val analyzer: StackTraceAnalyzer

    /**
     * [TagSupplier] used to provide custom explanations.
     */
    public val tagSupplier: TagSupplier

    /**
     * [MessageRenderer] used to send messages.
     */
    public val messageRenderer: MessageRenderer

    /**
     * [HtmlRenderer] used to render javadocs.
     */
    public val htmlRenderer: HtmlRenderer

    /**
     * The [Duration] after which all conversation should be closed
     */
    public val cleanUpTime: Duration

    /**
     * Closes all resources needed by autohelp.
     */
    public suspend fun close()
}
