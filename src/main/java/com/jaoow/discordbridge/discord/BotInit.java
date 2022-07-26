package com.jaoow.discordbridge.discord;

import com.jaoow.discordbridge.DiscordLink;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.logging.Logger;

public class BotInit  extends ListenerAdapter {
    private final Logger logger = DiscordLink.getPluginLogger();

    @Override
    public void onReady(ReadyEvent event) {
        // bot can only be connected to single server
        if (event.getGuildTotalCount() != 1) {
            logger.warning("Bot has to belong to EXACTLY one guild");
            // only singleton call
            // how do I get rid of it?
            // observer?
            DiscordLink.prematureDisable();
            return;
        }

        logger.info("Updating guild commands");
        Guild guild = event.getJDA().getGuilds().get(0);

        // update commands on server
        CommandListUpdateAction commands = guild.updateCommands();

        // build commands
        // /verify <username>
        CommandData verifyCmd = new CommandData("verify", "verify your minecraft account");

        // add commands
        commands.addCommands(verifyCmd).complete();
        // add command listener
        event.getJDA().addEventListener(new SlashCommandsListener());
        logger.info("Commands updated");
    }
}
