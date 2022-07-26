package com.jaoow.discordbridge.gui;

import com.jaoow.discordbridge.BridgeAPI;
import com.jaoow.discordbridge.DiscordLink;
import com.jaoow.discordbridge.config.Messages;
import com.jaoow.discordbridge.model.RewardType;
import com.jaoow.discordbridge.model.UserData;
import com.jaoow.discordbridge.repo.RewardRepository;
import com.jaoow.discordbridge.utils.TimeUtils;
import com.jaoow.discordbridge.utils.item.InventoryGUI;
import com.jaoow.discordbridge.utils.item.ItemBuilder;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class RewardInventory implements InventoryGUI.InventoryProvider {

    private final BridgeAPI bridgeAPI;
    private final UserData userData;


    @Override
    public void initialize(Player player, InventoryGUI builder) {

        RewardRepository.get().getRewardItemMap().forEach((type, rewardItem) -> {

            if (type == RewardType.BOOSTER) {
                if (!userData.getBoosterStatus().get()) {
                    builder.appendItem(rewardItem.getIcon().slot(), InventoryGUI.ClickableItem.of(Info.notBoosterItem.build(), action -> {
                        player.sendMessage(Messages.CANNOT_CLAIM.string);
                    }));
                    return;
                }
            }

            long lastOpen = userData.getLastOpenFor(type);
            long nextOpen = lastOpen + rewardItem.getDelay() - System.currentTimeMillis();
            String nextTime = TimeUtils.formatTime(nextOpen);

            builder.appendItem(rewardItem.getIcon().slot(), InventoryGUI.ClickableItem.of(rewardItem.getIcon().builder()
                    .build(
                            new String[]{"{time}"},
                            new String[]{nextTime}
                    ), event -> {

                if (nextOpen <= 0) {

                    switch (type) {
                        case LINK:
                            player.sendMessage(Messages.ON_CLAIM_LINK.string);
                            break;
                        case BOOSTER:
                            player.sendMessage(Messages.ON_CLAIM_BOOSTER.string);
                            break;
                    }

                    player.closeInventory();
                    for (String command : rewardItem.getCommands()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                command.replace("{player}", player.getName()));
                    }

                    userData.getLastOpening().put(type, System.currentTimeMillis());
                    bridgeAPI.updateUserData(player, userData);
                } else {
                    player.sendMessage(Messages.CANNOT_CLAIM.string);
                }

            }));

        });

    }

    public void open(Player player) {
        InventoryGUI.builder().name(Info.TITLE)
                .size(InventoryGUI.InventorySize.fromInt(Info.SIZE))
                .provider(this).build().open(player);
    }


   static class Info {

        private static final String TITLE = DiscordLink.getInstance().getConfig().getString("gui.title");
        private static final int SIZE = DiscordLink.getInstance().getConfig().getInt("gui.size");

        private static final ItemBuilder notBoosterItem = ItemBuilder.fromSection(DiscordLink.getInstance().getConfig().getConfigurationSection("gui.items.not-booster"));

    }

}
