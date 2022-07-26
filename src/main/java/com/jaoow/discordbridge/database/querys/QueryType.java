package com.jaoow.discordbridge.database.querys;

import com.jaoow.discordbridge.config.Settings;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public enum QueryType {

    CREATE_TABLE("CREATE TABLE IF NOT EXISTS `" + Settings.TABLE_NAME + "` (`" + Settings.DISCORD_COLLUM + "` VARCHAR(128), `" + Settings.UUID_COLLUM + "` VARCHAR(64), `" + Settings.CODE_COLLUM + "` VARCHAR(64), `" + Settings.BOOSTER_COLLUM + "` TINYINT(1), `" + Settings.LINK_REWARD_COLLUM + "` BIGINT, `" + Settings.BOOSTER_REWARD_COLUMN + "` BIGINT, PRIMARY KEY (`" + Settings.DISCORD_COLLUM +"`) );"),

    SELECT("SELECT * FROM `" + Settings.TABLE_NAME + "`"),
    SELECT_FROM_UUID("SELECT * FROM `" + Settings.TABLE_NAME + "` WHERE `" + Settings.UUID_COLLUM + "` = ?"),
    SELECT_FROM_DISCORD("SELECT * FROM `" + Settings.TABLE_NAME + "` WHERE `" + Settings.DISCORD_COLLUM + "` = ?"),
    SELECT_FROM_CODE("SELECT * FROM `" + Settings.TABLE_NAME + "` WHERE `" + Settings.CODE_COLLUM + "` = ?"),

    ASSIGN_PLAYER("UPDATE `" + Settings.TABLE_NAME + "` SET `" + Settings.UUID_COLLUM + "` = ?, `" + Settings.CODE_COLLUM + "` = ? WHERE `" + Settings.CODE_COLLUM + "` = ?"),

    CLAIM_REWARD("UPDATE `" + Settings.TABLE_NAME + "` SET `" + Settings.LINK_REWARD_COLLUM + "` = ?, `" + Settings.BOOSTER_REWARD_COLUMN + "` = ?  WHERE `" + Settings.UUID_COLLUM + "` = ?"),

    INSERT_PLAYER("REPLACE INTO `" + Settings.TABLE_NAME + "`(" + Settings.DISCORD_COLLUM + ", " + Settings.UUID_COLLUM + ", " + Settings.CODE_COLLUM + ", " + Settings.BOOSTER_COLLUM + ", " + Settings.LINK_REWARD_COLLUM + ", " + Settings.BOOSTER_REWARD_COLUMN + ") VALUES(?, ?, ?, ?, ?, ?)"),
    DELETE_FROM_UUID("DELETE FROM `" + Settings.TABLE_NAME + "` WHERE `" + Settings.UUID_COLLUM + "` = ?");

    private final String query;

}
