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

package de.nycode.bankobot.commands.dev

import de.nycode.bankobot.command.command
import de.nycode.bankobot.command.permissions.PermissionLevel
import de.nycode.bankobot.command.permissions.permission
import de.nycode.bankobot.command.slashcommands.disableSlashCommands
import de.nycode.bankobot.commands.BotOwnerModule
import de.nycode.bankobot.utils.EMBED_TITLE_MAX_LENGTH
import de.nycode.bankobot.utils.Embeds
import de.nycode.bankobot.utils.Embeds.editEmbed
import de.nycode.bankobot.utils.HastebinUtil
import de.nycode.bankobot.utils.doExpensiveTask
import dev.kord.x.commands.annotation.AutoWired
import dev.kord.x.commands.annotation.ModuleName
import dev.kord.x.commands.argument.text.StringArgument
import dev.kord.x.commands.model.command.invoke
import dev.kord.x.emoji.Emojis
import kotlinx.coroutines.Deferred
import javax.script.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

// Because we cannot opt in to kotlin experimental time
// we need this wrapper
@OptIn(ExperimentalTime::class)
private class TimeMarker(private val start: TimeMark) {
    private var end: Duration? = null

    @Suppress("unused") // Called in scripts
    fun markCompileEnd() {
        end = start.elapsedNow()
    }

    fun markRunEnd(): Pair<Duration, Duration?> {
        val compileEnd = end
        val runEnd = start.elapsedNow()
        return if (compileEnd != null) {
            compileEnd to (runEnd - compileEnd)
        } else {
            runEnd to null
        }
    }
}

@OptIn(ExperimentalTime::class)
@AutoWired
@ModuleName(BotOwnerModule)
fun evalCommand() = command("ev") {
    permission(PermissionLevel.BOT_OWNER)
    disableSlashCommands()

    invoke(StringArgument) { code ->
        doExpensiveTask {
            val engine = ScriptEngineManager().getEngineByName("kotlin")
            val start = TimeSource.Monotonic.markNow()
            val timeMarker = TimeMarker(start)
            engine.put("timeMarker", timeMarker)
            engine.put("krd_capture", kord)
            engine.put("ctx_capture", this@invoke)
            val res = try {
                //language=kotlin
                engine.eval(
                    """
                    import dev.kord.common.entity.*
                    import de.nycode.bankobot.*
                    import de.nycode.bankobot.utils.*
                    import de.nycode.bankobot.command.*
                    import kotlinx.coroutines.runBlocking

                    timeMarker.markCompileEnd()

                    val kord = krd_capture
                    val context = ctx_capture  
                    
                    runBlocking {
                        with(context) {
                            $code
                        }
                    }
                """.trimIndent()
                )
            } catch (e: ScriptException) {
                e.stackTraceToString()
            }
            val output = if (res is Deferred<*>) {
                res.await().toString()
            } else {
                res?.toString()
            }

            val (compileTime, runTime) = timeMarker.markRunEnd()

            val timeInfo = if (runTime != null) {
                "${Emojis.stopwatch}Compiled in $compileTime and ran in ${runTime}"
            } else {
                "${Emojis.stopwatch} Compilation failed after $compileTime"
            }

            editEmbed(
                Embeds.info(
                    timeInfo,
                    output?.uploadIfToLong()
                )
            )
        }
    }
}

private suspend fun String.uploadIfToLong(): String {
    return if (length > EMBED_TITLE_MAX_LENGTH) {
        HastebinUtil.postToHastebin(this)
    } else {
        "```$this```"
    }
}
