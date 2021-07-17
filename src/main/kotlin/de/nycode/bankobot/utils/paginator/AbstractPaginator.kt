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

import de.nycode.bankobot.BankoBot
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.time.ExperimentalTime

/**
 * Context for paginator creation.
 *
 * @see paginate
 */
interface PaginatorContext {

    /**
     * The [PaginatorContext].
     */
    val options: PaginatorOptions

    /**
     * The method that renders the paginator embed.
     */
    fun renderEmbed(rows: List<String>, currentPage: Int): EmbedBuilder
}

/**
 * Creates a paginator for all elements in this [ItemProvider].
 *
 * See List.paginate method above for default implementation
 *
 * @param title Shortcut to [PaginatorOptions.title]
 * @param createPaginator lambda called on [PaginatorContext] which creates a paginator of the desired type
 * @param sendOneMessage lambda called on [PaginatorContext] providing single [EmbedBuilder]
 * to send un-paginated single page
 * @see PaginatorOptions
 *
 * @see LazyItemProvider
 * @see paginate
 */
@OptIn(KordPreview::class, ExperimentalTime::class)
internal suspend fun ItemProvider.paginate(
    title: String? = null,
    builder: PaginatorOptions.() -> Unit = {},
    createPaginator: suspend PaginatorContext.() -> Unit,
    sendOneMessage: suspend PaginatorContext.(EmbedBuilder) -> Unit
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

    val context = object : PaginatorContext {
        override val options: PaginatorOptions
            get() = options

        override fun renderEmbed(rows: List<String>, currentPage: Int): EmbedBuilder = renderEmbed(rows, currentPage)
    }

    if (options.pages > 1) {
        createPaginator(context)
    } else {
       context.sendOneMessage(renderEmbed(this.subList(0, length), 1))
    }
}

/**
 * Abstract implementation of a paginator.
 *
 * @param renderEmbed a function which renders the embed for the given elements on the specified page
 * @property options the [PaginatorOptions]
 */
@OptIn(KordPreview::class, ExperimentalTime::class)
abstract class AbstractPaginator constructor(
    private val renderEmbed: (elements: List<String>, page: Int) -> EmbedBuilder,
    protected val options: PaginatorOptions,
) {
    protected var currentPage = -1

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

    /**
     * Method supposed to be called on interaction with the paginator (reaction/button).
     */
    protected suspend fun onInteraction(emoji: String) {
        val nextPage = when (emoji) {
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

    /**
     * Paginates to [page].
     */
    suspend fun paginate(page: Int) {
        currentPage = page
        val start: Int = (page - 1) * options.itemsPerPage
        val end = min(options.items.length, page * options.itemsPerPage)
        val rows = options.items.subList(start, end)
        updateMessage(renderEmbed(rows, currentPage))
    }

    /**
     * Call this close method to close override [close] to close own resources,
     */
    protected suspend fun internalClose() {
        if (canceller.isActive) canceller.cancel()
        close()
    }

    /**
     * Add own hooks to [internalClose].
     */
    protected abstract suspend fun close()

    /**
     * Updates the paginated message to [embedBuilder].
     */
    protected abstract suspend fun updateMessage(embedBuilder: EmbedBuilder)
}
