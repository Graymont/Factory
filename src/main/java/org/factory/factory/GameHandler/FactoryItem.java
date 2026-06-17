package org.factory.factory.GameHandler;

import com.google.common.collect.Multimap;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import org.factory.factory.Factory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.factory.factory.Database.*;
import static org.factory.factory.Events.*;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.GameHandler.Booster.GetBoosterPercent;
import static org.factory.factory.GameHandler.Dungeon.getDungeonPendant;
import static org.factory.factory.GameHandler.FactoryMachine.*;
import static org.factory.factory.GameHandler.FactoryQuest.getQuestPendant;
import static org.factory.factory.GameManager.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.GameManager.PlayerProgressManager.cropDropChance;
import static org.factory.factory.Utils.UserInterface.*;
import static org.factory.factory.Utils.VaultEconomy.icon;

public class FactoryItem {

    public List<String> availableBonus = Arrays.asList("meleedamage", "rangedamage", "undeaddamage", "undeaddefense",
            "mutantdamage", "mutantdefense", "health", "steam", "defense");

    private FactoryItem.Type type = Type.Weapon;
    private FactoryItem.SubType subType = SubType.Sword;

    private double attackDamage = 5;
    private double attackSpeed = 1;
    private double health = 0;
    private double steam = 0;
    private double attackRange = 3;
    private double criticalChance = 0;
    private double criticalDamage = 0;
    private double steamConsumption = 0;
    private double armor = 1;
    private double undeadDamage = 0;
    private double undeadDefense = 0;
    private double mutantDamage = 0;
    private double mutantDefense = 0;
    private double meleeDamage = 0;
    private double rangeDamage = 0;
    private double proficiency = 0;
    private String bonusStats = "";
    private double durability = 10000;
    private double maxDurability = 1000;
    private int levelMinimum = 1;
    private Sound attackSound = Sound.ENTITY_PLAYER_ATTACK_SWEEP;
    private AttackEffect attackEffect = AttackEffect.Slash;

    private double toolPower = 10;
    private double toolSpeed = 1;

    private double wandMultiplier = 1.0;

    private Color color = Color.fromARGB(0, 165, 42, 42);

    private Rarity.RarityType rarity = Rarity.RarityType.Common;
    private String displayname = "Unnamed Item";
    private Material material = Material.WOODEN_SWORD;

    private double farmingFortune = 0;
    private double miningFortune = 0;
    private double fishingFortune = 0;
    private double combatFortune = 0;
    private double foragingFortune = 0;

    public static String itemKey = "item";
    public static String revisionCodeKey = "revisionCode";
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
    public static String toolSpeedKey = "toolSpeed";
    public static String attackSoundKey = "attackSound";
    public static String attackEffectKey = "attackEffect";
    public static String canUseKey = "canUse";
    public static String multiplierKey = "multiplier";

    public static Booster.BoosterType boosterType = Booster.BoosterType.None;
    public static int boosterDuration = 0;

    public static String colorKey = "color";

    public static String worthKey = "worth";

    public static String levelMinimumKey = "levelMinimum";

    public static String backpackSizeKey = "backpackSize";

    public static String proficiencyKey = "proficiency";

    public static String boosterTypeKey = "boosterType";
    public static String boosterDurationKey = "boosterDuration";

    public static String farmingFortuneKey = "farmingFortune";
    public static String miningFortuneKey = "miningFortune";
    public static String combatFortuneKey = "combatFortune";
    public static String foragingFortuneKey = "foragingFortune";
    public static String fishingFortuneKey = "fishingFortune";

    public static Boolean canUse = true;

    public static void SpawnAttackEffect(Player player, AttackEffect e, double range){
        if (e == AttackEffect.Slash){
            Location eyeLocation = player.getEyeLocation();
            @NotNull Vector direction = eyeLocation.getDirection();
            Location particleLocation = eyeLocation.add(direction.multiply(3));
            PlaySound(Sound.ENTITY_PLAYER_ATTACK_SWEEP, player, 1, 2);
            player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, particleLocation, 1);
            player.getWorld().spawnParticle(Particle.CRIT, particleLocation, 1);
        }

        else if (e == AttackEffect.Smash){
            Location eyeLocation = player.getEyeLocation();
            @NotNull Vector direction = eyeLocation.getDirection();
            Location particleLocation = eyeLocation.add(direction.multiply(3));
            PlaySound(Sound.ENTITY_PLAYER_ATTACK_SWEEP, player, 1, 0);
            player.getWorld().spawnParticle(Particle.EXPLOSION, particleLocation, 1);
            player.getWorld().spawnParticle(Particle.CRIT, particleLocation, 1);
        }

        else if (e == AttackEffect.Steam){
            shootParticleLine(player, Particle.DUST, range, 0.2, getMainPlugin(), Color.WHITE);
            PlaySound(Sound.BLOCK_LAVA_EXTINGUISH, player, 1, 1);
            //PlaySound(Sound.ENTITY_BLAZE_SHOOT, player, 1, 2);
        }
        else if (e == AttackEffect.Acid){
            shootParticleLine(player, Particle.DUST, range, 0.2, getMainPlugin(), Color.GREEN);
            PlaySound(Sound.ENTITY_SLIME_DEATH, player, 1, 1);
            PlaySound(Sound.BLOCK_SLIME_BLOCK_PLACE, player, 1, 2);
        }
        else if (e == AttackEffect.Energy){
            shootParticleLine(player, Particle.DUST, range, 0.2, getMainPlugin(), Color.YELLOW);
            PlaySound(Sound.ENTITY_GUARDIAN_ATTACK, player, 1, 1);
            //PlaySound(Sound.ENTITY_BLAZE_SHOOT, player, 1, 2);
        }
        else if (e == AttackEffect.Flamethrower){
            shootParticleLine(player, Particle.DUST, range, 0.2, getMainPlugin(), Color.RED);
            shootParticleLine(player, Particle.DUST, range, 0.15f, getMainPlugin(), Color.ORANGE);
            //PlaySound(Sound.BLOCK_LAVA_EXTINGUISH, player, 1, 1);
            PlaySound(Sound.ENTITY_BLAZE_SHOOT, player, 1, 2);
        }


        else if (e == AttackEffect.Arrow){
            PlaySound(Sound.ENTITY_ARROW_SHOOT, player, 1, 1);
        }

        else if (e == AttackEffect.Bullet){
            PlaySound(Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, player, 1, 3);
            PlaySound(Sound.ENTITY_LIGHTNING_BOLT_IMPACT, player, 0.1f, 0);
        }
    }

    public static HashMap<String, ItemStack> factoryItemList = new HashMap<>();

    public static void InitFactoryItems() {
        Map<String, FactoryItem> factoryItemList = new HashMap<>();

        // Tool Data (Material, Breaking Power, Display Name, Rarity)
        Object[][] toolData = {
                // Fishing Rod
                { "fishingrod", Material.FISHING_ROD, 1, 1, "Fishing Rod", Rarity.RarityType.Common, Type.Tool, SubType.Fishing_Rod, 100, 1, 0 },

                // Pickaxes
                { "woodenpickaxe", Material.WOODEN_PICKAXE, 1, 1, "Wooden Pickaxe", Rarity.RarityType.Common, Type.Tool, SubType.Pickaxe, 100, 1, 0 },
                { "stonepickaxe", Material.STONE_PICKAXE, 2, 1, "Stone Pickaxe", Rarity.RarityType.Common, Type.Tool, SubType.Pickaxe, 250, 2, 0 },
                { "ironpickaxe", Material.IRON_PICKAXE, 3, 1, "Iron Pickaxe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Pickaxe, 500, 3, 0 },
                { "goldenpickaxe", Material.GOLDEN_PICKAXE, 4, 1, "Golden Pickaxe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Pickaxe, 850, 4, 0 },
                { "diamondpickaxe", Material.DIAMOND_PICKAXE, 5, 1, "Diamond Pickaxe", Rarity.RarityType.Rare, Type.Tool, SubType.Pickaxe, 1200, 5, 0 },
                { "netheritepickaxe", Material.NETHERITE_PICKAXE, 6, 1, "Netherite Pickaxe", Rarity.RarityType.Epic, Type.Tool, SubType.Pickaxe, 2500, 6, 0 },

                { "reinforcedcopperpickaxe", Material.IRON_PICKAXE, 7, 1, "Reinforced Copper Pickaxe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Pickaxe, 10000, 11, 1 },
                { "reinforcedredstonepickaxe", Material.IRON_PICKAXE, 7, 1, "Reinforced Redstone Pickaxe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Pickaxe, 10000, 12, 2 },
                { "reinforcedlapispickaxe", Material.IRON_PICKAXE, 7, 1, "Reinforced Lapis Pickaxe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Pickaxe, 10000, 14, 3 },
                { "reinforcedgoldenpickaxe", Material.GOLDEN_PICKAXE, 7, 1, "Reinforced Golden Pickaxe", Rarity.RarityType.Rare, Type.Tool, SubType.Pickaxe, 10000, 16, 4 },
                { "reinforceddiamondpickaxe", Material.DIAMOND_PICKAXE, 7, 1, "Reinforced Diamond Pickaxe", Rarity.RarityType.Epic, Type.Tool, SubType.Pickaxe, 10000, 18, 5 },
                { "reinforcednetheritepickaxe", Material.NETHERITE_PICKAXE, 7, 1, "Reinforced Netherite Pickaxe", Rarity.RarityType.Legendary, Type.Tool, SubType.Pickaxe, 10000, 20, 5 },

                { "darksteelpickaxe", Material.IRON_PICKAXE, 8, 1, "Dark Steel Pickaxe", Rarity.RarityType.Rare, Type.Tool, SubType.Pickaxe, 10000, 21, 6 },
                { "shadowpickaxe", Material.GOLDEN_PICKAXE, 8, 1, "Shadow Pickaxe", Rarity.RarityType.Rare, Type.Tool, SubType.Pickaxe, 10000, 22, 7 },
                { "crimsonpickaxe", Material.GOLDEN_PICKAXE, 8, 1, "Crimson Pickaxe", Rarity.RarityType.Rare, Type.Tool, SubType.Pickaxe, 10000, 24, 8 },
                { "moltenpickaxe", Material.DIAMOND_PICKAXE, 8, 1, "Molten Pickaxe", Rarity.RarityType.Epic, Type.Tool, SubType.Pickaxe, 10000, 26, 9 },
                { "meteoritepickaxe", Material.DIAMOND_PICKAXE, 8, 1, "Meteorite Pickaxe", Rarity.RarityType.Legendary, Type.Tool, SubType.Pickaxe, 10000, 28, 10 },
                { "reinforcedmeteoritepickaxe", Material.NETHERITE_PICKAXE, 8, 1, "Reinforced Meteorite Pickaxe", Rarity.RarityType.Immortal, Type.Tool, SubType.Pickaxe, 10000, 30, 10 },

                { "amethystpickaxe", Material.IRON_PICKAXE, 9, 1, "Amethyst Pickaxe", Rarity.RarityType.Epic, Type.Tool, SubType.Pickaxe, 10000, 31, 11 },
                { "sapphirepickaxe", Material.GOLDEN_PICKAXE, 9, 1, "Sapphire Pickaxe", Rarity.RarityType.Epic, Type.Tool, SubType.Pickaxe, 10000, 32, 12 },
                { "rubypickaxe", Material.GOLDEN_PICKAXE, 9, 1, "Ruby Pickaxe", Rarity.RarityType.Epic, Type.Tool, SubType.Pickaxe, 10000, 34, 13 },
                { "emeraldpickaxe", Material.DIAMOND_PICKAXE, 9, 1, "Emerald Pickaxe", Rarity.RarityType.Legendary, Type.Tool, SubType.Pickaxe, 10000, 36, 14 },
                { "reinforcedrubypickaxe", Material.DIAMOND_PICKAXE, 9, 1, "Reinforced Ruby Pickaxe", Rarity.RarityType.Immortal, Type.Tool, SubType.Pickaxe, 10000, 38, 15 },
                { "reinforcedemeraldpickaxe", Material.NETHERITE_PICKAXE, 9, 1, "Reinforced Emerald Pickaxe", Rarity.RarityType.Divine, Type.Tool, SubType.Pickaxe, 10000, 40, 15 },


                // Shovels
                { "woodenshovel", Material.WOODEN_SHOVEL, 1, 1, "Wooden Shovel", Rarity.RarityType.Common, Type.Tool, SubType.Shovel, 100, 1, 0 },
                { "stoneshovel", Material.STONE_SHOVEL, 2, 1, "Stone Shovel", Rarity.RarityType.Common, Type.Tool, SubType.Shovel, 250, 2, 0 },
                { "ironshovel", Material.IRON_SHOVEL, 3, 1, "Iron Shovel", Rarity.RarityType.Uncommon, Type.Tool, SubType.Shovel, 500, 3, 0 },
                { "goldenshovel", Material.GOLDEN_SHOVEL, 4, 1, "Golden Shovel", Rarity.RarityType.Uncommon, Type.Tool, SubType.Shovel, 850, 4, 0 },
                { "diamondshovel", Material.DIAMOND_SHOVEL, 5, 1, "Diamond Shovel", Rarity.RarityType.Rare, Type.Tool, SubType.Shovel, 1200, 5, 0 },
                { "netheriteshovel", Material.NETHERITE_SHOVEL, 6, 1, "Netherite Shovel", Rarity.RarityType.Epic, Type.Tool, SubType.Shovel, 2500, 6, 0 },

                { "reinforcedcoppershovel", Material.IRON_SHOVEL, 7, 1, "Reinforced Copper Shovel", Rarity.RarityType.Uncommon, Type.Tool, SubType.Shovel, 10000, 11, 1 },
                { "reinforcedredstoneshovel", Material.IRON_SHOVEL, 7, 1, "Reinforced Redstone Shovel", Rarity.RarityType.Uncommon, Type.Tool, SubType.Shovel, 10000, 12, 2 },
                { "reinforcedlapisshovel", Material.IRON_SHOVEL, 7, 1, "Reinforced Lapis Shovel", Rarity.RarityType.Uncommon, Type.Tool, SubType.Shovel, 10000, 14, 3 },
                { "reinforcedgoldenshovel", Material.GOLDEN_SHOVEL, 7, 1, "Reinforced Golden Shovel", Rarity.RarityType.Rare, Type.Tool, SubType.Shovel, 10000, 16, 4 },
                { "reinforceddiamondshovel", Material.DIAMOND_SHOVEL, 7, 1, "Reinforced Diamond Shovel", Rarity.RarityType.Epic, Type.Tool, SubType.Shovel, 10000, 18, 5 },
                { "reinforcednetheriteshovel", Material.NETHERITE_SHOVEL, 7, 1, "Reinforced Netherite Shovel", Rarity.RarityType.Legendary, Type.Tool, SubType.Shovel, 10000, 20, 5 },

                { "darksteelshovel", Material.IRON_SHOVEL, 8, 1, "Dark Steel Shovel", Rarity.RarityType.Rare, Type.Tool, SubType.Shovel, 10000, 21, 6 },
                { "shadowshovel", Material.GOLDEN_SHOVEL, 8, 1, "Shadow Shovel", Rarity.RarityType.Rare, Type.Tool, SubType.Shovel, 10000, 22, 7 },
                { "crimsonshovel", Material.GOLDEN_SHOVEL, 8, 1, "Crimson Shovel", Rarity.RarityType.Rare, Type.Tool, SubType.Shovel, 10000, 24, 8 },
                { "moltenshovel", Material.DIAMOND_SHOVEL, 8, 1, "Molten Shovel", Rarity.RarityType.Epic, Type.Tool, SubType.Shovel, 10000, 26, 9 },
                { "meteoriteshovel", Material.DIAMOND_SHOVEL, 8, 1, "Meteorite Shovel", Rarity.RarityType.Legendary, Type.Tool, SubType.Shovel, 10000, 28, 10 },
                { "reinforcedmeteoriteshovel", Material.NETHERITE_SHOVEL, 8, 1, "Reinforced Meteorite Shovel", Rarity.RarityType.Immortal, Type.Tool, SubType.Shovel, 10000, 30, 10 },

                { "amethystshovel", Material.IRON_SHOVEL, 9, 1, "Amethyst Shovel", Rarity.RarityType.Epic, Type.Tool, SubType.Shovel, 10000, 31, 11 },
                { "sapphireshovel", Material.GOLDEN_SHOVEL, 9, 1, "Sapphire Shovel", Rarity.RarityType.Epic, Type.Tool, SubType.Shovel, 10000, 32, 12 },
                { "rubyshovel", Material.GOLDEN_SHOVEL, 9, 1, "Ruby Shovel", Rarity.RarityType.Epic, Type.Tool, SubType.Shovel, 10000, 34, 13 },
                { "emeraldshovel", Material.DIAMOND_SHOVEL, 9, 1, "Emerald Shovel", Rarity.RarityType.Legendary, Type.Tool, SubType.Shovel, 10000, 36, 14 },
                { "reinforcedrubyshovel", Material.DIAMOND_SHOVEL, 9, 1, "Reinforced Ruby Shovel", Rarity.RarityType.Immortal, Type.Tool, SubType.Shovel, 10000, 38, 15 },
                { "reinforcedemeraldshovel", Material.NETHERITE_SHOVEL, 9, 1, "Reinforced Emerald Shovel", Rarity.RarityType.Divine, Type.Tool, SubType.Shovel, 10000, 40, 15 },



                // Axes
                { "woodenaxe", Material.WOODEN_AXE, 1, 1, "Wooden Axe", Rarity.RarityType.Common, Type.Tool, SubType.Axe, 100, 1, 0 },
                { "stoneaxe", Material.STONE_AXE, 2, 1, "Stone Axe", Rarity.RarityType.Common, Type.Tool, SubType.Axe, 250, 2, 0 },
                { "ironaxe", Material.IRON_AXE, 3, 1, "Iron Axe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Axe, 500, 3, 0 },
                { "goldenaxe", Material.GOLDEN_AXE, 4, 1, "Golden Axe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Axe, 850, 4, 0 },
                { "diamondaxe", Material.DIAMOND_AXE, 5, 1, "Diamond Axe", Rarity.RarityType.Rare, Type.Tool, SubType.Axe, 1200, 5, 0 },
                { "netheriteaxe", Material.NETHERITE_AXE, 6, 1, "Netherite Axe", Rarity.RarityType.Epic, Type.Tool, SubType.Axe, 2500, 8, 0 },

                { "reinforcedcopperaxe", Material.IRON_AXE, 7, 1, "Reinforced Copper Axe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Axe, 10000, 11, 1 },
                { "reinforcedredstoneaxe", Material.IRON_AXE, 7, 1, "Reinforced Redstone Axe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Axe, 10000, 12, 2 },
                { "reinforcedlapisaxe", Material.IRON_AXE, 7, 1, "Reinforced Lapis Axe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Axe, 10000, 14, 3 },
                { "reinforcedgoldenaxe", Material.GOLDEN_AXE, 7, 1, "Reinforced Golden Axe", Rarity.RarityType.Rare, Type.Tool, SubType.Axe, 10000, 16, 4 },
                { "reinforceddiamondaxe", Material.DIAMOND_AXE, 7, 1, "Reinforced Diamond Axe", Rarity.RarityType.Epic, Type.Tool, SubType.Axe, 10000, 18, 5 },
                { "reinforcednetheriteaxe", Material.NETHERITE_AXE, 7, 1, "Reinforced Netherite Axe", Rarity.RarityType.Legendary, Type.Tool, SubType.Axe, 10000, 20, 5 },

                { "darksteelaxe", Material.IRON_AXE, 8, 1, "Dark Steel Axe", Rarity.RarityType.Rare, Type.Tool, SubType.Axe, 10000, 21, 6 },
                { "shadowaxe", Material.GOLDEN_AXE, 8, 1, "Shadow Axe", Rarity.RarityType.Rare, Type.Tool, SubType.Axe, 10000, 22, 7 },
                { "crimsonaxe", Material.GOLDEN_AXE, 8, 1, "Crimson Axe", Rarity.RarityType.Rare, Type.Tool, SubType.Axe, 10000, 24, 8 },
                { "moltenaxe", Material.DIAMOND_AXE, 8, 1, "Molten Axe", Rarity.RarityType.Epic, Type.Tool, SubType.Axe, 10000, 26, 9 },
                { "meteoriteaxe", Material.DIAMOND_AXE, 8, 1, "Meteorite Axe", Rarity.RarityType.Legendary, Type.Tool, SubType.Axe, 10000, 28, 10 },
                { "reinforcedmeteoriteaxe", Material.NETHERITE_AXE, 8, 1, "Reinforced Meteorite Axe", Rarity.RarityType.Immortal, Type.Tool, SubType.Axe, 10000, 30, 10 },

                { "amethystaxe", Material.IRON_AXE, 9, 1, "Amethyst Axe", Rarity.RarityType.Epic, Type.Tool, SubType.Axe, 10000, 31, 11 },
                { "sapphireaxe", Material.GOLDEN_AXE, 9, 1, "Sapphire Axe", Rarity.RarityType.Epic, Type.Tool, SubType.Axe, 10000, 32, 12 },
                { "rubyaxe", Material.GOLDEN_AXE, 9, 1, "Ruby Axe", Rarity.RarityType.Epic, Type.Tool, SubType.Axe, 10000, 34, 13 },
                { "emeraldaxe", Material.DIAMOND_AXE, 9, 1, "Emerald Axe", Rarity.RarityType.Legendary, Type.Tool, SubType.Axe, 10000, 36, 14 },
                { "reinforcedrubyaxe", Material.DIAMOND_AXE, 9, 1, "Reinforced Ruby Axe", Rarity.RarityType.Immortal, Type.Tool, SubType.Axe, 10000, 38, 15 },
                { "reinforcedemeraldaxe", Material.NETHERITE_AXE, 9, 1, "Reinforced Emerald Axe", Rarity.RarityType.Divine, Type.Tool, SubType.Axe, 10000, 40, 15 },




                // Hoes
                { "woodenhoe", Material.WOODEN_HOE, 1, 1, "Wooden Hoe", Rarity.RarityType.Common, Type.Tool, SubType.Hoe, 100, 1, 0 },
                { "stonehoe", Material.STONE_HOE, 2, 1, "Stone Hoe", Rarity.RarityType.Common, Type.Tool, SubType.Hoe, 250, 2, 0 },
                { "ironhoe", Material.IRON_HOE, 3, 1, "Iron Hoe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Hoe, 500, 3, 0 },
                { "goldenhoe", Material.GOLDEN_HOE, 4, 1, "Golden Hoe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Hoe, 850, 4, 0 },
                { "diamondhoe", Material.DIAMOND_HOE, 5, 1, "Diamond Hoe", Rarity.RarityType.Rare, Type.Tool, SubType.Hoe, 1200, 5, 0 },
                { "netheritehoe", Material.NETHERITE_HOE, 6, 1, "Netherite Hoe", Rarity.RarityType.Epic, Type.Tool, SubType.Hoe, 2500, 6, 0 },

                { "reinforcedcopperhoe", Material.IRON_HOE, 7, 1, "Reinforced Copper Hoe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Hoe, 10000, 11, 1 },
                { "reinforcedredstonehoe", Material.IRON_HOE, 7, 1, "Reinforced Redstone Hoe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Hoe, 10000, 12, 2 },
                { "reinforcedlapishoe", Material.IRON_HOE, 7, 1, "Reinforced Lapis Hoe", Rarity.RarityType.Uncommon, Type.Tool, SubType.Hoe, 10000, 14, 3 },
                { "reinforcedgoldenhoe", Material.GOLDEN_HOE, 7, 1, "Reinforced Golden Hoe", Rarity.RarityType.Rare, Type.Tool, SubType.Hoe, 10000, 16, 4 },
                { "reinforceddiamondhoe", Material.DIAMOND_HOE, 7, 1, "Reinforced Diamond Hoe", Rarity.RarityType.Epic, Type.Tool, SubType.Hoe, 10000, 18, 5 },
                { "reinforcednetheritehoe", Material.NETHERITE_HOE, 7, 1, "Reinforced Netherite Hoe", Rarity.RarityType.Legendary, Type.Tool, SubType.Hoe, 10000, 20, 5 },

                { "darksteelhoe", Material.IRON_HOE, 8, 1, "Dark Steel Hoe", Rarity.RarityType.Rare, Type.Tool, SubType.Hoe, 10000, 21, 6 },
                { "shadowhoe", Material.GOLDEN_HOE, 8, 1, "Shadow Hoe", Rarity.RarityType.Rare, Type.Tool, SubType.Hoe, 10000, 22, 7 },
                { "crimsonhoe", Material.GOLDEN_HOE, 8, 1, "Crimson Hoe", Rarity.RarityType.Rare, Type.Tool, SubType.Hoe, 10000, 24, 8 },
                { "moltenhoe", Material.DIAMOND_HOE, 8, 1, "Molten Hoe", Rarity.RarityType.Epic, Type.Tool, SubType.Hoe, 10000, 26, 9 },
                { "meteoritehoe", Material.DIAMOND_HOE, 8, 1, "Meteorite Hoe", Rarity.RarityType.Legendary, Type.Tool, SubType.Hoe, 10000, 28, 10 },
                { "reinforcedmeteoritehoe", Material.NETHERITE_HOE, 8, 1, "Reinforced Meteorite Hoe", Rarity.RarityType.Immortal, Type.Tool, SubType.Hoe, 10000, 30, 10 },

                { "amethysthoe", Material.IRON_HOE, 9, 1, "Amethyst Hoe", Rarity.RarityType.Epic, Type.Tool, SubType.Hoe, 10000, 31, 11 },
                { "sapphirehoe", Material.GOLDEN_HOE, 9, 1, "Sapphire Hoe", Rarity.RarityType.Epic, Type.Tool, SubType.Hoe, 10000, 32, 12 },
                { "rubyhoe", Material.GOLDEN_HOE, 9, 1, "Ruby Hoe", Rarity.RarityType.Epic, Type.Tool, SubType.Hoe, 10000, 34, 13 },
                { "emeraldhoe", Material.DIAMOND_HOE, 9, 1, "Emerald Hoe", Rarity.RarityType.Legendary, Type.Tool, SubType.Hoe, 10000, 36, 14 },
                { "reinforcedrubyhoe", Material.DIAMOND_HOE, 9, 1, "Reinforced Ruby Hoe", Rarity.RarityType.Immortal, Type.Tool, SubType.Hoe, 10000, 38, 15 },
                { "reinforcedemeraldhoe", Material.NETHERITE_HOE, 9, 1, "Reinforced Emerald Hoe", Rarity.RarityType.Divine, Type.Tool, SubType.Hoe, 10000, 40, 15 },




                // Swords
                { "woodensword", Material.WOODEN_SWORD, 4, 1.2d, 2, "Wooden Sword", Rarity.RarityType.Common, Type.Weapon, SubType.Sword, 100, 1 },
                { "stonesword", Material.STONE_SWORD, 5, 1.1d, 3, "Stone Sword", Rarity.RarityType.Common, Type.Weapon, SubType.Sword, 250, 2 },
                { "ironsword", Material.IRON_SWORD, 6, 0.9d, 3, "Iron Sword", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Sword, 500, 3 },
                { "goldensword", Material.GOLDEN_SWORD, 7, 0.7d, 4, "Golden Sword", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Sword, 850, 4 },
                { "diamondsword", Material.DIAMOND_SWORD, 8, 0.5d, 5, "Diamond Sword", Rarity.RarityType.Rare, Type.Weapon, SubType.Sword, 1200, 5 },
                { "netheritesword", Material.NETHERITE_SWORD, 10, 0.3d, 6, "Netherite Sword", Rarity.RarityType.Epic, Type.Weapon, SubType.Sword, 2500, 6 },
                { "sharpnetheritesword", Material.NETHERITE_SWORD, 15, 0.3d, 6, "Sharp Netherite Sword", Rarity.RarityType.Legendary, Type.Weapon, SubType.Sword, 2500, 10 },
                { "bow", Material.BOW, 4, 1.5d, 10, "Bow", Rarity.RarityType.Common, Type.Weapon, SubType.Bow, 100, 1 },


                { "tungstenpickaxe", Material.GOLDEN_PICKAXE, 15, 7, "Tungsten Pickaxe", Rarity.RarityType.Epic, Type.Tool, SubType.Pickaxe, 10000, 50, 20 },
                { "tungstenaxe", Material.GOLDEN_AXE, 15, 7, "Tungsten Axe", Rarity.RarityType.Epic, Type.Tool, SubType.Axe, 10000, 50, 20 },
                { "tungstenshovel", Material.GOLDEN_SHOVEL, 15, 7, "Tungsten Shovel", Rarity.RarityType.Epic, Type.Tool, SubType.Shovel, 10000, 50, 20 },
                { "tungstenfishingrod", Material.GOLDEN_PICKAXE, 15, 7, "Tungsten Fishing Rod", Rarity.RarityType.Epic, Type.Tool, SubType.Fishing_Rod, 10000, 50, 20 },

                { "palladiumpickaxe", Material.GOLDEN_PICKAXE, 20, 10, "Palladium Pickaxe", Rarity.RarityType.Epic, Type.Tool, SubType.Pickaxe, 10000, 60, 25 },
                { "palladiumaxe", Material.GOLDEN_AXE, 20, 10, "Palladium Axe", Rarity.RarityType.Epic, Type.Tool, SubType.Axe, 10000, 60, 25 },
                { "palladiumshovel", Material.GOLDEN_SHOVEL, 20, 10, "Palladium Shovel", Rarity.RarityType.Epic, Type.Tool, SubType.Shovel, 10000, 60, 25 },
                { "palladiumfishingrod", Material.FISHING_ROD, 20, 10, "Palladium FishingRod", Rarity.RarityType.Epic, Type.Tool, SubType.Fishing_Rod, 10000, 60, 25 },

                { "cobaltpickaxe", Material.GOLDEN_PICKAXE, 30, 10, "Cobalt Pickaxe", Rarity.RarityType.Epic, Type.Tool, SubType.Pickaxe, 10000, 70, 30 },
                { "cobaltaxe", Material.GOLDEN_AXE, 30, 10, "Cobalt Axe", Rarity.RarityType.Epic, Type.Tool, SubType.Axe, 10000, 70, 30 },
                { "cobaltshovel", Material.GOLDEN_SHOVEL, 30, 10, "Cobalt Shovel", Rarity.RarityType.Epic, Type.Tool, SubType.Shovel, 10000, 70, 30 },
                { "cobaltfishingrod", Material.FISHING_ROD, 30, 10, "Cobalt Fishing Rod", Rarity.RarityType.Epic, Type.Tool, SubType.Fishing_Rod, 10000, 70, 30 },

                { "mithrilpickaxe", Material.DIAMOND_PICKAXE, 40, 15, "Mithril Pickaxe", Rarity.RarityType.Legendary, Type.Tool, SubType.Pickaxe, 10000, 80, 30 },
                { "mithrilaxe", Material.DIAMOND_AXE, 40, 15, "Mithril Axe", Rarity.RarityType.Legendary, Type.Tool, SubType.Axe, 10000, 80, 30 },
                { "mithrilshovel", Material.DIAMOND_SHOVEL, 40, 15, "Mithril Shovel", Rarity.RarityType.Legendary, Type.Tool, SubType.Shovel, 10000, 80, 30 },
                { "mithrilfishingrod", Material.FISHING_ROD, 40, 15, "Mithril Fishing Rod", Rarity.RarityType.Legendary, Type.Tool, SubType.Fishing_Rod, 10000, 80, 30 },

                { "orichalcumpickaxe", Material.DIAMOND_PICKAXE, 50, 15, "Orichalcum Pickaxe", Rarity.RarityType.Legendary, Type.Tool, SubType.Pickaxe, 10000, 90, 30 },
                { "orichalcumaxe", Material.DIAMOND_AXE, 50, 15, "Orichalcum Axe", Rarity.RarityType.Legendary, Type.Tool, SubType.Axe, 10000, 90, 30 },
                { "orichalcumshovel", Material.DIAMOND_SHOVEL, 50, 15, "Orichalcum Shovel", Rarity.RarityType.Legendary, Type.Tool, SubType.Shovel, 10000, 90, 30 },
                { "orichalcumfishingrod", Material.FISHING_ROD, 50, 15, "Orichalcum Fishing Rod", Rarity.RarityType.Legendary, Type.Tool, SubType.Fishing_Rod, 10000, 90, 30 },

                { "titaniumpickaxe", Material.DIAMOND_PICKAXE, 60, 20, "Titanium Pickaxe", Rarity.RarityType.Legendary, Type.Tool, SubType.Pickaxe, 10000, 100, 30 },
                { "titaniumaxe", Material.DIAMOND_AXE, 60, 20, "Titanium Axe", Rarity.RarityType.Legendary, Type.Tool, SubType.Axe, 10000, 100, 30 },
                { "titaniumshovel", Material.DIAMOND_SHOVEL, 60, 20, "Titanium Shovel", Rarity.RarityType.Legendary, Type.Tool, SubType.Shovel, 10000, 100, 30 },
                { "titaniumfishingrod", Material.FISHING_ROD, 60, 20, "Titanium Fishing Rod", Rarity.RarityType.Legendary, Type.Tool, SubType.Fishing_Rod, 10000, 100, 30 },

                { "adamantinepickaxe", Material.NETHERITE_PICKAXE, 70, 25, "Adamantine Pickaxe", Rarity.RarityType.Immortal, Type.Tool, SubType.Pickaxe, 10000, 120, 35 },
                { "adamantineaxe", Material.NETHERITE_AXE, 70, 25, "Adamantine Axe", Rarity.RarityType.Immortal, Type.Tool, SubType.Axe, 10000, 120, 35 },
                { "adamantineshovel", Material.NETHERITE_SHOVEL, 70, 25, "Adamantine Shovel", Rarity.RarityType.Immortal, Type.Tool, SubType.Shovel, 10000, 120, 35 },
                { "adamantinefishingrod", Material.FISHING_ROD, 70, 25, "Adamantine Fishing Rod", Rarity.RarityType.Immortal, Type.Tool, SubType.Fishing_Rod, 10000, 120, 35 },

                { "dragonitepickaxe", Material.NETHERITE_PICKAXE, 80, 30, "Dragonite Pickaxe", Rarity.RarityType.Immortal, Type.Tool, SubType.Pickaxe, 10000, 120, 35 },
                { "dragoniteaxe", Material.NETHERITE_AXE, 80, 30, "Dragonite Axe", Rarity.RarityType.Immortal, Type.Tool, SubType.Axe, 1000, 120, 35 },
                { "dragoniteshovel", Material.NETHERITE_SHOVEL, 80, 30, "Dragonite Shovel", Rarity.RarityType.Immortal, Type.Tool, SubType.Shovel, 10000, 120, 35 },
                { "dragonitefishingrod", Material.FISHING_ROD, 80, 30, "Dragonite Fishing Rod", Rarity.RarityType.Immortal, Type.Tool, SubType.Fishing_Rod, 10000, 120, 35 },

                { "voidsteelpickaxe", Material.NETHERITE_PICKAXE, 90, 40, "Voidsteel Pickaxe", Rarity.RarityType.Divine, Type.Tool, SubType.Pickaxe, 10000, 130, 40 },
                { "voidsteelaxe", Material.NETHERITE_AXE, 90, 40, "Voidsteel Axe", Rarity.RarityType.Divine, Type.Tool, SubType.Axe, 10000, 130, 40 },
                { "voidsteelshovel", Material.NETHERITE_SHOVEL, 90, 40, "Voidsteel Shovel", Rarity.RarityType.Divine, Type.Tool, SubType.Shovel, 10000, 130, 40 },
                { "voidsteelfishingrod", Material.FISHING_ROD, 90, 40, "Voidsteel Fishing Rod", Rarity.RarityType.Divine, Type.Tool, SubType.Fishing_Rod, 10000, 130, 40 },

                { "etheriumpickaxe", Material.NETHERITE_PICKAXE, 100, 45, "Etherium Pickaxe", Rarity.RarityType.Divine, Type.Tool, SubType.Pickaxe, 10000, 150, 40 },
                { "etheriumaxe", Material.NETHERITE_AXE, 100, 45, "Etherium Axe", Rarity.RarityType.Divine, Type.Tool, SubType.Axe, 10000, 150, 40 },
                { "etheriumshovel", Material.NETHERITE_SHOVEL, 100, 45, "Etherium Shovel", Rarity.RarityType.Divine, Type.Tool, SubType.Shovel, 10000, 150, 40 },
                { "etheriumfishingrod", Material.FISHING_ROD, 100, 45, "Etherium Fishing Rod", Rarity.RarityType.Divine, Type.Tool, SubType.Fishing_Rod, 10000, 150, 40 },




                // Armor
                { "leatherhelmet", Material.LEATHER_HELMET, 1, 1, 0, "Leather Helmet", Rarity.RarityType.Common, Type.Equipment, SubType.Helmet, 100, 1 },
                { "leatherchestplate", Material.LEATHER_CHESTPLATE, 3, 1, 0, "Leather Chestplate", Rarity.RarityType.Common, Type.Equipment, SubType.Chestplate, 100, 1 },
                { "leatherleggings", Material.LEATHER_LEGGINGS, 2, 1, 0, "Leather Leggings", Rarity.RarityType.Common, Type.Equipment, SubType.Leggings, 100, 1 },
                { "leatherboots", Material.LEATHER_BOOTS, 1, 1, 0, "Leather Boots", Rarity.RarityType.Common, Type.Equipment, SubType.Boots, 100, 1 },

                { "chainmailhelmet", Material.CHAINMAIL_HELMET, 2, 2, 0, "Chainmail Helmet", Rarity.RarityType.Uncommon, Type.Equipment, SubType.Helmet, 250, 2 },
                { "chainmailchestplate", Material.CHAINMAIL_CHESTPLATE, 4, 2, 0, "Chainmail Chestplate", Rarity.RarityType.Uncommon, Type.Equipment, SubType.Chestplate, 250, 2 },
                { "chainmailleggings", Material.CHAINMAIL_LEGGINGS, 3, 2, 0, "Chainmail Leggings", Rarity.RarityType.Uncommon, Type.Equipment, SubType.Leggings, 250, 2 },
                { "chainmailboots", Material.CHAINMAIL_BOOTS, 2, 2, 0, "Chainmail Boots", Rarity.RarityType.Uncommon, Type.Equipment, SubType.Boots, 250, 2 },

                { "ironhelmet", Material.IRON_HELMET, 5, 3, 0, "Iron Helmet", Rarity.RarityType.Rare, Type.Equipment, SubType.Helmet, 500, 3 },
                { "ironchestplate", Material.IRON_CHESTPLATE, 7, 3, 0, "Iron Chestplate", Rarity.RarityType.Rare, Type.Equipment, SubType.Chestplate, 500, 3 },
                { "ironleggings", Material.IRON_LEGGINGS, 6, 3, 0, "Iron Leggings", Rarity.RarityType.Rare, Type.Equipment, SubType.Leggings, 500, 3 },
                { "ironboots", Material.IRON_BOOTS, 5, 3, 0, "Iron Boots", Rarity.RarityType.Rare, Type.Equipment, SubType.Boots, 500, 3 },

                { "goldenhelmet", Material.GOLDEN_HELMET, 9, 4, 0, "Golden Helmet", Rarity.RarityType.Epic, Type.Equipment, SubType.Helmet, 850, 4 },
                { "goldenchestplate", Material.GOLDEN_CHESTPLATE, 11, 4, 0, "Golden Chestplate", Rarity.RarityType.Epic, Type.Equipment, SubType.Chestplate, 850, 4 },
                { "goldenleggings", Material.GOLDEN_LEGGINGS, 10, 4, 0, "Golden Leggings", Rarity.RarityType.Epic, Type.Equipment, SubType.Leggings, 850, 4 },
                { "goldenboots", Material.GOLDEN_BOOTS, 9, 4, 0, "Golden Boots", Rarity.RarityType.Epic, Type.Equipment, SubType.Boots, 850, 4 },

                { "diamondhelmet", Material.DIAMOND_HELMET, 11, 5, 0, "Diamond Helmet", Rarity.RarityType.Legendary, Type.Equipment, SubType.Helmet, 1200, 5 },
                { "diamondchestplate", Material.DIAMOND_CHESTPLATE, 15, 5, 0, "Diamond Chestplate", Rarity.RarityType.Legendary, Type.Equipment, SubType.Chestplate, 1200, 5 },
                { "diamondleggings", Material.DIAMOND_LEGGINGS, 12, 5, 0, "Diamond Leggings", Rarity.RarityType.Legendary, Type.Equipment, SubType.Leggings, 1200, 5 },
                { "diamondboots", Material.DIAMOND_BOOTS, 11, 5, 0, "Diamond Boots", Rarity.RarityType.Legendary, Type.Equipment, SubType.Boots, 1200, 5 },

                { "netheritehelmet", Material.NETHERITE_HELMET, 15, 8, 0, "Netherite Helmet", Rarity.RarityType.Immortal, Type.Equipment, SubType.Helmet, 2500, 6 },
                { "netheritechestplate", Material.NETHERITE_CHESTPLATE, 20, 12, 0, "Netherite Chestplate", Rarity.RarityType.Immortal, Type.Equipment, SubType.Chestplate, 2500, 6 },
                { "netheriteleggings", Material.NETHERITE_LEGGINGS, 18, 10, 0, "Netherite Leggings", Rarity.RarityType.Immortal, Type.Equipment, SubType.Leggings, 2500, 6 },
                { "netheriteboots", Material.NETHERITE_BOOTS, 15, 7, 0, "Netherite Boots", Rarity.RarityType.Immortal, Type.Equipment, SubType.Boots, 2500, 6 },

        };

        // Iterate and create factory items
        for (Object[] data : toolData) {
            String key = (String) data[0];
            Material material = (Material) data[1];
            if (material.toString().contains("PICKAXE") ||
            material.toString().contains("AXE")||
                    material.toString().contains("SHOVEL")||
                    material.toString().contains("HOE")||
                    material.toString().contains("FISHING")){
                int toolPower = (int) data[2];
                int toolSpeed = (int) data[3];
                String displayName = (String) data[4];
                Rarity.RarityType rarity = (Rarity.RarityType) data[5];
                Type type = (Type) data[6];
                SubType subType = (SubType) data[7];
                int durability = (int) data[8];

                int levelMinimum = (int) data[9];

                int fortune = (int) data[10];

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
                item.setSteamConsumption(0);
                item.setHealth(0);
                item.setSteam(0);
                item.setArmor(0);

                item.setLevelMinimum(levelMinimum);

                if (material.toString().contains("PICKAXE")){
                    item.setMiningFortune(fortune);
                }
                else if (material.toString().contains("AXE")){
                    item.setForagingFortune(fortune);
                }
                else if (material.toString().contains("SHOVEL")){
                    item.setMiningFortune(fortune);
                }
                else if (material.toString().contains("HOE")){
                    item.setFarmingFortune(fortune);
                }
                else if (material.toString().contains("FISHING")){
                    item.setFishingFortune(fortune);
                }

                if (key.contains("tungsten") || key.contains("palladium")
                || key.contains("cobalt") ||

                key.contains("mithril") || key.contains("orichalcum")
                        || key.contains("titanium") ||

                key.contains("adamantine") || key.contains("dragonite")
                        || key.contains("voidsteel")|| key.contains("etherium")){
                    item.setProficiency(100);
                }

                factoryItemList.put(key, item);
            }

            else if (material.toString().contains("SWORD") ||
                    material.toString().contains("BOW")){
                int attackDamage = (int) data[2];
                double attackSpeed = (double) data[3];
                int attackRange = (int) data[4];
                String displayName = (String) data[5];
                Rarity.RarityType rarity = (Rarity.RarityType) data[6];
                Type type = (Type) data[7];
                SubType subType = (SubType) data[8];
                int durability = (int) data[9];

                int levelMinimum = (int) data[10];

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
                item.setHealth(0);
                item.setSteam(0);
                item.setArmor(0);
                item.setCriticalChance(10);

                item.setLevelMinimum(levelMinimum);

                if (material.toString().contains("SWORD")){
                    item.setAttackEffect(AttackEffect.Slash);
                }
                else if (material.toString().contains("BOW")){
                    item.setAttackEffect(AttackEffect.Arrow);
                }
                else if (material.toString().contains("GUN")){
                    item.setAttackEffect(AttackEffect.Bullet);
                }

                factoryItemList.put(key, item);
            }

            else if (material.toString().contains("HELMET") ||
                    material.toString().contains("CHESTPLATE") ||
                    material.toString().contains("LEGGINGS")||
                    material.toString().contains("BOOTS")){
                int health = (int) data[2];
                int armor = (int) data[3];
                int steam = (int) data[4];
                String displayName = (String) data[5];
                Rarity.RarityType rarity = (Rarity.RarityType) data[6];
                Type type = (Type) data[7];
                SubType subType = (SubType) data[8];
                int durability = (int) data[9];

                int levelMinimum = (int) data[10];

                FactoryItem item = new FactoryItem();
                item.setType(type);
                item.setSubType(subType);
                item.setHealth(health);
                item.setArmor(armor);
                item.setSteam(steam);
                item.setMaterial(material);
                item.setDisplayname(displayName);
                item.setRarity(rarity);
                item.setDurability(durability);
                item.setMaxDurability(durability);
                item.setSteamConsumption(0);

                item.setLevelMinimum(levelMinimum);

                factoryItemList.put(key, item);
            }

        }

        for (String key : factoryItemList.keySet()) {
            SaveItem(key, factoryItemList.get(key).build().clone());
        }

        /*List<Material> vanillaMaterial = Arrays.asList(

                Material.COPPER_INGOT,
                Material.RAW_COPPER,
                Material.COPPER_ORE,

                Material.IRON_INGOT,
                Material.RAW_IRON,
                Material.IRON_ORE,

                Material.GOLD_INGOT,
                Material.RAW_GOLD,
                Material.GOLD_ORE,

                Material.DIAMOND,
                Material.EMERALD,
                Material.NETHERITE_SCRAP,
                Material.NETHERITE_INGOT

        );

        for (Material mat : vanillaMaterial){
            ItemStack addedItem = new ItemStack(mat);

            SaveItem(mat.toString().toLowerCase().replaceAll("_", "").trim(), ProcessItemMeta(addedItem));
        }*/

        for (int i = 1; i < 7; i++) {
            ItemStack backpackItem = new ItemStack(CreateBackpack(i));
            SaveItem("backpack"+i, backpackItem);
        }

        for (int i = 1; i < 11; i++) {
            ItemStack pendant = new ItemStack(getQuestPendant(i));
            SaveItem("questpendanttier"+i, pendant);
        }
        for (int i = 1; i < 11; i++) {
            ItemStack pendant = new ItemStack(getDungeonPendant(i));
            SaveItem("dungeonpendanttier"+i, pendant);
        }

        List<Rarity.RarityType> rarityList = Arrays.asList(Rarity.RarityType.Common, Rarity.RarityType.Uncommon, Rarity.RarityType.Rare,
                Rarity.RarityType.Epic, Rarity.RarityType.Legendary, Rarity.RarityType.Immortal);
        int wandCount = 1;
        double wandValue = 1;
        double multiplierCount = 0.1;
        for (Rarity.RarityType rt : rarityList){
            ItemStack pendant = new ItemStack(GetSellWand(wandValue+multiplierCount, rt));
            SaveItem("sellwand"+wandCount, pendant);
            wandCount++;
            multiplierCount += 0.2;
        }

        int sellBoosterCount = 1;

        int expBoosterCount = 1;
        for (Booster.BoosterType boosterType : Booster.BoosterType.values()){
            if (uncolouredText(boosterType.toString()).toLowerCase().replaceAll("_", "").trim().contains("sell")){
                String key = uncolouredText(boosterType.toString().toLowerCase()).replaceAll(" ", "").trim()
                        .replaceAll("percent", "").trim().replaceAll("_", "").trim()+"booster"+sellBoosterCount;

                SaveItem(key, GetBooster(GetBoosterPercent(boosterType), "Profit", boosterType, PotionType.HARMING));
                sellBoosterCount++;
            }
            else if (uncolouredText(boosterType.toString()).toLowerCase().replaceAll("_", "").trim().contains("exp")){
                String key = uncolouredText(boosterType.toString().toLowerCase()).replaceAll(" ", "").trim()
                        .replaceAll("percent", "").trim().replaceAll("_", "").trim()+"booster"+expBoosterCount;
                SaveItem(key, GetBooster(GetBoosterPercent(boosterType),"Focus", boosterType, PotionType.HARMING));
                expBoosterCount++;
            }
        }

        SaveItem("machinelicense1", GetMachineLicense(1));
        SaveItem("machinelicense2", GetMachineLicense(2));
        SaveItem("machinelicense3", GetMachineLicense(5));
        SaveItem("machinelicense4", GetMachineLicense(10));

        List<String> hookMaterial = Arrays.asList(
                "Wooden_Hook", "Flint_Hook", "Iron_Hook", "Golden_Hook", "Diamond_Hook", "Netherite_Hook");

        List<Rarity.RarityType> hookRarity = Arrays.asList( Rarity.RarityType.Common, Rarity.RarityType.Uncommon, Rarity.RarityType.Rare,
                Rarity.RarityType.Epic, Rarity.RarityType.Legendary, Rarity.RarityType.Immortal
        );

        for (int i = 1; i < 7; i++) {
            FactoryItem hook = new FactoryItem();
            int index = i-1;
            hook.setDisplayname(sendText(formatItemName(hookMaterial.get(index))));
            hook.setRarity(hookRarity.get(index));
            hook.setType(Type.Tool);
            hook.setSubType(SubType.Hook);
            hook.setSteamConsumption(i*3);
            hook.setMaterial(Material.FISHING_ROD);
            hook.setToolPower(i);
            hook.setToolSpeed(i+2);
            hook.setLevelMinimum(i*10);

            SaveItem(hookMaterial.get(index).replaceAll("_", "").trim().toLowerCase(), hook.build());
        }

        List<String> hookMaterial2 = Arrays.asList(
                "Tungsten_Hook", "Palladium_Hook", "Cobalt_Hook",
                "Mithril_Hook", "Orichalcum_Hook", "Titanium_Hook"
                , "Adamantine_Hook", "Dragonite_Hook"
                , "Voidsteel_Hook", "Etherium_Hook");
        int hPower = 7;
        int hLevelMinimum = 50;
        for (int i = 1; i < hookMaterial2.size()+1; i++) {
            FactoryItem hook = new FactoryItem();
            int index = i-1;
            String hookName = hookMaterial2.get(index);
            hook.setDisplayname(sendText(formatItemName(hookName)));
            if (hookName.equals("Tungsten_Hook") || hookName.equals("Palladium_Hook") || hookName.equals("Cobalt_Hook")){
                hook.setRarity(Rarity.RarityType.Rare);
            }
            else if (hookName.equals("Mithril_Hook") || hookName.equals("Orichalcum_Hook") || hookName.equals("Titanium_Hook")){
                hook.setRarity(Rarity.RarityType.Epic);
            }
            else if (hookName.equals("Adamantine_Hook") || hookName.equals("Dragonite_Hook")){
                hook.setRarity(Rarity.RarityType.Legendary);
            }
            else if (hookName.equals("Voidsteel_Hook") || hookName.equals("Etherium_Hook")){
                hook.setRarity(Rarity.RarityType.Immortal);
            }

            hook.setType(Type.Tool);
            hook.setSubType(SubType.Hook);
            hook.setSteamConsumption(hPower*3);
            hook.setMaterial(Material.FISHING_ROD);
            hook.setToolPower(hPower);
            hook.setToolSpeed(hPower+2);
            hook.setLevelMinimum(hLevelMinimum);

            hPower += 2;
            hLevelMinimum += 10;

            SaveItem(hookMaterial2.get(index).replaceAll("_", "").trim().toLowerCase(), hook.build());
        }


        /*List<Dungeon.LootType> lootTypeList = Arrays.asList(Dungeon.LootType.Weapon, Dungeon.LootType.Equipment);
        List<Integer> lootLevelList = Arrays.asList(25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 105, 110);
        for (Integer lootLevel : lootLevelList){
            for (Dungeon.LootType lootType : lootTypeList){
                SaveItem(lootType.toString().toLowerCase()+"dungeonlootbox"+lootLevel, new ItemStack(CreateDungeonLootBox(lootType, lootLevel)));
            }
        }*/

        for (int i = 1; i < 11; i++) {
            SaveItem("equipmentdungeonlootbox"+i, new ItemStack(CreateDungeonLootBox(Dungeon.LootType.Equipment, i)));
        }


        List<String> membraneList = Arrays.asList("alien", "mutant", "undead");

        int maxTier = 10;
        for (int i = 1; i < maxTier+1; i++) {
            for (String m : membraneList){
                SaveItem(m+"membrane"+i, GetMembrane(m, i));
            }
        }

        InitMachineDrops();
        InitMachineItems();
        InitSpawners();
        InitEquipments();
        InitMinerals();
        InitFactoryCustomTools();
        InitWeapons();

        consoleLog(sendText("&aFactory items initialized successfully!"));
    }

    public static void InitMinerals(){
        // shadowsteel, palladium, cobalt | mithril, orichalcum, titanium | adamantine, dragonite
        // voidsteel, etherium

        List<String> part1 = Arrays.asList("Tungsten", "Palladium", "Cobalt");
        List<String> part2 = Arrays.asList("Mithril", "Orichalcum", "Titanium");
        List<String> part3 = Arrays.asList("Adamantine", "Dragonite");
        List<String> part4 = Arrays.asList("Voidsteel", "Etherium");

        for (String key : part1){
            ItemStack item = new ItemStack(Material.COPPER_INGOT);
            ItemMeta meta = item.getItemMeta();

            meta.addEnchant(Enchantment.UNBREAKING, 10, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            meta.setDisplayName(sendText("&6"+key+" Ingot"));
            List<String> itemLore = new ArrayList<>();
            itemLore.add(sendText("&9Material"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&6Rare"));

            meta.setLore(itemLore);
            item.setItemMeta(meta);
            SaveItem(key.toLowerCase()+"ingot", item.clone());
        }

        for (String key : part2){
            ItemStack item = new ItemStack(Material.IRON_INGOT);
            ItemMeta meta = item.getItemMeta();

            meta.addEnchant(Enchantment.UNBREAKING, 10, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            meta.setDisplayName(sendText("&4"+key+" Ingot"));
            List<String> itemLore = new ArrayList<>();
            itemLore.add(sendText("&9Material"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&4Epic"));

            meta.setLore(itemLore);
            item.setItemMeta(meta);
            SaveItem(key.toLowerCase()+"ingot", item.clone());
        }

        for (String key : part3){
            ItemStack item = new ItemStack(Material.GOLD_INGOT);
            ItemMeta meta = item.getItemMeta();

            meta.addEnchant(Enchantment.UNBREAKING, 10, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            meta.setDisplayName(sendText("&b"+key+" Ingot"));
            List<String> itemLore = new ArrayList<>();
            itemLore.add(sendText("&9Material"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&bLegendary"));

            meta.setLore(itemLore);
            item.setItemMeta(meta);
            SaveItem(key.toLowerCase()+"ingot", item.clone());
        }
        for (String key : part4){
            ItemStack item = new ItemStack(Material.NETHERITE_INGOT);
            ItemMeta meta = item.getItemMeta();

            meta.addEnchant(Enchantment.UNBREAKING, 10, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            meta.setDisplayName(sendText("&5"+key+" Ingot"));
            List<String> itemLore = new ArrayList<>();
            itemLore.add(sendText("&9Material"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&5Immortal"));

            meta.setLore(itemLore);
            item.setItemMeta(meta);
            SaveItem(key.toLowerCase()+"ingot", item.clone());
        }
    }

    public static void InitEquipments(){

        Map<String, Color> preparedEquipments = new HashMap<>();

// 🌾 Crops
        preparedEquipments.put("wheat", Color.fromRGB(255, 223, 0)); // Golden Yellow
        preparedEquipments.put("barley", Color.fromRGB(194, 178, 128)); // Light Brown
        preparedEquipments.put("corn", Color.fromRGB(255, 216, 0)); // Bright Gold
        preparedEquipments.put("carrot", Color.fromRGB(255, 117, 24)); // Deep Orange
        preparedEquipments.put("potato", Color.fromRGB(218, 165, 32)); // Golden Brown
        preparedEquipments.put("beetroot", Color.fromRGB(156, 0, 60)); // Deep Beet Red
        preparedEquipments.put("whiteonion", Color.fromRGB(255, 250, 240)); // Off-White
        preparedEquipments.put("redonion", Color.fromRGB(160, 32, 240)); // Purple

// 🥦 Vegetables
        preparedEquipments.put("lettuce", Color.fromRGB(154, 205, 50)); // Lettuce Green
        preparedEquipments.put("cabbage", Color.fromRGB(85, 107, 47)); // Dark Green
        preparedEquipments.put("broccoli", Color.fromRGB(34, 139, 34)); // Forest Green
        preparedEquipments.put("cauliflower", Color.fromRGB(245, 245, 220)); // Beige White
        preparedEquipments.put("radish", Color.fromRGB(255, 0, 102)); // Bright Reddish-Pink
        preparedEquipments.put("cucumber", Color.fromRGB(50, 205, 50)); // Fresh Green
        preparedEquipments.put("greenbeans", Color.fromRGB(0, 128, 0)); // Deep Green
        preparedEquipments.put("eggplant", Color.fromRGB(138, 43, 226)); // Eggplant Purple
        preparedEquipments.put("chilipepper", Color.fromRGB(220, 20, 60)); // Dark Red

// 🍏 Fruits
        preparedEquipments.put("apple", Color.fromRGB(255, 0, 0)); // Bright Red
        preparedEquipments.put("banana", Color.fromRGB(255, 255, 102)); // Banana Yellow
        preparedEquipments.put("orange", Color.fromRGB(255, 140, 0)); // Rich Orange
        preparedEquipments.put("mango", Color.fromRGB(255, 165, 0)); // Vibrant Orange
        preparedEquipments.put("pineapple", Color.fromRGB(255, 223, 0)); // Golden Yellow
        preparedEquipments.put("grape", Color.fromRGB(75, 0, 130)); // Dark Indigo
        preparedEquipments.put("melon", Color.fromRGB(154, 205, 50)); // Light Green
        preparedEquipments.put("pumpkin", Color.fromRGB(255, 117, 24)); // Deep Orange
        preparedEquipments.put("strawberry", Color.fromRGB(220, 20, 60)); // Deep Crimson
        preparedEquipments.put("blueberry", Color.fromRGB(25, 25, 112)); // Dark Blue
        preparedEquipments.put("blackberry", Color.fromRGB(0, 0, 0)); // Pure Black
        preparedEquipments.put("kiwi", Color.fromRGB(139, 69, 19)); // Brown
        preparedEquipments.put("lemon", Color.fromRGB(255, 255, 0)); // Lemon Yellow
        preparedEquipments.put("peach", Color.fromRGB(255, 218, 185)); // Peachy
        preparedEquipments.put("papaya", Color.fromRGB(255, 165, 79)); // Light Orange

// 🍄 Mushrooms
        preparedEquipments.put("purplemushroom", Color.fromRGB(147, 112, 219)); // Light Purple
        preparedEquipments.put("redmushroom", Color.fromRGB(237, 41, 57)); // Bright Red
        preparedEquipments.put("bluemushroom", Color.fromRGB(70, 130, 180)); // Steel Blue
        preparedEquipments.put("brownmushroom", Color.fromRGB(123, 63, 0)); // Mushroom Brown
        preparedEquipments.put("greenmushroom", Color.fromRGB(34, 139, 34)); // Dark Green
        preparedEquipments.put("pinkmushroom", Color.fromRGB(255, 105, 180)); // Hot Pink
        preparedEquipments.put("yellowmushroom", Color.fromRGB(255, 255, 51)); // Bright Yellow

// 🌲 Logs
        preparedEquipments.put("cherrylog", Color.fromRGB(255, 182, 193)); // Soft Pink
        preparedEquipments.put("birchlog", Color.fromRGB(245, 222, 179)); // Birch Beige
        preparedEquipments.put("crimsonstem", Color.fromRGB(153, 0, 0)); // Dark Red
        preparedEquipments.put("junglelog", Color.fromRGB(139, 69, 19)); // Brown
        preparedEquipments.put("mangrovelog", Color.fromRGB(178, 34, 34)); // Firebrick Red
        preparedEquipments.put("sprucelog", Color.fromRGB(101, 67, 33)); // Dark Wood
        preparedEquipments.put("warpedstem", Color.fromRGB(0, 139, 139)); // Teal
        preparedEquipments.put("oaklog", Color.fromRGB(160, 82, 45)); // Wood Brown
        preparedEquipments.put("acacialog", Color.fromRGB(210, 105, 30)); // Reddish Brown
        preparedEquipments.put("darkoaklog", Color.fromRGB(92, 51, 23)); // Darkest Brown

// 🍁 Leaves
        preparedEquipments.put("oakleaves", Color.fromRGB(34, 139, 34)); // Oak Green
        preparedEquipments.put("pinkleaves", Color.fromRGB(255, 182, 193)); // Light Pink
        preparedEquipments.put("purpleleaves", Color.fromRGB(147, 112, 219)); // Purple
        preparedEquipments.put("redleaves", Color.fromRGB(237, 41, 57)); // Bright Red
        preparedEquipments.put("wisterialeaves", Color.fromRGB(173, 216, 230)); // Light Blue
        preparedEquipments.put("fallleaves", Color.fromRGB(255, 140, 0)); // Autumn Orange
        preparedEquipments.put("autumnleaves", Color.fromRGB(255, 215, 0)); // Golden Yellow
        preparedEquipments.put("azalealeaves", Color.fromRGB(154, 205, 50)); // Light Green
        preparedEquipments.put("acacialeaves", Color.fromRGB(255, 140, 0)); // Orange
        preparedEquipments.put("spruceleaves", Color.fromRGB(46, 139, 87)); // Dark Green
        preparedEquipments.put("sakuraleaves", Color.fromRGB(255, 105, 180)); // Sakura Pink
        preparedEquipments.put("birchleaves", Color.fromRGB(245, 222, 179)); // Beige
        preparedEquipments.put("cherryleaves", Color.fromRGB(255, 105, 180)); // Hot Pink
        preparedEquipments.put("jungleleaves", Color.fromRGB(34, 139, 34)); // Jungle Green
        preparedEquipments.put("mangroveleaves", Color.fromRGB(46, 139, 87)); // Deep Green

        // new

        preparedEquipments.put("limestone", Color.fromRGB(210, 210, 180)); // Pale Stone Beige
        preparedEquipments.put("marble", Color.fromRGB(255, 250, 250)); // White Marble
        preparedEquipments.put("basalt", Color.fromRGB(70, 70, 90)); // Dark Grayish Purple
        preparedEquipments.put("slate", Color.fromRGB(112, 128, 144)); // Slate Gray
        preparedEquipments.put("obsidian", Color.fromRGB(53, 56, 57)); // Obsidian Black
        preparedEquipments.put("quartz", Color.fromRGB(240, 240, 255)); // Icy White
        preparedEquipments.put("granite", Color.fromRGB(198, 145, 120)); // Brownish Pink
        preparedEquipments.put("coal", Color.fromRGB(54, 69, 79)); // Coal Gray
        preparedEquipments.put("copper", Color.fromRGB(184, 115, 51)); // Copper Orange
        preparedEquipments.put("tin", Color.fromRGB(192, 192, 192)); // Tin Silver
        preparedEquipments.put("iron", Color.fromRGB(160, 160, 160)); // Iron Gray
        preparedEquipments.put("lead", Color.fromRGB(120, 120, 120)); // Lead Gray
        preparedEquipments.put("aluminium", Color.fromRGB(200, 200, 200)); // Light Silver
        preparedEquipments.put("silver", Color.fromRGB(192, 192, 192)); // Metallic Silver
        preparedEquipments.put("gold", Color.fromRGB(255, 215, 0)); // Gold
        preparedEquipments.put("platinum", Color.fromRGB(229, 228, 226)); // Platinum White
        preparedEquipments.put("amethyst", Color.fromRGB(153, 102, 204)); // Violet
        preparedEquipments.put("garnet", Color.fromRGB(115, 0, 0)); // Deep Red
        preparedEquipments.put("topaz", Color.fromRGB(255, 200, 50)); // Golden Yellow
        preparedEquipments.put("jade", Color.fromRGB(0, 168, 107)); // Jade Green
        preparedEquipments.put("aquamarine", Color.fromRGB(127, 255, 212)); // Aqua Blue-Green
        preparedEquipments.put("sapphire", Color.fromRGB(15, 82, 186)); // Deep Blue
        preparedEquipments.put("ruby", Color.fromRGB(224, 17, 95)); // Crimson Red
        preparedEquipments.put("emerald", Color.fromRGB(80, 200, 120)); // Emerald Green
        preparedEquipments.put("diamond", Color.fromRGB(185, 242, 255)); // Light Cyan
        preparedEquipments.put("netherite", Color.fromRGB(48, 25, 52)); // Dark Purple-Gray

        preparedEquipments.put("tungsten", Color.fromRGB(105, 105, 105));     // Dark gray, metallic
        preparedEquipments.put("palladium", Color.fromRGB(190, 192, 194));    // Silvery-white metal
        preparedEquipments.put("cobalt", Color.fromRGB(0, 71, 171));          // Deep cobalt blue

        preparedEquipments.put("mithril", Color.fromRGB(173, 216, 230));      // Light silvery blue, fantasy metal
        preparedEquipments.put("orichalcum", Color.fromRGB(255, 120, 0));     // Bright orange-gold, mythical metal
        preparedEquipments.put("titanium", Color.fromRGB(135, 134, 129));     // Light gray metal with slight blue tint

        preparedEquipments.put("adamantine", Color.fromRGB(50, 50, 80));      // Deep bluish metal, tough fantasy alloy
        preparedEquipments.put("dragonite", Color.fromRGB(255, 60, 60));      // Intense red, fiery and rare

        preparedEquipments.put("voidsteel", Color.fromRGB(25, 25, 40));       // Dark purplish black, void-like
        preparedEquipments.put("etherium", Color.fromRGB(120, 0, 255));       // Magical purple, glowing fantasy metal




        List<String> equipmentVariants = Arrays.asList("helmet", "chestplate", "leggings", "boots");

        for (String variant : equipmentVariants){
            String materialKey = "leather_"+variant;

            FactoryItem item = new FactoryItem();

            for (String key : preparedEquipments.keySet()){
                consoleLog(sendText("&aProcessing Carbon Equipment: &2"+key));
                item.setType(Type.Equipment);
                item.setSubType(SubType.parseSubType(variant));

                Integer levelMinimum = GetLevelMinimum(key+"machine");
                if (levelMinimum == null){
                    levelMinimum = 1;
                    consoleLog(sendText("&aLevel minimum of &2"+key+" &awas null!"));
                }

                double percent = switch (variant) {
                    case "helmet" -> 0.3;
                    case "chestplate" -> 0.8;
                    case "leggings" -> 0.7;
                    case "boots" -> 0.45;
                    default -> 0.1;
                };

                double steam = levelMinimum*percent;

                item.setSteam(steam);
                item.setHealth(0);
                item.setArmor(0);

                item.setLevelMinimum(levelMinimum);

                String displayname = key.replaceAll("(log|leaves|mushroom|stem)$", "_$1");
                item.setDisplayname(formatItemName("carbon_"+displayname+"_"+variant));

                item.setMaterial(Material.getMaterial(materialKey.toUpperCase()));

                ItemStack resultItem = new ItemStack(item.build());
                LeatherArmorMeta meta = (LeatherArmorMeta) resultItem.getItemMeta();

                meta.setColor(preparedEquipments.get(key));

                resultItem.setItemMeta(meta);

                SaveItem("carbon"+key+variant, resultItem.clone());
            }
        }

    }

    public static void InitSpawners(){
        SaveItem("pigspawner", CreateSpawner(EntityType.PIG));
        SaveItem("chickenspawner", CreateSpawner(EntityType.CHICKEN));
        SaveItem("sheepspawner", CreateSpawner(EntityType.SHEEP));
        SaveItem("cowspawner", CreateSpawner(EntityType.COW));
        SaveItem("rabbitspawner", CreateSpawner(EntityType.RABBIT));

        SaveItem("zombiespawner", CreateSpawner(EntityType.ZOMBIE));
        SaveItem("skeletonspawner", CreateSpawner(EntityType.SKELETON));
        SaveItem("spiderspawner", CreateSpawner(EntityType.SPIDER));
        SaveItem("creeperspawner", CreateSpawner(EntityType.CREEPER));
        SaveItem("endermanspawner", CreateSpawner(EntityType.ENDERMAN));
        SaveItem("blazespawner", CreateSpawner(EntityType.BLAZE));
        SaveItem("guardianspawner", CreateSpawner(EntityType.GUARDIAN));
    }

    public static HashMap<String, List<ItemStack>> itemDatabase = new HashMap<>();

    static void InitMachineDrops(){

        // 🥦🌶🍆🔴🌿🧅🥒🥬🥕🥔

        List<String> itemLore = new ArrayList<>();
        itemLore.add(sendText("&9Machine Product"));

        List<String> dropList = Arrays.asList(

                "steam",

                "wheat", "barley", "corn",

                "carrot", "potato", "beetroot", "whiteonion", "redonion", "lettuce",
                "cabbage", "broccoli", "cauliflower", "radish", "cucumber", "greenbeans",
                "eggplant", "chilipepper",
                "apple", "banana", "orange", "mango", "pineapple", "grape", "melon",
                "pumpkin", "strawberry", "blueberry", "blackberry", "kiwi", "lemon", "peach",
                "papaya",


                "purplemushroom", "redmushroom", "bluemushroom",
                "brownmushroom", "greenmushroom", "pinkmushroom", "yellowmushroom", "cherrylog", "birchlog",
                "crimsonstem", "junglelog", "mangrovelog", "sprucelog", "warpedstem", "oaklog",
                "acacialog", "darkoaklog",
                "oakleaves", "pinkleaves", "purpleleaves", "redleaves", "wisterialeaves", "fallleaves", "autumnleaves",
                "azalealeaves", "acacialeaves", "spruceleaves", "sakuraleaves", "birchleaves", "cherryleaves", "jungleleaves",
                "mangroveleaves",

                "limestone", "marble", "basalt", "slate", "obsidian", "quartz", "granite",

                "coal", "iron", "copper", "platinum", "tin", "lead", "aluminium", "silver", "gold",

                "amethyst", "garnet", "topaz", "jade", "aquamarine", "sapphire", "ruby", "emerald", "diamond", "netherite",

                "tungsten", "palladium", "cobalt",
                "mithril", "orichalcum", "titanium",
                "adamantine", "dragonite", "voidsteel", "etherium"

        );

        for (String drop : dropList){
            ItemStack item = new ItemStack(GetItem(drop+"model"));
            ItemMeta meta = item.getItemMeta();

            PersistentDataContainer container = meta.getPersistentDataContainer();
            Double worth = GetWorth(drop+"_custom");
            if (worth == null){
                worth = 0.0;
            }

            container.set(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE, worth);

            drop = drop.replaceAll("(log|leaves|mushroom|stem)$", "_$1");
            meta.setDisplayName(sendText("&a"+formatItemName(drop)));
            meta.setLore(itemLore);
            item.setItemMeta(meta);

            item.setItemMeta(ProcessItemMeta(item).getItemMeta());
            meta = item.getItemMeta();
            List<String> lastLore = meta.getLore();
            lastLore.add(sendText("&aCommon"));

            meta.setLore(lastLore);
            item.setItemMeta(meta);

            SaveItem(drop.replaceAll("_", "").trim(), item.clone());
            //itemList.remove(drop);
        }

    }

    static void InitMachineItems() {
        // Farm Machine
        List<ItemStack> storedItems = new ArrayList<>();
        String name;
        ItemStack m;

        // farm - grains, vegetables, and fruits
        Map<String, Material> preparedMachines = new HashMap<>();

        // Grains
        preparedMachines.put("wheat", Material.HAY_BLOCK);
        preparedMachines.put("barley", Material.DIRT_PATH);
        preparedMachines.put("corn", Material.BAMBOO_MOSAIC);

        // Vegetables
        preparedMachines.put("carrot", Material.ORANGE_TERRACOTTA);
        preparedMachines.put("potato", Material.YELLOW_TERRACOTTA);
        preparedMachines.put("beetroot", Material.RED_TERRACOTTA);
        preparedMachines.put("whiteonion", Material.WHITE_TERRACOTTA);
        preparedMachines.put("redonion", Material.MAGENTA_TERRACOTTA);
        preparedMachines.put("lettuce", Material.GREEN_TERRACOTTA);
        preparedMachines.put("cabbage", Material.LIME_TERRACOTTA);
        preparedMachines.put("broccoli", Material.JUNGLE_LEAVES);
        preparedMachines.put("cauliflower", Material.ANDESITE);
        preparedMachines.put("radish", Material.NETHER_WART_BLOCK);
        preparedMachines.put("cucumber", Material.MOSS_BLOCK);
        preparedMachines.put("greenbeans", Material.GREEN_WOOL);
        preparedMachines.put("eggplant", Material.PURPLE_CONCRETE);
        preparedMachines.put("chilipepper", Material.FIRE_CORAL_BLOCK);

        // Fruits
        preparedMachines.put("apple", Material.RED_WOOL);
        preparedMachines.put("banana", Material.SANDSTONE);
        preparedMachines.put("orange", Material.HONEYCOMB_BLOCK);
        preparedMachines.put("mango", Material.MANGROVE_LOG);
        preparedMachines.put("pineapple", Material.YELLOW_WOOL);
        preparedMachines.put("grape", Material.PURPLE_CONCRETE);
        preparedMachines.put("melon", Material.MELON);
        preparedMachines.put("pumpkin", Material.PUMPKIN);
        preparedMachines.put("strawberry", Material.RED_CONCRETE);
        preparedMachines.put("blueberry", Material.BLUE_CONCRETE);
        preparedMachines.put("blackberry", Material.BLACK_WOOL);
        preparedMachines.put("kiwi", Material.BROWN_WOOL);
        preparedMachines.put("lemon", Material.HORN_CORAL_BLOCK);
        preparedMachines.put("peach", Material.HONEY_BLOCK);
        preparedMachines.put("papaya", Material.BROWN_WOOL);

        // Mushrooms
        preparedMachines.put("purplemushroom", Material.PURPLE_GLAZED_TERRACOTTA);
        preparedMachines.put("redmushroom", Material.RED_MUSHROOM_BLOCK);
        preparedMachines.put("bluemushroom", Material.BLUE_GLAZED_TERRACOTTA);
        preparedMachines.put("brownmushroom", Material.BROWN_MUSHROOM_BLOCK);
        preparedMachines.put("greenmushroom", Material.GREEN_GLAZED_TERRACOTTA);
        preparedMachines.put("pinkmushroom", Material.PINK_GLAZED_TERRACOTTA);
        preparedMachines.put("yellowmushroom", Material.YELLOW_GLAZED_TERRACOTTA);

        // Logs
        preparedMachines.put("cherrylog", Material.CHERRY_LOG);
        preparedMachines.put("birchlog", Material.BIRCH_LOG);
        preparedMachines.put("crimsonstem", Material.CRIMSON_STEM);
        preparedMachines.put("junglelog", Material.JUNGLE_LOG);
        preparedMachines.put("mangrovelog", Material.MANGROVE_LOG);
        preparedMachines.put("sprucelog", Material.SPRUCE_LOG);
        preparedMachines.put("warpedstem", Material.WARPED_STEM);
        preparedMachines.put("oaklog", Material.OAK_LOG);
        preparedMachines.put("acacialog", Material.ACACIA_LOG);
        preparedMachines.put("darkoaklog", Material.DARK_OAK_LOG);

        // Leaves
        preparedMachines.put("oakleaves", Material.OAK_LEAVES);
        preparedMachines.put("pinkleaves", Material.PINK_WOOL);
        preparedMachines.put("purpleleaves", Material.PURPLE_WOOL);
        preparedMachines.put("redleaves", Material.RED_WOOL);
        preparedMachines.put("wisterialeaves", Material.LIGHT_BLUE_WOOL);
        preparedMachines.put("fallleaves", Material.ORANGE_WOOL);
        preparedMachines.put("autumnleaves", Material.YELLOW_WOOL);
        preparedMachines.put("azalealeaves", Material.AZALEA_LEAVES);
        preparedMachines.put("acacialeaves", Material.ACACIA_LEAVES);
        preparedMachines.put("spruceleaves", Material.SPRUCE_LEAVES);
        preparedMachines.put("sakuraleaves", Material.PINK_TERRACOTTA);
        preparedMachines.put("birchleaves", Material.BIRCH_LEAVES);
        preparedMachines.put("cherryleaves", Material.CHERRY_LEAVES);
        preparedMachines.put("jungleleaves", Material.JUNGLE_LEAVES);
        preparedMachines.put("mangroveleaves", Material.MANGROVE_LEAVES);

        // Stones
        preparedMachines.put("limestone", Material.SANDSTONE);
        preparedMachines.put("marble", Material.QUARTZ_BLOCK);
        preparedMachines.put("basalt", Material.BASALT);
        preparedMachines.put("slate", Material.POLISHED_BLACKSTONE);
        preparedMachines.put("obsidian", Material.OBSIDIAN);
        preparedMachines.put("quartz", Material.QUARTZ_BLOCK);
        preparedMachines.put("granite", Material.GRANITE);

        // Ores
        preparedMachines.put("coal", Material.COAL_BLOCK);
        preparedMachines.put("iron", Material.IRON_BLOCK);
        preparedMachines.put("copper", Material.COPPER_BLOCK);
        preparedMachines.put("platinum", Material.IRON_BLOCK); // No platinum in vanilla, use iron or custom
        preparedMachines.put("tin", Material.IRON_BLOCK);      // Same for tin
        preparedMachines.put("lead", Material.IRON_BLOCK);
        preparedMachines.put("aluminium", Material.IRON_BLOCK);
        preparedMachines.put("silver", Material.IRON_BLOCK);
        preparedMachines.put("gold", Material.GOLD_BLOCK);

        // Gems
        preparedMachines.put("amethyst", Material.AMETHYST_BLOCK);
        preparedMachines.put("garnet", Material.REDSTONE_BLOCK); // Closest vanilla match
        preparedMachines.put("topaz", Material.YELLOW_CONCRETE);
        preparedMachines.put("jade", Material.GREEN_CONCRETE);
        preparedMachines.put("aquamarine", Material.LIGHT_BLUE_CONCRETE);
        preparedMachines.put("sapphire", Material.BLUE_CONCRETE);
        preparedMachines.put("ruby", Material.RED_CONCRETE);
        preparedMachines.put("emerald", Material.EMERALD_BLOCK);
        preparedMachines.put("diamond", Material.DIAMOND_BLOCK);
        preparedMachines.put("netherite", Material.NETHERITE_BLOCK);


        // Powerful mineral
        preparedMachines.put("tungsten", Material.IRON_BLOCK);
        preparedMachines.put("palladium", Material.WAXED_COPPER_BLOCK);
        preparedMachines.put("cobalt", Material.BLUE_GLAZED_TERRACOTTA);
        preparedMachines.put("mithril", Material.OXIDIZED_COPPER);
        preparedMachines.put("orichalcum", Material.PINK_GLAZED_TERRACOTTA);
        preparedMachines.put("titanium", Material.BLACK_GLAZED_TERRACOTTA);
        preparedMachines.put("adamantine", Material.RED_GLAZED_TERRACOTTA);
        preparedMachines.put("dragonite", Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
        preparedMachines.put("voidsteel", Material.OBSIDIAN);
        preparedMachines.put("etherium", Material.CRYING_OBSIDIAN);

        for (Map.Entry<String, Material> entry : preparedMachines.entrySet()) {
            name = entry.getKey();

            // Add underscore before suffixes
            name = name.replaceAll("(log|leaves|mushroom|stem)$", "_$1");

            m = GetMachine(name, entry.getValue(), MachineType.Item);
            SaveItem(name.replaceAll("_", "").trim() + "machine", m.clone());

            String fixedName = name.replaceAll("_", "").trim();

            // Init Ingredient
            ItemStack ingredient = new ItemStack(itemList.get(entry.getKey()));
            ItemMeta ingredientMeta = ingredient.getItemMeta();
            ingredientMeta.setDisplayName(sendText("&fCarbon "+formatItemName(fixedName)));
            List<String> itemLore = new ArrayList<>();
            itemLore.add(sendText("&9Machine Product"));
            itemLore.add(sendText("&9Material"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Can be used to craft &fEquipments"));
            itemLore.add(sendText(" &7at &eCarbon Forge &7(MultiBlock)"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&fBasic"));
            ingredientMeta.setLore(itemLore);
            ingredient.setItemMeta(ingredientMeta);
            SaveItem("carbon"+fixedName, ingredient.clone());



            //itemList.remove(name);
            storedItems.add(m.clone());
            consoleLog(fixedName);
        }

        itemDatabase.put("machine", storedItems);

        SaveItem("steammachine", GetMachine("steam", Material.DECORATED_POT, MachineType.Steam));
    }




    public static void OpenMachineDatabase(Player player, String page){
        Inventory inventory = Bukkit.createInventory(player, 54, "Machine Database");
        for (int i = 0; i < itemDatabase.get(page).size(); i++) {
            inventory.setItem(i, itemDatabase.get(page).get(i));
        }
        player.openInventory(inventory);
    }


    public FactoryItem setType(FactoryItem.Type type) {
        this.type = type;
        return this;
    }

    public FactoryItem setSubType(FactoryItem.SubType subType) {
        this.subType = subType;
        return this;
    }

    public FactoryItem setBoosterType(Booster.BoosterType boosterType) {
        this.boosterType = boosterType;
        return this;
    }

    public FactoryItem setBoosterDuration(int boosterDuration) {
        this.boosterDuration = boosterDuration;
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

    public FactoryItem setBonusStats(String bonusStats) {
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

    public FactoryItem setLevelMinimum(int levelMinimum) {
        this.levelMinimum = levelMinimum;
        return this;
    }

    public FactoryItem canUse(boolean canUse) {
        this.canUse = canUse;
        return this;
    }

    public FactoryItem setColor(Color color) {
        this.color = color;
        return this;
    }

    public FactoryItem setMultiplier(double wandMultiplier) {
        this.wandMultiplier = wandMultiplier;
        return this;
    }

    public FactoryItem setProficiency(double proficiency) {
        this.proficiency = proficiency;
        return this;
    }

    public FactoryItem setMiningFortune(double miningFortune) {
        this.miningFortune = miningFortune;
        return this;
    }
    public FactoryItem setForagingFortune(double foragingFortune) {
        this.foragingFortune = foragingFortune;
        return this;
    }
    public FactoryItem setFarmingFortune(double farmingFortune) {
        this.farmingFortune = farmingFortune;
        return this;
    }
    public FactoryItem setFishingFortune(double fishingFortune) {
        this.fishingFortune = fishingFortune;
        return this;
    }
    public FactoryItem setCombatFortune(double combatFortune) {
        this.combatFortune = combatFortune;
        return this;
    }


    public ItemStack build() {
        return CreateItem(
                type, subType, attackDamage, attackRange, attackSpeed, criticalChance, criticalDamage, steamConsumption,
                health, steam, armor, undeadDamage, undeadDefense, mutantDamage, mutantDefense, meleeDamage, rangeDamage,
                durability, maxDurability, toolPower, toolSpeed, bonusStats, rarity, displayname, material, attackEffect, levelMinimum,
                canUse, color, wandMultiplier, boosterType, boosterDuration, proficiency,
                miningFortune, foragingFortune, farmingFortune, fishingFortune, combatFortune);
    }

    public ItemStack testItem(){

        String addedBonus = "Attack Damage:10,Attack Damage:25,Undead Damage:35";

        return CreateItem(
                Type.Weapon, SubType.Sword, attackDamage, attackRange, attackSpeed, criticalChance, criticalDamage, steamConsumption,
                health, steam, armor, undeadDamage, undeadDefense, mutantDamage, mutantDefense, meleeDamage, rangeDamage,
                durability, maxDurability, toolPower, toolSpeed, addedBonus, rarity, displayname, material, attackEffect,
                levelMinimum, canUse, color, wandMultiplier, boosterType, boosterDuration, proficiency,
                miningFortune, foragingFortune, farmingFortune, fishingFortune, combatFortune);
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

    public static List<String> itemNeedModelList = Arrays.asList("reinforcedcopperhelmet", "reinforcedcopperheadgear",
            "reinforcedredstonehelmet", "reinforcedredstoneheadgear", "reinforcedlapishelmet", "reinforcedlapisheadgear",

            "tungstenhelmet", "tungstenheadgear",
            "palladiumhelmet", "palladiumheadgear",
            "cobalthelmet", "cobaltheadgear",


            "mithrilhelmet", "mithrilheadgear",
            "orichalcumhelmet", "orichalcumheadgear",
            "titaniumhelmet", "titaniumheadgear"

            ,
            "adamantinehelmet", "adamantineheadgear",
            "dragonitehelmet", "dragoniteheadgear"

            ,
            "voidsteelhelmet", "voidsteelheadgear",
            "etheriumhelmet", "etheriumheadgear");

    private ItemStack CreateItem
    (
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
            String bonusStats,
            Rarity.RarityType rarity,
            String displayname,
            Material material,
            AttackEffect attackEffect,
            int levelMinimum,
            boolean canUse,
            Color color,
            double wandMultiplier,
            Booster.BoosterType boosterType,
            int boosterDuration,
            double proficiency,

            double miningFortune,
            double foragingFortune,
            double farmingFortune,
            double fishingFortune,
            double combatFortune
    )

    {
        ItemStack item = new ItemStack(material);
        for (String needModel: itemNeedModelList){
            if (uncolouredText(displayname).replaceAll(" ", "").trim().toLowerCase().equals(needModel)){
                item = new ItemStack(GetItem(needModel+"model"));
                break;
            }
        }
        if (material == Material.LEATHER_HELMET || material == Material.LEATHER_CHESTPLATE
                || material == Material.LEATHER_LEGGINGS || material == Material.LEATHER_BOOTS){
            LeatherArmorMeta leatherMeta = (LeatherArmorMeta) item.getItemMeta();
            leatherMeta.setColor(color);
            item.setItemMeta(leatherMeta);
        }
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(displayname);
        PersistentDataContainer container = meta.getPersistentDataContainer();

        Map<String, Double> combinedStats = new HashMap<>();
        List<String> splittedStats = Arrays.asList(bonusStats.split(","));
        if (!bonusStats.isEmpty()){
            for (String stats : splittedStats) {
                String statsKey = getBonusKey(uncolouredText(stats).toLowerCase().replaceAll(" ", "").trim());
                double value = Double.parseDouble(numberInText(stats));

                combinedStats.put(statsKey, combinedStats.getOrDefault(statsKey, 0.0) + value);
                //container.set(GetNamespacedKey(statsKey), PersistentDataType.DOUBLE, Double.parseDouble(numberInText(stats)));
            }
        }

        for (Map.Entry<String, Double> entry : combinedStats.entrySet()) {
            String key = entry.getKey();
            double value = entry.getValue();
            container.set(GetNamespacedKey(key), PersistentDataType.DOUBLE, value);
            //consoleLog(key+": "+value);
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

                else if (statsKey.equalsIgnoreCase(baseKey+miningFortuneKey)){
                    value = miningFortune;
                }
                else if (statsKey.equalsIgnoreCase(baseKey+foragingFortuneKey)){
                    value = foragingFortune;
                }
                else if (statsKey.equalsIgnoreCase(baseKey+farmingFortuneKey)){
                    value = farmingFortune;
                }
                else if (statsKey.equalsIgnoreCase(baseKey+fishingFortuneKey)){
                    value = fishingFortune;
                }
                else if (statsKey.equalsIgnoreCase(baseKey+combatFortuneKey)){
                    value = combatFortune;
                }

                container.set(GetNamespacedKey(statsKey), PersistentDataType.DOUBLE, value);
            }
        }

        container.set(GetNamespacedKey(revisionCodeKey), PersistentDataType.INTEGER, globalRevision);

        PersistentDataType<Double, Double> doubleType = PersistentDataType.DOUBLE;

        container.set(GetNamespacedKey(itemKey), PersistentDataType.BOOLEAN, true);

        container.set(GetNamespacedKey(attackDamageKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+attackDamageKey), doubleType)
                        +getBonusStats(container, attackDamageKey));

        container.set(GetNamespacedKey(attackRangeKey), PersistentDataType.DOUBLE, attackRange);

        container.set(GetNamespacedKey(attackSpeedKey), PersistentDataType.DOUBLE, attackSpeed);

        container.set(GetNamespacedKey(criticalChanceKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+criticalChanceKey), doubleType)
                +getBonusStats(container, criticalChanceKey));

        container.set(GetNamespacedKey(criticalDamageKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+criticalDamageKey), doubleType)
                +getBonusStats(container, criticalDamageKey));

        container.set(GetNamespacedKey(steamKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+steamKey), doubleType)
                +getBonusStats(container, steamKey));

        container.set(GetNamespacedKey(steamConsumptionKey), PersistentDataType.DOUBLE, steamConsumption);

        container.set(GetNamespacedKey(healthKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+healthKey), doubleType)
                +getBonusStats(container, healthKey));

        container.set(GetNamespacedKey(armorKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+armorKey), doubleType)
                +getBonusStats(container, armorKey));

        container.set(GetNamespacedKey(undeadDamageKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+undeadDamageKey), doubleType)
                +getBonusStats(container, undeadDamageKey));

        container.set(GetNamespacedKey(undeadDefenseKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+undeadDefenseKey), doubleType)
                +getBonusStats(container, undeadDefenseKey));

        container.set(GetNamespacedKey(mutantDamageKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+mutantDamageKey), doubleType)
                +getBonusStats(container, mutantDamageKey));

        container.set(GetNamespacedKey(mutantDefenseKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+mutantDefenseKey), doubleType)
                +getBonusStats(container, mutantDefenseKey));

        container.set(GetNamespacedKey(meleeDamageKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+meleeDamageKey), doubleType)
                +getBonusStats(container, meleeDamageKey));

        container.set(GetNamespacedKey(rangeDamageKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+rangeDamageKey), doubleType)
                +getBonusStats(container, rangeDamageKey));


        // fortune
        container.set(GetNamespacedKey(miningFortuneKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+miningFortuneKey), doubleType)
                        +getBonusStats(container, miningFortuneKey));

        container.set(GetNamespacedKey(foragingFortuneKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+foragingFortuneKey), doubleType)
                        +getBonusStats(container, foragingFortuneKey));

        container.set(GetNamespacedKey(farmingFortuneKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+farmingFortuneKey), doubleType)
                        +getBonusStats(container, farmingFortuneKey));

        container.set(GetNamespacedKey(fishingFortuneKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+fishingFortuneKey), doubleType)
                        +getBonusStats(container, fishingFortuneKey));

        container.set(GetNamespacedKey(combatFortuneKey), PersistentDataType.DOUBLE,
                container.get(GetNamespacedKey(baseKey+combatFortuneKey), doubleType)
                        +getBonusStats(container, combatFortuneKey));



        if (type != Type.Booster){
            if (rarity == Rarity.RarityType.Common){
                maxDurability = 100;
            }
            else if (rarity == Rarity.RarityType.Uncommon){
                maxDurability = 250;
            }
            else if (rarity == Rarity.RarityType.Rare){
                maxDurability = 500;
            }
            else if (rarity == Rarity.RarityType.Epic){
                maxDurability = 800;
            }
            else if (rarity == Rarity.RarityType.Legendary){
                maxDurability = 1000;
            }
            else if (rarity == Rarity.RarityType.Immortal){
                maxDurability = 2500;
            }
            else if (rarity == Rarity.RarityType.Divine){
                maxDurability = 5000;
            }
        }

        container.set(GetNamespacedKey(durabilityKey), PersistentDataType.DOUBLE, durability);
        container.set(GetNamespacedKey(maxDurabilityKey), PersistentDataType.DOUBLE, maxDurability);

        if (durability > maxDurability){
            container.set(GetNamespacedKey(durabilityKey), PersistentDataType.DOUBLE, maxDurability);
            container.set(GetNamespacedKey(maxDurabilityKey), PersistentDataType.DOUBLE, maxDurability);
            durability = maxDurability;
        }


        container.set(GetNamespacedKey(typeKey), PersistentDataType.STRING, type.toString().toLowerCase());
        container.set(GetNamespacedKey(subTypeKey), PersistentDataType.STRING, subType.toString().toLowerCase());
        container.set(GetNamespacedKey(rarityKey), PersistentDataType.STRING, rarity.toString().toLowerCase());

        container.set(GetNamespacedKey(toolPowerKey), PersistentDataType.DOUBLE, toolPower);
        container.set(GetNamespacedKey(toolSpeedKey), PersistentDataType.DOUBLE, toolSpeed);

        container.set(GetNamespacedKey(serialCodeKey), PersistentDataType.STRING, GenerateSerialCode());

        container.set(GetNamespacedKey(attackEffectKey), PersistentDataType.STRING, attackEffect.toString().toLowerCase());

        container.set(GetNamespacedKey(canUseKey), PersistentDataType.BOOLEAN, canUse);


        container.set(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER, levelMinimum);

        container.set(GetNamespacedKey(boosterTypeKey), PersistentDataType.STRING, boosterType.toString().toLowerCase());
        container.set(GetNamespacedKey(boosterDurationKey), PersistentDataType.INTEGER, boosterDuration);

        container.set(GetNamespacedKey(multiplierKey), PersistentDataType.DOUBLE, wandMultiplier);

        if (proficiency > 1){
            proficiency = levelMinimum*0.25;
        }
        container.set(GetNamespacedKey(proficiencyKey), PersistentDataType.DOUBLE, proficiency);

        if (!bonusStats.isEmpty()){
            container.set(GetNamespacedKey(bonusStatsKey), PersistentDataType.STRING, bonusStats);
        }

        List<String> itemLore = new ArrayList<>();
        meta.setDisplayName(sendText(Rarity.getColor(rarity)+displayname));

        itemLore.add(sendText("&9" + type));
        if (subType != SubType.Fishing_Rod){
            itemLore.add(sendText(sendRgbText(formatItemName(subType.toString()), "#4A5357")));
        }else{
            itemLore.add(sendText(sendRgbText("Fishing Rod", "#4A5357")));
        }
        itemLore.add(sendText(" "));
        if (type.equals(Type.Weapon)){
            itemLore.add(sendText(" &c\uD83D\uDDE1 &7Attack Damage: &f" + (int) attackDamage));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &8♦ &7Attack Speed: &f" + FormatDouble(attackSpeed)+"&8&l\uD835\uDDCC"));
            itemLore.add(sendText(" &8♦ &7Attack Range: &f" +(int)  attackRange+"&8&l\uD835\uDDBB"));
            itemLore.add(sendText(" &8♦ &7Critical Chance: &f" + (int)  criticalChance+"%"));
            if (steamConsumption > 0){
                itemLore.add(sendText(" "));
                itemLore.add(sendText(" &e\uD83C\uDF0A &7Steam Consumption: &f" + FormatDouble(steamConsumption)));
            }
        }
        else if (type.equals(Type.Equipment)){
            if (health > 0){
                itemLore.add(sendText(" &4❤ &7Health: &c➕" + FormatDouble(health)));
            }
            if (steam > 0){
                itemLore.add(sendText(" &6\uD83C\uDF0A &7Steam: &e➕" +FormatDouble(steam)));
            }
            if (armor > 0){
                itemLore.add(sendText(" &8\uD83D\uDD30 &7Armor: &f➕"+FormatDouble(armor)));
            }
        }
        else if (type.equals(Type.Tool)){
            String toolLogo = "";
            String jobName = "";
            if (subType.equals(SubType.Pickaxe)){
                toolLogo = "⛏";
                jobName = "Mining";
            } else if (subType.equals(SubType.Axe)) {
                toolLogo = "🪓";
                jobName = "Foraging";
            } else if (subType.equals(SubType.Shovel)) {
                toolLogo = "🔨";
                jobName = "Mining";
            } else if (subType.equals(SubType.Hoe)) {
                toolLogo = "\uD83C\uDF3F";
                jobName = "Farming";
            } else if (subType.equals(SubType.Fishing_Rod)) {
                toolLogo = "🎣";
                jobName = "Fishing";
            }
            else if (subType.equals(SubType.Hook)) {
                toolLogo = "\uD83E\uDE9D";
                jobName = "";
            }
            String toolName = subType.toString();
            if (toolName.equals("Fishing_Rod")){
                toolName = "Fishing Rod";
            }
            itemLore.add(sendText(" "+sendRgbText(toolLogo, "#756A40")+" &7"+toolName+" Power: &f" + (int) toolPower));
            if (toolSpeed > 0){
                itemLore.add(sendText(" "+sendRgbText("⚡", "#F2C206")+" &7"+toolName+" Speed: &f" + (int) toolSpeed));
            }

            if (miningFortune > 0){
                itemLore.add(sendText(" "));
                itemLore.add(sendText(" "+sendRgbText("\uD83C\uDF40", "#00FF00")+" &aMining Fortune: +" + FormatDouble(miningFortune)));
                itemLore.add(sendText("  &7Increase Mineral drop amounts"));
                itemLore.add(sendText("  &7(Chance: &e"+(5+miningFortune)+"%&7)"));
                if (foragingFortune > 0){
                    itemLore.add(sendText(" "));
                }
            }
            if (foragingFortune > 0){
                if (miningFortune == 0){
                    itemLore.add(sendText(" "));
                }
                itemLore.add(sendText(" "+sendRgbText("\uD83C\uDF40", "#00FF00")+" &aForaging Fortune: +" + FormatDouble(foragingFortune)));
                itemLore.add(sendText("  &7Increase Wood drop amounts"));
                itemLore.add(sendText("  &7(Chance: &e"+(5+foragingFortune)+"%&7)"));
                if (farmingFortune > 0){
                    itemLore.add(sendText(" "));
                }
            }
            if (farmingFortune > 0){
                if (miningFortune == 0 && foragingFortune == 0){
                    itemLore.add(sendText(" "));
                }
                itemLore.add(sendText(" "+sendRgbText("\uD83C\uDF40", "#00FF00")+" &aFarming Fortune: +" + FormatDouble(farmingFortune)));
                itemLore.add(sendText("  &7Increase Crops drop chance at Farms"));
                itemLore.add(sendText("  &7(Chance: &e"+(cropDropChance+farmingFortune)+"%&7)"));
                /*itemLore.add(sendText("  &7when drop rates reaches &f100% &7fortune"));
                itemLore.add(sendText("  &7will converted into drop amounts"));*/
                if (fishingFortune > 0){
                    itemLore.add(sendText(" "));
                }
            }
            if (fishingFortune > 0){
                if (miningFortune == 0 && foragingFortune == 0 && farmingFortune == 0){
                    itemLore.add(sendText(" "));
                }
                itemLore.add(sendText(" "+sendRgbText("\uD83C\uDF40", "#00FF00")+" &aFishing Fortune: +" + FormatDouble(fishingFortune)));
                itemLore.add(sendText("  &7Increase Fish amount per catch"));
                itemLore.add(sendText("  &7(Chance: &e"+(10+fishingFortune)+"%&7)"));
                if (combatFortune > 0){
                    itemLore.add(sendText(" "));
                }
            }
            if (combatFortune > 0){
                if (miningFortune == 0 && foragingFortune == 0 && farmingFortune == 0 && fishingFortune == 0){
                    itemLore.add(sendText(" "));
                }
                itemLore.add(sendText(" "+sendRgbText("\uD83C\uDF40", "#00FF00")+" &aCombat Fortune: &f" + FormatDouble(combatFortune)));
                itemLore.add(sendText("  &7Increase Mob Loot drop rates"));
            }

            if (proficiency > 0){
                itemLore.add(sendText(" "));
                itemLore.add(sendText(" &9+"+FormatDouble(proficiency)+" &f"+jobName+" Proficiency"));
            }
            if (steamConsumption > 0){
                if (subType.equals(SubType.Hook)){
                    itemLore.add(sendText(" "));
                    itemLore.add(sendText(" &e\uD83C\uDF0A &7Steam Consumption: &f" + FormatDouble(steamConsumption)));
                }
            }
        }
        else if (type == Type.Booster){
            if (subType == SubType.Sell_Wand){
                itemLore.add(sendText(" &8\uD83D\uDFCC &7Multiplier: &9x"+FormatDouble(wandMultiplier)));
            }

            else if (subType == SubType.Potion){
                itemLore.add(sendText(" &8\uD83D\uDFCC &7Booster: &9+"+numberInText(boosterType.toString())+"% &f"+uncolouredText(
                        boosterType.toString().replaceAll("_", " ").trim())
                        .replaceAll("Percent", "").trim()
                ));
                itemLore.add(sendText(" &e⌛ &7Duration: &6"+getFormattedTime(boosterDuration)));
                itemLore.add(sendText(" "));
                itemLore.add(sendText("&8"+usageArrowSymbol+" &7Right-Click to consume"));
            }
        }

        if (type == Type.Tool || type == Type.Weapon || type == Type.Equipment){
            if (rarity == Rarity.RarityType.Common){
                itemLore.add(sendText(" "));
                itemLore.add(sendText(" &8(Empty Enchantment Slot)"));
            }
            else if (rarity == Rarity.RarityType.Uncommon){
                itemLore.add(sendText(" "));
                itemLore.add(sendText(" &8(Empty Enchantment Slot)"));
                itemLore.add(sendText(" &8(Empty Enchantment Slot)"));
            }
            else if (rarity == Rarity.RarityType.Rare){
                itemLore.add(sendText(" "));
                itemLore.add(sendText(" &8(Empty Enchantment Slot)"));
                itemLore.add(sendText(" &8(Empty Enchantment Slot)"));
                itemLore.add(sendText(" &8(Empty Enchantment Slot)"));
            }
            else if (rarity == Rarity.RarityType.Epic || rarity == Rarity.RarityType.Legendary
                    || rarity == Rarity.RarityType.Immortal || rarity == Rarity.RarityType.Divine){
                itemLore.add(sendText(" "));
                itemLore.add(sendText(" &8(Empty Enchantment Slot)"));
                itemLore.add(sendText(" &8(Empty Enchantment Slot)"));
                itemLore.add(sendText(" &8(Empty Enchantment Slot)"));
                itemLore.add(sendText(" &8(Empty Enchantment Slot)"));
            }
        }



        if (!type.equals(Type.Booster)){
            if (!bonusStats.isEmpty()){
                itemLore.add(sendText(" "));
                for (String stats : splittedStats){
                    itemLore.add(sendText(" &a+"+numberInText(stats)+" &7"+uncolouredText(stats)));
                }
            }
            itemLore.add(sendText(" "));
            if (canUse){
                itemLore.add(sendText(" &8♦ &7Level Minimum: &a"+levelMinimum+" &2✔"));
            }else{
                itemLore.add(sendText(" &8♦ &7Level Minimum: &c"+levelMinimum+" &4✘"));
            }
        }

        if (subType != SubType.Potion){
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &8♜ &7Durability: &f" + (int) durability + "&8/&f" + (int) maxDurability+GetDurabilityPercent(durability, maxDurability)));
            itemLore.add(sendText(" "));
            if (subType == SubType.Sell_Wand){
                itemLore.add(sendText(" &8"+usageArrowSymbol+" &7Right-Click at &f&nChest&7 to use"));
                itemLore.add(sendText(" "));
            }

            if (subType == SubType.Pickaxe || subType == SubType.Axe){
                int breakingTier = 1;
                if (material.toString().contains("STONE")){
                    breakingTier = 2;
                }
                else if (material.toString().contains("IRON")){
                    breakingTier = 3;
                }
                else if (material.toString().contains("GOLDEN")){
                    breakingTier = 4;
                }
                else if (material.toString().contains("DIAMOND")){
                    breakingTier = 5;
                }
                else if (material.toString().contains("NETHERITE")){
                    breakingTier = 6;
                }
                itemLore.add(sendText("&8Tier "+intToRoman(breakingTier)+" Breaking Power"));
            }

            itemLore.add(sendText(Rarity.setRarity(rarity)));
        }

        meta.setLore(itemLore);

        //--
        NamespacedKey damageKey = new NamespacedKey(Factory.getPlugin(Factory.class), "attack_damage");
        AttributeModifier damageModifier = new AttributeModifier(damageKey, -100, AttributeModifier.Operation.ADD_NUMBER);

        NamespacedKey speedKey = new NamespacedKey(Factory.getPlugin(Factory.class), "attack_speed");
        AttributeModifier speedModifier = new AttributeModifier(speedKey, 0, AttributeModifier.Operation.ADD_NUMBER);

        meta.setUnbreakable(true);
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, damageModifier);
        meta.addAttributeModifier(Attribute.ATTACK_SPEED, speedModifier);
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
        Tool,
        Booster;

        public static Type parseType(String type) {
            return switch (type.toLowerCase()) {
                case "weapon" -> Type.Weapon;
                case "equipment" -> Type.Equipment;
                case "accessories" -> Type.Accessories;
                case "tool" -> Type.Tool;
                case "booster" -> Type.Booster;
                default -> null;
            };
        }
    }

    public enum AttackEffect {
        None,
        Slash,
        Steam,
        Acid,
        Bullet,
        Arrow,
        Flamethrower,
        Energy,
        Smash;

        public static AttackEffect parseEffect(String type) {
            return switch (type.toLowerCase()) {
                case "slash" -> AttackEffect.Slash;
                case "steam" -> AttackEffect.Steam;
                case "acid" -> AttackEffect.Acid;
                case "bullet" -> AttackEffect.Bullet;
                case "arrow" -> AttackEffect.Arrow;
                case "flamethrower" -> AttackEffect.Flamethrower;
                case "energy" -> AttackEffect.Energy;
                case "smash" -> AttackEffect.Smash;
                default -> None;
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
        Headgear(Type.Equipment),
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
        Fishing_Rod(Type.Tool),
        Hook(Type.Tool),

        // Booster
        Sell_Wand(Type.Booster),
        Potion(Type.Booster);

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
                case "headgear" -> SubType.Headgear;
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
                case "fishing_rod" -> SubType.Fishing_Rod;
                case "sell_wand" -> SubType.Sell_Wand;
                case "potion" -> SubType.Potion;
                case "hook" -> SubType.Hook;
                default -> null;
            };
        }
    }

    public static boolean isPickaxe(ItemStack item) {

        if (item != null) {
            if (item.getType() == Material.AIR){
                return false;
            }
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
            if (item.getType() == Material.AIR){
                return false;
            }
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
            if (item.getType() == Material.AIR){
                return false;
            }
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
            if (item.getType() == Material.AIR){
                return false;
            }
            Material type = item.getType();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey(itemKey))){
                if (!container.has(GetNamespacedKey(subTypeKey), PersistentDataType.STRING)){
                    return false;
                }
                if (!container.get(GetNamespacedKey(subTypeKey), PersistentDataType.STRING).equals(SubType.Fishing_Rod.toString().toLowerCase())){
                    return false;
                }
            }
            return
                    type == Material.FISHING_ROD;
        }
        return false;
    }

    public static boolean isHook(ItemStack item) {
        if (item != null) {
            if (item.getType() == Material.AIR){
                return false;
            }
            Material type = item.getType();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey(itemKey))){
                if (!container.has(GetNamespacedKey(subTypeKey), PersistentDataType.STRING)){
                    return false;
                }
                return container.get(GetNamespacedKey(subTypeKey), PersistentDataType.STRING).equals(SubType.Hook.toString().toLowerCase());
            }
        }
        return false;
    }

    public static boolean isSellWand(ItemStack item) {
        if (item != null) {

            if (item.getType() == Material.AIR){
                return false;
            }

            Material type = item.getType();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey(itemKey))){
                if (!container.has(GetNamespacedKey(subTypeKey), PersistentDataType.STRING)){
                    return false;
                }
                if (container.get(GetNamespacedKey(subTypeKey), PersistentDataType.STRING).equals(SubType.Sell_Wand.toString().toLowerCase())){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isBackpack(ItemStack item) {
        if (item != null) {

            if (item.getType() == Material.AIR){
                return false;
            }

            Material type = item.getType();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey("backpackSize"))){
                return true;
            }
        }
        return false;
    }

    public static boolean isPotion(ItemStack item) {
        if (item != null) {

            if (item.getType() == Material.AIR){
                return false;
            }

            Material type = item.getType();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey(itemKey))){
                if (!container.has(GetNamespacedKey(subTypeKey), PersistentDataType.STRING)){
                    return false;
                }
                if (container.get(GetNamespacedKey(subTypeKey), PersistentDataType.STRING).equals(SubType.Potion.toString().toLowerCase())){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isJobTool(ItemStack item) {
        if (item != null) {

            if (item.getType() == Material.AIR){
                return false;
            }

            Material type = item.getType();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey(itemKey))){
                if (!container.has(GetNamespacedKey(typeKey), PersistentDataType.STRING)){
                    return false;
                }
                if (container.get(GetNamespacedKey(typeKey), PersistentDataType.STRING).equals(Type.Tool.toString().toLowerCase())){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isShovel(ItemStack item) {
        if (item != null) {
            if (item.getType() == Material.AIR){
                return false;
            }
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

            if (item.getType() == Material.AIR){
                return false;
            }

            Material type = item.getType();
            return
                    type == Material.WOODEN_HOE || type == Material.STONE_HOE ||
                            type == Material.IRON_HOE || type == Material.GOLDEN_HOE ||
                            type == Material.DIAMOND_HOE || type == Material.NETHERITE_HOE;
        }
        return false;
    }

    public static boolean isHazmat(ItemStack item) {
        if (item != null) {

            if (item.getType() == Material.AIR) {
                return false;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()){
                return uncolouredText(meta.getDisplayName()).toLowerCase().contains("hazmat");
            }
        }
        return false;
    }

    public static boolean isArmor(ItemStack item) {
        if (item != null) {

            if (item.getType() == Material.AIR){
                return false;
            }

            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey(itemKey))){
                if (!container.has(GetNamespacedKey(typeKey), PersistentDataType.STRING)){
                    return false;
                }
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
                if (!container.has(GetNamespacedKey(subTypeKey), PersistentDataType.STRING)){
                    return false;
                }
                if (!container.get(GetNamespacedKey(subTypeKey), PersistentDataType.STRING).equals(SubType.Helmet.toString().toLowerCase())
                && !container.get(GetNamespacedKey(subTypeKey), PersistentDataType.STRING).equals(SubType.Headgear.toString().toLowerCase())){
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
                if (!container.has(GetNamespacedKey(subTypeKey), PersistentDataType.STRING)){
                    return false;
                }
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
                if (!container.has(GetNamespacedKey(subTypeKey), PersistentDataType.STRING)){
                    return false;
                }
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
                if (!container.has(GetNamespacedKey(subTypeKey), PersistentDataType.STRING)){
                    return false;
                }
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
        if (item.getType() == Material.AIR){
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        //List<String> lore = meta.getLore();

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
                //|| type == Material.STICK || type == Material.BLAZE_ROD || type == Material.SHEARS
                || type == Material.LEATHER_HORSE_ARMOR || type == Material.IRON_HORSE_ARMOR || type == Material.GOLDEN_HORSE_ARMOR
                || type == Material.DIAMOND_HORSE_ARMOR  || type == Material.MACE;
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

    public static ItemMeta SetAditMeta(ItemMeta meta) {
        if (meta == null) return null;

        Factory plugin = Factory.getPlugin(Factory.class);

        boolean hasDamageModifier = false;
        boolean hasSpeedModifier = false;

        if (meta.hasAttributeModifiers()) {
            Multimap<Attribute, AttributeModifier> modifiers = meta.getAttributeModifiers();

            if (modifiers != null) { // Ensure it's not null
                for (Attribute attribute : modifiers.keySet()) {
                    if (attribute == Attribute.ATTACK_DAMAGE) {
                        hasDamageModifier = true;
                    }
                    if (attribute == Attribute.ATTACK_SPEED) {
                        hasSpeedModifier = true;
                    }
                }
            }
        }

        // Add attack damage modifier if missing
        if (!hasDamageModifier) {
            AttributeModifier damageModifier = new AttributeModifier(
                    new NamespacedKey(plugin, "attack_damage"), -100, AttributeModifier.Operation.ADD_NUMBER);
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, damageModifier);
        }

        // Add attack speed modifier if missing
        if (!hasSpeedModifier) {
            AttributeModifier speedModifier = new AttributeModifier(
                    new NamespacedKey(plugin, "attack_speed"), 0, AttributeModifier.Operation.ADD_NUMBER);
            meta.addAttributeModifier(Attribute.ATTACK_SPEED, speedModifier);
        }

        // Hide unnecessary item flags
        meta.addItemFlags(
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ARMOR_TRIM,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_STORED_ENCHANTS,
                ItemFlag.HIDE_ADDITIONAL_TOOLTIP,
                ItemFlag.HIDE_DYE
        );

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
        if (!meta.hasDisplayName()){
            meta.setDisplayName(sendText("&f"+formatItemName(item.getType().toString())));
            if (uncolouredText(meta.getDisplayName()).contains("Potion")){
                PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
                if (potionMeta.getBasePotionType().name().contains("WATER")){
                    meta.setDisplayName(sendText("&f"+formatItemName(potionMeta.getBasePotionType().toString())+" Bottle"));
                }else{
                    meta.setDisplayName(sendText("&f"+formatItemName(potionMeta.getBasePotionType().toString())+" Potion"));
                }
            }
        }
        List<String> itemLore = new ArrayList<>();
        if (meta.hasLore()){
            for (String lore : meta.getLore()){
                itemLore.add(sendText(lore));
            }
        }
        if (!meta.hasLore()){

            /*if (item.getType().isFuel()){
                itemLore.add(sendText("&9Fuel"));
            }*/
            if (isMaterial(item.getType())){
                itemLore.add(sendText("&9Material"));
            }
            if (item.getType().isBlock()){
                itemLore.add(sendText("&9Placeable"));
            }
            if (item.getType().isEdible()){
                itemLore.add(sendText("&9Consumable"));
            }
            /*if (item.getType().isBurnable()){
                itemLore.add(sendText("&9Burnable"));
            }
            if (item.getType().isFlammable()){
                itemLore.add(sendText("&9Flammable"));
            }
            if (item.getType().isCompostable()){
                itemLore.add(sendText("&9Compostable"));
            }*/
        }
        itemLore.add(sendText(" "));

        PersistentDataContainer container = meta.getPersistentDataContainer();
        Double worth = container.get(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE);
        if (worth == null){
            worth = GetWorth(item.getType().toString().toLowerCase().replaceAll("_", "").trim());
            if (worth != null){
                container.set(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE, worth);
                itemLore.add(sendText(" &7Worth: &f"+FormatDouble(worth)+icon));
                itemLore.add(sendText(" &8✧ &7Sell this item at &e/sellitem"));
                itemLore.add(sendText(" &8✧ &e/sellall &7to sell all from your inventory"));
                itemLore.add(sendText(" &8✧ &7or put in a chest for &f&nSell Wand&7 Multiplier"));
                itemLore.add(sendText(" "));
            }
        }

        if (!meta.hasLore()){
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
        }
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isMaterial(Material material) {
        // Check if the material is an output of any recipe
       /* if (!Bukkit.getServer().getRecipesFor(new ItemStack(material)).isEmpty()) {
            return true;
        }*/

        // Iterate through all registered recipes
        for (Iterator<Recipe> it = Bukkit.recipeIterator(); it.hasNext(); ) {
            Recipe recipe = it.next();

            if (recipe instanceof ShapedRecipe shapedRecipe) {
                // Check if the material is in the ingredient map
                for (ItemStack ingredient : shapedRecipe.getIngredientMap().values()) {
                    if (ingredient != null && ingredient.getType() == material) {
                        return true;
                    }
                }
            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                // Check if the material is in the ingredient list
                for (ItemStack ingredient : shapelessRecipe.getIngredientList()) {
                    if (ingredient.getType() == material) {
                        return true;
                    }
                }
            } else if (recipe instanceof FurnaceRecipe furnaceRecipe) {
                // Check if the material is used as furnace input
                if (furnaceRecipe.getInput().getType() == material) {
                    return true;
                }
            } else if (recipe instanceof CookingRecipe<?> cookingRecipe) {
                // Check Blast Furnace / Smoker recipes
                if (cookingRecipe.getInput().getType() == material) {
                    return true;
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


    public static ItemStack CreateBackpack(int size){
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(GetNamespacedKey(itemKey), PersistentDataType.BOOLEAN, true);
        container.set(GetNamespacedKey(backpackSizeKey), PersistentDataType.INTEGER, size);

        meta.setDisplayName(sendText("&fBackpack"));

        List<String> itemLore =

                Arrays.asList(

                        sendText("&9Tool"),
                        sendText(" "),
                        sendText(" &e❐ &7Size: &6"+size),
                        sendText(" "),
                        sendText("&8➤ &7Right-Click to store items")

                );

        container.set(GetNamespacedKey(serialCodeKey), PersistentDataType.STRING, GenerateSerialCode());

        meta.setLore(itemLore);

        item.setItemMeta(meta);
        return item;
    }


    public static ItemStack GetPotion(PotionType type){

        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();

        meta = SetAditMeta(meta);
        item.setItemMeta(meta);

        PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
        potionMeta.setBasePotionType(type);

        item.setItemMeta(potionMeta);

        return item;

    }

    public static ItemStack CreateSpawner(EntityType type){

        ItemStack item = new ItemStack(Material.SPAWNER);
        BlockStateMeta blockMeta = (BlockStateMeta) item.getItemMeta();
        CreatureSpawner spawner = (CreatureSpawner) blockMeta.getBlockState();
        spawner.setSpawnedType(type);
        blockMeta.setBlockState(spawner);

        item.setItemMeta(blockMeta);

        ItemMeta meta = item.getItemMeta();
        meta = SetAditMeta(meta);

        meta.setDisplayName(sendText("&f"+formatItemName(type.toString().toLowerCase())+" Spawner"));

        List<String> itemLore = new ArrayList<>();

        itemLore.add(sendText("&9Spawner"));
        itemLore.add(sendText("&9Placeable"));
        //itemLore.add(sendText("&8&o"+formatItemName(type.toString().toLowerCase())));

        meta.setLore(itemLore);

        item.setItemMeta(meta);

        return item;
    }


    public static ItemStack GetSellWand(double mult, Rarity.RarityType rarity){

        FactoryItem wand = new FactoryItem();

        wand.setMaterial(Material.BLAZE_ROD);

        if (rarity == Rarity.RarityType.Common){
            wand.setMaterial(Material.STICK);
            wand.setMaxDurability(25);
        }
        else if (rarity == Rarity.RarityType.Uncommon){
            wand.setMaterial(Material.STICK);
            wand.setMaxDurability(50);
        }
        else if (rarity == Rarity.RarityType.Rare){
            wand.setMaterial(Material.BLAZE_ROD);
            wand.setMaxDurability(100);
        }
        else if (rarity == Rarity.RarityType.Epic){
            wand.setMaterial(Material.BLAZE_ROD);
            wand.setMaxDurability(150);
        }
        else if (rarity == Rarity.RarityType.Legendary){
            wand.setMaterial(Material.BREEZE_ROD);
            wand.setMaxDurability(250);
        }
        else if (rarity == Rarity.RarityType.Immortal){
            wand.setMaterial(Material.BREEZE_ROD);
            wand.setMaxDurability(500);
        }


        wand.setType(Type.Booster);
        wand.setSubType(SubType.Sell_Wand);
        wand.setRarity(rarity);

        wand.setDisplayname(sendText("Sell Wand"));

        wand.setMultiplier(mult);

        return wand.build().clone();

    }

    public static ItemStack GetBooster(double mult, String potionName, Booster.BoosterType boosterType, PotionType potionType){

        ItemStack potion = new ItemStack(Material.POTION);


        FactoryItem wand = new FactoryItem();

        wand.setType(Type.Booster);
        wand.setSubType(SubType.Potion);
        wand.setBoosterType(boosterType);
        wand.setBoosterDuration(1800);
        wand.setDisplayname(sendText("&f"+potionName+" Potion"));
        wand.setMultiplier(mult);
        potion.setItemMeta(wand.build().clone().getItemMeta());

        PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
        potionMeta.setBasePotionType(potionType);
        //potionMeta.addEnchant(Enchantment.UNBREAKING, 10, true);
        potionMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        potionMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        /*potionMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        potionMeta.addItemFlags(ItemFlag.HIDE_STORED_ENCHANTS);*/
        potion.setItemMeta(potionMeta);
        return potion;

    }

    public static ItemStack GenerateItem(Type type, SubType subType, Material material, int level, Rarity.RarityType rarity){
        Random random = new Random();

        FactoryItem item = new FactoryItem();

        int bonusAmount = 1;

        int meleeAttackRange = 1;
        double meleeAttackSpeed = 1;
        int criticalChance = 10;
        int projectileAttackRange = 1;
        int blastAttackRange = 1;

        switch (rarity) {
            case Rarity.RarityType.Common:
                bonusAmount = 1;
                meleeAttackRange = random.nextInt(5) + 4;
                meleeAttackSpeed = 1.0 + (0.5 * random.nextDouble()); // 1.0 - 1.5

                criticalChance = random.nextInt(15)+10;

                projectileAttackRange = random.nextInt(10) + 8;
                blastAttackRange = random.nextInt(8) + 7;
                break;
            case Rarity.RarityType.Uncommon:
                bonusAmount = 2;
                meleeAttackRange = random.nextInt(5) + 4;
                meleeAttackSpeed = 0.9 + (0.4 * random.nextDouble()); // 0.9 - 1.3

                criticalChance = random.nextInt(18)+12;

                projectileAttackRange = random.nextInt(12) + 10;
                blastAttackRange = random.nextInt(9) + 8;
                break;
            case Rarity.RarityType.Rare:
                bonusAmount = 3;
                meleeAttackRange = random.nextInt(6) + 5;
                meleeAttackSpeed = 0.8 + (0.4 * random.nextDouble()); // 0.8 - 1.2

                criticalChance = random.nextInt(25)+15;

                projectileAttackRange = random.nextInt(14) + 12;
                blastAttackRange = random.nextInt(10) + 9;
                break;
            case Rarity.RarityType.Epic:
                bonusAmount = 4;
                meleeAttackRange = random.nextInt(6) + 5;
                meleeAttackSpeed = 0.7 + (0.3 * random.nextDouble()); // 0.7 - 1.0

                criticalChance = random.nextInt(30)+25;

                projectileAttackRange = random.nextInt(16) + 14;
                blastAttackRange = random.nextInt(10) + 9;
                break;
            case Rarity.RarityType.Legendary:
                bonusAmount = 5;
                meleeAttackRange = random.nextInt(6) + 5;
                meleeAttackSpeed = 0.6 + (0.2 * random.nextDouble()); // 0.6 - 0.8

                criticalChance = random.nextInt(38)+25;

                projectileAttackRange = random.nextInt(17) + 15;
                blastAttackRange = random.nextInt(14) + 12;
                break;
            case Rarity.RarityType.Immortal:
                bonusAmount = 6;
                meleeAttackRange = random.nextInt(8) + 7;
                meleeAttackSpeed = 0.5 + (0.2 * random.nextDouble()); // 0.5 - 0.7

                criticalChance = random.nextInt(45)+40;

                projectileAttackRange = random.nextInt(20) + 18;
                blastAttackRange = random.nextInt(15) + 12;
                break;
        }


        List<String> nameList = new ArrayList<>();

        if (subType == SubType.Sword){
            nameList = Arrays.asList(
                    "Gearfang Sabre",
                    "Cogblade",
                    "Steamcutter",
                    "Aetheredge",
                    "The Clockwork Fang",
                    "Rustlash Rapier",
                    "Piston Saber",
                    "Mechblade of Brass",
                    "Voltsteel Sabre",
                    "Chrono-Katana"
            );
        }
        else if (subType == SubType.Hammer) {
            nameList = Arrays.asList(
                    "Steamcore Maul",
                    "Brasspounder",
                    "Boilforge Hammer",
                    "The Mechasmash",
                    "Geargrinder",
                    "Pistoncrush Maul",
                    "The Crankhammer",
                    "Thunder Cog",
                    "Steam Titan's Mallet",
                    "Iron Howl Maul"
            );
        }

        else if (subType == SubType.Bow) {
            nameList = Arrays.asList(
                    "Brassstring Repeater",
                    "Steampiercer",
                    "The Gilded Draw",
                    "Cogwheel Longbow",
                    "Voltstring Arc",
                    "Mechanarch Bow",
                    "The Gyrobow",
                    "Ironwhistle Bow",
                    "Pneuma Boltcaster",
                    "Windgear Bow"
            );
        }

        else if (subType == SubType.Gun){
            nameList = Arrays.asList(
                    "Clockwork Reaver",
                    "Rustshot Revolver",
                    "Cogfire Pistol",
                    "Gearflare Carbine",
                    "Steambolt Rifle",
                    "The Valvegun",
                    "Brassbane Blaster",
                    "Aethercoil Gun",
                    "The Pistonic Repeater",
                    "Voltic Howler"
            );
        }

        else if (subType == SubType.Blast){
            nameList = Arrays.asList(
                    "Tesla Howler",
                    "Boilburst Cannon",
                    "Steam Lance",
                    "The Copper Roar",
                    "Voltflare Blaster",
                    "Shockcoil Emitter",
                    "Aether Pulsegun",
                    "Thunderburst Device",
                    "The Boiler Blaster",
                    "Gearfire Ejector"
            );
        }

        else if (subType == SubType.Helmet) {
            nameList = Arrays.asList(
                    "Cogwheel Visor",
                    "Steamforged Helm",
                    "Goggleguard",
                    "Voltsteel Helmet",
                    "Boilplate Crown",
                    "Aether Lenscap",
                    "Brass Dome",
                    "Ironfuse Helm",
                    "Chrono Visage",
                    "Tesla Guard"
            );
        }

        else if (subType == SubType.Chestplate) {
            nameList = Arrays.asList(
                    "Gearplate Harness",
                    "Steamcore Chest",
                    "Boilsteel Shell",
                    "Voltguard Plate",
                    "Brassheart Armor",
                    "Clockwork Cuirass",
                    "Ironcoil Breastplate",
                    "Aether Coreplate",
                    "Crankmail Vest",
                    "Tesla Torso"
            );
        }

        else if (subType == SubType.Leggings) {
            nameList = Arrays.asList(
                    "Piston Greaves",
                    "Steamline Legplates",
                    "Ironpipe Leggings",
                    "Voltframe Trousers",
                    "Boilforge Pants",
                    "Brasslink Legguards",
                    "Gearbind Greaves",
                    "Cogsteel Plating",
                    "Crankshaft Leggings",
                    "Mechstride Plates"
            );
        }

        else if (subType == SubType.Boots) {
            nameList = Arrays.asList(
                    "Steamstep Boots",
                    "Ironmarch Walkers",
                    "Voltgrip Treads",
                    "Brassheel Boots",
                    "Piston Stompers",
                    "Gearwalker Greaves",
                    "Shockcoil Boots",
                    "Boilplate Boots",
                    "Crankstep Striders",
                    "Aetherbound Soles"
            );
        }


        item.setType(type);
        item.setSubType(subType);
        int randomName = random.nextInt(nameList.size());
        String displayname = nameList.get(randomName);


        List<String> availableBonus = Arrays.asList(
                "Steam", "Movement Speed", "Attack Damage",
                "Critical Damage", "Accuracy", "Armor",
                "Undead Damage", "Undead Defense", "Mutant Damage", "Mutant Defense",
                "Melee Damage", "Range Damage"
        );

        List<String> bonusStrings = new ArrayList<>();

        for (int i = 0; i < bonusAmount; i++) {
            int randomAttributeBonus = random.nextInt(availableBonus.size());
            int randomAttributeValue = random.nextInt(level*2)+1;

            String attribute = availableBonus.get(randomAttributeBonus);
            bonusStrings.add(attribute + ":" + randomAttributeValue);
        }

        String combinedBonus = String.join(",", bonusStrings);
        //consoleLog("Combined Bonus: "+combinedBonus);

        item.setDisplayname(displayname);
        item.setMaterial(material);
        item.setLevelMinimum(level);
        item.setRarity(rarity);
        item.setBonusStats(combinedBonus);

        int randomAttackDamage = random.nextInt(level*2)+level;
        int randomHealth = random.nextInt(level*2)+level;
        int randomArmor = random.nextInt(level*2)+level;
        double randomSteam = 0.2 + ((level*0.5) * random.nextDouble());
        double randomAttackSpeed = 0.2 + (1 * random.nextDouble());

        if (isWeapon(item.build())){
            item.setAttackDamage(randomAttackDamage);
            item.setAttackSpeed(randomAttackSpeed);
            item.setCriticalChance(criticalChance);

            item.setAttackSpeed(meleeAttackSpeed);
            item.setAttackRange(meleeAttackRange);

            item.setHealth(0);
            item.setArmor(0);
            item.setSteam(0);
            item.setSteamConsumption(0);
            if (isBow(item.build()) || isGun(item.build()) || isBlast(item.build())){
                item.setSteamConsumption(level*0.08);

                if (isBow(item.build())){
                    item.setAttackEffect(AttackEffect.Arrow);
                    item.setAttackRange(projectileAttackRange);
                }
                else if (isGun(item.build())){
                    item.setAttackEffect(AttackEffect.Bullet);
                    item.setAttackRange(projectileAttackRange);
                }
                else if (isBlast(item.build())){
                    item.setAttackEffect(AttackEffect.Steam);
                    item.setAttackRange(blastAttackRange);
                }
            }
        }else{
            item.setHealth(randomHealth);
            item.setArmor(randomArmor);
            item.setSteam(randomSteam);

            item.setSteamConsumption(0);
        }

        return item.build().clone();
    }

    public static ItemStack GetMachineLicense(int amount){
        ItemStack item = new ItemStack(Material.PAPER);

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(GetNamespacedKey("maxMachineAmount"), PersistentDataType.INTEGER, amount);

        List<String> itemLore = new ArrayList<>();
        meta.setDisplayName(sendText("&fMachine License"));
        itemLore.add(sendText("&9Miscellaneous"));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7Max Machine: &b+"+amount));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7Increase your &f&nMax Machines"));
        itemLore.add(sendText(" &7Global Max Machine is &e"+globalMaxMachine));
        itemLore.add(sendText(" "));
        itemLore.add(sendText("&8"+usageArrowSymbol+" &7Right-Click to use"));


        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }

    public static String dungeonLootBoxKey = "dungeonLootBox";
    public static String dungeonLootBoxLevelKey = "dungeonLootBoxLevel";
    public static String dungeonLootBoxTierKey = "dungeonLootBoxTier";
    public static String lootTypeKey = "lootType";

    public static ItemStack CreateDungeonLootBox(Dungeon.LootType type, int tier){
        ItemStack item = new ItemStack(Material.ENDER_CHEST);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(GetNamespacedKey(itemKey), PersistentDataType.BOOLEAN, true);
        container.set(GetNamespacedKey(dungeonLootBoxKey), PersistentDataType.BOOLEAN, true);
        container.set(GetNamespacedKey(dungeonLootBoxTierKey), PersistentDataType.INTEGER, tier);
        container.set(GetNamespacedKey(lootTypeKey), PersistentDataType.STRING, type.toString().toLowerCase());

        List<String> itemLore = new ArrayList<>();

        meta.setDisplayName(sendText("&fDungeon Loot Box"));
        itemLore.add(sendText("&9Miscellaneous"));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7Tier: &f"+tier));
        itemLore.add(sendText(" &7Loot Type: &f"+type.toString()));
        itemLore.add(sendText(" "));
        itemLore.add(sendText("&8"+usageArrowSymbol+" &7Right-Click to open"));

        meta.setLore(itemLore);

        meta.addEnchant(Enchantment.UNBREAKING, 10, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack GetMembrane(String name, int tier){
        ItemStack item = new ItemStack(Material.ENDER_CHEST);
        if (name.equals("alien")){
            item = new ItemStack(Material.BEETROOT);
        }
        else if (name.equals("mutant")){
            item = new ItemStack(Material.SWEET_BERRIES);
        }
        else if (name.equals("undead")){
            item = new ItemStack(Material.DRIED_KELP);
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(GetNamespacedKey(itemKey), PersistentDataType.BOOLEAN, true);

        List<String> itemLore = new ArrayList<>();

        String display;
        display = formatItemName(name);
        meta.setDisplayName(sendText("&f"+display+" Membrane"));
        itemLore.add(sendText("&9Miscellaneous"));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7Tier: &f"+intToRoman(tier)));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7Obtained from dungeon"));
        itemLore.add(sendText(" &7Trade this item at &f&nTraders"));

        meta.setLore(itemLore);

        meta.addEnchant(Enchantment.UNBREAKING, 10, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
        return item;
    }


    public static void InitFactoryCustomTools(){
        FactoryItem item = new FactoryItem();

        item.setType(Type.Tool);
        item.setSubType(SubType.Pickaxe);

        item.setMaterial(Material.IRON_PICKAXE);
        item.setDisplayname("&aRedstone Pickaxe");
        item.setToolPower(7);
        item.setToolSpeed(1);
        item.setLevelMinimum(25);
        item.setRarity(Rarity.RarityType.Common);
        item.setMiningFortune(2);

        SaveItem("redstonepickaxe", new ItemStack(item.build()));
    }

    public static HashMap<String, String> storedItemStats = new HashMap<>();

    public static List<String> itemStatsList = Arrays.asList(
            "type", "subType", "attackDamage", "attackRange", "attackSpeed",
            "criticalChance", "criticalDamage", "steamConsumption",
            "health", "steam", "armor",
            "undeadDamage", "undeadDefense",
            "mutantDamage", "mutantDefense",
            "meleeDamage", "rangeDamage",
            "durability", "maxDurability",
            "toolPower", "toolSpeed", "bonusStats",
            "rarity", "displayname", "material", "attackEffect", "levelMinimum",
            "canUse", "color", "wandMultiplier",
            "boosterType", "boosterDuration", "proficiency",
            "miningFortune", "foragingFortune", "farmingFortune", "fishingFortune", "combatFortune"
    );

    public static void GenerateItemConfig(){
        File file = new File(developmentPath, itemConfigurationFile);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("configuration", null);

        for (String key : itemList.keySet()){

            ItemStack item = new ItemStack(GetItem(key));
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();


            if (isFactoryItem(item)){
                if (isWeapon(item) || isArmor(item) || isJobTool(item)){
                    String parent = "configuration.";

                    for (String itemStats : itemStatsList){
                        String itemKey = uncolouredText(meta.getDisplayName()).toLowerCase().replaceAll(" ", "").trim();
                        String statsTarget = parent+"."+itemKey+"."+itemStats;
                        String storedTarget = itemKey+"."+itemStats.toLowerCase();
                        if (config.get(statsTarget) == null){

                            if (itemStats.equals("displayname")){
                                config.set(statsTarget, meta.getDisplayName());
                                storedItemStats.put(storedTarget, meta.getDisplayName());
                            } else if (itemStats.equals("material")){
                                config.set(statsTarget, item.getType().toString());
                                storedItemStats.put(storedTarget, item.getType().toString());
                            }else{
                                if (container.has(GetNamespacedKey(baseKey+itemStats))){
                                    String value = ""+container.get(GetNamespacedKey(baseKey+itemStats), PersistentDataType.DOUBLE);
                                    config.set(statsTarget, value);
                                    storedItemStats.put(storedTarget, value);
                                }else{
                                    if (container.has(GetNamespacedKey(itemStats), PersistentDataType.DOUBLE)){
                                        String value = ""+container.get(GetNamespacedKey(itemStats), PersistentDataType.DOUBLE);
                                        config.set(statsTarget, value);
                                        storedItemStats.put(storedTarget, value);
                                    }
                                    if (container.has(GetNamespacedKey(itemStats), PersistentDataType.INTEGER)){
                                        String value = ""+container.get(GetNamespacedKey(itemStats), PersistentDataType.INTEGER);
                                        config.set(statsTarget, value);
                                        storedItemStats.put(storedTarget, value);
                                    }
                                }
                            }
                        }
                    }


                    consoleLog(sendText("&dSaved item configuration for: &b"+key));
                }
            }

        }


        try{
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }


        consoleLog(sendText("&aItem Config has been generated!"));
    }


    public static void InitWeapons(){
        Map<String, FactoryItem> factoryItemList = new HashMap<>();

        Object[][] toolData = {

                // normal weapon
                {"warriorsword", Material.WOODEN_SWORD, 3, 1.0d, 2, 0d, "Warrior Sword", Rarity.RarityType.Common, Type.Weapon, SubType.Sword, 100000, 1, 0, AttackEffect.Slash},
                {"knighthammer", Material.MACE, 3, 1.0d, 2, 0d, "Knight Hammer", Rarity.RarityType.Common, Type.Weapon, SubType.Hammer, 100000, 1, 0, AttackEffect.Smash},
                {"hunterbow", Material.BOW, 6, 1.0d, 12, 1d, "Hunter Bow", Rarity.RarityType.Common, Type.Weapon, SubType.Bow, 100000, 1, 0, AttackEffect.Arrow},
                {"pistol", Material.LEATHER_HORSE_ARMOR, 6, 2.0d, 15, 2d, "Pistol", Rarity.RarityType.Common, Type.Weapon, SubType.Gun, 100000, 1, 0, AttackEffect.Bullet},
                {"musket", Material.LEATHER_HORSE_ARMOR, 6, 3.0d, 30, 2d, "Musket", Rarity.RarityType.Common, Type.Weapon, SubType.Gun, 100000, 1, 0, AttackEffect.Bullet},
                {"steamcannon", Material.LEATHER_HORSE_ARMOR, 7, 4.0d, 8, 3d, "Steam Cannon", Rarity.RarityType.Common, Type.Weapon, SubType.Blast, 100000, 1, 0, AttackEffect.Steam},
                {"acidcannon", Material.LEATHER_HORSE_ARMOR, 7, 4.0d, 8, 3d, "Acid Cannon", Rarity.RarityType.Common, Type.Weapon, SubType.Blast, 100000, 1, 0, AttackEffect.Acid},
                {"flamethrower", Material.LEATHER_HORSE_ARMOR, 7, 4.0d, 8, 3d, "Flamethrower", Rarity.RarityType.Common, Type.Weapon, SubType.Blast, 100000, 1, 0, AttackEffect.Flamethrower},
                {"energycannon", Material.LEATHER_HORSE_ARMOR, 7, 4.0d, 8, 3d, "Energy Cannon", Rarity.RarityType.Common, Type.Weapon, SubType.Blast, 100000, 1, 0, AttackEffect.Energy},

                {"warriorsword2", Material.STONE_SWORD, 8, 1.0d, 3, 0d, "Warrior Sword", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Sword, 100000, 5, 0, AttackEffect.Slash},
                {"knighthammer2", Material.MACE, 8, 1.0d, 3, 0d, "Knight Hammer", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Hammer, 100000, 5, 0, AttackEffect.Smash},
                {"hunterbow2", Material.BOW, 11, 1.0d, 13, 2d, "Hunter Bow", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Bow, 100000, 5, 0, AttackEffect.Arrow},
                {"pistol2", Material.LEATHER_HORSE_ARMOR, 11, 1.0d, 16, 3d, "Pistol", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Gun, 100000, 5, 0, AttackEffect.Bullet},
                {"musket2", Material.LEATHER_HORSE_ARMOR, 11, 1.0d, 16, 3d, "Musket", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Gun, 100000, 5, 0, AttackEffect.Bullet},
                {"steamcannon2", Material.LEATHER_HORSE_ARMOR, 12, 1.0d, 9, 4d, "Steam Cannon", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Blast, 100000, 5, 0, AttackEffect.Steam},
                {"acidcannon2", Material.LEATHER_HORSE_ARMOR, 12, 1.0d, 9, 4d, "Acid Cannon", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Blast, 100000, 5, 0, AttackEffect.Acid},
                {"flamethrower2", Material.LEATHER_HORSE_ARMOR, 12, 1.0d, 9, 4d, "Flamethrower", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Blast, 100000, 5, 0, AttackEffect.Flamethrower},
                {"energycannon2", Material.LEATHER_HORSE_ARMOR, 12, 1.0d, 9, 4d, "Energy Cannon", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Blast, 100000, 5, 0, AttackEffect.Energy},

                {"warriorsword3", Material.STONE_SWORD, 18, 1.0d, 3, 0d, "Warrior Sword", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Sword, 100000, 10, 0, AttackEffect.Slash},
                {"knighthammer3", Material.MACE, 18, 1.0d, 3, 0d, "Knight Hammer", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Hammer, 100000, 10, 0, AttackEffect.Smash},
                {"hunterbow3", Material.BOW, 21, 1.0d, 13, 3d, "Hunter Bow", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Bow, 100000, 10, 0, AttackEffect.Arrow},
                {"pistol3", Material.LEATHER_HORSE_ARMOR, 21, 1.0d, 16, 4d, "Pistol", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Gun, 100000, 10, 0, AttackEffect.Bullet},
                {"musket3", Material.LEATHER_HORSE_ARMOR, 21, 1.0d, 16, 4d, "Musket", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Gun, 100000, 10, 0, AttackEffect.Bullet},
                {"steamcannon3", Material.LEATHER_HORSE_ARMOR, 22, 1.0d, 9, 5d, "Steam Cannon", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Blast, 100000, 10, 0, AttackEffect.Steam},
                {"acidcannon3", Material.LEATHER_HORSE_ARMOR, 22, 1.0d, 9, 5d, "Acid Cannon", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Blast, 100000, 10, 0, AttackEffect.Acid},
                {"flamethrower3", Material.LEATHER_HORSE_ARMOR, 22, 1.0d, 9, 5d, "Flamethrower", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Blast, 100000, 10, 0, AttackEffect.Flamethrower},
                {"energycannon3", Material.LEATHER_HORSE_ARMOR, 22, 1.0d, 9, 5d, "Energy Cannon", Rarity.RarityType.Uncommon, Type.Weapon, SubType.Blast, 100000, 10, 0, AttackEffect.Energy},


                // dungeon

                {"tungstensword", Material.IRON_SWORD, 25, 0.8d, 3, 0d, "Tungsten Sword", Rarity.RarityType.Rare, Type.Weapon, SubType.Sword, 100000, 20, 0, AttackEffect.Slash},
                {"tungstenhammer", Material.MACE, 25, 0.9d, 3, 0d, "Tungsten Hammer", Rarity.RarityType.Rare, Type.Weapon, SubType.Hammer, 100000, 20, 0, AttackEffect.Smash},
                {"tungstenbow", Material.BOW, 35, 1.2d, 13, 3d, "Tungsten Bow", Rarity.RarityType.Rare, Type.Weapon, SubType.Bow, 100000, 20, 0, AttackEffect.Arrow},
                {"tungstengun", Material.IRON_HORSE_ARMOR, 40, 1.1d, 16, 4d, "Tungsten Gun", Rarity.RarityType.Rare, Type.Weapon, SubType.Gun, 100000, 20, 0, AttackEffect.Bullet},

                {"tungstensword2", Material.IRON_SWORD, 35, 0.8d, 3, 0d, "Tungsten Sword", Rarity.RarityType.Epic, Type.Weapon, SubType.Sword, 100000, 30, 0, AttackEffect.Slash},
                {"tungstenhammer2", Material.MACE, 35, 0.9d, 3, 0d, "Tungsten Hammer", Rarity.RarityType.Epic, Type.Weapon, SubType.Hammer, 100000, 30, 0, AttackEffect.Smash},
                {"tungstenbow2", Material.BOW, 45, 1.2d, 13, 3d, "Tungsten Bow", Rarity.RarityType.Epic, Type.Weapon, SubType.Bow, 100000, 30, 0, AttackEffect.Arrow},
                {"tungstengun2", Material.IRON_HORSE_ARMOR, 50, 1.1d, 16, 4d, "Tungsten Gun", Rarity.RarityType.Epic, Type.Weapon, SubType.Gun, 100000, 30, 0, AttackEffect.Bullet},


                {"palladiumsword", Material.IRON_SWORD, 55, 0.7d, 3, 0d, "Palladium Sword", Rarity.RarityType.Rare, Type.Weapon, SubType.Sword, 100000, 50, 0, AttackEffect.Slash},
                {"palladiumhammer", Material.MACE, 55, 0.8d, 3, 0d, "Palladium Hammer", Rarity.RarityType.Rare, Type.Weapon, SubType.Hammer, 100000, 50, 0, AttackEffect.Smash},
                {"palladiumbow", Material.BOW, 65, 1.1d, 13, 3d, "Palladium Bow", Rarity.RarityType.Rare, Type.Weapon, SubType.Bow, 100000, 50, 0, AttackEffect.Arrow},
                {"palladiumgun", Material.IRON_HORSE_ARMOR, 70, 1.0d, 16, 4d, "Palladium Gun", Rarity.RarityType.Rare, Type.Weapon, SubType.Gun, 100000, 50, 0, AttackEffect.Bullet},

                {"palladiumsword2", Material.IRON_SWORD, 65, 0.7d, 3, 0d, "Palladium Sword", Rarity.RarityType.Epic, Type.Weapon, SubType.Sword, 100000, 60, 0, AttackEffect.Slash},
                {"palladiumhammer2", Material.MACE, 65, 0.8d, 3, 0d, "Palladium Hammer", Rarity.RarityType.Epic, Type.Weapon, SubType.Hammer, 100000, 60, 0, AttackEffect.Smash},
                {"palladiumbow2", Material.BOW, 75, 1.1d, 13, 3d, "Palladium Bow", Rarity.RarityType.Epic, Type.Weapon, SubType.Bow, 100000, 60, 0, AttackEffect.Arrow},
                {"palladiumgun2", Material.IRON_HORSE_ARMOR, 80, 1.0d, 16, 4d, "Palladium Gun", Rarity.RarityType.Epic, Type.Weapon, SubType.Gun, 100000, 60, 0, AttackEffect.Bullet},


                {"cobaltsword", Material.IRON_SWORD, 85, 0.6d, 3, 0d, "Cobalt Sword", Rarity.RarityType.Rare, Type.Weapon, SubType.Sword, 100000, 80, 0, AttackEffect.Slash},
                {"cobalthammer", Material.MACE, 85, 0.7d, 3, 0d, "Cobalt Hammer", Rarity.RarityType.Rare, Type.Weapon, SubType.Hammer, 100000, 80, 0, AttackEffect.Smash},
                {"cobaltbow", Material.BOW, 95, 1.0d, 13, 3d, "Cobalt Bow", Rarity.RarityType.Rare, Type.Weapon, SubType.Bow, 100000, 80, 0, AttackEffect.Arrow},
                {"cobaltgun", Material.IRON_HORSE_ARMOR, 100, 0.9d, 16, 4d, "Cobalt Gun", Rarity.RarityType.Rare, Type.Weapon, SubType.Gun, 100000, 80, 0, AttackEffect.Bullet},

                {"cobaltsword2", Material.IRON_SWORD, 95, 0.6d, 3, 0d, "Cobalt Sword", Rarity.RarityType.Epic, Type.Weapon, SubType.Sword, 100000, 90, 0, AttackEffect.Slash},
                {"cobalthammer2", Material.MACE, 95, 0.7d, 3, 0d, "Cobalt Hammer", Rarity.RarityType.Epic, Type.Weapon, SubType.Hammer, 100000, 90, 0, AttackEffect.Smash},
                {"cobaltbow2", Material.BOW, 105, 1.0d, 13, 3d, "Cobalt Bow", Rarity.RarityType.Epic, Type.Weapon, SubType.Bow, 100000, 90, 0, AttackEffect.Arrow},
                {"cobaltgun2", Material.IRON_HORSE_ARMOR, 110, 0.9d, 16, 4d, "Cobalt Gun", Rarity.RarityType.Epic, Type.Weapon, SubType.Gun, 100000, 90, 0, AttackEffect.Bullet},


                {"mithrilsword", Material.IRON_SWORD, 105, 0.5d, 3, 0d, "Mithril Sword", Rarity.RarityType.Rare, Type.Weapon, SubType.Sword, 100000, 110, 0, AttackEffect.Slash},
                {"mithrilhammer", Material.MACE, 105, 0.6d, 3, 0d, "Mithril Hammer", Rarity.RarityType.Rare, Type.Weapon, SubType.Hammer, 100000, 110, 0, AttackEffect.Smash},
                {"mithrilbow", Material.BOW, 115, 0.9d, 13, 3d, "Mithril Bow", Rarity.RarityType.Rare, Type.Weapon, SubType.Bow, 100000, 110, 0, AttackEffect.Arrow},
                {"mithrilgun", Material.IRON_HORSE_ARMOR, 120, 0.8d, 16, 4d, "Mithril Gun", Rarity.RarityType.Rare, Type.Weapon, SubType.Gun, 100000, 110, 0, AttackEffect.Bullet},

                {"mithrilsword2", Material.IRON_SWORD, 125, 0.5d, 3, 0d, "Mithril Sword", Rarity.RarityType.Epic, Type.Weapon, SubType.Sword, 100000, 120, 0, AttackEffect.Slash},
                {"mithrilhammer2", Material.MACE, 125, 0.6d, 3, 0d, "Mithril Hammer", Rarity.RarityType.Epic, Type.Weapon, SubType.Hammer, 100000, 120, 0, AttackEffect.Smash},
                {"mithrilbow2", Material.BOW, 135, 0.9d, 13, 3d, "Mithril Bow", Rarity.RarityType.Epic, Type.Weapon, SubType.Bow, 100000, 120, 0, AttackEffect.Arrow},
                {"mithrilgun2", Material.IRON_HORSE_ARMOR, 140, 0.8d, 16, 4d, "Mithril Gun", Rarity.RarityType.Epic, Type.Weapon, SubType.Gun, 100000, 120, 0, AttackEffect.Bullet},


                {"orichalcumsword", Material.IRON_SWORD, 145, 0.4d, 3, 0d, "Orichalcum Sword", Rarity.RarityType.Rare, Type.Weapon, SubType.Sword, 100000, 140, 0, AttackEffect.Slash},
                {"orichalcumhammer", Material.MACE, 145, 0.5d, 3, 0d, "Orichalcum Hammer", Rarity.RarityType.Rare, Type.Weapon, SubType.Hammer, 100000, 140, 0, AttackEffect.Smash},
                {"orichalcumbow", Material.BOW, 155, 0.8d, 13, 3d, "Orichalcum Bow", Rarity.RarityType.Rare, Type.Weapon, SubType.Bow, 100000, 140, 0, AttackEffect.Arrow},
                {"orichalcumgun", Material.IRON_HORSE_ARMOR, 160, 0.7d, 16, 4d, "Orichalcum Gun", Rarity.RarityType.Rare, Type.Weapon, SubType.Gun, 100000, 140, 0, AttackEffect.Bullet},

                {"orichalcumsword2", Material.IRON_SWORD, 155, 0.4d, 3, 0d, "Orichalcum Sword", Rarity.RarityType.Epic, Type.Weapon, SubType.Sword, 100000, 150, 0, AttackEffect.Slash},
                {"orichalcumhammer2", Material.MACE, 155, 0.5d, 3, 0d, "Orichalcum Hammer", Rarity.RarityType.Epic, Type.Weapon, SubType.Hammer, 100000, 150, 0, AttackEffect.Smash},
                {"orichalcumbow2", Material.BOW, 165, 0.8d, 13, 3d, "Orichalcum Bow", Rarity.RarityType.Epic, Type.Weapon, SubType.Bow, 100000, 150, 0, AttackEffect.Arrow},
                {"orichalcumgun2", Material.IRON_HORSE_ARMOR, 170, 0.7d, 16, 4d, "Orichalcum Gun", Rarity.RarityType.Epic, Type.Weapon, SubType.Gun, 100000, 150, 0, AttackEffect.Bullet},


                {"titaniumsword", Material.IRON_SWORD, 175, 0.3d, 3, 0d, "Titanium Sword", Rarity.RarityType.Rare, Type.Weapon, SubType.Sword, 100000, 170, 0, AttackEffect.Slash},
                {"titaniumhammer", Material.MACE, 175, 0.4d, 3, 0d, "Titanium Hammer", Rarity.RarityType.Rare, Type.Weapon, SubType.Hammer, 100000, 170, 0, AttackEffect.Smash},
                {"titaniumbow", Material.BOW, 185, 0.7d, 13, 3d, "Titanium Bow", Rarity.RarityType.Rare, Type.Weapon, SubType.Bow, 100000, 170, 0, AttackEffect.Arrow},
                {"titaniumgun", Material.IRON_HORSE_ARMOR, 190, 0.6d, 16, 4d, "Titanium Gun", Rarity.RarityType.Rare, Type.Weapon, SubType.Gun, 100000, 170, 0, AttackEffect.Bullet},

                {"titaniumsword2", Material.IRON_SWORD, 185, 0.3d, 3, 0d, "Titanium Sword", Rarity.RarityType.Epic, Type.Weapon, SubType.Sword, 100000, 180, 0, AttackEffect.Slash},
                {"titaniumhammer2", Material.MACE, 185, 0.4d, 3, 0d, "Titanium Hammer", Rarity.RarityType.Epic, Type.Weapon, SubType.Hammer, 100000, 180, 0, AttackEffect.Smash},
                {"titaniumbow2", Material.BOW, 195, 0.7d, 13, 3d, "Titanium Bow", Rarity.RarityType.Epic, Type.Weapon, SubType.Bow, 100000, 180, 0, AttackEffect.Arrow},
                {"titaniumgun2", Material.IRON_HORSE_ARMOR, 200, 0.6d, 16, 4d, "Titanium Gun", Rarity.RarityType.Epic, Type.Weapon, SubType.Gun, 100000, 180, 0, AttackEffect.Bullet},


                {"adamantinesword", Material.IRON_SWORD, 205, 0.3d, 3, 0d, "Adamantine Sword", Rarity.RarityType.Rare, Type.Weapon, SubType.Sword, 100000, 200, 0, AttackEffect.Slash},
                {"adamantinehammer", Material.MACE, 205, 0.4d, 3, 0d, "Adamantine Hammer", Rarity.RarityType.Rare, Type.Weapon, SubType.Hammer, 100000, 200, 0, AttackEffect.Smash},
                {"adamantinebow", Material.BOW, 215, 0.7d, 13, 3d, "Adamantine Bow", Rarity.RarityType.Rare, Type.Weapon, SubType.Bow, 100000, 200, 0, AttackEffect.Arrow},
                {"adamantinegun", Material.IRON_HORSE_ARMOR, 220, 0.6d, 16, 4d, "Adamantine Gun", Rarity.RarityType.Rare, Type.Weapon, SubType.Gun, 100000, 200, 0, AttackEffect.Bullet},

                {"adamantinesword2", Material.IRON_SWORD, 215, 0.3d, 3, 0d, "Adamantine Sword", Rarity.RarityType.Epic, Type.Weapon, SubType.Sword, 100000, 210, 0, AttackEffect.Slash},
                {"adamantinehammer2", Material.MACE, 215, 0.4d, 3, 0d, "Adamantine Hammer", Rarity.RarityType.Epic, Type.Weapon, SubType.Hammer, 100000, 210, 0, AttackEffect.Smash},
                {"adamantinebow2", Material.BOW, 225, 0.7d, 13, 3d, "Adamantine Bow", Rarity.RarityType.Epic, Type.Weapon, SubType.Bow, 100000, 210, 0, AttackEffect.Arrow},
                {"adamantinegun2", Material.IRON_HORSE_ARMOR, 230, 0.6d, 16, 4d, "Adamantine Gun", Rarity.RarityType.Epic, Type.Weapon, SubType.Gun, 100000, 210, 0, AttackEffect.Bullet},


                {"dragonitesword", Material.IRON_SWORD, 235, 0.3d, 3, 0d, "Dragonite Sword", Rarity.RarityType.Rare, Type.Weapon, SubType.Sword, 100000, 230, 0, AttackEffect.Slash},
                {"dragonitehammer", Material.MACE, 235, 0.4d, 3, 0d, "Dragonite Hammer", Rarity.RarityType.Rare, Type.Weapon, SubType.Hammer, 100000, 230, 0, AttackEffect.Smash},
                {"dragonitebow", Material.BOW, 245, 0.7d, 13, 3d, "Dragonite Bow", Rarity.RarityType.Rare, Type.Weapon, SubType.Bow, 100000, 230, 0, AttackEffect.Arrow},
                {"dragonitegun", Material.IRON_HORSE_ARMOR, 250, 0.6d, 16, 4d, "Dragonite Gun", Rarity.RarityType.Rare, Type.Weapon, SubType.Gun, 100000, 230, 0, AttackEffect.Bullet},

                {"dragonitesword2", Material.IRON_SWORD, 245, 0.3d, 3, 0d, "Dragonite Sword", Rarity.RarityType.Epic, Type.Weapon, SubType.Sword, 100000, 240, 0, AttackEffect.Slash},
                {"dragonitehammer2", Material.MACE, 245, 0.4d, 3, 0d, "Dragonite Hammer", Rarity.RarityType.Epic, Type.Weapon, SubType.Hammer, 100000, 240, 0, AttackEffect.Smash},
                {"dragonitebow2", Material.BOW, 255, 0.7d, 13, 3d, "Dragonite Bow", Rarity.RarityType.Epic, Type.Weapon, SubType.Bow, 100000, 240, 0, AttackEffect.Arrow},
                {"dragonitegun2", Material.IRON_HORSE_ARMOR, 260, 0.6d, 16, 4d, "Dragonite Gun", Rarity.RarityType.Epic, Type.Weapon, SubType.Gun, 100000, 240, 0, AttackEffect.Bullet},


                {"voidsteelsword", Material.IRON_SWORD, 265, 0.2d, 3, 0d, "Voidsteel Sword", Rarity.RarityType.Rare, Type.Weapon, SubType.Sword, 100000, 260, 0, AttackEffect.Slash},
                {"voidsteelhammer", Material.MACE, 265, 0.3d, 3, 0d, "Voidsteel Hammer", Rarity.RarityType.Rare, Type.Weapon, SubType.Hammer, 100000, 260, 0, AttackEffect.Smash},
                {"voidsteelbow", Material.BOW, 275, 0.6d, 13, 3d, "Voidsteel Bow", Rarity.RarityType.Rare, Type.Weapon, SubType.Bow, 100000, 260, 0, AttackEffect.Arrow},
                {"voidsteelgun", Material.IRON_HORSE_ARMOR, 280, 0.5d, 16, 4d, "Voidsteel Gun", Rarity.RarityType.Rare, Type.Weapon, SubType.Gun, 100000, 260, 0, AttackEffect.Bullet},

                {"voidsteelsword2", Material.IRON_SWORD, 275, 0.2d, 3, 0d, "Voidsteel Sword", Rarity.RarityType.Epic, Type.Weapon, SubType.Sword, 100000, 270, 0, AttackEffect.Slash},
                {"voidsteelhammer2", Material.MACE, 275, 0.3d, 3, 0d, "Voidsteel Hammer", Rarity.RarityType.Epic, Type.Weapon, SubType.Hammer, 100000, 270, 0, AttackEffect.Smash},
                {"voidsteelbow2", Material.BOW, 285, 0.6d, 13, 3d, "Voidsteel Bow", Rarity.RarityType.Epic, Type.Weapon, SubType.Bow, 100000, 270, 0, AttackEffect.Arrow},
                {"voidsteelgun2", Material.IRON_HORSE_ARMOR, 290, 0.5d, 16, 4d, "Voidsteel Gun", Rarity.RarityType.Epic, Type.Weapon, SubType.Gun, 100000, 270, 0, AttackEffect.Bullet},


                {"etheriumsword", Material.IRON_SWORD, 295, 0.2d, 3, 0d, "Etherium Sword", Rarity.RarityType.Rare, Type.Weapon, SubType.Sword, 100000, 290, 0, AttackEffect.Slash},
                {"etheriumhammer", Material.MACE, 295, 0.3d, 3, 0d, "Etherium Hammer", Rarity.RarityType.Rare, Type.Weapon, SubType.Hammer, 100000, 290, 0, AttackEffect.Smash},
                {"etheriumbow", Material.BOW, 315, 0.6d, 13, 3d, "Etherium Bow", Rarity.RarityType.Rare, Type.Weapon, SubType.Bow, 100000, 290, 0, AttackEffect.Arrow},
                {"etheriumgun", Material.IRON_HORSE_ARMOR, 320, 0.5d, 16, 4d, "Etherium Gun", Rarity.RarityType.Rare, Type.Weapon, SubType.Gun, 100000, 290, 0, AttackEffect.Bullet},

                {"etheriumsword2", Material.IRON_SWORD, 305, 0.2d, 3, 0d, "Etherium Sword", Rarity.RarityType.Epic, Type.Weapon, SubType.Sword, 100000, 300, 0, AttackEffect.Slash},
                {"etheriumhammer2", Material.MACE, 305, 0.3d, 3, 0d, "Etherium Hammer", Rarity.RarityType.Epic, Type.Weapon, SubType.Hammer, 100000, 300, 0, AttackEffect.Smash},
                {"etheriumbow2", Material.BOW, 315, 0.6d, 13, 3d, "Etherium Bow", Rarity.RarityType.Epic, Type.Weapon, SubType.Bow, 100000, 300, 0, AttackEffect.Arrow},
                {"etheriumgun2", Material.IRON_HORSE_ARMOR, 320, 0.5d, 16, 4d, "Etherium Gun", Rarity.RarityType.Epic, Type.Weapon, SubType.Gun, 100000, 300, 0, AttackEffect.Bullet},

        };



        for (Object[] data : toolData) {
            String key = (String) data[0];
            Material material = (Material) data[1];
            int attackDamage = (int) data[2];
            double attackSpeed = (double) data[3];
            int attackRange = (int) data[4];
            double steamConsumption = (double) data[5];
            String displayName = (String) data[6];
            Rarity.RarityType rarity = (Rarity.RarityType) data[7];
            Type type = (Type) data[8];
            SubType subType = (SubType) data[9];
            int durability = (int) data[10];

            int levelMinimum = (int) data[11];
            AttackEffect attackEffect = (AttackEffect) data[13];

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
            item.setSteamConsumption(steamConsumption);
            item.setHealth(0);
            item.setSteam(0);
            item.setArmor(0);
            item.setAttackEffect(attackEffect);

            item.setLevelMinimum(levelMinimum);

            int criticalChance = 10;
            if (levelMinimum > 15){
                criticalChance = 15+(levelMinimum/4);
                if (subType == SubType.Hammer){
                    criticalChance = 15+(levelMinimum/2);
                }
            }

            item.setCriticalChance(criticalChance);


            factoryItemList.put(key, item);
        }

        for (String key : factoryItemList.keySet()) {
            SaveItem(key, factoryItemList.get(key).build().clone());
        }



        Object[][] equipmentData = {


                // pre dungeon 1
                {"reinforcedcopperhelmet", Material.PLAYER_HEAD, "Reinforced Copper Helmet", Rarity.RarityType.Common, Type.Equipment, SubType.Helmet, 100000, 1, "Melee Damage:1", Color.ORANGE},
                {"reinforcedcopperheadgear", Material.PLAYER_HEAD, "Reinforced Copper Headgear", Rarity.RarityType.Common, Type.Equipment, SubType.Headgear, 100000, 1, "Range Damage:1", Color.ORANGE},
                {"reinforcedcopperchestplate", Material.LEATHER_CHESTPLATE, "Reinforced Copper Chestplate", Rarity.RarityType.Common, Type.Equipment, SubType.Chestplate, 100000, 1, "", Color.ORANGE},
                {"reinforcedcopperleggings", Material.LEATHER_LEGGINGS, "Reinforced Copper Leggings", Rarity.RarityType.Common, Type.Equipment, SubType.Leggings, 100000, 1, "", Color.ORANGE},
                {"reinforcedcopperboots", Material.LEATHER_BOOTS, "Reinforced Copper Boots", Rarity.RarityType.Common, Type.Equipment, SubType.Boots, 100000, 1, "", Color.ORANGE},

                {"reinforcedredstonehelmet", Material.PLAYER_HEAD, "Reinforced Redstone Helmet", Rarity.RarityType.Uncommon, Type.Equipment, SubType.Helmet, 100000, 5, "Melee Damage:3", Color.RED},
                {"reinforcedredstoneheadgear", Material.PLAYER_HEAD, "Reinforced Redstone Headgear", Rarity.RarityType.Uncommon, Type.Equipment, SubType.Headgear, 100000, 5, "Range Damage:3", Color.RED},
                {"reinforcedredstonechestplate", Material.LEATHER_CHESTPLATE, "Reinforced Redstone Chestplate", Rarity.RarityType.Uncommon, Type.Equipment, SubType.Chestplate, 100000, 5, "", Color.RED},
                {"reinforcedredstoneleggings", Material.LEATHER_LEGGINGS, "Reinforced Redstone Leggings", Rarity.RarityType.Uncommon, Type.Equipment, SubType.Leggings, 100000, 5, "", Color.RED},
                {"reinforcedredstoneboots", Material.LEATHER_BOOTS, "Reinforced Redstone Boots", Rarity.RarityType.Uncommon, Type.Equipment, SubType.Boots, 100000, 5, "", Color.RED},

                {"reinforcedlapishelmet", Material.PLAYER_HEAD, "Reinforced Lapis Helmet", Rarity.RarityType.Uncommon, Type.Equipment, SubType.Helmet, 100000, 10, "Melee Damage:4", Color.BLUE},
                {"reinforcedlapisheadgear", Material.PLAYER_HEAD, "Reinforced Lapis Headgear", Rarity.RarityType.Uncommon, Type.Equipment, SubType.Headgear, 100000, 10, "Range Damage:4", Color.BLUE},
                {"reinforcedlapischestplate", Material.LEATHER_CHESTPLATE, "Reinforced Lapis Chestplate", Rarity.RarityType.Uncommon, Type.Equipment, SubType.Chestplate, 100000, 10, "", Color.BLUE},
                {"reinforcedlapisleggings", Material.LEATHER_LEGGINGS, "Reinforced Lapis Leggings", Rarity.RarityType.Uncommon, Type.Equipment, SubType.Leggings, 100000, 10, "", Color.BLUE},
                {"reinforcedlapisboots", Material.LEATHER_BOOTS, "Reinforced Lapis Boots", Rarity.RarityType.Uncommon, Type.Equipment, SubType.Boots, 100000, 10, "", Color.BLUE},


                // dungeon 1
                {"tungstenhelmet", Material.PLAYER_HEAD, "Tungsten Helmet", Rarity.RarityType.Rare, Type.Equipment, SubType.Helmet, 100000, 20, "Melee Damage:5", Color.fromRGB(223, 222, 222)},
                {"tungstenheadgear", Material.PLAYER_HEAD, "Tungsten Headgear", Rarity.RarityType.Rare, Type.Equipment, SubType.Headgear, 100000, 20, "Range Damage:5", Color.fromRGB(223, 222, 222)},
                {"tungstenchestplate", Material.LEATHER_CHESTPLATE, "Tungsten Chestplate", Rarity.RarityType.Rare, Type.Equipment, SubType.Chestplate, 100000, 20, "", Color.fromRGB(223, 222, 222)},
                {"tungstenleggings", Material.LEATHER_LEGGINGS, "Tungsten Leggings", Rarity.RarityType.Rare, Type.Equipment, SubType.Leggings, 100000, 20, "", Color.fromRGB(223, 222, 222)},
                {"tungstenboots", Material.LEATHER_BOOTS, "Tungsten Boots", Rarity.RarityType.Rare, Type.Equipment, SubType.Boots, 100000, 20, "Movement Speed:5", Color.fromRGB(223, 222, 222)},

                {"tungstenhelmet2", Material.PLAYER_HEAD, "Tungsten Helmet", Rarity.RarityType.Epic, Type.Equipment, SubType.Helmet, 100000, 30, "Melee Damage:10", Color.fromRGB(223, 222, 222)},
                {"tungstenheadgear2", Material.PLAYER_HEAD, "Tungsten Headgear", Rarity.RarityType.Epic, Type.Equipment, SubType.Headgear, 100000, 30, "Range Damage:10", Color.fromRGB(223, 222, 222)},
                {"tungstenchestplate2", Material.LEATHER_CHESTPLATE, "Tungsten Chestplate", Rarity.RarityType.Epic, Type.Equipment, SubType.Chestplate, 100000, 30, "", Color.fromRGB(223, 222, 222)},
                {"tungstenleggings2", Material.LEATHER_LEGGINGS, "Tungsten Leggings", Rarity.RarityType.Epic, Type.Equipment, SubType.Leggings, 100000, 30, "", Color.fromRGB(223, 222, 222)},
                {"tungstenboots2", Material.LEATHER_BOOTS, "Tungsten Boots", Rarity.RarityType.Epic, Type.Equipment, SubType.Boots, 100000, 30, "Movement Speed:10", Color.fromRGB(223, 222, 222)},


                {"palladiumhelmet", Material.PLAYER_HEAD, "Palladium Helmet", Rarity.RarityType.Rare, Type.Equipment, SubType.Helmet, 100000, 50, "Melee Damage:20", Color.fromRGB(213, 146, 0)},
                {"palladiumheadgear", Material.PLAYER_HEAD, "Palladium Headgear", Rarity.RarityType.Rare, Type.Equipment, SubType.Headgear, 100000, 50, "Range Damage:20", Color.fromRGB(213, 146, 0)},
                {"palladiumchestplate", Material.LEATHER_CHESTPLATE, "Palladium Chestplate", Rarity.RarityType.Rare, Type.Equipment, SubType.Chestplate, 100000, 50, "", Color.fromRGB(213, 146, 0)},
                {"palladiumleggings", Material.LEATHER_LEGGINGS, "Palladium Leggings", Rarity.RarityType.Rare, Type.Equipment, SubType.Leggings, 100000, 50, "", Color.fromRGB(213, 146, 0)},
                {"palladiumboots", Material.LEATHER_BOOTS, "Palladium Boots", Rarity.RarityType.Rare, Type.Equipment, SubType.Boots, 100000, 50, "Movement Speed:20", Color.fromRGB(213, 146, 0)},

                {"palladiumhelmet2", Material.PLAYER_HEAD, "Palladium Helmet", Rarity.RarityType.Epic, Type.Equipment, SubType.Helmet, 100000, 60, "Melee Damage:30", Color.fromRGB(213, 146, 0)},
                {"palladiumheadgear2", Material.PLAYER_HEAD, "Palladium Headgear", Rarity.RarityType.Epic, Type.Equipment, SubType.Headgear, 100000, 60, "Range Damage:30", Color.fromRGB(213, 146, 0)},
                {"palladiumchestplate2", Material.LEATHER_CHESTPLATE, "Palladium Chestplate", Rarity.RarityType.Epic, Type.Equipment, SubType.Chestplate, 100000, 60, "", Color.fromRGB(213, 146, 0)},
                {"palladiumleggings2", Material.LEATHER_LEGGINGS, "Palladium Leggings", Rarity.RarityType.Epic, Type.Equipment, SubType.Leggings, 100000, 60, "", Color.fromRGB(213, 146, 0)},
                {"palladiumboots2", Material.LEATHER_BOOTS, "Palladium Boots", Rarity.RarityType.Epic, Type.Equipment, SubType.Boots, 100000, 60, "Movement Speed:30", Color.fromRGB(213, 146, 0)},


                {"cobalthelmet", Material.PLAYER_HEAD, "Cobalt Helmet", Rarity.RarityType.Rare, Type.Equipment, SubType.Helmet, 100000, 80, "Melee Damage:40", Color.fromRGB(124, 152, 247)},
                {"cobaltheadgear", Material.PLAYER_HEAD, "Cobalt Headgear", Rarity.RarityType.Rare, Type.Equipment, SubType.Headgear, 100000, 80, "Range Damage:40", Color.fromRGB(124, 152, 247)},
                {"cobaltchestplate", Material.LEATHER_CHESTPLATE, "Cobalt Chestplate", Rarity.RarityType.Rare, Type.Equipment, SubType.Chestplate, 100000, 80, "", Color.fromRGB(124, 152, 247)},
                {"cobaltleggings", Material.LEATHER_LEGGINGS, "Cobalt Leggings", Rarity.RarityType.Rare, Type.Equipment, SubType.Leggings, 100000, 80, "", Color.fromRGB(124, 152, 247)},
                {"cobaltboots", Material.LEATHER_BOOTS, "Cobalt Boots", Rarity.RarityType.Rare, Type.Equipment, SubType.Boots, 100000, 80, "Movement Speed:40", Color.fromRGB(124, 152, 247)},

                {"cobalthelmet2", Material.PLAYER_HEAD, "Cobalt Helmet", Rarity.RarityType.Epic, Type.Equipment, SubType.Helmet, 100000, 90, "Melee Damage:50", Color.fromRGB(124, 152, 247)},
                {"cobaltheadgear2", Material.PLAYER_HEAD, "Cobalt Headgear", Rarity.RarityType.Epic, Type.Equipment, SubType.Headgear, 100000, 90, "Range Damage:50", Color.fromRGB(124, 152, 247)},
                {"cobaltchestplate2", Material.LEATHER_CHESTPLATE, "Cobalt Chestplate", Rarity.RarityType.Epic, Type.Equipment, SubType.Chestplate, 100000, 90, "", Color.fromRGB(124, 152, 247)},
                {"cobaltleggings2", Material.LEATHER_LEGGINGS, "Cobalt Leggings", Rarity.RarityType.Epic, Type.Equipment, SubType.Leggings, 100000, 90, "", Color.fromRGB(124, 152, 247)},
                {"cobaltboots2", Material.LEATHER_BOOTS, "Cobalt Boots", Rarity.RarityType.Epic, Type.Equipment, SubType.Boots, 100000, 90, "Movement Speed:50", Color.fromRGB(124, 152, 247)},


                {"mithrilhelmet", Material.PLAYER_HEAD, "Mithril Helmet", Rarity.RarityType.Rare, Type.Equipment, SubType.Helmet, 100000, 110, "Melee Damage:60", Color.fromRGB(148, 255, 228)},
                {"mithrilheadgear", Material.PLAYER_HEAD, "Mithril Headgear", Rarity.RarityType.Rare, Type.Equipment, SubType.Headgear, 100000, 110, "Range Damage:60", Color.fromRGB(148, 255, 228)},
                {"mithrilchestplate", Material.LEATHER_CHESTPLATE, "Mithril Chestplate", Rarity.RarityType.Rare, Type.Equipment, SubType.Chestplate, 100000, 110, "", Color.fromRGB(148, 255, 228)},
                {"mithrilleggings", Material.LEATHER_LEGGINGS, "Mithril Leggings", Rarity.RarityType.Rare, Type.Equipment, SubType.Leggings, 100000, 110, "", Color.fromRGB(148, 255, 228)},
                {"mithrilboots", Material.LEATHER_BOOTS, "Mithril Boots", Rarity.RarityType.Rare, Type.Equipment, SubType.Boots, 100000, 110, "Movement Speed:60", Color.fromRGB(148, 255, 228)},

                {"mithrilhelmet2", Material.PLAYER_HEAD, "Mithril Helmet", Rarity.RarityType.Epic, Type.Equipment, SubType.Helmet, 100000, 120, "Melee Damage:70", Color.fromRGB(148, 255, 228)},
                {"mithrilheadgear2", Material.PLAYER_HEAD, "Mithril Headgear", Rarity.RarityType.Epic, Type.Equipment, SubType.Headgear, 100000, 120, "Range Damage:70", Color.fromRGB(148, 255, 228)},
                {"mithrilchestplate2", Material.LEATHER_CHESTPLATE, "Mithril Chestplate", Rarity.RarityType.Epic, Type.Equipment, SubType.Chestplate, 100000, 120, "", Color.fromRGB(148, 255, 228)},
                {"mithrilleggings2", Material.LEATHER_LEGGINGS, "Mithril Leggings", Rarity.RarityType.Epic, Type.Equipment, SubType.Leggings, 100000, 120, "", Color.fromRGB(148, 255, 228)},
                {"mithrilboots2", Material.LEATHER_BOOTS, "Mithril Boots", Rarity.RarityType.Epic, Type.Equipment, SubType.Boots, 100000, 120, "Movement Speed:70", Color.fromRGB(148, 255, 228)},


                {"orichalcumhelmet", Material.PLAYER_HEAD, "Orichalcum Helmet", Rarity.RarityType.Rare, Type.Equipment, SubType.Helmet, 100000, 140, "Melee Damage:80", Color.fromRGB(199, 169, 248)},
                {"orichalcumheadgear", Material.PLAYER_HEAD, "Orichalcum Headgear", Rarity.RarityType.Rare, Type.Equipment, SubType.Headgear, 100000, 140, "Range Damage:80", Color.fromRGB(199, 169, 248)},
                {"orichalcumchestplate", Material.LEATHER_CHESTPLATE, "Orichalcum Chestplate", Rarity.RarityType.Rare, Type.Equipment, SubType.Chestplate, 100000, 140, "", Color.fromRGB(199, 169, 248)},
                {"orichalcumleggings", Material.LEATHER_LEGGINGS, "Orichalcum Leggings", Rarity.RarityType.Rare, Type.Equipment, SubType.Leggings, 100000, 140, "", Color.fromRGB(199, 169, 248)},
                {"orichalcumboots", Material.LEATHER_BOOTS, "Orichalcum Boots", Rarity.RarityType.Rare, Type.Equipment, SubType.Boots, 100000, 140, "Movement Speed:80", Color.fromRGB(199, 169, 248)},

                {"orichalcumhelmet2", Material.PLAYER_HEAD, "Orichalcum Helmet", Rarity.RarityType.Epic, Type.Equipment, SubType.Helmet, 100000, 150, "Melee Damage:90", Color.fromRGB(199, 169, 248)},
                {"orichalcumheadgear2", Material.PLAYER_HEAD, "Orichalcum Headgear", Rarity.RarityType.Epic, Type.Equipment, SubType.Headgear, 100000, 150, "Range Damage:90", Color.fromRGB(199, 169, 248)},
                {"orichalcumchestplate2", Material.LEATHER_CHESTPLATE, "Orichalcum Chestplate", Rarity.RarityType.Epic, Type.Equipment, SubType.Chestplate, 100000, 150, "", Color.fromRGB(199, 169, 248)},
                {"orichalcumleggings2", Material.LEATHER_LEGGINGS, "Orichalcum Leggings", Rarity.RarityType.Epic, Type.Equipment, SubType.Leggings, 100000, 150, "", Color.fromRGB(199, 169, 248)},
                {"orichalcumboots2", Material.LEATHER_BOOTS, "Orichalcum Boots", Rarity.RarityType.Epic, Type.Equipment, SubType.Boots, 100000, 150, "Movement Speed:90", Color.fromRGB(199, 169, 248)},


                {"titaniumhelmet", Material.PLAYER_HEAD, "Titanium Helmet", Rarity.RarityType.Rare, Type.Equipment, SubType.Helmet, 100000, 170, "Melee Damage:100", Color.GRAY},
                {"titaniumheadgear", Material.PLAYER_HEAD, "Titanium Headgear", Rarity.RarityType.Rare, Type.Equipment, SubType.Headgear, 100000, 170, "Range Damage:100", Color.GRAY},
                {"titaniumchestplate", Material.LEATHER_CHESTPLATE, "Titanium Chestplate", Rarity.RarityType.Rare, Type.Equipment, SubType.Chestplate, 100000, 170, "", Color.GRAY},
                {"titaniumleggings", Material.LEATHER_LEGGINGS, "Titanium Leggings", Rarity.RarityType.Rare, Type.Equipment, SubType.Leggings, 100000, 170, "", Color.GRAY},
                {"titaniumboots", Material.LEATHER_BOOTS, "Titanium Boots", Rarity.RarityType.Rare, Type.Equipment, SubType.Boots, 100000, 170, "Movement Speed:100", Color.GRAY},

                {"titaniumhelmet2", Material.PLAYER_HEAD, "Titanium Helmet", Rarity.RarityType.Epic, Type.Equipment, SubType.Helmet, 100000, 180, "Melee Damage:110", Color.GRAY},
                {"titaniumheadgear2", Material.PLAYER_HEAD, "Titanium Headgear", Rarity.RarityType.Epic, Type.Equipment, SubType.Headgear, 100000, 180, "Range Damage:110", Color.GRAY},
                {"titaniumchestplate2", Material.LEATHER_CHESTPLATE, "Titanium Chestplate", Rarity.RarityType.Epic, Type.Equipment, SubType.Chestplate, 100000, 180, "", Color.GRAY},
                {"titaniumleggings2", Material.LEATHER_LEGGINGS, "Titanium Leggings", Rarity.RarityType.Epic, Type.Equipment, SubType.Leggings, 100000, 180, "", Color.GRAY},
                {"titaniumboots2", Material.LEATHER_BOOTS, "Titanium Boots", Rarity.RarityType.Epic, Type.Equipment, SubType.Boots, 100000, 180, "Movement Speed:110", Color.GRAY},


                {"adamantinehelmet", Material.PLAYER_HEAD, "Adamantine Helmet", Rarity.RarityType.Rare, Type.Equipment, SubType.Helmet, 100000, 200, "Melee Damage:120", Color.fromRGB(205, 205, 205)},
                {"adamantineheadgear", Material.PLAYER_HEAD, "Adamantine Headgear", Rarity.RarityType.Rare, Type.Equipment, SubType.Headgear, 100000, 200, "Range Damage:120", Color.fromRGB(205, 205, 205)},
                {"adamantinechestplate", Material.LEATHER_CHESTPLATE, "Adamantine Chestplate", Rarity.RarityType.Rare, Type.Equipment, SubType.Chestplate, 100000, 200, "", Color.fromRGB(205, 205, 205)},
                {"adamantineleggings", Material.LEATHER_LEGGINGS, "Adamantine Leggings", Rarity.RarityType.Rare, Type.Equipment, SubType.Leggings, 100000, 200, "", Color.fromRGB(205, 205, 205)},
                {"adamantineboots", Material.LEATHER_BOOTS, "Adamantine Boots", Rarity.RarityType.Rare, Type.Equipment, SubType.Boots, 100000, 200, "Movement Speed:120", Color.fromRGB(205, 205, 205)},

                {"adamantinehelmet2", Material.PLAYER_HEAD, "Adamantine Helmet", Rarity.RarityType.Epic, Type.Equipment, SubType.Helmet, 100000, 210, "Melee Damage:130", Color.fromRGB(205, 205, 205)},
                {"adamantineheadgear2", Material.PLAYER_HEAD, "Adamantine Headgear", Rarity.RarityType.Epic, Type.Equipment, SubType.Headgear, 100000, 210, "Range Damage:130", Color.fromRGB(205, 205, 205)},
                {"adamantinechestplate2", Material.LEATHER_CHESTPLATE, "Adamantine Chestplate", Rarity.RarityType.Epic, Type.Equipment, SubType.Chestplate, 100000, 210, "", Color.fromRGB(205, 205, 205)},
                {"adamantineleggings2", Material.LEATHER_LEGGINGS, "Adamantine Leggings", Rarity.RarityType.Epic, Type.Equipment, SubType.Leggings, 100000, 210, "", Color.fromRGB(205, 205, 205)},
                {"adamantineboots2", Material.LEATHER_BOOTS, "Adamantine Boots", Rarity.RarityType.Epic, Type.Equipment, SubType.Boots, 100000, 210, "Movement Speed:130", Color.fromRGB(205, 205, 205)},


                {"dragonitehelmet", Material.PLAYER_HEAD, "Dragonite Helmet", Rarity.RarityType.Rare, Type.Equipment, SubType.Helmet, 100000, 230, "Melee Damage:140", Color.fromRGB(55, 72, 254)},
                {"dragoniteheadgear", Material.PLAYER_HEAD, "Dragonite Headgear", Rarity.RarityType.Rare, Type.Equipment, SubType.Headgear, 100000, 230, "Range Damage:140", Color.fromRGB(55, 72, 254)},
                {"dragonitechestplate", Material.LEATHER_CHESTPLATE, "Dragonite Chestplate", Rarity.RarityType.Rare, Type.Equipment, SubType.Chestplate, 100000, 230, "", Color.fromRGB(55, 72, 254)},
                {"dragoniteleggings", Material.LEATHER_LEGGINGS, "Dragonite Leggings", Rarity.RarityType.Rare, Type.Equipment, SubType.Leggings, 100000, 230, "", Color.fromRGB(55, 72, 254)},
                {"dragoniteboots", Material.LEATHER_BOOTS, "Dragonite Boots", Rarity.RarityType.Rare, Type.Equipment, SubType.Boots, 100000, 230, "Movement Speed:140", Color.fromRGB(55, 72, 254)},

                {"dragonitehelmet2", Material.PLAYER_HEAD, "Dragonite Helmet", Rarity.RarityType.Epic, Type.Equipment, SubType.Helmet, 100000, 240, "Melee Damage:150", Color.fromRGB(55, 72, 254)},
                {"dragoniteheadgear2", Material.PLAYER_HEAD, "Dragonite Headgear", Rarity.RarityType.Epic, Type.Equipment, SubType.Headgear, 100000, 240, "Range Damage:150", Color.fromRGB(55, 72, 254)},
                {"dragonitechestplate2", Material.LEATHER_CHESTPLATE, "Dragonite Chestplate", Rarity.RarityType.Epic, Type.Equipment, SubType.Chestplate, 100000, 240, "", Color.fromRGB(55, 72, 254)},
                {"dragoniteleggings2", Material.LEATHER_LEGGINGS, "Dragonite Leggings", Rarity.RarityType.Epic, Type.Equipment, SubType.Leggings, 100000, 240, "", Color.fromRGB(55, 72, 254)},
                {"dragoniteboots2", Material.LEATHER_BOOTS, "Dragonite Boots", Rarity.RarityType.Epic, Type.Equipment, SubType.Boots, 100000, 240, "Movement Speed:150", Color.fromRGB(55, 72, 254)},


                {"voidsteelhelmet", Material.PLAYER_HEAD, "Voidsteel Helmet", Rarity.RarityType.Rare, Type.Equipment, SubType.Helmet, 100000, 260, "Melee Damage:160", Color.fromRGB(13,15,16)},
                {"voidsteelheadgear", Material.PLAYER_HEAD, "Voidsteel Headgear", Rarity.RarityType.Rare, Type.Equipment, SubType.Headgear, 100000, 260, "Range Damage:160", Color.fromRGB(13,15,16)},
                {"voidsteelchestplate", Material.LEATHER_CHESTPLATE, "Voidsteel Chestplate", Rarity.RarityType.Rare, Type.Equipment, SubType.Chestplate, 100000, 260, "", Color.fromRGB(13,15,16)},
                {"voidsteelleggings", Material.LEATHER_LEGGINGS, "Voidsteel Leggings", Rarity.RarityType.Rare, Type.Equipment, SubType.Leggings, 100000, 260, "", Color.fromRGB(13,15,16)},
                {"voidsteelboots", Material.LEATHER_BOOTS, "Voidsteel Boots", Rarity.RarityType.Rare, Type.Equipment, SubType.Boots, 100000, 260, "Movement Speed:160", Color.fromRGB(13,15,16)},

                {"voidsteelhelmet2", Material.PLAYER_HEAD, "Voidsteel Helmet", Rarity.RarityType.Epic, Type.Equipment, SubType.Helmet, 100000, 270, "Melee Damage:170", Color.fromRGB(13,15,16)},
                {"voidsteelheadgear2", Material.PLAYER_HEAD, "Voidsteel Headgear", Rarity.RarityType.Epic, Type.Equipment, SubType.Headgear, 100000, 270, "Range Damage:170", Color.fromRGB(13,15,16)},
                {"voidsteelchestplate2", Material.LEATHER_CHESTPLATE, "Voidsteel Chestplate", Rarity.RarityType.Epic, Type.Equipment, SubType.Chestplate, 100000, 270, "", Color.fromRGB(13,15,16)},
                {"voidsteelleggings2", Material.LEATHER_LEGGINGS, "Voidsteel Leggings", Rarity.RarityType.Epic, Type.Equipment, SubType.Leggings, 100000, 270, "", Color.fromRGB(13,15,16)},
                {"voidsteelboots2", Material.LEATHER_BOOTS, "Voidsteel Boots", Rarity.RarityType.Epic, Type.Equipment, SubType.Boots, 100000, 270, "Movement Speed:170", Color.fromRGB(13,15,16)},


                {"etheriumhelmet", Material.PLAYER_HEAD, "Etherium Helmet", Rarity.RarityType.Rare, Type.Equipment, SubType.Helmet, 100000, 290, "Melee Damage:180", Color.fromRGB(1,224,244)},
                {"etheriumheadgear", Material.PLAYER_HEAD, "Etherium Headgear", Rarity.RarityType.Rare, Type.Equipment, SubType.Headgear, 100000, 290, "Range Damage:180", Color.fromRGB(1,224,244)},
                {"etheriumchestplate", Material.LEATHER_CHESTPLATE, "Etherium Chestplate", Rarity.RarityType.Rare, Type.Equipment, SubType.Chestplate, 100000, 290, "", Color.fromRGB(1,224,244)},
                {"etheriumleggings", Material.LEATHER_LEGGINGS, "Etherium Leggings", Rarity.RarityType.Rare, Type.Equipment, SubType.Leggings, 100000, 290, "", Color.fromRGB(1,224,244)},
                {"etheriumboots", Material.LEATHER_BOOTS, "Etherium Boots", Rarity.RarityType.Rare, Type.Equipment, SubType.Boots, 100000, 290, "Movement Speed:180", Color.fromRGB(1,224,244)},

                {"etheriumhelmet2", Material.PLAYER_HEAD, "Etherium Helmet", Rarity.RarityType.Epic, Type.Equipment, SubType.Helmet, 100000, 300, "Melee Damage:190", Color.fromRGB(1,224,244)},
                {"etheriumheadgear2", Material.PLAYER_HEAD, "Etherium Headgear", Rarity.RarityType.Epic, Type.Equipment, SubType.Headgear, 100000, 300, "Range Damage:190", Color.fromRGB(1,224,244)},
                {"etheriumchestplate2", Material.LEATHER_CHESTPLATE, "Etherium Chestplate", Rarity.RarityType.Epic, Type.Equipment, SubType.Chestplate, 100000, 300, "", Color.fromRGB(1,224,244)},
                {"etheriumleggings2", Material.LEATHER_LEGGINGS, "Etherium Leggings", Rarity.RarityType.Epic, Type.Equipment, SubType.Leggings, 100000, 300, "", Color.fromRGB(1,224,244)},
                {"etheriumboots2", Material.LEATHER_BOOTS, "Etherium Boots", Rarity.RarityType.Epic, Type.Equipment, SubType.Boots, 100000, 300, "Movement Speed:190", Color.fromRGB(1,224,244)},





        };



        for (Object[] data : equipmentData) {
            String key = (String) data[0];
            Material material = (Material) data[1];
            String displayName = (String) data[2];
            Rarity.RarityType rarity = (Rarity.RarityType) data[3];
            Type type = (Type) data[4];
            SubType subType = (SubType) data[5];
            int durability = (int) data[6];
            int levelMinimum = (int) data[7];
            String bonusStats = (String) data[8];


            double percent = switch (subType.toString().toLowerCase()) {
                case "helmet" -> 0.3;
                case "headgear" -> 0.3;
                case "chestplate" -> 0.8;
                case "leggings" -> 0.7;
                case "boots" -> 0.45;
                default -> 0.1;
            };

            double health = levelMinimum*(percent*8);
            double armor = levelMinimum*(percent*2);
            double steam = levelMinimum*(percent*0.5);

            if (rarity == Rarity.RarityType.Epic){
                health = levelMinimum*(percent*9);
                armor = levelMinimum*(percent*3);
                steam = levelMinimum*(percent*0.8);
            }

            FactoryItem item = new FactoryItem();
            item.setType(type);
            item.setSubType(subType);
            item.setMaterial(material);
            item.setDisplayname(displayName);
            item.setRarity(rarity);
            item.setDurability(durability);
            item.setMaxDurability(durability);
            item.setHealth(health);
            item.setArmor(armor);
            item.setSteam(steam);
            item.setBonusStats(bonusStats);

            if (material != Material.PLAYER_HEAD){
                Color color = (Color) data[9];
                item.setColor(color);
            }

            item.setLevelMinimum(levelMinimum);

            factoryItemList.put(key, item);
        }

        for (String key : factoryItemList.keySet()) {
            SaveItem(key, factoryItemList.get(key).build().clone());
        }

    }

}
