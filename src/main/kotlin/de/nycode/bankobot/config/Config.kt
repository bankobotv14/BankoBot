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

package de.nycode.bankobot.config

import ch.qos.logback.classic.Level
import de.nycode.bankobot.command.DebugErrorHandler
import de.nycode.bankobot.command.HastebinErrorHandler
import de.nycode.bankobot.command.permissions.DebugPermissionHandler
import de.nycode.bankobot.command.permissions.RolePermissionHandler
import dev.kord.common.entity.Snowflake
import io.ktor.http.*

object Config {

    val ENVIRONMENT: Environment by getEnv(default = Environment.PRODUCTION) {
        Environment.valueOf(
            it
        )
    }
    val LOG_LEVEL: Level by getEnv(default = Level.INFO) { Level.toLevel(it) }

    val HASTE_HOST: String by getEnv(default = "https://pasta.with-rice.by.devs-from.asia/")

    val SENTRY_TOKEN: String? by getEnv().optional()
    val DISCORD_TOKEN: String by getEnv()

    val DOCDEX_URL: Url by getEnv(default = Url("https://docs-repository.schlaubi.net/")) { Url(it) }

    val MONGO_DATABASE: String by getEnv()
    val MONGO_URL: String by getEnv()

    val GOOGLE_API_KEY: String by getEnv()
    val GOOGLE_CX_CODE: String by getEnv()

    val JDOODLE_SECRET: String by getEnv()
    val JDOODLE_ID: String by getEnv()
    val JDOODLE_REQUESTS_MAX: Int by getEnv(default = 200) { it.toInt() }

    val MODERATOR_ROLE: Snowflake? by getEnv { Snowflake(it) }.optional()
    val ADMIN_ROLE: Snowflake? by getEnv { Snowflake(it) }.optional()
}

/**
 * Different environments used to determine bot behavior.
 */
enum class Environment {
    /**
     * Used in production.
     *
     * @see HastebinErrorHandler
     * @see RolePermissionHandler
     */
    PRODUCTION,

    /**
     * Used whilst development.
     *
     * @see DebugErrorHandler
     * @see DebugPermissionHandler
     */
    DEVELOPMENT
}
