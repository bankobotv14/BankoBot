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

package de.nycode.bankobot.utils

import dev.kord.common.Color

/**
 * Wrapper for [Discordapp.com/branding][https://discordapp.com/branding] colors and some other colors:
 */
@Suppress("KDocMissingDocumentation", "unused", "MagicNumber")
object Colors {
    // Discord
    val BLURLPLE: Color = Color(114, 137, 218)
    val FULL_WHITE: Color = Color(255, 255, 255)
    val GREYPLE: Color = Color(153, 170, 181)
    val DARK_BUT_NOT_BLACK: Color = Color(44, 47, 51)
    val NOT_QUITE_BLACK: Color = Color(33, 39, 42)

    // Other colors
    val LIGHT_RED: Color = Color(231, 76, 60)
    val DARK_RED: Color = Color(192, 57, 43)
    val LIGHT_GREEN: Color = Color(46, 204, 113)
    val DARK_GREEN: Color = Color(39, 174, 96)
    val BLUE: Color = Color(52, 152, 219)
    val YELLOW: Color = Color(241, 196, 15)
}
