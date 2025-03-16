package org.factory.factory;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.factory.factory.Utils.FactoryItem;
import org.factory.factory.Utils.GUIManager;
import org.factory.factory.Utils.Rarity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.factory.factory.Database.*;
import static org.factory.factory.Events.SetSteam;
import static org.factory.factory.Events.UpdateItem;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.Utils.FactoryItem.baseKey;
import static org.factory.factory.Utils.FactoryItem.itemKey;
import static org.factory.factory.Utils.FactoryMachine.*;
import static org.factory.factory.Utils.GUIManager.OpenMenu;
import static org.factory.factory.Utils.PersistentDataManager.GetNamespacedKey;
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
        if (command.getName().equalsIgnoreCase("factory")){

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
                        material, drop, potentialDrop, rarity, "Active", 0, MachineType.parseType(machineType), steamProduction);
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
                Player player = (Player) sender;
                ItemStack item = player.getInventory().getItemInMainHand();
                assert item.getType() != Material.AIR;
                String statsKey = args[2];
                String value = args[3];

                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();

                if (!container.has(GetNamespacedKey(itemKey))){
                    player.sendMessage(sendText("&4This item is not a factory item, try a different one!"));
                    return false;
                }

                String baseText = baseKey;
                if (!args[1].equals("base")){
                    baseText = "";
                }

                if (container.get(GetNamespacedKey(baseText+statsKey), PersistentDataType.DOUBLE) != null){
                    container.set(GetNamespacedKey(baseText+statsKey), PersistentDataType.DOUBLE, Double.parseDouble(value));
                }
                else if (container.get(GetNamespacedKey(statsKey), PersistentDataType.STRING) != null){
                    container.set(GetNamespacedKey(statsKey), PersistentDataType.STRING, value);
                }
                player.sendMessage(sendText("&aSet &2"+statsKey+" &ato &6"+value));
                item.setItemMeta(meta);
                UpdateItem(player, "hand");
            }
            else if (args[0].equalsIgnoreCase("testitem")){
                FactoryItem item = new FactoryItem();
                ((Player)sender).getInventory().addItem(item.testItem());
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
                SaveData();
                sender.sendMessage(sendText("&aSaved all data to &6database.db"));
            }
            else if (args[0].equalsIgnoreCase("load-data")){
                LoadData();
                sender.sendMessage(sendText("&aLoaded all data from &6database.db"));
            }

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

            else if (args[0].equalsIgnoreCase("setsteam")){
                Player player = Bukkit.getPlayer(args[1]);
                int amount = Integer.parseInt(args[2]);
                assert player != null;
                SetSteam(player, amount);
            }

        }
        else if (command.getName().equalsIgnoreCase("refundmachine")){
            events.RefundMachines(((Player) sender));
        }
        else if (command.getName().equalsIgnoreCase("shop")){
            Player player = (Player) sender;
            OpenMenu(player, GUIManager.MenuList.Shop);
        }
        else if (command.getName().equalsIgnoreCase("spawn")){
            Player player = (Player) sender;
            Location spawnLocation = GetLocation("spawn");
            if (spawnLocation != null){
                player.sendTitle(sendRgbText("Teleported", color_darkGreenAcid), sendRgbText("You've been teleported to &bSpawn", color_brightGreenAcid));
                player.sendMessage(sendRgbText("You've been teleported to &bSpawn", "#6EDA10"));
                player.teleport(spawnLocation);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    PlaySound(Sound.ENTITY_ENDERMAN_TELEPORT, player, 1, 1);
                }, 20L);
            }
        }
        return false;
    }

    void SaveData(){
        plugin.sqLiteDatabase.SaveMachineData(plugin.events.placedMachines);
        plugin.sqLiteDatabase.SaveMachineItems(plugin.events.machineItems);
        consoleLog(sendText("&aSaved SQL Data!"));
    }

    void LoadData(){
        plugin.sqLiteDatabase.LoadMachineData(plugin.sqLiteDatabase.getConnection());
        plugin.sqLiteDatabase.LoadMachineItems(plugin.sqLiteDatabase.getConnection());
        consoleLog(sendText("&aLoaded SQL Data!"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("factory")){
            if (args.length == 1){
                List<String> argsList = new ArrayList<>();
                argsList.add("setmachinespeed");
                argsList.add("createmachine");
                argsList.add("createitem");
                argsList.add("setitemstats");
                argsList.add("setmachinestats");
                argsList.add("testitem");
                argsList.add("saveitem");
                argsList.add("loaditem");
                argsList.add("checkitem");
                argsList.add("savelocation");
                argsList.add("tplocation");
                argsList.add("removeitem");
                argsList.add("reload");
                argsList.add("save-all-config");
                argsList.add("save-data");
                argsList.add("load-data");
                argsList.add("viewattributes");
                argsList.add("refundmachine");
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
            }
        }

        return suggestions;
    }
}
