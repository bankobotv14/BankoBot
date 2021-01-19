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

import de.nycode.bankobot.command.command
import de.nycode.bankobot.utils.Emotes
import de.nycode.bankobot.utils.GitHubUtil
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.edit
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.model.command.invoke


@AutoWired
@ModuleName(GeneralModule)
fun infoCommand() = command("info") {
    alias("whoareyou")
    invoke {
        channel.sendInfo()
    }
}

suspend fun MessageChannelBehavior.sendInfo() {
    val embed: EmbedBuilder.() -> Unit = {
        field {
            name = "Programmiersprache"
            value = "[Kotlin](https://kotlinlang.org)"
            inline = true
        }

        field {
            name = "Prefix"
            value = "xd"
        }
    }
    createEmbed {
        embed(this)
        field {
            name = "Entwickler"
            value = Emotes.LOADING
        }


    }.edit {
        val contributors = GitHubUtil.retrieveContributors()
        embed {
            embed(this)
            field {
                name = "Entwickler"
                value = contributors.joinToString(", ") {
                    "[${it.login}](${it.htmlUrl})"
                }
            }
        }
    }
}