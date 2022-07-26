package com.jaoow.discordbridge.commands.subcommands;

import com.jaoow.discordbridge.BridgeAPI;
import com.jaoow.discordbridge.DiscordLink;
import lombok.var;
import org.bukkit.command.CommandSender;

import java.util.concurrent.atomic.AtomicReference;

public class AdminCommand implements SubCommand {

    private final BridgeAPI bridgeAPI = DiscordLink.getInstance().getBridgeAPI();

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        if (args.length == 0) {
            sender.sendMessage("§cInvalid syntax. Use /discord " + name() + " " + usage());
            return;
        }

        if (!sender.hasPermission("discord.admin")) {
            sender.sendMessage("§cAccess denied.");
            return;
        }

        String function = args[0];

        var uniqueId = new AtomicReference<>("");
        var discordId = new AtomicReference<>("");

        switch (function.toUpperCase()) {
            case "LINK":
                if (args.length < 3) {
                    sender.sendMessage("§cInvalid syntax. Use /discord " + name() + " " + usage());
                    return;
                }

                uniqueId.set(args[1]);
                discordId.set(args[2]);

                bridgeAPI.injectPlayer(discordId.get(), uniqueId.get())
                        .whenCompleteAsync((response, throwable) -> {

                            if (throwable != null) {
                                throwable.printStackTrace();
                                return;
                            }

                            switch (response) {
                                case SUCCESS:
                                    sender.sendMessage(String.format("§aEntry '%s: %s' was successfully inserted into Database", uniqueId.get(), discordId.get()));
                                    break;

                                case ERROR:
                                    sender.sendMessage("§cAn unexpected error occurred");
                                    break;
                            }
                        });

                break;
            case "UNLINK":

                if (args.length < 2) {
                    sender.sendMessage("§cInvalid syntax. Use /discord " + name() + " " + usage());
                    return;
                }

                uniqueId.set(args[1]);

                bridgeAPI.deletePlayer(uniqueId.get())
                        .whenCompleteAsync((response, throwable) -> {

                            if (throwable != null) {
                                throwable.printStackTrace();
                                return;
                            }


                            switch (response) {
                                case SUCCESS:
                                    sender.sendMessage(String.format("§aUUID '%s' was successfully removed from Database", uniqueId.get()));
                                    break;

                                case NOT_FOUND:
                                    sender.sendMessage("§cUUID not found in the database.");
                                    break;
                            }
                        });
                break;
            case "ADDTEST":

                if (args.length < 2) {
                    sender.sendMessage("§cInvalid syntax. Use /discord " + name() + " " + usage());
                    return;
                }

                discordId.set(args[1]);

                bridgeAPI.injectPlayer(discordId.get(), "null")
                        .whenCompleteAsync((response, throwable) -> {

                            if (throwable != null) {
                                throwable.printStackTrace();
                                return;
                            }
                            switch (response) {
                                case SUCCESS:
                                    sender.sendMessage(String.format("§aID '%s' was successfully inserted into the Database", discordId.get()));
                                    break;

                                case ERROR:
                                    sender.sendMessage("§cAn unexpected error occurred");
                                    break;
                            }
                        });
        }
    }

    @Override
    public String name() {
        return "admin";
    }

    @Override
    public String info() {
        return "Administrator Commands";
    }

    @Override
    public String usage() {
        return "<link|unlink|addtest> <uuid> [discord-id]";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
