package com.jaoow.discordbridge.repo;

import com.jaoow.discordbridge.model.RewardItem;
import com.jaoow.discordbridge.model.RewardType;
import com.jaoow.discordbridge.utils.TimeUtils;
import com.jaoow.discordbridge.utils.item.ItemBuilder;
import com.jaoow.discordbridge.utils.item.SlotItem;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

@Getter
public class RewardRepository {

    private static RewardRepository singleton;
    private final Map<RewardType, RewardItem> rewardItemMap = new HashMap<>();

    public static RewardRepository get() {
        return singleton;
    }

    public RewardRepository() {
        singleton = this;
    }

    public void loadAll(Plugin plugin) {

        String[] paths = { "link", "booster" };
        rewardItemMap.clear();

        for (String path : paths) {

            ConfigurationSection section = plugin.getConfig().getConfigurationSection("rewards." + path);
            if (section == null) return;

            RewardType type = path.equals("link") ? RewardType.LINK : RewardType.BOOSTER;
            rewardItemMap.put(type, new RewardItem(
                    type,
                    new SlotItem(plugin.getConfig().getConfigurationSection("gui.items." + path)),
                    section.getStringList("commands"),
                    TimeUtils.getDuration(section.getString("delay")).toMillis()
            ));
        }

    }

}
