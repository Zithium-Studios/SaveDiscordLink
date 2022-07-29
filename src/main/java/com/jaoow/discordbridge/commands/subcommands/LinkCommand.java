package com.jaoow.discordbridge.commands.subcommands;

import com.jaoow.discordbridge.BridgeAPI;
import com.jaoow.discordbridge.DiscordLink;
import com.jaoow.discordbridge.config.Messages;
import com.jaoow.discordbridge.config.Settings;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class LinkCommand implements SubCommand {

    private final BridgeAPI bridgeAPI = DiscordLink.getInstance().getBridgeAPI();

    @SneakyThrows
    @Override
    public void onCommand(CommandSender sender, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Player only!");
            return;
        }

        if (args.length != 1) {
            sender.sendMessage("§cInvalid syntax. Use /discord " + name() + " " + usage());
            return;
        }

        String codeStr = args[0];
        Player player = (Player) sender;

        bridgeAPI.isAssigned(player).whenCompleteAsync((assigned, throwable_) -> {

            if (assigned) {
                player.sendMessage(Messages.ALREADY_LINKED.string);
                return;
            }

            bridgeAPI.assignPlayer(player, codeStr, id -> {
                        Guild guild = DiscordLink.getInstance().getJda().getGuilds().get(0);
                        Role roleById = guild.getRoleById(Settings.LINKED_ROLE);
                        if (roleById != null) {
                            guild.addRoleToMember(id, roleById).queue();
                        }
                    })
                    .whenComplete((response, throwable) -> {

                        if (throwable != null) {
                            throwable.printStackTrace();
                            return;
                        }

                        Bukkit.getScheduler().runTask(DiscordLink.getInstance(), () -> {
                            for (String command : Settings.LINKED_COMMANDS) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                        command.replace("{player}", player.getName()));
                            }
                        });

                        switch (response) {
                            case SUCCESS:
                                player.sendMessage(Messages.SUCCESSFULLY_LINKED.string);
                                break;
                            case NOT_FOUND:
                                player.sendMessage(Messages.CODE_NOT_FOUND.string);
                                break;
                        }
                    });
        });
    }

    @Override
    public String name() {
        return "link";
    }

    @Override
    public String info() {
        return "Link your account to discord";
    }

    @Override
    public String usage() {
        return "<code>";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}