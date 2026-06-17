package org.factory.factory.GameHandler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.factory.factory.Utils.ItemSerializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.factory.factory.Database.*;
import static org.factory.factory.Events.*;
import static org.factory.factory.GameHandler.FactoryItem.*;
import static org.factory.factory.GameManager.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.GameHandler.PlayerProgress.GetPrestigeRequirement;
import static org.factory.factory.Utils.SQLiteDatabase.isConnected;
import static org.factory.factory.Utils.SQLiteDatabase.machineSqlSyntax;
import static org.factory.factory.Utils.UserInterface.*;

public class FactoryMachine {

    public enum MachineType{
        Item,
        Steam;

        public static MachineType parseType(String subType) {
            return switch (subType.toLowerCase()) {
                case "item" -> MachineType.Item;
                case "steam" -> MachineType.Steam;
                default -> null;
            };
        }
    }

    public static ItemStack GetMachine(String name, Material material, MachineType type){
        if (type == MachineType.Steam){
            name = "Steam";
            material = Material.SMOKER;
        }

        String removedUnderscore = name.replaceAll("_", "").trim();
        Double sConsumption = GetPrice(removedUnderscore+"machine_custom");

        if (sConsumption == null){
            sConsumption = 0.0;
        }

        int steamConsumptionCalculation = (int) (sConsumption*0.01)+1;
        if (steamConsumptionCalculation < 1){
            steamConsumptionCalculation = 1;
        }

        Integer machineLevelMinimum = GetLevelMinimum(removedUnderscore.toLowerCase()+"machine");
        if (machineLevelMinimum == null){
            machineLevelMinimum = 1;
        }
        consoleLog("Machine Level Minimum: "+removedUnderscore.toLowerCase()+"machine"+" ("+machineLevelMinimum+")");

        ItemStack obtainedMachine = new ItemStack(CreateMachine(formatItemName(name)+" Machine", 1, machineBaseSpeed, 1,
                machineBaseSteamConsumption+steamConsumptionCalculation, machineBaseDurability, machineBaseDurability, material, name.toLowerCase().replaceAll("_", "").trim().replaceAll(" ", "").trim(), 1,
                Rarity.RarityType.Common, "Active", 0, type, machineBaseSteamProduction, true, machineLevelMinimum));


        /*consoleLog("Removed Underscore: "+removedUnderscore);
        consoleLog("sConsumption from price: "+sConsumption);
        consoleLog("SteamConsumption of "+removedUnderscore+": "+steamConsumptionCalculation);*/
        return obtainedMachine.clone();
    }

    public static int machineMaxLevel = 30;
    public static int machineBaseSpeed = 35;
    public static int machineBaseSteamConsumption = 3;
    public static int machineBaseSteamProduction = 3;
    public static int machineBaseDurability = 100;

    public static String machineKey = "machine";

    public static String dropNameKey = "dropName";
    public static String potentialDropKey = "potentialDrop";
    public static String speedKey = "speed";
    public static String productionRateKey = "productionRate";
    public static String steamConsumptionKey = "steamConsumption";
    public static String machineLevelKey = "machineLevel";
    public static String durabilityKey = "durability";
    public static String maxDurabilityKey = "maxDurability";
    public static String rarityKey = "rarity";
    public static String totalProductionKey = "totalProduction";
    public static String machineStatusKey = "status";
    public static String machineTypeKey = "machineType";
    public static String steamProductionKey = "steamProduction";

    public static String serialCodeKey = "serialCode";

    public static String __dropNameKey = ".dropName";
    public static String __potentialDropKey = ".potentialDrop";
    public static String __speedKey = ".speed";
    public static String __productionRateKey = ".productionRate";
    public static String __steamConsumptionKey = ".steamConsumption";
    public static String __machineLevelKey = ".machineLevel";
    public static String __durabilityKey = ".durability";
    public static String __maxDurabilityKey = ".maxDurability";
    public static String __rarityKey = ".rarity";
    public static String __machineNameKey = ".machineName";
    public static String __locationKey = ".location";
    public static String __ownerKey = ".owner";
    public static String __uuidKey = ".uuid";
    public static String __taskIdKey = ".taskId";
    public static String __machineStatusKey = ".status";
    public static String __totalProductionKey = ".totalProduction";
    public static String __machineTypeKey = ".machineType";
    public static String __steamProductionKey = ".steamProduction";

    public static String __countdownKey = ".countdown";

    public static ItemStack CreateMachine(String name, int machineLevel, long speed, int productionRate, int steamConsumption,
                                          int durability, int maxDurability, Material material, String dropName, int potentialDrop,
                                          Rarity.RarityType rarity, String status, int totalProduction, MachineType machineType,
                                          int steamProduction, boolean canUse, int levelMinimum) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        List<String> itemLore = new ArrayList<>();

        String rarityDisplay = "";
        String rarityColor = "";

        // Identifier set
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(GetNamespacedKey(machineKey), PersistentDataType.BOOLEAN, true);
        container.set(GetNamespacedKey(dropNameKey), PersistentDataType.STRING, dropName);
        container.set(GetNamespacedKey(potentialDropKey), PersistentDataType.INTEGER, potentialDrop);
        container.set(GetNamespacedKey(speedKey), PersistentDataType.LONG, speed);
        container.set(GetNamespacedKey(productionRateKey), PersistentDataType.INTEGER, productionRate);
        container.set(GetNamespacedKey(steamConsumptionKey), PersistentDataType.INTEGER, steamConsumption);
        container.set(GetNamespacedKey(machineLevelKey), PersistentDataType.INTEGER, machineLevel);
        container.set(GetNamespacedKey(durabilityKey), PersistentDataType.INTEGER, durability);
        container.set(GetNamespacedKey(maxDurabilityKey), PersistentDataType.INTEGER, maxDurability);
        container.set(GetNamespacedKey(rarityKey), PersistentDataType.STRING, rarity.toString());
        container.set(GetNamespacedKey(machineStatusKey), PersistentDataType.STRING, status);
        container.set(GetNamespacedKey(totalProductionKey), PersistentDataType.INTEGER, totalProduction);

        container.set(GetNamespacedKey(machineTypeKey), PersistentDataType.STRING, machineType.toString().toLowerCase());
        container.set(GetNamespacedKey(steamProductionKey), PersistentDataType.INTEGER, steamProduction);

        container.set(GetNamespacedKey(serialCodeKey), PersistentDataType.STRING, GenerateSerialCode());



        container.set(GetNamespacedKey(canUseKey), PersistentDataType.BOOLEAN, canUse);



        container.set(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER, levelMinimum);

        ItemStack dropItem = GetItem(dropName).clone();

        Double productValue = dropItem.getItemMeta().getPersistentDataContainer().get(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE);
        if (productValue == null){
            productValue = 0.0;
        }

        int prestigeMinimum = 0;
        if (levelMinimum >= 50){
            prestigeMinimum = GetPrestigeRequirement(levelMinimum);
        }

        // Set item display name and lore
        meta.setDisplayName(sendText(Rarity.getColor(rarity)+name));
        itemLore.add(sendText("&9Machine"));
        itemLore.add(sendText("&8&o"+machineType));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(rarityColor+"&9Information"));
        itemLore.add(sendText(" &8♦ &7Machine Level: "+machineLevel));
        itemLore.add(sendText(" &8♦ &7Machine Status: "+status));
        itemLore.add(sendText(" &8♦ &7Total Production: "+totalProduction));
        itemLore.add(sendText(" &8♦ &7Product Value: "+FormatDouble(productValue)));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(rarityColor+"&9Requirements"));
        itemLore.add(sendText(" &8♦ &7Level Minimum to Place: &7"+levelMinimum));
        if (prestigeMinimum > 0){
            itemLore.add(sendText(" &8♦ &7Prestige Minimum to Place: &7"+intToRoman(prestigeMinimum)));
        }
        if (canUse){
            itemLore.add(sendText(" &8♦ &7Can Use: &aTrue &2"+checkSymbol));
        }else{
            itemLore.add(sendText(" &8♦ &7Can Use: &cFalse &4"+xSymbol));
        }
        itemLore.add(sendText(" "));
        itemLore.add(sendText(rarityColor+"&9Attributes"));
        itemLore.add(sendText(" &e⚡ &7Speed: &f" + speed+" &9seconds"));
        if (machineType == MachineType.Item){
            itemLore.add(sendText(" &e✿ &7Production Rate: &f" + productionRate));
            itemLore.add(sendText(" &e\uD83C\uDF0A &7Steam Consumption: &f" + steamConsumption));
        }
        if (machineType == MachineType.Steam){
            itemLore.add(sendText(" &e\uD83C\uDF0A &7Steam Production: &f" + steamProduction));
        }
        itemLore.add(sendText(" "));
        if (machineType == MachineType.Item){
            itemLore.add(sendText(rarityColor+"&9Drops:"));
            itemLore.add(sendText(" &8\uD83C\uDFB2 &7["+dropItem.getItemMeta().getDisplayName()+"&7]"));
            String displayname = uncolouredText(dropItem.getItemMeta().getDisplayName());
            if (potentialDrop >= 2){
                itemLore.add(sendText(" &8\uD83C\uDFB2 &7[&2Fine "+displayname+"&7]"));
            }
            if (potentialDrop >= 3){
                itemLore.add(sendText(" &8\uD83C\uDFB2 &7[&6Good "+displayname+"&7]"));
            }
            if (potentialDrop >= 4){
                itemLore.add(sendText(" &8\uD83C\uDFB2 &7[&4Special "+displayname+"&7]"));
            }
            if (potentialDrop >= 5){
                itemLore.add(sendText(" &8\uD83C\uDFB2 &7[&bMajestic "+displayname+"&7]"));
            }
            itemLore.add(sendText(" "));
        }
        itemLore.add(sendText(" &7Durability: &f"+durability+"&8/&f"+maxDurability+GetDurabilityPercent(durability, maxDurability)));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &8"+usageArrowSymbol+" &7Place anywhere to generate"));
        itemLore.add(sendText(" &7"+machineType.toString().toLowerCase()+" from this machine &8✎"));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(Rarity.setRarity(rarity)));

        meta.setLore(itemLore);
        item.setItemMeta(meta);

        return item;
    }

    public static boolean isMachine(ItemStack item){
        if (item != null){
            if (item.getType() != Material.AIR){
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                Boolean machineContainer = container.get(GetNamespacedKey(machineKey), PersistentDataType.BOOLEAN);
                return machineContainer != null;
            }
        }
        return false;
    }

    public static ItemStack UpdateMachineItem(ItemStack item){

        ItemMeta meta = item.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        String name = uncolouredText(meta.getDisplayName());
        Integer machineLevel = container.get(GetNamespacedKey(machineLevelKey), PersistentDataType.INTEGER);
        Long speed = container.get(GetNamespacedKey(speedKey), PersistentDataType.LONG);
        Integer productionRate = container.get(GetNamespacedKey(productionRateKey), PersistentDataType.INTEGER);
        Integer steamConsumption = container.get(GetNamespacedKey(steamConsumptionKey), PersistentDataType.INTEGER);
        Integer durability = container.get(GetNamespacedKey(durabilityKey), PersistentDataType.INTEGER);
        Integer maxDurability = container.get(GetNamespacedKey(maxDurabilityKey), PersistentDataType.INTEGER);
        Material material = item.getType();
        String dropName = container.get(GetNamespacedKey(dropNameKey), PersistentDataType.STRING);
        Integer potentialDrop = container.get(GetNamespacedKey(potentialDropKey), PersistentDataType.INTEGER);
        String rarity = container.get(GetNamespacedKey(rarityKey), PersistentDataType.STRING);
        String status = container.get(GetNamespacedKey(machineStatusKey), PersistentDataType.STRING);
        Integer totalProduction = container.get(GetNamespacedKey(totalProductionKey), PersistentDataType.INTEGER);
        String machineType = container.get(GetNamespacedKey(machineTypeKey), PersistentDataType.STRING);
        Integer steamProduction = container.get(GetNamespacedKey(steamProductionKey), PersistentDataType.INTEGER);
        Integer levelMinimum = container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER);

        Boolean canUse = container.get(GetNamespacedKey(canUseKey), PersistentDataType.BOOLEAN);

        item = new ItemStack(CreateMachine(name, machineLevel, speed, productionRate
                , steamConsumption, durability, maxDurability, material, dropName, potentialDrop, Rarity.RarityType.parseRarity(rarity),
                status, totalProduction, MachineType.parseType(machineType), steamProduction, canUse, levelMinimum));

        /*meta = item.getItemMeta();
        container = meta.getPersistentDataContainer();
        container.set(GetNamespacedKey(canUseKey), PersistentDataType.BOOLEAN, canUse);
        item.setItemMeta(meta);*/

        return item;
    }

    public static void RefreshMachine(Location location){
        ItemStack previousItem = machineItems.get(location);
        ItemMeta previousItemMeta = previousItem.getItemMeta();
        PersistentDataContainer previousContainer = previousItemMeta.getPersistentDataContainer();
        int currentLevel = Integer.parseInt(placedMachines.get(location+__machineLevelKey));
        //consoleLog(sendText("Level from refresh: "+currentLevel));


        // -- Upgrade
        Long speed = (long) (machineBaseSpeed-(currentLevel));
        placedMachines.put(location+__speedKey, ""+speed);


        Double sConsumption = GetPrice(uncolouredText(previousItemMeta.getDisplayName().replaceAll(" ", "").trim()).toLowerCase()+"_custom");
        if (sConsumption == null){
            sConsumption = 0.0;
        }
        int steamConsumptionCalculation = (int) (sConsumption*0.01);
        if (steamConsumptionCalculation < 1){
            steamConsumptionCalculation = 1;
        }
        Integer steamConsumption = steamConsumptionCalculation+machineBaseSteamConsumption+currentLevel;
        placedMachines.put(location+__steamConsumptionKey, ""+steamConsumption);

        Integer durability = (machineBaseDurability+currentLevel*100);
        placedMachines.put(location+__durabilityKey, ""+durability);

        Integer maxDurability = durability;
        placedMachines.put(location+__maxDurabilityKey, ""+maxDurability);

        Integer steamProduction = machineBaseSteamProduction+currentLevel+2;
        placedMachines.put(location+__steamProductionKey, ""+steamProduction);

        Integer potentialDrop = previousContainer.get(GetNamespacedKey(potentialDropKey), PersistentDataType.INTEGER);

        Rarity.RarityType rarity = Rarity.RarityType.parseRarity(placedMachines.get(location+__rarityKey));

        String machineName = uncolouredText(previousItemMeta.getDisplayName());
        placedMachines.put(location+__machineNameKey, Rarity.getColor(rarity)+machineName);

        //consoleLog(sendText("Machine Name: "+placedMachines.get(location+__machineNameKey)));

        Integer productionRate = 1;

        switch (rarity){
            case Rarity.RarityType.Common ->
                    potentialDrop = 1;
            case Rarity.RarityType.Uncommon ->
                    potentialDrop = 2;
            case Rarity.RarityType.Rare ->
                    potentialDrop = 3;
            case Rarity.RarityType.Epic ->
                    potentialDrop = 4;
            case Rarity.RarityType.Legendary ->
                    potentialDrop = 5;
            case Rarity.RarityType.Immortal ->
                    potentialDrop = 6;
        }
        switch (rarity){
            case Rarity.RarityType.Common ->
                    productionRate = 1;
            case Rarity.RarityType.Uncommon ->
                    productionRate = 2;
            case Rarity.RarityType.Rare ->
                    productionRate = 3;
            case Rarity.RarityType.Epic ->
                    productionRate = 4;
            case Rarity.RarityType.Legendary ->
                    productionRate = 5;
            case Rarity.RarityType.Immortal ->
                    productionRate = 6;
        }

        placedMachines.put(location+__potentialDropKey, ""+potentialDrop);
        placedMachines.put(location+__productionRateKey, ""+productionRate);

        // ---


        String dropName = previousContainer.get(GetNamespacedKey(dropNameKey), PersistentDataType.STRING);
        Material material = previousItem.getType();

        String status = previousContainer.get(GetNamespacedKey(machineStatusKey), PersistentDataType.STRING);

        String machineType = previousContainer.get(GetNamespacedKey(machineTypeKey), PersistentDataType.STRING);

        Integer totalProduction = Integer.parseInt(placedMachines.get(location+__totalProductionKey));

        Integer levelMinimum = previousContainer.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER);

        ItemStack newItem = new ItemStack(CreateMachine(machineName, currentLevel, speed, 1
                , steamConsumption, durability, maxDurability, material, dropName, potentialDrop, rarity,
                status, totalProduction, MachineType.parseType(machineType), steamProduction, true, levelMinimum));

        machineItems.put(location, newItem.clone());
    }

    public static int GetMaxMachine(Player player){
        return maxMachines.get(player.getUniqueId()) + extraMaxMachines.get(player.getUniqueId());
    }

    public static void AddMaxMachine(Player player, int amount){
        int current = maxMachines.get(player.getUniqueId());

        current += amount;
        if (current < globalMaxMachine + extraMaxMachines.get(player.getUniqueId())){
            maxMachines.put(player.getUniqueId(), current);
        }else{
            current = globalMaxMachine + extraMaxMachines.get(player.getUniqueId());
            maxMachines.put(player.getUniqueId(), current);
        }
    }

    public static void RemoveMaxMachine(Player player, int amount){
        int current = maxMachines.get(player.getUniqueId());

        current -= amount;
        if (current > 0){
            maxMachines.put(player.getUniqueId(), current);
        }else{
            maxMachines.put(player.getUniqueId(), 1);
        }
    }

    public static void SetMaxMachine(Player player, int amount){
        if (amount <= globalMaxMachine + extraMaxMachines.get(player.getUniqueId())){
            maxMachines.put(player.getUniqueId(), amount);
        }
    }

    public static void SavePlayerMachines(Player player, Connection connection){
        if (connection == null || !isConnected()) {
            consoleLog(sendText("&4[DB] &cSQLite connection is closed! can't save data of placed machines from &4"+player.getName()+" &cerror code: 2"));
            return;
        }

        String sqlInsert = machineSqlSyntax;

        try (PreparedStatement pstmt = connection.prepareStatement(sqlInsert)) {
            for (Location location : playerPlacedMachines.get(player.getUniqueId())) {
                String owner = placedMachines.get(location + ".owner");
                String UUID = placedMachines.get(location + ".uuid");
                int taskId = Integer.parseInt(placedMachines.getOrDefault(location + ".taskId", "0"));
                int machineLevel = Integer.parseInt(placedMachines.getOrDefault(location + ".machineLevel", "0"));
                int speed = Integer.parseInt(placedMachines.getOrDefault(location + ".speed", "0"));
                int productionRate = Integer.parseInt(placedMachines.getOrDefault(location + ".productionRate", "0"));
                int steamConsumption = Integer.parseInt(placedMachines.getOrDefault(location + ".steamConsumption", "0"));
                int durability = Integer.parseInt(placedMachines.getOrDefault(location + ".durability", "0"));
                int maxDurability = Integer.parseInt(placedMachines.getOrDefault(location + ".maxDurability", "0"));
                String machineName = placedMachines.get(location + ".machineName");
                String rarity = placedMachines.get(location + ".rarity");

                String machineType = placedMachines.get(location + ".machineType");
                int steamProduction = Integer.parseInt(placedMachines.getOrDefault(location + ".steamProduction", "0"));

                String status = placedMachines.get(location + ".status");
                int totalProduction = Integer.parseInt(placedMachines.getOrDefault(location + ".totalProduction", "0"));


                String drop = placedMachines.get(location + ".dropName");
                int potentialDrop = Integer.parseInt(placedMachines.getOrDefault(location + ".potentialDrop", "1"));
                pstmt.setString(1, location+"");
                pstmt.setString(2, owner);
                pstmt.setString(3, UUID);
                pstmt.setInt(4, taskId);
                pstmt.setInt(5, machineLevel);
                pstmt.setInt(6, speed);
                pstmt.setInt(7, productionRate);
                pstmt.setInt(8, steamConsumption);
                pstmt.setInt(9, durability);
                pstmt.setInt(10, maxDurability);
                pstmt.setString(11, drop);
                pstmt.setInt(12, potentialDrop);
                pstmt.setString(13, rarity);
                pstmt.setString(14, machineName);
                pstmt.setString(15, status);
                pstmt.setInt(16, totalProduction);
                pstmt.setString(17, machineType);
                pstmt.setInt(18, steamProduction);

                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void SavePlayerMachineItems(Player player, Connection connection){
        if (connection == null || !isConnected()) {
            consoleLog(sendText("&4[DB] &cSQLite connection is closed! can't save data of machine items from &4"+player.getName()+" &cerror code: 2"));
            return;
        }

        String sqlInsert = "INSERT OR REPLACE INTO MachineItems (location, item) " +
                "VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sqlInsert)) {
            String serializedItem = "";
            for (Location location : playerPlacedMachines.get(player.getUniqueId())) {
                try {
                    serializedItem = ItemSerializer.ItemStackArrayToBase64(new ItemStack[]{machineItems.get(location)});
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pstmt.setString(1, ""+location);
                pstmt.setString(2, serializedItem);

                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void DeleteMachineFromDatabase(Connection connection, Location location) {
        String sql = "DELETE FROM PlacedMachines WHERE location = ?";

        String locationString = location.getWorld().getName() + ":" +
                location.getBlockX() + ":" +
                location.getBlockY() + ":" +
                location.getBlockZ();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, location.toString());
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                consoleLog(sendText("&aMachine at location &2" + locationString + " &awas successfully deleted."));
            } else {
                consoleLog(sendText("&eNo machine found at location &6" + locationString + "."));
            }
        } catch (SQLException e) {
            consoleLog(sendText("&cError while deleting machine: &4" + e.getMessage()));
        }
    }

    public static void DeleteMachineItemsFromDatabase(Connection connection, Location location) {
        String sql = "DELETE FROM MachineItems WHERE location = ?";

        String locationString = location.getWorld().getName() + ":" +
                location.getBlockX() + ":" +
                location.getBlockY() + ":" +
                location.getBlockZ();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, location.toString());
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                consoleLog(sendText("&aMachine Items at location &2" + locationString + " &awas successfully deleted."));
            } else {
                consoleLog(sendText("&eNo machine item found at location &6" + locationString + "."));
            }
        } catch (SQLException e) {
            consoleLog(sendText("&cError while deleting machine items: &4" + e.getMessage()));
        }
    }


}
