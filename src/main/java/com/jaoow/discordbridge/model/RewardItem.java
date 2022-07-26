package com.jaoow.discordbridge.model;

import com.jaoow.discordbridge.utils.item.SlotItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
@AllArgsConstructor
public class RewardItem {

    private final RewardType type;
    private final SlotItem icon;
    private final List<String> commands;
    private final long delay;


}
