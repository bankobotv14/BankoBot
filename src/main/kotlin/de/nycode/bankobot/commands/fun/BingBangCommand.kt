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

package de.nycode.bankobot.commands.`fun`

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.command.respond
import de.nycode.bankobot.command.safeMember
import de.nycode.bankobot.config.Config
import de.nycode.bankobot.utils.Embeds
import dev.kord.x.emoji.Emojis
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.TrackStartEvent
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.kord.lavakord
import dev.schlaubi.lavakord.rest.loadItem
import dev.schlaubi.mikbot.plugin.api.util.safeGuild
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private val tehMusic = listOf(
    "https://youtube.com/watch?v=l-Egisu_4AA",
    "https://youtube.com/watch?v=NVRLoNWdnQY",
    "https://youtube.com/watch?v=0CD4VHzjwbs",
    "https://youtube.com/watch?v=zbxm5-9-508",
    "https://youtube.com/watch?v=mTnDyLXKdAs",
    "https://youtube.com/watch?v=SA99wua3B1s",
    "https://youtube.com/watch?v=ON-HaqBLt_0"
)

class BingBangArguments : Arguments() {
    val force by defaultingBoolean {
        name = "force"
        description = "Erzwingt den BingBang"
        defaultValue = true
    }
}

suspend fun FunModule.bingBangCommand() = ephemeralSlashCommand(::BingBangArguments) {
    name = "bingbang"
    description = "LÄSST DICH SPAß HABEN"

    val lavalink = BankoBot.kord.lavakord()

    val lavalinkHost = Config.LAVALINK_HOST
    val lavalinkPassword = Config.LAVALINK_PASSWORD
    if (lavalinkHost != null && lavalinkPassword != null) {
        lavalink.addNode(lavalinkHost, lavalinkPassword)
    }

    action {
        val channelId = safeMember.getVoiceStateOrNull()?.channelId
        if (channelId == null) {
            respond(
                Embeds.error(
                    "Du bist in keinem voice channel!",
                    "Bitte joine einen VoiceChannel."
                )
            )
            return@action
        }

        val guildId = safeGuild.id

        val link = lavalink.getLink(guildId)
        link.connectAudio(channelId)

        val player = link.player

        @Suppress("SpellCheckingInspection") // STFU THE NAME IS GREAT
        val track = link.loadItem(if (arguments.force) tehMusic.first() else tehMusic.random()).track
        player.playTrack(track)

        respond(Emojis.musicalNote)

        BankoBot.launch {
            if (track.info.identifier == "l-Egisu_4AA") {
                player.events
                    .filterIsInstance<TrackStartEvent>()
                    .take(1)
                    .single() // wait for start
                @Suppress("MagicNumber")
                player.seekTo(5.seconds)
            }

            player.events
                .filterIsInstance<TrackEndEvent>()
                .take(1)
                .single() // this should wait for track end event

            link.destroy()
        }
    }
}
