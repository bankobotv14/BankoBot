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

package me.schlaubi.autohelp.help

import dev.schlaubi.forp.parser.stacktrace.CausedStackTrace
import dev.schlaubi.forp.parser.stacktrace.DefaultStackTraceElement
import dev.schlaubi.forp.parser.stacktrace.StackTrace
import me.schlaubi.autohelp.AutoHelpVersion
import me.schlaubi.autohelp.internal.DiscordConversation

/**
 * Representation of an AutoHelp message that can be sent using a [MessageRenderer].
 *
 * @property content the content of the message if set
 * @property embed the [Embed] of the message if set
 *
 * @see Embed
 */
public class OutgoingMessage(public val content: String?, public val embed: Embed?)

/**
 * Representation of a Discord Embed.
 *
 * @see OutgoingMessage
 */
public data class Embed(
    public val title: String,
    public val description: String?,
    public val fields: List<Field>,
    public val footer: Footer?,
) {
    public data class Field(public val title: String, public val value: String, public val inline: Boolean)
    public data class Footer(public val name: String, public val url: String?, public val avatar: String?)
}

@OptIn(ExperimentalStdlibApi::class)
internal fun DiscordConversation.toEmbed(htmlRenderer: HtmlRenderer): Embed {
    val fields = buildList {
        val stackTrace = exception
        if (stackTrace != null) {
            add(Embed.Field("Exception", stackTrace.exceptionName, false))
            val exceptionMessage = stackTrace.message
            if (!exceptionMessage.isNullOrBlank() && exceptionMessage != "null") {
                add(Embed.Field("Beschreibung", exceptionMessage, false))
            }
            val documentation = doc
            if (doc != null) {
                val description = with(htmlRenderer) {
                    documentation?.description?.toMarkdown()
                }
                add(Embed.Field("Exception Doc", description?.ifBlank { null } ?: "<loading>", false))
            }
            val causeElement = causeElement
            if (causeElement != null
                && causeElement is DefaultStackTraceElement
                && causeElement.source is DefaultStackTraceElement.FileSource
            ) {
                val source = causeElement.source as DefaultStackTraceElement.FileSource
                add(
                    Embed.Field(
                        "Ursache",
                        "Der Fehler befindet sich vermutlich in der Datei" +
                                " `${source.fileName}` in Zeile `${source.lineNumber}`",
                        false
                    )
                )
            }

            val causee = (stackTrace as? CausedStackTrace)?.parent
            if (causee != null) {
                add(Embed.Field("Verursacht", causee.exceptionName, false))
            }
        }

        val source = causeLine
        if (source != null) {
            add(
                Embed.Field(
                    "Ursache - Code",
                    """```java
                        |$source
                        |```""".trimMargin(),
                    false
                )
            )
        } else {
            add(
                Embed.Field(
                    "Ursache - Code",
                    "Ich konnte keinen Code finden, bitte schicke die komplette Klasse, in der der Fehler auftritt (Am besten via hastebin)",
                    false
                )
            )
        }

    }

    val footer = Embed.Footer(
        "AutoHelp ${AutoHelpVersion.VERSION} (${AutoHelpVersion.COMMIT_HASH})" +
                " - Bitte bugs auf https://github.com/DRSchlaubi/furry-octo-rotary-phone melden",
        "https://github.com/DRSchlaubi/furry-octo-rotary-phone",
        null
    )
    val exceptionName = exception?.exception?.let { it.innerClassName ?: it.className } ?: "<unknown exception>"
    val title = "AutoHelp - $exceptionName"


    return Embed(title, explanation, fields, footer)
}

private val StackTrace.exceptionName
    get() = exception.innerClassName ?: exception.className
