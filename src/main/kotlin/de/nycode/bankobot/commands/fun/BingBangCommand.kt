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

package de.nycode.bankobot.commands.`fun`

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.description
import de.nycode.bankobot.command.slashcommands.arguments.asSlashArgument
import de.nycode.bankobot.commands.FunModule
import de.nycode.bankobot.config.Config
import de.nycode.bankobot.utils.Embeds
import dev.kord.common.annotation.KordPreview
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.extension.named
import dev.kord.x.commands.argument.extension.withDefault
import dev.kord.x.commands.argument.primitive.BooleanArgument
import dev.kord.x.commands.model.command.invoke
import dev.kord.x.emoji.Emojis
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.TrackStartEvent
import dev.schlaubi.lavakord.kord.connectAudio
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.kord.lavakord
import dev.schlaubi.lavakord.rest.loadItem
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

private val tehMusic = listOf(
    "https://youtube.com/watch?v=l-Egisu_4AA",
    "https://youtube.com/watch?v=NVRLoNWdnQY",
    "https://youtube.com/watch?v=0CD4VHzjwbs",
    "https://youtube.com/watch?v=zbxm5-9-508",
    "https://youtube.com/watch?v=mTnDyLXKdAs",
    "https://youtube.com/watch?v=SA99wua3B1s",
    "https://youtube.com/watch?v=ON-HaqBLt_0"
)

@OptIn(KordPreview::class)
private val ForceSongArgument =
    BooleanArgument.named("force").withDefault(false).asSlashArgument("Erzwingt den BingBang") {
        default = false
    }

@OptIn(ExperimentalTime::class)
@ModuleName(FunModule)
@AutoWired
fun bingBangCommand() = command("bingbang") {
    description("LÄSST DICH SPAß HABEN")

    val lavalink = BankoBot.kord.lavakord()

    val lavalinkHost = Config.LAVALINK_HOST
    val lavalinkPassword = Config.LAVALINK_PASSWORD
    if (lavalinkHost != null && lavalinkPassword != null) {
        lavalink.addNode(lavalinkHost, lavalinkPassword)
    }

    invoke(ForceSongArgument) { force ->
        val channelId = message.getAuthorAsMember()?.getVoiceStateOrNull()?.channelId
        if (channelId == null) {
            sendResponse(
                Embeds.error(
                    "Du bist in keinem voice channel!",
                    "Bitte joine einen VoiceChannel."
                )
            )
            return@invoke
        }

        val guildId = guild?.id ?: return@invoke

        val link = lavalink.getLink(guildId)
        link.connectAudio(channelId)

        val player = link.player

        @Suppress("SpellCheckingInspection") // STFU THE NAME IS GREAT
        val track = link.loadItem(if (force) tehMusic.first() else tehMusic.random()).track
        player.playTrack(track)

        sendResponse(Emojis.musicalNote)

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
