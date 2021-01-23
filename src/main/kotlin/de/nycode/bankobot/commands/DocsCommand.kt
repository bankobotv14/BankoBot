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

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.description
import de.nycode.bankobot.docdex.*
import de.nycode.bankobot.utils.*
import de.nycode.bankobot.utils.Embeds.editEmbed
import de.nycode.bankobot.utils.Embeds.respondEmbed
import dev.kord.core.behavior.MessageBehavior
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.argument.text.WordArgument
import dev.kord.x.commands.kord.model.context.KordCommandEvent
import dev.kord.x.commands.model.command.invoke

@Suppress("TopLevelPropertyNaming")
const val DocsModule = "Documentation"

@AutoWired
@ModuleName(DocsModule)
fun allDocsCommand() = command("alldocs") {
    description("Zeigt eine Lister alle unterstützten Javadocs")
    alias("all-docs", "list-docs", "listdocs", "availabledocs", "available-docs")

    invoke {
        respondEmbed(Embeds.info(
            "Verfügbare Dokumentationen!",
            BankoBot.availableDocs.format()
        ))
    }
}

@AutoWired
@ModuleName(DocsModule)
fun docsCommand() = command("docs") {
    description("Zeigt Javadoc in Discord an")
    alias("doc", "d")

    invoke(
        WordArgument.named("javadoc-name"),
        ReferenceArgument.named("query")
    ) { doc, query ->
        if (doc !in BankoBot.availableDocs) {
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
    doExpensiveTask(statusDescription = "Das Javadoc wird gesucht.") {
        val docElements = DocDex.search(doc, reference.toDocDexQuery())

        if (docElements.isEmpty()) {
            editEmbed(
                Embeds.error(
                    "Nichts gefunden!",
                    "Ich konnte leider kein doc für `${reference.raw}` finden."
                )
            )
            return@doExpensiveTask
        }

        val mostSuitableDoc = DocsGoogle.findMostSuitable(docElements, reference)
        respond(mostSuitableDoc)
    }
}

private suspend fun MessageBehavior.respond(doc: DocumentedElement) {
    val embed = when (val obj = doc.`object`) {
        is DocumentedClassObject -> renderClass(obj)
        is DocumentedMethodObject -> renderMethod(obj)
    }

    editEmbed(embed)
}

private fun formatDocDefinition(name: String) = """```java
    |$name
    |```
""".trimMargin().limit(EMBED_TITLE_MAX_LENGTH)

@Suppress("TooLongLine")
private fun formatClassDefinition(doc: DocumentedClassObject): String =
    "${
        doc.annotations.joinToString(
            " ",
            postfix = "\n"
        ) { "@${it.className}" }
    }${doc.modifiers.joinToString(" ")} ${doc.type.name.toLowerCase()} ${doc.name} ${
        if (doc.metadata.extensions.isNotEmpty()) {
            "extends ${doc.metadata.extensions.first().className} "
        } else {
            ""
        }
    }${
        if (doc.metadata.implementations.isNotEmpty()) {
            "implements ${
                doc.metadata.implementations.joinToString(", ") { it.className }
            }"
        } else ""
    }"

private fun formatClassName(doc: DocumentedObject) = "${doc.`package`}.${doc.name}"

private fun renderClass(doc: DocumentedClassObject): EmbedBuilder = Embeds.doc(doc) {
    val meta = doc.metadata
    definition(formatClassDefinition(doc))
    title = formatClassName(doc)

    if (meta.extensions.isNotEmpty()) {
        field {
            name = "Extends"
            value = meta.extensions.formatReferences()
        }
    }

    if (meta.allImplementations.isNotEmpty()) {
        field {
            name = "Implements"
            value = meta.allImplementations.formatReferences()
        }
    }

    if (meta.implementingClasses.isNotEmpty()) {
        field {
            name = "All known implementing Classes"
            value = meta.implementingClasses.formatReferences()
        }
    }

    if (doc.type == DocumentedObject.Type.ENUM) {
        field {
            name = "fields"
            value = meta.fields.formatReferences()
        }
    }
}

@Suppress("MaxLineLength")
private fun formatMethodName(doc: DocumentedMethodObject) =
    "${doc.`package`}.${doc.metadata.owner}#${doc.name}(${doc.metadata.parameters.joinToString { "${it.type} ${it.name}" }})"

private fun formatMethodDefinition(doc: DocumentedMethodObject): String {
    val meta = doc.metadata
    return "${
        doc.annotations.joinToString(
            " ",
            postfix = "\n"
        ) { "@${it.className}" }
    }${doc.modifiers.joinToString(" ")} ${doc.metadata.returns} ${doc.name}(${
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
    definition(formatMethodDefinition(doc))

    field {
        name = "Returns"
        value =
            "${meta.returns}${meta.returnsDescription.asDescriber()}"
    }

    if (meta.parameters.isNotEmpty()) {
        field {
            name = "Parameters"
            value =
                meta.parameters.joinToString("\n") {
                    "${it.type} ${it.name}${meta.parameterDescriptions[it.name].asDescriber()}"
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

private fun List<DocumentedReference>.formatReferences() = format {
    when (it) {
        is DocumentedReference.Class -> "${it.`package`}${it.className}"
        is DocumentedReference.Method -> "${it.className}#${it.methodName}"
        is DocumentedReference.Field -> "${it.className}.${it.fieldName}"
    }
}.limit(EMBED_DESCRIPTION_MAX_LENGTH)

private fun EmbedBuilder.definition(definition: String) = field {
    name = "Definition"
    value = formatDocDefinition(definition)
}

@Suppress("unused")
private fun Embeds.doc(doc: DocumentedObject, creator: EmbedCreator): EmbedBuilder {
    return EmbedBuilder().apply {
        url = doc.link
        // Originally I wanted to limit the input, but that sucks because
        // 1) you would count html chars which don't exist
        // 2) sometimes flexmark wil remove the ... at the end
        description = htmlRenderer.convert(doc.description).limit(EMBED_DESCRIPTION_MAX_LENGTH)

        if (doc.deprecated) {
            field {
                name = "Deprecated"
                value = doc.deprecationMessage
            }
        }
    }.apply(creator)
}

private const val EMBED_DESCRIPTION_MAX_LENGTH = 2048
private const val EMBED_TITLE_MAX_LENGTH = 1024
