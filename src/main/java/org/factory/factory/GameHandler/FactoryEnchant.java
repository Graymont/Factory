package org.factory.factory.GameHandler;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static org.factory.factory.GameManager.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.UserInterface.*;

public class FactoryEnchant implements Listener {

    public static String enchantmentBookKey = "enchantmentBook";
    public static String enchantmentKey = "enchantment";

    public enum Enchant{
        None,
        Efficiency,
        Fortune;


        public static Enchant parseEnchant(String type){
            return switch (type.toLowerCase()){

                case "efficiency" -> Enchant.Efficiency;
                case "fortune" -> Enchant.Fortune;

                default -> FactoryEnchant.Enchant.None;
            };
        }

        public static EnchantType parseType(Enchant enchant){
            return switch (enchant){

                case Enchant.Efficiency, Enchant.Fortune -> EnchantType.Tool;

                default -> EnchantType.Universal;
            };
        }
    }

    public enum EnchantType{
        Universal,
        Tool,
        Equipment,
        Weapon;


        public static EnchantType parseEnchant(String type){
            return switch (type.toLowerCase()){

                case "tool" -> EnchantType.Tool;
                case "equipment" -> EnchantType.Equipment;
                case "weapon" -> EnchantType.Weapon;

                default -> EnchantType.Universal;
            };
        }
    }


    public static ItemStack GetEnchantmentBook(Enchant enchant, EnchantType type, int level){
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(GetNamespacedKey(enchantmentBookKey), PersistentDataType.BOOLEAN, true);

        List<String> itemLore = new ArrayList<>();

        meta.setDisplayName(sendText("&7"+enchant+" &fEnchantment Book &e&l"+intToRoman(level)));

        itemLore.add(sendText("&9Miscellaneous"));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7Type: &f"+type));
        itemLore.add(sendText(" "));
        itemLore.add(sendText("&8"+usageArrowSymbol+" &7Drag and Drop to Item"));

        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }

}
