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

package de.nycode.bankobot.utils.paginator

import de.nycode.bankobot.command.Context
import de.nycode.bankobot.command.DelegatedKordCommandContext
import de.nycode.bankobot.command.slashcommands.SlashCommandContext
import de.nycode.bankobot.utils.Colors
import dev.kord.common.Color
import dev.kord.common.annotation.KordPreview
import dev.kord.x.emoji.Emojis
import org.litote.kmongo.coroutine.CoroutineFindPublisher
import kotlin.math.ceil
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

internal val BULK_LEFT: String = Emojis.rewind.unicode
internal val LEFT: String = Emojis.arrowLeft.unicode
internal val STOP: String = Emojis.stopButton.unicode
internal val RIGHT: String = Emojis.arrowRight.unicode
internal val BULK_RIGHT: String = Emojis.fastForward.unicode

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
fun LazyItemProvider(length: Int, subList: suspend (Int, Int) -> List<String>): ItemProvider =
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
 * Implementation of [ItemProvider] which delegates to [list].
 *
 * @see List
 */
class DelegatedItemProvider(private val list: List<String>) : ItemProvider {
    override val length: Int
        get() = list.size

    override suspend fun subList(startIndex: Int, endIndex: Int): List<String> =
        list.subList(startIndex, endIndex)
}


/**
 * Creates a context-aware paginator for all elements in this [ItemProvider] using [context].
 *
 * **Note:** this will use buttons for interactions and reactions for message commands
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
    context: Context,
    title: String? = null,
    builder: PaginatorOptions.() -> Unit = {},
) =
    when (context) {
        is DelegatedKordCommandContext -> paginate(context.channel, title, builder) // message context
        is SlashCommandContext -> paginate(context.interactionId, context.ack, title, builder) // public slash command
        else -> error("ephemerals are not supported due to unpredictable delete behavior")
    }

/**
 * Creates a context-aware paginator for all items in this list.
 *
 * @see ItemProvider.paginate
 */
@OptIn(KordPreview::class)
suspend fun List<String>.paginate(
    context: Context,
    title: String? = null,
    builder: PaginatorOptions.() -> Unit = {},
): Unit = when (context) {
    is DelegatedKordCommandContext -> paginate(context.channel, title, builder) // message context
    is SlashCommandContext -> paginate(context.interactionId, context.ack, title, builder) // public slash command
    else -> error("ephemerals are not supported due to unpredictable delete behavior")
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
): List<String> =
    skip(start)
        .limit(pageSize)
        .toList()
        .map(result)
