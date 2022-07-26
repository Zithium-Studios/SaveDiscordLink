package com.jaoow.discordbridge.utils.item;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Jaoow
 * @version 1.0
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ItemBuilder implements Cloneable {

    private static final Pattern COMPARE_WITH_PLACEHOLDERS = Pattern.compile("%[^]]*%");

    private final ItemStack item; // Final item.

    /**
     * Create a new ItemBuilder
     * object.
     *
     * @param item final item to add.
     */
    public ItemBuilder(ItemStack item) {
        Validate.notNull(item, "@Item cannot be null.");
        this.item = item;
    }

    /**
     * Create a new ItemBuilder
     * object.
     *
     * @param material material type to create.
     */
    public ItemBuilder(Material material) {
        this(material, 1);
    }

    /**
     * Create a new ItemBuilder
     * object.
     *
     * @param material material type to create.
     * @param amount   amount of items.
     */
    public ItemBuilder(Material material, int amount) {
        Validate.notNull(material, "@Material cannot be null.");

        if (StringUtils.equals(material.name(), "SKULL_ITEM")) {
            this.item = new ItemStack(material, amount, (short) 3);
        } else {
            this.item = new ItemStack(material, amount);
        }
    }

    /**
     * Create a new ItemBuilder
     * object.
     *
     * @param material   material type to create.
     * @param amount     amount of items.
     * @param durability item durability/data.
     */
    @Deprecated
    public ItemBuilder(Material material, int amount, short durability) {
        Validate.notNull(material, "@Material cannot be null.");

        if (StringUtils.equals(material.name(), "SKULL_ITEM") && durability != 3) {
            this.item = new ItemStack(material, amount, (short) 3);
        } else {
            item = new ItemStack(material, amount, durability);
        }
    }

    /**
     * Create a new ItemBuilder
     * from a ItemStack.
     *
     * @param item original item
     * @return {@link ItemBuilder}
     */
    public static ItemBuilder from(ItemStack item) {
        return new ItemBuilder(item);
    }

    /**
     * Create a new ItemBuilder
     * object.
     *
     * @param item material type to build.
     * @return {@link ItemBuilder}
     */
    public static ItemBuilder get(String item) {
        return new ItemBuilder(getMaterial(item));
    }

    /**
     * Builds a new ItemBuilder
     * object by the configuration
     * section item.
     *
     * @param section section to value the item information.
     * @return {@link ItemBuilder}
     */
    public static ItemBuilder fromSection(ConfigurationSection section, String path) {
        return fromSection(section.getConfigurationSection(path), null, null, null);
    }

    /**
     * Builds a new ItemBuilder
     * object by the configuration
     * section item.
     *
     * @param section section to value the item information.
     * @return {@link ItemBuilder}
     */
    public static ItemBuilder fromSection(ConfigurationSection section) {
        return fromSection(section, null, null, null);
    }

    /**
     * Builds a new {@link ItemBuilder}
     * object by the configuration
     * section item.
     *
     * @param section      section to value the item information.
     * @param placeholders the custom placeholders.
     * @param replaces     the placeholder replaces.
     * @return {@link ItemBuilder}
     */
    public static ItemBuilder fromSection(ConfigurationSection section, String[] placeholders, String[] replaces, String playerName) {
        Validate.notNull(section, "section cannot be null.");

        ItemBuilder builder;

        try {
            String type = StringUtils.replace(section.getString("material", "AIR"), " ", "").toUpperCase();
            int amount = section.contains("amount") ? section.getInt("Amount") : 1;

            if (StringUtils.contains(type, ":")) {
                String[] typeSplit = type.split(":");
                short durability = Short.parseShort(typeSplit[1]);

                builder = new ItemBuilder(getMaterial(typeSplit[0]), amount, durability);
            } else {
                builder = new ItemBuilder(getMaterial(type), amount);
            }

            if (section.contains("owner")) {
                String owner = section.getString("owner", "none");

                if (owner.length() <= 17) {
                    builder.setSkullOwner(StringUtils.replace(owner, "%player_name%", playerName == null ? "" : playerName));
                }
            }



            if (section.contains("name")) {
                String name = section.getString("name");
                builder.withName(applyPlaceholder(name, placeholders, replaces));
            }

            if (section.contains("color")) {
                int red = section.getInt("color.r");
                int green = section.getInt("color.g");
                int blue = section.getInt("color.b");
                builder.withColor(red, green, blue);
            }

            if (section.contains("lore")) {
                builder.withLore(section.getStringList("lore"), placeholders, replaces);
            }


            if (section.contains("flags")) {
                for (String str : section.getStringList("flags")) {
                    builder.withFlag(ItemFlag.valueOf(str));
                }
            }

            if (section.contains("glow") && section.getBoolean("glow")) {
                builder.setGlow();
            } else if (section.contains("enchants")) {
                for (String str : section.getStringList("enchants")) {
                    String enchantment = StringUtils.replace(str, " ", "");

                    try {
                        if (StringUtils.contains(enchantment, ",")) {
                            String[] enchantmentSplit = enchantment.split(",");
                            builder.withEnchantment(Enchantment.getByName(enchantmentSplit[0]), Integer.parseInt(enchantmentSplit[1]));
                        } else {
                            builder.withEnchantment(Enchantment.getByName(enchantment));
                        }
                    } catch (IndexOutOfBoundsException | NumberFormatException ex) {
                        // Could not found this enchantment.
                    }
                }
            }
        } catch (NullPointerException | NumberFormatException | IndexOutOfBoundsException ex) {
            return new ItemBuilder(Material.BARRIER).withName(ChatColor.RED + "Invalid Item").addLore("&7Where: " + section.getCurrentPath());
        }

        return builder;
    }

    @Nullable
    public static Material getMaterial(String type) {
        if (type == null || type.isEmpty()) return null;
        return Material.matchMaterial(type.toUpperCase());
    }

    private static String applyPlaceholder(String text, String[] placeholders, String[] replacers) {
        if (text == null || text.isEmpty()) return text;

        if (placeholders != null && placeholders.length > 0 && placeholders.length == replacers.length) {
            text = StringUtils.replaceEach(text, placeholders, replacers);
        }

        return text;
    }

    /**
     * This method will compare
     * two strings.
     * If this String has some
     * placeholders, @COMPARE_WITH_PLACEHOLDERS
     * will remove and then compare it.
     *
     * @param one This is the first String
     *            this String should be
     *            the default String.
     *            example: String str = Hello %player_name%.
     * @param two This is the second String
     *            this should be the
     *            replaced string.
     *            example: String str = Hello Jaoow.
     * @return If the Strings are equals.
     */
    private static Boolean isStringEquals(String one, String two) {
        if (one == null && two == null) return true;
        if (one == null || two == null) return false;
        if (StringUtils.equalsIgnoreCase(one, two)) return true;

        String regex = COMPARE_WITH_PLACEHOLDERS.matcher(one.replace('&', ChatColor.COLOR_CHAR)).replaceAll("(.*)");
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(two).matches();
    }

    private ItemBuilder setType(Material material) {
        this.item.setType(material);
        return this;
    }

    /**
     * Get display item name.
     *
     * @return the item display name
     */
    @Nullable
    public String getItemName() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getDisplayName();
    }

    /**
     * Get item lore.
     *
     * @return the item lore
     */
    @Nullable
    public List<String> getItemLore() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return new ArrayList<>();
        return meta.getLore();
    }


    /**
     * Set display item name.
     *
     * @param name the name.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withName(String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        meta.setDisplayName(name.replace('&', ChatColor.COLOR_CHAR));
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Set item lore.
     * <p>
     * Using the methods "withName" will
     * set the given name.
     * To add new lines on the
     * current item lore
     *
     * @param name         the name to set.
     * @param placeholders the custom placeholders.
     * @param replaces     the placeholder replaces.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withName(String name, String[] placeholders, String[] replaces) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || name == null) return this;

        meta.setDisplayName(applyPlaceholder(name, placeholders, replaces).replace('&', ChatColor.COLOR_CHAR));
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Set item lore.
     * <p>
     * Using the methods "withLore" will
     * set the given lore.
     * To add new lines on the
     * current item lore, use "addLore"
     * methods.
     *
     * @param lore the lore to set.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withLore(List<String> lore) {
        return withLore(lore, null, null);
    }

    /**
     * Set item lore.
     * <p>
     * Using the methods "withLore" will
     * set the given lore.
     * To add new lines on the
     * current item lore, use "addLore"
     * methods.
     *
     * @param lore the lore to set.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withLore(String[] lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || lore == null || lore.length <= 0) return this;

        List<String> ret = new ArrayList<>(lore.length);
        Collections.addAll(ret, lore);

        return withLore(ret, null, null);
    }

    /**
     * Set item lore.
     * <p>
     * Using the methods "withLore" will
     * set the given lore.
     * To add new lines on the
     * current item lore, use "addLore"
     * methods.
     *
     * @param lore         the lore to set.
     * @param placeholders the custom placeholders.
     * @param replaces     the placeholder replaces.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withLore(List<String> lore, String[] placeholders, String[] replaces) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || lore == null || lore.size() <= 0) return this;

        List<String> toAdd = new ArrayList<>(lore.size());

        for (String str : lore) {
            toAdd.add(applyPlaceholder(str, placeholders, replaces).replace('&', ChatColor.COLOR_CHAR));
        }

        meta.setLore(toAdd);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Add new lines to
     * the current item lore.
     *
     * @param lore lore lines to be added.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addLore(List<String> lore) {
        return addLore(lore, null, null);
    }

    /**
     * Add new lines to
     * the current item lore.
     *
     * @param lore line to be added.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addLore(String lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || lore == null || lore.isEmpty()) return this;

        return addLore(new String[]{lore});
    }

    /**
     * Add new lines to
     * the current item lore.
     *
     * @param lore lore lines to be added.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addLore(String[] lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || lore == null) return this;

        List<String> ret = new ArrayList<>(lore.length);

        Collections.addAll(ret, lore);

        return addLore(ret, null, null);
    }

    /**
     * Add new lines to
     * the current item lore.
     *
     * @param lore         lore lines to be added.
     * @param placeholders the custom placeholders.
     * @param replaces     the placeholder replaces
     * @return {@link ItemBuilder}
     */
    public ItemBuilder addLore(List<String> lore, String[] placeholders, String[] replaces) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || lore == null || lore.size() <= 0) return this;

        List<String> toAdd;

        if (item.hasItemMeta() && item.getItemMeta().getLore() != null) {
            List<String> oldLore = item.getItemMeta().getLore();
            toAdd = new ArrayList<>(oldLore.size() + lore.size());

            toAdd.addAll(oldLore);
        } else {
            toAdd = new ArrayList<>(lore.size());
        }

        for (String str : lore) {
            toAdd.add(applyPlaceholder(str, placeholders, replaces).replace('&', ChatColor.COLOR_CHAR));
        }

        meta.setLore(toAdd);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Remove a specific line
     * on the current item
     * lore.
     *
     * @param line text to be removed.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder removeLore(String line) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getLore() == null || line == null || line.isEmpty()) return this;

        List<String> toAdd = new ArrayList<>(meta.getLore());

        for (String str : meta.getLore()) {
            if (isStringEquals(line, str)) {
                toAdd.remove(str);
                break;
            }
        }

        meta.setLore(toAdd);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Remove a specific lines
     * on the current item
     * lore.
     *
     * @param lore texts to be removed.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder removeLore(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getLore() == null || lore == null || lore.size() <= 0) return this;

        List<String> toAdd = new ArrayList<>(meta.getLore());

        for (String values : lore) {
            for (String str : meta.getLore()) {
                if (isStringEquals(values, str)) {
                    toAdd.remove(str);
                    break;
                }
            }
        }

        meta.setLore(toAdd);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Remove a specific line
     * on the current item lore,
     * remove it by the index.
     *
     * @param index index to be removed.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder removeLore(int index) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getLore() == null) return this;

        List<String> toAdd = new ArrayList<>(meta.getLore());
        if (toAdd.size() < index) return this;
        toAdd.remove(index);

        meta.setLore(toAdd);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Replace placeholder in
     * the current item lore.
     *
     * @param placeholder placeholder to be replaced.
     * @param lines       lines to be injected
     * @return {@link ItemBuilder}
     */
    public ItemBuilder replaceLine(String placeholder, List<String> lines) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getLore() == null || placeholder == null || placeholder.isEmpty()) return this;

        List<String> toAdd = new ArrayList<>();

        for (String str : meta.getLore()) {
            if (StringUtils.containsIgnoreCase(str, placeholder)) {
                for (String line : lines) {
                    toAdd.add(str.replace(placeholder, line));
                }
                continue;
            }
            toAdd.add(str);
        }

        return withLore(toAdd);
    }

    /**
     * Replace placeholder in
     * the current item lore.
     *
     * @param placeholder placeholder to be founded.
     * @param line        line to be inject
     * @return {@link ItemBuilder}
     */
    public ItemBuilder replaceLine(String placeholder, String line) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getLore() == null || placeholder == null || placeholder.isEmpty()) return this;

        List<String> toAdd = new ArrayList<>();

        for (String str : meta.getLore()) {
            if (StringUtils.containsIgnoreCase(str, placeholder)) {
                toAdd.add(str.replace(placeholder, line));
                continue;
            }
            toAdd.add(str);
        }
        return withLore(toAdd);
    }


    /**
     * Add a custom enchantment
     * to item.
     *
     * @param enchantment the enchant.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withEnchantment(Enchantment enchantment) {
        return withEnchantment(enchantment, 1, false);
    }

    /**
     * Add a custom enchantment
     * to item.
     *
     * @param enchantment to enchant.
     * @param level       enchant level.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withEnchantment(Enchantment enchantment, int level) {
        return withEnchantment(enchantment, level, false);
    }

    /**
     * Add a custom enchantment
     * to item.
     *
     * @param enchantment the enchant.
     * @param level       enchant level.
     * @param unsafe      add this enchantment as unsafe enchantment?
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withEnchantment(Enchantment enchantment, int level, Boolean unsafe) {
        if (enchantment == null) return this;

        if (unsafe) {
            item.addUnsafeEnchantment(enchantment, level);
        } else {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return this;

            meta.addEnchant(enchantment, level, true);
            item.setItemMeta(meta);
        }

        return this;
    }

    /**
     * Set this item glowing.
     *
     * @return {@link ItemBuilder}
     */
    public ItemBuilder setGlow() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        meta.addEnchant(Enchantment.OXYGEN, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
        return this;
    }

    /**
     * Add item flag.
     *
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withFlag(ItemFlag... flags) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        meta.addItemFlags(flags);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Set the skull owner.
     *
     * @param owner owner name.
     * @return {@link ItemBuilder}
     */
    @Deprecated
    public ItemBuilder setSkullOwner(String owner) {
        if (owner == null || owner.isEmpty()) return this;
        if (item.getItemMeta() == null || !(item.getItemMeta() instanceof SkullMeta)) return this;

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return this;

        meta.setOwner(owner);
        item.setItemMeta(meta);
        return this;
    }


    /**
     * Change the leather armor color
     * using RGB color.
     *
     * @param red   Red amount
     * @param green Green amount
     * @param blue  Blue amount
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withColor(int red, int green, int blue) {
        return withColor(Color.fromRGB(red, green, blue));
    }

    /**
     * Change the leather armor color.
     *
     * @param color Color to be set.
     * @return {@link ItemBuilder}
     */
    public ItemBuilder withColor(Color color) {
        if (!StringUtils.contains(item.getType().toString(), "LEATHER")) return this;

        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta == null) return this;

        meta.setColor(color);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Build the current itemStack
     * and replace the given placeholders.
     *
     * @param placeholder placeholders
     * @param replace     replacements
     * @return the built itemStack.
     */
    public ItemStack build(String[] placeholder, String[] replace) {
        ItemStack item = this.item;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this.item;

        if (meta.hasDisplayName()) {
            withName(meta.getDisplayName(), placeholder, replace);
        }

        if (meta.hasLore()) {
            withLore(meta.getLore(), placeholder, replace);
        }
        return this.item;
    }

    /**
     * Build the current
     * itemStack.
     *
     * @return the built itemStack.
     */
    public ItemStack build() {
        return item;
    }

    /**
     * Get the current item builder
     * as a copy.
     *
     * @return the item builder as copy
     */
    public ItemBuilder asCopy() {
        return new ItemBuilder(this.item.clone());
    }

    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public ItemBuilder clone() {
        return new ItemBuilder(this.item.clone());
    }
}