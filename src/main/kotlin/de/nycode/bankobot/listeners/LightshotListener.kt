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

package de.nycode.bankobot.listeners

import de.nycode.bankobot.utils.Embeds
import dev.kord.core.Kord
import dev.kord.core.behavior.reply
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on

// https://regex101.com/r/JEHxwG/1
private val lightshotRegex by lazy { "(?:http[s]?://)?(?:prnt\\.sc|prntscr.com)".toRegex() }

internal fun Kord.lightshotListener() = on<MessageCreateEvent> {
    if (message.content.contains(lightshotRegex)) {
        message.reply {
            embed = Embeds.info(
                "Lightshot erkannt!", """Du scheinst Lightshot zu verwenden.
                    |Aus vielen Gründen möchten wir dich bitten Lightshot nicht mehr zu verwenden.
                    |
                    |Eine deutlich bessere Alternative zu Lightshot ist [ShareX](https://getsharex.com)
                """.trimMargin()
            )
        }
    }
}
