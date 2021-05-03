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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import me.schlaubi.autohelp.help.HtmlRenderer
import me.schlaubi.autohelp.help.MessageRenderer
import me.schlaubi.autohelp.internal.AutoHelpImpl
import me.schlaubi.autohelp.source.EventContext
import me.schlaubi.autohelp.source.EventFilter
import me.schlaubi.autohelp.source.EventSource
import me.schlaubi.autohelp.source.ReceivedMessage
import me.schlaubi.autohelp.tags.TagSupplier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty0
import kotlin.time.Duration
import kotlin.time.minutes

/**
 * Creates a new [AutoHelp] instance and applies [builder] to it.
 */
@OptIn(ExperimentalContracts::class)
public inline fun autoHelp(builder: AutoHelpBuilder.() -> Unit = {}): AutoHelp {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }
    return AutoHelpBuilder().apply(builder).build()
}

/**
 * Buildser for [AutoHelp].
 *
 * @property analyzer the [StackTraceAnalyzer] to analyze exceptions
 * @property tagSupplier the [TagSupplier] for custom answers
 * @property messageRenderer the [MessageRenderer] to send messages
 * @property htmlRenderer the [HtmlRenderer] to render documentation descriptions
 * @property cleanupTime the [Duration] after which conversations should be forgotten automatically
 * @property contexts a list of all [EventContext]s supplying events to analyze
 * @property dispatcher the [CoroutineContext] used for I/O
 */
public class AutoHelpBuilder {

    public lateinit var analyzer: StackTraceAnalyzer
    public lateinit var tagSupplier: TagSupplier
    public lateinit var messageRenderer: MessageRenderer
    public lateinit var htmlRenderer: HtmlRenderer
    public lateinit var loadingEmote: String
    public var cleanupTime: Duration = 2.minutes
    public var contexts: MutableList<EventContext<*>> = mutableListOf()
    public var dispatcher: CoroutineContext = Dispatchers.IO + Job()

    /**
     * Set's the [tagSupplier] to [supplier].
     *
     * ```kotlin
     * tagSupplier {
     *  // tag finding logic
     * }
     * ```
     */
    public fun tagSupplier(supplier: TagSupplier) {
        this.tagSupplier = supplier
    }

    /**
     * Set's the [htmlRenderer] to [htmlRenderer].
     *
     * ```kotlin
     * htmlRenderer {
     *  // conversion logic
     * }
     * ```
     */
    public fun htmlRenderer(htmlRenderer: HtmlRenderer) {
        this.htmlRenderer = htmlRenderer
    }

    /**
     * Creates a new context of type [T] and applies [builder].
     */
    @OptIn(ExperimentalContracts::class)
    public fun <T : ReceivedMessage> context(builder: ContextBuilder<T>.() -> Unit) {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }

        contexts.add(ContextBuilder<T>().apply(builder).build(dispatcher))
    }


    @PublishedApi
    internal fun build(): AutoHelp {
        require(::analyzer.isInitialized) { "analyzer may not be null" }
        val tags: KProperty0<*> = ::tagSupplier
        require(tags.get() != null) { "tagSupplier may not be null" }
        require(::messageRenderer.isInitialized) { "messageRenderer may not be null" }
        val html: KProperty0<*> = ::htmlRenderer
        require(html.get() != null) { "htmlRenderer may not be null" }
        require(::loadingEmote.isInitialized) { "loadingEmote may not be null" }
        return AutoHelpImpl(
            analyzer,
            contexts,
            dispatcher,
            tagSupplier,
            cleanupTime,
            messageRenderer,
            htmlRenderer,
            loadingEmote
        )
    }
}

/**
 * Builder for an [EventContext].
 */
public class ContextBuilder<T : ReceivedMessage> {
    public val sources: MutableList<EventSource<out T>> = mutableListOf()
    public val filters: MutableList<EventFilter<in T>> = mutableListOf()

    /**
     * Adds this [EventFilter] to [filters].
     */
    public operator fun EventFilter<in T>.unaryPlus() {
        filters += this
    }

    /**
     * Adds this [EventSource] to [sources].
     */
    public operator fun EventSource<out T>.unaryPlus() {
        sources += this
    }


    /**
     * Adds a new [eventFilter] to this context.
     */
    public fun filter(eventFilter: EventFilter<in T>): Unit = +eventFilter

    internal fun build(dispatcher: CoroutineContext): EventContext<T> = EventContext(sources, filters, dispatcher)
}