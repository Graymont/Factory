package org.factory.factory.Utils;

import it.unimi.dsi.fastutil.Hash;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static org.factory.factory.Database.*;

import static org.factory.factory.Events.*;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.Utils.Booster.getFormattedBoosterName;
import static org.factory.factory.Utils.CooldownManager.getFormattedRemainingTime;
import static org.factory.factory.Utils.CooldownManager.hasCooldown;
import static org.factory.factory.Utils.Dungeon.TeleportDungeon;
import static org.factory.factory.Utils.FactoryItem.*;
import static org.factory.factory.Utils.FactoryMachine.*;
import static org.factory.factory.Utils.FactoryQuest.*;
import static org.factory.factory.Utils.ItemSerializer.ItemStackFromBase64;
import static org.factory.factory.Utils.ItemSerializer.loadSerializedItem;
import static org.factory.factory.Utils.MultiBlock.OpenCarbonForge;
import static org.factory.factory.Utils.MultiBlock.carbonMaterials;
import static org.factory.factory.Utils.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.PlayerProgress.*;
import static org.factory.factory.Utils.QuestManager.questCount;
import static org.factory.factory.Utils.RewardsManager.ClaimRewards;
import static org.factory.factory.Utils.SQLiteDatabase.parseLocationString;
import static org.factory.factory.Utils.UserInterface.*;
import static org.factory.factory.Utils.VaultEconomy.*;

public class GUIManager implements Listener {

    public static HashMap<Player, Integer> playerInput_Integer = new HashMap<>();
    public static HashMap<Player, Boolean> isInput = new HashMap<>();

    public enum MenuList{
        None,
        MachineEngine,
        Shop,
        ShopPage,
        Anvil,
        EnchantingTable,
        Catalog,
        MultiBlock,
        GameMenu,
        WarpMenu,
        Profile,
        AcidMaker,
        Rewards,
        Quest,
        Sell,
        Blacksmith,
        Carbon_Forge,
        Trash,
        Prestige,
        Dungeon,
        Farms;

        public static MenuList parseMenu(String m){
            return switch (m.toLowerCase()) {
                case "machineengine" -> MenuList.MachineEngine;
                case "shop" -> MenuList.Shop;
                case "shoppage" -> MenuList.ShopPage;
                case "anvil" -> MenuList.Anvil;
                case "enchantingtable" -> MenuList.EnchantingTable;
                case "catalog" -> MenuList.Catalog;
                case "multiblock" -> MenuList.MultiBlock;
                case "gamemenu" -> MenuList.GameMenu;
                case "warpmenu" -> MenuList.WarpMenu;
                case "profile" -> MenuList.Profile;
                case "acidmaker" -> MenuList.AcidMaker;
                case "rewards" -> MenuList.Rewards;
                case "quest" -> MenuList.Quest;
                case "sell" -> MenuList.Sell;
                case "blacksmith" -> MenuList.Blacksmith;
                case "carbon_forge" -> MenuList.Carbon_Forge;
                case "trash" -> MenuList.Trash;
                case "prestige" -> MenuList.Prestige;
                case "dungeon" -> MenuList.Dungeon;
                case "farms" -> MenuList.Farms;

                default -> MenuList.None;
            };
        }
    }

    public static void OpenMenu(Player player, MenuList menu){
        if (player.getVehicle() != null){
            return;
        }
        menuPage.put(player, 1);
        openedMenu.put(player, menu);

        if (menu.equals(MenuList.Shop)){
            Inventory inventory = OpenGUI(player, 6, "Shop");
            for (String key : categoryList.keySet()){
                String rawKey = key
                        .replaceAll(".slot", "").trim()
                        .replaceAll(".material", "").trim()
                        .replaceAll(".name", "").trim();

                ItemStack item = new ItemStack(Material.getMaterial(categoryList.get(rawKey+".material")));
                ItemMeta meta = item.getItemMeta();

                String categoryKey = rawKey+"_items";
                PersistentDataContainer container = meta.getPersistentDataContainer();
                container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, "category");
                container.set(GetNamespacedKey("category"), PersistentDataType.STRING, categoryKey);

                meta.setDisplayName(sendText(categoryList.get(rawKey+".name")));
                int slot = Integer.parseInt(categoryList.get(rawKey+".slot"));

                item.setItemMeta(meta);

                inventory.setItem(slot, item);
                buyAmount.put(player, 1);
            }
            SetHeaderFooter(inventory);
            inventory.setItem(49, getBasicUi("catalog"));
            player.openInventory(inventory);

            SetBackground(inventory, Material.BLACK_STAINED_GLASS_PANE);

            //PlaySound(Sound.BLOCK_ENDER_CHEST_OPEN, player, 1, 1);

        }

        else if (menu.equals(MenuList.Anvil)){
            Inventory inventory = OpenGUI(player, 3, "Anvil");

            SetHeaderFooter(inventory);
            inventory.setItem(13, getBasicUi("anvil"));
            SetBackground(inventory, Material.GRAY_STAINED_GLASS_PANE);

            player.openInventory(inventory);
            PlaySoundAt(Sound.BLOCK_ANVIL_PLACE, player.getLocation(), 1, 1);
        }

        else if (menu.equals(MenuList.Farms)){
            Inventory inventory = OpenGUI(player, 4, "Farms");

            SetHeaderFooter(inventory);
            inventory.setItem(10, getBasicUi("wheatfarm"));
            inventory.setItem(12, getBasicUi("beetrootfarm"));
            inventory.setItem(14, getBasicUi("carrotfarm"));
            inventory.setItem(16, getBasicUi("potatofarm"));
            inventory.setItem(22, getBasicUi("netherwartfarm"));
            SetBackground(inventory, Material.GRAY_STAINED_GLASS_PANE);

            player.openInventory(inventory);
            //PlaySoundAt(Sound.BLOCK_ANVIL_PLACE, player.getLocation(), 1, 1);
        }

        else if (menu.equals(MenuList.Dungeon)){
            Inventory inventory = OpenGUI(player, 6, "Dungeon");

            SetHeaderFooter(inventory);

            int index = 9;
            for (int i = 1; i < 11; i++) {
                inventory.setItem(index, getDungeonIcon(player, i));
                index++;
            }

            SetBackground(inventory, Material.GRAY_STAINED_GLASS_PANE);

            player.openInventory(inventory);
        }
        else if (menu.equals(MenuList.MultiBlock)){
            Inventory inventory = OpenGUI(player, 6, "Multi Block Guide");
            SetHeaderFooter(inventory);

            inventory.setItem(9, getBasicUi("multiblock_acid_pipe"));
            inventory.setItem(10, getBasicUi("multiblock_acid_maker"));
            inventory.setItem(11, getBasicUi("multiblock_carbon_forge"));
            inventory.setItem(12, getBasicUi("multiblock_armor_crafter"));
            inventory.setItem(13, getBasicUi("multiblock_nether_smelter"));

            SetBackground(inventory, Material.GRAY_STAINED_GLASS_PANE);
            player.openInventory(inventory);
        }
        else if (menu.equals(MenuList.GameMenu)){
            Inventory inventory = OpenGUI(player, 6, "Game Menu");
            SetHeaderFooter(inventory);
            SetBackground(inventory, Material.BLACK_STAINED_GLASS_PANE);

            inventory.setItem(49, getBasicUi("gamemenu_spawn"));
            inventory.setItem(20, getBasicUi("gamemenu_shop"));
            inventory.setItem(21, getBasicUi("gamemenu_quest"));
            inventory.setItem(22, getBasicUi("gamemenu_catalog"));
            inventory.setItem(23, getBasicUi("gamemenu_rewards"));
            inventory.setItem(24, getBasicUi("gamemenu_creditshop"));

            inventory.setItem(30, getBasicUi("gamemenu_crafting"));
            inventory.setItem(32, getBasicUi("gamemenu_multiblock"));
            inventory.setItem(53, getBasicUi("gamemenu_profile"));
            inventory.setItem(52, getBasicUi("gamemenu_prestige"));
            inventory.setItem(4, getBasicUi("gamemenu_island"));
            inventory.setItem(45, getBasicUi("gamemenu_bank"));
            inventory.setItem(46, getBasicUi("gamemenu_trash"));
            inventory.setItem(0, getBasicUi("gamemenu_sellitem"));
            inventory.setItem(8, getBasicUi("gamemenu_auctionhouse"));

            inventory.setItem(31, getBasicUi("gamemenu_warp"));

            // yg belum: warp, trash, machine manager

            player.openInventory(inventory);
        }

        else if (menu.equals(MenuList.Prestige)){
            Inventory inventory = OpenGUI(player, 5, "Prestige");
            SetHeaderFooter(inventory);

            int index = 9;
            for (int i = 0; i < 10; i++) {
                inventory.setItem(index, getPrestigeIcon(player, i));
                index++;
            }

            if (playerPrestige.get(player.getUniqueId()) < maxPrestige){
                inventory.setItem(40, getBasicUi("prestige"));
            }

            SetBackground(inventory, Material.GRAY_STAINED_GLASS_PANE);

            player.openInventory(inventory);
        }

        else if (menu.equals(MenuList.WarpMenu)){
            Inventory inventory = OpenGUI(player, 5, "Warp Menu");

            SetHeaderFooter(inventory);

            inventory.setItem(12, getBasicUi("warp_lake"));
            inventory.setItem(13, getBasicUi("warp_blacksmith"));
            inventory.setItem(14, getBasicUi("warp_garden"));

            inventory.setItem(20, getBasicUi("warp_crates"));
            inventory.setItem(21, getBasicUi("warp_mine"));
            inventory.setItem(22, getBasicUi("warp_graveyard"));
            inventory.setItem(23, getBasicUi("warp_dungeon"));
            inventory.setItem(24, getBasicUi("warp_communitycenter"));
            inventory.setItem(31, getBasicUi("warp_farms"));

            SetBackground(inventory, Material.BLACK_STAINED_GLASS_PANE);

            player.openInventory(inventory);
        }

        else if (menu.equals(MenuList.Profile)){
            Inventory inventory = OpenGUI(player, 4, "Profile");
            SetHeaderFooter(inventory);
            inventory.setItem(13, getProfileIcon(player, "progress"));
            player.openInventory(inventory);
        }

        else if (menu.equals(MenuList.Sell)){
            Inventory inventory = OpenGUI(player, 3, "Sell");
            SetHeaderFooter(inventory);

            inventory.setItem(12, getBasicUi("sellgui"));
            inventory.setItem(14, getBasicUi("sellall"));

            player.openInventory(inventory);
        }

        else if (menu.equals(MenuList.Rewards)){
            Inventory inventory = OpenGUI(player, 3, "Rewards");
            SetHeaderFooter(inventory);

            inventory.setItem(9, getRewardsIcon(player, "hourly"));
            inventory.setItem(11, getRewardsIcon(player, "daily"));
            inventory.setItem(13, getRewardsIcon(player, "weekly"));
            inventory.setItem(15, getRewardsIcon(player, "monthly"));

            inventory.setItem(17, getRewardsIcon(player, "ocd"));

            player.openInventory(inventory);

            int taskId = new BukkitRunnable() {
                @Override
                public void run() {
                    Inventory topInventory = player.getOpenInventory().getTopInventory();
                    for (ItemStack item : topInventory.getContents()){
                        if (item != null){
                            inventory.setItem(9, getRewardsIcon(player, "hourly"));
                            inventory.setItem(11, getRewardsIcon(player, "daily"));
                            inventory.setItem(13, getRewardsIcon(player, "weekly"));
                            inventory.setItem(15, getRewardsIcon(player, "monthly"));

                            inventory.setItem(17, getRewardsIcon(player, "ocd"));
                        }
                    }
                    player.updateInventory();
                }
            }.runTaskTimer(getMainPlugin(), 0L, 20L).getTaskId();

            openedMachine.put(player, taskId);
        }

        else if (menu.equals(MenuList.Blacksmith)){
            OpenBlacksmith(player);
        }

        else if (menu.equals(MenuList.Trash)){
            Inventory inventory = OpenChest(player, 6, "Trash");
            player.openInventory(inventory);
        }

        else if (menu.equals(MenuList.Carbon_Forge)){
            Inventory inventory = OpenGUI(player, 3, "Carbon Forge ["+(playerSelector.get(player)+1)+"/"+carbonMaterials.length+"]");
            SetHeaderFooter(inventory);

            inventory.setItem(13, getCarbonEquipmentIcon(player, "carbon"));
            inventory.setItem(21, getCarbonEquipmentIcon(player, "previous_page"));
            inventory.setItem(23, getCarbonEquipmentIcon(player, "next_page"));
            inventory.setItem(4, getCarbonEquipmentIcon(player, "view"));
            inventory.setItem(0, getCarbonEquipmentIcon(player, "input"));

            player.openInventory(inventory);
        }

        else if (menu.equals(MenuList.Quest)){
            Inventory inventory = OpenGUI(player, 6, "Quest");
            SetHeaderFooter(inventory);

            List<String> questKeyList = Arrays.asList("miner", "hunter", "fisherman");

            for (String questKey : questKeyList){
                int start = 9;
                int end = start+5;
                int index = 1;
                if (questKey.equals("miner")){
                    start = 9;
                    end = start+5;
                    for (int i = start; i < end; i++) {
                        inventory.setItem(i, getQuestIcon(player, questKey+"_"+index));
                        index++;
                    }
                }
                else if (questKey.equals("hunter")){
                    start = 18;
                    end = start+5;
                    for (int i = start; i < end; i++) {
                        inventory.setItem(i, getQuestIcon(player, questKey+"_"+index));
                        index++;
                    }
                }
                else if (questKey.equals("fisherman")){
                    start = 27;
                    end = start+5;
                    for (int i = start; i < end; i++) {
                        inventory.setItem(i, getQuestIcon(player, questKey+"_"+index));
                        index++;
                    }
                }
            }

            inventory.setItem(49, getQuestIcon(player, "complete"));
            inventory.setItem(4, getQuestIcon(player, "abandon"));
            player.openInventory(inventory);

            int taskId = new BukkitRunnable() {
                @Override
                public void run() {
                    for (String questKey : questKeyList){
                        int start = 9;
                        int end = start+5;
                        int index = 1;
                        if (questKey.equals("miner")){
                            start = 9;
                            end = start+5;
                            for (int i = start; i < end; i++) {
                                inventory.setItem(i, getQuestIcon(player, questKey+"_"+index));
                                index++;
                            }
                        }
                        else if (questKey.equals("hunter")){
                            start = 18;
                            end = start+5;
                            for (int i = start; i < end; i++) {
                                inventory.setItem(i, getQuestIcon(player, questKey+"_"+index));
                                index++;
                            }
                        }
                        else if (questKey.equals("fisherman")){
                            start = 27;
                            end = start+5;
                            for (int i = start; i < end; i++) {
                                inventory.setItem(i, getQuestIcon(player, questKey+"_"+index));
                                index++;
                            }
                        }
                        inventory.setItem(49, getQuestIcon(player, "complete"));
                        inventory.setItem(4, getQuestIcon(player, "abandon"));
                    }
                    player.updateInventory();
                }
            }.runTaskTimer(getMainPlugin(), 0L, 20L).getTaskId();

            openedMachine.put(player, taskId);
        }

        player.updateInventory();
    }

    public static HashMap<Player, Integer> playerSelector = new HashMap<>();

    public static ItemStack getCarbonEquipmentIcon(Player player, String name) {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta = SetAditMeta(meta);
        List<String> itemLore = new ArrayList<>();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (name.equals("carbon")){
            item.setType(Material.LEATHER_CHESTPLATE);
            meta.setDisplayName(sendText("&a"+formatItemName(carbonMaterials[playerSelector.get(player)])+" &aEquipments"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7These equipments can increase"));
            itemLore.add(sendText(" &7your max steam"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&aClick to view"));
        }
        else if (name.equals("next_page")){
            item.setType(Material.ARROW);
            meta.setDisplayName(sendText("&aNext Page &6&l→"));
        }
        else if (name.equals("previous_page")){
            item.setType(Material.SPECTRAL_ARROW);
            meta.setDisplayName(sendText("&6&l← &2Previous Page"));
        }
        else if (name.equals("view")){
            item.setType(Material.PAPER);
            meta.setDisplayName(sendText("&bView all Equipments"));
        }
        else if (name.equals("input")){
            item.setType(Material.OAK_SIGN);
            meta.setDisplayName(sendText("&bInput Selection"));
        }

        container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, name);
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;

    }

    public static ItemStack getProfileIcon(Player player, String name){
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        List<String> itemLore = new ArrayList<>();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (name.equals("progress")){
            int level = playerLevel.get(player.getUniqueId());
            int prestige = playerPrestige.get(player.getUniqueId());
            double exp = playerExp.get(player.getUniqueId());
            double maxExp = PlayerProgress.maxExp.get(level);

            int percent = (int) (exp/maxExp*100);

            item.setType(Material.PLAYER_HEAD);
            meta.setDisplayName(sendText("&6Progress of &b"+player.getName()));
            itemLore.add(sendText(""));
            itemLore.add(sendText(" &7Level: &e"+level));
            itemLore.add(sendText(" &7Experience: &d&o"+FormatDouble(exp)));
            itemLore.add(sendText(" &7Prestige: &b"+prestige));
            itemLore.add(sendText(""));
            itemLore.add(sendText(" &8--------------------"));
            itemLore.add(sendText(" &7[Progress]"));
            itemLore.add(sendText(" &b"+FormatDouble(exp)+"/"+FormatDouble(maxExp)+" &8[&9"+FormatDouble(percent)+"%&8]"));
            itemLore.add(sendText(" &8--------------------"));
        }
        container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, name);
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getRewardsIcon(Player player, String name) {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        List<String> itemLore = new ArrayList<>();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (name.equals("hourly") || name.equals("daily") || name.equals("weekly") || name.equals("monthly")){
            String color = "&a";
            if (name.equals("hourly")){
                item.setType(Material.WHITE_SHULKER_BOX);
            }
            else if (name.equals("daily")){
                color = "&2";
                item.setType(Material.LIME_SHULKER_BOX);
            }
            else if (name.equals("weekly")){
                color = "&6";
                item.setType(Material.GREEN_SHULKER_BOX);
            }
            else {
                color = "&4";
                item.setType(Material.YELLOW_SHULKER_BOX);
            }

            RewardsManager.RewardType rewardType = RewardsManager.RewardType.parseReward(name);

            meta.setDisplayName(sendText(color+formatItemName(name)+" Rewards"));
            itemLore.add(sendText(" "));
            int lvMin = RewardsManager.RewardType.getLevel(rewardType);
            if (playerLevel.get(player.getUniqueId()) >= lvMin){
                itemLore.add(sendText(" &7Level Minimum: &a"+lvMin+" &2"+checkSymbol));
            }else{
                itemLore.add(sendText(" &7Level Minimum: &c"+lvMin+" &4"+xSymbol));
            }
            List<ItemStack> rewardsList = RewardsManager.RewardType.getItems(rewardType);
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &8You will get:"));
            itemLore.add(sendText(" &7- &ax"+FormatDouble(RewardsManager.RewardType.getMoney(rewardType))+" &f"+icon));
            itemLore.add(sendText(" &7- &ax"+FormatDouble(RewardsManager.RewardType.getExp(rewardType))+" &fExp"));
            if (!rewardsList.isEmpty()){
                for (ItemStack rewardItem : rewardsList){
                    itemLore.add(sendText(" &7- &ax"+rewardItem.getAmount()+" &f"+uncolouredText(rewardItem.getItemMeta().getDisplayName())));
                }
            }
            itemLore.add(sendText(" "));
            if (!hasCooldown(player, CooldownManager.CooldownType.parseCooldown(rewardType.toString()))){
                itemLore.add(sendText(" &7Status: &aAvailable &2"+checkSymbol));
                itemLore.add(sendText(" "));
                itemLore.add(sendText(color+"Click to claim "+checkSymbol));
            }else{
                item.setType(Material.RED_STAINED_GLASS_PANE);
                itemLore.add(sendText(" &7Status: &cNot Available &4"+xSymbol+" &6("+getFormattedRemainingTime(player,
                        CooldownManager.CooldownType.parseCooldown(rewardType.toString()))+"&6)"));
                itemLore.add(sendText(" "));
                itemLore.add(sendText(color+"You can't claim this rewards right now! "+xSymbol));
            }
            container.set(GetNamespacedKey("rewards"), PersistentDataType.STRING, name);
        }

        else if (name.equals("ocd")){
            item.setType(Material.BLUE_SHULKER_BOX);
            meta.setDisplayName(sendText("&6Anti-Ocd UI"));
        }

        meta = SetAditMeta(meta);
        container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, name);
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getQuestIcon(Player player, String name) {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        List<String> itemLore = new ArrayList<>();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        int questLevel = 1;
        if (!numberInText(name).isEmpty()){
            questLevel = Integer.parseInt(numberInText(name));
        }


        if (name.contains("miner") || name.contains("hunter") || name.contains("fisherman")){
            item.setType(Material.BOOK);

            Quest claimedQuest = Quest.parseQuest(name);
            int levelMin = Quest.getLevel(claimedQuest);

            meta.setDisplayName(sendText("&b"+formatItemName(uncolouredText(name))+" &3"+intToRoman(questLevel)));
            itemLore.add(sendText(" "));
            if (playerLevel.get(player.getUniqueId()) >= Quest.getLevel(claimedQuest)){
                itemLore.add(sendText(" &7Level Minimum: &a"+levelMin+" &2"+checkSymbol));
            }else{
                itemLore.add(sendText(" &7Level Minimum: &c"+levelMin+" &4"+xSymbol));
            }

            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Quest Type: &f"+ formatItemName(FactoryQuest.QuestType.parseType(name).toString())));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &8Tasks:"));
            HashMap<String, Integer> tasks = initQuestRequirements(FactoryQuest.Quest.parseQuest(name));
            for (String key : tasks.keySet()){
                String countKey = player.getUniqueId()+ ":" +key;
                if (quest.get(player.getUniqueId()) != Quest.parseQuest(name)){
                    itemLore.add(sendText("  &7- &e"+key+" &6x"+tasks.get(key)));
                }else{
                    item.setType(Material.WRITABLE_BOOK);
                    itemLore.add(sendText("  &7- &e"+key+" &6"+questCount.get(countKey)+"/"+tasks.get(key)));

                }
            }
            if (quest.get(player.getUniqueId()) == Quest.parseQuest(name)){
                if (isQuestCompleted(player, quest.get(player.getUniqueId()))){
                    item.setType(Material.ENCHANTED_BOOK);
                    itemLore.add(sendText(" "));
                    itemLore.add(sendText(" &2⚠ &a&lTasks Complete!"));
                    itemLore.add(sendText(" &7finish your quest now to claim rewards!"));
                }
            }
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &8Rewards"));
            itemLore.add(sendText(" &7- &ax"+Quest.getExpRewards(claimedQuest)));
            itemLore.add(sendText(" &7- &ax"+Quest.getMoneyRewards(claimedQuest)+" &f"+icon));
            List<ItemStack> itemRewards = Quest.getItemRewards(claimedQuest);
            for (ItemStack itemReward : itemRewards){
                itemLore.add(sendText(" &7- &ax"+itemReward.getAmount()+" &f"+uncolouredText(itemReward.getItemMeta().getDisplayName())));
            }
            itemLore.add(sendText(" "));
            if (!hasCooldown(player, CooldownManager.CooldownType.parseCooldown(name))){
                itemLore.add(sendText(" &7Status: &aAvailable &2"+checkSymbol));
                itemLore.add(sendText(" "));
                itemLore.add(sendText("&aClick to claim"));
            }else{
                item.setType(Material.RED_STAINED_GLASS_PANE);
                itemLore.add(sendText(" &7Status: &cNot Available &4"+xSymbol+" &6("+getFormattedRemainingTime(player,
                        CooldownManager.CooldownType.parseCooldown(name))+")"));
                itemLore.add(sendText(" "));
                itemLore.add(sendText("&cYou can't claim this quest right now! &4"+xSymbol));
            }
            container.set(GetNamespacedKey("quest"), PersistentDataType.STRING, name);
        }

        else if (name.equals("complete")){
            item.setType(Material.PAPER);
            meta.setDisplayName(sendText("&aFinish Quest"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7If you already completed all tasks"));
            itemLore.add(sendText(" &7you can claim your rewards"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&aClick to finish"));
        }

        else if (name.equals("abandon")){
            item.setType(Material.BUCKET);
            meta.setDisplayName(sendText("&cAbandon Quest"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Abandon current quest will"));
            itemLore.add(sendText(" &7reset all progress &f(no undo)"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&cClick to abandon"));
        }

        meta = SetAditMeta(meta);
        container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, name);
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getPrestigeIcon(Player player, int index) {
        ItemStack item = new ItemStack(Material.EMERALD_ORE);
        ItemMeta meta = item.getItemMeta();
        List<String> itemLore = new ArrayList<>();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        int level = playerLevel.get(player.getUniqueId());
        int current = playerPrestige.get(player.getUniqueId());
        int req = prestigeRequirement.get(index);

        double moneyReq = req*5000;

        meta.setDisplayName(sendText("&bPrestige &8➛ &3"+intToRoman((index+1))));

        itemLore.add(sendText(" "));
        if (level < req){
            item.setType(Material.REDSTONE_BLOCK);
            itemLore.add(sendText(" &7Level Cost: &c"+req+" &4"+xSymbol));
        }else{
            itemLore.add(sendText(" &7Level Cost: &a"+req+" &2"+checkSymbol));
            //itemLore.add(sendText(" &7Money Cost: &f"+moneyReq+icon));
        }
        if (GetPlayerBalance(player) < moneyReq){
            itemLore.add(sendText(" &7Money Cost: &c"+FormatDouble(moneyReq)+icon+" &4"+xSymbol));
        }else{
            itemLore.add(sendText(" &7Money Cost: &a"+FormatDouble(moneyReq)+icon+" &2"+checkSymbol));
        }

        if (current > index){
            item.setType(Material.EMERALD_BLOCK);
        }

        itemLore.add(sendText(" "));

        meta = SetAditMeta(meta);
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getDungeonIcon(Player player, int tier) {
        ItemStack item = new ItemStack(Material.MOSSY_STONE_BRICKS);
        ItemMeta meta = item.getItemMeta();
        List<String> itemLore = new ArrayList<>();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        Dungeon.DungeonList dungeonList = Dungeon.DungeonList.parseDungeon("dungeon_"+tier);

        int currentLevel = playerLevel.get(player.getUniqueId());
        int currentPrestige = playerPrestige.get(player.getUniqueId());

        int levelReq = Dungeon.DungeonList.getLevel("dungeon_"+tier);

        meta.setDisplayName(sendText("&cTier &4&l"+intToRoman(tier)));

        itemLore.add(sendText(" "));

        if (currentLevel < levelReq){
            itemLore.add(sendText(" &7Level Minimum: &a"+ levelReq +" &2"+checkSymbol));
        }else{
            itemLore.add(sendText(" &7Level Minimum: &c"+ levelReq +" &4"+xSymbol));
        }

        if (currentPrestige < 20){
            itemLore.add(sendText(" &7Prestige Minimum: &a"+intToRoman(Dungeon.DungeonList.getPrestige(dungeonList))+" &2"+checkSymbol));
        }else{
            itemLore.add(sendText(" &7Prestige Minimum: &c"+intToRoman(Dungeon.DungeonList.getPrestige(dungeonList))+" &4"+xSymbol));
        }

        itemLore.add(sendText(" "));

        itemLore.add(sendText("&cClick to teleport"));


        container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, "dungeon");
        container.set(GetNamespacedKey("dungeon"), PersistentDataType.STRING, "dungeon_"+tier);

        meta = SetAditMeta(meta);
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }


    public static ItemStack getBasicUi(String name){
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        List<String> itemLore = new ArrayList<>();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (name.equals("anvil")){
            item.setType(Material.ANVIL);
            meta.setDisplayName(sendText("&bAnvil"));

            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &d&lHow to repair?"));
            itemLore.add(sendText(" &7Click on the item you wanted to fix"));
            itemLore.add(sendText(" &7inside your inventory"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Repair will cost &a8% from durability"+icon));
            itemLore.add(sendText(" "));
        }
        else if (name.equals("prestige")){
            item.setType(Material.PAPER);
            meta.setDisplayName(sendText("&bConfirm"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &c&lWarnings!"));
            itemLore.add(sendText("  &7Once you confirm, your required levels"));
            itemLore.add(sendText("  &7will be consumed"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&bClick to confirm"));
        }

        else if (name.equals("sellall")){
            item.setType(Material.EMERALD);
            meta.setDisplayName(sendText("&a&lSell All"));
        }
        else if (name.equals("sellgui")){
            item.setType(Material.DIAMOND);
            meta.setDisplayName(sendText("&a&lSell Gui"));
        }

        else if (name.equals("back")){
            item.setType(Material.BOOK);
            meta.setDisplayName(sendText("&cReturn to Main Page"));
        }
        else if (name.equals("catalog")){
            item.setType(Material.KNOWLEDGE_BOOK);
            meta.setDisplayName(sendText("&a&lCatalog"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7View all available items that can"));
            itemLore.add(sendText(" &7be sold using &f/sellall, /sellgui"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&aClick to view"));
        }

        // multiblock

        else if (name.equals("multiblock_acid_pipe")){
            item.setType(Material.MOSSY_COBBLESTONE_WALL);
            meta.setDisplayName(sendText("&bAcid Pipe"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Make your machine produce acid"));
            itemLore.add(sendText(" &7used for upgrading/evolving machines!"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&bClick to view"));
        }
        else if (name.equals("multiblock_nether_smelter")){
            item.setType(Material.BLAST_FURNACE);
            meta.setDisplayName(sendText("&bNether Smelter"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Smelt Powerful ingots to craft"));
            itemLore.add(sendText(" &7powerful tools at blacksmith!"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&bClick to view"));
        }
        else if (name.equals("multiblock_pipe")){
            item.setType(Material.MOSSY_COBBLESTONE_WALL);
            meta.setDisplayName(sendText("&fMossy Cobblestone Wall &3(all direction)"));
        }

        else if (name.equals("multiblock_acid_maker")){
            item.setType(Material.BREWING_STAND);
            meta.setDisplayName(sendText("&bAcid Maker"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Create Basic Acid using Water Bottle"));
            itemLore.add(sendText(" &7used for upgrading machines!"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&bClick to view"));
        }
        else if (name.equals("acidmaker_acid")){
            item.setType(Material.GLASS_BOTTLE);
            meta.setDisplayName(sendText("&fBasic Acid"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &9Ingredients:"));
            itemLore.add(sendText(" &7- x1 Water Bottle"));
            itemLore.add(sendText(" &7- x1 Sugar"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&aClick to craft"));

        }
        else if (name.equals("multiblock_carbon_forge")){
            item.setType(Material.FURNACE);
            meta.setDisplayName(sendText("&bCarbon Forge"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Craft carbon tech equipment"));
            itemLore.add(sendText(" &7to increase your max steam!"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&bClick to view"));
        }
        else if (name.equals("multiblock_armor_crafter")){
            item.setType(Material.DISPENSER);
            meta.setDisplayName(sendText("&bArmor Crafter"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Craft armor for extra defense"));
            itemLore.add(sendText(" &7againts enemy!"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&bClick to view"));
        }

        // gameMenu
        else if (name.equals("gamemenu_spawn")) {
            item.setType(Material.BEACON);
            meta.setDisplayName(sendText("&bReturn to Spawn"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Safezone wich you can explore"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&bClick to teleport"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "spawn");
        }

        else if (name.equals("gamemenu_prestige")) {
            item.setType(Material.DIAMOND);
            meta.setDisplayName(sendText("&bPrestige"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Sacrifice levels to get more"));
            itemLore.add(sendText(" &7experiences!"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&bClick to view"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "prestige");
        }
        else if (name.equals("gamemenu_sellitem")) {
            item.setType(Material.EMERALD);
            meta.setDisplayName(sendText("&aSell Item"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Sell your available items that listed"));
            itemLore.add(sendText(" &7in a catalogue menu"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&aClick to view"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "sell");
        }
        else if (name.equals("gamemenu_shop")) {
            item.setType(Material.CHEST);
            meta.setDisplayName(sendText("&aShop"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Buy items using your money"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&aClick to view"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "shop");
        }
        else if (name.equals("gamemenu_catalog")) {
            item.setType(Material.KNOWLEDGE_BOOK);
            meta.setDisplayName(sendText("&aCatalog"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7View all available items that can"));
            itemLore.add(sendText(" &7be sold for money"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&aClick to view"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "catalog");
        }
        else if (name.equals("gamemenu_multiblock")) {
            item.setType(Material.BOOK);
            meta.setDisplayName(sendText("&dMulti Block"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Obtain multi block guide book"));
            itemLore.add(sendText(" &7to guide you build a multi block"));
            itemLore.add(sendText(" &7with a specific combinations"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&dClick to view"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "multiblock");
        }
        else if (name.equals("gamemenu_auctionhouse")) {
            item.setType(Material.SPRUCE_SIGN);
            meta.setDisplayName(sendText("&aAuction House"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Open Auction House to"));
            itemLore.add(sendText(" &7buy/sell items from other"));
            itemLore.add(sendText(" &7players"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&aClick to view"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "ah");
        }
        else if (name.equals("gamemenu_creditshop")) {
            item.setType(Material.CHEST);
            meta.setDisplayName(sendText("&aCredit Shop"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Buy unique items using your credits"));
            itemLore.add(sendText(" &7that can be bought from store"));
            itemLore.add(sendText(" &e   &estore.minegens.id"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&aClick to view"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "credit shop");
        }
        else if (name.equals("gamemenu_crafting")) {
            item.setType(Material.CRAFTING_TABLE);
            meta.setDisplayName(sendText("&dCrafting"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Craft any items using crafting table"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&dClick to use"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "craft");
        }
        else if (name.equals("gamemenu_island")) {
            item.setType(Material.STONE_BRICKS);
            meta.setDisplayName(sendText("&bReturn to Factory"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Return to your Factory"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&bClick to use"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "island");
        }
        else if (name.equals("gamemenu_bank")) {
            item.setType(Material.PAPER);
            meta.setDisplayName(sendText("&dBank"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Store your money at bank"));
            itemLore.add(sendText(" &7recommended to prevent losing"));
            itemLore.add(sendText(" &7money when dying"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&dClick to use"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "bank");
        }
        else if (name.equals("gamemenu_trash")) {
            item.setType(Material.COBWEB);
            meta.setDisplayName(sendText("&6Trash"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Delete your unused items"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &c&lWarnings!"));
            itemLore.add(sendText(" &7(Once the items are deleted, they cannot be retrieved)"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&6Click to use"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "trash");
        }
        else if (name.equals("gamemenu_sell")) {
            item.setType(Material.PAPER);
            meta.setDisplayName(sendText("&dSell Item"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Open Sell Item section"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&dClick to view"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "sell");
        }
        else if (name.equals("gamemenu_warp")) {
            item.setType(Material.ENDER_PEARL);
            meta.setDisplayName(sendText("&dWarp"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Fast Travel to a specific places"));
            itemLore.add(sendText(" &7at spawn"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&dClick to view"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "warp");
        }
        else if (name.equals("gamemenu_profile")) {
            item.setType(Material.PLAYER_HEAD);
            meta.setDisplayName(sendText("&dProfile"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7View your profile details"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&dClick to view"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "profile");
        }
        else if (name.equals("gamemenu_quest")) {
            item.setType(Material.WRITABLE_BOOK);
            meta.setDisplayName(sendText("&aQuest"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Finish quest to get rewards"));
            itemLore.add(sendText(" &7and quest pendant"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&aClick to view"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "quest");
        }
        else if (name.equals("gamemenu_rewards")) {
            item.setType(Material.WHITE_SHULKER_BOX);
            meta.setDisplayName(sendText("&aRewards"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Claim free items from rewards"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&aClick to view"));
            container.set(GetNamespacedKey("command"), PersistentDataType.STRING, "rewards");
        }

        else if (name.equals("warp_crates")) {
            item.setType(Material.ENDER_CHEST);
            meta.setDisplayName(sendText("&3Warp: &b&lCrates"));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&bClick to teleport"));
            container.set(GetNamespacedKey("warpName"), PersistentDataType.STRING, "crates");
        }
        else if (name.equals("warp_mine")) {
            item.setType(Material.IRON_PICKAXE);
            meta.setDisplayName(sendText("&3Warp: &b&lMine"));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&bClick to teleport"));
            container.set(GetNamespacedKey("warpName"), PersistentDataType.STRING, "mine");
        }

        else if (name.equals("warp_dungeon")) {
            item.setType(Material.IRON_BARS);
            meta.setDisplayName(sendText("&3Warp: &b&lDungeon"));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&bClick to teleport"));
            container.set(GetNamespacedKey("warpName"), PersistentDataType.STRING, "dungeon");
        }

        else if (name.equals("warp_communitycenter")) {
            item.setType(Material.BEACON);
            meta.setDisplayName(sendText("&3Warp: &b&lCommunity Center"));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&bClick to teleport"));
            container.set(GetNamespacedKey("warpName"), PersistentDataType.STRING, "community_center");
        }


        else if (name.equals("warp_blacksmith")) {
            item.setType(Material.ANVIL);
            meta.setDisplayName(sendText("&3Warp: &b&lBlacksmith"));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&bClick to teleport"));
            container.set(GetNamespacedKey("warpName"), PersistentDataType.STRING, "blacksmith");
        }
        else if (name.equals("warp_lake")) {
            item.setType(Material.FISHING_ROD);
            meta.setDisplayName(sendText("&3Warp: &b&lLake"));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&bClick to teleport"));
            container.set(GetNamespacedKey("warpName"), PersistentDataType.STRING, "lake");
        }
        else if (name.equals("warp_garden")) {
            item.setType(Material.LEATHER);
            meta.setDisplayName(sendText("&3Warp: &b&lGarden"));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&bClick to teleport"));
            container.set(GetNamespacedKey("warpName"), PersistentDataType.STRING, "garden");
        }
        else if (name.equals("warp_graveyard")) {
            item.setType(Material.ZOMBIE_HEAD);
            meta.setDisplayName(sendText("&3Warp: &b&lGraveyard"));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&bClick to teleport"));
            container.set(GetNamespacedKey("warpName"), PersistentDataType.STRING, "graveyard");
        }
        else if (name.equals("warp_farms")) {
            item.setType(Material.IRON_HOE);
            meta.setDisplayName(sendText("&3Warp: &b&lFarms"));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&bClick to teleport"));
            container.set(GetNamespacedKey("warpName"), PersistentDataType.STRING, "farms");
        }

        else if (name.equals("multiblock_machine")){
            item.setType(Material.HAY_BLOCK);
            meta.setDisplayName(sendText("&fMachine &3(any type)"));
        }

        else if (name.equals("multiblock_fire")){
            item.setType(Material.NETHERRACK);
            meta.setDisplayName(sendText("&fFire"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Ignite fire using flint and steel"));
            itemLore.add(sendText(" &7and netherrack"));
            itemLore.add(sendText(" "));
        }

        else if (name.equals("wheatfarm")) {
            item.setType(Material.WHEAT);
            meta.setDisplayName(sendText("&b&l"+formatItemName(name.replace("farm", ""))+" Farm"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&8Specification:"));
            itemLore.add(sendText(" &7Wheat Sell Value: &f"+GetWorth("wheat")+icon));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&bClick to teleport"));
            container.set(GetNamespacedKey("farmName"), PersistentDataType.STRING, name);
            container.set(GetNamespacedKey("farmPrestige"), PersistentDataType.INTEGER, 0);
        }
        else if (name.equals("beetrootfarm")) {
            item.setType(Material.BEETROOT);
            meta.setDisplayName(sendText("&b&l"+formatItemName(name.replace("farm", ""))+" Farm"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&8Specification:"));
            itemLore.add(sendText(" &7Beetroot Sell Value: &f"+GetWorth("beetroot")+icon));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&8Requirements:"));
            itemLore.add(sendText(" &7Prestige Minimum: &72"));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&bClick to teleport"));
            container.set(GetNamespacedKey("farmName"), PersistentDataType.STRING, name);
            container.set(GetNamespacedKey("farmPrestige"), PersistentDataType.INTEGER, 2);
        }
        else if (name.equals("carrotfarm")) {
            item.setType(Material.CARROT);
            meta.setDisplayName(sendText("&b&l"+formatItemName(name.replace("farm", ""))+" Farm"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&8Specification:"));
            itemLore.add(sendText(" &7Carrot Sell Value: &f"+GetWorth("carrot")+icon));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&8Requirements:"));
            itemLore.add(sendText(" &7Prestige Minimum: &74"));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&bClick to teleport"));
            container.set(GetNamespacedKey("farmName"), PersistentDataType.STRING, name);
            container.set(GetNamespacedKey("farmPrestige"), PersistentDataType.INTEGER, 4);
        }
        else if (name.equals("potatofarm")) {
            item.setType(Material.POTATO);
            meta.setDisplayName(sendText("&b&l"+formatItemName(name.replace("farm", ""))+" Farm"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&8Specification:"));
            itemLore.add(sendText(" &7Potato Sell Value: &f"+GetWorth("potato")+icon));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&8Requirements:"));
            itemLore.add(sendText(" &7Prestige Minimum: &76"));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&bClick to teleport"));
            container.set(GetNamespacedKey("farmName"), PersistentDataType.STRING, name);
            container.set(GetNamespacedKey("farmPrestige"), PersistentDataType.INTEGER, 6);
        }
        else if (name.equals("netherwartfarm")) {
            item.setType(Material.NETHER_WART);
            meta.setDisplayName(sendText("&b&l"+formatItemName(name.replace("farm", "").replace("wart", ""))+" Wart Farm"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&8Specification:"));
            itemLore.add(sendText(" &7Nether Wart Sell Value: &f"+GetWorth("netherwart")+icon));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&8Requirements:"));
            itemLore.add(sendText(" &7Prestige Minimum: &78"));
            itemLore.add(sendText(""));
            itemLore.add(sendText("&bClick to teleport"));
            container.set(GetNamespacedKey("farmName"), PersistentDataType.STRING, name);
            container.set(GetNamespacedKey("farmPrestige"), PersistentDataType.INTEGER, 8);
        }


        meta = SetAditMeta(meta);

        container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, name);
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getShopContent(Player player, ItemStack item, boolean custom){
        //ItemStack item = new ItemStack()
        ItemMeta meta = item.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        int itemAmount = buyAmount.get(player);
        Double price = GetPrice(item.getType().toString().toLowerCase().replaceAll("_", "").trim());
        if (price == null){
            price = 100000000.0;
        }

        if (custom){
            price = GetPrice((container.get(GetNamespacedKey("configkey"), PersistentDataType.STRING)));
            if (price == null){
                price = 100000000.0;
            }
        }

        container.set(GetNamespacedKey("price"), PersistentDataType.DOUBLE, price);

        List<String> itemLore = new ArrayList<>();

        if (meta.hasLore()){
            for (String lore : meta.getLore()){
                itemLore.add(sendText(lore));
            }
        }

        if (!meta.hasDisplayName()){
            meta.setDisplayName(sendText("&3x"+itemAmount+" &b"+formatItemName(item.getType().toString())));
        }else{
            String actualDisplayname = meta.getDisplayName();
            meta.setDisplayName(sendText("&3x"+itemAmount+" "+actualDisplayname));
        }

        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7Price: &f"+FormatDouble(price*itemAmount)+icon));
        itemLore.add(sendText(" "));

        ItemStack storedItem;
        try{
            storedItem = ItemStackFromBase64(container.get(GetNamespacedKey("storeditem"), PersistentDataType.STRING));
        }catch (Exception e){
            storedItem = new ItemStack(Material.STICK);
        }

        ItemMeta storedMeta = storedItem.getItemMeta();
        PersistentDataContainer storedContainer = storedMeta.getPersistentDataContainer();
        Double storedWorth = storedContainer.get(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE);
        if (storedWorth != null){
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7This item can be sold!"));
            itemLore.add(sendText(" &7Worth: &f"+storedWorth+icon));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &fsell using &e/sellall, /sellgui"));
            itemLore.add(sendText(" "));
        }

        double playerBalance = GetPlayerBalance(player);
        if (playerBalance >= price){
            itemLore.add(sendText("&aClick to buy"));
        }else{
            itemLore.add(sendText("&cYou can't afford this item!"));
            itemLore.add(sendText("&cyou need &6"+FormatDouble(price-playerBalance)+icon+" more!"));
        }

        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }


    public static ItemStack getHeaderFooter(){
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        return border;
    }
    public static ItemStack getBackrgound(Material material){
        ItemStack border = new ItemStack(material);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        return border;
    }

    public static void SetHeaderFooter(Inventory inventory){
        int size = inventory.getSize();
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, getHeaderFooter());
        }
        for (int i = size-9; i < size; i++) {
            inventory.setItem(i, getHeaderFooter());
        }
    }

    public static void SetFooter(Inventory inventory){
        int size = inventory.getSize();
        for (int i = size-9; i < size; i++) {
            inventory.setItem(i, getHeaderFooter());
        }
    }

    public static void SetBackground(Inventory inventory, Material material){
        int size = inventory.getSize();
        for (int i = 0; i < size; i++) {
            if (inventory.getItem(i) == null){
                inventory.setItem(i, getBackrgound(material));
            }
        }
    }

    public static HashMap<Player, MenuList> openedMenu = new HashMap<>();
    public static HashMap<Player, MenuList> tempMenu = new HashMap<>();

    @EventHandler
    public void onInventoryClickHolder(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory == null) return;

        InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();

        if (holder instanceof CustomInventoryHolder) {
            event.setCancelled(true);
            PlaySound(Sound.BLOCK_NOTE_BLOCK_BIT, player, 1, 3);
        }
    }

    @EventHandler
    public void OnJoin(PlayerJoinEvent event){
        buyAmount.putIfAbsent(event.getPlayer(), 1);
        openedMenu.put(event.getPlayer(), MenuList.None);

        GameMenu(event.getPlayer());
    }

    @EventHandler
    public void OnQuit(PlayerQuitEvent event){
        openedMenu.put(event.getPlayer(), MenuList.None);
    }

    @EventHandler
    public void OnInventoryClose(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        if (event.getReason() == InventoryCloseEvent.Reason.PLAYER){
            openedMenu.put(player, MenuList.None);
            menuPage.put(player, 1);
            //player.sendMessage(sendText("&aClosed inventory caused by player!"));
        }

        if (openedMachine.get(player) != null){
            Bukkit.getScheduler().cancelTask(openedMachine.get(player));
            openedMachine.put(player, 0);
            //consoleLog(sendText("&aTask id of opened machine is canceled &6("+openedMachine.get(player)+")"));
        }else{
            openedMachine.put(player, 0);
            //consoleLog(sendText("&aPlayer does not have opened machine yet!"));
        }
    }

    public static HashMap<Player, Integer> buyAmount = new HashMap<>();
    public static HashMap<Player, String> openedCategory = new HashMap<>();

    @EventHandler
    public void OnInventoryClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();

        MenuList inventory = openedMenu.get(player);
        menuPage.putIfAbsent(player, 1);

        ItemStack item = event.getCurrentItem();

        if (item != null && item.getType() != Material.AIR){
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (inventory.equals(MenuList.MachineEngine)){
                if (GetTag(item).equals("upgrade")){

                    // debugging
                    Double upgradeContainer = container.get(GetNamespacedKey("upgradePrice"), PersistentDataType.DOUBLE);
                    if (upgradeContainer == null){
                        return;
                    }
                    String locationContainer = container.get(GetNamespacedKey("location"), PersistentDataType.STRING);
                    if (locationContainer == null){
                        return;
                    }

                    // gain location
                    Location machineLocation = parseLocationString(locationContainer);

                    // machine item meta
                    ItemStack previousItem = getMainPlugin().events.machineItems.get(machineLocation);
                    ItemMeta previousItemMeta = previousItem.getItemMeta();
                    PersistentDataContainer previousContainer = previousItemMeta.getPersistentDataContainer();

                    // gain the current level
                    int currentLevel = Integer.parseInt(placedMachines.get(machineLocation+__machineLevelKey));

                    // upgrade price
                    double playerBalance = GetPlayerBalance(player);
                    double upgradePrice = upgradeContainer;
                    // req: production
                    int totalProductionReq = currentLevel*25;
                    // cost of the acid
                    int advancedAcidCost = currentLevel*2; // something like refining, adaptive etc
                    int baseAcidCost = advancedAcidCost*5; // base acid with no rarity (used in every rarities machine upgrades)

                    Integer totalProduction = Integer.parseInt(placedMachines.get(machineLocation+__totalProductionKey));
                    Rarity.RarityType rarity = Rarity.RarityType.parseRarity(previousContainer.get(GetNamespacedKey(rarityKey), PersistentDataType.STRING));

                    // acid item discovery from rarity
                    ItemStack acidBase = GetItem("acid");
                    ItemStack acidItem = null;
                    if (rarity == Rarity.RarityType.Common){
                        acidItem = GetItem("refiningacid");
                    }
                    else if (rarity == Rarity.RarityType.Uncommon){
                        acidItem = GetItem("corrosiveacid");
                    }
                    else if (rarity == Rarity.RarityType.Rare){
                        acidItem = GetItem("energeticacid");
                    }
                    else if (rarity == Rarity.RarityType.Epic){
                        acidItem = GetItem("mutagenicacid");
                    }
                    else if (rarity == Rarity.RarityType.Legendary){
                        acidItem = GetItem("adaptiveacid");
                    }
                    else if (rarity == Rarity.RarityType.Immortal){
                        acidItem = GetItem("voidacid");
                    }

                    // requirement check (will return if not fulfilled)

                    if (currentLevel >= machineMaxLevel){
                        event.setCancelled(true);
                        player.sendMessage(sendText("&4You have reached the maximum level! &c(current level)"));
                        return;
                    }

                    if (playerBalance < upgradePrice) {
                        event.setCancelled(true);
                        player.sendMessage(Notification_NoMoney(player, upgradePrice-playerBalance));
                        return;
                    }

                    if (totalProduction < totalProductionReq) {
                        event.setCancelled(true);
                        player.sendMessage(sendText("&4You don't have enough requirement! &c("+(totalProductionReq-totalProduction)+" " +
                                "more total productions to upgrade)"));
                        return;
                    }

                    if (!isPlayerHasItem(player, acidBase, baseAcidCost)) {
                        event.setCancelled(true);
                        player.sendMessage(Notification_NoItem(player));
                        player.sendMessage(sendText("&cYou need x"+baseAcidCost+" Basic Acid to Upgrade!"));
                        return;
                    }

                    if (!isPlayerHasItem(player, acidItem, advancedAcidCost)) {
                        event.setCancelled(true);
                        player.sendMessage(Notification_NoItem(player));
                        player.sendMessage(sendText("&cYou need x"+advancedAcidCost+" "+uncolouredText(acidItem.getItemMeta().getDisplayName())+" " +
                                "to Upgrade!"));
                        return;
                    }

                    // removing item from player inventory
                    RemovePlayerBalance(player, upgradePrice);
                    RemoveItemFromPlayer(player, acidBase, baseAcidCost);
                    RemoveItemFromPlayer(player, acidItem, advancedAcidCost);

                    // level management
                    currentLevel++;
                    placedMachines.put(machineLocation+__machineLevelKey, ""+currentLevel);
                    consoleLog(sendText("Level from engines: "+placedMachines.get(machineLocation+__machineLevelKey)));

                    // refresh the machine item data
                    RefreshMachine(machineLocation);

                    // refresh the machine's hashmap in Events
                    getMainPlugin().events.EnableMachine(player, machineLocation);

                    player.sendMessage(sendText("&aSuccessfully upgrade your machine! to level &2"
                            +placedMachines.get(machineLocation+__machineLevelKey)));

                    OpenMachineEngines(player, machineLocation);

                    PlaySoundAt(Sound.ENTITY_PLAYER_LEVELUP, machineLocation, 1, 1);
                }
                else if (GetTag(item).equals("evolve")){

                    Double upgradeContainer = container.get(GetNamespacedKey("upgradePrice"), PersistentDataType.DOUBLE);
                    if (upgradeContainer == null){
                        return;
                    }

                    String locationContainer = container.get(GetNamespacedKey("location"), PersistentDataType.STRING);
                    if (locationContainer == null){
                        return;
                    }

                    Location machineLocation = parseLocationString(locationContainer);

                    ItemStack previousItem = getMainPlugin().events.machineItems.get(machineLocation);
                    ItemMeta previousItemMeta = previousItem.getItemMeta();
                    PersistentDataContainer previousContainer = previousItemMeta.getPersistentDataContainer();

                    int currentLevel = Integer.parseInt(placedMachines.get(machineLocation+__machineLevelKey));

                    double playerBalance = GetPlayerBalance(player);
                    double upgradePrice = upgradeContainer;
                    int acidCost = currentLevel*100;
                    int totalProductionReq = currentLevel*500;

                    if (acidCost > 2000){
                        acidCost = 2000;
                    }

                    Integer totalProduction = Integer.parseInt(placedMachines.get(machineLocation+__totalProductionKey));
                    Rarity.RarityType rarity = Rarity.RarityType.parseRarity(placedMachines.get(machineLocation+__rarityKey));

                    ItemStack acidItem = null;
                    String nextRarity= Rarity.RarityType.Common.toString();
                    if (rarity == Rarity.RarityType.Common){
                        acidItem = GetItem("refiningacid");
                        nextRarity = Rarity.RarityType.Uncommon.toString();
                    }
                    else if (rarity == Rarity.RarityType.Uncommon){
                        acidItem = GetItem("corrosiveacid");
                        nextRarity = Rarity.RarityType.Rare.toString();
                    }
                    else if (rarity == Rarity.RarityType.Rare){
                        acidItem = GetItem("energeticacid");
                        nextRarity = Rarity.RarityType.Epic.toString();
                    }
                    else if (rarity == Rarity.RarityType.Epic){
                        acidItem = GetItem("mutagenicacid");
                        nextRarity = Rarity.RarityType.Legendary.toString();
                    }
                    else if (rarity == Rarity.RarityType.Legendary){
                        acidItem = GetItem("adaptiveacid");
                        nextRarity = Rarity.RarityType.Immortal.toString();
                    }
                    else if (rarity == Rarity.RarityType.Immortal){
                        acidItem = GetItem("voidacid");
                    }

                    else{
                        player.sendMessage(sendText("&4You've reached the maximum rarity on this machine!"));
                        return;
                    }

                    if (playerBalance < upgradePrice) {
                        event.setCancelled(true);
                        player.sendMessage(Notification_NoMoney(player, upgradePrice-playerBalance));
                        return;
                    }

                    if (totalProduction < totalProductionReq) {
                        event.setCancelled(true);
                        player.sendMessage(sendText("&4You don't have enough requirement! &c("+(totalProductionReq-totalProduction)+" " +
                                "more total productions to evolve)"));
                        return;
                    }

                    if (!isPlayerHasItem(player, acidItem, acidCost)) {
                        event.setCancelled(true);
                        player.sendMessage(Notification_NoItem(player));
                        player.sendMessage(sendText("&cYou need x"+acidCost+" "+uncolouredText(acidItem.getItemMeta().getDisplayName())+" Acid to Upgrade!"));
                        return;
                    }

                    RemovePlayerBalance(player, upgradePrice);
                    RemoveItemFromPlayer(player, acidItem, acidCost);

                    placedMachines.put(machineLocation+__rarityKey, nextRarity);

                    RefreshMachine(machineLocation);

                    getMainPlugin().events.EnableMachine(player, machineLocation);

                    OpenMachineEngines(player, machineLocation);

                    UpdateMachineTag(player.getName(), machineLocation, placedMachines.get(machineLocation+__machineNameKey), 0);
                    consoleLog(sendText(placedMachines.get(machineLocation+__machineNameKey)));

                    player.sendMessage(sendText("&aMachine has been evolved to "+Rarity.getColor(Rarity.RarityType.parseRarity(nextRarity))+Rarity.RarityType.parseRarity(nextRarity)));
                    PlaySoundAt(Sound.ENTITY_PLAYER_LEVELUP, machineLocation, 1, 2);
                    PlaySoundAt(Sound.UI_TOAST_CHALLENGE_COMPLETE, machineLocation, 1, 0);
                    PlaySoundAt(Sound.BLOCK_ANVIL_USE, machineLocation, 1, 0);
                }
                else if (GetTag(item).equals("switch")){
                    String locationContainer = container.get(GetNamespacedKey("location"), PersistentDataType.STRING);
                    if (locationContainer == null){
                        return;
                    }

                    Location machineLocation = parseLocationString(locationContainer);

                    if (!placedMachines.get(machineLocation+__machineStatusKey).equals("Broken")){
                        if (placedMachines.get(machineLocation+__machineStatusKey).equals("Active")){
                            placedMachines.put(machineLocation+__machineStatusKey, "Disabled");
                            getMainPlugin().events.DisableMachine(player, machineLocation);
                        }else{
                            placedMachines.put(machineLocation+__machineStatusKey, "Active");
                            getMainPlugin().events.EnableMachine(player, machineLocation);
                        }
                    }

                    getMainPlugin().events.UpdateMachineTag(player.getName(), machineLocation, sendText(getMainPlugin().events.machineItems.get(machineLocation)
                            .getItemMeta().getDisplayName()), 0);

                    OpenMachineEngines(player, machineLocation);

                    //player.sendMessage(sendText("&aPressed the switch"));
                }
            }

            else if (inventory.equals(MenuList.Farms)) {
                if (GetTag(item).contains("farm")){
                    String farmName = container.get(GetNamespacedKey("farmName"), PersistentDataType.STRING);
                    Integer prestigeReq = container.get(GetNamespacedKey("farmPrestige"), PersistentDataType.INTEGER);

                    int currentPrestige = playerPrestige.get(player.getUniqueId());

                    if (currentPrestige < prestigeReq){
                        player.sendMessage(sendText("&cYou need to reach &6Prestige "+intToRoman(prestigeReq)+" &cto enter "+farmName.replace("farm", "")+" farm!"));
                        return;
                    }

                    Location farmLocation = GetLocation(farmName);
                    if (farmLocation == null){
                        player.sendMessage(sendText("&4Sorry, that farm location is currently unavailable!"));
                        return;
                    }

                    player.teleport(farmLocation);
                }
            }


            else if (inventory.equals(MenuList.Shop)){
                if (GetTag(item).equals("category")){
                    String category = container.get(GetNamespacedKey("category"), PersistentDataType.STRING);
                    openedCategory.put(player, category);
                    OpenShopCategory(player, openedCategory.get(player));
                }
                else if (GetTag(item).equals("catalog")){
                    OpenCatalog(player, 1);
                }
            }

            else if (inventory.equals(MenuList.Prestige)) {
                if (GetTag(item).equals("prestige")) {
                    player.closeInventory();
                    PerformPrestige(player);
                }
            }

            else if (inventory.equals(MenuList.Dungeon)){
                if (GetTag(item).equals("dungeon")){
                    String dungeonTarget = container.get(GetNamespacedKey("dungeon"), PersistentDataType.STRING);

                    player.closeInventory();

                    assert dungeonTarget != null;
                    Dungeon.DungeonList dungeon = Dungeon.DungeonList.parseDungeon(dungeonTarget);

                    assert dungeon != Dungeon.DungeonList.None;

                    TeleportDungeon(player, dungeon);
                }
            }


            else if (inventory.equals(MenuList.ShopPage)){
                if (container.has(GetNamespacedKey("price"))){
                    double playerBalance = GetPlayerBalance(player);
                    Double priceContainer = container.get(GetNamespacedKey("price"), PersistentDataType.DOUBLE);
                    int itemAmount = buyAmount.get(player);
                    if (item.getMaxStackSize() == 1){
                        itemAmount = 1;
                    }
                    double price = 1000000.0*itemAmount;

                    String storedItem = container.get(GetNamespacedKey("storeditem"), PersistentDataType.STRING);

                    ItemStack obtainedItem = loadSerializedItem(storedItem);

                    if (obtainedItem == null){
                        player.sendMessage(sendText("&cYou can't buy this item, please contact server's admin if you believe this was an error!"));
                        return;
                    }

                    if (priceContainer != null){
                        price = priceContainer*itemAmount;
                    }

                    if (playerBalance < price){
                        player.sendMessage(Notification_NoMoney(player, playerBalance));
                        return;
                    }

                    obtainedItem.setAmount(itemAmount);

                    ItemMeta obtainedItemMeta = obtainedItem.getItemMeta();
                    PersistentDataContainer obtainedItemContainer = obtainedItemMeta.getPersistentDataContainer();
                    if (obtainedItemContainer.has(GetNamespacedKey(machineKey)) || obtainedItemContainer.has(GetNamespacedKey(backpackSizeKey))){
                        obtainedItemContainer.set(GetNamespacedKey(serialCodeKey), PersistentDataType.STRING, GenerateSerialCode());
                        obtainedItem.setItemMeta(obtainedItemMeta);
                    }

                    Map<Integer, ItemStack> addedItem = player.getInventory().addItem(obtainedItem);
                    if (addedItem.isEmpty()){
                        RemovePlayerBalance(player, price);
                        PlaySound(Sound.BLOCK_NOTE_BLOCK_CHIME, player, 1, 1);
                        player.sendMessage(sendText("&aSuccessfully bought "+item.getItemMeta().getDisplayName()+" &afor &2"+FormatDouble(price)+icon));
                    }else{
                        int successfullyAdded = itemAmount - addedItem.values().stream().mapToInt(ItemStack::getAmount).sum();

                        if (successfullyAdded == 0) {
                            player.sendMessage(Notification_InventoryFull(player));
                        } else {
                            double partialPrice = successfullyAdded * price;
                            RemovePlayerBalance(player, partialPrice);
                            PlaySound(Sound.BLOCK_NOTE_BLOCK_CHIME, player, 1, 1);
                            player.sendMessage(sendText("&aSuccessfully bought &bx" + successfullyAdded + " " + item.getItemMeta().getDisplayName() + "&afor &2" + FormatDouble(partialPrice)+icon));

                            int remainingItems = itemAmount - successfullyAdded;
                            player.sendMessage(sendText("&cYour inventory was too full to add &bx" + remainingItems + " " + item.getItemMeta().getDisplayName() + "&c."));
                        }
                    }
                }
                if (GetTag(item).equals("buyamount_plus")){
                    int currentBuyAmount = buyAmount.get(player);
                    if (currentBuyAmount < 64){
                        if (currentBuyAmount == 1){
                            currentBuyAmount += 3;
                        }else{
                            currentBuyAmount += 4;
                        }
                    }
                    buyAmount.put(player, currentBuyAmount);
                    RefreshShop(player);
                }
                else if (GetTag(item).equals("buyamount_minus")){
                    int currentBuyAmount = buyAmount.get(player);
                    if (currentBuyAmount >= 4){
                        currentBuyAmount -= 4;
                    }
                    if (currentBuyAmount <= 0){
                        currentBuyAmount = 1;
                    }
                    buyAmount.put(player, currentBuyAmount);
                    RefreshShop(player);
                }

                else if (GetTag(item).equals("next_page")){
                    int currentPage = menuPage.get(player);
                    if (currentPage < maxCategoryPage){
                        currentPage++;
                        menuPage.put(player, currentPage);
                        OpenShopCategory(player, openedCategory.get(player));
                    }
                }
                else if (GetTag(item).equals("previous_page")){
                    int currentPage = menuPage.get(player);
                    if (currentPage > 1){
                        currentPage--;
                        menuPage.put(player, currentPage);
                        OpenShopCategory(player, openedCategory.get(player));
                    }
                }

                else if (GetTag(item).equals("back")){
                    //player.closeInventory();
                    OpenMenu(player, MenuList.Shop);
                }
            }

            else if (inventory.equals(MenuList.Anvil)){
                if (container.has(GetNamespacedKey(itemKey)) || container.has(GetNamespacedKey(machineKey))){
                    ItemStack currentItem = event.getCurrentItem();

                    if (isSellWand(currentItem)){
                        player.sendMessage(sendText("&4You can't repair this item!"));
                        return;
                    }

                    ItemMeta currentItemMeta = currentItem.getItemMeta();
                    PersistentDataContainer currentItemContainer = currentItemMeta.getPersistentDataContainer();

                    double durability = 0;
                    double maxDurability = 0;

                    if (!container.has(GetNamespacedKey(durabilityKey), PersistentDataType.DOUBLE)
                    && !container.has(GetNamespacedKey(durabilityKey), PersistentDataType.INTEGER)){
                        //player.sendMessage(sendText("&"));
                        return;
                    }

                    if (isFactoryItem(item)){
                        durability = currentItemContainer.get(GetNamespacedKey(durabilityKey), PersistentDataType.DOUBLE);
                        maxDurability = currentItemContainer.get(GetNamespacedKey(maxDurabilityKey), PersistentDataType.DOUBLE);
                    }else{
                        durability = currentItemContainer.get(GetNamespacedKey(durabilityKey), PersistentDataType.INTEGER);
                        maxDurability = currentItemContainer.get(GetNamespacedKey(maxDurabilityKey), PersistentDataType.INTEGER);
                    }

                    double playerMoney = GetPlayerBalance(player);
                    double fixPrice = maxDurability*0.08;

                    if (playerMoney < fixPrice){
                        player.sendMessage(sendText(Notification_NoMoney(player, fixPrice)));
                        return;
                    }


                    if (durability < maxDurability){
                        durability = maxDurability;
                    }else{
                        if (isMachine(item)){
                            currentItemContainer.set(GetNamespacedKey(machineStatusKey), PersistentDataType.STRING, "Active");
                            currentItem.setItemMeta(currentItemMeta);
                            item.setItemMeta(UpdateMachineItem(item).getItemMeta());
                        }

                        player.sendMessage(sendText("&aThis item is already fixed!"));
                        PlaySoundAt(Sound.BLOCK_LANTERN_PLACE, player.getLocation(), 1, 1);

                        return;
                    }
                    RemovePlayerBalance(player, fixPrice);

                    if (isFactoryItem(item)){
                        currentItemContainer.set(GetNamespacedKey(durabilityKey), PersistentDataType.DOUBLE, durability);
                    }

                    else if (isMachine(item)){
                        currentItemContainer.set(GetNamespacedKey(durabilityKey), PersistentDataType.INTEGER, (int) durability);
                        currentItemContainer.set(GetNamespacedKey(machineStatusKey), PersistentDataType.STRING, "Active");
                    }

                    currentItem.setItemMeta(currentItemMeta);
                    if (isFactoryItem(item)){
                        UpdateItem(player, ""+event.getSlot(), item);
                    }

                    else if (isMachine(item)){
                        item.setItemMeta(UpdateMachineItem(item).getItemMeta());
                    }
                    PlaySoundAt(Sound.BLOCK_ANVIL_USE, player.getLocation(), 1, 1);
                }
            }
            else if (inventory.equals(MenuList.Catalog)){
                if (GetTag(item).equals("next_page")){
                    int currentPage = menuPage.get(player);
                    if (currentPage < maxCatalogPage){
                        currentPage++;
                        menuPage.put(player, currentPage);
                        OpenCatalog(player, menuPage.get(player));
                    }
                }
                else if (GetTag(item).equals("previous_page")){
                    int currentPage = menuPage.get(player);
                    if (currentPage > 1){
                        currentPage--;
                        menuPage.put(player, currentPage);
                        OpenCatalog(player, menuPage.get(player));
                    }
                }
                else if (GetTag(item).equals("shop")){
                    OpenMenu(player, MenuList.Shop);
                }
            }

            else if (inventory.equals(MenuList.MultiBlock)) {
                if (GetTag(item).contains("multiblock")){
                    Inventory multiBlockMenu = OpenGUI(player, 4, ""+formatItemName(GetTag(item).replaceAll("machine_", "").trim()));
                    SetHeaderFooter(multiBlockMenu);

                    if (GetTag(item).equals("multiblock_acid_pipe")) {
                        multiBlockMenu.setItem(13, getBasicUi("multiblock_pipe"));
                        multiBlockMenu.setItem(22, getBasicUi("multiblock_machine"));
                    }
                    else if (GetTag(item).equals("multiblock_acid_maker")) {
                        multiBlockMenu.setItem(13, new ItemStack(Material.MOSSY_COBBLESTONE_WALL));
                        multiBlockMenu.setItem(22, new ItemStack(Material.BREWING_STAND));
                    }
                    else if (GetTag(item).equals("multiblock_carbon_forge")) {
                        multiBlockMenu.setItem(13, new ItemStack(Material.ANVIL));
                        multiBlockMenu.setItem(22, new ItemStack(Material.FURNACE));
                    }
                    else if (GetTag(item).equals("multiblock_armor_crafter")) {
                        multiBlockMenu.setItem(13, new ItemStack(Material.ANVIL));
                        multiBlockMenu.setItem(22, new ItemStack(Material.DISPENSER));
                    }
                    else if (GetTag(item).equals("multiblock_nether_smelter")) {
                        multiBlockMenu.setItem(13, new ItemStack(Material.BLAST_FURNACE));
                        multiBlockMenu.setItem(22, getBasicUi("multiblock_fire"));
                    }
                    multiBlockMenu.setItem(31, getBasicUi("back"));
                    player.openInventory(multiBlockMenu);
                    player.updateInventory();
                }
                else if (GetTag(item).equals("back")){
                    OpenMenu(player, MenuList.MultiBlock);
                    player.updateInventory();
                }
            }

            else if (inventory.equals(MenuList.GameMenu)) {
                if (container.has(GetNamespacedKey("command"), PersistentDataType.STRING)){
                    String commandKey = container.get(GetNamespacedKey("command"), PersistentDataType.STRING);

                    player.closeInventory();

                    assert commandKey != null;
                    player.performCommand(commandKey);
                }
            }

            else if (inventory.equals(MenuList.WarpMenu)){
                if (container.has(GetNamespacedKey("warpName"))){
                    String warpName = container.get(GetNamespacedKey("warpName"), PersistentDataType.STRING);

                    assert warpName != null;
                    Location warpLocation = GetLocation(warpName.replaceAll("_", "").trim());
                    if (warpLocation == null){
                        player.sendMessage(sendText("&4That warp is not exist!"));
                        return;
                    }

                    player.teleport(warpLocation);
                    PlaySoundAt(Sound.ENTITY_ENDERMAN_TELEPORT, player.getLocation(), 1, 1);
                    player.sendMessage(sendText("&bTeleported to &3"+formatItemName(warpName)));
                }
            }

            else if (inventory.equals(MenuList.AcidMaker)) {
                //player.sendMessage("AcidMaker");
                /*if (GetTag(item).equals("acidmaker_acid")) {
                    //player.sendMessage("acidmaker_acid");
                    ItemStack waterBottle = new ItemStack(GetPotion(PotionType.WATER));
                    ItemStack sugar = new ItemStack(Material.SUGAR);

                    waterBottle.setItemMeta(ProcessItemMeta(waterBottle).getItemMeta());
                    sugar.setItemMeta(ProcessItemMeta(sugar).getItemMeta());

                    if (!isPlayerHasItem(player, waterBottle, 1)){
                        player.sendMessage(sendText("&4You need &cx1 Water Bottle!"));
                        return;
                    }

                    if (!isPlayerHasItem(player, sugar, 1)){
                        player.sendMessage(sendText("&4You need &cx1 Sugar!"));
                        return;
                    }

                    RemoveItemFromPlayer(player, waterBottle, 1);
                    RemoveItemFromPlayer(player, sugar, 1);

                    player.getInventory().addItem(GetItem("acid"));
                }*/
            }

            else if (inventory.equals(MenuList.Rewards)) {
                if (container.has(GetNamespacedKey("rewards"))){
                    String rewards = container.get(GetNamespacedKey("rewards"), PersistentDataType.STRING);

                    assert rewards != null;
                    RewardsManager.RewardType rewardType = RewardsManager.RewardType.parseReward(rewards);
                    assert rewardType != RewardsManager.RewardType.None;

                    if (playerLevel.get(player.getUniqueId()) < RewardsManager.RewardType.getLevel(rewardType)){
                        player.sendMessage(Notification_NoLevel(player));
                        return;
                    }

                    if (hasCooldown(player, CooldownManager.CooldownType.parseCooldown(rewardType.toString()))){
                        return;
                    }

                    ClaimRewards(player, RewardsManager.RewardType.parseReward(rewards));

                    PlaySoundAt(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, player.getLocation(), 1, 1);
                    PlaySoundAt(Sound.ENTITY_FIREWORK_ROCKET_BLAST, player.getLocation(), 1, 2);

                    player.updateInventory();
                }
            }

            else if (inventory.equals(MenuList.Quest)) {
                if (container.has(GetNamespacedKey("quest"))) {
                    String quest = container.get(GetNamespacedKey("quest"), PersistentDataType.STRING);
                    assert quest != null;
                    FactoryQuest.Quest claimedQuest = FactoryQuest.Quest.parseQuest(quest);
                    assert claimedQuest != FactoryQuest.Quest.None;
                    ClaimQuest(player, claimedQuest);
                    player.closeInventory();
                }

                else if (GetTag(item).equals("complete")){
                    CompleteQuest(player, quest.get(player.getUniqueId()));
                    player.closeInventory();
                }
                else if (GetTag(item).equals("abandon")){
                    AbandonQuest(player);
                    player.closeInventory();
                }
            }

            else if (inventory.equals(MenuList.Sell)){
                if (GetTag(item).equals("sellall")){
                    SellAll(player);
                }
                else if (GetTag(item).equals("sellgui")){
                    player.closeInventory();
                    OpenSellGui(player);
                }
            }

            else if (inventory.equals(MenuList.Carbon_Forge)) {
                if (GetTag(item).equals("carbon")) {
                    OpenCarbonForge(player, carbonMaterials[playerSelector.get(player)]);
                }

                else if (GetTag(item).equals("next_page")){
                    int currentPage = playerSelector.get(player);
                    if (currentPage < carbonMaterials.length-1){
                        currentPage++;
                        playerSelector.put(player, currentPage);
                        OpenMenu(player, MenuList.Carbon_Forge);
                    }
                }
                else if (GetTag(item).equals("previous_page")){
                    int currentPage = playerSelector.get(player);
                    if (currentPage > 0){
                        currentPage--;
                        playerSelector.put(player, currentPage);
                        OpenMenu(player, MenuList.Carbon_Forge);
                    }
                }

                else if (GetTag(item).equals("view")){
                    player.closeInventory();
                    int index = 1;
                    player.sendMessage(" ");
                    player.sendMessage(sendText(" &b&lCarbon Equipments"));
                    for (String eq : carbonMaterials){
                        player.sendMessage(sendText("  &7["+index+"] &f"+formatItemName(eq)+" Equipments"));
                        index++;
                    }
                }

                else if (GetTag(item).equals("input")){
                    player.closeInventory();
                    Notification_Input(player);
                    isInput.put(player, true);
                    tempMenu.put(player, MenuList.Carbon_Forge);
                }
            }


            if (container.has(GetNamespacedKey(gameMenuKey))){
                event.setCancelled(true);
                if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CHEST){
                    String openedTitle = uncolouredText(player.getOpenInventory().getTitle());

                    if (!openedTitle.equals("Chest") && !openedTitle.equals("Dispenser") &&
                    !openedTitle.equals("Dropper") && !openedTitle.equals("Barrel")){
                        OpenMenu(player, MenuList.GameMenu);
                    }
                }
            }
        }
    }

    public void OpenShopCategory(Player player, String category){
        openedMenu.put(player, MenuList.ShopPage);
        openedCategory.put(player, category);

        Inventory openedCategory = OpenGUI(player, 6, "Shop"+" ["+menuPage.get(player)+"/"+maxCategoryPage+"]");

        List<ItemStack> contentList = shopItemList.get(category+menuPage.get(player));

        SetFooter(openedCategory);
        if (!category.contains("machine") && !category.contains("steam")){
            openedCategory.setItem(48, getShopIcons(player, "buyamount_minus"));
            openedCategory.setItem(50, getShopIcons(player, "buyamount_plus"));
            buyAmount.put(player, 1);
        }
        for (int i = 0; i < contentList.size(); i++) {
            ItemStack addedItem = contentList.get(i);
            ItemMeta addedItemMeta = addedItem.getItemMeta();
            PersistentDataContainer addedItemContainer = addedItemMeta.getPersistentDataContainer();
            if (!addedItemContainer.has(GetNamespacedKey("customitem"))){
                openedCategory.setItem(i, getShopContent(player, contentList.get(i).clone(), false));
            }else{
                openedCategory.setItem(i, getShopContent(player, contentList.get(i).clone(), true));
            }
        }

        openedCategory.setItem(49, getShopIcons(player, "back"));

        openedCategory.setItem(53, getShopIcons(player, "next_page"));
        openedCategory.setItem(45, getShopIcons(player, "previous_page"));

        player.openInventory(openedCategory);

        //player.sendMessage("Category: "+category);
    }

    public static HashMap<Player, Integer> menuPage = new HashMap<>();

    static void RefreshShop(Player player){
        for (ItemStack shopContent : player.getOpenInventory().getTopInventory().getContents()){
            if (shopContent != null){
                ItemMeta contentMeta = shopContent.getItemMeta();
                if (contentMeta.getPersistentDataContainer().has(GetNamespacedKey("price"))){
                    int itemAmount = 1;
                    if (!contentMeta.getPersistentDataContainer().has(GetNamespacedKey(machineKey)) &&
                            !contentMeta.getPersistentDataContainer().has(GetNamespacedKey(backpackSizeKey))){
                        itemAmount = buyAmount.get(player);
                    }
                    PersistentDataContainer container = contentMeta.getPersistentDataContainer();
                    Double contentPrice = container.get(GetNamespacedKey("price"), PersistentDataType.DOUBLE);
                    if (contentPrice == null){
                        contentPrice = 1000000000.0;
                    }
                    if (shopContent.getMaxStackSize() == 1){
                        itemAmount = 1;
                    }
                    shopContent.setAmount(itemAmount);
                    List<String> displayName = Arrays.asList(contentMeta.getDisplayName().split(" "));

                    String fixedDisplayName = contentMeta.getDisplayName().replace(displayName.getFirst()+" ", "").trim();
                    contentMeta.setDisplayName(sendText("&bx"+itemAmount+" "+fixedDisplayName));

                    List<String> storedLore = new ArrayList<>();

                    for (String lore : contentMeta.getLore()){
                        if (lore != null){
                            if (uncolouredText(lore).contains("Price")){
                                storedLore.add(sendText(" &7Price: &f"+(FormatDouble(contentPrice*itemAmount))+icon));
                            }else{
                                storedLore.add(sendText(lore));
                            }
                        }
                    }

                    contentMeta.setLore(storedLore);
                    shopContent.setItemMeta(contentMeta);
                }
            }
        }
        player.updateInventory();
    }

    public static HashMap<Player, Integer> openedMachine = new HashMap<>();

    public static void OpenMachineEngines(Player player, Location location){
        int level = Integer.parseInt(placedMachines.get(location+__machineLevelKey));
        String machineName = placedMachines.get(location+__machineNameKey);
        String owner = placedMachines.get(location+__ownerKey);
        Inventory inventory = OpenGUI(player, 4, owner+"'s Machine Engine - Lv. "+level);
        openedMenu.put(player, MenuList.MachineEngine);
        SetHeaderFooter(inventory);
        SetBackground(inventory, Material.GRAY_STAINED_GLASS_PANE);
        inventory.setItem(13, getMachineEnginesItem(player,"upgrade", location));
        inventory.setItem(8, getMachineEnginesItem(player,"switch", location));
        inventory.setItem(22, getMachineEnginesItem(player,"timer", location));
        inventory.setItem(31, getMachineEnginesItem(player,"evolve", location));
        inventory.setItem(0, getMachineEnginesItem(player,"information", location));
        player.openInventory(inventory);

        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                //Inventory topInventory = player.getOpenInventory().getTopInventory();

                inventory.setItem(22, getMachineEnginesItem(player,"timer", location));
                inventory.setItem(13, getMachineEnginesItem(player,"upgrade", location));
                inventory.setItem(8, getMachineEnginesItem(player,"switch", location));
                inventory.setItem(31, getMachineEnginesItem(player,"evolve", location));
                inventory.setItem(0, getMachineEnginesItem(player,"information", location));

                player.updateInventory();
            }
        }.runTaskTimer(getMainPlugin(), 0L, 20L).getTaskId();

        openedMachine.put(player, taskId);
        player.updateInventory();
    }

    public static ItemStack getMachineEnginesItem(Player player, String name, Location location){
        int level = Integer.parseInt(placedMachines.get(location+__machineLevelKey));
        int nextLevel = level+1;
        int totalProduction = Integer.parseInt(placedMachines.get(location+__totalProductionKey));

        String status = placedMachines.get(location+__machineStatusKey);
        ItemStack item = new ItemStack(machineItems.get(location).clone());
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        ItemStack machineDrop = GetItem(container.get(GetNamespacedKey(dropNameKey), PersistentDataType.STRING));
        ItemMeta machineDropMeta = machineDrop.getItemMeta();
        PersistentDataContainer machineDropContainer = machineDropMeta.getPersistentDataContainer();

        double dropWorth = machineDropContainer.get(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE);

        List<String> itemLore = new ArrayList<>();

        if (name.equals("upgrade")){
            item = new ItemStack(item.getType());
            meta = item.getItemMeta();
            container = meta.getPersistentDataContainer();

            Integer machineLevelReq = GetLevelMinimum(uncolouredText(machineItems.get(location).clone().getItemMeta()
                    .getDisplayName().replaceAll(" ", "").trim().toLowerCase()));
            if (machineLevelReq == null){
                machineLevelReq = 1;
            }

            //consoleLog(uncolouredText(machineItems.get(location).clone().getItemMeta()
                    //.getDisplayName().replaceAll(" ", "").trim().toLowerCase()));

            double upgradePreCalculate = (level*100)+level*120;
            double upgradePrice = upgradePreCalculate+(machineLevelReq*35);
            int totalProductionReq = level*25;
            int advancedAcidCost = level*2;
            int baseAcidCost = advancedAcidCost*5;

            meta.setDisplayName(sendText("&a&lUpgrade Machine &2("+(level)+" &b➠ &2"+(level+1)+")"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &8➭ &fNext Upgrade:"));
            if (placedMachines.get(location+__machineTypeKey).equals("item")){
                itemLore.add(sendText(" &8➳ &7Steam Consumption: &e"+(level+2)+" &6-> &e"+(nextLevel+2)));
            }else{
                itemLore.add(sendText(" &8➳ &7Steam Production: &e"+(level+2)+" &6-> &e"+(nextLevel+2)));
            }
            itemLore.add(sendText(" &8➳ &7Machine Speed: &e"+(machineBaseSpeed-(level))+"s &6-> &e"+(machineBaseSpeed-(nextLevel)+"s")));
            itemLore.add(sendText(" &8➳ &7Durability: &e"+(level*100)+" &6-> &e"+(nextLevel*100)));
            itemLore.add(sendText(" &8➳ &7Production Worth: &e"+(dropWorth+level)+" &6-> &e"+(dropWorth+nextLevel)));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &8↙ &fRequirements:"));
            itemLore.add(sendText(" &8➽ &7Money: &f"+FormatDouble(upgradePrice)+icon));
            itemLore.add(sendText(" &8➽ &7Basic Acid: &ex"+baseAcidCost));
            ItemStack acidBase = GetItem("acid");
            ItemStack acidItem = null;
            Rarity.RarityType rarity = Rarity.RarityType.parseRarity(placedMachines.get(location+__rarityKey));
            if (rarity == Rarity.RarityType.Common){
                acidItem = GetItem("refiningacid");
            }
            else if (rarity == Rarity.RarityType.Uncommon){
                acidItem = GetItem("corrosiveacid");
            }
            else if (rarity == Rarity.RarityType.Rare){
                acidItem = GetItem("energeticacid");
            }
            else if (rarity == Rarity.RarityType.Epic){
                acidItem = GetItem("mutagenicacid");
            }
            else if (rarity == Rarity.RarityType.Legendary){
                acidItem = GetItem("adaptiveacid");
            }
            else if (rarity == Rarity.RarityType.Immortal){
                acidItem = GetItem("voidacid");
            }
            itemLore.add(sendText(" &8➽ &7"+uncolouredText(acidItem.getItemMeta().getDisplayName())+" Acid: &ex"+advancedAcidCost));
            itemLore.add(sendText(" &8➽ &7Total Production: &e"+level*25));
            itemLore.add(sendText(" "));
            double playerBalance = GetPlayerBalance(player);
            if (playerBalance >= upgradePrice){
                itemLore.add(sendText("&aClick to upgrade"));
            }else{
                itemLore.add(sendText("&cClick to upgrade"));
            }
            container.set(GetNamespacedKey("upgradePrice"), PersistentDataType.DOUBLE, upgradePrice);
            container.set(GetNamespacedKey("totalProductionReq"), PersistentDataType.INTEGER, totalProductionReq);
            //container.set(GetNamespacedKey("acidCost"), PersistentDataType.INTEGER, acidCost);
        }
        else if (name.equals("switch")){
            item = new ItemStack(Material.LEVER);
            meta = item.getItemMeta();
            container = meta.getPersistentDataContainer();

            String currentStats = "&aON";
            if (status.equals("Disabled")){
                currentStats = "&cOFF";
            }
            else if (status.equals("Enabled")){
                currentStats = "&aON";
            }

            if (!status.equals("Broken")){
                meta.setDisplayName(sendText("&fSwitch &8["+currentStats+"&8]"));
            }else{
                item.setType(Material.REDSTONE_TORCH);
                meta.setDisplayName(sendText("&c&lSWITCH BROKEN"));
            }
        }
        else if (name.equals("timer")){
            item = new ItemStack(Material.CLOCK);
            meta = item.getItemMeta();
            container = meta.getPersistentDataContainer();

            String productionTimer = placedMachines.get(location+__countdownKey)+"s";
            if (placedMachines.get(location+__countdownKey) == null){
                productionTimer = "0s";
            }

            meta.setDisplayName(sendText("&6Machine Timer"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &fMachine Speed: &e"+placedMachines.get(location+__speedKey)+" seconds"));
            itemLore.add(sendText(" &fTime until next produce: &f"+productionTimer));
            itemLore.add(sendText(" "));
        }
        else if (name.equals("information")){
            item = new ItemStack(Material.PAPER);
            meta = item.getItemMeta();
            container = meta.getPersistentDataContainer();

            meta.setDisplayName(sendText("&dMachine Information"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Speed: &f"+placedMachines.get(location+__speedKey)));
            itemLore.add(sendText(" &7Production Rate: &f"+placedMachines.get(location+__productionRateKey)));
            itemLore.add(sendText(" &7Steam Consumption: &f"+placedMachines.get(location+__steamConsumptionKey)));
            itemLore.add(sendText(" &7Total Production: &f"+placedMachines.get(location+__totalProductionKey)));
            itemLore.add(sendText(" &7Rarity: &f"+ Rarity.RarityType.parseRarity(placedMachines.get(location+__rarityKey))));
            itemLore.add(sendText(" &7Durability: &f"+placedMachines.get(location+__durabilityKey)+"/"+placedMachines.get(location+__maxDurabilityKey)));
            itemLore.add(sendText(" "));
        }
        else if (name.equals("evolve")){
            item = new ItemStack(Material.FIRE_CHARGE);
            meta = item.getItemMeta();
            container = meta.getPersistentDataContainer();

            double upgradePrice = (double) level /4;
            meta.setDisplayName(sendText("&d&lEvolve Machine"));

            Rarity.RarityType rarity = Rarity.RarityType.parseRarity(placedMachines.get(location+__rarityKey));

            Rarity.RarityType nextRarity = Rarity.RarityType.Common;

            ItemStack acidRequirement = GetItem("acid");
            if (rarity == Rarity.RarityType.Common){
                acidRequirement = GetItem("refiningacid");
                nextRarity = Rarity.RarityType.Uncommon;
            }
            else if (rarity == Rarity.RarityType.Uncommon){
                acidRequirement = GetItem("corrosiveacid");
                nextRarity = Rarity.RarityType.Rare;
            }
            else if (rarity == Rarity.RarityType.Rare){
                acidRequirement = GetItem("energeticacid");
                nextRarity = Rarity.RarityType.Epic;
            }
            else if (rarity == Rarity.RarityType.Epic){
                acidRequirement = GetItem("mutagenicacid");
                nextRarity = Rarity.RarityType.Legendary;
            }
            else if (rarity == Rarity.RarityType.Legendary){
                acidRequirement = GetItem("adaptiveacid");
                nextRarity = Rarity.RarityType.Immortal;
            }
            else if (rarity == Rarity.RarityType.Immortal){
                acidRequirement = GetItem("voidacid");
            }

            int acidAmountRequirement = level*100;

            itemLore.add(sendText(" "));
            if (rarity != Rarity.RarityType.Immortal){
                itemLore.add(sendText(" &7Evolve to: "+Rarity.getColor(nextRarity)+nextRarity));
            }else{
                itemLore.add(sendText(" &c&lYou've reached maximum Rarity!"));
            }
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &8↙ &fRequirements:"));
            itemLore.add(sendText(" &8➽ &7Total Production: &ex"+(level*500)));
            itemLore.add(sendText(" &8➽ &7"+uncolouredText(acidRequirement.getItemMeta().getDisplayName())+" Acid: &ex"+acidAmountRequirement));
            itemLore.add(sendText(" "));
            itemLore.add(sendText("&dClick to evolve"));
            container.set(GetNamespacedKey("upgradePrice"), PersistentDataType.DOUBLE, upgradePrice);
        }
        meta.setLore(itemLore);

        container.set(GetNamespacedKey("location"), PersistentDataType.STRING, ""+location);
        container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, name);

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getShopIcons(Player player, String name){
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        List<String> itemLore = new ArrayList<>();

        if (name.equals("buyamount_plus")){
            item.setType(Material.GREEN_CONCRETE);
            meta.setDisplayName(sendText("&a[+] &7Buy Amount"));
        }
        else if (name.equals("buyamount_minus")){
            item.setType(Material.RED_CONCRETE);
            meta.setDisplayName(sendText("&c[-] &7Buy Amount"));
        }

        else if (name.equals("buyamount_status")){
            item.setType(Material.PAPER);
            meta.setDisplayName(sendText("&7Buy Amount: &b"+buyAmount.get(player)));
        }

        else if (name.equals("back")){
            item.setType(Material.CHEST);
            meta.setDisplayName(sendText("&cReturn to Main Page"));
        }

        else if (name.equals("next_page")){
            item.setType(Material.ARROW);
            meta.setDisplayName(sendText("&aNext Page &6&l→"));
        }
        else if (name.equals("previous_page")){
            item.setType(Material.SPECTRAL_ARROW);
            meta.setDisplayName(sendText("&6&l← &2Previous Page"));
        }

        container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, name);

        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }

    public static String GetTag(ItemStack item){
        String tag = "none";
        if (item != null){
            if (item.getType() != Material.AIR){
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (container.has(GetNamespacedKey("gui-icon"))){
                    String obtainedTag = container.get(GetNamespacedKey("gui-icon"), PersistentDataType.STRING);
                    if (obtainedTag != null){
                        tag = obtainedTag;
                    }
                }
            }
        }
        return tag;
    }


    public static int maxCatalogPage = 10;

    public static HashMap<Integer, List<ItemStack>> catalogItems = new HashMap<>();

    public static void OpenCatalog(Player player, int page){
        openedMenu.put(player, MenuList.Catalog);
        for (int i = 1; i < maxCatalogPage+1; i++) {
            catalogItems.put(i, new ArrayList<>());
        }

        List<ItemStack> allCatalogItems = new ArrayList<>();

        for (Material material : Material.values()){
            Double worth = GetWorth(material.toString().toLowerCase().replaceAll("_", "").trim());
            if (worth != null){
                ItemStack catalogItem = new ItemStack(material);
                ItemMeta catalogItemMeta = catalogItem.getItemMeta();
                PersistentDataContainer container = catalogItemMeta.getPersistentDataContainer();
                container.set(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE, worth);
                List<String> itemLore = new ArrayList<>();
                catalogItemMeta.setDisplayName(sendText("&b"+formatItemName(catalogItem.getType().toString().toLowerCase())));
                itemLore.add(sendText("&8---------------"));
                itemLore.add(sendText(" &7Worth: &f"+FormatDouble(worth)+icon));
                itemLore.add(sendText("&8---------------"));
                catalogItemMeta.setLore(itemLore);
                catalogItem.setItemMeta(catalogItemMeta);
                allCatalogItems.add(catalogItem);
            }
        }

        for (String key : worthList.keySet()){
            if (key.contains("custom")){
                String itemKey = key.replaceAll("_custom", "").trim();
                ItemStack customItem = itemList.get(itemKey).clone();
                ItemMeta customItemMeta = customItem.getItemMeta();
                PersistentDataContainer container = customItemMeta.getPersistentDataContainer();

                Double worth = GetWorth(key);
                if (worth != null){
                    container.set(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE, worth);
                    List<String> itemLore = new ArrayList<>();
                    customItemMeta.setDisplayName(sendText("&b"+uncolouredText(customItemMeta.getDisplayName())));
                    itemLore.add(sendText("&8---------------"));
                    itemLore.add(sendText(" &7Worth: &f"+FormatDouble(worth)+icon));
                    itemLore.add(sendText("&8---------------"));
                    customItemMeta.setLore(itemLore);
                    customItem.setItemMeta(customItemMeta);
                    allCatalogItems.add(customItem.clone());
                }
            }
        }

        allCatalogItems.sort(Comparator.comparingDouble(list -> list.getItemMeta().getPersistentDataContainer().get(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE)));

        int currentPage = 1;
        for (ItemStack item : allCatalogItems){
            if (catalogItems.get(currentPage).size() >= 36){
                currentPage++;
            }
            catalogItems.get(currentPage).add(item.clone());
        }

        Inventory inventory = OpenGUI(player, 6, "Catalog ["+page+"/"+maxCatalogPage+"]");
        int start = 0;
        for (int i = 0; i < catalogItems.get(page).size(); i++) {
            inventory.setItem(9+i, catalogItems.get(page).get(start).clone());
            start++;
        }
        SetHeaderFooter(inventory);
        inventory.setItem(48, getCatalogIcon("previous_page"));
        inventory.setItem(49, getCatalogIcon("shop"));
        inventory.setItem(50, getCatalogIcon("next_page"));
        player.openInventory(inventory);
    }

    public static ItemStack getCatalogIcon(String name){
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        List<String> itemLore = new ArrayList<>();

        if (name.equals("next_page")){
            item.setType(Material.ARROW);
            meta.setDisplayName(sendText("&aNext Page &6&l→"));
        }
        else if (name.equals("previous_page")){
            item.setType(Material.SPECTRAL_ARROW);
            meta.setDisplayName(sendText("&6&l← &2Previous Page"));
        }
        else if (name.equals("shop")){
            item.setType(Material.CHEST);
            meta.setDisplayName(sendText("&a&lShop"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Open shop to buy items"));
            itemLore.add(sendText(" "));
        }

        container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, name);

        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void OnInteract(PlayerInteractEvent event){
        if (event.getAction() == Action.RIGHT_CLICK_AIR ||
        event.getAction() == Action.RIGHT_CLICK_BLOCK){
            Player player = event.getPlayer();

            ItemStack item = player.getInventory().getItemInMainHand();
            if(item.getType() != Material.AIR){
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                if(container.has(GetNamespacedKey("multiBlockGuide"))){
                    event.setCancelled(true);
                    OpenMenu(player, MenuList.MultiBlock);
                    PlaySoundAt(Sound.ENTITY_ENDER_DRAGON_FLAP, player.getLocation(), 1, 3);
                    PlaySoundAt(Sound.ITEM_BOOK_PAGE_TURN, player.getLocation(), 1, 1);
                }

                else if(container.has(GetNamespacedKey(gameMenuKey))){
                    event.setCancelled(true);

                    OpenMenu(player, MenuList.GameMenu);
                }
            }
        }
    }

    public static String gameMenuKey = "gameMenu";

    @EventHandler
    public void OnDrop(PlayerDropItemEvent event){
        ItemStack item = event.getItemDrop().getItemStack();
        if (item.getItemMeta().getPersistentDataContainer().has(GetNamespacedKey(gameMenuKey))){
            event.setCancelled(true);
        }
    }

    public static void GameMenu(Player player){
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(GetNamespacedKey(gameMenuKey), PersistentDataType.BOOLEAN, true);

        meta.setDisplayName(sendText("&aGadget"));

        List<String> itemLore = new ArrayList<>();
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7All game features"));
        itemLore.add(sendText(" "));
        itemLore.add(sendText("&aClick to open"));
        meta.setLore(itemLore);

        item.setItemMeta(meta);


        PlayerInventory inventory = player.getInventory();

        if (inventory.getItem(8) == null){
            inventory.setItem(8, item);
        }else{
            if (!Objects.requireNonNull(inventory.getItem(8)).isSimilar(item)){
                inventory.setItem(8, item);
            }
        }
    }

    // blacksmith

    public static void OpenBlacksmith(Player player){
        Merchant merchant = Bukkit.createMerchant(sendText("&nBlacksmith"));
        List<MerchantRecipe> trades = new ArrayList<>();



        ItemStack netheriteIngot = new ItemStack(ProcessItemMeta(new ItemStack(Material.NETHERITE_INGOT)));
        netheriteIngot.setAmount(20);



        ItemStack tungstenIngot = new ItemStack(GetItem("tungsteningot"));
        tungstenIngot.setAmount(10);

        ItemStack palladiumIngot = new ItemStack(GetItem("palladiumingot"));
        palladiumIngot.setAmount(12);

        ItemStack cobaltIngot = new ItemStack(GetItem("cobaltingot"));
        cobaltIngot.setAmount(15);




        ItemStack mithrilIngot = new ItemStack(GetItem("mithrilingot"));
        mithrilIngot.setAmount(18);

        ItemStack orichalcumIngot = new ItemStack(GetItem("orichalcumingot"));
        orichalcumIngot.setAmount(20);

        ItemStack titaniumIngot = new ItemStack(GetItem("titaniumingot"));
        titaniumIngot.setAmount(25);



        ItemStack adamantineIngot = new ItemStack(GetItem("adamantineingot"));
        adamantineIngot.setAmount(30);

        ItemStack dragoniteIngot = new ItemStack(GetItem("dragoniteingot"));
        dragoniteIngot.setAmount(35);




        ItemStack voidsteelIngot = new ItemStack(GetItem("voidsteelingot"));
        voidsteelIngot.setAmount(40);

        ItemStack etheriumIngot = new ItemStack(GetItem("etheriumingot"));
        etheriumIngot.setAmount(48);



        List<String> toolsList = Arrays.asList("pickaxe", "axe", "shovel", "fishingrod");

        MerchantRecipe merchantRecipe;
        for (String tool : toolsList){
            merchantRecipe  = new MerchantRecipe(new ItemStack(GetItem("tungsten"+tool)), 9999);
            merchantRecipe.addIngredient(tungstenIngot);
            merchantRecipe.addIngredient(netheriteIngot);
            trades.add(merchantRecipe);
        }
        for (String tool : toolsList){
            merchantRecipe  = new MerchantRecipe(new ItemStack(GetItem("palladium"+tool)), 9999);
            merchantRecipe.addIngredient(palladiumIngot);
            merchantRecipe.addIngredient(tungstenIngot);
            trades.add(merchantRecipe);
        }
        for (String tool : toolsList){
            merchantRecipe  = new MerchantRecipe(new ItemStack(GetItem("cobalt"+tool)), 9999);
            merchantRecipe.addIngredient(cobaltIngot);
            merchantRecipe.addIngredient(palladiumIngot);
            trades.add(merchantRecipe);
        }


        for (String tool : toolsList){
            merchantRecipe  = new MerchantRecipe(new ItemStack(GetItem("mithril"+tool)), 9999);
            merchantRecipe.addIngredient(mithrilIngot);
            merchantRecipe.addIngredient(cobaltIngot);
            trades.add(merchantRecipe);
        }
        for (String tool : toolsList){
            merchantRecipe  = new MerchantRecipe(new ItemStack(GetItem("orichalcum"+tool)), 9999);
            merchantRecipe.addIngredient(orichalcumIngot);
            merchantRecipe.addIngredient(mithrilIngot);
            trades.add(merchantRecipe);
        }
        for (String tool : toolsList){
            merchantRecipe  = new MerchantRecipe(new ItemStack(GetItem("titanium"+tool)), 9999);
            merchantRecipe.addIngredient(titaniumIngot);
            merchantRecipe.addIngredient(orichalcumIngot);
            trades.add(merchantRecipe);
        }

        for (String tool : toolsList){
            merchantRecipe  = new MerchantRecipe(new ItemStack(GetItem("adamantine"+tool)), 9999);
            merchantRecipe.addIngredient(adamantineIngot);
            merchantRecipe.addIngredient(titaniumIngot);
            trades.add(merchantRecipe);
        }
        for (String tool : toolsList){
            merchantRecipe  = new MerchantRecipe(new ItemStack(GetItem("dragonite"+tool)), 9999);
            merchantRecipe.addIngredient(dragoniteIngot);
            merchantRecipe.addIngredient(adamantineIngot);
            trades.add(merchantRecipe);
        }

        for (String tool : toolsList){
            merchantRecipe  = new MerchantRecipe(new ItemStack(GetItem("voidsteel"+tool)), 9999);
            merchantRecipe.addIngredient(voidsteelIngot);
            merchantRecipe.addIngredient(dragoniteIngot);
            trades.add(merchantRecipe);
        }
        for (String tool : toolsList){
            merchantRecipe  = new MerchantRecipe(new ItemStack(GetItem("etherium"+tool)), 9999);
            merchantRecipe.addIngredient(etheriumIngot);
            merchantRecipe.addIngredient(voidsteelIngot);
            trades.add(merchantRecipe);
        }

        merchant.setRecipes(trades);
        player.openMerchant(merchant, true);
    }

    @EventHandler
    public void OnChatInput(PlayerChatEvent event){
        Player player = event.getPlayer();
        if (isInput.get(player)){
            event.setCancelled(true);
            if (tempMenu.get(player) == MenuList.Carbon_Forge){
                isInput.put(player, false);
                String messages = event.getMessage();
                Integer number = 0;
                number = Integer.valueOf(numberInText(messages));
                if (number > 0){
                    playerInput_Integer.put(player, number);
                }else{
                    player.sendMessage(sendText("&4Input must be &c1 or higher!"));
                }
            }

            if (tempMenu.get(player) == MenuList.Carbon_Forge){
                if (playerInput_Integer.get(player) <= carbonMaterials.length){
                    playerSelector.put(player, playerInput_Integer.get(player)-1);
                    OpenMenu(player, MenuList.Carbon_Forge);
                    consoleLog(sendText("&2"+player.getName()+" &ainputed &2"+playerInput_Integer.get(player)+" &afor carbon forge"));
                }
            }

            playerInput_Integer.put(player, 0);
        }
    }

}
