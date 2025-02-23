package org.factory.factory.Utils;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;

import static org.factory.factory.Utils.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.UserInterface.*;

public class GUIManager implements Listener {

    public enum MenuList{
        MachineUpgrade;

        MenuList parseMenu(String m){
            return switch (m) {
                case "machineupgrade" -> MenuList.MachineUpgrade;

                default -> MenuList.MachineUpgrade;
            };
        }
    }

    public static void OpenMenu(Player player, MenuList menu){
        if (player.getVehicle() != null){
            return;
        }


    }

    public static ItemStack getHeaderFooter(){
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        return border;
    }

    public static void SetHeaderFooter(Inventory inventory){
        int size = inventory.getSize();
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, getHeaderFooter());
        }
        for (int i = size-9; i < size; i++) {
            inventory.setItem(i, getHeaderFooter());
        }
    }

    public static HashMap<Player, MenuList> openedMenu = new HashMap<>();

    @EventHandler
    public void onInventoryClickHolder(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory == null) return;

        InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();

        if (holder instanceof CustomInventoryHolder) {
            event.setCancelled(true);
            PlaySound(Sound.BLOCK_NOTE_BLOCK_BIT, player, 1, 3);
        }
    }

    @EventHandler
    public void OnJoin(PlayerJoinEvent event){
        openedMenu.put(event.getPlayer(), MenuList.MachineUpgrade);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void OnInventoryClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();

        MenuList inventory = openedMenu.get(player);

        ItemStack item = event.getCurrentItem();

        if (inventory.equals(MenuList.MachineUpgrade)){
            if (GetTag(item).equals("upgrade")){
                player.sendMessage(sendText("&aYou pressed upgrade button!"));
            }
        }
    }

    public static void OpenMachineUpgrades(Player player, int level){
        Inventory inventory = OpenGUI(player, 3, "Upgrade Machine");
        openedMenu.put(player, MenuList.MachineUpgrade);
        SetHeaderFooter(inventory);
        inventory.setItem(14, getMachineUpgradesItem("upgrade"));
        player.openInventory(inventory);

    }

    public static ItemStack getMachineUpgradesItem(String name){
        ItemStack item = new ItemStack(Material.STRING);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (name.equals("upgrade")){
            meta.setDisplayName(sendText("&aUpgrade Machine"));
        }

        container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, name);

        item.setItemMeta(meta);
        return item;
    }

    public static String GetTag(ItemStack item){
        String tag = "none";
        if (item != null){
            if (item.getType() != Material.AIR){
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (container.has(GetNamespacedKey("gui-icon"))){
                    String obtainedTag = container.get(GetNamespacedKey("gui-icon"), PersistentDataType.STRING);
                    if (obtainedTag != null){
                        tag = obtainedTag;
                    }
                }
            }
        }
        return tag;
    }
}
