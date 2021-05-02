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

import dev.schlaubi.forp.analyze.StackTraceAnalyzer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.schlaubi.autohelp.AutoHelp
import me.schlaubi.autohelp.help.HtmlRenderer
import me.schlaubi.autohelp.help.MessageRenderer
import me.schlaubi.autohelp.source.EventContext
import me.schlaubi.autohelp.tags.TagSupplier
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

internal class AutoHelpImpl(
    override val analyzer: StackTraceAnalyzer, contexts: List<EventContext<*>>,
    override val coroutineContext: CoroutineContext,
    override val tagSupplier: TagSupplier,
    override val cleanUpTime: Duration,
    override val messageRenderer: MessageRenderer,
    override val htmlRenderer: HtmlRenderer,
) : AutoHelp, CoroutineScope {

    private val listeners = contexts.map {
        it.events.onEach { message ->
            message.handle(this)
        }.launchIn(this)
    }


    override suspend fun close() {
        coroutineScope {
            listeners.forEach {
                launch { it.cancel() }
            }
        }
    }
}
