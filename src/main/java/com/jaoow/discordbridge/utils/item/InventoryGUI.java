package com.jaoow.discordbridge.utils.item;

import com.google.common.collect.Maps;
import com.jaoow.discordbridge.utils.Utility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.IntPredicate;

/**
 * class to facilitate the construction of inventories.
 * remember to register inventory listener {@link InventoryListener}
 *
 * @author Jaoow
 * @version 1.0
 */
@Getter
public class InventoryGUI implements InventoryHolder {

    private final String name;
    private final InventorySize size;

    private final InventoryProvider provider;
    private final Map<Integer, ClickableItem> itemMapping = Maps.newHashMap();

    @Builder
    public InventoryGUI(String name, InventorySize size, InventoryProvider provider, BiConsumer<InventoryGUI, InventoryClickEvent> defaultAction, Inventory inventory) {
        this.name = name.replace('&', ChatColor.COLOR_CHAR);
        this.size = size;
        this.provider = provider;
        this.defaultAction = defaultAction;
        this.inventory = inventory;
    }

    @Builder
    public InventoryGUI(String name, int size, InventoryProvider provider, BiConsumer<InventoryGUI, InventoryClickEvent> defaultAction, Inventory inventory) {
        this.name = name.replace('&', ChatColor.COLOR_CHAR);
        this.size = InventorySize.fromInt(size);
        this.provider = provider;
        this.defaultAction = defaultAction;
        this.inventory = inventory;
    }


    /**
     * Action to be executed if player click on non-mapped item
     */
    private final BiConsumer<InventoryGUI, InventoryClickEvent> defaultAction;

    /**
     * Inventory (maybe null if not built)
     */
    private Inventory inventory;


    /**
     * Set the item of inventory
     *
     * @param slot the slot
     * @param item the clickable item
     * @return the inventory gui
     */
    public InventoryGUI appendItem(int slot, @NotNull ClickableItem item) {
        itemMapping.put(slot, item);
        return this;
    }

    /**
     * Set the item of inventory
     *
     * @param slot the slot
     * @param item the clickable item
     * @return the inventory gui
     */
    public InventoryGUI appendItem(int slot, @NotNull ItemStack item) {
        itemMapping.put(slot, ClickableItem.of(item));
        return this;
    }

    /**
     * Set the item of inventory
     *
     * @param scape the scape
     * @param items the clickable item list
     * @return the inventory gui
     */
    public InventoryGUI appendItems(IntPredicate scape, @NotNull List<ClickableItem> items) {
        int slot = 0;
        for (int index = 0; index < items.size(); slot++) {

            if (slot > this.getSize()) {
                break;
            }

            if (scape.test(slot)) {
                continue;
            }

            ClickableItem item = items.get(index);
            appendItem(slot, item);

            // Update Index;
            index += 1;
        }
        return this;
    }


    public void clear() {
        Utility.notNull(this.inventory, value -> {
            inventory.clear();
            itemMapping.clear();
        });
    }

    /**
     * Get raw inventory size
     *
     * @return the raw inventory size
     */
    public int getSize() {
        return size.getSize();
    }

    /**
     * get active viewer
     *
     * @return active viewer;
     */
    @Nullable
    public HumanEntity viewer() {
        try {
            return this.inventory.getViewers().get(0);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Get clickable item by slot
     *
     * @param slot slot to search the item
     * @return the clickable item {@link ClickableItem}
     */
    @Nullable
    public ClickableItem getBySlot(int slot) {
        return itemMapping.getOrDefault(slot, null);
    }

    /**
     * Format the inventory and add the items.
     *
     * @param player player to initialize inventory.
     * @return the built inventory
     */
    public Inventory build(Player player) {
        Utility.notNull(this.provider, "provider cannot be null.");
        Utility.notNull(this.inventory, this::clear);

        this.inventory = Utility.notNull(this.inventory, () -> Bukkit.createInventory(this, size.getSize(), name.replace('&', ChatColor.COLOR_CHAR)));
        this.provider.initialize(player, this);

        itemMapping.forEach((slot, value) -> inventory.setItem(slot, value.item));
        return inventory;
    }

    /**
     * Open inventory to player.
     *
     * @param player player to open inventory.
     */
    public void open(Player player) {
        player.openInventory(this.build(player));
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Consumer method to use as inventory listener
     *
     * @param event the event
     */
    private void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        if (event.getClickedInventory() == player.getOpenInventory().getTopInventory()) {
            Utility.notNull(this.getBySlot(event.getSlot()), (v) -> v.run(event));
            return;
        }

        Utility.notNull(getDefaultAction(), (action) -> action.accept(this, event));
    }

    @Getter
    @AllArgsConstructor
    public enum InventorySize {

        ONE_ROW(9),
        TWO_ROWS(18),
        THREE_ROWS(27),
        FOUR_ROWS(36),
        FIVE_ROWS(45),
        SIX_ROWS(54);

        private final int size;

        @NotNull
        public static InventorySize fromInt(int value) {
            switch (value) {
                case 1:
                    return ONE_ROW;
                case 2:
                    return TWO_ROWS;
                case 3:
                    return THREE_ROWS;
                case 4:
                    return FOUR_ROWS;
                case 5:
                    return FIVE_ROWS;
                default:
                    return SIX_ROWS;
            }
        }

        @NotNull
        public static InventorySize round(int items) {
            int value = Math.min(6, (int) Math.ceil(items / 9f));
            return fromInt(value);
        }
    }

    /**
     * A class to padronize the creation of inventory
     */
    public interface InventoryProvider {

        void initialize(Player player, InventoryGUI builder);

        void open(Player player);

    }

    /**
     * Class called when player click in a valid item of inventory
     */
    @FunctionalInterface
    public interface ClickAction {

        ClickAction EMPTY = event -> {
        };

        void run(InventoryClickEvent event);

    }

    /**
     * Class used to create clickable items.
     */
    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class ClickableItem {

        private final ItemStack item;
        private final ClickAction action;

        public static ClickableItem of(ItemStack item) {
            return new ClickableItem(item, ClickAction.EMPTY);
        }

        public void run(InventoryClickEvent event) {
            this.action.run(event);
        }
    }

    /**
     * Listener class needed for the implementation of the inventories
     */
    public static class InventoryListener implements Listener {

        public InventoryListener() {
        }

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (event.getInventory().getHolder() instanceof InventoryGUI) {
                InventoryGUI inventory = (InventoryGUI) event.getInventory().getHolder();
                inventory.onClick(event);
            }
        }
    }
}
