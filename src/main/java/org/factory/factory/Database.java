package org.factory.factory;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.factory.factory.Utils.ItemSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.factory.factory.Events.globalRevision;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.GameHandler.FactoryItem.*;
import static org.factory.factory.GameHandler.FactoryMachine.machineKey;
import static org.factory.factory.GameManager.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.UserInterface.*;

public class Database {

    public static String pluginName = "Factory";

    public static String configFile = "config.yml";

    public static String directory = "plugins/"+pluginName;
    public static String databasePath = "plugins/"+pluginName+"/Database";
    public static String developmentPath = databasePath+"/Development/";
    public static String developmentDataFile = "Development Data.yml";
    public static String developmentItemFile = "Development Item.yml";
    public static String itemConfigurationFile = "Item Configuration.yml";

    static Events events;
    static Factory plugin;

    public static HashMap<String, Double> priceList = new HashMap<>();
    public static HashMap<String, Double> worthList = new HashMap<>();
    public static HashMap<String, ItemStack> itemList = new HashMap<>();

    public static HashMap<String, Location> locationList = new HashMap<>();

    public static HashMap<String, Location> spawnerList = new HashMap<>();

    public static HashMap<String, List<ItemStack>> shopItemList = new HashMap<>();
    public static HashMap<String, String> categoryList = new HashMap<>();

    public static HashMap<String, Integer> levelMinimumList = new HashMap<>();

    public Database (Events e, Factory pl){
        events = e;
        plugin = pl;
    }

    public static ItemStack GetItem(String name){
        ItemStack addedItem = new ItemStack(itemList.get(name).clone());
        if (addedItem.isEmpty()){
            consoleLog(sendText("&cItem with id: &4"+name+" &cis not exist!"));
            return new ItemStack(Material.STICK);
        }
        /*ItemMeta meta = addedItem.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(GetNamespacedKey(machineKey), PersistentDataType.BOOLEAN) &&
                !container.has(GetNamespacedKey(itemKey), PersistentDataType.BOOLEAN)){
            container.set(GetNamespacedKey(itemKey), PersistentDataType.BOOLEAN, true);
        }
        addedItem.setItemMeta(meta);*/
        return addedItem;
    }

    public static Double GetPrice(String name){
        return priceList.get(name);
    }

    public static Double GetWorth(String name){
        return worthList.get(name);
    }

    public static Location GetLocation(String name){
        return locationList.get(name);
    }

    public static Location GetSpawner(String name){
        return spawnerList.get(name);
    }

    public static Integer GetLevelMinimum(String name){
        return levelMinimumList.get(name);
    }

    public static void SetPrice(String name, double price){
        priceList.put(name, price);
        consoleLog(sendText("&bSaved Price: &6"+name+" &b(&eprice"+price+"&b)"));
    }

    public static void SetWorth(String name, double price){
        worthList.put(name, price);
        consoleLog(sendText("&bSaved Worth: &6"+name+" &b(&eprice"+price+"&b)"));
    }

    public static void SetLevelMinimum(String name, int level){
        levelMinimumList.put(name, level);
        consoleLog(sendText("&bSaved Level Requirement: &6"+name+" &b(&eprice"+level+"&b)"));
    }

    public static void SaveItem(String name, ItemStack item){
        ItemStack addedItem = new ItemStack(item.clone());
        ItemMeta meta = addedItem.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(GetNamespacedKey(machineKey), PersistentDataType.BOOLEAN)
                && !container.has(GetNamespacedKey(itemKey), PersistentDataType.BOOLEAN)){
            container.set(GetNamespacedKey(itemKey), PersistentDataType.BOOLEAN, true);
        }
        addedItem.setItemMeta(meta);
        itemList.put(name, addedItem.clone());
        consoleLog(sendText("&bSaved Items with name: &6"+name));
    }

    public static void SaveLocation(String name, Location location){
        locationList.put(name, location);
        consoleLog(sendText("&bSaved Location with name: &6"+name));
    }

    public static void SaveSpawner(String name, Location location){
        spawnerList.put(name, location);
        consoleLog(sendText("&bSaved Spawner with name: &6"+name));
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

    public static void RemoveLocation(String name){
        locationList.remove(name);
        consoleLog(sendText("&bRemoved Location with name: &6"+name));
    }

    public static void RemoveSpawner(String name){
        spawnerList.remove(name);
        consoleLog(sendText("&bRemoved Spawner with name: &6"+name));
    }

    public static void InitDefaultFile(){
        File file = new File(directory, configFile);
        if (!file.exists()) {
            getMainPlugin().saveResource(configFile, false);
            consoleLog(sendText("&eConfig file not exist, creating a new one!"));
        }
    }

    public static void LoadConfigurations(){
        File file = new File(directory, configFile);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        globalRevision = config.getInt("revision");
        consoleLog(sendText("&aGlobal Revision: "+globalRevision));
    }

    public static void SaveLocations(){
        File file = new File(developmentPath, developmentItemFile);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "locations.";
        for (String key : locationList.keySet()){
            config.set(parent+key, locationList.get(key));
        }
        try{
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void SaveSpawners(){
        File file = new File(developmentPath, developmentItemFile);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "spawners.";
        for (String key : spawnerList.keySet()){
            config.set(parent+key, spawnerList.get(key));
        }
        try{
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void LoadLocations() {
        File file = new File(developmentPath, developmentItemFile);

        if (!file.exists()) {
            consoleLog(sendText("&3[Factory Config] &eLocations file does not exist. No data loaded."));
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "locations";
        String section = parent+".";

        if (config.isConfigurationSection(parent)) {
            Set<String> keys = config.getConfigurationSection(parent).getKeys(false);
            for (String key : keys) {
                locationList.put(key, config.getLocation(section + key));
            }
            consoleLog(sendText("&3[Factory Config] &aLocations loaded successfully!"));
        } else {
            consoleLog(sendText("&3[Factory Config] &cNo locations section found in file."));
        }
    }

    public static void LoadSpawners() {
        File file = new File(developmentPath, developmentItemFile);

        if (!file.exists()) {
            consoleLog(sendText("&3[Factory Config] &eSpawners file does not exist. No data loaded."));
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "spawners";
        String section = parent+".";

        if (config.isConfigurationSection(parent)) {
            Set<String> keys = config.getConfigurationSection(parent).getKeys(false);
            for (String key : keys) {
                spawnerList.put(key, config.getLocation(section + key));
            }
            consoleLog(sendText("&3[Factory Config] &eSpawners loaded successfully!"));
        } else {
            consoleLog(sendText("&3[Factory Config] &cNo spawners section found in file."));
        }
    }

    public static void SavePrices(){
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

    public static void LoadPrices() {
        File file = new File(developmentPath, developmentDataFile);

        if (!file.exists()) {
            consoleLog(sendText("&3[Factory Config] &ePrices file does not exist. No data loaded."));
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
            consoleLog(sendText("&3[Factory Config] &aPrices loaded successfully!"));
        } else {
            consoleLog(sendText("&3[Factory Config] &cNo prices section found in file."));
        }
    }

    public static void SaveWorths(){
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

    public static void LoadWorths() {
        File file = new File(developmentPath, developmentDataFile);

        if (!file.exists()) {
            consoleLog(sendText("&3[Factory Config] &eWorths file does not exist. No data loaded."));
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
            consoleLog(sendText("&3[Factory Config] &aPrices loaded successfully!"));
        } else {
            consoleLog(sendText("&3[Factory Config] &cNo prices section found in file."));
        }
    }

    public static int maxCategoryPage = 10;

    public static void LoadShopItems() {
        File file = new File(developmentPath, developmentDataFile);

        if (!file.exists()) {
            consoleLog(sendText("&3[Factory Config] &eShop Items file does not exist. No data loaded."));
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<String> parentCategory = new ArrayList<>();

        String parent = "category";
        if (config.isList(parent)) {
            List<String> keys = config.getStringList(parent);
            for (String key : keys) {
                List<String> keySplit = Arrays.asList(key.split(","));
                String categoryKey = keySplit.getFirst();
                String categorySlot = keySplit.get(1);
                String categoryMaterial = keySplit.get(2);
                String categoryDisplayname = keySplit.get(3);
                String isDisplayed = keySplit.get(4);
                categoryList.put(categoryKey+".slot", categorySlot);
                categoryList.put(categoryKey+".material", Material.getMaterial(categoryMaterial).toString());
                categoryList.put(categoryKey+".name", categoryDisplayname);
                categoryList.put(categoryKey+".isDisplayed", isDisplayed);

                String formatCategory = categoryKey+"_items";
                parentCategory.add(formatCategory);
                consoleLog(sendText("&3[Factory Config] &aCategory: " + formatCategory + " slot: "+categorySlot+" with material: "+categoryMaterial+" with displayname: "+categoryDisplayname+" &adisplayed: "+isDisplayed+" loaded successfully!"));
            }
        } else {
            consoleLog(sendText("&3[Factory Config] &cNo category " + parent + " section found in file."));
        }

        for (String category : parentCategory){
            if (config.isList(category)) {
                List<String> keys = config.getStringList(category);
                List<ItemStack> _iList = new ArrayList<>();
                for (String key : keys) {
                    if (!key.contains("custom")){
                        Material material = Material.getMaterial(key);
                        if (material != null) {
                            ItemStack addedItem = new ItemStack(material);
                            ItemMeta addedItemMeta = addedItem.getItemMeta();

                            PersistentDataContainer container = addedItemMeta.getPersistentDataContainer();
                            /*Double worth = GetWorth(addedItem.getType().toString().toLowerCase().replaceAll("_", "").trim());
                            if (worth != null){
                                container.set(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE, worth);
                            }
                            addedItem.setItemMeta(addedItemMeta);*/
                            addedItem.setItemMeta(ProcessItemMeta(addedItem).getItemMeta());

                            String serializedItem = "";
                            try {
                                serializedItem = ItemSerializer.ItemStackToBase64(addedItem);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            container.set(GetNamespacedKey("storeditem"), PersistentDataType.STRING, serializedItem);
                            addedItem.setItemMeta(addedItemMeta);
                            _iList.add(addedItem);
                        } else {
                            consoleLog(sendText("&3[Factory Config] &cInvalid material: " + key));
                        }
                    }else{
                        String cItemKey = key.replaceAll(":custom", "").trim();
                        ItemStack addedItem = GetItem(cItemKey);
                        ItemMeta addedItemMeta = addedItem.getItemMeta();
                        PersistentDataContainer container = addedItemMeta.getPersistentDataContainer();

                        container.set(GetNamespacedKey("customitem"), PersistentDataType.BOOLEAN, true);
                        container.set(GetNamespacedKey("configkey"), PersistentDataType.STRING, cItemKey+"_custom");

                        String serializedItem = "";
                        try {
                            serializedItem = ItemSerializer.ItemStackToBase64(addedItem);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        container.set(GetNamespacedKey("storeditem"), PersistentDataType.STRING, serializedItem);
                        addedItem.setItemMeta(addedItemMeta);
                        _iList.add(addedItem);
                    }
                }

                for (int i = 1; i < maxCategoryPage+1; i++) {
                    shopItemList.put(category+i, new ArrayList<>());
                }

                int currentPage = 1;
                for (ItemStack item : _iList){
                    if (shopItemList.get(category+currentPage).size() >= 46){
                        currentPage++;
                    }
                    shopItemList.get(category+currentPage).add(item.clone());
                }

                //shopItemList.put(category, _iList);
                consoleLog(sendText("&3[Factory Config] &aShop Items category " + category + " loaded successfully!"));
            } else {
                consoleLog(sendText("&3[Factory Config] &cNo category " + category + " section found in file."));
            }
        }
    }

    // level requirements
    public static void SaveLevelMinimums(){
        File file = new File(developmentPath, developmentDataFile);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "level-minimums.";
        for (String key : levelMinimumList.keySet()){
            config.set(parent+key, levelMinimumList.get(key));
        }
        try{
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void LoadLevelMinimums() {
        File file = new File(developmentPath, developmentDataFile);

        if (!file.exists()) {
            consoleLog(sendText("&3[Factory Config] &eLevel Requirements file does not exist. No data loaded."));
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "level-minimums";
        String section = parent+".";

        if (config.isConfigurationSection(parent)) {
            Set<String> keys = config.getConfigurationSection(parent).getKeys(false);
            for (String key : keys) {
                levelMinimumList.put(key, config.getInt(section + key));
            }
            consoleLog(sendText("&3[Factory Config] &aLevel Requirements loaded successfully!"));
        } else {
            consoleLog(sendText("&3[Factory Config] &cNo Level Requirements section found in file."));
        }
    }

    // items

    public static void SaveItems(){
        File file = new File(developmentPath, developmentItemFile);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String parent = "items.";
        config.set("items", null);
        for (String key : itemList.keySet()){
            config.set(parent+key, itemList.get(key));
        }
        try{
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void LoadItems() {
        File file = new File(developmentPath, developmentItemFile);

        if (!file.exists()) {
            consoleLog(sendText("&3[Factory Config] &eItems file does not exist. No data loaded."));
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
            consoleLog(sendText("&3[Factory Config] &aItems loaded successfully!"));
        } else {
            consoleLog(sendText("&3[Factory Config] &cNo items section found in file."));
        }
    }


    public static void LoadAllData(){
        LoadPrices();
        LoadWorths();
        LoadLevelMinimums();
        LoadItems();
        LoadShopItems();
        LoadLocations();
        LoadSpawners();
        LoadConfigurations();
        GenerateItemConfig();
        consoleLog(sendText("&3[Factory Config] &aLoading all configuration and model data..."));
    }

    public static void SaveAllData(){
        SavePrices();
        SaveWorths();
        SaveLevelMinimums();
        SaveItems();
        SaveLocations();
        SaveSpawners();
        consoleLog(sendText("&3[Factory Config] &aSaving all configuration data..."));
    }

    public static void BackupDatabase(File pluginFolder) {
        File sourceFile = new File(pluginFolder, "database.db");

        // Format: 5.10.2025 14-33-22
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d.MM.yyyy HH-mm-ss"));
        File destFile = new File(pluginFolder, "database " + timestamp + ".db");

        try {
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Database backup created: " + destFile.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
