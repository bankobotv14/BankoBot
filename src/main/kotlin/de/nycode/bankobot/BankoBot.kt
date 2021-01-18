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

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import de.nycode.bankobot.config.Config
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.kord.x.commands.kord.bot
import dev.kord.x.commands.kord.model.prefix.kord
import dev.kord.x.commands.kord.model.prefix.mention
import dev.kord.x.commands.model.prefix.literal
import dev.kord.x.commands.model.prefix.or
import org.bson.UuidRepresentation
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

suspend fun main() {
    Kord(Config.DISCORD_TOKEN).login()
}

object BankoBot {

    private var initialized = false

    private lateinit var database: CoroutineDatabase
    lateinit var kord: Kord
        private set

    object Repositories {

    }

    suspend operator fun invoke() {
        require(!initialized) { "Cannot initialize bot twice" }
        initialized = true

        val client = KMongo.createClient(
            MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .applyConnectionString(ConnectionString(Config.MONGO_URL))
                .build()
        )
            .coroutine
        database = client.getDatabase(Config.MONGO_DATABASE)

        kord = Kord(Config.DISCORD_TOKEN)

        bot(kord) {
            prefix {
                kord {
                    mention() or literal("xd")
                }
            }
        }

        kord.login {
            status = PresenceStatus.DoNotDisturb
            playing("Starting ...")
        }
    }
}
