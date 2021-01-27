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

package de.nycode.bankobot.command

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.config.Config
import de.nycode.bankobot.config.Environment
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.x.commands.kord.model.context.KordCommandEvent
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Entry in the blacklist.
 *
 * @property userId [Snowflake] of the user represented by this entry
 */
@Serializable
data class BlacklistEntry(@SerialName("_id") @Contextual val userId: Snowflake)

internal object BlacklistEnforcer : AbstractKordPrecondition() {
    override suspend fun invoke(event: KordCommandEvent): Boolean {
        return if (Config.ENVIRONMENT != Environment.PRODUCTION) {
            val id = event.message.author?.id ?: return false
            BankoBot.repositories.blacklist.findOneById(id) == null
        } else {
            true // debug mode disabled blacklist
        }
    }
}
