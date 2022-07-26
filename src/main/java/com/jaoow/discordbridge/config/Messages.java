package com.jaoow.discordbridge.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

@Getter @RequiredArgsConstructor
public enum Messages {

    GENERAL("general"),
    UNLINKED("unlinked"),

    ON_CLAIM_LINK("on-claim-link"),
    ON_CLAIM_BOOSTER("on-claim-booster"),
    ON_CLAIM_BOTH("on-claim-both"),

    CANNOT_CLAIM("cannot-claim"),

    ALREADY_LINKED("already-linked"),
    CODE_NOT_FOUND("code-not-found"),
    SUCCESSFULLY_LINKED("successfully-linked"),
    SUCCESSFULLY_UNLINKED("successfully-unlinked")
    ;


    private final String path;
    public String string;

    @SuppressWarnings("unchecked")
    private static String toString(final Object obj) {
        List<String> list = (List<String>) obj;
        return list.stream().map(s -> s + " \n").collect(Collectors.joining());
    }

    public static void loadAll(FileConfiguration file) {
        for (Messages message : Messages.values()) {
            Object object = file.get("messages." + message.path);

            if (object instanceof List) {
                message.setString(toString(object));
            } else if (object instanceof String) {
                message.setString(object.toString());
            }
        }
    }

    public void setString(String string) {
        this.string = ChatColor.translateAlternateColorCodes('&', string);
    }
}
