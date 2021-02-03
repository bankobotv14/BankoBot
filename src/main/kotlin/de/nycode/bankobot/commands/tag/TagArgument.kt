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

package de.nycode.bankobot.commands.tag

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.command.slashcommands.arguments.AbstractSlashCommandArgument
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.BaseApplicationBuilder
import dev.kord.x.commands.argument.Argument
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.argument.extension.tryMap
import dev.kord.x.commands.argument.result.extension.MapResult
import dev.kord.x.commands.argument.text.WordArgument
import org.litote.kmongo.contains
import org.litote.kmongo.eq
import org.litote.kmongo.or

val TagArgument: Argument<Tag, Any?> = InternalTagArgument()

@OptIn(KordPreview::class)
internal class InternalTagArgument(
    description: String = "Der Tag"
) : AbstractSlashCommandArgument<Tag, Any?>(description, WordArgument.named("tag").tagMap()) {
    override fun BaseApplicationBuilder.applyArgument() {
        string(name, description, required())
    }
}

private fun <CONTEXT> Argument<String, CONTEXT>.tagMap() = tryMap { tagName ->
    val tag = BankoBot.repositories.tag.findOne(or(TagEntry::name eq tagName, TagEntry::aliases contains tagName))
    MapResult.Pass(tag ?: EmptyTag)
}
