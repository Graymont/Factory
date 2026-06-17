package org.factory.factory.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;

public class MerchantListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = player.getOpenInventory().getTopInventory();


        if (!(topInventory instanceof MerchantInventory)) {
            return;
        }


        if (event.getClick() != ClickType.SHIFT_LEFT) {
            return;
        }


        if (event.getClickedInventory() == null || event.getClickedInventory().equals(topInventory)) {
            return;
        }

        MerchantInventory merchantInventory = (MerchantInventory) topInventory;
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null) {
            return;
        }


        event.setCancelled(true);


        ItemStack ingredient1 = merchantInventory.getItem(0);
        ItemStack ingredient2 = merchantInventory.getItem(1);


        if (ingredient1 == null) {
            merchantInventory.setItem(0, clickedItem.clone());
            event.getCurrentItem().setAmount(0);
        } else if (ingredient1.isSimilar(clickedItem) && ingredient1.getAmount() < ingredient1.getMaxStackSize()) {
            int amountToAdd = Math.min(clickedItem.getAmount(), ingredient1.getMaxStackSize() - ingredient1.getAmount());
            ingredient1.setAmount(ingredient1.getAmount() + amountToAdd);
            event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - amountToAdd);
        } else if (ingredient2 == null) {
            merchantInventory.setItem(1, clickedItem.clone());
            event.getCurrentItem().setAmount(0);
        } else if (ingredient2.isSimilar(clickedItem) && ingredient2.getAmount() < ingredient2.getMaxStackSize()) {
            int amountToAdd = Math.min(clickedItem.getAmount(), ingredient2.getMaxStackSize() - ingredient2.getAmount());
            ingredient2.setAmount(ingredient2.getAmount() + amountToAdd);
            event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - amountToAdd);
        }
    }
}
