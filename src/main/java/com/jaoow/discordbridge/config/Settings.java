package com.jaoow.discordbridge.config;

import com.jaoow.discordbridge.DiscordLink;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Settings {

    private final static FileConfiguration CONFIGURATION = DiscordLink.getInstance().getConfig();

    public static final String TABLE_NAME = CONFIGURATION.getString("table-name");
    public static final String LINKED_ROLE = CONFIGURATION.getString("linked-role", "");

    public static final List<String> LINKED_COMMANDS = CONFIGURATION.getStringList("commands.linked");
    public static final List<String> UNLINKED_COMMANDS = CONFIGURATION.getStringList("commands.unlinked");

    public static final String DISCORD_COLLUM = CONFIGURATION.getString("columns.discord");
    public static final String UUID_COLLUM = CONFIGURATION.getString("columns.uuid");
    public static final String CODE_COLLUM = CONFIGURATION.getString("columns.code");

    public static final String BOOSTER_COLLUM = CONFIGURATION.getString("columns.booster");
    public static final String LINK_REWARD_COLLUM = CONFIGURATION.getString("columns.link-reward");
    public static final String BOOSTER_REWARD_COLUMN = CONFIGURATION.getString("columns.booster-reward");

    public final static Boolean MYSQL_ENABLED = CONFIGURATION.getBoolean("mysql.enable");
    public final static String HOST = CONFIGURATION.getString("mysql.host");
    public final static Integer PORT = CONFIGURATION.getInt("mysql.port");
    public final static String DATABASE = CONFIGURATION.getString("mysql.database");
    public final static String USERNAME = CONFIGURATION.getString("mysql.username");
    public final static String PASSWORD = CONFIGURATION.getString("mysql.password");

}