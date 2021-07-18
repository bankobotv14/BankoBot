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

import info.debatty.java.stringsimilarity.Levenshtein
import java.util.*

/**
 * Discount Google (aka. Bing) which searches for docs
 */
object DocsGoogle {
    private val levenshtein = Levenshtein()

    /**
     * Calculates a score for [element] on how good it servers the [query]
     */
    @Suppress("MagicNumber")
    fun calculateScore(element: DocumentedElement, query: Reference): Double {
        val notClass = query.method != null
        val obj = element.`object`
        var penalty = 0.0

        if (notClass != obj is DocumentedMethodObject) {
            penalty += 10
        }

        if (query.`package` != null) {
            penalty += levenshtein.distance(
                query.`package`.lowercase(Locale.getDefault()),
                obj.`package`.lowercase(Locale.getDefault())
            )
        }

        if (query.clazz != null) {
            penalty += levenshtein.distance(
                query.clazz.lowercase(Locale.getDefault()),
                if (obj is DocumentedMethodObject) {
                    obj.metadata.owner.lowercase(Locale.getDefault())
                } else {
                    obj.name.lowercase(
                        Locale.getDefault()
                    )
                }
            ) * 0.3
            // this has a very low value since sometimes you refer
            // to a method from an interface by a common implementation
            // e.g Player#sendMessage(String) is actually CommandSender#sendMessage()
        }

        if (query.method != null) {
            if (obj !is DocumentedMethodObject) {
                penalty += 100
            } else {
                penalty += levenshtein.distance(
                    query.method.lowercase(Locale.getDefault()),
                    obj.name.lowercase(Locale.getDefault())
                ) * 20 // This has a very high value, see explanation for className above
            }
        }

        return penalty
    }

    /**
     * Finds the most suitable [DocumentedElement] for the requested [Reference].
     */
    fun findMostSuitable(
        options: List<DocumentedElement>,
        reference: Reference,
    ): DocumentedElement {
        return options.minByOrNull { calculateScore(it, reference) } ?: options.first()
    }
}
