package net.nullpointer.kreport.util

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import net.nullpointer.kreport.entity.AvatarContext
import net.nullpointer.kreport.entity.MessageContext
import net.nullpointer.kreport.entity.ReportContext

suspend fun JDA.uploadApplicationEmojis() {
    val exists = retrieveApplicationEmojis().await()
        .map { it.name }

    val actions = Resources.images("images")
        .filter { !exists.contains(it.first) }
        .map { (name, input) ->
            createApplicationEmoji(name, Icon.from(input))
        }

    if (actions.isNotEmpty()) RestAction.allOf(actions).await()
}

fun ISnowflake.toReportContext(): ReportContext {
    return when (this) {
        is Message -> MessageContext(
            message = contentDisplay,
            messageId = idLong,
            channelId = channel.idLong,
            guildId = guild.idLong,
            attachments = attachments.map { it.url }
        )

        is User -> AvatarContext(effectiveAvatarUrl)
        is Member -> AvatarContext(effectiveAvatarUrl)
        else -> error("Unknown type for context")
    }
}

inline fun CommandListUpdateAction.contextCommand(
    type: Command.Type,
    name: String,
    builder: CommandData.() -> Unit = {}
) = addCommands(Commands.context(type, name).apply { builder() })