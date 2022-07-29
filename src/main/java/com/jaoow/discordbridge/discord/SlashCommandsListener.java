package com.jaoow.discordbridge.discord;

import com.jaoow.discordbridge.BridgeAPI;
import com.jaoow.discordbridge.DiscordLink;
import com.jaoow.discordbridge.utils.colors.Discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.apache.commons.lang.RandomStringUtils;

import java.util.logging.Logger;

public class SlashCommandsListener extends ListenerAdapter {

    private final Logger logger = DiscordLink.getPluginLogger();
    private final BridgeAPI bridgeAPI = DiscordLink.getInstance().getBridgeAPI();


    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        // send thinking message
        event.deferReply(true).queue();
        logger.info(event.getUser().getAsTag() + " called discord command: " + event.getName());
        // switch for appropriate handler
        if (event.getName().equals("verify")) {
            onVerify(event);
        }
    }

    public void onVerify(SlashCommandEvent event) {
        // Check discord account not linked already
        Member eventMember = event.getMember();
        String caller_id = eventMember.getId();

        bridgeAPI.hasRegister(caller_id).whenComplete((result, throwable) -> {

            if (result) {
                sendUserError(event.getHook(), "You are already linked to a minecraft account.");
                return;
            }

            // everything in order, start linking
            eventMember.getUser().openPrivateChannel()
                    .queue((success) -> {

                                String code = RandomStringUtils.randomAlphanumeric(6);
                                EmbedBuilder eb = new EmbedBuilder()
                                        .setTitle("Linking started...")
                                        .setThumbnail(eventMember.getEffectiveAvatarUrl())
                                        .setColor(Discord.BLURPLE)
                                        .setDescription("Here's your linking code: **" + code + "**\nCopy paste this command in-game: '/discord link " + code + "'");

                                success.sendMessageEmbeds(eb.build())
                                        .queue(
                                                (message) -> {
                                                    bridgeAPI.injectMember(caller_id, code);
                                                    sendUserSuccess(event.getHook(), "Link process started, please check your DMs");
                                                },
                                                (error) -> sendUserError(event.getHook(), "You need to open your DMs to receive your code."));
                            },
                            (error) -> sendUserError(event.getHook(), "You need to open your DMs to receive your code."));

        });
    }

    public static void sendUserError(InteractionHook webhook, String msg) {
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Discord.RED)
                .setDescription(msg);

        webhook.sendMessageEmbeds(eb.build()).setEphemeral(true).queue();
    }

    public static void sendUserSuccess(InteractionHook webhook, String msg) {
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Discord.GREEN)
                .setDescription(msg);
        webhook.sendMessageEmbeds(eb.build()).setEphemeral(true).queue();
    }

    public void sendSQLError(InteractionHook webhook, String msg) {
        logger.warning("SQL ERROR: " + msg);
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Discord.YELLOW)
                .setTitle("SQL ERROR")
                .setDescription(msg + "\nPlease notify an administrator.");
        webhook.sendMessageEmbeds(eb.build()).queue();
    }
}
