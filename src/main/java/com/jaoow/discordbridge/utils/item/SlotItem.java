package com.jaoow.discordbridge.utils.item;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Class to make item with slot defined
 */
public class SlotItem {

    private final ItemBuilder builder;
    private final int slot;

    public SlotItem(ConfigurationSection section) {
        this.builder = ItemBuilder.fromSection(section);
        this.slot = section.getInt("slot", 0);
    }

    public SlotItem(ConfigurationSection parent, String path) {
        this.builder = ItemBuilder.fromSection(parent, path);
        this.slot = parent.getInt(path + ".slot", 0);
    }

    public ItemBuilder builder() {
        return builder.asCopy();
    }

    public int slot() {
        return slot;
    }
}
