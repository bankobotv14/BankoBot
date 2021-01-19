/*
 * Copyright 2020 Daniel Scherf & Michael Rittmeister & Julian König
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.nycode.bankobot.utils

import dev.kord.common.Color

/**
 * Wrapper for [Discordapp.com/branding][https://discordapp.com/branding] colors and some other colors:
 */
@Suppress("KDocMissingDocumentation", "unused")
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
