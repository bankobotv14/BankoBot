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

package de.nycode.bankobot.command

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.config.Config
import de.nycode.bankobot.config.Environment
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.interaction.GuildInteraction
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
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

@OptIn(KordPreview::class)
internal object BlacklistEnforcer : AbstractKordFilter() {

    override suspend fun invoke(event: Event): Boolean {
        val author = when (event) {
            is MessageCreateEvent -> event.message.author?.id ?: return false
            is InteractionCreateEvent -> (event.interaction as GuildInteraction).member.id
            else -> return false
        }
        return if (Config.ENVIRONMENT != Environment.PRODUCTION) {
            BankoBot.repositories.blacklist.findOneById(author) == null
        } else {
            true // debug mode disabled blacklist
        }
    }
}
