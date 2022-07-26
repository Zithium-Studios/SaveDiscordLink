package com.jaoow.discordbridge.commands.subcommands;

import com.jaoow.discordbridge.BridgeAPI;
import com.jaoow.discordbridge.DiscordLink;
import com.jaoow.discordbridge.config.Settings;
import com.jaoow.discordbridge.database.Database;
import com.jaoow.discordbridge.database.mysql.MySQLDatabase;
import com.jaoow.discordbridge.database.mysql.SQLiteDatabase;
import com.jaoow.discordbridge.database.querys.Query;
import com.jaoow.discordbridge.database.querys.QueryType;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.sql.SQLException;

public class MigrateCommand implements SubCommand {

    private final BridgeAPI bridgeAPI = DiscordLink.getInstance().getBridgeAPI();

    @SneakyThrows
    @Override
    public void onCommand(CommandSender sender, String[] args) {

        if (!sender.hasPermission("discord.admin")) {
            sender.sendMessage("§cAccess denied.");
            return;
        }

        if (args.length != 1) {
            sender.sendMessage("§cInvalid syntax. Use /discord " + name() + " " + usage());
            return;
        }

        String from = args[0];
        Database database;

        switch (from.toLowerCase()) {
            case "mysql": {
                database = new MySQLDatabase(Settings.HOST, Settings.USERNAME, Settings.PASSWORD, Settings.PORT, Settings.DATABASE);
                break;
            }
            case "sqlite": {
                database = new SQLiteDatabase(new File(DiscordLink.getInstance().getDataFolder(), "database.db").getPath());
                break;
            }
            default: {
                sender.sendMessage(ChatColor.RED + "Invalid database type. " + ChatColor.GRAY + "(MySQL and SQLite)");
                return;
            }
        }

        try {
            database.tryConnection();
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Couldn't connect to the database!");
            ex.printStackTrace();
            return;
        }

        if ((database instanceof MySQLDatabase && bridgeAPI.getDatabase() instanceof MySQLDatabase)
                || (database instanceof SQLiteDatabase && bridgeAPI.getDatabase() instanceof SQLiteDatabase)) {
            sender.sendMessage(ChatColor.RED + "The old database needs to be different from the current active one");
            return;
        }

        BridgeAPI oldBridgeAPI = new BridgeAPI(database);
        sender.sendMessage(ChatColor.GREEN + "Starting the Migration Process");
        oldBridgeAPI.consumeAll(resultSet -> {
            try {
                Query injectQuery = Query.builder()
                        .connection(bridgeAPI.getDatabase().getConnection())
                        .query(QueryType.INSERT_PLAYER)
                        .parameters(new Object[]{
                                resultSet.getString(Settings.DISCORD_COLLUM),
                                resultSet.getString(Settings.UUID_COLLUM),
                                resultSet.getString(Settings.CODE_COLLUM),
                                resultSet.getString(Settings.BOOSTER_COLLUM),
                                resultSet.getString(Settings.LINK_REWARD_COLLUM),
                                resultSet.getString(Settings.BOOSTER_REWARD_COLUMN),
                        })
                        .build();

                bridgeAPI.getDatabase().executeUpdate(injectQuery);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).whenComplete((unused, throwable) -> sender.sendMessage(ChatColor.GREEN + "Migration successfully completed."));
    }

    @Override
    public String name() {
        return "migrate";
    }

    @Override
    public String info() {
        return "Migrate data from old database";
    }

    @Override
    public String usage() {
        return "<database>";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}