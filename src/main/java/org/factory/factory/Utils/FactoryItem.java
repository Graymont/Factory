package org.factory.factory.Utils;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.factory.factory.Factory;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.factory.factory.Database.*;
import static org.factory.factory.Events.attributeList;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.Utils.FactoryMachine.*;
import static org.factory.factory.Utils.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.UserInterface.*;
import static org.factory.factory.Utils.VaultEconomy.icon;

public class FactoryItem {

    public List<String> availableBonus = Arrays.asList("meleedamage", "rangedamage", "undeaddamage", "undeaddefense",
            "mutantdamage", "mutantdefense", "health", "steam", "defense");

    private FactoryItem.Type type = Type.Weapon;
    private FactoryItem.SubType subType = SubType.Sword;

    private double attackDamage = 5;
    private double attackSpeed = 1;
    private double health = 1;
    private double steam = 1;
    private double attackRange = 3;
    private double criticalChance = 10;
    private double criticalDamage = 100;
    private double steamConsumption = 5;
    private double armor = 1;
    private double undeadDamage = 1;
    private double undeadDefense = 1;
    private double mutantDamage = 1;
    private double mutantDefense = 1;
    private double meleeDamage = 1;
    private double rangeDamage = 1;
    private List<String> bonusStats = new ArrayList<>();
    private double durability = 1000;
    private double maxDurability = 1000;
    private Sound attackSound = Sound.ENTITY_PLAYER_ATTACK_SWEEP;
    private AttackEffect attackEffect = AttackEffect.Slash;

    private double toolPower = 10;
    private double toolSpeed = 1;

    private Rarity.RarityType rarity = Rarity.RarityType.Common;
    private String displayname = "Unnamed Item";
    private Material material = Material.WOODEN_SWORD;

    public static String itemKey = "item";
    public static String attackDamageKey = "attackDamage";
    public static String attackRangeKey = "attackRange";
    public static String attackSpeedKey = "attackSpeed";
    public static String criticalChanceKey = "criticalChance";
    public static String criticalDamageKey = "criticalDamage";
    public static String steamKey = "steam";
    //public static String steamConsumptionKey = "steamConsumption";
    public static String healthKey = "health";
    public static String armorKey = "armor";
    public static String undeadDamageKey = "undeadDamage";
    public static String undeadDefenseKey = "undeadDefense";
    public static String mutantDamageKey = "mutantDamage";
    public static String mutantDefenseKey = "mutantDefense";
    public static String meleeDamageKey = "meleeDamage";
    public static String rangeDamageKey = "rangeDamage";
    //public static String durabilityKey = "durability";
    //public static String maxDurabilityKey = "maxDurability";
    public static String typeKey = "type";
    public static String subTypeKey = "subType";
    public static String baseKey = "base";
    public static String bonusStatsKey = "bonusStats";
    public static String toolPowerKey = "toolPower";
    public static String toolSpeedKey = "toolSpeedKey";
    public static String attackSoundKey = "attackSound";
    public static String attackEffectKey = "attackEffect";

    public static String worthKey = "worth";


    public static void SpawnAttackEffect(Player player, AttackEffect e, double range){
        if (e == AttackEffect.Slash){
            Location eyeLocation = player.getEyeLocation();
            @NotNull Vector direction = eyeLocation.getDirection();
            Location particleLocation = eyeLocation.add(direction.multiply(3));
            PlaySound(Sound.ENTITY_PLAYER_ATTACK_SWEEP, player, 1, 2);
            player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, particleLocation, 1);
            player.getWorld().spawnParticle(Particle.CRIT, particleLocation, 1);
        }

        else if (e == AttackEffect.Steam){
            shootParticleLine(player, Particle.DUST, range, 0.2, getMainPlugin(), Color.WHITE);
            PlaySound(Sound.BLOCK_LAVA_EXTINGUISH, player, 1, 1);
            PlaySound(Sound.ENTITY_BLAZE_SHOOT, player, 1, 2);
        }
    }

    public static HashMap<String, ItemStack> factoryItemList = new HashMap<>();

    public static void InitFactoryItems() {
        Map<String, FactoryItem> factoryItemList = new HashMap<>();

        // Tool Data (Material, Breaking Power, Display Name, Rarity)
        Object[][] toolData = {
                // Pickaxes
                { "woodenpickaxe", Material.WOODEN_PICKAXE, 1, 1, "Wooden Pickaxe", Rarity.RarityType.Common, Type.Tool, SubType.Pickaxe, 100 },
                { "stonepickaxe", Material.STONE_PICKAXE, 2, 1, "Stone Pickaxe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Pickaxe, 250 },
                { "ironpickaxe", Material.IRON_PICKAXE, 3, 1, "Iron Pickaxe", Rarity.RarityType.Rare, Type.Tool, SubType.Pickaxe, 500 },
                { "goldenpickaxe", Material.GOLDEN_PICKAXE, 4, 1, "Golden Pickaxe", Rarity.RarityType.Epic, Type.Tool, SubType.Pickaxe, 850 },
                { "diamondpickaxe", Material.DIAMOND_PICKAXE, 5, 1, "Diamond Pickaxe", Rarity.RarityType.Legendary, Type.Tool, SubType.Pickaxe, 1200 },
                { "netheritepickaxe", Material.NETHERITE_PICKAXE, 6, 1, "Netherite Pickaxe", Rarity.RarityType.Immortal, Type.Tool, SubType.Pickaxe, 2500 },

                // Shovels
                { "woodenshovel", Material.WOODEN_SHOVEL, 1, 1, "Wooden Shovel", Rarity.RarityType.Common, Type.Tool, SubType.Shovel, 100 },
                { "stoneshovel", Material.STONE_SHOVEL, 2, 1, "Stone Shovel", Rarity.RarityType.Uncommon, Type.Tool, SubType.Shovel, 250 },
                { "ironshovel", Material.IRON_SHOVEL, 3, 1, "Iron Shovel", Rarity.RarityType.Rare, Type.Tool, SubType.Shovel, 500 },
                { "goldenshovel", Material.GOLDEN_SHOVEL, 4, 1, "Golden Shovel", Rarity.RarityType.Epic, Type.Tool, SubType.Shovel, 850 },
                { "diamondshovel", Material.DIAMOND_SHOVEL, 5, 1, "Diamond Shovel", Rarity.RarityType.Legendary, Type.Tool, SubType.Shovel, 1200 },
                { "netheriteshovel", Material.NETHERITE_SHOVEL, 6, 1, "Netherite Shovel", Rarity.RarityType.Immortal, Type.Tool, SubType.Shovel, 2500 },

                // Axes
                { "woodenaxe", Material.WOODEN_AXE, 1, 1, "Wooden Axe", Rarity.RarityType.Common, Type.Tool, SubType.Axe, 100 },
                { "stoneaxe", Material.STONE_AXE, 2, 1, "Stone Axe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Axe, 250 },
                { "ironaxe", Material.IRON_AXE, 3, 1, "Iron Axe", Rarity.RarityType.Rare, Type.Tool, SubType.Axe, 500 },
                { "goldenaxe", Material.GOLDEN_AXE, 4, 1, "Golden Axe", Rarity.RarityType.Epic, Type.Tool, SubType.Axe, 850 },
                { "diamondaxe", Material.DIAMOND_AXE, 5, 1, "Diamond Axe", Rarity.RarityType.Legendary, Type.Tool, SubType.Axe, 1200 },
                { "netheriteaxe", Material.NETHERITE_AXE, 6, 1, "Netherite Axe", Rarity.RarityType.Immortal, Type.Tool, SubType.Axe, 2500 },

                // Hoes
                { "woodenhoe", Material.WOODEN_HOE, 1, 1, "Wooden Hoe", Rarity.RarityType.Common, Type.Tool, SubType.Hoe, 100 },
                { "stonehoe", Material.STONE_HOE, 2, 1, "Stone Hoe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Hoe, 250 },
                { "ironhoe", Material.IRON_HOE, 3, 1, "Iron Hoe", Rarity.RarityType.Rare, Type.Tool, SubType.Hoe, 500 },
                { "goldenhoe", Material.GOLDEN_HOE, 4, 1, "Golden Hoe", Rarity.RarityType.Epic, Type.Tool, SubType.Hoe, 850 },
                { "diamondhoe", Material.DIAMOND_HOE, 5, 1, "Diamond Hoe", Rarity.RarityType.Legendary, Type.Tool, SubType.Hoe, 1200 },
                { "netheritehoe", Material.NETHERITE_HOE, 6, 1, "Netherite Hoe", Rarity.RarityType.Immortal, Type.Tool, SubType.Hoe, 2500 },

                // Swords
                { "woodensword", Material.WOODEN_SWORD, 4, 1.2d, 2, "Wooden Sword", Rarity.RarityType.Common, Type.Weapon, SubType.Sword, 100 },
                { "stonesword", Material.STONE_SWORD, 5, 1.1d, 3, "Stone Sword", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Sword, 250 },
                { "ironsword", Material.IRON_SWORD, 6, 0.9d, 3, "Iron Sword", Rarity.RarityType.Rare, Type.Weapon, SubType.Sword, 500 },
                { "goldensword", Material.GOLDEN_SWORD, 7, 0.7d, 4, "Golden Sword", Rarity.RarityType.Epic, Type.Weapon, SubType.Sword, 850 },
                { "diamondsword", Material.DIAMOND_SWORD, 8, 0.5d, 5, "Diamond Sword", Rarity.RarityType.Legendary, Type.Weapon, SubType.Sword, 1200 },
                { "netheritesword", Material.NETHERITE_SWORD, 10, 0.3d, 6, "Netherite Sword", Rarity.RarityType.Immortal, Type.Weapon, SubType.Sword, 2500 },
        };

        // Iterate and create factory items
        for (Object[] data : toolData) {
            String key = (String) data[0];
            Material material = (Material) data[1];
            if (!material.toString().contains("SWORD")){
                int toolPower = (int) data[2];
                int toolSpeed = (int) data[3];
                String displayName = (String) data[4];
                Rarity.RarityType rarity = (Rarity.RarityType) data[5];
                Type type = (Type) data[6];
                SubType subType = (SubType) data[7];
                int durability = (int) data[8];

                FactoryItem item = new FactoryItem();
                item.setType(type);
                item.setSubType(subType);
                item.setToolPower(toolPower);
                item.setToolSpeed(toolSpeed);
                item.setMaterial(material);
                item.setDisplayname(displayName);
                item.setRarity(rarity);
                item.setDurability(durability);
                item.setMaxDurability(durability);

                factoryItemList.put(key, item);
            }else{
                int attackDamage = (int) data[2];
                double attackSpeed = (double) data[3];
                int attackRange = (int) data[4];
                String displayName = (String) data[5];
                Rarity.RarityType rarity = (Rarity.RarityType) data[6];
                Type type = (Type) data[7];
                SubType subType = (SubType) data[8];
                int durability = (int) data[9];

                FactoryItem item = new FactoryItem();
                item.setType(type);
                item.setSubType(subType);
                item.setAttackDamage(attackDamage);
                item.setAttackSpeed(attackSpeed);
                item.setAttackRange(attackRange);
                item.setMaterial(material);
                item.setDisplayname(displayName);
                item.setRarity(rarity);
                item.setDurability(durability);
                item.setMaxDurability(durability);
                item.setSteamConsumption(0);
                item.setAttackEffect(AttackEffect.Slash);

                factoryItemList.put(key, item);
            }

        }

        for (String key : factoryItemList.keySet()) {
            itemList.put(key, factoryItemList.get(key).build().clone());
        }

        List<Material> vanillaMaterial = Arrays.asList(

                Material.COPPER_INGOT,
                Material.RAW_COPPER,
                Material.COPPER_ORE,

                Material.IRON_INGOT,
                Material.RAW_IRON,
                Material.IRON_ORE,

                Material.GOLD_INGOT,
                Material.RAW_GOLD,
                Material.GOLD_ORE

        );

        for (Material mat : vanillaMaterial){
            ItemStack addedItem = new ItemStack(mat);

            itemList.put(mat.toString().toLowerCase().replaceAll("_", "").trim(), ProcessItemMeta(addedItem));
        }

        InitMachineDrops();
        InitMachineItems();

        consoleLog(sendText("&aFactory items initialized successfully!"));
    }
    static void InitMachineDrops(){

        // 🥦🌶🍆🔴🌿🧅🥒🥬🥕🥔

        List<String> itemLore = new ArrayList<>();
        itemLore.add(sendText("&9Machine Product"));
        itemLore.add(sendText(" "));
        itemLore.add(sendText("&aCommon"));

        List<String> dropList = Arrays.asList(

                "wheat", "barley", "corn",

                "carrot", "potato", "beetroot", "whiteonion", "redonion", "lettuce",
                "cabbage", "broccoli", "cauliflower", "radish", "cucumber", "greenbeans",
                "eggplant", "chilipepper"

        );

        for (String drop : dropList){
            ItemStack item = new ItemStack(GetItem(drop+"model"));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(sendText("&a"+formatItemName(drop)));
            meta.setLore(itemLore);
            item.setItemMeta(meta);
            itemList.put(drop, item.clone());
        }

    }

    static void InitMachineItems(){
        // Farm Machine

        // farm - grain
        String name = "wheat";
        itemList.put(name+"machine", GetMachine(name, Material.HAY_BLOCK, MachineType.Item));

        name = "barley";
        itemList.put(name+"machine", GetMachine(name, Material.SPONGE, MachineType.Item));

        name = "corn";
        itemList.put(name+"machine", GetMachine(name, Material.YELLOW_TERRACOTTA, MachineType.Item));
    }


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

    public FactoryItem setCriticalDamage(double criticalDamage) {
        this.criticalDamage = criticalDamage;
        return this;
    }

    public FactoryItem setSteamConsumption(double steamConsumption) {
        this.steamConsumption = steamConsumption;
        return this;
    }

    public FactoryItem setHealth(double health) {
        this.health = health;
        return this;
    }

    public FactoryItem setSteam(double steam) {
        this.steam = steam;
        return this;
    }

    public FactoryItem setArmor(double armor) {
        this.armor = armor;
        return this;
    }

    public FactoryItem setUndeadDamage(double undeadDamage) {
        this.undeadDamage = undeadDamage;
        return this;
    }

    public FactoryItem setUndeadDefense(double undeadDefense) {
        this.undeadDefense = undeadDefense;
        return this;
    }

    public FactoryItem setMutantDamage(double mutantDamage) {
        this.mutantDamage = mutantDamage;
        return this;
    }

    public FactoryItem setMutantDefense(double mutantDefense) {
        this.mutantDefense = mutantDefense;
        return this;
    }

    public FactoryItem setMeleeDamage(double meleeDamage) {
        this.meleeDamage = meleeDamage;
        return this;
    }

    public FactoryItem setRangeDamage(double rangeDamage) {
        this.rangeDamage = rangeDamage;
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

    public FactoryItem setBonusStats(List<String> bonusStats) {
        this.bonusStats = bonusStats;
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

    public FactoryItem setToolPower(double toolPower) {
        this.toolPower = toolPower;
        return this;
    }

    public FactoryItem setToolSpeed(double toolSpeed) {
        this.toolSpeed = toolSpeed;
        return this;
    }


    public FactoryItem setAttackEffect(AttackEffect attackEffect) {
        this.attackEffect = attackEffect;
        return this;
    }

    public FactoryItem setAttackSound(Sound attackSound) {
        this.attackSound = attackSound;
        return this;
    }

    public ItemStack build() {
        return CreateItem(
                type, subType, attackDamage, attackRange, attackSpeed, criticalChance, criticalDamage, steamConsumption,
                health, steam, armor, undeadDamage, undeadDefense, mutantDamage, mutantDefense, meleeDamage, rangeDamage,
                durability, maxDurability, toolPower, toolSpeed, bonusStats, rarity, displayname, material, attackEffect);
    }

    public ItemStack testItem(){

        List<String> addedBonus = Arrays.asList("Attack Damage:100");

        return CreateItem(
                Type.Weapon, SubType.Sword, attackDamage, attackRange, attackSpeed, criticalChance, criticalDamage, steamConsumption,
                health, steam, armor, undeadDamage, undeadDefense, mutantDamage, mutantDefense, meleeDamage, rangeDamage,
                durability, maxDurability, toolPower, toolSpeed, addedBonus, rarity, displayname, material, attackEffect);
    }

    public String getBonusKey(String key){
        return "bonus"+key;
    }

    public Double getBonusStats(PersistentDataContainer container, String namespace){
        Double bonusContainer = container.get(GetNamespacedKey(getBonusKey(namespace)), PersistentDataType.DOUBLE);
        if (bonusContainer == null){
            return 0.0;
        }else{
            return container.get(GetNamespacedKey(getBonusKey(namespace)), PersistentDataType.DOUBLE);
        }
    }

    private ItemStack CreateItem(
            Type type,
            SubType subType,
            double attackDamage,
            double attackRange,
            double attackSpeed,
            double criticalChance,
            double criticalDamage,
            double steamConsumption,
            double health,
            double steam,
            double armor,
            double undeadDamage,
            double undeadDefense,
            double mutantDamage,
            double mutantDefense,
            double meleeDamage,
            double rangeDamage,
            double durability,
            double maxDurability,
            double toolPower,
            double toolSpeed,
            List<String> bonusStats,
            Rarity.RarityType rarity,
            String displayname,
            Material material,
            AttackEffect attackEffect) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(displayname);
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (!bonusStats.isEmpty()){
            for (String stats : bonusStats) {
                String statsKey = getBonusKey(uncolouredText(stats).toLowerCase().replaceAll(" ", "").trim());
                container.set(GetNamespacedKey(statsKey), PersistentDataType.DOUBLE, Double.parseDouble(numberInText(stats)));
            }
        }

        for (String stats : attributeList) {
            String statsKey = "base"+uncolouredText(stats).toLowerCase().replaceAll(" ", "").trim();
            Double baseStatsContainer = container.get(GetNamespacedKey(statsKey), PersistentDataType.DOUBLE);
            if (baseStatsContainer == null){
                double value = 0;
                if (statsKey.equalsIgnoreCase(baseKey+attackDamageKey)){
                    value = attackDamage;
                }
                else if (statsKey.equalsIgnoreCase(baseKey+criticalChanceKey)){
                    value = criticalChance;
                }
                else if (statsKey.equalsIgnoreCase(baseKey+criticalDamageKey)){
                    value = criticalDamage;
                }
                else if (statsKey.equalsIgnoreCase(baseKey+undeadDamageKey)){
                    value = undeadDamage;
                }
                else if (statsKey.equalsIgnoreCase(baseKey+undeadDefenseKey)){
                    value = undeadDefense;
                }
                else if (statsKey.equalsIgnoreCase(baseKey+mutantDamageKey)){
                    value = mutantDamage;
                }
                else if (statsKey.equalsIgnoreCase(baseKey+mutantDefenseKey)){
                    value = mutantDefense;
                }

                else if (statsKey.equalsIgnoreCase(baseKey+healthKey)){
                    value = health;
                }
                else if (statsKey.equalsIgnoreCase(baseKey+armorKey)){
                    value = armor;
                }
                else if (statsKey.equalsIgnoreCase(baseKey+steamKey)){
                    value = steam;
                }

                else if (statsKey.equalsIgnoreCase(baseKey+meleeDamageKey)){
                    value = meleeDamage;
                }
                else if (statsKey.equalsIgnoreCase(baseKey+rangeDamageKey)){
                    value = rangeDamage;
                }

                container.set(GetNamespacedKey(statsKey), PersistentDataType.DOUBLE, value);
            }
        }

        PersistentDataType<Double, Double> doubleType = PersistentDataType.DOUBLE;

        container.set(GetNamespacedKey(itemKey), PersistentDataType.BOOLEAN, true);

        container.set(GetNamespacedKey(attackDamageKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+attackDamageKey), doubleType)+getBonusStats(container, attackDamageKey));

        container.set(GetNamespacedKey(attackRangeKey), PersistentDataType.DOUBLE, attackRange);

        container.set(GetNamespacedKey(attackSpeedKey), PersistentDataType.DOUBLE, attackSpeed);

        container.set(GetNamespacedKey(criticalChanceKey), PersistentDataType.DOUBLE, container.get(GetNamespacedKey(baseKey+criticalChanceKey), doubleType)
                +getBonusStats(container, criticalChanceKey));

        container.set(GetNamespacedKey(criticalDamageKey), PersistentDataType.DOUBLE, container.get(GetNamespacedKey(baseKey+criticalDamageKey), doubleType)
                +getBonusStats(container, criticalDamageKey));

        container.set(GetNamespacedKey(steamKey), PersistentDataType.DOUBLE, container.get(GetNamespacedKey(baseKey+steamKey), doubleType)
                +getBonusStats(container, steamKey));

        container.set(GetNamespacedKey(steamConsumptionKey), PersistentDataType.DOUBLE, steamConsumption);

        container.set(GetNamespacedKey(healthKey), PersistentDataType.DOUBLE, container.get(GetNamespacedKey(baseKey+healthKey), doubleType)
                +getBonusStats(container, healthKey));

        container.set(GetNamespacedKey(armorKey), PersistentDataType.DOUBLE, container.get(GetNamespacedKey(baseKey+armorKey), doubleType)
                +getBonusStats(container, armorKey));

        container.set(GetNamespacedKey(undeadDamageKey), PersistentDataType.DOUBLE, container.get(GetNamespacedKey(baseKey+undeadDamageKey), doubleType)
                +getBonusStats(container, undeadDamageKey));

        container.set(GetNamespacedKey(undeadDefenseKey), PersistentDataType.DOUBLE, container.get(GetNamespacedKey(baseKey+undeadDefenseKey), doubleType)
                +getBonusStats(container, undeadDefenseKey));

        container.set(GetNamespacedKey(mutantDamageKey), PersistentDataType.DOUBLE, container.get(GetNamespacedKey(baseKey+mutantDamageKey), doubleType)
                +getBonusStats(container, mutantDamageKey));

        container.set(GetNamespacedKey(mutantDefenseKey), PersistentDataType.DOUBLE, container.get(GetNamespacedKey(baseKey+mutantDefenseKey), doubleType)
                +getBonusStats(container, mutantDefenseKey));

        container.set(GetNamespacedKey(meleeDamageKey), PersistentDataType.DOUBLE, container.get(GetNamespacedKey(baseKey+meleeDamageKey), doubleType)
                +getBonusStats(container, meleeDamageKey));

        container.set(GetNamespacedKey(rangeDamageKey), PersistentDataType.DOUBLE, container.get(GetNamespacedKey(baseKey+rangeDamageKey), doubleType)
                +getBonusStats(container, rangeDamageKey));


        container.set(GetNamespacedKey(durabilityKey), PersistentDataType.DOUBLE, durability);
        container.set(GetNamespacedKey(maxDurabilityKey), PersistentDataType.DOUBLE, maxDurability);
        container.set(GetNamespacedKey(typeKey), PersistentDataType.STRING, type.toString().toLowerCase());
        container.set(GetNamespacedKey(subTypeKey), PersistentDataType.STRING, subType.toString().toLowerCase());
        container.set(GetNamespacedKey(rarityKey), PersistentDataType.STRING, rarity.toString().toLowerCase());

        container.set(GetNamespacedKey(toolPowerKey), PersistentDataType.DOUBLE, toolPower);
        container.set(GetNamespacedKey(toolSpeedKey), PersistentDataType.DOUBLE, toolSpeed);

        container.set(GetNamespacedKey(serialCodeKey), PersistentDataType.STRING, GenerateSerialCode());

        container.set(GetNamespacedKey(attackEffectKey), PersistentDataType.STRING, attackEffect.toString().toLowerCase());

        if (!bonusStats.isEmpty()){
            container.set(GetNamespacedKey(bonusStatsKey), PersistentDataType.STRING, String.join(",", bonusStats));
        }

        List<String> itemLore = new ArrayList<>();
        meta.setDisplayName(sendText(Rarity.getColor(rarity)+displayname));

        itemLore.add(sendText("&9" + type));
        itemLore.add(sendText(sendRgbText("&o"+subType, "#4A5357")));
        itemLore.add(sendText(" "));
        if (type.equals(Type.Weapon)){
            itemLore.add(sendText(" &c\uD83D\uDDE1 &7Attack Damage: &f" + (int) attackDamage));
            itemLore.add(sendText(" "+sendRgbText("⚔", "#ED1415")+" &7Attack Speed: &f" + attackSpeed+"&8&l\uD835\uDDCC"));
            itemLore.add(sendText(" "+sendRgbText("༒", "#7C1D2C")+" &7Attack Range: &f" +(int)  attackRange+"&8&l\uD835\uDDBB"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" "+sendRgbText("\uD83C\uDFF9", "#40BCC1")+" &7Critical Chance: &f" + (int)  criticalChance+"%"));
            if (steamConsumption > 0){
                itemLore.add(sendText(" "));
                itemLore.add(sendText(" &e\uD83C\uDF0A &7Steam Consumption: &f" + (int)  steamConsumption));
            }
        }
        else if (type.equals(Type.Equipment)){
            itemLore.add(sendText(" &4❤ &7Health: &c➕" + (int) health));
            itemLore.add(sendText(" &6\uD83C\uDF0A &7Steam: &e➕" +(int) steam));
            itemLore.add(sendText(" &8\uD83D\uDD30 &7Armor: &f➕"+(int) armor));
        }
        else if (type.equals(Type.Tool)){
            String toolLogo = "";
            if (subType.equals(SubType.Pickaxe)){
                toolLogo = "⛏";
            } else if (subType.equals(SubType.Axe)) {
                toolLogo = "🪓";
            } else if (subType.equals(SubType.Shovel)) {
                toolLogo = "🔨";
            } else if (subType.equals(SubType.Hoe)) {
                toolLogo = "\uD83C\uDF3F";
            } else if (subType.equals(SubType.FishingRod)) {
                toolLogo = "🎣";
            }
            String toolName = subType.toString();
            if (toolName.equals("FishingRod")){
                toolName = "Fishing Rod";
            }
            itemLore.add(sendText(" "+sendRgbText(toolLogo, "#756A40")+" &7"+toolName+" Power: &f" + (int) toolPower));
            if (toolSpeed > 0){
                itemLore.add(sendText(" "+sendRgbText("⚡", "#F2C206")+" &7"+toolName+" Speed: &f" + (int) toolPower));
            }
        }
        for (String stats : bonusStats){
            itemLore.add(sendText(" &a+"+numberInText(stats)+" &7"+uncolouredText(stats)));
        }
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7Durability: &f" + (int)  durability + "&8/&f" + (int)  maxDurability));
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
        Accessories,
        Tool;

        public static Type parseType(String type) {
            return switch (type.toLowerCase()) {
                case "weapon" -> Type.Weapon;
                case "equipment" -> Type.Equipment;
                case "accessories" -> Type.Accessories;
                case "tool" -> Type.Tool;
                default -> null;
            };
        }
    }

    public enum AttackEffect {
        Slash,
        Steam,
        Acid,
        Bullet;

        public static AttackEffect parseEffect(String type) {
            return switch (type.toLowerCase()) {
                case "slash" -> AttackEffect.Slash;
                case "steam" -> AttackEffect.Steam;
                case "acid" -> AttackEffect.Acid;
                case "bullet" -> AttackEffect.Bullet;
                default -> null;
            };
        }
    }

    public enum SubType {
        // Weapons
        Sword(Type.Weapon),
        Hammer(Type.Weapon),
        Bow(Type.Weapon),
        Blast(Type.Weapon),
        Crossbow(Type.Weapon),
        Gun(Type.Weapon),

        // Equipments
        Helmet(Type.Equipment),
        Chestplate(Type.Equipment),
        Leggings(Type.Equipment),
        Boots(Type.Equipment),

        // Accessories
        Ring(Type.Accessories),
        Gloves(Type.Accessories),
        Shield(Type.Accessories),
        Emblem(Type.Accessories),

        // Tools
        Pickaxe(Type.Tool),
        Axe(Type.Tool),
        Shovel(Type.Tool),
        Hoe(Type.Tool),
        FishingRod(Type.Tool);

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
                case "blast" -> SubType.Blast;
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
                case "pickaxe" -> SubType.Pickaxe;
                case "axe" -> SubType.Axe;
                case "shovel" -> SubType.Shovel;
                case "hoe" -> SubType.Hoe;
                case "fishingrod" -> SubType.FishingRod;
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
            if (container.has(GetNamespacedKey(itemKey))){
                if (!container.get(GetNamespacedKey(typeKey), PersistentDataType.STRING).equals(Type.Equipment.toString().toLowerCase())){
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
            if (container.has(GetNamespacedKey(itemKey))){
                if (!container.get(GetNamespacedKey(subTypeKey), PersistentDataType.STRING).equals(SubType.Helmet.toString().toLowerCase())){
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
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey(itemKey))){
                if (!container.get(GetNamespacedKey(subTypeKey), PersistentDataType.STRING).equals(SubType.Chestplate.toString().toLowerCase())){
                    return false;
                }
            }
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
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey(itemKey))){
                if (!container.get(GetNamespacedKey(subTypeKey), PersistentDataType.STRING).equals(SubType.Leggings.toString().toLowerCase())){
                    return false;
                }
            }
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
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey(itemKey))){
                if (!container.get(GetNamespacedKey(subTypeKey), PersistentDataType.STRING).equals(SubType.Boots.toString().toLowerCase())){
                    return false;
                }
            }
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

    public static boolean isBow(ItemStack item) {
        if (item != null) {
            Material type = item.getType();
            return type == Material.BOW
                    ;
        }
        return false;
    }

    public static boolean isGun(ItemStack item) {
        if (item != null) {
            Material type = item.getType();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey(itemKey))){
                if (container.has(GetNamespacedKey(subTypeKey))){
                    if (container.get(GetNamespacedKey(subTypeKey), PersistentDataType.STRING).equals(SubType.Gun.toString().toLowerCase())){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isBlast(ItemStack item) {
        if (item != null) {
            Material type = item.getType();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey(itemKey))){
                if (container.has(GetNamespacedKey(subTypeKey))){
                    if (container.get(GetNamespacedKey(subTypeKey), PersistentDataType.STRING).equals(SubType.Blast.toString().toLowerCase())){
                        return true;
                    }
                }

            }
        }
        return false;
    }

    public static boolean isFactoryItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(GetNamespacedKey(itemKey));
    }

    public static boolean isWeapon(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(GetNamespacedKey(itemKey))){
            if (container.has(GetNamespacedKey(typeKey))){
                if (!container.get(GetNamespacedKey(typeKey), PersistentDataType.STRING).equals(Type.Weapon.toString().toLowerCase())){
                    return false;
                }
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

    public static void shootParticleLine(Player player, Particle particle, double length, double step, JavaPlugin plugin, Color color) {
        Location startLocation = player.getEyeLocation();
        Vector direction = startLocation.getDirection().normalize();
        List<Location> particleLocations = new ArrayList<>();

        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1);

        for (double i = 0; i < length; i += step) {
            Location particleLocation = startLocation.clone().add(direction.clone().multiply(i));
            player.getWorld().spawnParticle(particle, particleLocation, 1, 0, 0, 0, 0, dustOptions);
            particleLocations.add(particleLocation);
        }
    }

    public static Boolean ItemNotBroken(ItemStack item) {
        if (item.getType() != Material.AIR) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey(itemKey))){
                double durability = 0;
                Double durabilityValue = container.get(GetNamespacedKey(durabilityKey), PersistentDataType.DOUBLE);
                if (durabilityValue != null){
                    durability = durabilityValue;
                }
                return durability > 0;
            }
        }
        return false;
    }

    public static ItemStack ProcessItemMeta(ItemStack item){

        ItemMeta meta = item.getItemMeta();
        meta = SetAditMeta(meta);
        meta.setDisplayName(sendText("&f"+formatItemName(item.getType().toString())));
        List<String> itemLore = new ArrayList<>();
        if (meta.hasLore()){
            for (String lore : meta.getLore()){
                itemLore.add(sendText(lore));
            }
        }
        if (item.getType().isFuel()){
            itemLore.add(sendText("&9Fuel"));
        }
        if (isMaterial(item.getType())){
            itemLore.add(sendText("&9Material"));
        }
        if (item.getType().isBlock()){
            itemLore.add(sendText("&9Placeable"));
        }
        if (item.getType().isEdible()){
            itemLore.add(sendText("&9Consumable"));
        }
        if (item.getType().isBurnable()){
            itemLore.add(sendText("&9Burnable"));
        }
        if (item.getType().isFlammable()){
            itemLore.add(sendText("&9Flammable"));
        }
        if (item.getType().isCompostable()){
            itemLore.add(sendText("&9Compostable"));
        }
        itemLore.add(sendText(" "));

        PersistentDataContainer container = meta.getPersistentDataContainer();
        Double worth = container.get(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE);
        if (worth == null){
            worth = GetWorth(item.getType().toString().toLowerCase().replaceAll("_", "").trim());
        }

        if (worth != null){
            itemLore.add(sendText(" &7Worth: &f"+FormatDouble(worth)+icon));
            itemLore.add(sendText(" &8✧ &7Sell this item at &e/sellitem"));
            itemLore.add(sendText(" &8✧ &e/sellall &7to sell all from your inventory"));
            itemLore.add(sendText(" &8✧ &7or put in a chest for &f&nSell Wand &7Multiplier"));
            itemLore.add(sendText(" "));
        }

        if (isCommonItem(item)){
            Rarity.RarityType rarityDisplay = Rarity.RarityType.Common;

            meta.setDisplayName(sendText(Rarity.getColor(rarityDisplay)
                    +formatItemName(item.getType().toString())));

            itemLore.add(sendText(Rarity.setRarity(rarityDisplay)));
        }
        else if (isUncommonItem(item)){
            Rarity.RarityType rarityDisplay = Rarity.RarityType.Uncommon;
            meta.setDisplayName(sendText(Rarity.getColor(rarityDisplay)
                    +formatItemName(item.getType().toString())));
            itemLore.add(sendText(Rarity.setRarity(rarityDisplay)));
        }
        else if (isRareItem(item)){
            Rarity.RarityType rarityDisplay = Rarity.RarityType.Rare;
            meta.setDisplayName(sendText(Rarity.getColor(rarityDisplay)
                    +formatItemName(item.getType().toString())));
            itemLore.add(sendText(Rarity.setRarity(rarityDisplay)));
        }
        else if (isEpicItem(item)){
            Rarity.RarityType rarityDisplay = Rarity.RarityType.Epic;
            meta.setDisplayName(sendText(Rarity.getColor(rarityDisplay)
                    +formatItemName(item.getType().toString())));
            itemLore.add(sendText(Rarity.setRarity(rarityDisplay)));
        }
        else if (isLegendaryItem(item)){
            Rarity.RarityType rarityDisplay = Rarity.RarityType.Legendary;
            meta.setDisplayName(sendText(Rarity.getColor(rarityDisplay)
                    +formatItemName(item.getType().toString())));
            itemLore.add(sendText(Rarity.setRarity(rarityDisplay)));
        }
        else if (isImmortalItem(item)){
            Rarity.RarityType rarityDisplay = Rarity.RarityType.Immortal;
            meta.setDisplayName(sendText(Rarity.getColor(rarityDisplay)
                    +formatItemName(item.getType().toString())));
            itemLore.add(sendText(Rarity.setRarity(rarityDisplay)));
        }else{
            itemLore.add(sendText("&fBasic"));
        }
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isMaterial(Material material) {
        for (Recipe recipe : Bukkit.getServer().getRecipesFor(new ItemStack(material))) {
            return true; // The material is used as an output in a recipe
        }

        for (@NotNull Iterator<Recipe> it = Bukkit.recipeIterator(); it.hasNext(); ) {
            Recipe recipe = it.next();
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                if (shapedRecipe.getIngredientMap().containsValue(new ItemStack(material))) {
                    return true; // The material is used as an ingredient
                }
            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                for (ItemStack ingredient : shapelessRecipe.getIngredientList()) {
                    if (ingredient.getType() == material) {
                        return true; // The material is used as an ingredient
                    }
                }
            } else if (recipe instanceof FurnaceRecipe furnaceRecipe) {
                if (furnaceRecipe.getInput().getType() == material) {
                    return true; // The material is used as an ingredient in smelting
                }
            }
        }

        return false; // The item is not used in any recipe
    }

    public static boolean isCommonItem(ItemStack item){
        return item.getType() == Material.COPPER_INGOT || item.getType() == Material.LAPIS_LAZULI
                || item.getType() == Material.REDSTONE|| item.getType() == Material.RAW_COPPER
                || item.getType() == Material.COPPER_BLOCK|| item.getType() == Material.LAPIS_BLOCK
                || item.getType() == Material.REDSTONE_BLOCK|| item.getType() == Material.COPPER_ORE
                || item.getType() == Material.REDSTONE_ORE|| item.getType() == Material.LAPIS_ORE
                || item.getType() == Material.LEATHER_HORSE_ARMOR|| item.getType() == Material.WOLF_ARMOR;
    }

    public static boolean isUncommonItem(ItemStack item){
        return item.getType() == Material.IRON_INGOT|| item.getType() == Material.RAW_IRON
                || item.getType() == Material.IRON_BLOCK|| item.getType() == Material.IRON_NUGGET
                || item.getType() == Material.IRON_ORE|| item.getType() == Material.CREEPER_HEAD
                || item.getType() == Material.IRON_HORSE_ARMOR;
    }

    public static boolean isRareItem(ItemStack item){
        return item.getType() == Material.GOLD_INGOT|| item.getType() == Material.RAW_GOLD
                || item.getType() == Material.GOLD_BLOCK|| item.getType() == Material.GOLD_NUGGET
                || item.getType() == Material.GOLD_ORE|| item.getType() == Material.WITHER_SKELETON_SKULL
                || item.getType() == Material.GOLDEN_HORSE_ARMOR;
    }

    public static boolean isEpicItem(ItemStack item){
        return item.getType() == Material.DIAMOND|| item.getType() == Material.DIAMOND_ORE
                || item.getType() == Material.DIAMOND_BLOCK|| item.getType() == Material.GOLD_ORE
                || item.getType() == Material.CREEPER_HEAD|| item.getType() == Material.BEACON
                || item.getType() == Material.DIAMOND_HORSE_ARMOR;
    }

    public static boolean isLegendaryItem(ItemStack item){
        return item.getType() == Material.EMERALD|| item.getType() == Material.EMERALD_ORE
                || item.getType() == Material.EMERALD_BLOCK|| item.getType() == Material.ELYTRA;
    }

    public static boolean isImmortalItem(ItemStack item){
        return item.getType() == Material.NETHERITE_INGOT|| item.getType() == Material.ANCIENT_DEBRIS
                || item.getType() == Material.NETHERITE_SCRAP
                || item.getType() == Material.NETHERITE_BLOCK;
    }

    public static boolean isPlayerHasItem(Player player, ItemStack item, int amount) {
        int count = 0;

        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack != null){
                if (itemStack.getItemMeta().hasDisplayName()){
                    if (itemStack.getItemMeta().getDisplayName().equals(item.getItemMeta().getDisplayName())) {
                        if (itemStack.getItemMeta().hasLore()){
                            if (itemStack.getItemMeta().getLore().equals(item.getItemMeta().getLore())){
                                count += itemStack.getAmount();
                                if (count >= amount) {
                                    return true;  // Return true early if enough items are found
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;  // Return false if not enough items are found
    }

    public static void RemoveItemFromPlayer(Player player, ItemStack item, int amount) {
        int count = 0;

        for (int i = 0; i < 36; i++) {
            ItemStack currentItem = player.getInventory().getItem(i);
            if (currentItem != null && currentItem.isSimilar(item)) {
                int itemAmount = currentItem.getAmount();
                int toRemove = Math.min(itemAmount, amount - count);

                currentItem.setAmount(itemAmount - toRemove);
                player.getInventory().setItem(i, currentItem);

                count += toRemove;

                if (count >= amount) {
                    break;
                }
            }
        }

        if (count < amount) {
            // Not enough items found, handle this case
            //player.sendMessage("Not enough items to remove.");
        }
    }
}
