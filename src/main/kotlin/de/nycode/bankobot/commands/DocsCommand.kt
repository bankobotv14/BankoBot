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

package de.nycode.bankobot.commands

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.description
import de.nycode.bankobot.docdex.*
import de.nycode.bankobot.utils.EmbedCreator
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.editEmbed
import de.nycode.bankobot.utils.Embeds.respondEmbed
import de.nycode.bankobot.utils.limit
import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.argument.text.WordArgument
import dev.kord.x.commands.kord.model.command.KordCommandBuilder
import dev.kord.x.commands.kord.model.context.KordCommandEvent
import dev.kord.x.commands.model.command.invoke
import dev.kord.x.commands.model.module.CommandSet

@Suppress("FunctionName")
fun AbstractDocsCommand(displayName: String, name: String, doc: String, builder: KordCommandBuilder.() -> Unit): CommandSet = command(name) {
    builder(this)
    description("Zeigt das $displayName Javadoc in Discord an")
    invoke(ReferenceArgument.named("query")) { query ->
        docs(doc, query)
    }
}

@AutoWired
@ModuleName(GeneralModule)
fun docsCommand() = command("docs") {
    description("Zeigt Javadoc in Discord an")
    alias("doc")

    invoke(
        WordArgument.named("javadoc-name"),
        ReferenceArgument.named("query")
    ) { doc, query ->
        val available = DocDex.allJavadocs().map { it.names }.flatten()
        if (doc !in available) {
            respondEmbed(
                Embeds.error(
                    "Unbekannte docs!",
                    "Diese Docs kenn ich leider nicht."
                )
            )
            return@invoke
        }
        docs(doc, query)
    }
}

suspend fun KordCommandEvent.docs(doc: String, reference: Reference) {
    val status = respondEmbed(
        Embeds.loading(
            "Bitte warten!",
            "Das Javadoc wird gesucht."
        )
    )

    val docElements = DocDex.search(doc, reference.toDocDexQuery())

    if (docElements.isEmpty()) {
        status.editEmbed(
            Embeds.error(
                "Nichts gefunden!",
                "Ich konnte leider kein doc für `${reference.raw}` finden."
            )
        )
        return
    }

    val mostSuitableDoc = DocsGoogle.findMostSuitable(docElements, reference)
    respond(status, mostSuitableDoc)
}

private suspend fun respond(status: Message, doc: DocumentedElement) {
    val embed = when (val obj = doc.`object`) {
        is DocumentedClassObject -> renderClass(obj)
        is DocumentedMethodObject -> renderMethod(obj)
    }

    status.editEmbed(embed)
}

@Suppress("TooLongLine")
private fun formatClassName(doc: DocumentedClassObject): String =
    "${
        doc.annotations.joinToString(
            " ",
            postfix = "\n"
        ) { "@${it.className}" }
    }${doc.modifiers.joinToString(" ")} ${doc.type.name.toLowerCase()} ${doc.`package`}.${doc.name}"

private fun renderClass(doc: DocumentedClassObject): EmbedBuilder = Embeds.doc(doc) {
    val meta = doc.metadata
    title = formatClassName(doc)

    if (meta.extensions.isNotEmpty()) {
        field {
            name = "Extends"
            value = meta.extensions.format()
        }
    }

    if (meta.allImplementations.isNotEmpty()) {
        field {
            name = "Implements"
            value = meta.allImplementations.format()
        }
    }

    if (meta.implementingClasses.isNotEmpty()) {
        field {
            name = "All known implementing Classes"
            value = meta.implementingClasses.format()
        }
    }

    if (doc.type == DocumentedObject.Type.ENUM) {
        field {
            name = "fields"
            value = meta.fields.format()
        }
    }
}

private fun formatMethodName(doc: DocumentedMethodObject): String {
    val meta = doc.metadata
    return "${
        doc.annotations.joinToString(
            " ",
            postfix = "\n"
        ) { "@${it.className}" }
    } ${doc.modifiers.joinToString(" ")} ${doc.metadata.returns} ${doc.name}(${
        meta.parameters.joinToString {
            "${
                it.annotations.joinToString(
                    " ",
                    postfix = " "
                )
            }${it.type} ${it.name}"
        }
    })"
}

private fun String?.asDescriber() = if (isNullOrBlank()) "" else " - $this"

private fun renderMethod(doc: DocumentedMethodObject): EmbedBuilder = Embeds.doc(doc) {
    val meta = doc.metadata
    title = formatMethodName(doc)

    field {
        name = "Returns"
        value =
            "${meta.returns}${meta.returnsDescription.asDescriber()}"
    }

    if (meta.parameters.isNotEmpty()) {
        field {
            name = "Parameters"
            value =
                meta.parameters.joinToString("\n")
                {
                    "${
                        it.annotations.joinToString(
                            " ",
                            postfix = " "
                        )
                    }${it.type} ${it.name}${meta.parameterDescriptions[it.name].asDescriber()}"
                }
        }
    }

    if (meta.throws.isNotEmpty()) {
        field {
            name = "Throws"
            value =
                meta.throws.joinToString("\n") { "**${it.exception}**${it.description.asDescriber()}" }
        }
    }
}

private fun List<DocumentedReference>.format() = joinToString("`, `", "`", "`") {
    when (it) {
        is DocumentedReference.Class -> "${it.`package`}${it.className}"
        is DocumentedReference.Method -> "${it.className}#${it.methodName}"
        is DocumentedReference.Field -> "${it.className}.${it.fieldName}"
    }
}.limit(EMBED_DESCRIPTION_MAX_LENGTH)

@Suppress("unused")
private fun Embeds.doc(doc: DocumentedObject, creator: EmbedCreator): EmbedBuilder {
    return EmbedBuilder().apply(creator).apply {
        url = doc.link
        description = htmlRenderer.convert(doc.description.limit(EMBED_DESCRIPTION_MAX_LENGTH))

        if (doc.deprecated) {
            field {
                name = "Deprecated"
                value = doc.deprecationMessage
            }
        }
    }
}

private val htmlRenderer = FlexmarkHtmlConverter.builder().build()

private const val EMBED_DESCRIPTION_MAX_LENGTH = 2048

