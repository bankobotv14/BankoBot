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

package de.nycode.bankobot.commands.general

import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.description
import de.nycode.bankobot.command.slashcommands.arguments.asSlashArgument
import de.nycode.bankobot.commands.GeneralModule
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.respondEmbed
import dev.kord.common.annotation.KordPreview
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.argument.extension.optional
import dev.kord.x.commands.argument.text.WordArgument
import dev.kord.x.commands.kord.model.context.KordCommandEvent
import dev.kord.x.commands.model.command.AliasInfo
import dev.kord.x.commands.model.command.invoke
import dev.kord.x.commands.model.module.CommandSet
import dev.kord.x.commands.model.precondition.Precondition

@OptIn(KordPreview::class)
private val CommandArgument = WordArgument.named("command")
    .optional()
    .asSlashArgument("Der spezifische Befehl für den Hilfe angezeigt werden soll")

@PublishedApi
@AutoWired
@ModuleName(GeneralModule)
internal fun helpCommand(): CommandSet = command("help") {
    alias("h", "hilfe", "idunnowhattodo", "tech-support", "whodoyouwork")
    description("Zeigt die eine Liste aller Befehle oder Informationen zu einem spezifischem Befehl")

    invoke(CommandArgument) { commandName ->
        if (commandName.isNullOrBlank()) {
            allCommands()
        } else {
            specificCommand(commandName)
        }
    }
}

private suspend fun KordCommandEvent.specificCommand(commandName: String) {
    val command = processor.getCommand(commandName)
    if (command == null) {
        respondEmbed(Embeds.error("Unbekannter Befehl", "Dieser Befehl ist nicht bekannt"))
        return
    }
    respondEmbed(Embeds.command(command, processor))
}

private suspend fun KordCommandEvent.allCommands() {
    val commands = processor.commands.values.groupBy { it.module.name }

    respondEmbed(
        Embeds.info(
            "Hilfe - Befehlsliste",
            """Dies ist eine Liste aller Befehle, die du benutzen kannst,
                |um mehr über einen Befehl zu erfahren kannst du `xd help [command]` ausführen""".trimMargin()
        )
    ) {
        commands
            .mapValues { (_, it) ->
                it.filter {
                    it.aliasInfo !is AliasInfo.Child &&
                            it.preconditions.all { precondition ->
                                @Suppress("UNCHECKED_CAST")
                                (precondition as Precondition<KordCommandEvent>).invoke(
                                    this@allCommands
                                )
                            }
                }
            }
            .forEach { (module, commands) ->
                if (commands.isNotEmpty()) {
                    field {
                        name = module
                        value = commands.joinToString("`, `", "`", "`") { it.name }
                    }
                }
            }
    }
}
