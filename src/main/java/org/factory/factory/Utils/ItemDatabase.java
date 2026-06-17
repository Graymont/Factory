package org.factory.factory.Utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.factory.factory.Database.GetItem;
import static org.factory.factory.GameManager.GUIManager.*;
import static org.factory.factory.GameManager.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.UserInterface.*;

public class ItemDatabase {


    public static HashMap<Integer, List<ItemStack>> itemDatabaseList = new HashMap<>();

    public static List<String> advancedMineralList = Arrays.asList("tungsten", "palladium", "cobalt", "mithril", "orichalcum", "titanium",
            "adamantine", "dragonite", "voidsteel", "etherium", "reinforcedcopper", "reinforcedredstone", "reinforcedlapis");


    public static void OpenItemDatabase(Player player, int page){

        openedMenu.put(player, MenuList.Item_Database);
        for (int i = 1; i < maxCatalogPage+1; i++) {
            itemDatabaseList.put(i, new ArrayList<>());
        }

        List<ItemStack> allCatalogItems = new ArrayList<>();

        List<String> armorMaterialList = Arrays.asList("helmet", "headgear", "chestplate", "leggings", "boots");

        String key = "";
        for (String advancedIngot : advancedMineralList){
            for (String armorMaterial : armorMaterialList){
                allCatalogItems.add(new ItemStack(GetItem(advancedIngot+armorMaterial)));
                if (!advancedIngot.contains("reinforced")){
                    allCatalogItems.add(new ItemStack(GetItem(advancedIngot+armorMaterial+"2")));
                }
            }
        }


        int currentPage = 1;
        for (ItemStack item : allCatalogItems){
            if (itemDatabaseList.get(currentPage).size() >= 36){
                currentPage++;
            }
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, "itemDatabase");
            container.set(GetNamespacedKey("itemDatabaseKey"), PersistentDataType.STRING, uncolouredText(meta.getDisplayName()).toLowerCase().replaceAll(" ", "").trim());
            item.setItemMeta(meta);
            itemDatabaseList.get(currentPage).add(item.clone());
        }

        Inventory inventory = OpenGUI(player, 6, "Item Database ["+page+"/"+maxCatalogPage+"]");
        int start = 0;
        for (int i = 0; i < itemDatabaseList.get(page).size(); i++) {
            inventory.setItem(9+i, itemDatabaseList.get(page).get(start).clone());
            start++;
        }
        SetHeaderFooter(inventory);
        inventory.setItem(48, getCatalogIcon("previous_page"));
        inventory.setItem(50, getCatalogIcon("next_page"));
        player.openInventory(inventory);
    }

}
