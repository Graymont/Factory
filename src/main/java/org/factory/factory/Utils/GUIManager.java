package org.factory.factory.Utils;

import org.bukkit.Location;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.Utils.FactoryMachine.*;
import static org.factory.factory.Utils.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.SQLiteDatabase.parseLocationString;
import static org.factory.factory.Utils.UserInterface.*;
import static org.factory.factory.Utils.VaultEconomy.GetPlayerBalance;
import static org.factory.factory.Utils.VaultEconomy.RemovePlayerBalance;

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
    public static ItemStack getBackrgound(Material material){
        ItemStack border = new ItemStack(material);
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

    public static void SetBackground(Inventory inventory, Material material){
        int size = inventory.getSize();
        for (int i = 0; i < size; i++) {
            if (inventory.getItem(i) == null){
                inventory.setItem(i, getBackrgound(material));
            }
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

        if (item != null && item.getType() != Material.AIR){
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (inventory.equals(MenuList.MachineUpgrade)){
                if (GetTag(item).equals("upgrade")){

                    double playerBalance = GetPlayerBalance(player);
                    double upgradePrice = 1;
                    Double upgradeContainer = container.get(GetNamespacedKey("upgradePrice"), PersistentDataType.DOUBLE);
                    if (upgradeContainer == null){
                        return;
                    }
                    String locationContainer = container.get(GetNamespacedKey("location"), PersistentDataType.STRING);
                    if (locationContainer == null){
                        return;
                    }
                    upgradePrice = upgradeContainer;
                    Location machineLocation = parseLocationString(locationContainer);
                    if (playerBalance >= upgradePrice){
                        RemovePlayerBalance(player, upgradePrice);
                        int currentLevel = Integer.parseInt(getMainPlugin().events.placedMachines.get(machineLocation+__machineLevelKey));
                        currentLevel++;

                        getMainPlugin().events.placedMachines.put(machineLocation+__machineLevelKey, ""+currentLevel);

                        ItemStack previousItem = getMainPlugin().events.machineItems.get(machineLocation);
                        ItemMeta previousItemMeta = previousItem.getItemMeta();
                        PersistentDataContainer previousContainer = previousItemMeta.getPersistentDataContainer();
                        String machineName = sendText(previousItemMeta.getDisplayName());
                        int machineLevel = currentLevel;
                        Long speed = previousContainer.get(GetNamespacedKey(speedKey), PersistentDataType.LONG);
                        Integer productionRate = previousContainer.get(GetNamespacedKey(productionRateKey), PersistentDataType.INTEGER);
                        Integer steamConsumption = previousContainer.get(GetNamespacedKey(steamConsumptionKey), PersistentDataType.INTEGER);
                        Integer durability = previousContainer.get(GetNamespacedKey(durabilityKey), PersistentDataType.INTEGER);
                        Integer maxDurability = previousContainer.get(GetNamespacedKey(maxDurabilityKey), PersistentDataType.INTEGER);
                        Integer potentialDrop = previousContainer.get(GetNamespacedKey(potentialDropKey), PersistentDataType.INTEGER);
                        String dropName = previousContainer.get(GetNamespacedKey(dropNameKey), PersistentDataType.STRING);
                        Rarity.RarityType rarity = Rarity.RarityType.parseRarity(previousContainer.get(GetNamespacedKey(rarityKey), PersistentDataType.STRING));
                        Material material = previousItem.getType();

                        ItemStack newItem = CreateMachine(machineName, machineLevel, speed, productionRate
                        , steamConsumption, durability, maxDurability, material, dropName, potentialDrop, rarity);

                        getMainPlugin().events.machineItems.put(machineLocation, newItem);

                        player.sendMessage(sendText("&aSuccessfully upgrade your machine! to level &2"
                                +getMainPlugin().events.placedMachines.get(machineLocation+".machineLevel")));
                    }else{
                        event.setCancelled(true);
                        player.sendMessage(Notification_NoMoney(player, upgradePrice-playerBalance));
                        return;
                    }
                    player.closeInventory();
                    OpenMachineUpgrades(player, machineLocation);
                }
            }
        }
    }

    public static void OpenMachineUpgrades(Player player, Location location){
        int level = Integer.parseInt(getMainPlugin().events.placedMachines.get(location+__machineLevelKey));
        Inventory inventory = OpenGUI(player, 3, "Upgrade Machine");
        openedMenu.put(player, MenuList.MachineUpgrade);
        SetHeaderFooter(inventory);
        SetBackground(inventory, Material.GRAY_STAINED_GLASS_PANE);
        inventory.setItem(13, getMachineUpgradesItem(player,"upgrade", location));
        player.openInventory(inventory);
    }

    public static ItemStack getMachineUpgradesItem(Player player, String name, Location location){
        int level = Integer.parseInt(getMainPlugin().events.placedMachines.get(location+__machineLevelKey));
        ItemStack item = new ItemStack(Material.STRING);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        List<String> itemLore = new ArrayList<>();

        if (name.equals("upgrade")){
            double upgradePrice = level*10000;
            meta.setDisplayName(sendText("&aUpgrade Machine"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Current Level: &f"+level));
            itemLore.add(sendText(" &7Upgrade Cost: &f"+FormatDouble(upgradePrice)));
            itemLore.add(sendText(" "));
            double playerBalance = GetPlayerBalance(player);
            if (playerBalance >= upgradePrice){
                itemLore.add(sendText("&aClick to upgrade"));
            }else{
                itemLore.add(sendText("&cClick to upgrade"));
            }
            container.set(GetNamespacedKey("upgradePrice"), PersistentDataType.DOUBLE, upgradePrice);
            container.set(GetNamespacedKey("location"), PersistentDataType.STRING, ""+location);
        }
        meta.setLore(itemLore);

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
