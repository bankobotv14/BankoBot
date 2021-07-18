package de.nycode.bankobot.utils.paginator

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageDeleteEvent
import dev.kord.core.on
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.interaction.actionRow
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.Job
import kotlin.time.ExperimentalTime

/**
 * Creates a paginator for all items in this list.
 *
 * @see ItemProvider.paginate
 */
@OptIn(KordPreview::class)
suspend fun List<String>.paginate(
    interactionId: Snowflake,
    response: PublicInteractionResponseBehavior,
    title: String? = null,
    builder: PaginatorOptions.() -> Unit = {},
): Unit = DelegatedItemProvider(this).paginate(interactionId, response, title, builder)

/**
 * Creates a button based paginator for all elements in this [ItemProvider] in [response]
 *
 * See List.paginate method above for default implementation
 *
 * @param title Shortcut to [PaginatorOptions.title]
 * @param interactionId the id of the interaction
 * @see PaginatorOptions
 *
 * @see LazyItemProvider
 */
@OptIn(KordPreview::class, ExperimentalTime::class)
suspend fun ItemProvider.paginate(
    interactionId: Snowflake,
    response: PublicInteractionResponseBehavior,
    title: String? = null,
    builder: PaginatorOptions.() -> Unit = {},
) = paginate(title, builder, {
    InteractionsPaginator(interactionId, response, ::renderEmbed, options).paginate(options.firstPage)
}, { response.edit { embeds = mutableListOf(it) } })

@OptIn(KordPreview::class, ExperimentalTime::class)
private class InteractionsPaginator constructor(
    private val interactionId: Snowflake,
    private val message: PublicInteractionResponseBehavior,
    renderEmbed: (List<String>, Int) -> EmbedBuilder,
    options: PaginatorOptions,
) : AbstractPaginator(renderEmbed, options) {
    @OptIn(KordPreview::class)
    private val listener: Job = message.kord
        .on(consumer = ::handleEvent)
    private val onDeleteListener = message.kord.on<MessageDeleteEvent> {
        if (messageId == message?.id) {
            internalClose()
        }
    }

    override suspend fun updateMessage(embedBuilder: EmbedBuilder) {
        message.edit {
            embeds = mutableListOf(embedBuilder)
            actionRow {
                prepareButtons()
            }

            actionRow {
                button(STOP, "cancel")
            }
        }
    }

    override suspend fun close() {
        message.edit {
            components = mutableListOf()
        }
        listener.cancel()
        onDeleteListener.cancel()
    }

    private suspend fun handleEvent(event: InteractionCreateEvent) {
        val componentInteraction = event.interaction as? ComponentInteraction ?: return
        if (componentInteraction.message?.interaction?.id != interactionId) return
        val component = componentInteraction.component ?: return
        val emoji = component.data.emoji.value?.name ?: return
        componentInteraction.acknowledgePublicDeferredMessageUpdate()
        onInteraction(emoji)
    }

    private fun ActionRowBuilder.button(emoji: String, name: String) = interactionButton(ButtonStyle.Primary, name) {
        this.emoji = DiscordPartialEmoji(name = emoji)
    }

    private fun ActionRowBuilder.prepareButtons() {
        if (currentPage > 1) { // can go back
            if (currentPage > 2) { // can go to beginning
                button(BULK_LEFT, "bulkleft")
            }

            button(LEFT, "left")
        }

        if (currentPage < options.pages) { // can go forward
            button(RIGHT, "right")
            if (currentPage < options.pages - 1) { // can go to end
                button(BULK_RIGHT, "bulkright")
            }
        }
    }
}
