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

import dev.kord.x.emoji.Emojis
import dev.schlaubi.forp.analyze.Conversation
import dev.schlaubi.forp.analyze.SourceFile
import dev.schlaubi.forp.analyze.events.Event
import dev.schlaubi.forp.analyze.events.JavaDocFoundEvent
import dev.schlaubi.forp.analyze.javadoc.DocumentedClassObject
import dev.schlaubi.forp.analyze.on
import dev.schlaubi.forp.fetch.input.Input.Companion.toInput
import dev.schlaubi.forp.fetch.input.Input.Companion.toPlainInput
import dev.schlaubi.forp.parser.stacktrace.RootStackTrace
import dev.schlaubi.forp.parser.stacktrace.StackTrace
import dev.schlaubi.forp.parser.stacktrace.StackTraceElement
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.time.withTimeout
import me.schlaubi.autohelp.help.EditableMessage
import me.schlaubi.autohelp.source.ReceivedMessage
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

private val memory = mutableListOf<DiscordConversation>()

internal suspend fun ReceivedMessage.handle(autoHelp: AutoHelpImpl) {
    if (authorId == null) return // ignore webhooks
    val (conversation, found) = memory.firstOrNull { (message) ->
        message.channelId == channelId
                && message.authorId == authorId
    }?.to(true) ?: newDiscordConversation(this, autoHelp, memory) to false

    content?.let { raw ->
        conversation.consumeNewInput(raw.toInput())
        conversation.consumeNewInput(raw.toPlainInput())
    }

    coroutineScope {
        files.forEach {
            launch {
                val download = it.download()
                conversation.consumeNewInput(download.toInput(it.type))
            }
        }
    }

    @Suppress("MagicNumber")
    if (found) {
        autoHelp.launch {
            withTimeout(Duration.ofSeconds(10)) {
                conversation.events.take(1).single() // wait
                react(Emojis.thumbsup)
            }
        }
    }
}

private suspend fun newDiscordConversation(
    message: ReceivedMessage,
    autoHelp: AutoHelpImpl,
    memory: MutableList<DiscordConversation>,
): DiscordConversation {
    val conversation = autoHelp.analyzer.createNewConversation()
    val discordConversation = DiscordConversation(message, conversation)
    val forgetfulness = autoHelp.launch {
        delay(autoHelp.cleanUpTime.inWholeMilliseconds)
        discordConversation.forget()
    }

    conversation.on<Event>(discordConversation) {
        discordConversation.handleEvent(this, autoHelp)
    }

    memory.add(discordConversation)
    discordConversation.onForget {
        memory.remove(discordConversation)
        forgetfulness.cancel()
    }
    return discordConversation
}

internal data class DiscordConversation(
    val message: ReceivedMessage,
    val delegate: Conversation,
) : Conversation by delegate, CoroutineScope {
    private val eventQueue: Queue<Event> = LinkedList()
    private var dead = false
    private var queueRunning = false
    private var onForget: (() -> Unit)? = null
    override val coroutineContext: CoroutineContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    var causeLine: String? = null
    var doc: DocumentedClassObject? = null
    var exception: StackTrace? = null
    var causeElement: StackTraceElement? = null
    var explanation: String? = null
    var renderDoc: Boolean = false

    var status: EditableMessage? = null

    val isComplete
        get() = exception != null && doc != null && causeLine != null

    val exceptions: MutableList<RootStackTrace> = mutableListOf()
    val files: MutableList<SourceFile> = mutableListOf()
    val docs: MutableList<JavaDocFoundEvent> = mutableListOf()

    suspend fun handleEvent(event: Event, autoHelp: AutoHelpImpl) {
        eventQueue.add(event)
        if (!queueRunning) {
            processEvents(autoHelp)
        }
    }

    fun onForget(block: () -> Unit) {
        onForget = block
    }

    private suspend fun processEvents(autoHelp: AutoHelpImpl) {
        queueRunning = true
        while (eventQueue.isNotEmpty()) {
            autoHelp.handleEvent(eventQueue.poll(), this)
        }
        queueRunning = false
    }


    override fun forget() {
        if (dead) return
        dead = true
        onForget?.invoke()
        delegate.forget()
    }
}
