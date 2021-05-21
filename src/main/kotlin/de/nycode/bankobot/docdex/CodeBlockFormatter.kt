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

package de.nycode.bankobot.docdex

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import com.vladsch.flexmark.html2md.converter.HtmlNodeRendererHandler
import com.vladsch.flexmark.html2md.converter.PhasedHtmlNodeRenderer
import com.vladsch.flexmark.html2md.converter.internal.HtmlConverterCoreNodeRenderer
import org.jsoup.nodes.Element

/**
 * [FlexmarkHtmlConverter] used to render HTML as markdown.
 */
val htmlRenderer: FlexmarkHtmlConverter = buildRenderer()

private fun buildRenderer(): FlexmarkHtmlConverter =
    FlexmarkHtmlConverter.Builder().apply {
        htmlNodeRendererFactory { options ->
            DelegatedHtmlRenderer(HtmlConverterCoreNodeRenderer(options))
        }
    }.build()

private class DelegatedHtmlRenderer(private val renderer: PhasedHtmlNodeRenderer) :
    PhasedHtmlNodeRenderer by renderer {
    private val overwrittenNodes =
        listOf(FlexmarkHtmlConverter.CODE_NODE, FlexmarkHtmlConverter.BLOCKQUOTE_NODE)

    override fun getHtmlNodeRendererHandlers(): MutableSet<HtmlNodeRendererHandler<*>> {
        val superList = renderer.htmlNodeRendererHandlers
        superList.removeIf {
            it.tagName in overwrittenNodes
        }

        return (superList + HtmlNodeRendererHandler(FlexmarkHtmlConverter.PRE_NODE,
            Element::class.java,
            FlexmarkUtils::processPre)).toMutableSet()
    }
}
