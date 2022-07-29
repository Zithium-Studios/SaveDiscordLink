package com.jaoow.discordbridge;

import com.jaoow.discordbridge.commands.CommandManager;
import com.jaoow.discordbridge.commands.RewardCommand;
import com.jaoow.discordbridge.commands.subcommands.*;
import com.jaoow.discordbridge.database.Database;
import com.jaoow.discordbridge.database.mysql.MySQLDatabase;
import com.jaoow.discordbridge.config.Messages;
import com.jaoow.discordbridge.config.Settings;
import com.jaoow.discordbridge.database.mysql.SQLiteDatabase;
import com.jaoow.discordbridge.discord.BotInit;
import com.jaoow.discordbridge.repo.RewardRepository;
import com.jaoow.discordbridge.utils.item.InventoryGUI;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.logging.Logger;

public final class DiscordLink extends JavaPlugin {

    private @Getter
    static Logger pluginLogger;

    private @Getter
    static DiscordLink instance;

    private @Getter
    static RewardRepository rewardRepository;

    private @Getter
    Database database;

    private @Getter
    BridgeAPI bridgeAPI;

    private @Getter
    JDA jda;

    public static void prematureDisable() {
        pluginLogger.warning("Premature disable");
        Bukkit.getPluginManager().disablePlugin(instance);
    }

    @Override
    public void onEnable() {

        instance = this;
        pluginLogger = this.getLogger();

        saveDefaultConfig();
        Messages.loadAll(getConfig());

        try {
            if (Settings.MYSQL_ENABLED) {
                this.database = new MySQLDatabase(Settings.HOST, Settings.USERNAME, Settings.PASSWORD, Settings.PORT, Settings.DATABASE);
            } else {
                this.database = new SQLiteDatabase(new File(this.getDataFolder(), "database.db").getPath());
            }
            this.database.tryConnection();
        } catch (Exception ex) {
            pluginLogger.severe("Couldn't connect to the database!");
            Bukkit.getPluginManager().disablePlugin(this);
            ex.printStackTrace();
            return;
        }

        if (getConfig().getBoolean("enable-bot", true)) {
            // try to open discord gateway
            try {
                /* TODO: Figure out needed GatewayIntents
                 *   Needed OAuth2 scopes: bot, applications.commands
                 *   Bot permissions int: 413122553856
                 */
                this.jda = JDABuilder.createLight(getConfig().getString("token"))
                        .addEventListeners(new BotInit())
                        .setActivity(Activity.playing(getConfig().getString("bot-status")))
                        .build();
            } catch (LoginException e) {
                // disable plugin on Login failure
                pluginLogger.warning(e.toString());
                prematureDisable();
            }
        }


        rewardRepository = new RewardRepository();
        rewardRepository.loadAll(this);

        this.bridgeAPI = new BridgeAPI(database);
        this.bridgeAPI.createTable();

        getServer().getPluginManager().registerEvents(new InventoryGUI.InventoryListener(), this);

        getCommand("rewards").setExecutor(new RewardCommand());
        new CommandManager(this, "discord", ((sender, subCommands) -> {
            sender.sendMessage(Messages.GENERAL.string);
        })).register(AdminCommand.class,  MigrateCommand.class, LinkCommand.class, UnlinkCommand.class, ReloadCommand.class);
    }

    @SneakyThrows
    @Override
    public void onDisable() {
        pluginLogger.info("Closing database connection");
        if (database != null) database.close();
        pluginLogger.info("Closing JDA gateway");
        if (jda != null) jda.shutdown();
        instance = null;
    }
}
