package br.com.gmfonseca.music.application.command

import br.com.gmfonseca.DiscordApp
import br.com.gmfonseca.music.application.listener.TrackSchedulerListener
import br.com.gmfonseca.shared.command.Command
import br.com.gmfonseca.shared.command.CommandHandler
import br.com.gmfonseca.shared.util.EmbedMessage
import br.com.gmfonseca.shared.util.ext.connectVoice
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel

/**
 * Created by Gabriel Fonseca on 03/10/2020.
 */
@CommandHandler(name = "jump", aliases = ["j"])
class JumpCommand : Command() {

    override fun onCommand(message: Message, channel: TextChannel, args: List<String>): Boolean {
        val guildId = channel.guild.id
        val musicManager = DiscordApp.getMusicManager(guildId)
        val scheduler = musicManager.scheduler

        with(scheduler) {
            listener = TrackSchedulerListener(channel)
        }

        if (args.isEmpty()) {
            scheduler.skip()
        } else {
            val guild = channel.guild
            val voiceChannel = guild.voiceChannels.find { voiceChannel ->
                voiceChannel.members.find { it.user.idLong == message.idLong } != null
            }

            if (voiceChannel == null) {
                EmbedMessage.failure(
                    channel,
                    description = "Você não está conectado em um canal de voz!"
                )
            } else {
                // try to connect to voice channel if not connected before or user changed voice channel
                if (musicManager.voiceChannel?.id != voiceChannel.id) {
                    musicManager.voiceChannel = voiceChannel
                }

                guild.audioManager.connectVoice(channel, voiceChannel)

                // schedule jump music
                args.first().toIntOrNull()?.let {
                    scheduler.jump(it - 1)
                } ?: onWrongCommand(channel)
            }
        }

        return true
    }
}