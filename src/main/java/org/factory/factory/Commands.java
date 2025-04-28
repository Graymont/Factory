package org.factory.factory;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.factory.factory.Utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.Name;
import java.lang.reflect.Array;
import java.util.*;

import static org.factory.factory.Database.*;
import static org.factory.factory.Events.*;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.Utils.Booster.*;
import static org.factory.factory.Utils.CooldownManager.ResetCooldown;
import static org.factory.factory.Utils.CooldownManager.SetCooldown;
import static org.factory.factory.Utils.Dungeon.GetDungeonLoot;
import static org.factory.factory.Utils.Dungeon.TeleportDungeon;
import static org.factory.factory.Utils.FactoryEvents.RollEvents;
import static org.factory.factory.Utils.FactoryEvents.SetEvent;
import static org.factory.factory.Utils.FactoryItem.*;
import static org.factory.factory.Utils.FactoryMachine.*;
import static org.factory.factory.Utils.FactoryMob.SpawnMob;
import static org.factory.factory.Utils.FactoryQuest.*;
import static org.factory.factory.Utils.GUIManager.*;
import static org.factory.factory.Utils.MultiBlock.*;
import static org.factory.factory.Utils.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.PlayerProgress.*;
import static org.factory.factory.Utils.RewardsManager.*;
import static org.factory.factory.Utils.SQLiteDatabase.SaveAllProgress;
import static org.factory.factory.Utils.TraderManager.OpenTrader;
import static org.factory.factory.Utils.UserInterface.*;


public class Commands implements CommandExecutor, TabCompleter {

    Events events;
    Factory plugin;

    public Commands(Events e, Factory pl){
        events = e;
        plugin = pl;
    }



    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("factoryutils")){

            if (sender instanceof Player){
                if (!sender.hasPermission("factory.admin")){
                    sender.sendMessage(sendText("&cYou don't have permission to use &6Factory's Administrator Commands!"));
                    return false;
                }
            }

            if (args[0].equalsIgnoreCase("setmachinespeed")){
                Player player = (Player) sender;
                Block targetBlock = player.getTargetBlockExact(5);
                ItemStack drop = player.getInventory().getItemInMainHand().clone();

                if (targetBlock == null){
                    player.sendMessage(sendText("&eTarget block must be a machine!"));
                    return false;
                }

                if (drop.getType() == Material.AIR){
                    player.sendMessage(sendText("&eDrop must be a valid ItemStack!"));
                    return false;
                }

                Location location = targetBlock.getLocation();
                if (events.placedMachines.get(location+".owner") != null){
                    long speed = Long.parseLong(args[1]);
                    //events.StartMachine(player, speed, targetBlock, drop, events.machineItems.get(location));
                    player.sendMessage(sendText("&aSpeed set to "+args[1]));
                }
            }
            else if (args[0].equalsIgnoreCase("createmachine")){
                Player player = (Player) sender;
                int machineLevel = Integer.parseInt(args[1]);
                long speed = Long.parseLong(args[2]);
                int productionRate = Integer.parseInt(args[3]);
                int steamConsumption = Integer.parseInt(args[4]);
                int durability = Integer.parseInt(args[5]);
                int maxDurability = Integer.parseInt(args[6]);
                Material material = Material.getMaterial(args[7]);
                String drop = args[8];
                int potentialDrop = Integer.parseInt(args[9]);
                Rarity.RarityType rarity = Rarity.RarityType.parseRarity(args[10]);

                String machineType = args[11];
                Integer steamProduction = Integer.parseInt(args[12]);

                String machineName = String.join(" ", Arrays.copyOfRange(args, 13, args.length));



                ItemStack machine = CreateMachine(machineName, machineLevel, speed, productionRate, steamConsumption, durability, maxDurability,
                        material, drop, potentialDrop, rarity, "Active", 0, MachineType.parseType(machineType), steamProduction, true,
                        1);
                player.getInventory().addItem(machine);
            }

            else if (args[0].equalsIgnoreCase("createitem")){
                Player player = (Player) sender;
                /*if (args.length == 1){
                    Player player = (Player) sender;
                    player.sendMessage(sendText("&aCreate Item Format: Type, SubType," +
                            " AttackDamage, AttackSpeed, CriticalChance, SteamConsumption," +
                            " Durability, MaxDurability, Rarity, DisplayName, Material"));
                    return false;
                }*/

                /*FactoryItem.Type item_type = FactoryItem.Type.parseType(args[1]);
                FactoryItem.SubType item_subType = FactoryItem.SubType.parseSubType(args[2]);
                double attackDamage = Double.parseDouble(args[3]);
                double attackRange = Double.parseDouble(args[4]);
                double attackSpeed = Double.parseDouble(args[5]);
                double criticalChance = Double.parseDouble(args[6]);
                double steamConsumption = Double.parseDouble(args[7]);
                double durability = Double.parseDouble(args[8]);
                double maxDurability = Double.parseDouble(args[9]);
                Rarity.RarityType rarity = Rarity.RarityType.parseRarity(args[10]);
                String displayname = String.join(" ", Arrays.copyOfRange(args, 11, args.length-1));;
                Material material = Material.getMaterial(args[args.length-1]);

                FactoryItem createdItem = new FactoryItem();
                createdItem.setType(item_type);
                createdItem.setSubType(item_subType);
                createdItem.setAttackDamage(attackDamage);
                createdItem.setAttackRange(attackRange);
                createdItem.setAttackSpeed(attackSpeed);
                createdItem.setCriticalChance(criticalChance);
                createdItem.setSteamConsumption(steamConsumption);
                createdItem.setDurability(durability);
                createdItem.setMaxDurability(maxDurability);
                createdItem.setRarity(rarity);
                createdItem.setDisplayname(sendText(displayname));
                createdItem.setMaterial(material);
                ItemStack obtainedItem = createdItem.build();*/
                FactoryItem obtainedItem = new FactoryItem();
                ((Player)sender).getInventory().addItem(obtainedItem.build());
                player.sendMessage(sendText("&aCreated a stock item! modify the stats using &2/setitemstats <key> <value>"));

            }
            else if (args[0].equalsIgnoreCase("setitemstats")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                ItemStack item = player.getInventory().getItemInMainHand();
                assert item.getType() != Material.AIR;
                String statsKey = args[1];
                String keyValue = args[2];
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();

                if (!container.has(GetNamespacedKey(itemKey))){
                    player.sendMessage(sendText("&4This item is not a factory item, try a different one!"));
                    return false;
                }

                if (container.has(GetNamespacedKey(statsKey), PersistentDataType.INTEGER)) {
                    container.set(GetNamespacedKey(statsKey), PersistentDataType.INTEGER, Integer.parseInt(keyValue));
                } else if (container.has(GetNamespacedKey(statsKey), PersistentDataType.STRING)) {
                    container.set(GetNamespacedKey(statsKey), PersistentDataType.STRING, keyValue);
                } else if (container.has(GetNamespacedKey(statsKey), PersistentDataType.DOUBLE)) {
                    container.set(GetNamespacedKey(statsKey), PersistentDataType.DOUBLE, Double.parseDouble(keyValue));
                } else if (container.has(GetNamespacedKey(statsKey), PersistentDataType.BYTE)) {
                    container.set(GetNamespacedKey(statsKey), PersistentDataType.BYTE, Byte.parseByte(keyValue));
                }
                else if (container.has(GetNamespacedKey(statsKey), PersistentDataType.LONG)) {
                    container.set(GetNamespacedKey(statsKey), PersistentDataType.LONG, Long.parseLong(keyValue));
                }else {
                    player.sendMessage(sendText("&4Invalid data type!"));
                    return false;
                }
                item.setItemMeta(meta);

                //player.getInventory().getItemInMainHand().setItemMeta(UpdateMachineItem(item).getItemMeta());

                UpdateItem(player, "hand", item);
                player.sendMessage(sendText("&aSet &2"+statsKey+" &ato &6"+keyValue));
            }
            else if (args[0].equalsIgnoreCase("testitem")){
                FactoryItem item = new FactoryItem();
                List<ItemStack> testItemList = Arrays.asList(

                            GetDungeonLoot(Dungeon.LootType.Weapon, 1),
                            GetDungeonLoot(Dungeon.LootType.Equipment, 1)

                        );

                for (ItemStack addedItem : testItemList){
                    ((Player) sender).getInventory().addItem(addedItem);
                }

                FactoryItem hook = new FactoryItem();
                hook.setType(Type.Tool);
                hook.setSubType(SubType.Hook);
                hook.setToolPower(1);
                hook.setToolSpeed(1);
                hook.setDisplayname(sendText("&aHook"));
                hook.setMaterial(Material.FISHING_ROD);
                ((Player) sender).getInventory().addItem(hook.build());
            }
            else if (args[0].equalsIgnoreCase("viewattributes")){
                Player player = (Player) sender;
                events.ViewAttributes(player, args[1], args[2]);
            }
            else if (args[0].equalsIgnoreCase("reload")){
                LoadAllData();
                sender.sendMessage(sendText("&aConfiguration Reloaded from yml!"));
            }

            else if (args[0].equalsIgnoreCase("save-all-config")){
                SaveAllData();
                LoadAllData();
                sender.sendMessage(sendText("&aConfiguration Saved to yml, and loaded to server!"));
            }
            else if (args[0].equalsIgnoreCase("saveitem")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                String name = args[1];
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR){
                    player.sendMessage(sendText("&4Cannot save air type!"));
                    return false;
                }
                SaveItem(name, item);
                sender.sendMessage(sendText("&aSaved item with name: &2"+name));
            }
            else if (args[0].equalsIgnoreCase("savelocation")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                String name = args[1];
                Location location = player.getLocation();

                SaveLocation(name, location);
                sender.sendMessage(sendText("&aSaved location with name: &2"+name+" &aat: &6"+location.toString()));
            }
            else if (args[0].equalsIgnoreCase("savespawner")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                String name = args[1];
                Location location = player.getLocation();

                SaveSpawner(name, location);
                sender.sendMessage(sendText("&aSaved spawner with name: &2"+name+" &aat: &6"+location.toString()));
            }

            else if (args[0].equalsIgnoreCase("removelocation")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                String name = args[1];
                RemoveLocation(name);
                sender.sendMessage(sendText("&aRemoved location with name: &2"+name));
            }
            else if (args[0].equalsIgnoreCase("removespawner")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                String name = args[1];
                RemoveSpawner(name);
                sender.sendMessage(sendText("&aRemoved spawner with name: &2"+name));
            }

            else if (args[0].equalsIgnoreCase("loaditem")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                String name = args[1];
                ItemStack item = GetItem(name);
                player.getInventory().addItem(item);
                sender.sendMessage(sendText("&aLoaded item with name: &2"+name));
            }

            else if (args[0].equalsIgnoreCase("tplocation")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                String name = args[1];
                Location location = GetLocation(name);
                assert location != null;
                player.teleport(location);
                sender.sendMessage(sendText("&aTeleported to: &2"+name));
            }
            else if (args[0].equalsIgnoreCase("tpspawner")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                String name = args[1];
                Location location = GetSpawner(name);
                assert location != null;
                player.teleport(location);
                sender.sendMessage(sendText("&aTeleported to: &2"+name));
            }

            else if (args[0].equalsIgnoreCase("removeitem")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                String name = args[1];
                RemoveItem(name);
                player.sendMessage(sendText("&aRemoved item named: &2"+name));
            }

            else if (args[0].equalsIgnoreCase("checkdatabase")){
                if (plugin.sqLiteDatabase.isConnected()) {
                    sender.sendMessage("§aDatabase is connected!");
                } else {
                    sender.sendMessage("§cDatabase connection failed!");
                }
                return true;
            }

            else if (args[0].equalsIgnoreCase("save-data")){
                //SaveAllData();

                SaveAllProgress();
                StartMachineBehaviour();
                sender.sendMessage(sendText("&aSaved all data to &6database.db"));
            }
            /*else if (args[0].equalsIgnoreCase("load-data")){+
                LoadData();
                sender.sendMessage(sendText("&aLoaded all data from &6database.db"));
            }*/

            else if (args[0].equalsIgnoreCase("setmachinestats")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                ItemStack item = new ItemStack(player.getInventory().getItemInMainHand());
                assert item.getType() != Material.AIR;
                String statsKey = args[1];
                String keyValue = args[2];
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();

                if (!container.has(GetNamespacedKey(machineKey))){
                    player.sendMessage(sendText("&4This item is not a machine, try a different one!"));
                    return false;
                }

                if (container.has(GetNamespacedKey(statsKey), PersistentDataType.INTEGER)) {
                    container.set(GetNamespacedKey(statsKey), PersistentDataType.INTEGER, Integer.parseInt(keyValue));
                } else if (container.has(GetNamespacedKey(statsKey), PersistentDataType.STRING)) {
                    container.set(GetNamespacedKey(statsKey), PersistentDataType.STRING, keyValue);
                } else if (container.has(GetNamespacedKey(statsKey), PersistentDataType.DOUBLE)) {
                    container.set(GetNamespacedKey(statsKey), PersistentDataType.DOUBLE, Double.parseDouble(keyValue));
                } else if (container.has(GetNamespacedKey(statsKey), PersistentDataType.BYTE)) {
                    container.set(GetNamespacedKey(statsKey), PersistentDataType.BYTE, Byte.parseByte(keyValue));
                }
                else if (container.has(GetNamespacedKey(statsKey), PersistentDataType.LONG)) {
                    container.set(GetNamespacedKey(statsKey), PersistentDataType.LONG, Long.parseLong(keyValue));
                }else {
                    player.sendMessage(sendText("&4Invalid data type!"));
                    return false;
                }
                item.setItemMeta(meta);

                player.getInventory().getItemInMainHand().setItemMeta(UpdateMachineItem(item).getItemMeta());

                player.sendMessage(sendText("&aSet &2"+statsKey+" &ato &6"+keyValue));
            }

            else if (args[0].equalsIgnoreCase("refundmachine")){
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                events.RefundMachines((Player) target);
            }

            else if (args[0].equalsIgnoreCase("machinedatabase")){
                OpenMachineDatabase((Player) sender, "machine");
            }

            else if (args[0].equalsIgnoreCase("checkitem")){
                Player player = (Player) sender;
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() != Material.AIR){
                    ItemMeta meta = item.getItemMeta();
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    if (container.has(GetNamespacedKey(itemKey), PersistentDataType.BOOLEAN)){
                        player.sendMessage(sendText("&aThis is a valid Factory item!"));
                        return true;
                    }
                    else if (container.has(GetNamespacedKey(machineKey), PersistentDataType.BOOLEAN)){
                        player.sendMessage(sendText("&aThis is a valid Machine item!"));
                        return true;
                    }
                    else if (!container.has(GetNamespacedKey(itemKey), PersistentDataType.BOOLEAN)
                    && !container.has(GetNamespacedKey(machineKey), PersistentDataType.BOOLEAN)){
                        player.sendMessage(sendText("&eThis is a vanilla item!"));
                        return true;
                    }
                }
            }

            else if (args[0].equalsIgnoreCase("setevent")){
                SetEvent(FactoryEvents.EventType.parseEvent(args[1]));
            }

            else if (args[0].equalsIgnoreCase("inspect")){
                assert sender instanceof Player;
                InspectMachine((Player) sender);
            }

            else if (args[0].equalsIgnoreCase("setsteam")){
                Player player = Bukkit.getPlayer(args[1]);
                int amount = Integer.parseInt(args[2]);
                assert player != null;
                SetSteam(player, amount);
            }

            else if (args[0].equalsIgnoreCase("init")){
                InitFactoryItems();
                String msg = "&aAll things has been initialized!";
                sender.sendMessage(sendText(msg));
                consoleLog(sendText(msg));
            }

            else if (args[0].equalsIgnoreCase("setlevel")){
                Player player = Bukkit.getPlayer(args[1]);
                int amount = Integer.parseInt(args[2]);
                SetLevel(player, amount);
            }

            else if (args[0].equalsIgnoreCase("addlevel")){
                Player player = Bukkit.getPlayer(args[1]);
                int amount = Integer.parseInt(args[2]);
                AddLevel(player, amount);
            }
            else if (args[0].equalsIgnoreCase("removelevel")){
                Player player = Bukkit.getPlayer(args[1]);
                int amount = Integer.parseInt(args[2]);
                RemoveLevel(player, amount);
            }

            else if (args[0].equalsIgnoreCase("setprestige")){
                Player player = Bukkit.getPlayer(args[1]);
                int amount = Integer.parseInt(args[2]);
                SetPrestige(player, amount);
            }

            // level
            else if (args[0].equalsIgnoreCase("setexp")){
                Player player = Bukkit.getPlayer(args[1]);
                int amount = Integer.parseInt(args[2]);
                assert player != null;
                SetExp(player, amount);
            }
            else if (args[0].equalsIgnoreCase("addexp")){
                Player player = Bukkit.getPlayer(args[1]);
                int amount = Integer.parseInt(args[2]);
                assert player != null;
                AddExp(player, amount, 0);
            }
            else if (args[0].equalsIgnoreCase("removeexp")){
                Player player = Bukkit.getPlayer(args[1]);
                int amount = Integer.parseInt(args[2]);
                assert player != null;
                RemoveExp(player, amount);
            }
            // maxmachine
            else if (args[0].equalsIgnoreCase("setmaxmachine")){
                Player player = Bukkit.getPlayer(args[1]);
                int amount = Integer.parseInt(args[2]);
                SetMaxMachine(player, amount);
            }
            else if (args[0].equalsIgnoreCase("addmaxmachine")){
                Player player = Bukkit.getPlayer(args[1]);
                int amount = Integer.parseInt(args[2]);
                assert player != null;
                AddMaxMachine(player, amount);
            }
            else if (args[0].equalsIgnoreCase("removemaxmachine")){
                Player player = Bukkit.getPlayer(args[1]);
                int amount = Integer.parseInt(args[2]);
                assert player != null;
                RemoveMaxMachine(player, amount);
            }

            else if (args[0].equalsIgnoreCase("testbooster")){
                assert sender instanceof Player;
                ApplyBooster((Player) sender, Booster.BoosterType._5_Percent_Sell_Bonus, 3);
            }

            else if (args[0].equalsIgnoreCase("resetbooster")){
                Player target = Bukkit.getPlayer(args[1]);

                assert target != null;
                ResetBooster(target);
            }

            else if (args[0].equalsIgnoreCase("viewbooster")){
                assert sender instanceof Player;
                sender.sendMessage(getFormattedRemainingBooster((Player) sender));
                sender.sendMessage(" Booster: "+boosters.get((((Player) sender).getUniqueId())));
            }

            // multiplier
            else if (args[0].equalsIgnoreCase("setsellmultiplier")){
                Player player = Bukkit.getPlayer(args[1]);
                double amount = Double.parseDouble(args[2]);
                SetPlayerSellMultiplier(player, amount);
            }
            else if (args[0].equalsIgnoreCase("setexpmultiplier")){
                Player player = Bukkit.getPlayer(args[1]);
                double amount = Double.parseDouble(args[2]);
                SetPlayerExpMultiplier(player, amount);
            }

            else if (args[0].equalsIgnoreCase("deleteunderscorekey")){
                /*List<String> keyList = itemList.keySet().stream().toList();
                for (String key : keyList){
                    if (key.contains("_")){
                        itemList.remove(key);
                        sender.sendMessage(sendText("&aRemoved: &2"+key));
                    }
                }*/

                List<String> materialList = Arrays.asList(
                        "wheat", "barley", "corn", "carrot", "potato", "beetroot", "whiteonion", "redonion",
                        "lettuce", "cabbage", "broccoli", "cauliflower", "radish", "cucumber", "greenbeans",
                        "eggplant", "chilipepper", "apple", "banana", "orange", "grape", "melon", "pumpkin",
                        "strawberry", "blueberry", "blackberry", "kiwi", "lemon", "peach", "papaya", "pineapple",
                        "mango", "purplemushroom", "redmushroom", "bluemushroom", "brownmushroom", "greenmushroom",
                        "pinkmushroom", "yellowmushroom", "cherrylog", "birchlog", "crimsonstem", "junglelog",
                        "mangrovelog", "sprucelog", "warpedstem", "oaklog", "acacialog", "darkoaklog", "oakleaves",
                        "pinkleaves", "purpleleaves", "redleaves", "wisterialeaves", "fallleaves", "autumnleaves",
                        "azalealeaves", "acacialeaves", "spruceleaves", "sakuraleaves", "birchleaves", "cherryleaves",
                        "jungleleaves", "mangroveleaves"
                );

                List<String> eType = Arrays.asList("helmet", "chestplate", "leggings", "boots");

                for (String type : eType){
                    for (String mat : materialList){
                        itemList.remove(mat+type);
                        sender.sendMessage(sendText("&aRemoved: &2"+mat+type));
                    }
                }

            }

            else if (args[0].equalsIgnoreCase("refreshmachinetag")){
                RefreshMachineTag(args[1], args[2]);
                if (args[1].equals("all")){
                    sender.sendMessage(sendText("&aRefreshed all machine's tags!"));
                }else{
                    sender.sendMessage(sendText("&aRefreshed all machine's tags of &2"+args[2]+"!"));
                }
            }

            else if (args[0].equalsIgnoreCase("givereward")){
                Player target = Bukkit.getPlayer(args[1]);
                RewardsManager.RewardType type = RewardsManager.RewardType.parseReward(args[2]);
                if (type == null){
                    sender.sendMessage(sendText("&c"+args[2]+" &4Reward Type is not exist!"));
                    return false;
                }
                GiveReward(target, type);
                sender.sendMessage(sendText("&aGived &2"+type+" &aReward to &2"+target.getName()));
            }

            else if (args[0].equalsIgnoreCase("getbackpack")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                player.getInventory().addItem(CreateBackpack(Integer.parseInt(args[1])));
                player.sendMessage(sendText("&aObtained backpack with size: &2"+Integer.parseInt(args[1])));
            }

            else if (args[0].equalsIgnoreCase("testtrader")){
                assert sender instanceof Player;
                //OpenCarbonForge((Player) sender);
                OpenTrader((Player) sender, TraderManager.TraderType.Dungeon_Loot_Box);
            }
            else if (args[0].equalsIgnoreCase("testtrader2")){
                assert sender instanceof Player;
                //OpenCarbonForge((Player) sender);
                OpenTrader((Player) sender, TraderManager.TraderType.Dungeon_Junk_Collector);
            }

            else if (args[0].equalsIgnoreCase("resetcooldown")){
                Player target = Bukkit.getPlayer(args[1]);
                assert target != null;
                String key = args[2];
                CooldownManager.CooldownType cooldownType = CooldownManager.CooldownType.parseCooldown(key);
                if (cooldownType == CooldownManager.CooldownType.None){
                    sender.sendMessage(sendText("&c"+key+" &4is not a valid Cooldowns key!"));
                    return false;
                }

                ResetCooldown(target, CooldownManager.CooldownType.parseCooldown(key));
                sender.sendMessage(sendText("&aCooldowns of &2"+target.getName()+" &a(&b"+key+"&a) &ahas been reset!"));
            }

            else if (args[0].equalsIgnoreCase("testmob")){
                assert sender instanceof Player;
                SpawnMob(((Player) sender).getLocation());
            }
            else if (args[0].equalsIgnoreCase("testregion")){
                assert sender instanceof Player;
                GetPlayerRegion(((Player) sender));
            }
            else if (args[0].equalsIgnoreCase("completequest")){
                assert sender instanceof Player;
                CompleteQuest((Player) sender, quest.get(sender));
            }
            else if (args[0].equalsIgnoreCase("forcecompletequest")){
                assert sender instanceof Player;
                ForceCompleteQuest((Player) sender);
            }
            else if (args[0].equalsIgnoreCase("announcepayment")){
                String paymentName = String.join(" ", Arrays.copyOfRange(args, 2, args.length-1));
                String price = args[args.length-1];
                AnnouncePayment(Objects.requireNonNull(Bukkit.getPlayer(args[1])), paymentName, price);
            }
            else if (args[0].equalsIgnoreCase("giveitem")){
                Player target = Bukkit.getPlayer(args[1]);
                ItemStack key = GetItem(args[2]);
                assert target != null;
                Map<Integer, ItemStack> addedItem = target.getInventory().addItem(key);
                if (!addedItem.isEmpty()){
                    Inventory backupChest = OpenChest(target, 6, "Unclaimed Item");
                    backupChest.setItem(0, key);

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        target.closeInventory();
                        target.openInventory(backupChest);
                    }, 5L);

                    //DropItem(target.getLocation(), key, 1);
                    target.sendMessage(sendText("&4Your inventory is full, purchased item stored in backup chest! &6(don't close it before take the item or the items you've purchased will be lost!)"));
                }
            }
            else if (args[0].equalsIgnoreCase("rollevents")){
                RollEvents();
            }
            else if (args[0].equalsIgnoreCase("performprestige")){
                assert sender instanceof Player;
                PerformPrestige((Player) sender);
            }
            else if (args[0].equalsIgnoreCase("clearmobs")){
                World world = null;
                if (sender instanceof Player player){
                    if (args.length == 1){
                        world = player.getWorld();
                    }
                }else{
                    if (args.length > 1){
                        world = Bukkit.getWorld(args[1]);
                    }
                }

                if (world == null){
                    sender.sendMessage(sendText("&c"+args[1]+" &4is not a valid world!"));
                    return false;
                }

                sender.sendMessage(sendText("Cleared &2"+ClearMobs(world)+" &aEntities!"));
            }
            else if (args[0].equalsIgnoreCase("openmenu")){
                String menuName = args[2];
                Player target = Bukkit.getPlayer(args[1]);
                assert target != null;
                OpenMenu(target, MenuList.parseMenu(menuName));
            }
            else if (args[0].equalsIgnoreCase("testmenu")){
                assert sender instanceof Player;
                OpenNetherSmelter((Player) sender);
            }
        }
        else if (command.getName().equalsIgnoreCase("refundmachine")){
            events.RefundMachines(((Player) sender));
        }
        else if (command.getName().equalsIgnoreCase("multiblock")){
            ObtainMultiBlockGuide((Player) sender);
        }
        else if (command.getName().equalsIgnoreCase("shop")){
            Player player = (Player) sender;
            OpenMenu(player, GUIManager.MenuList.Shop);
        }
        else if (command.getName().equalsIgnoreCase("viewmaxexp")){
            ViewMaxExp();
        }
        else if (command.getName().equalsIgnoreCase("abandonquest")){
            assert sender instanceof Player;
            AbandonQuest((Player)sender);
        }
        else if (command.getName().equalsIgnoreCase("quest")){
            assert sender instanceof Player;
            OpenMenu((Player) sender, MenuList.Quest);
        }
        else if (command.getName().equalsIgnoreCase("trash")){
            assert sender instanceof Player;
            OpenMenu((Player) sender, MenuList.Trash);
        }


        else if (command.getName().equalsIgnoreCase("spawn")){
            Player player = (Player) sender;
            Location spawnLocation = GetLocation("spawn");
            if (spawnLocation != null){
                //player.sendTitle(sendRgbText("Teleported", color_darkGreenAcid), sendRgbText("You've been teleported to &bSpawn", color_brightGreenAcid));
                //player.sendMessage(sendRgbText("You've been teleported to &bSpawn", "#6EDA10"));
                player.teleport(spawnLocation);
                //Bukkit.getScheduler().runTaskLater(plugin, () -> {
                PlaySound(Sound.ENTITY_ENDERMAN_TELEPORT, player, 1, 1);
                //}, 20L);
            }
        }


        else if (command.getName().equalsIgnoreCase("sellall")){
            Player player = (Player) sender;
            SellAll(player);
        }

        else if (command.getName().equalsIgnoreCase("sellgui")){
            Player player = (Player) sender;
            OpenSellGui(player);
        }

        else if (command.getName().equalsIgnoreCase("warp")){
            Player player = (Player) sender;
            OpenMenu(player, MenuList.WarpMenu);
        }

        else if (command.getName().equalsIgnoreCase("profile")){
            Player player = (Player) sender;
            OpenMenu(player, MenuList.Profile);
        }

        else if (command.getName().equalsIgnoreCase("hazmat")){
            GiveReward((Player) sender, RewardsManager.RewardType.Hazmat);
        }

        else if (command.getName().equalsIgnoreCase("rewards")){
            OpenMenu((Player) sender, MenuList.Rewards);
        }

        else if (command.getName().equalsIgnoreCase("sell")){
            OpenMenu((Player) sender, MenuList.Sell);
        }

        else if (command.getName().equalsIgnoreCase("prestige")){
            OpenMenu((Player) sender, MenuList.Prestige);
        }

        else if (command.getName().equalsIgnoreCase("catalog")){
            Player player = (Player) sender;
            int page = 1;
            if (args.length == 1){
                page = Integer.parseInt(args[0]);
            }
            if (page >= maxCatalogPage){
                sender.sendMessage(sendText("&4Max Catalog Page &c"+maxCatalogPage));
                return false;
            }
            OpenCatalog(player, page);
            menuPage.put(player, page);
        }
        return false;
    }

    /*void SaveData(){
        getMainPlugin().sqLiteDatabase.SaveMachineData(placedMachines);
        getMainPlugin().sqLiteDatabase.SaveMachineItems(machineItems);
        consoleLog(sendText("&aSaved SQL Data!"));
    }

    void LoadData(){
        getMainPlugin().sqLiteDatabase.LoadMachineData(plugin.sqLiteDatabase.getConnection());
        getMainPlugin().sqLiteDatabase.LoadMachineItems(plugin.sqLiteDatabase.getConnection());
        consoleLog(sendText("&aLoaded SQL Data!"));
    }*/

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("factoryutils")){
            if (args.length == 1){
                List<String> argsList = new ArrayList<>();
                argsList.add("setmachinespeed");
                argsList.add("createmachine");
                argsList.add("createitem");
                argsList.add("setitemstats");
                argsList.add("setmachinestats");
                argsList.add("machinedatabase");
                argsList.add("testitem");
                argsList.add("saveitem");
                argsList.add("loaditem");
                argsList.add("checkitem");
                argsList.add("savelocation");
                argsList.add("savespawner");
                argsList.add("tplocation");
                argsList.add("tpspawner");
                argsList.add("removeitem");
                argsList.add("init");
                argsList.add("reload");
                argsList.add("save-all-config");
                argsList.add("save-data");
                argsList.add("load-data");
                argsList.add("viewattributes");
                argsList.add("refundmachine");
                argsList.add("refreshmachinetag");
                argsList.add("getbackpack");
                argsList.add("givereward");
                argsList.add("setsteam");
                argsList.add("removesteam");
                argsList.add("addsteam");
                argsList.add("openmenu");

                argsList.add("setprestige");

                argsList.add("setlevel");
                argsList.add("addlevel");
                argsList.add("removelevel");

                argsList.add("removelocation");
                argsList.add("removespawner");

                argsList.add("setmaxmachine");
                argsList.add("addmaxmachine");
                argsList.add("removemaxmachine");

                argsList.add("setsellmultiplier");
                argsList.add("setexpmultiplier");
                argsList.add("setevent");

                argsList.add("resetcooldown");
                argsList.add("resetbooster");

                argsList.add("setexp");
                argsList.add("addexp");
                argsList.add("removeexp");

                argsList.add("testquest");
                argsList.add("completequest");
                argsList.add("forcecompletequest");

                argsList.add("clearmobs");

                String partialInput = args[0].toLowerCase();
                for (String key : argsList) {
                    if (key.toLowerCase().startsWith(partialInput)) {
                        suggestions.add(key);
                    }
                }
            }

            else if (args.length == 2){
                String partialInput = args[1].toLowerCase();
                if (args[0].equalsIgnoreCase("modifymachine")){
                    List<String> argsList = new ArrayList<>();
                    argsList.add("string");
                    argsList.add("integer");
                    argsList.add("double");
                    argsList.add("long");

                    for (String key : argsList) {
                        if (key.toLowerCase().startsWith(partialInput)) {
                            suggestions.add(key);
                        }
                    }
                }

                else if (args[0].equalsIgnoreCase("loaditem")
                || args[0].equalsIgnoreCase("removeitem")){
                    for (String key : itemList.keySet()) {
                        if (key.toLowerCase().startsWith(partialInput)) {
                            suggestions.add(key);
                        }
                    }
                }

                else if (  args[0].equalsIgnoreCase("setlevel")
                        || args[0].equalsIgnoreCase("addlevel")
                        || args[0].equalsIgnoreCase("removelevel")

                        || args[0].equalsIgnoreCase("setsteam")
                        || args[0].equalsIgnoreCase("addsteam")
                        || args[0].equalsIgnoreCase("removesteam")
                        || args[0].equalsIgnoreCase("givereward")

                        || args[0].equalsIgnoreCase("setprestige")

                        || args[0].equalsIgnoreCase("setexp")
                        || args[0].equalsIgnoreCase("addexp")
                        || args[0].equalsIgnoreCase("removeexp")

                        || args[0].equalsIgnoreCase("setmaxmachine")
                        || args[0].equalsIgnoreCase("addmaxmachine")
                        || args[0].equalsIgnoreCase("removemaxmachine")

                        || args[0].equalsIgnoreCase("resetcooldown")

                        || args[0].equalsIgnoreCase("openmenu")

                        || args[0].equalsIgnoreCase("resetbooster")){

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.getName().toLowerCase().startsWith(partialInput)) {
                            suggestions.add(onlinePlayer.getName());
                        }
                    }
                }

                else if (args[0].equalsIgnoreCase("tplocation") || args[0].equalsIgnoreCase("removelocation")){
                    for (String key : locationList.keySet()) {
                        if (key.toLowerCase().startsWith(partialInput)) {
                            suggestions.add(key);
                        }
                    }
                }
                else if (args[0].equalsIgnoreCase("tpspawner") || args[0].equalsIgnoreCase("removespawner")){
                    for (String key : spawnerList.keySet()) {
                        if (key.toLowerCase().startsWith(partialInput)) {
                            suggestions.add(key);
                        }
                    }
                }

            }
            else if (args.length == 3){
                if (args[0].equalsIgnoreCase("givereward")){
                    List<String> argsList = new ArrayList<>();
                    argsList.add("starter");
                    String partialInput = args[2].toLowerCase();
                    for (String key : argsList) {
                        if (key.toLowerCase().startsWith(partialInput)) {
                            suggestions.add(key);
                        }
                    }
                }
            }
        }

        return suggestions;
    }
}
