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
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import kotlin.time.*

/**
 * Wrapper class to measure time in scripts
 */
@OptIn(ExperimentalTime::class)
class TimeMarker(private val start: TimeMark) {
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
                eval(engine, code)
            } catch (e: TimeoutCancellationException) {
                sendResponse(
                    Embeds.info(
                        "Das hat zu lange gedauert",
                        "Dieser Code hat länger als eine minute gebraucht"
                    )
                )
                return@doExpensiveTask
            }
            val output = if (res is Deferred<*>) {
                res.await().toString()
            } else {
                res?.toString()
            }

            val (compileTime, runTime) = timeMarker.markRunEnd()

            val timeInfo = if (runTime != null) {
                "${Emojis.stopwatch}Compiled in $compileTime and ran in $runTime"
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

@OptIn(ExperimentalTime::class)
private suspend fun eval(engine: ScriptEngine, code: String): Any? {
    return withTimeout(1.minutes) {
        try {
            //language=kotlin
            engine.eval(
                """
                    import dev.kord.common.entity.*
                    import de.nycode.bankobot.*
                    import de.nycode.bankobot.utils.*
                    import de.nycode.bankobot.command.*
                    import de.nycode.bankobot.commands.dev.TimeMarker
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
    }
}

private suspend fun String.uploadIfToLong(): String {
    return if (length > EMBED_TITLE_MAX_LENGTH) {
        HastebinUtil.postToHastebin(this)
    } else {
        "```$this```"
    }
}
