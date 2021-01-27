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

package de.nycode.bankobot.commands.dev

import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.description
import de.nycode.bankobot.command.permissions.PermissionLevel
import de.nycode.bankobot.command.permissions.permission
import de.nycode.bankobot.command.slashcommands.disableSlashCommands
import de.nycode.bankobot.command.slashcommands.registerSlashCommands
import de.nycode.bankobot.commands.BotOwnerModule
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.editEmbed
import de.nycode.bankobot.utils.doExpensiveTask
import dev.kord.common.annotation.KordPreview
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.model.command.invoke
import kotlinx.coroutines.flow.toList

@AutoWired
@ModuleName(BotOwnerModule)
@OptIn(KordPreview::class)
fun updateSlashCommandsCommand() = command("update-slash-commands") {
    permission(PermissionLevel.BOT_OWNER)
    description("Registriert alle Slash command erneut")
    disableSlashCommands()

    invoke {
        doExpensiveTask {
//            kord.slashCommands
//                .getGlobalApplicationCommands()
//                .toList()
//                .forEach { it.delete() }
            processor.registerSlashCommands(kord)

            editEmbed(Embeds.success("DONE"))
        }
    }
}
