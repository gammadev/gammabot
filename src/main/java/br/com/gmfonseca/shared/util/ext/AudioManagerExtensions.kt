package br.com.gmfonseca.shared.util.ext

import br.com.gmfonseca.DiscordApp
import br.com.gmfonseca.music.application.listener.TrackSchedulerListener
import br.com.gmfonseca.shared.util.EmbedMessage
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.managers.AudioManager
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by Gabriel Fonseca on 02/12/2020.
 */
fun AudioManager.connectVoice(textChannel: TextChannel, voiceChannel: VoiceChannel): Boolean {
    val musicManager = DiscordApp.getMusicManager(guild.id).apply {
        scheduler.listener = TrackSchedulerListener(textChannel)
    }

    sendingHandler = musicManager.audioSenderHandler

    return safetyConnectVoice(voiceChannel, onInsufficientPermission = {
        EmbedMessage.failure(
            textChannel,
            title = "Sem permissão",
            description = "Não tenho permissão para me conectar aos canais de voz."
        )
    })
}

fun AudioManager.safetyConnectVoice(channel: VoiceChannel, onInsufficientPermission: () -> Unit = {}): Boolean {
    try {
        openAudioConnection(channel)
        return true
    } catch (exception: InsufficientPermissionException) {
        Logger.getGlobal().log(
            Level.WARNING,
            "Insufficiente Permission on guild [name: ${channel.guild.name} | id: ${channel.guild.id}]"
        )
        onInsufficientPermission()
    } catch (throwable: Throwable) {
        Logger.getGlobal().log(
            Level.SEVERE,
            "Untreated exception: ${throwable.message}",
            throwable
        )
    }

    return false
}