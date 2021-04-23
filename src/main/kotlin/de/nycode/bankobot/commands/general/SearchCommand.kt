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
import de.nycode.bankobot.utils.Embeds.editEmbed
import de.nycode.bankobot.utils.GoogleUtil
import de.nycode.bankobot.utils.doExpensiveTask
import de.nycode.bankobot.utils.paginate
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.argument.text.StringArgument
import dev.kord.x.commands.kord.model.context.KordCommandEvent
import dev.kord.x.commands.model.command.invoke

private val QueryArgument = StringArgument.named("Text").asSlashArgument("Die Such-Query nach der gesucht werden soll")

@AutoWired
@ModuleName(GeneralModule)
fun searchCommand() = command("google") {
    alias("find", "search", "duckduckgo")
    description("Sucht dir deinen Scheiß aus dem Internet zusammen.")

    invoke(QueryArgument) { argument ->
        search(argument)
    }
}

private suspend fun KordCommandEvent.search(search: String) {
    doExpensiveTask("Searching...", "Bitte warte, bis ich Ergebnisse gefunden habe!") {
        val list = getResultAsList(search)
        if (list.isNullOrEmpty()) {
            editEmbed(Embeds.error("Schade!", "Google möchte dir anscheinend nicht antworten! ._."))
        } else {
            delete()
            list.paginate(channel, "Suchergebnisse") {
                itemsPerPage = 1
            }
        }
    }
}

suspend fun getResultAsList(search: String): List<String>? {
    val result = GoogleUtil.getResults(search) ?: return null
    return result.map { "**${it.title}**\n ${it.link} \n${it.snippet}" }
}
