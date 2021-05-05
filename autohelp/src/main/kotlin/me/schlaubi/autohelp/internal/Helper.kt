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

import dev.schlaubi.forp.analyze.events.Event
import dev.schlaubi.forp.analyze.events.ExceptionFoundEvent
import dev.schlaubi.forp.analyze.events.JavaDocFoundEvent
import dev.schlaubi.forp.analyze.events.SourceFileFoundEvent
import dev.schlaubi.forp.parser.stacktrace.DefaultStackTraceElement
import dev.schlaubi.forp.parser.stacktrace.StackTraceElement
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.schlaubi.autohelp.help.OutgoingMessage
import me.schlaubi.autohelp.help.toEmbed
import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.seconds

private val LOG = KotlinLogging.logger { }

@Suppress("SuspendFunctionOnCoroutineScope")
internal suspend fun AutoHelpImpl.handleEvent(event: Event, conversation: DiscordConversation) {

    when (event) {
        is ExceptionFoundEvent -> conversation.exceptions += event.exception
        is JavaDocFoundEvent -> conversation.docs += event
        is SourceFileFoundEvent -> conversation.files += event.file
    }

    if (conversation.exceptions.isEmpty()) return

    val (exception, tag) = conversation.exceptions
        .asSequence()
        .flatMap { root ->
            val children = root.children.map { ParentAwareChildren(it, root) }
            (children + root).asSequence()
        }
        .mapNotNull { exception ->
            val tag = tagSupplier.findTagForException(exception)
            tag?.let { exception to tag }
        }.firstOrNull() ?: conversation.exceptions.first() to null

    if (exception.exception != conversation.exception?.exception) {
        conversation.renderDoc = true

        // This should run async from that to prevent a lock
        @Suppress("SuspendFunctionOnCoroutineScope")
        launch {
            delay(Duration.seconds(2))
            if (conversation.doc != null) {
                conversation.renderDoc = false
            }
        }
    }

    conversation.exception = exception
    conversation.causeElement = exception.elements.firstByUser()
    conversation.explanation = tag

    conversation.doc = conversation.docs.firstOrNull {
        it.exceptionName == exception.exception
    }?.doc

    if (conversation.doc != null) {
        conversation.renderDoc = true
    }
    val causeElement = conversation.causeElement as? DefaultStackTraceElement
    val causeSource = causeElement?.source as? DefaultStackTraceElement.FileSource
    if (causeSource != null) {
        conversation.causeLine = conversation.files.firstOrNull {
            it.name == causeSource.fileName.substringBeforeLast('.')
        }?.content?.lineSequence()?.drop(causeSource.lineNumber - 1)?.first()
    }

    val newMessage = OutgoingMessage(null, conversation.toEmbed(htmlRenderer, loadingEmote))
    if (conversation.status == null) {
        val message = conversation.message
        conversation.status = messageRenderer.sendMessage(
            message.guildId,
            message.channelId,
            newMessage
        )
    } else {
        conversation.status?.edit(newMessage)
    }

    if (conversation.isComplete) {
        LOG.debug { "Forgetting about $conversation" }
        conversation.forget()
    }
}

private fun List<StackTraceElement>.firstByUser(): StackTraceElement? = firstOrNull {
    it is DefaultStackTraceElement && (it.method.clazz.packagePath?.matches(KNOWN_PACKAGES)?.not() ?: true)
}

private val KNOWN_PACKAGES: Regex = listOf(
    "java", "javax", "javafx", "sun", // Java
    "com.google", "org.apache", "com.jetbrains", "org.jetbrains", "okhttp3", // publishers of big libs
    "net.minecraft", "org.bukkit", "org.spigotmc", "net.md_5", "co.aikar", "com.destroystokyo", // mc servers
    "net.milkbowl", "me.clip", "org.sk89q", "com.onarandombox" // known libraries
).joinToString(prefix = "(?:", separator = "|", postfix = ").*").toRegex()
