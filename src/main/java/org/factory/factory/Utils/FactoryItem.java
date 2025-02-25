package org.factory.factory.Utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.factory.factory.Factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.factory.factory.Utils.FactoryMachine.*;
import static org.factory.factory.Utils.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.UserInterface.sendText;

public class FactoryItem {

    private FactoryItem.Type type;
    private FactoryItem.SubType subType;
    private double attackDamage = 5;
    private double attackSpeed = 1;
    private double attackRange = 3;
    private double criticalChance = 10;
    private double steamConsumption = 5;
    private double durability = 1000;
    private double maxDurability = 1000;
    private Rarity.RarityType rarity = Rarity.RarityType.COMMON;
    private String displayname = "Unnamed Item";
    private Material material = Material.STICK;

    public static String itemKey = "item";
    public static String attackDamageKey = "attackDamage";
    public static String attackRangeKey = "attackRange";
    public static String attackSpeedKey = "attackSpeed";
    public static String criticalChanceKey = "criticalChance";
    public static String typeKey = "type";
    public static String subTypeKey = "subType";

    public FactoryItem setType(FactoryItem.Type type) {
        this.type = type;
        return this;
    }

    public FactoryItem setSubType(FactoryItem.SubType subType) {
        this.subType = subType;
        return this;
    }

    public FactoryItem setAttackDamage(double attackDamage) {
        this.attackDamage = attackDamage;
        return this;
    }

    public FactoryItem setAttackSpeed(double attackSpeed) {
        this.attackSpeed = attackSpeed;
        return this;
    }

    public FactoryItem setAttackRange(double attackRange) {
        this.attackRange = attackRange;
        return this;
    }

    public FactoryItem setCriticalChance(double criticalChance) {
        this.criticalChance = criticalChance;
        return this;
    }

    public FactoryItem setSteamConsumption(double steamConsumption) {
        this.steamConsumption = steamConsumption;
        return this;
    }

    public FactoryItem setDurability(double durability) {
        this.durability = durability;
        return this;
    }

    public FactoryItem setMaxDurability(double maxDurability) {
        this.maxDurability = maxDurability;
        return this;
    }

    public FactoryItem setRarity(Rarity.RarityType rarity) {
        this.rarity = rarity;
        return this;
    }

    public FactoryItem setDisplayname(String displayname) {
        this.displayname = displayname;
        return this;
    }

    public FactoryItem setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public ItemStack build() {
        return CreateItem(
                type, subType, attackDamage, attackRange, attackSpeed, criticalChance, steamConsumption,
                durability, maxDurability, rarity, displayname, material);
    }

    private ItemStack CreateItem(
            Type type,
            SubType subType,
            double attackDamage,
            double attackRange,
            double attackSpeed,
            double criticalChance,
            double steamConsumption,
            double durability,
            double maxDurability,
            Rarity.RarityType rarity,
            String displayname,
            Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(displayname);
        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(GetNamespacedKey(itemKey), PersistentDataType.BOOLEAN, true);
        container.set(GetNamespacedKey(attackDamageKey), PersistentDataType.DOUBLE, attackDamage);
        container.set(GetNamespacedKey(attackRangeKey), PersistentDataType.DOUBLE, attackRange);
        container.set(GetNamespacedKey(attackSpeedKey), PersistentDataType.DOUBLE, attackSpeed);
        container.set(GetNamespacedKey(criticalChanceKey), PersistentDataType.DOUBLE, criticalChance);
        container.set(GetNamespacedKey(steamConsumptionKey), PersistentDataType.DOUBLE, steamConsumption);
        container.set(GetNamespacedKey(durabilityKey), PersistentDataType.DOUBLE, durability);
        container.set(GetNamespacedKey(maxDurabilityKey), PersistentDataType.DOUBLE, maxDurability);
        container.set(GetNamespacedKey(typeKey), PersistentDataType.STRING, type.toString().toLowerCase());
        container.set(GetNamespacedKey(subTypeKey), PersistentDataType.STRING, subType.toString().toLowerCase());
        container.set(GetNamespacedKey(rarityKey), PersistentDataType.STRING, rarity.toString().toLowerCase());

        List<String> itemLore = new ArrayList<>();
        meta.setDisplayName(sendText(Rarity.getColor(rarity)+displayname));

        itemLore.add(sendText("&9" + type));
        itemLore.add(sendText("&8&o" + subType));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &c\uD83D\uDDE1 &7Attack Damage: &f" + (int) attackDamage));
        itemLore.add(sendText(" &c༒ &7Attack Range: &f" +(int)  attackRange+"&8&l\uD835\uDDBB"));
        itemLore.add(sendText(" &c⚔ &7Attack Speed: &f" + attackSpeed+"&8&l\uD835\uDDCC"));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &3\uD83C\uDFF9 &7Critical Chance: &f" + (int)  criticalChance+"%"));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &e⚡ &7Steam Consumption: &f" + (int)  steamConsumption));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7Durability: &f" + (int)  durability + "/" + (int)  maxDurability));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(Rarity.setRarity(rarity)));

        meta.setLore(itemLore);

        //--
        NamespacedKey damageKey = new NamespacedKey(Factory.getPlugin(Factory.class), "attack_damage");
        AttributeModifier damageModifier = new AttributeModifier(damageKey, -100, AttributeModifier.Operation.ADD_NUMBER);

        NamespacedKey speedKey = new NamespacedKey(Factory.getPlugin(Factory.class), "attack_speed");
        AttributeModifier speedModifier = new AttributeModifier(speedKey, 0, AttributeModifier.Operation.ADD_NUMBER);

        meta.setUnbreakable(true);
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier);
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, speedModifier);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_STORED_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.addItemFlags(ItemFlag.HIDE_DYE);
        // --

        item.setItemMeta(meta);
        return item;
    }
    
    
    public enum Type {
        Weapon,
        Equipment,
        Accessories;

        public static Type parseType(String type) {
            return switch (type.toLowerCase()) {
                case "weapon" -> Type.Weapon;
                case "equipment" -> Type.Equipment;
                case "accessories" -> Type.Accessories;
                default -> null;
            };
        }
    }

    public enum SubType {
        // Weapon
        Sword(Type.Weapon),
        Hammer(Type.Weapon),
        Bow(Type.Weapon),
        Crossbow(Type.Weapon),
        Gun(Type.Weapon),

        // Equipment
        Helmet(Type.Equipment),
        Chestplate(Type.Equipment),
        Leggings(Type.Equipment),
        Boots(Type.Equipment),

        // Accessories
        Ring(Type.Accessories),
        Gloves(Type.Accessories),
        Shield(Type.Accessories),
        Emblem(Type.Accessories);

        private final Type type;

        SubType(Type type) {
            this.type = type;
        }

        public Type getType() {
            return type;
        }

        public static SubType parseSubType(String subType) {
            return switch (subType.toLowerCase()) {
                case "sword" -> SubType.Sword;
                case "hammer" -> SubType.Hammer;
                case "gun" -> SubType.Gun;
                case "bow" -> SubType.Bow;
                case "crossbow" -> SubType.Crossbow;
                case "helmet" -> SubType.Helmet;
                case "chestplate" -> SubType.Chestplate;
                case "leggings" -> SubType.Leggings;
                case "boots" -> SubType.Boots;
                case "ring" -> SubType.Ring;
                case "gloves" -> SubType.Gloves;
                case "shield" -> SubType.Shield;
                case "emblem" -> SubType.Emblem;
                default -> null;
            };
        }
    }

    public static boolean isPickaxe(ItemStack item) {
        if (item != null) {
            Material type = item.getType();
            return
                    type == Material.WOODEN_PICKAXE || type == Material.STONE_PICKAXE ||
                            type == Material.IRON_PICKAXE || type == Material.GOLDEN_PICKAXE ||
                            type == Material.DIAMOND_PICKAXE || type == Material.NETHERITE_PICKAXE;
        }
        return false;
    }

    public static boolean isSword(ItemStack item) {
        if (item != null) {
            Material type = item.getType();
            return
                    type == Material.WOODEN_SWORD || type == Material.STONE_SWORD ||
                            type == Material.IRON_SWORD || type == Material.GOLDEN_SWORD ||
                            type == Material.DIAMOND_SWORD || type == Material.NETHERITE_SWORD;
        }
        return false;
    }

    public static boolean isMace(ItemStack item) {
        if (item != null) {
            Material type = item.getType();
            return
                    type == Material.MACE ;
        }
        return false;
    }

    public static boolean isHammer(ItemStack item) {
        if (item != null) {
            Material type = item.getType();
            return
                    type == Material.MACE ;
        }
        return false;
    }

    public static boolean isAxe(ItemStack item) {
        if (item != null) {
            Material type = item.getType();
            return
                    type == Material.WOODEN_AXE || type == Material.STONE_AXE ||
                            type == Material.IRON_AXE || type == Material.GOLDEN_AXE ||
                            type == Material.DIAMOND_AXE || type == Material.NETHERITE_AXE;
        }
        return false;
    }

    public static boolean isFishingRod(ItemStack item) {
        if (item != null) {
            Material type = item.getType();
            return
                    type == Material.FISHING_ROD;
        }
        return false;
    }

    public static boolean isShovel(ItemStack item) {
        if (item != null) {
            Material type = item.getType();
            return
                    type == Material.WOODEN_SHOVEL || type == Material.STONE_SHOVEL ||
                            type == Material.IRON_SHOVEL || type == Material.GOLDEN_SHOVEL ||
                            type == Material.DIAMOND_SHOVEL || type == Material.NETHERITE_SHOVEL;
        }
        return false;
    }

    public static boolean isHoe(ItemStack item) {
        if (item != null) {
            Material type = item.getType();
            return
                    type == Material.WOODEN_HOE || type == Material.STONE_HOE ||
                            type == Material.IRON_HOE || type == Material.GOLDEN_HOE ||
                            type == Material.DIAMOND_HOE || type == Material.NETHERITE_HOE;
        }
        return false;
    }

    public static boolean isArmor(ItemStack item) {
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey("item"))){
                if (container.get(GetNamespacedKey("subType"), PersistentDataType.STRING).equals("accessories")){
                    return false;
                }
            }
            Material type = item.getType();
            return
                    type == Material.LEATHER_HELMET || type == Material.LEATHER_CHESTPLATE ||
                            type == Material.LEATHER_LEGGINGS || type == Material.LEATHER_BOOTS ||
                            type == Material.CHAINMAIL_HELMET || type == Material.CHAINMAIL_CHESTPLATE ||
                            type == Material.CHAINMAIL_LEGGINGS || type == Material.CHAINMAIL_BOOTS ||
                            type == Material.IRON_HELMET || type == Material.IRON_CHESTPLATE ||
                            type == Material.IRON_LEGGINGS || type == Material.IRON_BOOTS ||
                            type == Material.GOLDEN_HELMET || type == Material.GOLDEN_CHESTPLATE ||
                            type == Material.GOLDEN_LEGGINGS || type == Material.GOLDEN_BOOTS ||
                            type == Material.DIAMOND_HELMET || type == Material.DIAMOND_CHESTPLATE ||
                            type == Material.DIAMOND_LEGGINGS || type == Material.DIAMOND_BOOTS ||
                            type == Material.NETHERITE_HELMET || type == Material.NETHERITE_CHESTPLATE ||
                            type == Material.NETHERITE_LEGGINGS || type == Material.NETHERITE_BOOTS
                            || type == Material.PLAYER_HEAD
                    ;
        }
        return false;
    }

    public static boolean isHelmet(ItemStack item) {
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey("item"))){
                if (container.get(GetNamespacedKey("subType"), PersistentDataType.STRING).equals("accessories")){
                    return false;
                }
            }
            Material type = item.getType();
            return
                    type == Material.LEATHER_HELMET ||
                            type == Material.CHAINMAIL_HELMET ||
                            type == Material.IRON_HELMET ||
                            type == Material.GOLDEN_HELMET ||
                            type == Material.DIAMOND_HELMET ||
                            type == Material.NETHERITE_HELMET
                            ||
                            type == Material.PLAYER_HEAD
                    ;
        }
        return false;
    }

    public static boolean isChestplate(ItemStack item) {
        if (item != null) {
            Material type = item.getType();
            return
                    type == Material.LEATHER_CHESTPLATE ||
                            type == Material.CHAINMAIL_CHESTPLATE ||
                            type == Material.IRON_CHESTPLATE ||
                            type == Material.GOLDEN_CHESTPLATE ||
                            type == Material.DIAMOND_CHESTPLATE ||
                            type == Material.NETHERITE_CHESTPLATE
                    ;
        }
        return false;
    }

    public static boolean isLeggings(ItemStack item) {
        if (item != null) {
            Material type = item.getType();
            return
                    type == Material.LEATHER_LEGGINGS ||
                            type == Material.CHAINMAIL_LEGGINGS ||
                            type == Material.IRON_LEGGINGS ||
                            type == Material.GOLDEN_LEGGINGS ||
                            type == Material.DIAMOND_LEGGINGS ||
                            type == Material.NETHERITE_LEGGINGS
                    ;
        }
        return false;
    }

    public static boolean isBoots(ItemStack item) {
        if (item != null) {
            Material type = item.getType();
            return
                    type == Material.LEATHER_BOOTS ||
                            type == Material.CHAINMAIL_BOOTS ||
                            type == Material.IRON_BOOTS ||
                            type == Material.GOLDEN_BOOTS ||
                            type == Material.DIAMOND_BOOTS ||
                            type == Material.NETHERITE_BOOTS
                    ;
        }
        return false;
    }

    public static boolean isTrident(ItemStack item) {
        if (item != null) {
            Material type = item.getType();
            return type == Material.TRIDENT
                    ;
        }
        return false;
    }

    public static boolean isWeapon(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(GetNamespacedKey("item"))){
            if (!container.get(GetNamespacedKey("type"), PersistentDataType.STRING).equals("weapon")){
                return false;
            }
        }

        Material type = item.getType();
        return type == Material.WOODEN_AXE || type == Material.STONE_AXE ||
                type == Material.IRON_AXE || type == Material.GOLDEN_AXE ||
                type == Material.DIAMOND_AXE || type == Material.NETHERITE_AXE ||
                type == Material.WOODEN_SHOVEL || type == Material.STONE_SHOVEL ||
                type == Material.IRON_SHOVEL || type == Material.GOLDEN_SHOVEL ||
                type == Material.DIAMOND_SHOVEL || type == Material.NETHERITE_SHOVEL ||
                type == Material.WOODEN_PICKAXE || type == Material.STONE_PICKAXE ||
                type == Material.IRON_PICKAXE || type == Material.GOLDEN_PICKAXE ||
                type == Material.DIAMOND_PICKAXE || type == Material.NETHERITE_PICKAXE ||
                type == Material.WOODEN_SWORD || type == Material.STONE_SWORD ||
                type == Material.IRON_SWORD || type == Material.GOLDEN_SWORD ||
                type == Material.DIAMOND_SWORD || type == Material.NETHERITE_SWORD ||
                type == Material.BOW || type == Material.CROSSBOW || type == Material.TRIDENT
                ||
                type == Material.WOODEN_HOE || type == Material.STONE_HOE || type == Material.GOLDEN_HOE ||
                type == Material.DIAMOND_HOE || type == Material.IRON_HOE || type == Material.NETHERITE_HOE
                || type == Material.STICK || type == Material.BLAZE_ROD || type == Material.SHEARS
                || type == Material.LEATHER_HORSE_ARMOR || type == Material.IRON_HORSE_ARMOR || type == Material.GOLDEN_HORSE_ARMOR
                || type == Material.DIAMOND_HORSE_ARMOR || type == Material.BOOK || type == Material.MACE;
    }

    public static boolean isTool(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            Material type = item.getType();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (type == Material.LEATHER_HELMET || type == Material.LEATHER_CHESTPLATE ||
                    type == Material.LEATHER_LEGGINGS || type == Material.LEATHER_BOOTS ||
                    type == Material.CHAINMAIL_HELMET || type == Material.CHAINMAIL_CHESTPLATE ||
                    type == Material.CHAINMAIL_LEGGINGS || type == Material.CHAINMAIL_BOOTS ||
                    type == Material.IRON_HELMET || type == Material.IRON_CHESTPLATE ||
                    type == Material.IRON_LEGGINGS || type == Material.IRON_BOOTS ||
                    type == Material.GOLDEN_HELMET || type == Material.GOLDEN_CHESTPLATE ||
                    type == Material.GOLDEN_LEGGINGS || type == Material.GOLDEN_BOOTS ||
                    type == Material.DIAMOND_HELMET || type == Material.DIAMOND_CHESTPLATE ||
                    type == Material.DIAMOND_LEGGINGS || type == Material.DIAMOND_BOOTS ||
                    type == Material.NETHERITE_HELMET || type == Material.NETHERITE_CHESTPLATE ||
                    type == Material.NETHERITE_LEGGINGS || type == Material.NETHERITE_BOOTS ||
                    type == Material.WOODEN_AXE || type == Material.STONE_AXE ||
                    type == Material.IRON_AXE || type == Material.GOLDEN_AXE ||
                    type == Material.DIAMOND_AXE || type == Material.NETHERITE_AXE ||
                    type == Material.WOODEN_SHOVEL || type == Material.STONE_SHOVEL ||
                    type == Material.IRON_SHOVEL || type == Material.GOLDEN_SHOVEL ||
                    type == Material.DIAMOND_SHOVEL || type == Material.NETHERITE_SHOVEL ||
                    type == Material.WOODEN_PICKAXE || type == Material.STONE_PICKAXE ||
                    type == Material.IRON_PICKAXE || type == Material.GOLDEN_PICKAXE ||
                    type == Material.DIAMOND_PICKAXE || type == Material.NETHERITE_PICKAXE ||
                    type == Material.WOODEN_SWORD || type == Material.STONE_SWORD ||
                    type == Material.IRON_SWORD || type == Material.GOLDEN_SWORD ||
                    type == Material.DIAMOND_SWORD || type == Material.NETHERITE_SWORD ||
                    type == Material.BOW || type == Material.CROSSBOW || type == Material.TRIDENT || type == Material.FISHING_ROD
                    || type == Material.SHEARS || type == Material.WOODEN_HOE || type == Material.STONE_HOE || type == Material.GOLDEN_HOE ||
                    type == Material.DIAMOND_HOE || type == Material.IRON_HOE || type == Material.NETHERITE_HOE || type == Material.MACE
            ) {
                return true;
            } else if (item.getItemMeta().hasLore() && type == Material.STICK || type == Material.BLAZE_ROD || type == Material.LEATHER_HORSE_ARMOR ||
                    type == Material.IRON_HORSE_ARMOR || type == Material.GOLDEN_HORSE_ARMOR || type == Material.DIAMOND_HORSE_ARMOR || type == Material.BOOK
                    || type == Material.PLAYER_HEAD || type == Material.ITEM_FRAME || type == Material.RED_DYE|| type == Material.LIME_DYE
                    || type == Material.ORANGE_DYE|| type == Material.BLUE_DYE) {

                return container.has(GetNamespacedKey("item"));
            }
        }
        return false;
    }

    public static ItemMeta SetAditMeta(ItemMeta meta){

        NamespacedKey damageKey = new NamespacedKey(Factory.getPlugin(Factory.class), "attack_damage");
        AttributeModifier damageModifier = new AttributeModifier(damageKey, -100, AttributeModifier.Operation.ADD_NUMBER);

        NamespacedKey speedKey = new NamespacedKey(Factory.getPlugin(Factory.class), "attack_speed");
        AttributeModifier speedModifier = new AttributeModifier(speedKey, 0, AttributeModifier.Operation.ADD_NUMBER);

        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier);
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, speedModifier);

        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_STORED_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.addItemFlags(ItemFlag.HIDE_DYE);

        return meta;
    }

}
