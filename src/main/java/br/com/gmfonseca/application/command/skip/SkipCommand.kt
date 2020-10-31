package br.com.gmfonseca.application.command.skip

import br.com.gmfonseca.DiscordApp
import br.com.gmfonseca.application.command.Command
import br.com.gmfonseca.application.command.CommandHandler
import br.com.gmfonseca.application.listener.TrackSchedulerListener
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User

/**
 * Created by Gabriel Fonseca on 29/10/2020.
 */
@CommandHandler(name = "skip", aliases = ["s", "next", "n"])
class SkipCommand : Command() {
    override fun onCommand(author: User, channel: TextChannel, args: List<String>): Boolean {
        with(DiscordApp.getMusicManager(channel.guild.id).scheduler) {
            listener = TrackSchedulerListener(channel)
            skip()
        }

        return true
    }
}