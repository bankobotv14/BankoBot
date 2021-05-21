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

package de.nycode.bankobot.utils

import de.nycode.bankobot.BankoBot
import de.nycode.bankobot.utils.Embeds.createEmbed
import de.nycode.bankobot.utils.Embeds.editEmbed
import dev.kord.common.Color
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
import dev.kord.x.emoji.Emojis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.litote.kmongo.coroutine.CoroutineFindPublisher
import kotlin.math.ceil
import kotlin.math.min
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

/**
 * Interface to provide paginateable items
 */
interface ItemProvider {
    /**
     * The total amount of items
     */
    val length: Int

    /**
     * Whether there are items or not
     */
    val isEmpty: Boolean
        get() = length < 0

    /**
     * Suspendable function to retrieve items from [startIndex] to [endIndex].
     */
    suspend fun subList(startIndex: Int, endIndex: Int): List<String>
}

@Suppress("FunctionName")
fun LazyItemProvider(length: Int, subList: suspend (Int, Int) -> List<String>) =
    object : ItemProvider {
        override val length: Int = length

        override suspend fun subList(startIndex: Int, endIndex: Int): List<String> =
            subList(startIndex, endIndex)
    }

/**
 * Options for paginator.
 *
 * @property items an [ItemProvider]
 * @property title the title of this paginator
 * @property loadingTitle the title for the loading embed
 * @property loadingDescription the description for the loading embed
 * @property timeout the [Duration] after which the Paginator should stop listening for input
 * @property firstPage the first page to get rendered
 * @property itemsPerPage the maximal amount of items per page
 * @property color the [Color] of the embed
 */
@Suppress("MagicNumber")
class PaginatorOptions(val items: ItemProvider) {
    lateinit var title: String
    internal var pages by Delegates.notNull<Int>()
    var loadingTitle: String = "Bitte warten"
    var loadingDescription: String = "Bitte warte, während die Liste geladen wird."

    @OptIn(ExperimentalTime::class)
    var timeout: Duration = Duration.seconds(15)
    var firstPage: Int = 1
    var itemsPerPage: Int = 8
        set(value) {
            require(value > 0) { "Items per page must be > 0" }
            field = value
        }
    var color: Color = Colors.BLUE

    internal fun ensureReady(): PaginatorOptions {
        require(::title.isInitialized) { "Title need to be set" }
        require(!items.isEmpty) { "Items cannot be empty" }
        pages = ceil(items.length.toDouble() / itemsPerPage).toInt()
        require(firstPage <= pages) { "First page must exist" }

        return this
    }
}

/**
 * Creates a paginator for all items in this list.
 *
 * @see ItemProvider.paginate
 */
suspend fun List<String>.paginate(
    channel: MessageChannelBehavior,
    title: String? = null,
    builder: PaginatorOptions.() -> Unit = {},
) = DelegatedItemProvider(this).paginate(channel, title, builder)

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
) {
    require(!isEmpty) { "Items must not be empty" }
    val options = PaginatorOptions(this).apply {
        if (title != null) {
            this.title = title
        }
    }.apply(builder).ensureReady()

    fun renderEmbed(rows: List<String>, currentPage: Int): EmbedBuilder {
        return EmbedBuilder().apply {
            color = options.color
            this.title = options.title
            val rowBuilder = StringBuilder()
            rows.indices.forEach {
                rowBuilder.append('`').append(it + (options.itemsPerPage * (currentPage - 1)) + 1)
                    .append("`. ")
                    .appendLine(rows[it])
            }
            description = rowBuilder.toString()
            footer {
                text = "Seite $currentPage/${options.pages} (${length} Einträge)"
            }
        }
    }

    if (options.pages > 1) {
        channel.doExpensiveTask(options.loadingTitle, options.loadingDescription) {
            ALL.forEach {
                addReaction(ReactionEmoji.Unicode(it))
            }

            Paginator(asMessage().live(), ::renderEmbed, options).paginate(options.firstPage)
        }
    } else {
        channel.createEmbed(renderEmbed(this.subList(0, length), 1))
    }
}

private val BULK_LEFT: String = Emojis.rewind.unicode
private val LEFT: String = Emojis.arrowLeft.unicode
private val STOP: String = Emojis.stopButton.unicode
private val RIGHT: String = Emojis.arrowRight.unicode
private val BULK_RIGHT: String = Emojis.fastForward.unicode

private val ALL = listOf(BULK_LEFT, LEFT, STOP, RIGHT, BULK_RIGHT)

private class DelegatedItemProvider(private val list: List<String>) : ItemProvider {
    override val length: Int
        get() = list.size

    override suspend fun subList(startIndex: Int, endIndex: Int): List<String> =
        list.subList(startIndex, endIndex)
}

@OptIn(KordPreview::class, ExperimentalTime::class)
private class Paginator constructor(
    private val message: LiveMessage,
    private val renderEmbed: (List<String>, Int) -> EmbedBuilder,
    private val options: PaginatorOptions,
) {
    private var currentPage = -1

    @OptIn(KordPreview::class)
    private val listener: Job = message
        .events
        .onEach(::handleEvent)
        .launchIn(BankoBot)

    private var canceller = timeout()

    private fun timeout(): Job {
        return BankoBot.launch {
            delay(options.timeout.inWholeMilliseconds)
            close()
        }
    }

    private fun rescheduleTimeout() {
        canceller.cancel()
        canceller = timeout()
    }

    private suspend fun onReactionAdd(event: ReactionAddEvent) {
        if (event.user.id != event.kord.selfId) {
            event.message.deleteReaction(event.userId, event.emoji)
        } else return // Don't react to the bots reactions
        val emote = event.emoji.name
        if (emote !in ALL) return

        val nextPage = when (emote) {
            BULK_LEFT -> 1
            LEFT -> currentPage - 1
            RIGHT -> currentPage + 1
            BULK_RIGHT -> options.pages
            else -> -1
        }

        if (nextPage == -1) {
            close()
            return
        }

        rescheduleTimeout()

        if (nextPage !in 1..options.pages) return

        paginate(nextPage)
    }

    private suspend fun onReactionRemove(event: ReactionRemoveEvent) {
        if (event.emoji.name in ALL) {
            message.message.addReaction(event.emoji)
        }
    }

    private suspend fun onDelete() = close()

    suspend fun paginate(page: Int) {
        currentPage = page
        val start: Int = (page - 1) * options.itemsPerPage
        val end = min(options.items.length, page * options.itemsPerPage)
        val rows = options.items.subList(start, end)
        message.message.editEmbed(renderEmbed(rows, currentPage))
    }

    private suspend fun close() {
        message.message.deleteAllReactions()
        if (canceller.isActive) canceller.cancel()
        listener.cancel()
    }

    private suspend fun handleEvent(event: Event) {
        when (event) {
            is ReactionAddEvent -> onReactionAdd(event)
            is ReactionRemoveEvent -> onReactionRemove(event)
            is MessageDeleteEvent -> onDelete()
        }
    }
}

/**
 * Queries a sub-list of the entries used for pagination
 * @param start where to start the query
 * @param pageSize the size of a single page
 * @param result function to map the result to a [String]
 */
suspend fun <T : Any> CoroutineFindPublisher<T>.paginate(
    start: Int,
    pageSize: Int = 8,
    result: (T) -> String
) =
    skip(start)
        .limit(pageSize)
        .toList()
        .map(result)
