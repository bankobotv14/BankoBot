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

package de.nycode.bankobot

import ch.qos.logback.classic.Logger
import de.nycode.bankobot.config.Config
import de.nycode.bankobot.config.Environment
import io.sentry.Sentry
import io.sentry.SentryOptions
import mu.KotlinLogging
import org.slf4j.LoggerFactory

private val LOG = KotlinLogging.logger { }

suspend fun main() {
    initializeSentry()
    initializeLogging()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        LOG.error(throwable) { "Got unhandled error on $thread" }
    }
    BankoBot()
}

private fun initializeLogging() {
    val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
    rootLogger.level = Config.LOG_LEVEL
}

private fun initializeSentry() {
    val configure: (SentryOptions) -> Unit =
        if (Config.ENVIRONMENT != Environment.DEVELOPMENT) {
            { it.dsn = Config.SENTRY_TOKEN; }
        } else {
            { it.dsn = "" }
        }

    Sentry.init(configure)
}
