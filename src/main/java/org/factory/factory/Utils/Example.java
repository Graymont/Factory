package org.factory.factory.Utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.factory.factory.GameHandler.FactoryItem;

public class Example {

    public static void Testitem(Player player){

        FactoryItem item = new FactoryItem();

        item.setType(FactoryItem.Type.Tool);

        item.setAttackDamage(10);

        ItemStack itemJadi = new ItemStack(item.build());


        player.getInventory().addItem(itemJadi);
    }

}
