package com.jaoow.discordbridge.commands.subcommands;

import com.jaoow.discordbridge.DiscordLink;
import com.jaoow.discordbridge.config.Messages;
import com.jaoow.discordbridge.repo.RewardRepository;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements SubCommand {

    @SneakyThrows
    @Override
    public void onCommand(CommandSender sender, String[] args) {

        if (!sender.hasPermission("discord.admin")) {
            sender.sendMessage("§cAccess denied.");
            return;
        }

        DiscordLink.getInstance().reloadConfig();
        Messages.loadAll(DiscordLink.getInstance().getConfig());
        RewardRepository.get().loadAll(DiscordLink.getInstance());
        sender.sendMessage(ChatColor.GREEN + "Messages and rewards successfully reloaded.");


    }

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public String info() {
        return "Reload configuration";
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