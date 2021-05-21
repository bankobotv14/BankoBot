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

import de.nycode.bankobot.command.Context
import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.description
import de.nycode.bankobot.command.isMessageCommandContext
import de.nycode.bankobot.command.slashcommands.arguments.asSlashArgument
import de.nycode.bankobot.command.slashcommands.supportsSlashCommands
import de.nycode.bankobot.commands.GeneralModule
import de.nycode.bankobot.utils.Embeds
import dev.kord.common.annotation.KordPreview
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.argument.extension.optional
import dev.kord.x.commands.argument.text.WordArgument
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

private suspend fun Context.specificCommand(commandName: String) {
    val command = processor.getCommand(commandName)
    if (command == null) {
        sendResponse(Embeds.error("Unbekannter Befehl", "Dieser Befehl ist nicht bekannt"))
        return
    }
    sendResponse(Embeds.command(command, processor))
}

private suspend fun Context.allCommands() {
    val commands = processor.commands.values.groupBy { it.module.name }
    val myCommands = commands
        .mapValues { (_, it) ->
            it.filter {
                it.aliasInfo !is AliasInfo.Child &&
                        it.preconditions.all { precondition ->
                            @Suppress("UNCHECKED_CAST")
                            (precondition as Precondition<Context>).invoke(
                                this@allCommands
                            )
                        } && (isMessageCommandContext() || it.supportsSlashCommands) // only show /commands in /help
            }
        }

    sendResponse(
        Embeds.info(
            "Hilfe - Befehlsliste",
            """Dies ist eine Liste aller Befehle, die du benutzen kannst,
                |um mehr über einen Befehl zu erfahren kannst du `xd help [command]` ausführen""".trimMargin()
        )
    ) {
        myCommands.forEach { (module, commands) ->
            if (commands.isNotEmpty()) {
                field {
                    name = module
                    value = commands.joinToString("`, `", "`", "`") { it.name }
                }
            }
        }
    }
}
