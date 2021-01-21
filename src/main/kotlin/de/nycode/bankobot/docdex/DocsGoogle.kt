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

package de.nycode.bankobot.docdex

import info.debatty.java.stringsimilarity.Levenshtein

/**
 * Discount Google (aka. Bing) which searches for docs
 */
object DocsGoogle {
    private val levenshtein = Levenshtein()

    /**
     * Finds the most suitable [DocumentedElement] for the requested [Reference].
     */
    fun findMostSuitable(
        options: List<DocumentedElement>,
        reference: Reference,
    ): DocumentedElement {
        val notClass = reference.method != null
        return options.minByOrNull {
            val obj = it.`object`
            var penalty = 0.0

            if (notClass != obj is DocumentedMethodObject) {
                penalty += 10
            }

            if (reference.`package` != null) {
                penalty += levenshtein.distance(
                    reference.`package`.toLowerCase(),
                    obj.`package`.toLowerCase()
                )
            }

            if (reference.clazz != null) {
                penalty += levenshtein.distance(
                    reference.clazz.toLowerCase(),
                    if (obj is DocumentedMethodObject) obj.metadata.owner.toLowerCase() else obj.name.toLowerCase()
                ) * 0.3
                // this has a very low value since sometimes you refer
                // to a method from an interface by a common implementation
                // e.g Player#sendMessage(String) is actually CommandSender#sendMessage()
            }

            if (reference.method != null) {
                if (obj !is DocumentedMethodObject) {
                    penalty += 100
                } else {
                    penalty += levenshtein.distance(
                        reference.method.toLowerCase(),
                        obj.name.toLowerCase()
                    ) * 20 // This has a very high value, see explanation for className above
                }
            }

            penalty
        } ?: options.first()
    }
}
