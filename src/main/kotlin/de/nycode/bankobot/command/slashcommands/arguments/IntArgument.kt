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

package de.nycode.bankobot.command.slashcommands.arguments

import de.nycode.bankobot.command.slashcommands.SlashArgument
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.BaseApplicationBuilder
import dev.kord.rest.builder.interaction.ChannelBuilder
import dev.kord.rest.builder.interaction.IntChoiceBuilder
import dev.kord.x.commands.argument.Argument
import dev.kord.x.commands.argument.primitive.IntArgument

/**
 * Turns this [Argument] into a [SlashArgument] with [description]
 *
 * @see IntChoiceBuilder
 * @see IntArgument
 */
@OptIn(KordPreview::class)
fun <T : Number?, CONTEXT> Argument<T, CONTEXT>.asSlashArgument(
    description: String,
    choiceBuilder: IntChoiceBuilder.() -> Unit = {},
): SlashArgument<T, CONTEXT> {
    return IntSlashArgument(description, this, choiceBuilder)
}

@OptIn(KordPreview::class)
private class IntSlashArgument<T : Number?, CONTEXT>(
    description: String,
    delegate: Argument<T, CONTEXT>,
    private val choiceBuilder: IntChoiceBuilder.() -> Unit,
) : AbstractSlashCommandArgument<T, CONTEXT>(description, delegate) {
    override fun BaseApplicationBuilder.applyArgument() {
        int(name, description, choiceBuilder.applyRequired())
    }
}
