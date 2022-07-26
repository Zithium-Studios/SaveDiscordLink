package com.jaoow.discordbridge.commands.subcommands;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jaoow.discordbridge.BridgeAPI;
import com.jaoow.discordbridge.DiscordLink;
import com.jaoow.discordbridge.config.Messages;
import com.jaoow.discordbridge.gui.RewardInventory;
import com.jaoow.discordbridge.model.UserData;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ClaimCommand implements SubCommand {

    private static final ConsoleCommandSender CONSOLE = Bukkit.getConsoleSender();
    private final BridgeAPI bridgeAPI = DiscordLink.getInstance().getBridgeAPI();
    private final LoadingCache<UUID, Optional<UserData>> dataCache = CacheBuilder.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build(new CacheLoader<UUID, Optional<UserData>>() {
                @Override
                public Optional<UserData> load(@NotNull UUID key) throws Exception {
                    return bridgeAPI.getUserData(key).get();
                }
            });


    @SneakyThrows
    @Override
    public void onCommand(CommandSender sender, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Player only!");
            return;
        }

        final Player player = (Player) sender;
        final Optional<UserData> dataOptional = dataCache.get(player.getUniqueId());

        if (!dataOptional.isPresent()) {
            player.sendMessage(Messages.UNLINKED.string);
            return;
        }

        UserData userData = dataOptional.get();
        new RewardInventory(bridgeAPI, userData).open(player);

    }

    @Override
    public String name() {
        return "claim";
    }

    @Override
    public String info() {
        return "Claim your reward";
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
