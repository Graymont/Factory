package org.factory.factory;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.factory.factory.Utils.UserInterface.consoleLog;
import static org.factory.factory.Utils.UserInterface.sendText;

public class Database {

    public static String pluginName = "Factory";

    static String databasePath = "plugins/"+pluginName+"/Database";
    static String developmentPath = databasePath+"/Development/";
    static String developmentDataFile = "Development Data.yml";
    static String developmentItemFile = "Development Item.yml";

    static Events events;
    static Factory plugin;

    public static HashMap<String, Double> priceList = new HashMap<>();
    public static HashMap<String, Double> worthList = new HashMap<>();
    public static HashMap<String, ItemStack> itemList = new HashMap<>();

    public static HashMap<String, List<ItemStack>> shopItemList = new HashMap<>();

    public Database (Events e, Factory pl){
        events = e;
        plugin = pl;
    }

    public static ItemStack GetItem(String name){
        return itemList.get(name).clone();
    }

    public static Double GetPrice(String name){
        return priceList.get(name);
    }

    public static Double GetWorth(String name){
        return worthList.get(name);
    }

    public static void SetPrice(String name, double price){
        priceList.put(name, price);
        consoleLog(sendText("&bSaved Price: &6"+name+" &b(&eprice"+price+"&b)"));
    }

    public static void SetWorth(String name, double price){
        worthList.put(name, price);
        consoleLog(sendText("&bSaved Worth: &6"+name+" &b(&eprice"+price+"&b)"));
    }

    public static void SaveItem(String name, ItemStack item){
        itemList.put(name, item.clone());
        consoleLog(sendText("&bSaved Items with name: &6"+name));
    }


    public static void RemovePrice(String name){
        priceList.remove(name);
        consoleLog(sendText("&bRemoved Price: &6"+name));
    }

    public static void RemoveWorth(String name){
        worthList.remove(name);
        consoleLog(sendText("&bRemoved Worth: &6"+name));
    }

    public static void RemoveItem(String name){
        itemList.remove(name);
        consoleLog(sendText("&bRemoved Item with name: &6"+name));
    }

    static void SavePrices(){
        File file = new File(developmentPath, developmentDataFile);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "prices.";
        for (String key : priceList.keySet()){
            config.set(parent+key, priceList.get(key));
        }
        try{
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void LoadPrices() {
        File file = new File(developmentPath, developmentDataFile);

        if (!file.exists()) {
            consoleLog(sendText("&3[Factory] &ePrices file does not exist. No data loaded."));
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "prices";
        String section = parent+".";

        if (config.isConfigurationSection(parent)) {
            Set<String> keys = config.getConfigurationSection(parent).getKeys(false);
            for (String key : keys) {
                priceList.put(key, config.getDouble(section + key));
            }
            consoleLog(sendText("&3[Factory] &aPrices loaded successfully!"));
        } else {
            consoleLog(sendText("&3[Factory] &cNo prices section found in file."));
        }
    }

    static void SaveWorths(){
        File file = new File(developmentPath, developmentDataFile);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "worths.";
        for (String key : worthList.keySet()){
            config.set(parent+key, worthList.get(key));
        }
        try{
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void LoadWorths() {
        File file = new File(developmentPath, developmentDataFile);

        if (!file.exists()) {
            consoleLog(sendText("&3[Factory] &eWorths file does not exist. No data loaded."));
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "worths";
        String section = parent+".";

        if (config.isConfigurationSection(parent)) {
            Set<String> keys = config.getConfigurationSection(parent).getKeys(false);
            for (String key : keys) {
                worthList.put(key, config.getDouble(section + key));
            }
            consoleLog(sendText("&3[Factory] &aPrices loaded successfully!"));
        } else {
            consoleLog(sendText("&3[Factory] &cNo prices section found in file."));
        }
    }

    static void LoadShopItems() {
        File file = new File(developmentPath, developmentDataFile);

        if (!file.exists()) {
            consoleLog(sendText("&3[Factory] &eShop Items file does not exist. No data loaded."));
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String parent = "farm_items";
        if (config.isList(parent)) {

            List<String> keys = config.getStringList(parent);
            List<ItemStack> _iList = new ArrayList<>();
            for (String key : keys) {
                Material material = Material.getMaterial(key);
                if (material != null) {
                    _iList.add(new ItemStack(material));
                } else {
                    consoleLog(sendText("&3[Factory] &cInvalid material: " + key));
                }
            }
            shopItemList.put(parent, _iList);
            consoleLog(sendText("&3[Factory] &aShop Items category " + parent + " loaded successfully!"));
        } else {
            consoleLog(sendText("&3[Factory] &cNo category " + parent + " section found in file."));
        }

        parent = "mineral_items";
        if (config.isList(parent)) {

            List<String> keys = config.getStringList(parent);
            List<ItemStack> _iList = new ArrayList<>();
            for (String key : keys) {
                Material material = Material.getMaterial(key);
                if (material != null) {
                    _iList.add(new ItemStack(material));
                } else {
                    consoleLog(sendText("&3[Factory] &cInvalid material: " + key));
                }
            }
            shopItemList.put(parent, _iList);
            consoleLog(sendText("&3[Factory] &aShop Items category " + parent + " loaded successfully!"));
        } else {
            consoleLog(sendText("&3[Factory] &cNo category " + parent + " section found in file."));
        }

        parent = "mob_items";
        if (config.isList(parent)) {

            List<String> keys = config.getStringList(parent);
            List<ItemStack> _iList = new ArrayList<>();
            for (String key : keys) {
                Material material = Material.getMaterial(key);
                if (material != null) {
                    _iList.add(new ItemStack(material));
                } else {
                    consoleLog(sendText("&3[Factory] &cInvalid material: " + key));
                }
            }
            shopItemList.put(parent, _iList);
            consoleLog(sendText("&3[Factory] &aShop Items category " + parent + " loaded successfully!"));
        } else {
            consoleLog(sendText("&3[Factory] &cNo category " + parent + " section found in file."));
        }

        parent = "block_items";
        if (config.isList(parent)) {

            List<String> keys = config.getStringList(parent);
            List<ItemStack> _iList = new ArrayList<>();
            for (String key : keys) {
                Material material = Material.getMaterial(key);
                if (material != null) {
                    _iList.add(new ItemStack(material));
                } else {
                    consoleLog(sendText("&3[Factory] &cInvalid material: " + key));
                }
            }
            shopItemList.put(parent, _iList);
            consoleLog(sendText("&3[Factory] &aShop Items category " + parent + " loaded successfully!"));
        } else {
            consoleLog(sendText("&3[Factory] &cNo category " + parent + " section found in file."));
        }
    }



    // items

    static void SaveItems(){
        File file = new File(developmentPath, developmentItemFile);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "items.";
        for (String key : itemList.keySet()){
            config.set(parent+key, itemList.get(key));
        }
        try{
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void LoadItems() {
        File file = new File(developmentPath, developmentItemFile);

        if (!file.exists()) {
            consoleLog(sendText("&3[Factory] &eItems file does not exist. No data loaded."));
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "items";
        String section = parent+".";

        if (config.isConfigurationSection(parent)) {
            Set<String> keys = config.getConfigurationSection(parent).getKeys(false);
            for (String key : keys) {
                itemList.put(key, config.getItemStack(section + key));
            }
            consoleLog(sendText("&3[Factory] &aItems loaded successfully!"));
        } else {
            consoleLog(sendText("&3[Factory] &cNo items section found in file."));
        }
    }


    public static void LoadAllData(){
        LoadPrices();
        LoadWorths();
        LoadItems();
        LoadShopItems();
        consoleLog(sendText("&3[Factory] &aLoading all configuration data..."));
    }

    public static void SaveAllData(){
        SavePrices();
        SaveWorths();
        SaveItems();
        consoleLog(sendText("&3[Factory] &aSaving all configuration data..."));
    }

}
