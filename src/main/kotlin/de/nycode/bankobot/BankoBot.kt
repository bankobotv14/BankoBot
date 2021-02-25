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
import com.mongodb.client.model.IndexOptions
import de.nycode.bankobot.command.*
import de.nycode.bankobot.command.permissions.DebugPermissionHandler
import de.nycode.bankobot.command.permissions.RolePermissionHandler
import de.nycode.bankobot.command.slashcommands.*
import de.nycode.bankobot.commands.tag.TagAction
import de.nycode.bankobot.commands.tag.TagEntry
import de.nycode.bankobot.config.Config
import de.nycode.bankobot.config.Environment
import de.nycode.bankobot.docdex.DocDex
import de.nycode.bankobot.docdex.DocumentationModule
import de.nycode.bankobot.listeners.autoUploadListener
import de.nycode.bankobot.listeners.selfMentionListener
import de.nycode.bankobot.serialization.LocalDateTimeSerializer
import de.nycode.bankobot.serialization.SnowflakeSerializer
import de.nycode.bankobot.twitch.twitchIntegration
import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.on
import dev.kord.x.commands.kord.bot
import dev.kord.x.commands.kord.model.prefix.kord
import dev.kord.x.commands.kord.model.prefix.mention
import dev.kord.x.commands.kord.model.processor.KordContext
import dev.kord.x.commands.model.module.forEachModule
import dev.kord.x.commands.model.prefix.or
import dev.kord.x.commands.model.processor.BaseEventHandler
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.util.*
import kapt.kotlin.generated.configure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.bson.UuidRepresentation
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.serialization.registerSerializer
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

object BankoBot : CoroutineScope {

    private var initialized = false
    override val coroutineContext: CoroutineContext = Dispatchers.IO + Job()

    var availableDocs: List<String>? = null
        private set

    private val logger = KotlinLogging.logger { }

    @Suppress("MagicNumber")
    @OptIn(KtorExperimentalAPI::class, ExperimentalTime::class)
    val httpClient = HttpClient(CIO) {
        install(JsonFeature) {
            val json = kotlinx.serialization.json.Json {
                serializersModule = DocumentationModule
                ignoreUnknownKeys = true
            }
            serializer = KotlinxSerializer(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10.seconds.toLongMilliseconds()
            connectTimeoutMillis = 10.seconds.toLongMilliseconds()
        }
    }

    val repositories = Repositories()
    private lateinit var database: CoroutineDatabase
    lateinit var kord: Kord
        private set

    val permissionHandler =
        if (Config.ENVIRONMENT == Environment.PRODUCTION) RolePermissionHandler else DebugPermissionHandler

    class Repositories internal constructor() {
        lateinit var blacklist: CoroutineCollection<BlacklistEntry>
        lateinit var tag: CoroutineCollection<TagEntry>
        lateinit var tagActions: CoroutineCollection<TagAction>
    }

    suspend operator fun invoke() {
        require(!initialized) { "Cannot initialize bot twice" }
        initialized = true
        loadJavadocs()

        initializeDatabase()

        kord = Kord(Config.DISCORD_TOKEN)

        initializeKord()
    }

    private suspend fun loadJavadocs(): Unit = coroutineScope {
        runCatching {
            availableDocs = DocDex.allJavadocs().map { it.names }.flatten()
        }
            .onSuccess {
                logger.info("Successfully loaded available javadocs! (${availableDocs?.size})")
            }
            .onFailure {
                logger.error("Unable to load available javadocs!")
            }
    }

    @Suppress("MagicNumber")
    private suspend fun initializeDatabase() {
        registerSerializer(SnowflakeSerializer)
        registerSerializer(LocalDateTimeSerializer)

        val client = KMongo.createClient(
            MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .applyConnectionString(ConnectionString(Config.MONGO_URL))
                .build()
        ).coroutine
        database = client.getDatabase(Config.MONGO_DATABASE)

        repositories.blacklist = database.getCollection("blacklist")
        repositories.tag = database.getCollection("tag")
        repositories.tag.ensureIndex(TagEntry::name, indexOptions = IndexOptions().unique(true))
        repositories.tagActions = database.getCollection("tagActions")
    }

    @OptIn(KordPreview::class)
    private suspend fun initializeKord() {
        bot(kord) {
            configure() // add annotation processed commands
            prefix {
                kord {
                    literal("xd") or literal("!") or mention()
                }
            }

            eventFilters.add(BlacklistEnforcer)
            preconditions.add(permissionHandler)
            eventHandlers[KordContext] = BaseEventHandler(
                KordContext,
                BankoBotContextConverter,
                if (Config.ENVIRONMENT == Environment.PRODUCTION) HastebinErrorHandler else DebugErrorHandler
            )

            eventSources += InteractionEventSource(kord)

            dispatcher = Dispatchers.IO

            eventHandlers[InteractionContext] = InteractionEventHandler

            if (Config.REGISTER_SLASH_COMMANDS) {
                moduleModifiers += forEachModule {
                    commands.values
                        .asSequence()
                        .filter { it.supportsSlashCommands }
                        .forEach { kord.registerCommand(it) }
                }
            }

            // listeners
            kord.apply {
                selfMentionListener()
                autoUploadListener()
                with(BankoBotContextConverter) {
                    messageDeleteListener()
                }
                if (Config.ENABLE_TWITCH_WEBHOOKS) {
                    on<ReadyEvent> {
                        twitchIntegration()
                    }
                }
            }
        }
    }
}
