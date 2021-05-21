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

package de.nycode.bankobot

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.IndexOptions
import de.nycode.bankobot.autohelp.TagSupplier
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
import de.nycode.bankobot.docdex.htmlRenderer
import de.nycode.bankobot.listeners.autoUploadListener
import de.nycode.bankobot.listeners.lightshotListener
import de.nycode.bankobot.listeners.selfMentionListener
import de.nycode.bankobot.serialization.LocalDateTimeSerializer
import de.nycode.bankobot.serialization.SnowflakeSerializer
import de.nycode.bankobot.twitch.twitchIntegration
import de.nycode.bankobot.utils.Emotes
import de.nycode.bankobot.utils.afterAll
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.on
import dev.kord.x.commands.kord.BotBuilder
import dev.kord.x.commands.kord.model.prefix.mention
import dev.kord.x.commands.model.prefix.or
import dev.kord.x.commands.model.processor.BaseEventHandler
import dev.schlaubi.forp.analyze.client.RemoteStackTraceAnalyzer
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.util.*
import kapt.kotlin.generated.configure
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import me.schlaubi.autohelp.AutoHelp
import me.schlaubi.autohelp.kord.*
import mu.KotlinLogging
import org.bson.UuidRepresentation
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.serialization.registerSerializer
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

object BankoBot : CoroutineScope {

    private var initialized = false
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

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
            requestTimeoutMillis = Duration.seconds(10).inWholeMilliseconds
            connectTimeoutMillis = Duration.seconds(10).inWholeMilliseconds
        }
    }

    val repositories = Repositories()
    private lateinit var database: CoroutineDatabase
    lateinit var kord: Kord
        private set

    val permissionHandler =
        if (Config.ENVIRONMENT == Environment.PRODUCTION) RolePermissionHandler else DebugPermissionHandler

    lateinit var autoHelp: AutoHelp
        private set

    class Repositories internal constructor() {
        lateinit var blacklist: CoroutineCollection<BlacklistEntry>
        lateinit var tag: CoroutineCollection<TagEntry>
        lateinit var tagActions: CoroutineCollection<TagAction>
    }

    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke() {
        require(!initialized) { "Cannot initialize bot twice" }
        initialized = true
        loadJavadocs()

        coroutineScope {
            launch {
                initializeDatabase()
            }
        }

        kord = Kord(Config.DISCORD_TOKEN)
        val renderer = htmlRenderer
        autoHelp = me.schlaubi.autohelp.autoHelp {
            loadingEmote = Emotes.LOADING
            context<KordUpdateMessage> {
                kordEditEventSource(kord)
            }
            useKordMessageRenderer(kord)
            context<KordReceivedMessage> {
                kordEventSource(kord)
                filter {
                    it.kordMessage.author?.isBot != true && it.channelId in Config.AUTO_HELP_CHANNELS
                }
            }

            @Suppress("MagicNumber")
            cleanupTime = Duration.seconds(30)
            analyzer = RemoteStackTraceAnalyzer {
                serverUrl = Config.AUTO_HELP_SERVER
                authKey = Config.AUTO_HELP_KEY
                dispatcher = coroutineContext
            }

            dispatcher = coroutineContext

            tagSupplier(TagSupplier)

            htmlRenderer {
                renderer.convert(this)
            }
        }

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

    @Suppress("LongMethod")
    @OptIn(KordPreview::class)
    private suspend fun initializeKord() {
        BotBuilder(kord).processorBuilder.apply {
            configure() // add annotation processed commands
            prefix {
                bankoBot {
                    literal("xd") or literal("!") or mention()
                }
            }

            eventFilters.add(BlacklistEnforcer)
            preconditions.add(permissionHandler)
            eventHandlers[BankoBotContext] = BaseEventHandler(
                BankoBotContext,
                BankoBotContextConverter,
                if (Config.ENVIRONMENT == Environment.PRODUCTION) HastebinErrorHandler else DebugErrorHandler
            )

            eventSources += InteractionEventSource(kord)
            eventSources += MessageCommandEventSource(kord)

            dispatcher = Dispatchers.IO

            eventHandlers[InteractionContext] = InteractionEventHandler

            if (Config.REGISTER_SLASH_COMMANDS) {
                moduleModifiers += afterAll {
                    val commands =
                        asSequence()
                            .flatMap { it.commands.values.asSequence() }
                            .filter { it.supportsSlashCommands }
                            .map { it.toSlashCommand() }
                            .toList()

                    if (Config.ENVIRONMENT == Environment.DEVELOPMENT) {
                        println(kord.slashCommands.createGuildApplicationCommands(Config.DEV_GUILD_ID) {
                            commands.forEach {
                                with(it) { register() }
                            }
                        }.toList())
                    } else {
                        kord.slashCommands.createGlobalApplicationCommands {
                            commands.forEach {
                                with(it) { register() }
                            }
                        }
                    }
                }
            }

            // listeners
            kord.apply {
                selfMentionListener()
                autoUploadListener()
                lightshotListener()
                with(BankoBotContextConverter) {
                    messageDeleteListener()
                }
                if (Config.ENABLE_TWITCH_WEBHOOKS) {
                    on<ReadyEvent> {
                        twitchIntegration()
                    }
                }
            }
        }.build()

        kord.login {
            status = PresenceStatus.DoNotDisturb
            playing("Starting ...")
        }
    }
}
