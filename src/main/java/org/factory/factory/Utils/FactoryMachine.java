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




    public static ItemStack CreateMachine(String name, int machineLevel, long speed, int productionRate, int steamConsumption,
                                          int durability, int maxDurability, Material material, String dropName, int potentialDrop, Rarity.RarityType rarity) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        List<String> itemLore = new ArrayList<>();

        String rarityDisplay = "";
        String rarityColor = "";


        // Identifier set
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(GetNamespacedKey("machine"), PersistentDataType.BOOLEAN, true);
        container.set(GetNamespacedKey("dropName"), PersistentDataType.STRING, dropName);
        container.set(GetNamespacedKey("potentialDrop"), PersistentDataType.INTEGER, potentialDrop);
        container.set(GetNamespacedKey("speed"), PersistentDataType.LONG, speed);
        container.set(GetNamespacedKey("productionRate"), PersistentDataType.INTEGER, productionRate);
        container.set(GetNamespacedKey("steamConsumption"), PersistentDataType.INTEGER, steamConsumption);
        container.set(GetNamespacedKey("machineLevel"), PersistentDataType.INTEGER, machineLevel);
        container.set(GetNamespacedKey("durability"), PersistentDataType.INTEGER, durability);
        container.set(GetNamespacedKey("maxDurability"), PersistentDataType.INTEGER, maxDurability);
        container.set(GetNamespacedKey("rarity"), PersistentDataType.STRING, rarity.toString());

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
