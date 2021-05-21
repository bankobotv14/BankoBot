/*
 *     This file is part of the BankoBot Project.
 *     Copyright (C) 2021  BankoBot Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Also add information on how to contact you by electronic and paper mail.
 *
 *   If your software can interact with users remotely through a computer
 * network, you should also make sure that it provides a way for users to
 * get its source.  For example, if your program is a web application, its
 * interface could display a "Source" link that leads users to an archive
 * of the code.  There are many ways you could offer source, and different
 * solutions will be better for different programs; see section 13 for the
 * specific requirements.
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU AGPL, see
 * <https://www.gnu.org/licenses/>.
 *
 */

package de.nycode.bankobot.commands.general

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.command.Context
import de.nycode.bankobot.command.EditableMessage
import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.description
import de.nycode.bankobot.command.slashcommands.arguments.AbstractSlashCommandArgument
import de.nycode.bankobot.command.slashcommands.arguments.asSlashArgument
import de.nycode.bankobot.docdex.*
import de.nycode.bankobot.utils.*
import dev.kord.common.annotation.KordPreview
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.interaction.BaseApplicationBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.Argument
import dev.kord.x.commands.argument.extension.filter
import dev.kord.x.commands.argument.extension.map
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.argument.result.extension.FilterResult
import dev.kord.x.commands.argument.text.WordArgument
import dev.kord.x.commands.model.command.invoke

@Suppress("TopLevelPropertyNaming")
const val DocsModule = "Documentation"

private fun <CONTEXT> Argument<String, CONTEXT>.docsFilter() = filter { doc ->

    if (BankoBot.availableDocs == null) {
        return@filter FilterResult
            .Fail("Die verfügbaren Docs konnten leider nicht geladen werden! Versuche es später erneut!")
    }

    if (BankoBot.availableDocs?.contains(doc) == true) {
        FilterResult.Pass
    } else {
        FilterResult.Fail("Dieses Doc is unbekannt. (Siehe `xd list-docs`)")
    }
}

@OptIn(KordPreview::class)
private val JavaDocArgument = WordArgument
    .named("javadoc-name")
    .map { it.toLowerCase() }
    .docsFilter()
    .asSlashArgument("Das Javadoc in dem gesucht werden soll") {
        choice("JDK 11 Dokumentation", "jdk11")
        choice("JDK 8 Dokumentation", "jdk8")
        choice("Spigot 1.16.5 Dokumentation", "spigot1165")
        choice("Paper Spigot Dokumentation", "paper")
        choice("JDA Dokumentation", "jda")
    }

private object QueryArgument :
    AbstractSlashCommandArgument<Reference, MessageCreateEvent>(
        "Die Referenz zum Doc Eintrag nach dem gesucht werden soll",
        ReferenceArgument.named("query")
    ) {
    @OptIn(KordPreview::class)
    override fun BaseApplicationBuilder.applyArgument() {
        string(name, description, required())
    }
}

@AutoWired
@ModuleName(DocsModule)
fun allDocsCommand() = command("alldocs") {
    description("Zeigt eine Lister alle unterstützten Javadocs")
    alias("all-docs", "list-docs", "listdocs", "availabledocs", "available-docs")

    invoke {

        val embed = if (BankoBot.availableDocs == null) {
            Embeds.error(
                "Keine Docs verfügbar!",
                "Die Docs konnten leider nicht geladen werden! Versuche es später erneut!"
            )
        } else {
            Embeds.info(
                "Verfügbare Dokumentationen!",
                BankoBot.availableDocs?.format()
            )
        }

        sendResponse(embed)
    }
}

@AutoWired
@ModuleName(DocsModule)
fun docsCommand() = command("docs") {
    description("Zeigt Javadoc in Discord an")
    alias("doc")

    invoke(
        JavaDocArgument,
        QueryArgument
    ) { doc, query ->
        docs(doc, query)
    }
}

suspend fun Context.docs(doc: String, reference: Reference) {
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

private suspend fun EditableMessage.respond(doc: DocumentedElement) {
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

    if (meta.subInterfaces.isNotEmpty()) {
        field {
            name = "All known sub interfaces"
            value = meta.subInterfaces.formatReferences()
        }
    }

    if (meta.subClasses.isNotEmpty()) {
        field {
            name = "All known subclasses"
            value = meta.subClasses.formatReferences()
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
    @Suppress("MaxLineLength")
    return "${
        doc.annotations.joinToString(
            " ",
            postfix = "\n"
        ) { "@${it.className}" }
    }${doc.modifiers.joinToString(" ") + if (doc.modifiers.isNotEmpty()) " " else ""}${doc.metadata.returns} ${doc.name}(${
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
