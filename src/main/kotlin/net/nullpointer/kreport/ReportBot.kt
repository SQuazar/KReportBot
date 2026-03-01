package net.nullpointer.kreport

import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.nullpointer.kreport.handler.ReactionHandler
import net.nullpointer.kreport.registry.CommandRegistry
import net.nullpointer.kreport.repository.ReportDataRepository
import net.nullpointer.kreport.service.ReportService
import net.nullpointer.kreport.util.ACCEPT_EMOJI
import net.nullpointer.kreport.util.DELETE_EMOJI
import net.nullpointer.kreport.util.DENIED_EMOJI
import net.nullpointer.kreport.util.uploadApplicationEmojis
import org.bson.codecs.configuration.CodecRegistries

class ReportBot(
    private val token: String =
        System.getenv("BOT_TOKEN") ?: error("BOT_TOKEN is required"),
) {
    private lateinit var reportDataRepository: ReportDataRepository

    suspend fun start() {
        setupMongo()

        val jda = light(token) {
            intents += GatewayIntent.GUILD_MEMBERS
            intents += GatewayIntent.MESSAGE_CONTENT
            setStatus(OnlineStatus.OFFLINE)
        }

        val reportService = ReportService(reportDataRepository)
        val reactionHandler = ReactionHandler(reportService)

        jda.listener<MessageReactionAddEvent> { event -> reactionHandler.handle(event) }
        jda.uploadApplicationEmojis()

        jda.retrieveApplicationEmojis().queue { emojis ->
            ACCEPT_EMOJI = emojis.first { it.name == "accept" }
            DENIED_EMOJI = emojis.first { it.name == "denied" }
            DELETE_EMOJI = emojis.first { it.name == "delete" }
        }

        val commandRegistry = CommandRegistry(jda, reportService)
        commandRegistry.registerCommands()
    }

    private fun setupMongo() {
        val codec = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry()
        )

        val mongo = MongoClient.create(System.getenv("MONGO_URI") ?: error("MONGO_URI is required"))
            .getDatabase(System.getenv("MONGO_DB") ?: error("MONGO_DB is required"))
            .withCodecRegistry(codec)
        reportDataRepository = ReportDataRepository(mongo)
    }
}