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

package me.schlaubi.autohelp.kord

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import me.schlaubi.autohelp.AutoHelp
import me.schlaubi.autohelp.help.MessageRenderer
import me.schlaubi.autohelp.source.EventSource
import me.schlaubi.autohelp.AutoHelpBuilder
import me.schlaubi.autohelp.ContextBuilder

public fun Kord.autoHelp(block: AutoHelpBuilder.() -> Unit = {}): AutoHelp = me.schlaubi.autohelp.autoHelp {
    useKordMessageRenderer(this@autoHelp)
    kordContext {
        kordEventSource(this@autoHelp)
    }

    block()
}

/**
 * Adds a [MessageRenderer] which uses [kord] to send messages.
 */
public fun AutoHelpBuilder.useKordMessageRenderer(kord: Kord) {
    messageRenderer = KordMessageRenderer(kord)
}

/**
 * Adds an [EventSource] which listens for [MessageCreateEvent]s.
 *
 * @param kord the [Kord] instance providing the event.
 */
public fun ContextBuilder<KordReceivedMessage>.kordEventSource(kord: Kord): Unit = +KordEventSource(kord)
public fun ContextBuilder<KordUpdateMessage>.kordEditEventSource(kord: Kord): Unit = +KordUpdateEventSource(kord)

/**
 * Builds a new Kord based context.
 */
public fun AutoHelpBuilder.kordContext(builder: ContextBuilder<KordReceivedMessage>.() -> Unit): Unit = context(builder)
