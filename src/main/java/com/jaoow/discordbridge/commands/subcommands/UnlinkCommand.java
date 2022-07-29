package com.jaoow.discordbridge.commands.subcommands;

import com.jaoow.discordbridge.BridgeAPI;
import com.jaoow.discordbridge.DiscordLink;
import com.jaoow.discordbridge.commands.RewardCommand;
import com.jaoow.discordbridge.config.Messages;
import com.jaoow.discordbridge.config.Settings;
import com.jaoow.discordbridge.database.querys.Response;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class UnlinkCommand implements SubCommand {

    private final BridgeAPI bridgeAPI = DiscordLink.getInstance().getBridgeAPI();

    @SneakyThrows
    @Override
    public void onCommand(CommandSender sender, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Player only!");
            return;
        }


        Player player = (Player) sender;
        bridgeAPI.isAssigned(player).whenCompleteAsync((assigned, throwable_) -> {

            if (!assigned) {
                player.sendMessage(Messages.UNLINKED.string);
                return;
            }

            RewardCommand.DATA_CACHE.invalidate(player.getUniqueId());
            bridgeAPI.getDiscordIdByPlayer(player).whenComplete((s, throwable) -> {
               s.ifPresent(id -> {
                   Guild guild = DiscordLink.getInstance().getJda().getGuilds().get(0);
                   Role roleById = guild.getRoleById(Settings.LINKED_ROLE);
                   if (roleById != null) {
                       guild.removeRoleFromMember(id, roleById).queue();
                   }
               });
            });
            bridgeAPI.deletePlayer(player.getUniqueId().toString()).whenCompleteAsync((response, throwable) -> {

                if (throwable != null) {
                    throwable.printStackTrace();
                    return;
                }

                Bukkit.getScheduler().runTask(DiscordLink.getInstance(), () -> {
                    for (String command : Settings.UNLINKED_COMMANDS) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                command.replace("{player}", player.getName()));
                    }
                });

                if (response == Response.SUCCESS) {
                    player.sendMessage(Messages.SUCCESSFULLY_UNLINKED.string);
                } else {
                    player.sendMessage(ChatColor.RED + "Error!");
                }
            });
        });
    }

    @Override
    public String name() {
        return "unlink";
    }

    @Override
    public String info() {
        return "Unlink your account to discord";
    }

    @Override
    public String usage() {
        return "";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}