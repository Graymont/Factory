package org.factory.factory.GameHandler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.factory.factory.Database.GetItem;
import static org.factory.factory.Database.GetLocation;
import static org.factory.factory.GameHandler.FactoryItem.*;
import static org.factory.factory.GameManager.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.GameHandler.PlayerProgress.playerLevel;
import static org.factory.factory.GameHandler.PlayerProgress.playerPrestige;
import static org.factory.factory.Utils.UserInterface.*;

public class Dungeon implements Listener {

    public enum LootType{
        None,
        Weapon,
        Equipment;

        public static LootType parseLoot(String type){
            return switch (type.toLowerCase()){

                case "weapon" -> LootType.Weapon;
                case "equipment" -> LootType.Equipment;

                default -> LootType.None;
            };
        }
    }

    public enum DungeonList{
        None,
        Dungeon_1,
        Dungeon_2,
        Dungeon_3,
        Dungeon_4,
        Dungeon_5,
        Dungeon_6,
        Dungeon_7,
        Dungeon_8,
        Dungeon_9,
        Dungeon_10;

        public static DungeonList parseDungeon(String type){
            return switch (type.toLowerCase()){

                case "dungeon_1" -> DungeonList.Dungeon_1;
                case "dungeon_2" -> DungeonList.Dungeon_2;
                case "dungeon_3" -> DungeonList.Dungeon_3;
                case "dungeon_4" -> DungeonList.Dungeon_4;
                case "dungeon_5" -> DungeonList.Dungeon_5;
                case "dungeon_6" -> DungeonList.Dungeon_6;
                case "dungeon_7" -> DungeonList.Dungeon_7;
                case "dungeon_8" -> DungeonList.Dungeon_8;
                case "dungeon_9" -> DungeonList.Dungeon_9;
                case "dungeon_10" -> DungeonList.Dungeon_10;

                default -> DungeonList.None;
            };
        }

        public static Integer getLevel(String type){
            return switch (type.toLowerCase()){

                case "dungeon_1" -> 20;
                case "dungeon_2" -> 30;
                case "dungeon_3" -> 40;
                case "dungeon_4" -> 50;
                case "dungeon_5" -> 60;
                case "dungeon_6" -> 70;
                case "dungeon_7" -> 80;
                case "dungeon_8" -> 90;
                case "dungeon_9" -> 100;
                case "dungeon_10" -> 110;

                default -> 0;
            };
        }

        public static Integer getPrestige(DungeonList type){
            return switch (type.toString().toLowerCase()){

                default -> Integer.parseInt(numberInText(type.toString()));
            };
        }

    }


    public static void TeleportDungeon(Player player, DungeonList dungeon){
        Location dungeonLocation = GetLocation(dungeon.toString().toLowerCase().replaceAll("_", "").trim());

        if (playerPrestige.get(player.getUniqueId()) < DungeonList.getPrestige(dungeon)){
            player.sendMessage(sendText("&cYou need to reach &6Prestige "+intToRoman(DungeonList.getPrestige(dungeon))+" &cto enter the dungeon!"));
            return;
        }

        if (dungeonLocation == null){
            player.sendMessage(sendText("&cSorry, this dungeon is not available right now..."));
            return;
        }

        if (playerLevel.get(player.getUniqueId()) >= DungeonList.getLevel(dungeon.toString())){
            player.teleport(dungeonLocation);

            SendTitle(player, "&c"+uncolouredText(dungeon.toString()).replaceAll("_", "").trim()+" "+intToRoman(Integer.parseInt(numberInText(dungeon.toString()))), "", 2, 3, 2);

            PotionEffect effect = new PotionEffect(PotionEffectType.DARKNESS, 60, 3, false, false);
            player.addPotionEffect(effect);

            PlaySoundAt(Sound.AMBIENT_CAVE, player.getLocation(), 1, 1);
            PlaySoundAt(Sound.BLOCK_PORTAL_TRIGGER, player.getLocation(), 0.2f, 2);
        }else{
            player.sendMessage(Notification_NoLevel(player));
            PlaySoundAt(Sound.BLOCK_GLASS_BREAK, player.getLocation(), 1, 1);
        }
    }



    public static ItemStack GetDungeonLoot(LootType type, int level){
        Random random = new Random();


        List<FactoryItem.SubType> itemSubTypeList = new ArrayList<>();

        FactoryItem.Type itemType = FactoryItem.Type.parseType(type.toString());

        if (type == LootType.Weapon){
            itemSubTypeList = Arrays.asList(FactoryItem.SubType.Sword, FactoryItem.SubType.Hammer,
                    FactoryItem.SubType.Gun, FactoryItem.SubType.Blast, FactoryItem.SubType.Bow);
        }
        else if (type == LootType.Equipment){
            itemSubTypeList = Arrays.asList(FactoryItem.SubType.Helmet, FactoryItem.SubType.Chestplate,
                    FactoryItem.SubType.Leggings, FactoryItem.SubType.Boots);
        }
        int randomSubType = random.nextInt(itemSubTypeList.size());
        FactoryItem.SubType itemSubType = itemSubTypeList.get(randomSubType);

        int randomRarity = random.nextInt(100)+1;

        Rarity.RarityType itemRarity;
        Material itemMaterial = Material.STICK;
        if (randomRarity <= 1){
            itemRarity = Rarity.RarityType.Immortal;
            if (itemSubType == FactoryItem.SubType.Sword){
                itemMaterial = Material.NETHERITE_SWORD;
            }
            else if (itemSubType == FactoryItem.SubType.Gun || itemSubType == FactoryItem.SubType.Blast){
                itemMaterial = Material.DIAMOND_HORSE_ARMOR;
            }
            else if (itemSubType == FactoryItem.SubType.Helmet){
                itemMaterial = Material.NETHERITE_HELMET;
            }
            else if (itemSubType == FactoryItem.SubType.Chestplate){
                itemMaterial = Material.NETHERITE_CHESTPLATE;
            }
            else if (itemSubType == FactoryItem.SubType.Leggings){
                itemMaterial = Material.NETHERITE_LEGGINGS;
            }
            else if (itemSubType == FactoryItem.SubType.Boots){
                itemMaterial = Material.NETHERITE_BOOTS;
            }
        }
        else if (randomRarity <= 3){
            itemRarity = Rarity.RarityType.Legendary;
            if (itemSubType == FactoryItem.SubType.Sword){
                itemMaterial = Material.DIAMOND_SWORD;
            }
            else if (itemSubType == FactoryItem.SubType.Gun || itemSubType == FactoryItem.SubType.Blast){
                itemMaterial = Material.DIAMOND_HORSE_ARMOR;
            }
            else if (itemSubType == FactoryItem.SubType.Helmet){
                itemMaterial = Material.DIAMOND_HELMET;
            }
            else if (itemSubType == FactoryItem.SubType.Chestplate){
                itemMaterial = Material.DIAMOND_CHESTPLATE;
            }
            else if (itemSubType == FactoryItem.SubType.Leggings){
                itemMaterial = Material.DIAMOND_LEGGINGS;
            }
            else if (itemSubType == FactoryItem.SubType.Boots){
                itemMaterial = Material.DIAMOND_BOOTS;
            }
        }
        else if (randomRarity <= 10){
            itemRarity = Rarity.RarityType.Epic;
            if (itemSubType == FactoryItem.SubType.Sword){
                itemMaterial = Material.GOLDEN_SWORD;
            }
            else if (itemSubType == FactoryItem.SubType.Gun || itemSubType == FactoryItem.SubType.Blast){
                itemMaterial = Material.GOLDEN_HORSE_ARMOR;
            }
            else if (itemSubType == FactoryItem.SubType.Helmet){
                itemMaterial = Material.GOLDEN_HELMET;
            }
            else if (itemSubType == FactoryItem.SubType.Chestplate){
                itemMaterial = Material.GOLDEN_CHESTPLATE;
            }
            else if (itemSubType == FactoryItem.SubType.Leggings){
                itemMaterial = Material.GOLDEN_LEGGINGS;
            }
            else if (itemSubType == FactoryItem.SubType.Boots){
                itemMaterial = Material.GOLDEN_BOOTS;
            }
        }
        else if (randomRarity <= 25){
            itemRarity = Rarity.RarityType.Rare;
            if (itemSubType == FactoryItem.SubType.Sword){
                itemMaterial = Material.IRON_SWORD;
            }
            else if (itemSubType == FactoryItem.SubType.Gun || itemSubType == FactoryItem.SubType.Blast){
                itemMaterial = Material.IRON_HORSE_ARMOR;
            }
            else if (itemSubType == FactoryItem.SubType.Helmet){
                itemMaterial = Material.IRON_HELMET;
            }
            else if (itemSubType == FactoryItem.SubType.Chestplate){
                itemMaterial = Material.IRON_CHESTPLATE;
            }
            else if (itemSubType == FactoryItem.SubType.Leggings){
                itemMaterial = Material.IRON_LEGGINGS;
            }
            else if (itemSubType == FactoryItem.SubType.Boots){
                itemMaterial = Material.IRON_BOOTS;
            }
        }
        else if (randomRarity <= 65){
            itemRarity = Rarity.RarityType.Uncommon;
            if (itemSubType == FactoryItem.SubType.Sword){
                itemMaterial = Material.STONE_SWORD;
            }
            else if (itemSubType == FactoryItem.SubType.Gun || itemSubType == FactoryItem.SubType.Blast){
                itemMaterial = Material.LEATHER_HORSE_ARMOR;
            }
            else if (itemSubType == FactoryItem.SubType.Helmet){
                itemMaterial = Material.CHAINMAIL_HELMET;
            }
            else if (itemSubType == FactoryItem.SubType.Chestplate){
                itemMaterial = Material.CHAINMAIL_CHESTPLATE;
            }
            else if (itemSubType == FactoryItem.SubType.Leggings){
                itemMaterial = Material.CHAINMAIL_LEGGINGS;
            }
            else if (itemSubType == FactoryItem.SubType.Boots){
                itemMaterial = Material.CHAINMAIL_BOOTS;
            }
        }else{
            itemRarity = Rarity.RarityType.Common;
            if (itemSubType == FactoryItem.SubType.Sword){
                itemMaterial = Material.WOODEN_SWORD;
            }
            else if (itemSubType == FactoryItem.SubType.Gun || itemSubType == FactoryItem.SubType.Blast){
                itemMaterial = Material.LEATHER_HORSE_ARMOR;
            }
            else if (itemSubType == FactoryItem.SubType.Helmet){
                itemMaterial = Material.LEATHER_HELMET;
            }
            else if (itemSubType == FactoryItem.SubType.Chestplate){
                itemMaterial = Material.LEATHER_CHESTPLATE;
            }
            else if (itemSubType == FactoryItem.SubType.Leggings){
                itemMaterial = Material.LEATHER_LEGGINGS;
            }
            else if (itemSubType == FactoryItem.SubType.Boots){
                itemMaterial = Material.LEATHER_BOOTS;
            }
        }


        if (itemSubType == FactoryItem.SubType.Hammer){
            itemMaterial = Material.MACE;
        }
        else if (itemSubType == FactoryItem.SubType.Bow){
            itemMaterial = Material.BOW;
        }



        return new ItemStack(GenerateItem(itemType, itemSubType, itemMaterial, level, itemRarity));
    }


    public static List<ItemStack> GetEquipmentLoot(int tier){
        List<ItemStack> storedLoot = new ArrayList<>();
        List<String> mineralList = Arrays.asList("tungsten", "palladium", "cobalt", "mithril", "orichalcum", "titanium",
                "adamantine", "dragonite", "voidsteel", "etherium");
        List<String> materialList = Arrays.asList("helmet", "headgear", "chestplate", "leggings", "boots");
        for (String material : materialList){
            storedLoot.add(new ItemStack(GetItem(mineralList.get(tier-1)+material)));
        }
        return storedLoot;
    }

    public static List<ItemStack> GetWeaponLoot(int tier){
        List<ItemStack> storedLoot = new ArrayList<>();



        return storedLoot;
    }

    @EventHandler
    public void OnLootOpen(PlayerInteractEvent event){
        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR){
            ItemMeta meta = item.getItemMeta();

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey(dungeonLootBoxKey))){
                Integer tier = container.get(GetNamespacedKey(dungeonLootBoxTierKey), PersistentDataType.INTEGER);
                if (tier == null){
                    tier = 1;
                }
                String lootType = container.get(GetNamespacedKey(lootTypeKey), PersistentDataType.STRING);
                if (lootType == null){
                    lootType = "weapon";
                }

                LootType parsedType = LootType.parseLoot(lootType);
                if (event.getAction().isRightClick()){
                    player.getInventory().getItemInMainHand().setAmount(item.getAmount()-1);
                    List<ItemStack> storedLootList =  new ArrayList<>();
                    if (parsedType == LootType.Equipment){
                        storedLootList = GetEquipmentLoot(tier);
                        Random random = new Random();
                        int randomGet = random.nextInt(100)+1;
                        if (randomGet <= 20){
                            player.getInventory().addItem(storedLootList.get(0));
                        }
                        else if (randomGet <= 25){

                        }
                    }


                    SendTitle(player, "&bDungeon Loot Opened!", "", 1, 3, 1);
                    PlaySoundAt(Sound.BLOCK_NOTE_BLOCK_GUITAR, player.getLocation(), 1, 2);
                }

                else if (event.getAction().isLeftClick()){
                    Inventory inventory = OpenGUI(player, 1, "Loot View");
                    List<ItemStack> storedLootList =  new ArrayList<>();
                    if (parsedType == LootType.Equipment){
                        storedLootList = GetEquipmentLoot(tier);
                    }
                    for (int i = 0; i < storedLootList.size(); i++) {
                        inventory.setItem(i, storedLootList.get(i));
                    }

                    player.openInventory(inventory);
                    PlaySoundAt(Sound.BLOCK_CHEST_OPEN, player.getLocation(), 1, 1);
                }
            }

        }
    }

    public static ItemStack getDungeonPendant(int tier){
        ItemStack item = new ItemStack(Material.FIRE_CHARGE);
        ItemMeta meta = item.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(GetNamespacedKey(itemKey), PersistentDataType.BOOLEAN, true);

        meta.setDisplayName(sendText("&fDungeon Pendant &7Tier &l"+intToRoman(tier)));

        List<String> itemLore = new ArrayList<>();

        itemLore.add(sendText("&9Miscellaneous"));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7Obtained by exchanging a membrane"));
        itemLore.add(sendText(" &7(Alien, Mutant, or Undead Membrane)"));
        itemLore.add(sendText(" &7Trade this item at &f&nTraders"));

        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }




}
