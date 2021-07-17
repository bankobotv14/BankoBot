package de.nycode.bankobot.utils.paginator

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.utils.Embeds.createEmbed
import de.nycode.bankobot.utils.Embeds.editEmbed
import de.nycode.bankobot.utils.doExpensiveTask
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.Event
import dev.kord.core.event.message.MessageDeleteEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.event.message.ReactionRemoveEvent
import dev.kord.core.live.LiveMessage
import dev.kord.core.live.live
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.time.ExperimentalTime

private val ALL = listOf(BULK_LEFT, LEFT, STOP, RIGHT, BULK_RIGHT)

/**
 * Creates a reaction based paginator for all elements in this [ItemProvider] in [channel]
 *
 * See List.paginate method above for default implementation
 *
 * @param title Shortcut to [PaginatorOptions.title]
 * @see PaginatorOptions
 *
 * @see LazyItemProvider
 */
@OptIn(KordPreview::class, ExperimentalTime::class)
suspend fun ItemProvider.paginate(
    channel: MessageChannelBehavior,
    title: String? = null,
    builder: PaginatorOptions.() -> Unit = {},
) = paginate(title, builder, {
    channel.doExpensiveTask(options.loadingTitle, options.loadingDescription) {
        ALL.forEach {
            addReaction(ReactionEmoji.Unicode(it))
        }

        MessagePaginator(asMessage().live(), ::renderEmbed, options).paginate(options.firstPage)
    }
}, { channel.createEmbed(it) })

/**
 * Creates a paginator for all items in this list.
 *
 * @see ItemProvider.paginate
 */
suspend fun List<String>.paginate(
    channel: MessageChannelBehavior,
    title: String? = null,
    builder: PaginatorOptions.() -> Unit = {},
): Unit = DelegatedItemProvider(this).paginate(channel, title, builder)

@OptIn(KordPreview::class, ExperimentalTime::class)
private class MessagePaginator constructor(
    private val message: LiveMessage,
    renderEmbed: (List<String>, Int) -> EmbedBuilder,
    options: PaginatorOptions,
) : AbstractPaginator(renderEmbed, options) {
    @OptIn(KordPreview::class)
    private val listener: Job = message
        .events
        .onEach(::handleEvent)
        .launchIn(BankoBot)

    override suspend fun updateMessage(embedBuilder: EmbedBuilder) {
        message.message.editEmbed(embedBuilder)
    }

    override suspend fun close() {
        message.message.deleteAllReactions()
        listener.cancel()
    }

    private suspend fun onReactionAdd(event: ReactionAddEvent) {
        if (event.user.id != event.kord.selfId) {
            event.message.deleteReaction(event.userId, event.emoji)
        } else return // Don't react to the bots reactions
        val emote = event.emoji.name
        if (emote !in ALL) return

        onInteraction(event.emoji.name)
    }

    private suspend fun onReactionRemove(event: ReactionRemoveEvent) {
        if (event.emoji.name in ALL) {
            message.message.addReaction(event.emoji)
        }
    }

    private suspend fun onDelete() = internalClose()

    private suspend fun handleEvent(event: Event) {
        when (event) {
            is ReactionAddEvent -> onReactionAdd(event)
            is ReactionRemoveEvent -> onReactionRemove(event)
            is MessageDeleteEvent -> onDelete()
        }
    }
}
