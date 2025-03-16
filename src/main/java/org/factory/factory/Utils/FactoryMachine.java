package org.factory.factory.Utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static org.factory.factory.Database.GetItem;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.Utils.PersistentDataManager.GetNamespacedKey;
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
        ItemStack obtainedMachine = CreateMachine(formatItemName(name)+" Machine", 1, machineBaseSpeed, 1,
                machineBaseSteamConsumption, machineBaseDurability, machineBaseDurability, material, name.toLowerCase(), 1,
                Rarity.RarityType.Common, "Active", 0, type, machineBaseSteamProduction);

        return obtainedMachine;
    }

    public static int machineMaxLevel = 100;
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
                                          Rarity.RarityType rarity, String status, int totalProduction, MachineType machineType, int steamProduction) {

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

        ItemStack dropItem = GetItem(dropName).clone();

        // Set item display name and lore
        meta.setDisplayName(sendText(Rarity.getColor(rarity)+name));
        itemLore.add(sendText("&9Machine"));
        itemLore.add(sendText("&8&o"+machineType));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(rarityColor+"&9Information"));
        itemLore.add(sendText(" &8♦ &7Machine Level: "+machineLevel));
        itemLore.add(sendText(" &8♦ &7Machine Status: "+status));
        itemLore.add(sendText(" &8♦ &7Total Production: "+totalProduction));
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
        itemLore.add(sendText(" &7Place anywhere to generate"));
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

        item = new ItemStack(CreateMachine(name, machineLevel, speed, productionRate
                , steamConsumption, durability, maxDurability, material, dropName, potentialDrop, Rarity.RarityType.parseRarity(rarity),
                status, totalProduction, MachineType.parseType(machineType), steamProduction));

        return item;
    }

    public static void RefreshMachine(Location location){
        ItemStack previousItem = getMainPlugin().events.machineItems.get(location);
        ItemMeta previousItemMeta = previousItem.getItemMeta();
        PersistentDataContainer previousContainer = previousItemMeta.getPersistentDataContainer();
        int currentLevel = Integer.parseInt(getMainPlugin().events.placedMachines.get(location+__machineLevelKey));

        String machineName = sendText(previousItemMeta.getDisplayName());

        // -- Upgrade
        Long speed = (long) (machineBaseSpeed-(currentLevel/4));
        getMainPlugin().events.placedMachines.put(location+__speedKey, ""+speed);

        Integer steamConsumption = machineBaseSteamConsumption+currentLevel+2;
        getMainPlugin().events.placedMachines.put(location+__steamConsumptionKey, ""+steamConsumption);

        Integer durability = (machineBaseDurability+currentLevel*100);
        getMainPlugin().events.placedMachines.put(location+__durabilityKey, ""+durability);

        Integer maxDurability = durability;
        getMainPlugin().events.placedMachines.put(location+__maxDurabilityKey, ""+maxDurability);

        Integer steamProduction = machineBaseSteamProduction+currentLevel+2;
        getMainPlugin().events.placedMachines.put(location+__steamProductionKey, ""+steamProduction);

        Integer productionRate = 1;

        Rarity.RarityType rarity = Rarity.RarityType.parseRarity(previousContainer.get(GetNamespacedKey(rarityKey), PersistentDataType.STRING));
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

        // ---


        Integer potentialDrop = previousContainer.get(GetNamespacedKey(potentialDropKey), PersistentDataType.INTEGER);
        String dropName = previousContainer.get(GetNamespacedKey(dropNameKey), PersistentDataType.STRING);
        Material material = previousItem.getType();

        String status = previousContainer.get(GetNamespacedKey(machineStatusKey), PersistentDataType.STRING);

        String machineType = previousContainer.get(GetNamespacedKey(machineTypeKey), PersistentDataType.STRING);

        Integer totalProduction = Integer.parseInt(getMainPlugin().events.placedMachines.get(location+__totalProductionKey));

        ItemStack newItem = CreateMachine(machineName, currentLevel, speed, productionRate
                , steamConsumption, durability, maxDurability, material, dropName, potentialDrop, rarity,
                status, totalProduction, MachineType.parseType(machineType), steamProduction);

        getMainPlugin().events.machineItems.put(location, newItem);
    }

}
