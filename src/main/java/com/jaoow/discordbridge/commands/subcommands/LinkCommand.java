package com.jaoow.discordbridge.commands.subcommands;

import com.jaoow.discordbridge.BridgeAPI;
import com.jaoow.discordbridge.DiscordLink;
import com.jaoow.discordbridge.config.Messages;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

            bridgeAPI.assignPlayer(player, codeStr)
                    .whenCompleteAsync((response, throwable) -> {

                        if (throwable != null) {
                            throwable.printStackTrace();
                            return;
                        }

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