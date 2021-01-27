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
import dev.kord.core.entity.Role
import dev.kord.rest.builder.interaction.BaseApplicationBuilder
import dev.kord.rest.builder.interaction.RoleBuilder
import dev.kord.x.commands.argument.Argument
import dev.kord.x.commands.kord.argument.RoleArgument

/**
 * Turns this [Argument] into a [SlashArgument] with [description]
 *
 * @see RoleBuilder
 * @see RoleArgument
 */
@OptIn(KordPreview::class)
fun <T : Role?, CONTEXT> Argument<T, CONTEXT>.asSlashArgument(
    description: String,
    builder: RoleBuilder.() -> Unit = {},
): SlashArgument<T, CONTEXT> = RoleSlashArgument(description, this, builder)

@OptIn(KordPreview::class)
private class RoleSlashArgument<T : Role?, CONTEXT>(
    description: String,
    delegate: Argument<T, CONTEXT>,
    private val builder: RoleBuilder.() -> Unit,
) : AbstractSlashCommandArgument<T, CONTEXT>(description, delegate) {
    override fun BaseApplicationBuilder.applyArgument() {
        role(name, description, builder.applyRequired())
    }
}
