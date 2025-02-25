package org.factory.factory.Utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static org.factory.factory.Database.GetItem;
import static org.factory.factory.Utils.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.UserInterface.sendText;
import static org.factory.factory.Utils.UserInterface.uncolouredText;

public class FactoryMachine {

    int maxLevel = 30;

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
    public static String __statusKey = ".status";

    public static ItemStack CreateMachine(String name, int machineLevel, long speed, int productionRate, int steamConsumption,
                                          int durability, int maxDurability, Material material, String dropName, int potentialDrop,
                                          Rarity.RarityType rarity) {

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

        ItemStack dropItem = GetItem(dropName).clone();

        // Set item display name and lore
        meta.setDisplayName(sendText(Rarity.getColor(rarity)+name));
        itemLore.add(sendText("&8Machine"));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(rarityColor+"&9Information"));
        itemLore.add(sendText(" &7Machine Level: &f"+machineLevel));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(rarityColor+"&9Attributes"));
        itemLore.add(sendText(" &e⚡ &7Speed: &f" + speed+" &9seconds"));
        itemLore.add(sendText(" &e✿ &7Production Rate: &f" + productionRate));
        itemLore.add(sendText(" &e✦ &7Steam Consumption: &f" + steamConsumption));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(rarityColor+"&9Drops:"));
        itemLore.add(sendText(" &8- &7["+dropItem.getItemMeta().getDisplayName()+"&7]"));
        String displayname = uncolouredText(dropItem.getItemMeta().getDisplayName());
        if (potentialDrop >= 2){
            itemLore.add(sendText(" &8- &7[&2Fine "+displayname+"&7]"));
        }
        if (potentialDrop >= 3){
            itemLore.add(sendText(" &8- &7[&6Good "+displayname+"&7]"));
        }
        if (potentialDrop >= 4){
            itemLore.add(sendText(" &8- &7[&4Special "+displayname+"&7]"));
        }
        if (potentialDrop >= 5){
            itemLore.add(sendText(" &8- &7[&bMajestic "+displayname+"&7]"));
        }
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7Durability: &f"+durability+"&8/&f"+maxDurability));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7Place anywhere to generate"));
        itemLore.add(sendText(" &7items from this machine &8✎"));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(Rarity.setRarity(rarity)));

        meta.setLore(itemLore);
        item.setItemMeta(meta);

        return item;
    }

}
