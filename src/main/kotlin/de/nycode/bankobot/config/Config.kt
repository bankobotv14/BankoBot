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

package de.nycode.bankobot.config

import ch.qos.logback.classic.Level
import de.nycode.bankobot.command.DebugErrorHandler
import de.nycode.bankobot.command.HastebinErrorHandler
import de.nycode.bankobot.command.permissions.DebugPermissionHandler
import de.nycode.bankobot.command.permissions.RolePermissionHandler
import dev.kord.common.entity.Snowflake
import io.ktor.http.*

@Suppress("MagicNumber")
object Config {

    val AUTO_HELP_SERVER: Url by getEnv { Url(it) }
    val AUTO_HELP_KEY: String by getEnv()

    val AUTO_HELP_CHANNELS: List<Long> by getEnv(default = emptyList()) {
        it.split(", ").map { item -> item.toLong() }
    }

    val ENVIRONMENT: Environment by getEnv(default = Environment.PRODUCTION) {
        Environment.valueOf(it)
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

    val DEV_GUILD_ID: Snowflake by getEnv(default = Snowflake(803209056730349568L)) { Snowflake(it) }

    val TWITCH_CLIENT_ID: String by getEnv()
    val TWITCH_CLIENT_SECRET: String by getEnv()
    val TWITCH_CHANNEL: String by getEnv()

    val WEBHOOK_URL: String by getEnv()
    val WEBHOOK_SECRET: String by getEnv()

    /**
     * MathJS Evaluation Server used by the CalcExpression
     * https://mathjs.org/
     */
    val MATHJS_SERVER_URL: String by getEnv()

    /**
     * Used to disable registering slash commands in development
     */
    val REGISTER_SLASH_COMMANDS: Boolean by getEnv(default = true) { it.toBoolean() }

    /**
     * Used to disable or enable twitch webhooks
     */
    val ENABLE_TWITCH_WEBHOOKS: Boolean by getEnv(default = true) { it.toBoolean() }

    val LAVALINK_HOST: String? by getEnv().optional()
    val LAVALINK_PASSWORD: String? by getEnv().optional()
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
