package org.factory.factory.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static org.factory.factory.Database.*;
import static org.factory.factory.Events.UpdateItem;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.Utils.FactoryItem.*;
import static org.factory.factory.Utils.FactoryMachine.*;
import static org.factory.factory.Utils.ItemSerializer.loadSerializedItem;
import static org.factory.factory.Utils.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.SQLiteDatabase.parseLocationString;
import static org.factory.factory.Utils.UserInterface.*;
import static org.factory.factory.Utils.VaultEconomy.*;

public class GUIManager implements Listener {

    public enum MenuList{
        None,
        MachineEngine,
        Shop,
        ShopPage,
        Anvil,
        EnchantingTable;

        MenuList parseMenu(String m){
            return switch (m) {
                case "machineengine" -> MenuList.MachineEngine;
                case "shop" -> MenuList.Shop;
                case "shoppage" -> MenuList.ShopPage;
                case "anvil" -> MenuList.Anvil;
                case "enchantingtable" -> MenuList.EnchantingTable;

                default -> MenuList.None;
            };
        }
    }

    public static void OpenMenu(Player player, MenuList menu){
        if (player.getVehicle() != null){
            return;
        }
        openedMenu.put(player, menu);

        if (menu.equals(MenuList.Shop)){
            Inventory inventory = OpenGUI(player, 6, "Shop");
            for (String key : categoryList.keySet()){
                String rawKey = key
                        .replaceAll(".slot", "").trim()
                        .replaceAll(".material", "").trim()
                        .replaceAll(".name", "").trim();

                ItemStack item = new ItemStack(Material.getMaterial(categoryList.get(rawKey+".material")));
                ItemMeta meta = item.getItemMeta();

                String categoryKey = rawKey+"_items";
                PersistentDataContainer container = meta.getPersistentDataContainer();
                container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, "category");
                container.set(GetNamespacedKey("category"), PersistentDataType.STRING, categoryKey);

                meta.setDisplayName(sendText(categoryList.get(rawKey+".name")));
                int slot = Integer.parseInt(categoryList.get(rawKey+".slot"));

                item.setItemMeta(meta);

                inventory.setItem(slot, item);

            }
            SetFooter(inventory);
            player.openInventory(inventory);
            PlaySound(Sound.BLOCK_ENDER_CHEST_OPEN, player, 1, 1);
        }

        else if (menu.equals(MenuList.Anvil)){
            Inventory inventory = OpenGUI(player, 3, "Anvil");

            SetHeaderFooter(inventory);
            inventory.setItem(13, getBasicUi("anvil"));
            SetBackground(inventory, Material.GRAY_STAINED_GLASS_PANE);

            player.openInventory(inventory);
            PlaySoundAt(Sound.BLOCK_ANVIL_PLACE, player.getLocation(), 1, 1);
        }
        player.updateInventory();
    }

    public static ItemStack getBasicUi(String name){
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        List<String> itemLore = new ArrayList<>();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (name.equals("anvil")){
            meta.setDisplayName(sendText("&bAnvil"));

            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &d&lHow to repair?"));
            itemLore.add(sendText(" &7Click on the item you wanted to fix"));
            itemLore.add(sendText(" &7inside your inventory"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Repair will cost &a100"+icon));
            itemLore.add(sendText(" "));
        }
        container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, name);
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getShopContent(Player player, ItemStack item, boolean custom){
        //ItemStack item = new ItemStack()
        ItemMeta meta = item.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        int itemAmount = buyAmount.get(player);
        Double price = GetPrice(item.getType().toString().toLowerCase().replaceAll("_", "").trim());
        if (price == null){
            price = 100000000.0;
        }

        if (custom){
            price = GetPrice((container.get(GetNamespacedKey("configkey"), PersistentDataType.STRING)));
            if (price == null){
                price = 100000000.0;
            }
        }

        container.set(GetNamespacedKey("price"), PersistentDataType.DOUBLE, price);

        List<String> itemLore = new ArrayList<>();

        if (meta.hasLore()){
            for (String lore : meta.getLore()){
                itemLore.add(sendText(lore));
            }
        }

        if (!meta.hasDisplayName()){
            meta.setDisplayName(sendText("&3x"+itemAmount+" &b"+formatItemName(item.getType().toString())));
        }else{
            String actualDisplayname = meta.getDisplayName();
            meta.setDisplayName(sendText("&3x"+itemAmount+" "+actualDisplayname));
        }

        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7Price: &f"+FormatDouble(price*itemAmount)+icon));
        itemLore.add(sendText(" "));
        double playerBalance = GetPlayerBalance(player);
        if (playerBalance >= price){
            itemLore.add(sendText("&aClick to buy"));
        }else{
            itemLore.add(sendText("&cYou can't afford this item!"));
            itemLore.add(sendText("&cyou need &6"+FormatDouble(price-playerBalance)+icon+" more!"));
        }

        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
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

    public static void SetFooter(Inventory inventory){
        int size = inventory.getSize();
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
        buyAmount.putIfAbsent(event.getPlayer(), 1);
        openedMenu.put(event.getPlayer(), MenuList.None);
    }

    @EventHandler
    public void OnQuit(PlayerQuitEvent event){
        openedMenu.put(event.getPlayer(), MenuList.None);
    }

    @EventHandler
    public void OnInventoryClose(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        if (event.getReason() == InventoryCloseEvent.Reason.PLAYER){
            openedMenu.put(player, MenuList.None);
            //player.sendMessage(sendText("&aClosed inventory caused by player!"));
        }

        if (openedMachine.get(player) != null){
            Bukkit.getScheduler().cancelTask(openedMachine.get(player));
            openedMachine.put(player, 0);
            //consoleLog(sendText("&aTask id of opened machine is canceled &6("+openedMachine.get(player)+")"));
        }else{
            openedMachine.put(player, 0);
            //consoleLog(sendText("&aPlayer does not have opened machine yet!"));
        }
    }

    public static HashMap<Player, Integer> buyAmount = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void OnInventoryClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();

        MenuList inventory = openedMenu.get(player);

        ItemStack item = event.getCurrentItem();

        if (item != null && item.getType() != Material.AIR){
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (inventory.equals(MenuList.MachineEngine)){
                if (GetTag(item).equals("upgrade")){
                    // debugging
                    Double upgradeContainer = container.get(GetNamespacedKey("upgradePrice"), PersistentDataType.DOUBLE);
                    if (upgradeContainer == null){
                        return;
                    }
                    String locationContainer = container.get(GetNamespacedKey("location"), PersistentDataType.STRING);
                    if (locationContainer == null){
                        return;
                    }

                    // gain location
                    Location machineLocation = parseLocationString(locationContainer);

                    // machine item meta
                    ItemStack previousItem = getMainPlugin().events.machineItems.get(machineLocation);
                    ItemMeta previousItemMeta = previousItem.getItemMeta();
                    PersistentDataContainer previousContainer = previousItemMeta.getPersistentDataContainer();

                    // gain the current level
                    int currentLevel = Integer.parseInt(getMainPlugin().events.placedMachines.get(machineLocation+__machineLevelKey));

                    // upgrade price
                    double playerBalance = GetPlayerBalance(player);
                    double upgradePrice = upgradeContainer;
                    // req: production
                    int totalProductionReq = currentLevel*25;
                    // cost of the acid
                    int advancedAcidCost = currentLevel*2; // something like refining, adaptive etc
                    int baseAcidCost = advancedAcidCost*5; // base acid with no rarity (used in every rarities machine upgrades)

                    Integer totalProduction = Integer.parseInt(getMainPlugin().events.placedMachines.get(machineLocation+__totalProductionKey));
                    Rarity.RarityType rarity = Rarity.RarityType.parseRarity(previousContainer.get(GetNamespacedKey(rarityKey), PersistentDataType.STRING));

                    // acid item discovery from rarity
                    ItemStack acidBase = GetItem("acid");
                    ItemStack acidItem = null;
                    if (rarity == Rarity.RarityType.Common){
                        acidItem = GetItem("refiningacid");
                    }
                    else if (rarity == Rarity.RarityType.Uncommon){
                        acidItem = GetItem("corrosiveacid");
                    }
                    else if (rarity == Rarity.RarityType.Rare){
                        acidItem = GetItem("energeticacid");
                    }
                    else if (rarity == Rarity.RarityType.Epic){
                        acidItem = GetItem("mutagenicacid");
                    }
                    else if (rarity == Rarity.RarityType.Legendary){
                        acidItem = GetItem("adaptiveacid");
                    }
                    else if (rarity == Rarity.RarityType.Immortal){
                        acidItem = GetItem("voidacid");
                    }

                    // requirement check (will return if not fulfilled)

                    if (currentLevel >= machineMaxLevel){
                        event.setCancelled(true);
                        player.sendMessage(sendText("&4You have reached the maximum level! &c(current level -> "+currentLevel+")"));
                        return;
                    }

                    if (playerBalance < upgradePrice) {
                        event.setCancelled(true);
                        player.sendMessage(Notification_NoMoney(player, upgradePrice-playerBalance));
                        return;
                    }

                    if (totalProduction < totalProductionReq) {
                        event.setCancelled(true);
                        player.sendMessage(sendText("&4You don't have enough requirement! &c("+(totalProductionReq-totalProduction)+" " +
                                "more total productions to upgrade)"));
                        return;
                    }

                    if (!isPlayerHasItem(player, acidBase, baseAcidCost)) {
                        event.setCancelled(true);
                        player.sendMessage(Notification_NoItem(player));
                        player.sendMessage(sendText("&cYou need x"+baseAcidCost+" Basic Acid to Upgrade!"));
                        return;
                    }

                    if (!isPlayerHasItem(player, acidItem, advancedAcidCost)) {
                        event.setCancelled(true);
                        player.sendMessage(Notification_NoItem(player));
                        player.sendMessage(sendText("&cYou need x"+advancedAcidCost+" "+uncolouredText(acidItem.getItemMeta().getDisplayName())+" " +
                                "to Upgrade!"));
                        return;
                    }

                    // removing item from player inventory
                    RemovePlayerBalance(player, upgradePrice);
                    RemoveItemFromPlayer(player, acidBase, baseAcidCost);
                    RemoveItemFromPlayer(player, acidItem, advancedAcidCost);

                    // level management
                    currentLevel++;
                    getMainPlugin().events.placedMachines.put(machineLocation+__machineLevelKey, ""+currentLevel);

                    // refresh the machine item data
                    RefreshMachine(machineLocation);

                    // refresh the machine's hashmap in Events
                    getMainPlugin().events.EnableMachine(player, machineLocation);

                    player.sendMessage(sendText("&aSuccessfully upgrade your machine! to level &2"
                            +getMainPlugin().events.placedMachines.get(machineLocation+".machineLevel")));

                    OpenMachineEngines(player, machineLocation);

                    PlaySoundAt(Sound.ENTITY_PLAYER_LEVELUP, machineLocation, 1, 1);
                }
                else if (GetTag(item).equals("switch")){
                    String locationContainer = container.get(GetNamespacedKey("location"), PersistentDataType.STRING);
                    if (locationContainer == null){
                        return;
                    }

                    Location machineLocation = parseLocationString(locationContainer);

                    if (!getMainPlugin().events.placedMachines.get(machineLocation+__machineStatusKey).equals("Broken")){
                        if (getMainPlugin().events.placedMachines.get(machineLocation+__machineStatusKey).equals("Active")){
                            getMainPlugin().events.placedMachines.put(machineLocation+__machineStatusKey, "Disabled");
                            getMainPlugin().events.DisableMachine(player, machineLocation);
                        }else{
                            getMainPlugin().events.placedMachines.put(machineLocation+__machineStatusKey, "Active");
                            getMainPlugin().events.EnableMachine(player, machineLocation);
                        }
                    }

                    getMainPlugin().events.UpdateMachineTag(player, machineLocation, sendText(getMainPlugin().events.machineItems.get(machineLocation)
                            .getItemMeta().getDisplayName()), 0);

                    OpenMachineEngines(player, machineLocation);

                    //player.sendMessage(sendText("&aPressed the switch"));
                }
            }
            else if (inventory.equals(MenuList.Shop)){
                if (GetTag(item).equals("category")){
                    //player.closeInventory();
                    openedMenu.put(player, MenuList.ShopPage);
                    String category = container.get(GetNamespacedKey("category"), PersistentDataType.STRING);

                    Inventory openedCategory = OpenGUI(player, 6, "Category - "+formatItemName(category));
                    List<ItemStack> contentList = shopItemList.get(category);
                    for (int i = 0; i < contentList.size(); i++) {
                        ItemStack addedItem = contentList.get(i);
                        ItemMeta addedItemMeta = addedItem.getItemMeta();
                        PersistentDataContainer addedItemContainer = addedItemMeta.getPersistentDataContainer();
                        if (!addedItemContainer.has(GetNamespacedKey("customitem"))){
                            openedCategory.setItem(i, getShopContent(player, contentList.get(i).clone(), false));
                        }else{
                            openedCategory.setItem(i, getShopContent(player, contentList.get(i).clone(), true));
                        }
                    }
                    SetFooter(openedCategory);

                    openedCategory.setItem(45, getShopIcons(player, "back"));
                    openedCategory.setItem(48, getShopIcons(player, "buyamount_minus"));
                    openedCategory.setItem(49, getShopIcons(player, "buyamount_status"));
                    openedCategory.setItem(50, getShopIcons(player, "buyamount_plus"));

                    player.openInventory(openedCategory);

                    //player.sendMessage("Category: "+category);
                }
            }
            else if (inventory.equals(MenuList.ShopPage)){
                if (container.has(GetNamespacedKey("price"))){
                    double playerBalance = GetPlayerBalance(player);
                    Double priceContainer = container.get(GetNamespacedKey("price"), PersistentDataType.DOUBLE);
                    int itemAmount = buyAmount.get(player);
                    if (item.getMaxStackSize() == 1){
                        itemAmount = 1;
                    }
                    double price = 1000000.0*itemAmount;

                    String storedItem = container.get(GetNamespacedKey("storeditem"), PersistentDataType.STRING);

                    ItemStack obtainedItem = loadSerializedItem(storedItem);

                    if (obtainedItem == null){
                        player.sendMessage(sendText("&cYou can't buy this item, please contact server's admin if you believe this was an error!"));
                        return;
                    }

                    if (priceContainer != null){
                        price = priceContainer*itemAmount;
                    }

                    if (playerBalance < price){
                        player.sendMessage(Notification_NoMoney(player, playerBalance));
                        return;
                    }

                    obtainedItem.setAmount(itemAmount);
                    Map<Integer, ItemStack> addedItem = player.getInventory().addItem(obtainedItem);
                    if (addedItem.isEmpty()){
                        RemovePlayerBalance(player, price);
                        PlaySound(Sound.BLOCK_NOTE_BLOCK_CHIME, player, 1, 1);
                        player.sendMessage(sendText("&aSuccessfully bought "+item.getItemMeta().getDisplayName()+" &afor &2"+FormatDouble(price)+icon));
                    }else{
                        int successfullyAdded = itemAmount - addedItem.values().stream().mapToInt(ItemStack::getAmount).sum();

                        if (successfullyAdded == 0) {
                            player.sendMessage(Notification_InventoryFull(player));
                        } else {
                            double partialPrice = successfullyAdded * price;
                            RemovePlayerBalance(player, partialPrice);
                            PlaySound(Sound.BLOCK_NOTE_BLOCK_CHIME, player, 1, 1);
                            player.sendMessage(sendText("&aSuccessfully bought &bx" + successfullyAdded + " " + item.getItemMeta().getDisplayName() + "&afor &2" + FormatDouble(partialPrice)+icon));

                            int remainingItems = itemAmount - successfullyAdded;
                            player.sendMessage(sendText("&cYour inventory was too full to add &bx" + remainingItems + " " + item.getItemMeta().getDisplayName() + "&c."));
                        }
                    }
                }
                else if (GetTag(item).equals("buyamount_plus")){
                    int currentBuyAmount = buyAmount.get(player);
                    if (currentBuyAmount < 64){
                        if (currentBuyAmount == 1){
                            currentBuyAmount += 3;
                        }else{
                            currentBuyAmount += 4;
                        }
                    }
                    buyAmount.put(player, currentBuyAmount);
                    RefreshShop(player);
                }
                else if (GetTag(item).equals("buyamount_minus")){
                    int currentBuyAmount = buyAmount.get(player);
                    if (currentBuyAmount >= 4){
                        currentBuyAmount -= 4;
                    }
                    if (currentBuyAmount <= 0){
                        currentBuyAmount = 1;
                    }
                    buyAmount.put(player, currentBuyAmount);
                    RefreshShop(player);
                }

                else if (GetTag(item).equals("back")){
                    //player.closeInventory();
                    OpenMenu(player, MenuList.Shop);
                }
            }

            else if (inventory.equals(MenuList.Anvil)){
                if (container.has(GetNamespacedKey(itemKey)) || container.has(GetNamespacedKey(machineKey))){
                    ItemStack currentItem = event.getCurrentItem();
                    ItemMeta currentItemMeta = currentItem.getItemMeta();
                    PersistentDataContainer currentItemContainer = currentItemMeta.getPersistentDataContainer();
                    double durability = currentItemContainer.get(GetNamespacedKey(durabilityKey), PersistentDataType.DOUBLE);
                    double maxDurability = currentItemContainer.get(GetNamespacedKey(maxDurabilityKey), PersistentDataType.DOUBLE);
                    if (durability < maxDurability){
                        durability = maxDurability;
                    }else{
                        player.sendMessage(sendText("&aThis item is already fixed!"));
                        PlaySoundAt(Sound.BLOCK_LANTERN_PLACE, player.getLocation(), 1, 1);
                        return;
                    }
                    currentItemContainer.set(GetNamespacedKey(durabilityKey), PersistentDataType.DOUBLE, durability);
                    currentItem.setItemMeta(currentItemMeta);
                    UpdateItem(player, ""+event.getSlot());
                    PlaySoundAt(Sound.BLOCK_ANVIL_USE, player.getLocation(), 1, 1);
                }
            }
        }
    }



    static void RefreshShop(Player player){
        for (ItemStack shopContent : player.getOpenInventory().getTopInventory().getContents()){
            if (shopContent != null){
                ItemMeta contentMeta = shopContent.getItemMeta();
                if (contentMeta.getPersistentDataContainer().has(GetNamespacedKey("price"))){
                    PersistentDataContainer container = contentMeta.getPersistentDataContainer();
                    Double contentPrice = container.get(GetNamespacedKey("price"), PersistentDataType.DOUBLE);
                    if (contentPrice == null){
                        contentPrice = 1000000000.0;
                    }
                    int itemAmount = buyAmount.get(player);
                    if (shopContent.getMaxStackSize() == 1){
                        itemAmount = 1;
                    }
                    shopContent.setAmount(itemAmount);
                    List<String> displayName = Arrays.asList(contentMeta.getDisplayName().split(" "));

                    String fixedDisplayName = contentMeta.getDisplayName().replace(displayName.getFirst()+" ", "").trim();
                    contentMeta.setDisplayName(sendText("&bx"+itemAmount+" "+fixedDisplayName));

                    List<String> storedLore = new ArrayList<>();

                    for (String lore : contentMeta.getLore()){
                        if (lore != null){
                            if (uncolouredText(lore).contains("Price")){
                                storedLore.add(sendText(" &7Price: &f"+(FormatDouble(contentPrice*itemAmount))+icon));
                            }else{
                                storedLore.add(sendText(lore));
                            }
                        }
                    }

                    contentMeta.setLore(storedLore);
                    shopContent.setItemMeta(contentMeta);
                }
            }
        }
        player.updateInventory();
    }

    public static HashMap<Player, Integer> openedMachine = new HashMap<>();

    public static void OpenMachineEngines(Player player, Location location){
        int level = Integer.parseInt(getMainPlugin().events.placedMachines.get(location+__machineLevelKey));
        String machineName = getMainPlugin().events.placedMachines.get(location+__machineNameKey);
        Inventory inventory = OpenGUI(player, 4, uncolouredText(machineName)+" Engine - Lv. "+level);
        openedMenu.put(player, MenuList.MachineEngine);
        SetHeaderFooter(inventory);
        SetBackground(inventory, Material.GRAY_STAINED_GLASS_PANE);
        inventory.setItem(13, getMachineEnginesItem(player,"upgrade", location));
        inventory.setItem(8, getMachineEnginesItem(player,"switch", location));
        inventory.setItem(22, getMachineEnginesItem(player,"timer", location));
        inventory.setItem(0, getMachineEnginesItem(player,"information", location));
        player.openInventory(inventory);

        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                Inventory topInventory = player.getOpenInventory().getTopInventory();
                for (ItemStack item : topInventory.getContents()){
                    if (item != null){
                        topInventory.setItem(22, getMachineEnginesItem(player,"timer", location));
                        topInventory.setItem(13, getMachineEnginesItem(player,"upgrade", location));
                        topInventory.setItem(8, getMachineEnginesItem(player,"switch", location));
                        topInventory.setItem(0, getMachineEnginesItem(player,"information", location));
                    }
                }
                player.updateInventory();
            }
        }.runTaskTimer(getMainPlugin(), 0L, 20L).getTaskId();

        openedMachine.put(player, taskId);
        player.updateInventory();
    }

    public static ItemStack getMachineEnginesItem(Player player, String name, Location location){
        int level = Integer.parseInt(getMainPlugin().events.placedMachines.get(location+__machineLevelKey));
        int nextLevel = level+1;
        int totalProduction = Integer.parseInt(getMainPlugin().events.placedMachines.get(location+__totalProductionKey));
        String status = getMainPlugin().events.placedMachines.get(location+__machineStatusKey);
        ItemStack item = new ItemStack(getMainPlugin().events.machineItems.get(location).getType());
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        List<String> itemLore = new ArrayList<>();

        if (name.equals("upgrade")){

            double upgradePrice = (level*100)+level*120;
            int totalProductionReq = level*25;
            int advancedAcidCost = level*2;
            int baseAcidCost = advancedAcidCost*5;

            meta.setDisplayName(sendText("&a&lUpgrade Machine &2("+(level)+" &b➠ &2"+(level+1)+")"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &8➭ &fNext Upgrade:"));
            if (getMainPlugin().events.placedMachines.get(location+__machineTypeKey).equals("item")){
                itemLore.add(sendText(" &8➳ &7Steam Consumption: &e"+(level+2)+" &6-> &e"+(nextLevel+2)));
            }else{
                itemLore.add(sendText(" &8➳ &7Steam Production: &e"+(level+2)+" &6-> &e"+(nextLevel+2)));
            }
            itemLore.add(sendText(" &8➳ &7Machine Speed: &e"+(machineBaseSpeed-(level/4))+"s &6-> &e"+(machineBaseSpeed-(nextLevel/4))+"s"));
            itemLore.add(sendText(" &8➳ &7Durability: &e"+(level*100)+" &6-> &e"+(nextLevel*100)));
            itemLore.add(sendText(" &8➳ &7Production Worth: &e"+(level)+" &6-> &e"+(nextLevel)));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &8↙ &fRequirements:"));
            itemLore.add(sendText(" &8➽ &7Money: &f"+FormatDouble(upgradePrice)+icon));
            itemLore.add(sendText(" &8➽ &7Basic Acid: &ex"+baseAcidCost));
            ItemStack acidBase = GetItem("acid");
            ItemStack acidItem = null;
            Rarity.RarityType rarity = Rarity.RarityType.parseRarity(getMainPlugin().events.placedMachines.get(location+__rarityKey));
            if (rarity == Rarity.RarityType.Common){
                acidItem = GetItem("refiningacid");
            }
            else if (rarity == Rarity.RarityType.Uncommon){
                acidItem = GetItem("corrosiveacid");
            }
            else if (rarity == Rarity.RarityType.Rare){
                acidItem = GetItem("energeticacid");
            }
            else if (rarity == Rarity.RarityType.Epic){
                acidItem = GetItem("mutagenicacid");
            }
            else if (rarity == Rarity.RarityType.Legendary){
                acidItem = GetItem("adaptiveacid");
            }
            else if (rarity == Rarity.RarityType.Immortal){
                acidItem = GetItem("voidacid");
            }
            itemLore.add(sendText(" &8➽ &7"+uncolouredText(acidItem.getItemMeta().getDisplayName())+" Acid: &ex"+advancedAcidCost));
            itemLore.add(sendText(" &8➽ &7Total Production: &e"+level*25));
            itemLore.add(sendText(" "));
            double playerBalance = GetPlayerBalance(player);
            if (playerBalance >= upgradePrice){
                itemLore.add(sendText("&aClick to upgrade"));
            }else{
                itemLore.add(sendText("&cClick to upgrade"));
            }
            container.set(GetNamespacedKey("upgradePrice"), PersistentDataType.DOUBLE, upgradePrice);
            container.set(GetNamespacedKey("totalProductionReq"), PersistentDataType.INTEGER, totalProductionReq);
            //container.set(GetNamespacedKey("acidCost"), PersistentDataType.INTEGER, acidCost);
        }
        else if (name.equals("switch")){
            item.setType(Material.LEVER);
            String currentStats = "&aON";
            if (status.equals("Disabled")){
                currentStats = "&cOFF";
            }
            else if (status.equals("Enabled")){
                currentStats = "&aON";
            }

            if (!status.equals("Broken")){
                meta.setDisplayName(sendText("&fSwitch &8["+currentStats+"&8]"));
            }else{
                item.setType(Material.REDSTONE_TORCH);
                meta.setDisplayName(sendText("&c&lSWITCH BROKEN"));
            }
        }
        else if (name.equals("timer")){
            item.setType(Material.CLOCK);

            String productionTimer = getMainPlugin().events.placedMachines.get(location+__countdownKey)+"s";
            if (getMainPlugin().events.placedMachines.get(location+__countdownKey) == null){
                productionTimer = "0s";
            }

            meta.setDisplayName(sendText("&6Machine Timer"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &fMachine Speed: &e"+getMainPlugin().events.placedMachines.get(location+__speedKey)+" seconds"));
            itemLore.add(sendText(" &fTime until next produce: &f"+productionTimer));
            itemLore.add(sendText(" "));
        }
        else if (name.equals("information")){
            item.setType(Material.PAPER);
            meta.setDisplayName(sendText("&dMachine Information"));
            itemLore.add(sendText(" "));
            itemLore.add(sendText(" &7Speed: &f"+getMainPlugin().events.placedMachines.get(location+__speedKey)));
            itemLore.add(sendText(" &7Production Rate: &f"+getMainPlugin().events.placedMachines.get(location+__productionRateKey)));
            itemLore.add(sendText(" &7Steam Consumption: &f"+getMainPlugin().events.placedMachines.get(location+__steamConsumptionKey)));
            itemLore.add(sendText(" &7Total Production: &f"+getMainPlugin().events.placedMachines.get(location+__totalProductionKey)));
            itemLore.add(sendText(" &7Durability: &f"+getMainPlugin().events.placedMachines.get(location+__durabilityKey)+"/"+getMainPlugin().events.placedMachines.get(location+__maxDurabilityKey)));
            itemLore.add(sendText(" "));
        }
        meta.setLore(itemLore);

        container.set(GetNamespacedKey("location"), PersistentDataType.STRING, ""+location);
        container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, name);

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getShopIcons(Player player, String name){
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        List<String> itemLore = new ArrayList<>();

        if (name.equals("buyamount_plus")){
            item.setType(Material.ARROW);
            meta.setDisplayName(sendText("&a[+] &7Buy Amount"));
        }
        else if (name.equals("buyamount_minus")){
            item.setType(Material.SPECTRAL_ARROW);
            meta.setDisplayName(sendText("&c[-] &7Buy Amount"));
        }

        else if (name.equals("buyamount_status")){
            item.setType(Material.PAPER);
            meta.setDisplayName(sendText("&7Buy Amount: &b"+buyAmount.get(player)));
        }

        else if (name.equals("back")){
            item.setType(Material.CHEST);
            meta.setDisplayName(sendText("&cReturn to Main Page"));
        }

        container.set(GetNamespacedKey("gui-icon"), PersistentDataType.STRING, name);

        meta.setLore(itemLore);
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
