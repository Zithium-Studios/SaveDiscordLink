package com.jaoow.discordbridge.commands;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jaoow.discordbridge.BridgeAPI;
import com.jaoow.discordbridge.DiscordLink;
import com.jaoow.discordbridge.config.Messages;
import com.jaoow.discordbridge.gui.RewardInventory;
import com.jaoow.discordbridge.model.UserData;
import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RewardCommand implements CommandExecutor {

    private static final BridgeAPI bridgeAPI = DiscordLink.getInstance().getBridgeAPI();
    public static final LoadingCache<UUID, Optional<UserData>> DATA_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build(new CacheLoader<UUID, Optional<UserData>>() {
                @Override
                public Optional<UserData> load(@NotNull UUID key) throws Exception {
                    return bridgeAPI.getUserData(key).get();
                }
            });


    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Player only!");
            return false;
        }

        final Player player = (Player) sender;
        final Optional<UserData> dataOptional = DATA_CACHE.get(player.getUniqueId());

        if (!dataOptional.isPresent()) {
            player.sendMessage(Messages.UNLINKED.string);
            return false;
        }

        UserData userData = dataOptional.get();
        new RewardInventory(bridgeAPI, userData).open(player);
        return true;
    }
}
