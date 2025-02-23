package org.factory.factory;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.factory.factory.Utils.FactoryItem;
import org.factory.factory.Utils.Rarity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.factory.factory.Database.*;
import static org.factory.factory.Utils.FactoryMachine.CreateMachine;
import static org.factory.factory.Utils.UserInterface.sendText;


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
                String machineName = String.join(" ", Arrays.copyOfRange(args, 11, args.length));
                ItemStack machine = CreateMachine(machineName, machineLevel, speed, productionRate, steamConsumption, durability, maxDurability,
                        material, drop, potentialDrop, rarity);
                player.getInventory().addItem(machine);
            }

            else if (args[0].equalsIgnoreCase("createitem")){

                if (args.length == 1){
                    Player player = (Player) sender;
                    player.sendMessage(sendText("&aCreate Item Format: Type, SubType," +
                            " AttackDamage, AttackSpeed, CriticalChance, SteamConsumption," +
                            " Durability, MaxDurability, Rarity, DisplayName, Material"));
                    return false;
                }

                FactoryItem.Type item_type = FactoryItem.Type.parseType(args[1]);
                FactoryItem.SubType item_subType = FactoryItem.SubType.parseSubType(args[2]);
                double attackDamage = Double.parseDouble(args[3]);
                double attackSpeed = Double.parseDouble(args[4]);
                double criticalChance = Double.parseDouble(args[5]);
                double steamConsumption = Double.parseDouble(args[6]);
                double durability = Double.parseDouble(args[7]);
                double maxDurability = Double.parseDouble(args[8]);
                Rarity.RarityType rarity = Rarity.RarityType.parseRarity(args[9]);
                String displayname = String.join(" ", Arrays.copyOfRange(args, 10, args.length-1));;
                Material material = Material.getMaterial(args[args.length-1]);

                FactoryItem createdItem = new FactoryItem();
                createdItem.setType(item_type);
                createdItem.setSubType(item_subType);
                createdItem.setAttackDamage(attackDamage);
                createdItem.setAttackSpeed(attackSpeed);
                createdItem.setCriticalChance(criticalChance);
                createdItem.setSteamConsumption(steamConsumption);
                createdItem.setDurability(durability);
                createdItem.setMaxDurability(maxDurability);
                createdItem.setRarity(rarity);
                createdItem.setDisplayname(sendText(displayname));
                createdItem.setMaterial(material);
                ItemStack obtainedItem = createdItem.build();

                ((Player)sender).getInventory().addItem(obtainedItem);
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

            else if (args[0].equalsIgnoreCase("loaditem")){
                assert sender instanceof Player;
                Player player = (Player) sender;
                String name = args[1];
                ItemStack item = GetItem(name);
                if (item == null){
                    player.sendMessage(sendText("&4That item is not exist!"));
                    return false;
                }
                player.getInventory().addItem(item);
                sender.sendMessage(sendText("&aLoaded item with name: &2"+name));
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

            else if (args[0].equalsIgnoreCase("modifymachine")){
                Player cSender = (Player)sender;
                String key = args[1];

                if (args[2].equals("string")){
                    events.ModifyMachine(cSender, key, args[3]);
                }
                else if (args[2].equals("integer")){
                    events.ModifyMachine(cSender, key, Integer.parseInt(args[3]));
                }
                else if (args[2].equals("double")){
                    events.ModifyMachine(cSender, key, Double.parseDouble(args[3]));
                }
                else if (args[2].equals("long")){
                    events.ModifyMachine(cSender, key, Long.parseLong(args[3]));
                }

            }

        }
        return false;
    }

    void SaveData(){
        plugin.sqLiteDatabase.SaveMachineData(plugin.events.placedMachines);
        plugin.sqLiteDatabase.SaveMachineItems(plugin.events.machineItems);
    }

    void LoadData(){
        plugin.sqLiteDatabase.LoadMachineData(plugin.sqLiteDatabase.getConnection());
        plugin.sqLiteDatabase.LoadMachineItems(plugin.sqLiteDatabase.getConnection());
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
                argsList.add("saveitem");
                argsList.add("loaditem");
                argsList.add("reload");
                argsList.add("save-all-config");
                argsList.add("save-data");
                argsList.add("load-data");
                argsList.add("modifymachine");
                argsList.add("viewattributes");
                String partialInput = args[0].toLowerCase();
                for (String key : argsList) {
                    if (key.toLowerCase().startsWith(partialInput)) {
                        suggestions.add(key);
                    }
                }
            }

            else if (args.length == 2){
                if (args[0].equalsIgnoreCase("modifymachine")){
                    List<String> argsList = new ArrayList<>();
                    argsList.add("string");
                    argsList.add("integer");
                    argsList.add("double");
                    argsList.add("long");

                    String partialInput = args[1].toLowerCase();
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
