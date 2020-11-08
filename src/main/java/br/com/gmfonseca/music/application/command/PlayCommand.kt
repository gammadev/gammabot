package br.com.gmfonseca.music.application.command

import br.com.gmfonseca.DiscordApp
import br.com.gmfonseca.shared.command.Command
import br.com.gmfonseca.shared.command.CommandHandler
import br.com.gmfonseca.music.application.listener.TrackSchedulerListener
import br.com.gmfonseca.music.application.listener.YoutubeClientListener
import br.com.gmfonseca.music.business.client.YoutubeClient
import br.com.gmfonseca.shared.REGEX_YOUTUBE
import br.com.gmfonseca.shared.utils.EmbedMessage
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User

/**
 * Created by Gabriel Fonseca on 19/09/2020.
 */
@CommandHandler(name = "play", aliases = ["p"])
class PlayCommand : Command() {

    override fun onCommand(author: User, channel: TextChannel, args: List<String>): Boolean {
        if (args.isEmpty()) {
            onWrongCommand(channel, "<youtube-link>")
        } else {
            val guild = channel.guild
            val voiceChannel = guild.voiceChannels.find { voiceChannel ->
                voiceChannel.members.find { it.user.idLong == author.idLong } != null
            }

            if (voiceChannel == null) {
                EmbedMessage.failure(
                    channel,
                    description = "Você não está conectado em um canal de voz!"
                )
            } else {
                if (args.first().matches(Regex(REGEX_YOUTUBE))) {
                    YoutubeClient(YoutubeClientListener(channel)).download(args.first())
                } else {
//                    YoutubeClient(YoutubeClientListener(channel)).search(args.reduce { acc, cur -> "$acc $cur" })
                    EmbedMessage.info(
                        channel,
                        title = "Ainda não suportado!",
                        description = "Ainda não suportamos pesquisas, então por favor insira uma URL válida do Youtube."
                    )
                }

                with(guild.audioManager) {
                    val musicManager = DiscordApp.getMusicManager(guild.id).apply {
                        scheduler.listener = TrackSchedulerListener(channel)
                    }

                    sendingHandler = musicManager.audioSenderHandler
                    openAudioConnection(voiceChannel) // TODO: threat InsufficientPermissionException here
                }
            }
        }

        return true
    }
}