package org.factory.factory.GameManager;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.factory.factory.Factory;

import java.util.ArrayList;
import java.util.List;

public class PersistentDataManager {

    public static NamespacedKey GetNamespacedKey(String keyName){
        return new NamespacedKey(Factory.getPlugin(Factory.class), keyName);
    }

    public static List<String> GetPersistentDataContainer(ItemStack item){

        List<String> containerList = new ArrayList<>();

        if ( item != null && item.getType() != Material.AIR){
            ItemMeta meta = item.getItemMeta();

            PersistentDataContainer container = meta.getPersistentDataContainer();

            for (NamespacedKey key : container.getKeys()){
                if (container.has(key, PersistentDataType.INTEGER)){
                    containerList.add(key+"-> Integer: "+container.get(key, PersistentDataType.INTEGER));
                }
                else if (container.has(key, PersistentDataType.LONG)){
                    containerList.add(key+"-> Long: "+container.get(key, PersistentDataType.LONG));
                }
                else if (container.has(key, PersistentDataType.DOUBLE)){
                    containerList.add(key+"-> Double: "+container.get(key, PersistentDataType.DOUBLE));
                }
                else if (container.has(key, PersistentDataType.STRING)){
                    containerList.add(key+"-> String: "+container.get(key, PersistentDataType.STRING));
                }
            }
        }

        return containerList;
    }

}
