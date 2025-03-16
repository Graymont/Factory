package org.factory.factory;

import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import it.unimi.dsi.fastutil.Hash;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.factory.factory.Utils.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.naming.Name;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.*;
import static org.factory.factory.Database.GetItem;
import static org.factory.factory.Database.events;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.Utils.FactoryItem.*;

import static org.factory.factory.Utils.FactoryMachine.*;
import static org.factory.factory.Utils.GUIManager.OpenMachineEngines;
import static org.factory.factory.Utils.GUIManager.OpenMenu;
import static org.factory.factory.Utils.PersistentDataManager.*;
import static org.factory.factory.Utils.SQLiteDatabase.parseLocationString;
import static org.factory.factory.Utils.UserInterface.*;
import static org.factory.factory.Utils.VaultEconomy.icon;

public class Events implements Listener {

    Factory plugin;
    SQLiteDatabase sqLiteDatabase;

    public Events (Factory pl){
        plugin = pl;
    }



    public String checkName(String text){
        return text.replaceAll("§.", "").trim().replaceAll("'s", "").trim();
    }

    public void DropItem(Location location, ItemStack item, int amount) {
        for (int i = 0; i < amount; i++) {
            Item droppedItem = location.getWorld().dropItem(location, item);
            Vector upward = new Vector(0, 0.2, 0);
            droppedItem.setVelocity(upward);
        }
    }

    void PlayerDebug(Player player, String message){
        player.sendMessage(sendText("&a"+message));
    }

    void ConsoleDebug(String message){
        consoleLog("&a"+message);
    }

    public HashMap<String, Double> playerAttributes = new HashMap<>();
    public HashMap<String, Integer> playerCooldown = new HashMap<>();

    public static HashMap<Player, Double> playerSteam = new HashMap<>();
    public static HashMap<Player, Double> playerMaxSteam = new HashMap<>();
    public static HashMap<Player, Double> playerArmor = new HashMap<>();

    public HashMap<String, String> placedMachines = new HashMap<>();
    public HashMap<Location, ItemStack> machineItems = new HashMap<>();
    public HashMap<UUID, List<ItemStack>> storedMachines = new HashMap<>();

    public HashMap<UUID, Integer> machineCount = new HashMap<>();
    public int defaultMaxMachine = 15;

    double defaultSteam = 20;
    double defaultMaxSteam = 20;

    public void InitAttributes(Player player){
        for (String attr : attributeList){
            playerAttributes.putIfAbsent(player.getName()+".attribute."+attr, 0.0);
        }
        for (String cd : cooldownList){
            playerCooldown.putIfAbsent(player.getName()+".cooldown."+cd, 0);
        }

        playerSteam.putIfAbsent(player, defaultSteam);
        playerMaxSteam.putIfAbsent(player, defaultMaxSteam);
        storedMachines.putIfAbsent(player.getUniqueId(), new ArrayList<>());

        consoleLog(sendText("&aAttributes of &2"+player.getName()+" &ahas been Initialized Successfully!"));
    }

    public void RefundMachines(Player player){
        storedMachines.putIfAbsent(player.getUniqueId(), new ArrayList<>());
        List<ItemStack> mList = new ArrayList<>(storedMachines.get(player.getUniqueId()));

        if (!mList.isEmpty()){
            List<ItemStack> removedList = new ArrayList<>();

            for (ItemStack item : mList) {
                ItemStack itemClone = item.clone();
                Map<Integer, ItemStack> addedItem = player.getInventory().addItem(itemClone);

                if (addedItem.isEmpty()) {
                    removedList.add(item);
                } else {
                    player.sendMessage(Notification_InventoryFull(player));
                    break;
                }
            }

            mList.removeAll(removedList);
            storedMachines.put(player.getUniqueId(), mList);
        }else{
            player.sendMessage(sendText("&4You don't have any stored machines right now!"));
            player.sendMessage(sendText(" "));
            player.sendMessage(sendText("&eThis feature is only used when you got kicked from somebody's island and your placed machines will be stored in here"));
        }
    }


    public void StartMachine(Player player,
                             Long speed,
                             Block block,
                             String dropName,
                             int potentialDrop,
                             ItemStack machine,
                             int productionRate,
                             int steamConsumption,
                             int durability,
                             int maxDurability,
                             int machineLevel,
                             Rarity.RarityType rarity,
                             String machineName,
                             String status,
                             int totalProduction,
                             String machineType,
                             int steamProduction
    ){
        Location location = block.getLocation();
        placedMachines.put(location+__machineStatusKey, status);
        placedMachines.put(location+__speedKey, ""+speed);

        if (placedMachines.get(location+__taskIdKey) != null){
            int taskIdToRemove = Integer.parseInt(placedMachines.get(location+__taskIdKey));
            Bukkit.getScheduler().cancelTask(taskIdToRemove);
            //PlayerDebug(player, "Removed taskId: "+taskIdToRemove);
        }

        /*Bukkit.getScheduler().runTaskLater(plugin, () -> {}, speed);*/
        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack drop = GetItem(dropName).clone();
                Random random = new Random();
                int dropChance = random.nextInt(100)+1;
                Vector offset = new Vector(0.5, 1, 0.5);
                Location dropLocation = block.getRelative(BlockFace.UP).getLocation().add(offset);

                double steam = playerSteam.get(player);

                int _durability = Integer.parseInt(placedMachines.get(location+__durabilityKey));
                int _maxDurability = Integer.parseInt(placedMachines.get(location+__maxDurabilityKey));
                String mType = placedMachines.get(location+__machineTypeKey);

                if (!mType.equals("steam")){
                    if (!hasSteam(player)){
                        return;
                    }

                    if (GetSteam(player) < steamConsumption){
                        return;
                    }
                }

                if (_durability <= 0){
                    if (placedMachines.get(location+__taskIdKey) != null){
                        if (!placedMachines.get(location+__machineStatusKey).equals("Disabled")){
                            if (!placedMachines.get(location+__machineStatusKey).equals("Broken")){
                                placedMachines.put(location+__machineStatusKey, "Broken");
                                UpdateMachineTag(player, location, machineName, Integer.parseInt(placedMachines.get(location+__taskIdKey)));
                                player.sendMessage(sendText(" "));
                                player.sendMessage(sendText(" &4⚠ &c&lWarning"));
                                player.sendMessage(sendText(" &fOne of your machine"));
                                player.sendMessage(sendText(" &fat &e"+LocationDisplay(location)+" &fis broken"));
                                player.sendMessage(sendText(" &fyou need to fix it so it can produce"));
                                player.sendMessage(sendText(" &fthings again!"));
                                player.sendMessage(sendText(" "));
                            }
                        }
                    }
                    return;
                }else{
                    if (placedMachines.get(location+__machineStatusKey).equals("Broken")){
                        placedMachines.put(location+__machineStatusKey, "Active");
                        UpdateMachineTag(player, location, machineName, Integer.parseInt(placedMachines.get(location+__taskIdKey)));
                    }

                    else if (placedMachines.get(location+__machineStatusKey).equals("Inactive")){
                        placedMachines.put(location+__machineStatusKey, "Active");
                        UpdateMachineTag(player, location, machineName, Integer.parseInt(placedMachines.get(location+__taskIdKey)));
                    }

                    else if (placedMachines.get(location+__machineStatusKey).equals("Disabled")){
                        return;
                    }
                }

                /*steam -= steamConsumption;
                if (steam < 0 ){
                    steam = 0;
                }
                playerSteam.put(player, steam);*/




                placedMachines.putIfAbsent(location+__countdownKey, ""+speed);
                int countdown = Integer.parseInt(placedMachines.get(location+__countdownKey));
                if (countdown <= 0){
                    countdown = Math.toIntExact(speed);
                    placedMachines.put(location+__countdownKey, ""+countdown);
                    if (!mType.equals("steam")){
                        RemoveSteam(player, steamConsumption);
                    }
                }
                else{
                    countdown--;
                    placedMachines.put(location+__countdownKey, ""+countdown);
                    return;
                }

                if (mType.equals("steam")){
                    int steamProduction = Integer.parseInt(placedMachines.get(location+__steamProductionKey));
                    AddSteam(player, steamProduction);

                    int currentProduction = Integer.parseInt(placedMachines.get(location+__totalProductionKey));
                    currentProduction++;
                    placedMachines.put(location+__totalProductionKey, ""+currentProduction);
                    PlaySoundAt(Sound.BLOCK_LAVA_EXTINGUISH, block.getLocation(), 0.3f, 1);
                    return;
                }

                if (dropChance <= 5 && potentialDrop >= 5){

                    ItemStack drop2 = new ItemStack(drop.clone());
                    ItemMeta meta = drop2.getItemMeta().clone();
                    meta.setDisplayName(sendText("&bMajestic "+uncolouredText(meta.getDisplayName())));
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    double worth = machineLevel*1.75;
                    container.set(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE, worth);
                    List<String> itemLore = new ArrayList<>();
                    itemLore.add(sendText("&9Machine Product"));
                    itemLore.add(sendText(" "));
                    itemLore.add(sendText(" &7Worth: &f"+FormatDouble(worth)+icon+" &7(+75%)"));
                    itemLore.add(sendText(" &8✧ &7Sell this item at &e/sellitem"));
                    itemLore.add(sendText(" &8✧ &e/sellall &7to sell all from your inventory"));
                    itemLore.add(sendText(" &8✧ &7or put in a chest for &f&nSell Wand &7Multiplier"));
                    itemLore.add(sendText(" "));
                    itemLore.add(sendText("&bLegendary"));
                    meta.setLore(itemLore);
                    drop2.setItemMeta(meta);
                    DropItem(dropLocation, drop2.clone(), productionRate);


                    int currentProduction = Integer.parseInt(placedMachines.get(location+__totalProductionKey));
                    currentProduction++;
                    placedMachines.put(location+__totalProductionKey, ""+currentProduction);
                }
                else if (dropChance <= 15 && potentialDrop >= 4){
                    ItemStack drop2 = new ItemStack(drop.clone());
                    ItemMeta meta = drop2.getItemMeta().clone();
                    meta.setDisplayName(sendText("&4Special "+uncolouredText(meta.getDisplayName())));
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    double worth = machineLevel*1.5;
                    container.set(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE, worth);
                    List<String> itemLore = new ArrayList<>();
                    itemLore.add(sendText("&9Machine Product"));
                    itemLore.add(sendText(" "));
                    itemLore.add(sendText(" &7Worth: &f"+FormatDouble(worth)+icon+" &7(+50%)"));
                    itemLore.add(sendText(" &8✧ &7Sell this item at &e/sellitem"));
                    itemLore.add(sendText(" &8✧ &e/sellall &7to sell all from your inventory"));
                    itemLore.add(sendText(" &8✧ &7or put in a chest for &f&nSell Wand &7Multiplier"));
                    itemLore.add(sendText(" "));
                    itemLore.add(sendText("&4Epic"));
                    meta.setLore(itemLore);
                    drop2.setItemMeta(meta);
                    DropItem(dropLocation, drop2.clone(), productionRate);

                    int currentProduction = Integer.parseInt(placedMachines.get(location+__totalProductionKey));
                    currentProduction++;
                    placedMachines.put(location+__totalProductionKey, ""+currentProduction);
                }

                else if (dropChance <= 30 && potentialDrop >= 3){
                    ItemStack drop2 = new ItemStack(drop.clone());
                    ItemMeta meta = drop2.getItemMeta().clone();
                    meta.setDisplayName(sendText("&6Good "+uncolouredText(meta.getDisplayName())));
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    double worth = machineLevel*1.2;
                    container.set(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE, worth);
                    List<String> itemLore = new ArrayList<>();
                    itemLore.add(sendText("&9Machine Product"));
                    itemLore.add(sendText(" "));
                    itemLore.add(sendText(" &7Worth: &f"+FormatDouble(worth)+icon+" &7(+20%)"));
                    itemLore.add(sendText(" &8✧ &7Sell this item at &e/sellitem"));
                    itemLore.add(sendText(" &8✧ &e/sellall &7to sell all from your inventory"));
                    itemLore.add(sendText(" &8✧ &7or put in a chest for &f&nSell Wand &7Multiplier"));
                    itemLore.add(sendText(" "));
                    itemLore.add(sendText("&6Rare"));
                    meta.setLore(itemLore);
                    drop2.setItemMeta(meta);
                    DropItem(dropLocation, drop2.clone(), productionRate);

                    int currentProduction = Integer.parseInt(placedMachines.get(location+__totalProductionKey));
                    currentProduction++;
                    placedMachines.put(location+__totalProductionKey, ""+currentProduction);
                }

                else if (dropChance <= 50 && potentialDrop >= 2){
                    ItemStack drop2 = new ItemStack(drop.clone());
                    ItemMeta meta = drop2.getItemMeta().clone();
                    meta.setDisplayName(sendText("&2Fine "+uncolouredText(meta.getDisplayName())));
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    double worth = machineLevel*1.1;
                    container.set(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE, worth);
                    List<String> itemLore = new ArrayList<>();
                    itemLore.add(sendText("&9Machine Product"));
                    itemLore.add(sendText(" "));
                    itemLore.add(sendText(" &7Worth: &f"+FormatDouble(worth)+icon+" &7(+10%)"));
                    itemLore.add(sendText(" &8✧ &7Sell this item at &e/sellitem"));
                    itemLore.add(sendText(" &8✧ &e/sellall &7to sell all from your inventory"));
                    itemLore.add(sendText(" &8✧ &7or put in a chest for &f&nSell Wand &7Multiplier"));
                    itemLore.add(sendText(" "));
                    itemLore.add(sendText("&2Uncommon"));
                    meta.setLore(itemLore);
                    drop2.setItemMeta(meta);
                    DropItem(dropLocation, drop2.clone(), productionRate);

                    int currentProduction = Integer.parseInt(placedMachines.get(location+__totalProductionKey));
                    currentProduction++;
                    placedMachines.put(location+__totalProductionKey, ""+currentProduction);
                }

                else{
                    ItemStack drop2 = new ItemStack(drop.clone());
                    ItemMeta meta = drop2.getItemMeta().clone();
                    meta.setDisplayName(sendText("&a"+meta.getDisplayName()));
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    double worth = machineLevel;
                    container.set(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE, worth);
                    List<String> itemLore = new ArrayList<>();
                    itemLore.add(sendText("&9Machine Product"));
                    itemLore.add(sendText(" "));
                    itemLore.add(sendText(" &7Worth: &f"+FormatDouble(worth)+icon));
                    itemLore.add(sendText(" &8✧ &7Sell this item at &e/sellitem"));
                    itemLore.add(sendText(" &8✧ &e/sellall &7to sell all from your inventory"));
                    itemLore.add(sendText(" &8✧ &7or put in a chest for &f&nSell Wand&7 Multiplier"));
                    itemLore.add(sendText(" "));
                    itemLore.add(sendText("&aCommon"));
                    meta.setLore(itemLore);
                    drop2.setItemMeta(meta);
                    DropItem(dropLocation, drop2.clone(), productionRate);

                    int currentProduction = Integer.parseInt(placedMachines.get(location+__totalProductionKey));
                    currentProduction++;
                    placedMachines.put(location+__totalProductionKey, ""+currentProduction);
                }

                // remove the durability
                _durability -= 1;
                placedMachines.put(location+__durabilityKey, ""+_durability);
                PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, block.getLocation(), 0.3f, 1);
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();

        placedMachines.put(location+__locationKey, ""+location);
        placedMachines.put(location+__ownerKey, player.getName());
        placedMachines.put(location+__uuidKey, ""+player.getUniqueId());
        placedMachines.put(location+__taskIdKey, ""+taskId);
        placedMachines.put(location+__productionRateKey, ""+productionRate);
        placedMachines.put(location+__steamConsumptionKey, ""+steamConsumption);
        placedMachines.put(location+__durabilityKey, ""+durability);
        placedMachines.put(location+__maxDurabilityKey, ""+maxDurability);
        placedMachines.put(location+__machineLevelKey, ""+machineLevel);
        placedMachines.put(location+__dropNameKey, dropName);
        placedMachines.put(location+__potentialDropKey, ""+potentialDrop);
        placedMachines.put(location+__machineNameKey, machineName);
        placedMachines.put(location+__rarityKey, rarity.toString());
        placedMachines.put(location+__machineStatusKey, status);
        placedMachines.put(location+__totalProductionKey, ""+totalProduction);

        placedMachines.put(location+__machineTypeKey, ""+machineType);
        placedMachines.put(location+__steamProductionKey, ""+steamProduction);
        machineItems.put(location, machine.clone());

        UpdateMachineTag(player, location, machineName, taskId);
    }

    @EventHandler
    public void OnJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        machineCount.putIfAbsent(player.getUniqueId(), 0);

        InitAttributes(player);
        StartAllMachines(player);
    }

    @EventHandler
    public void OnQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        StopAllMachines(player);
    }

    public void EnableMachine(Player player, Location location){
        long speed = Long.parseLong(placedMachines.get(location + __speedKey));
        int productionRate = Integer.parseInt(placedMachines.get(location + __productionRateKey));
        int steamConsumption = Integer.parseInt(placedMachines.get(location + __steamConsumptionKey));
        int durability = Integer.parseInt(placedMachines.get(location + __durabilityKey));
        int maxDurability = Integer.parseInt(placedMachines.get(location + __maxDurabilityKey));
        int machineLevel = Integer.parseInt(placedMachines.get(location + __machineLevelKey));
        int potentialDrop = Integer.parseInt(placedMachines.get(location + __potentialDropKey));
        String dropName = placedMachines.get(location + __dropNameKey);
        String rarity = placedMachines.get(location + __rarityKey);
        String machineName = placedMachines.get(location + __machineNameKey);
        String status = placedMachines.get(location + __machineStatusKey);
        int totalProduction = Integer.parseInt(placedMachines.get(location + __totalProductionKey));

        String machineType = placedMachines.get(location + __machineTypeKey);
        int steamProduction = Integer.parseInt(placedMachines.get(location + __steamProductionKey));

        ItemStack machine = machineItems.get(location);
        World world = location.getWorld();

        if (location.getBlock().getType() == Material.AIR){
            location.getBlock().setType(machineItems.get(location).getType());
        }

        Block block = world.getBlockAt(location);
        StartMachine(player, speed, block, dropName, potentialDrop, machine, productionRate, steamConsumption,
                durability, maxDurability, machineLevel, Rarity.RarityType.parseRarity(rarity), machineName, status, totalProduction,
                machineType, steamProduction);
        player.sendMessage(sendText("&aMachine has been enabled!"));
    }

    public void StartAllMachines(Player player){
        int mCount = 0;

        Set<String> keys = new HashSet<>(placedMachines.keySet());

        for (String key : keys) {
            if (key.endsWith(__locationKey)) {
                String location = key.replace(__locationKey, "");
                if (placedMachines.containsKey(location + __ownerKey) &&
                        placedMachines.get(location + __ownerKey).equals(player.getName())) {

                    mCount++;
                    Location locationParsed = parseLocationString(location);

                    if (locationParsed == null) {
                        consoleLog(sendText("&cError: Invalid machine location for " + player.getName()));
                        continue;
                    }

                    try {
                        long speed = Long.parseLong(placedMachines.get(location + __speedKey));
                        int productionRate = Integer.parseInt(placedMachines.get(location + __productionRateKey));
                        int steamConsumption = Integer.parseInt(placedMachines.get(location + __steamConsumptionKey));
                        int durability = Integer.parseInt(placedMachines.get(location + __durabilityKey));
                        int maxDurability = Integer.parseInt(placedMachines.get(location + __maxDurabilityKey));
                        int machineLevel = Integer.parseInt(placedMachines.get(location + __machineLevelKey));
                        int potentialDrop = Integer.parseInt(placedMachines.get(location + __potentialDropKey));
                        String dropName = placedMachines.get(location + __dropNameKey);
                        String rarity = placedMachines.get(location + __rarityKey);
                        String machineName = placedMachines.get(location + __machineNameKey);
                        String status = placedMachines.get(location + __machineStatusKey);

                        String machineType = placedMachines.get(location + __machineTypeKey);
                        int steamProduction = Integer.parseInt(placedMachines.get(location + __steamProductionKey));

                        if (status.equals("Inactive")){
                            status = "Active";
                        }

                        int totalProduction = Integer.parseInt(placedMachines.get(location + __totalProductionKey));

                        ItemStack machine = machineItems.get(locationParsed);
                        World world = locationParsed.getWorld();

                        if (world == null) {
                            consoleLog(sendText("&cError: World not found for machine at " + location));
                            continue;
                        }

                        if (locationParsed.getBlock().getType() == Material.AIR){
                            locationParsed.getBlock().setType(machineItems.get(locationParsed).getType());
                            consoleLog(sendText("&aBlock at &2"+locationParsed+" &ais air! so it replaced with: &6"+machineItems.get(locationParsed).getType()));
                        }

                        Block block = world.getBlockAt(locationParsed);
                        StartMachine(player, speed, block, dropName, potentialDrop, machine, productionRate, steamConsumption,
                                durability, maxDurability, machineLevel, Rarity.RarityType.parseRarity(rarity), machineName, status, totalProduction,
                                machineType, steamProduction);

                    } catch (NumberFormatException e) {
                        consoleLog(sendText("&cError: Invalid number format in machine data for " + location));
                    }
                }
            }
        }

        machineCount.put(player.getUniqueId(), mCount);
        consoleLog(sendText("&aAll Machines owned by &2" + player.getName() + " &ahas been started with &6" + machineCount.get(player.getUniqueId()) + " Machines!"));
    }

    public void DisableMachine(Player player, Location location){
        assert location != null;
        int taskId = Integer.parseInt(placedMachines.get(location+__taskIdKey));
        placedMachines.put(location+__machineStatusKey, "Disabled");
        Bukkit.getScheduler().cancelTask(taskId);
        String machineName = placedMachines.get(location+__machineNameKey);
        UpdateMachineTag(player, location, machineName, taskId);
        player.sendMessage(sendText("&cMachine has stopped!"));
    }

    public void StopAllMachines(Player player){
        for (String key : placedMachines.keySet()) {
            if (key.endsWith(__locationKey)) {
                String location = key.replace(__locationKey, "");
                Location parsedLocation = parseLocationString(location);
                assert parsedLocation != null;
                int taskId = Integer.parseInt(placedMachines.get(location+__taskIdKey));
                if (!placedMachines.get(location+__machineStatusKey).equals("Disabled")){
                    placedMachines.put(location+__machineStatusKey, "Inactive");
                }
                Bukkit.getScheduler().cancelTask(taskId);
                String machineName = placedMachines.get(location+__machineNameKey);
                UpdateMachineTag(player, parsedLocation, machineName, taskId);
            }
        }
        consoleLog(sendText("&aAll Machines owner by &2"+player.getName()+" &ahas been stopped!"));
    }

    @EventHandler
    public void MachinePlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        Block block = event.getBlock();
        Location location = block.getLocation();

        if (block.getType() != Material.AIR){
            ItemStack item = player.getInventory().getItemInMainHand().clone();
            if (!isMachine(item)){
                return;
            }
            if (event.isCancelled()){
                return;
            }
            ItemMeta meta = item.getItemMeta();
            item.setAmount(1);
            NamespacedKey key = GetNamespacedKey(machineKey);
            //NamespacedKey dropKey = new NamespacedKey(plugin, "machine_drop");
            //NamespacedKey potentialKey = new NamespacedKey(plugin, "machine_potentialDrop");
            PersistentDataContainer container = meta.getPersistentDataContainer();

            if (!container.has(key)){
                return;
            }

            if (playerCooldown.get(player.getName()+".cooldown.Place Machine") > 0){
                event.setCancelled(true);
                int cooldown = playerCooldown.get(player.getName()+".cooldown.Place Machine");
                player.sendMessage(sendText("&4Please wait &6"+cooldown+"s &4before placing machine again!"));
                PlaySoundAt(Sound.ENTITY_VILLAGER_NO, player.getLocation(), 1, 2);
                return;
            }

            playerCooldown.put(player.getName()+".cooldown.Place Machine", 2);
            int mc = machineCount.get(player.getUniqueId());
            if (mc >= defaultMaxMachine){
                event.setCancelled(true);
                player.sendMessage(sendText("&4You already reach the maximum machine placement!"));
                PlaySoundAt(Sound.BLOCK_MUD_BRICKS_PLACE, location, 1, 3);
                return;
            }

            mc++;
            machineCount.put(player.getUniqueId(), mc);

            Long speed = 20L;
            Integer productionRate = 1;
            Integer steamConsumption = 10;
            Integer durability = 1000;
            Integer maxDurability = 1000;
            Integer machineLevel = 1;
            Integer potentialDrop = 1;

            speed = container.get(GetNamespacedKey(speedKey), PersistentDataType.LONG);
            productionRate = container.get(GetNamespacedKey(productionRateKey), PersistentDataType.INTEGER);
            steamConsumption = container.get(GetNamespacedKey(steamConsumptionKey), PersistentDataType.INTEGER);
            machineLevel = container.get(GetNamespacedKey(machineLevelKey), PersistentDataType.INTEGER);
            durability = container.get(GetNamespacedKey(durabilityKey), PersistentDataType.INTEGER);
            maxDurability = container.get(GetNamespacedKey(maxDurabilityKey), PersistentDataType.INTEGER);
            potentialDrop = container.get(GetNamespacedKey(potentialDropKey), PersistentDataType.INTEGER);

            String dropName = container.get(GetNamespacedKey(dropNameKey), PersistentDataType.STRING);
            String rarity = container.get(GetNamespacedKey(rarityKey), PersistentDataType.STRING);
            String machineName = meta.getDisplayName();

            String status = container.get(GetNamespacedKey(machineStatusKey), PersistentDataType.STRING);
            Integer totalProduction = container.get(GetNamespacedKey(totalProductionKey), PersistentDataType.INTEGER);

            String machineType = container.get(GetNamespacedKey(machineTypeKey), PersistentDataType.STRING);
            Integer steamProduction = container.get(GetNamespacedKey(steamProductionKey), PersistentDataType.INTEGER);

            StartMachine(player, speed, block, dropName, potentialDrop, item.clone(), productionRate,
                    steamConsumption, durability, maxDurability, machineLevel, Rarity.RarityType.parseRarity(rarity), machineName,
                    status, totalProduction, machineType, steamProduction);

            PlaySoundAt(Sound.BLOCK_ANVIL_PLACE, location, 1, 3);

            player.sendMessage(" ");
            player.sendMessage(sendText("&bPlaced Machine "+meta.getDisplayName()+" &6["+mc+"&e/&6"+defaultMaxMachine+"]"));
        }
    }


    @EventHandler
    public void MachineAction(PlayerInteractEvent event){
        Player player = event.getPlayer();
        // Machine Take
        if (event.getAction() == Action.LEFT_CLICK_BLOCK){
            Block block = event.getClickedBlock();
            if (block != null){
                Location location = block.getLocation();
                //Location hologramLocation = parseLocationString(placedMachines.get(location+".location"));

                if (placedMachines.get(location+__ownerKey) != null){

                    if (playerCooldown.get(player.getName()+".cooldown.Place Machine") > 0){
                        event.setCancelled(true);
                        int cooldown = playerCooldown.get(player.getName()+".cooldown.Place Machine");
                        player.sendMessage(sendText("&4Please wait &6"+cooldown+"s &4before interacting with machine again!"));
                        PlaySoundAt(Sound.ENTITY_VILLAGER_NO, player.getLocation(), 1, 2);
                        return;
                    }
                    String owner = placedMachines.get(location+__ownerKey);
                    int machineLevel = Integer.parseInt(placedMachines.get(location+__machineLevelKey));
                    if (owner.equals(player.getName())){

                        if (player.isSneaking()){
                            OpenMachineEngines(player, location);
                            event.setCancelled(true);
                            return;
                        }

                        playerCooldown.put(player.getName()+".cooldown.Place Machine", 2);
                        RemoveMachine(player.getUniqueId(), block, location);
                    }else{
                        player.sendMessage(sendText("&4This is not your machine! &c(owned by: "+owner+")"));
                        PlaySoundAt(Sound.BLOCK_NOTE_BLOCK_BIT, location, 1, 0);
                    }
                }
            }
        }

    }


    @EventHandler
    public void OnBreakMachine(BlockBreakEvent event){
        Player player = event.getPlayer();

        Block block = event.getBlock();

        Location location = block.getLocation();
        if (placedMachines.get(location+__ownerKey) != null){
            event.setCancelled(true);
        }
    }

    public void RemoveMachine(UUID player, Block block, Location location){
        String taskIdString = placedMachines.get(location+__taskIdKey);
        int taskId = Integer.parseInt(taskIdString);

        ItemStack obtainedMachine = machineItems.get(location).clone();

        ItemMeta meta = obtainedMachine.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        String name = uncolouredText(meta.getDisplayName());
        int machineLevel = Integer.parseInt(placedMachines.get(location+__machineLevelKey));
        long speed = Long.parseLong(placedMachines.get(location+__speedKey));
        int productionRate = Integer.parseInt(placedMachines.get(location+__productionRateKey));
        int steamConsumption = Integer.parseInt(placedMachines.get(location+__steamConsumptionKey));
        int durability = Integer.parseInt(placedMachines.get(location+__durabilityKey));
        int maxDurability = Integer.parseInt(placedMachines.get(location+__maxDurabilityKey));
        Material material = obtainedMachine.getType();
        String dropName = container.get(GetNamespacedKey(dropNameKey), PersistentDataType.STRING);
        int potentialDrop = Integer.parseInt(placedMachines.get(location+__potentialDropKey));
        String rarity = container.get(GetNamespacedKey(rarityKey), PersistentDataType.STRING);

        String status = placedMachines.get(location+__machineStatusKey);
        int totalProduction = Integer.parseInt(placedMachines.get(location+__totalProductionKey));

        String machineType = placedMachines.get(location+__machineTypeKey);
        int steamProduction = Integer.parseInt(placedMachines.get(location+__steamProductionKey));

        obtainedMachine = new ItemStack(CreateMachine(name, machineLevel, speed, productionRate
                , steamConsumption, durability, maxDurability, material, dropName, potentialDrop, Rarity.RarityType.parseRarity(rarity),
                status, totalProduction, MachineType.parseType(machineType), steamProduction));

        Player onlinePlayer = Bukkit.getPlayer(player);

        if (onlinePlayer != null){
            Map<Integer, ItemStack> mapItem = onlinePlayer.getInventory().addItem(obtainedMachine.clone());

            if (!mapItem.isEmpty()){
                StoreMachine(player, obtainedMachine);
                onlinePlayer.sendMessage(Notification_InventoryFull(onlinePlayer));
            }
        }else{
            StoreMachine(player, obtainedMachine);
        }

        Bukkit.getScheduler().cancelTask(taskId);
        //long speed = Long.parseLong(placedMachines.get(location+".speed"));

        RemoveMachineAttributes(location);
        /*placedMachines.remove(location+__locationKey);
        placedMachines.remove(location+__ownerKey);
        placedMachines.remove(location+__uuidKey);
        placedMachines.remove(location+__machineLevelKey);
        placedMachines.remove(location+__productionRateKey);
        placedMachines.remove(location+__taskIdKey);
        placedMachines.remove(location+__speedKey);
        placedMachines.remove(location+__steamConsumptionKey);
        placedMachines.remove(location+__durabilityKey);
        placedMachines.remove(location+__maxDurabilityKey);
        placedMachines.remove(location+__dropNameKey);
        placedMachines.remove(location+__potentialDropKey);
        machineItems.remove(location);*/

        block.setType(Material.AIR);

        int mc = machineCount.get(player);
        mc--;
        if (mc<0) mc = 0;
        machineCount.put(player, mc);

        /*PlayerDebug(player, "Machine Owner: "+player.getName());
        PlayerDebug(player, "Machine TaskId: "+taskIdString);
        PlayerDebug(player, "Machine Speed: "+speed);*/

        if (onlinePlayer != null){
            onlinePlayer.sendMessage(" ");
            onlinePlayer.sendMessage(sendText("&bRemoved Machine "+obtainedMachine.getItemMeta().getDisplayName()+" &6["+mc+"&e/&6"+defaultMaxMachine+"]"));
        }

        RemoveMachineTags(location, taskId);
        PlaySoundAt(Sound.BLOCK_CHEST_CLOSE, location, 1, 3);
    }

    public void StoreMachine(UUID player, ItemStack obtainedMachine){
        storedMachines.putIfAbsent(player, new ArrayList<>());
        List<ItemStack> storedMachineList = new ArrayList<>(storedMachines.get(player));
        storedMachineList.add(obtainedMachine.clone());
        storedMachines.put(player, storedMachineList);
        consoleLog(sendText("&2"+Bukkit.getOfflinePlayer(player).getName()+" &ais offline or having full inventory so machine items are stored in &6[StoredMachines]"));
    }

    private void RemoveMachineAttributes(Location location){
        placedMachines.remove(location+__locationKey);
        placedMachines.remove(location+__ownerKey);
        placedMachines.remove(location+__uuidKey);
        placedMachines.remove(location+__machineLevelKey);
        placedMachines.remove(location+__productionRateKey);
        placedMachines.remove(location+__taskIdKey);
        placedMachines.remove(location+__speedKey);
        placedMachines.remove(location+__steamConsumptionKey);
        placedMachines.remove(location+__durabilityKey);
        placedMachines.remove(location+__maxDurabilityKey);
        placedMachines.remove(location+__dropNameKey);
        placedMachines.remove(location+__potentialDropKey);
        placedMachines.remove(location+__machineStatusKey);
        placedMachines.remove(location+__totalProductionKey);
        placedMachines.remove(location+__countdownKey);
        placedMachines.remove(location+__machineTypeKey);
        placedMachines.remove(location+__steamProductionKey);
        machineItems.remove(location);

        consoleLog(sendText("&aAll Machine Attributes at &2"+location+" &ahas been reset!"));
    }

    public void RemoveMachineTags(Location location, int taskId){
        World world = location.getWorld();
        for (Entity entity : Bukkit.getWorld(world.getName()).getEntities()){
            if (entity instanceof TextDisplay){
                if (entity.hasMetadata("MachineTag."+location)){
                    entity.remove();
                }
            }
        }
    }

    public void SpawnMachineTag(String player, Location location, String machineName, int taskId) {
        Vector tagOffset = new Vector(0.5, 2.5, 0.5);
        Location spawnLocation = location.clone().add(tagOffset);


        double lineSpacing = 0.3;

        Location spawnLocation2 = spawnLocation.clone().add(0, -lineSpacing, 0);
        Location spawnLocation3 = spawnLocation2.clone().add(0, -lineSpacing, 0);
        Location spawnLocation4 = spawnLocation3.clone().add(0, -lineSpacing, 0);
        Location spawnLocation5 = spawnLocation4.clone().add(0, -lineSpacing, 0);


        String machineStatus = placedMachines.get(location + ".status");
        String statusColor = "&a✔ ";
        if ("Broken".equals(machineStatus)) {
            statusColor = "&c✗ ";
        } else if ("Inactive".equals(machineStatus)) {
            statusColor = "&4\uD83D\uDC80 ";
        }
        else if ("Disabled".equals(machineStatus)) {
            statusColor = "&8⚠ ";
        }


        spawnMachineTag(spawnLocation, sendText(machineName), location);
        spawnMachineTag(spawnLocation2, "&8[ " + statusColor + machineStatus + " &8]", location);
        spawnMachineTag(spawnLocation3, "&8[ &fOwner: &e" + player + " &8]", location);
        spawnMachineTag(spawnLocation4, "&8[ &9Left-Click &fto &3Take &8]", location);
        spawnMachineTag(spawnLocation5, "&8[ &9Sneak &f+ &9Left-Click &fto &3Open &8]", location);
    }


    public void UpdateMachineTag(Player player, Location location, String machineName, int taskId){
        World world = location.getWorld();
        for (Entity entity : Bukkit.getWorld(world.getName()).getEntities()){
            if (entity instanceof TextDisplay){
                if (entity.hasMetadata("MachineTag."+location)){
                    entity.remove();
                }
            }
        }
        SpawnMachineTag(player.getName(), location, machineName, taskId);
    }



    public void ModifyMachine(Player player, String key, Object keyValue) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            player.sendMessage(sendText("&4Your item can't be air!"));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey machineKey = new NamespacedKey(plugin, "machine");
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (!container.has(machineKey)) {
            player.sendMessage(sendText("&4This item is not a machine!"));
            return;
        }

        NamespacedKey statsKey = new NamespacedKey(plugin, "machine_"+key);

        if (container.has(statsKey, PersistentDataType.INTEGER) && keyValue instanceof Integer) {
            container.set(statsKey, PersistentDataType.INTEGER, (Integer) keyValue);
        } else if (container.has(statsKey, PersistentDataType.STRING) && keyValue instanceof String) {
            container.set(statsKey, PersistentDataType.STRING, (String) keyValue);
        } else if (container.has(statsKey, PersistentDataType.DOUBLE) && keyValue instanceof Double) {
            container.set(statsKey, PersistentDataType.DOUBLE, (Double) keyValue);
        } else if (container.has(statsKey, PersistentDataType.BYTE) && keyValue instanceof Byte) {
            container.set(statsKey, PersistentDataType.BYTE, (Byte) keyValue);
        }
        else if (container.has(statsKey, PersistentDataType.LONG) && keyValue instanceof Long) {
            container.set(statsKey, PersistentDataType.LONG, (Long) keyValue);
        }else {
            player.sendMessage(sendText("&4Invalid data type!"));
            return;
        }

        item.setItemMeta(meta);
        item.setItemMeta(UpdateMachineItem(item).getItemMeta());

        player.sendMessage(sendText("&aMachine data modified successfully! &2("+key+" &8- &2"+keyValue+")"));
    }

    // String name, int machineLevel, long speed, int productionRate, int steamConsumption,
    // int durability, int maxDurability, Material material, String drop, int potentialDrop, String rarity




    public void spawnMachineTag(Location location, String name, Location machineLocation) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            World world = location.getWorld();
            if (world == null) return;

            TextDisplay textDisplay = (TextDisplay) world.spawnEntity(location, EntityType.TEXT_DISPLAY);

            textDisplay.setText(sendText(name));
            textDisplay.setBillboard(Display.Billboard.CENTER);
            textDisplay.setShadowed(false);
            textDisplay.setSeeThrough(false);
            textDisplay.setLineWidth(300);
            textDisplay.setViewRange(30.0f);

            textDisplay.setMetadata("MachineTag." + machineLocation, new FixedMetadataValue(plugin, true));

        }, 5L);

        /*Bukkit.getScheduler().runTaskLater(plugin, () -> {

        }, 5L);*/
    }



    public void PlayerInventoryItems(Player player){
        PlayerItemAttributes(player);
        PlayerMiningSpeed(player);
    }

    public void PlayerItemAttributes(Player player){
        for (ItemStack item : player.getInventory()) {
            if (item != null && item.getType() != Material.AIR ) {
                if (isTool(item)){
                    ItemMeta meta = item.getItemMeta();

                    if (!meta.isUnbreakable()) {
                        meta.setUnbreakable(true);
                    }

                    if (!meta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES) || !meta.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)) {


                        NamespacedKey damageKey = new NamespacedKey(plugin, "attack_damage");
                        AttributeModifier damageModifier = new AttributeModifier(damageKey, -100, AttributeModifier.Operation.ADD_NUMBER);

                        NamespacedKey speedKey = new NamespacedKey(plugin, "attack_speed");
                        AttributeModifier speedModifier = new AttributeModifier(speedKey, 0, AttributeModifier.Operation.ADD_NUMBER);

                        if (meta.getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE) == null ||
                                meta.getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE).stream().noneMatch(modifier ->
                                        modifier.getKey().equals(damageKey))) {

                            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier);
                        }

                        if (meta.getAttributeModifiers(Attribute.GENERIC_ATTACK_SPEED) == null ||
                                meta.getAttributeModifiers(Attribute.GENERIC_ATTACK_SPEED).stream().noneMatch(modifier ->
                                        modifier.getKey().equals(speedKey))) {

                            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, speedModifier);
                        }

                        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        meta.addItemFlags(ItemFlag.HIDE_STORED_ENCHANTS);
                        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                        meta.addItemFlags(ItemFlag.HIDE_DYE);

                        item.setItemMeta(meta);
                    }
                }else{
                    if (!isMachine(item) && !isFactoryItem(item)){
                        if (!item.getItemMeta().hasLore() && !item.getItemMeta().hasDisplayName()){
                            item.setItemMeta(ProcessItemMeta(item).getItemMeta());
                        }
                    }
                }
            }
        }
    }

    public void PlayerMiningSpeed(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR) {
            ItemMeta itemMeta = item.getItemMeta();

            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            if (!container.has(GetNamespacedKey(itemKey))){
                return;
            }
            if (!container.has(GetNamespacedKey(typeKey)) || !container.has(GetNamespacedKey(subTypeKey))){
                return;
            }
            if (!container.get(GetNamespacedKey(typeKey), PersistentDataType.STRING).equals("tool")){
                return;
            }

            int defaultPower = 0;
            Double power = container.get(GetNamespacedKey(toolPowerKey), PersistentDataType.DOUBLE);
            double enchantValue = 1;

            if (item.getType() == Material.GOLDEN_PICKAXE){
                defaultPower = 25;
            }

            double powerValue = (int) ((defaultPower+power)*enchantValue);

            if (isPickaxe(item) || isAxe(item) || isShovel(item)){
                AttributeInstance miningSpeed = player.getAttribute(Attribute.PLAYER_MINING_EFFICIENCY);
                if (miningSpeed != null) {
                    if (miningSpeed.getValue() != powerValue){
                        miningSpeed.setBaseValue(powerValue);
                        PlaySound(Sound.BLOCK_NOTE_BLOCK_CHIME, player, 1, 2);
                    }
                }
            }
        }
    }

    @EventHandler
    public void OnPlayerMining(BlockBreakEvent event){
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.AIR){
            ItemMeta meta = item.getItemMeta();

            if (event.isCancelled()){
                return;
            }

            PersistentDataContainer container = meta.getPersistentDataContainer();

            if (container.has(GetNamespacedKey(itemKey))){

                if (!ItemNotBroken(item)){
                    Notification_ItemBroken(player);
                    event.setCancelled(true);
                    return;
                }

                ManageDurability(player, "hand");
                UpdateItem(player, "hand");
            }
        }

    }

    public static List<String> attributeList = Arrays.asList(
      "Steam", "Movement Speed", "Steam Regen", "Attack Damage", "Attack Range", "Attack Speed", "Critical Chance", "Critical Damage"
            , "Accuracy", "Armor", "Undead Damage", "Undead Defense", "Mutant Damage", "Mutant Defense",
            "Melee Damage", "Range Damage", "Steam Consumption", "Health"
    );

    public List<String> cooldownList = Arrays.asList(
            "Place Machine"
    );

    @EventHandler
    public void OnItemSpawn(ItemSpawnEvent event){
        ItemStack item = event.getEntity().getItemStack();
        if (!isTool(item) && !isFactoryItem(item)){
            if (item.getType() != Material.AIR){
                ItemMeta meta = item.getItemMeta();
                if (!meta.hasLore() && !meta.hasDisplayName()){
                    event.getEntity().getItemStack().setItemMeta(ProcessItemMeta(event.getEntity().getItemStack()).getItemMeta());
                }
            }
        }
    }

    @EventHandler
    public void OnItemCraft(PrepareItemCraftEvent event){
        if (event.getInventory().getResult() != null){
            ItemStack item = event.getInventory().getResult();
            if (!isTool(item)){
                event.getInventory().getResult().setItemMeta(ProcessItemMeta(event.getInventory().getResult()).getItemMeta());
            }
        }
    }

    public void InventoryItemCheck(Player player){
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();

            String attributesKey = "hand";

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(GetNamespacedKey("item"))){
                return;
            }

            if (isWeapon(item)){
                if (!ItemNotBroken(item)){
                    return;
                }
                //player.sendMessage("this is weapon");

                for (String attr : attributeList) {
                    playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + FixedAttributes(attr), 0.0);
                }

                for (NamespacedKey key : container.getKeys()) {
                    String fixedKey = key.getKey().toLowerCase().replaceAll(" ", "").trim();
                    /*consoleLog("key: "+key);
                    consoleLog("fixedKey: "+fixedKey);*/
                    if (isValidAttributes(fixedKey)) {
                        double a = playerAttributes.get(player.getName() + ".attribute."+attributesKey+"." + fixedKey);
                        Double keyValue = container.get(key, PersistentDataType.DOUBLE);
                        assert keyValue != null;
                        a += keyValue;
                        playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + fixedKey, a);
                        //player.sendMessage("1");
                    }
                }
            }else {
                for (String attr : attributeList) {
                    playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + FixedAttributes(attr), 0.0);
                }
                //player.sendMessage("not weapon");
            }
        } else {
            for (String attr : attributeList) {
                playerAttributes.put(player.getName() + ".attribute.hand." + FixedAttributes(attr), 0.0);
            }
        }

        if (player.getInventory().getHelmet() != null) {
            ItemStack item = player.getInventory().getHelmet();
            ItemMeta meta = item.getItemMeta();

            String attributesKey = "helmet";

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(GetNamespacedKey("item"))){
                return;
            }

            if (isHelmet(item)){
                if (!ItemNotBroken(item)){
                    return;
                }

                for (String attr : attributeList) {
                    playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + FixedAttributes(attr), 0.0);
                }

                for (NamespacedKey key : container.getKeys()) {
                    String fixedKey = key.getKey().toLowerCase().replaceAll(" ", "").trim();
                    if (isValidAttributes(fixedKey)) {
                        double a = playerAttributes.get(player.getName() + ".attribute."+attributesKey+"." + fixedKey);
                        Double keyValue = container.get(key, PersistentDataType.DOUBLE);
                        assert keyValue != null;
                        a += keyValue;
                        playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + fixedKey, a);
                    }
                }
            }else {
                for (String attr : attributeList) {
                    playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + FixedAttributes(attr), 0.0);
                }
            }
        } else {
            for (String attr : attributeList) {
                playerAttributes.put(player.getName() + ".attribute.helmet." + FixedAttributes(attr), 0.0);
            }
        }

        if (player.getInventory().getChestplate() != null) {
            ItemStack item = player.getInventory().getChestplate();
            ItemMeta meta = item.getItemMeta();

            String attributesKey = "chestplate";

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(GetNamespacedKey("item"))){
                return;
            }

            if (isChestplate(item)){
                if (!ItemNotBroken(item)){
                    return;
                }

                for (String attr : attributeList) {
                    playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + FixedAttributes(attr), 0.0);
                }

                for (NamespacedKey key : container.getKeys()) {
                    String fixedKey = key.getKey().toLowerCase().replaceAll(" ", "").trim();
                    if (isValidAttributes(fixedKey)) {
                        double a = playerAttributes.get(player.getName() + ".attribute."+attributesKey+"." + fixedKey);
                        Double keyValue = container.get(key, PersistentDataType.DOUBLE);
                        assert keyValue != null;
                        a += keyValue;
                        playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + fixedKey, a);
                    }
                }
            }else {
                for (String attr : attributeList) {
                    playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + FixedAttributes(attr), 0.0);
                }
            }
        } else {
            for (String attr : attributeList) {
                playerAttributes.put(player.getName() + ".attribute.chestplate." + FixedAttributes(attr), 0.0);
            }
        }

        if (player.getInventory().getLeggings() != null) {
            ItemStack item = player.getInventory().getLeggings();
            ItemMeta meta = item.getItemMeta();

            String attributesKey = "leggings";

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(GetNamespacedKey("item"))){
                return;
            }

            if (isLeggings(item)){
                if (!ItemNotBroken(item)){
                    return;
                }

                for (String attr : attributeList) {
                    playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + FixedAttributes(attr), 0.0);
                }

                for (NamespacedKey key : container.getKeys()) {
                    String fixedKey = key.getKey().toLowerCase().replaceAll(" ", "").trim();
                    if (isValidAttributes(fixedKey)) {
                        double a = playerAttributes.get(player.getName() + ".attribute."+attributesKey+"." + fixedKey);
                        Double keyValue = container.get(key, PersistentDataType.DOUBLE);
                        assert keyValue != null;
                        a += keyValue;
                        playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + fixedKey, a);
                    }
                }
            }else {
                for (String attr : attributeList) {
                    playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + FixedAttributes(attr), 0.0);
                }
            }
        } else {
            for (String attr : attributeList) {
                playerAttributes.put(player.getName() + ".attribute.leggings." + FixedAttributes(attr), 0.0);
            }
        }

        if (player.getInventory().getBoots() != null) {
            ItemStack item = player.getInventory().getBoots();
            ItemMeta meta = item.getItemMeta();

            String attributesKey = "boots";

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(GetNamespacedKey("item"))){
                return;
            }

            if (isBoots(item)){
                if (!ItemNotBroken(item)){
                    return;
                }

                for (String attr : attributeList) {
                    playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + FixedAttributes(attr), 0.0);
                }

                for (NamespacedKey key : container.getKeys()) {
                    String fixedKey = key.getKey().toLowerCase().replaceAll(" ", "").trim();
                    if (isValidAttributes(fixedKey)) {
                        double a = playerAttributes.get(player.getName() + ".attribute."+attributesKey+"." + fixedKey);
                        Double keyValue = container.get(key, PersistentDataType.DOUBLE);
                        assert keyValue != null;
                        a += keyValue;
                        playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + fixedKey, a);
                    }
                }
            }else {
                for (String attr : attributeList) {
                    playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + FixedAttributes(attr), 0.0);
                }
            }
        } else {
            for (String attr : attributeList) {
                playerAttributes.put(player.getName() + ".attribute.boots." + FixedAttributes(attr), 0.0);
            }
        }

        CalculateAttributes(player);
    }

    public void ViewAttributes(Player player, String part, String attr){
        player.sendMessage(attr+": "+playerAttributes.get(player.getName()+".attribute."+part+"."+FixedAttributes(attr)));
    }

    public String FixedAttributes(String attr){
        return attr.toLowerCase().replaceAll(" ", "").trim();
    }

    public void CalculateAttributes(Player player) {
        //CalculateEnchantments(player);
        for (String attr : attributeList) {
            Double a = playerAttributes.get(player.getName() + ".attribute.hand." + FixedAttributes(attr));
            Double b = playerAttributes.get(player.getName() + ".attribute.helmet." + FixedAttributes(attr));
            Double c = playerAttributes.get(player.getName() + ".attribute.chestplate." + FixedAttributes(attr));
            Double d = playerAttributes.get(player.getName() + ".attribute.leggings." + FixedAttributes(attr));
            Double e = playerAttributes.get(player.getName() + ".attribute.boots." + FixedAttributes(attr));
            /*Integer f = Double.parseDouble(playerAttributes.get(player.getName() + ".attribute.accessories.1." + attr));
            Integer g = Double.parseDouble(playerAttributes.get(player.getName() + ".attribute.accessories.2." + attr));
            Integer h = Double.parseDouble(playerAttributes.get(player.getName() + ".attribute.accessories.3." + attr));
            Integer i = Double.parseDouble(playerAttributes.get(player.getName() + ".attribute.accessories.4." + attr));*/
            double total = a + b + c + d + e;
            playerAttributes.put(player.getName() + ".attribute.total." + FixedAttributes(attr), total);
        }

        player.setMaxHealth(20+playerAttributes.get(player.getName()+".attribute.total.health"));
        playerArmor.put(player, playerAttributes.get(player.getName()+".attribute.total.armor"));
        if (player.getMaxHealth() > 20){
            player.setHealthScale(player.getMaxHealth()*0.85);
        }
        else if (player.getMaxHealth() <= 20){
            player.setHealthScale(player.getMaxHealth());
        }
        playerMaxSteam.put(player, defaultMaxSteam+playerAttributes.get(player.getName()+".attribute.total.steam"));

        double totalMovementSpeed = playerAttributes.get(player.getName() + ".attribute.total.movementspeed");
        AttributeInstance movementSpeed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.setBaseValue(0.1 + (totalMovementSpeed / 10000));
        }

        //double totalAttackSpeed = playerAttributes.get(player.getName() + ".attribute.total.movementspeed");
        AttributeInstance attackSpeed = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.setBaseValue(100);
        }

        AttributeInstance knockbackResistance = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (knockbackResistance != null) {
            knockbackResistance.setBaseValue(1000);
        }

        AttributeInstance attackKnockback = player.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK);
        if (attackKnockback != null) {
            attackKnockback.setBaseValue(1000);
        }

        AttributeInstance attackDamage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(-100);
        }
    }

    void ResetPlayerMiningSpeed(Player player){
        AttributeInstance miningSpeed = player.getAttribute(Attribute.PLAYER_MINING_EFFICIENCY);
        if (miningSpeed != null) {
            if (miningSpeed.getValue() != 0){
                miningSpeed.setBaseValue(0);
                PlaySound(Sound.BLOCK_NOTE_BLOCK_CHIME, player, 1, 2);
            }
        }
    }



    public Boolean isValidAttributes(String value) {
        for (String attr : attributeList) {
            if (FixedAttributes(value).equals(FixedAttributes(attr))) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void OnItemDamage(PlayerItemDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void OnPlayerItemHeld(PlayerItemHeldEvent event) {
        InventoryItemCheck(event.getPlayer());
    }

    /*@EventHandler
    public void OnDamageEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player){
            if (event.getEntity() instanceof Mob || event.getEntity() instanceof Player){
                if (event.getEntity() != event.getDamager()){
                    Player player = (Player) event.getDamager();
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (item.getType() != Material.AIR){
                        if (player.hasCooldown(item.getType())){
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }*/


    @EventHandler
    public void OnArmSwing(PlayerArmSwingEvent event){
        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR){

            ItemMeta meta = item.getItemMeta();

            if (!isFactoryItem(item)){
                return;
            }

            if (!isWeapon(item)){
                return;
            }

            PersistentDataContainer container = meta.getPersistentDataContainer();
            Double steamConsumptionContainer = container.get(GetNamespacedKey(steamConsumptionKey), PersistentDataType.DOUBLE);
            assert steamConsumptionContainer != null;
            double steamConsumption = steamConsumptionContainer;

            if (GetSteam(player) < steamConsumption){
                player.sendMessage(Notification_NoSteam(player));
                return;
            }

            RemoveSteam(player, steamConsumption);

            Double attackRangeContainer = container.get(GetNamespacedKey(attackRangeKey), PersistentDataType.DOUBLE);
            assert attackRangeContainer != null;
            double attackRange = attackRangeContainer;

            Entity entity = player.getTargetEntity((int)attackRange);

            if (isSword(item) || isHammer(item)){
                PlayerBasicAttack(player, entity, "melee");
            }else{
                PlayerBasicAttack(player, entity, "range");
            }

        }
    }

    public void PlayerBasicAttack(Player player, Entity target, String type){
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR){
            ItemMeta meta = item.getItemMeta();

            if (meta == null){return;}

            if (!ItemNotBroken(item)){
                Notification_ItemBroken(player);
                return;
            }

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(GetNamespacedKey("item"))){return;}
            if (!container.get(GetNamespacedKey(typeKey), PersistentDataType.STRING).equals("weapon")){return;}

            double totalDamage = CalculateDamage(player, target, type);
            double attackSpeed = 3.0;
            Double attackSpeedContainer = container.get(GetNamespacedKey(attackSpeedKey), PersistentDataType.DOUBLE);
            assert attackSpeedContainer != null;
            attackSpeed = attackSpeedContainer;

            Double attackRangeContainer = container.get(GetNamespacedKey(attackRangeKey), PersistentDataType.DOUBLE);
            assert attackRangeContainer != null;
            double attackRange = attackRangeContainer;

            if (player.hasCooldown(item.getType())){
                return;
            }
            player.setCooldown(item.getType(), (int)(attackSpeed*20));

            if (isBow(item) || isGun(item)){
                if (isBow(item)){
                    Arrow arrow = player.launchProjectile(Arrow.class);
                    arrow.setVelocity(player.getLocation().getDirection().multiply(3));
                    arrow.setShooter(player);
                }else{
                    Snowball ammo = player.launchProjectile(Snowball.class);
                    ammo.setVelocity(player.getLocation().getDirection().multiply(3));
                    ammo.setShooter(player);
                }
            }

            if (!isSword(item) && !isHammer(item) && !isBlast(item)){
                return;
            }

            ManageDamage(player, target, totalDamage);
            if ((target instanceof LivingEntity)){
                ManageDurability(player, "hand");
                UpdateItem(player, "hand");
            }

            String attackEffect = container.get(GetNamespacedKey(attackEffectKey), PersistentDataType.STRING);
            assert attackEffect != null;
            SpawnAttackEffect(player, AttackEffect.parseEffect(attackEffect), attackRange);

        }
    }

    public void ManageDamage(Player player, Entity target, double totalDamage){
        if (target instanceof Mob) {
            if (isCrit(player)){
                ((Mob) target).damage(totalDamage*2, player);
                player.sendMessage(sendText("(mob) CRIT You damage enemy with: "+FormatDouble(totalDamage*2)));
                return;
            }
            ((Mob) target).damage(totalDamage, player);
            player.sendMessage(sendText("(mob) You damage enemy with: "+FormatDouble(totalDamage)));
        }
        else if (target instanceof Player){
            if (target != player){
                if (isCrit(player)){
                    ((Player) target).damage(totalDamage*2, player);
                    player.sendMessage(sendText("(player) CRIT You damage enemy with: "+FormatDouble(totalDamage*2)));
                    return;
                }
                ((Player) target).damage(totalDamage, player);
                player.sendMessage(sendText("(player) You damage enemy with: "+FormatDouble(totalDamage)));
            }
        }
    }

    @EventHandler
    public void OnProjectileHit(ProjectileHitEvent event){
        Entity entity = event.getEntity();
        if (entity instanceof Arrow || entity instanceof Snowball) {
            Player player = (Player) event.getEntity().getShooter();
            Entity target = event.getHitEntity();
            if (player != null && target instanceof LivingEntity){
                double totalDamage = CalculateDamage(player, target, "range");
                ManageDamage(player, target, totalDamage);
                ManageDurability(player, "hand");
                UpdateItem(player, "hand");
            }
            entity.remove();
        }
    }

    public boolean isCrit(Player player){
        boolean crit = false;
        double criticalChance = playerAttributes.get(player.getName()+".attribute.total.criticalchance");
        Random random = new Random();
        int criticalGet = random.nextInt(100)+1;
        if (criticalGet <= criticalChance){
            crit = true;
        }
        return crit;
    }

    public static boolean hasSteam(Player player){
        double current = playerSteam.get(player);
        if (playerSteam.get(player) < 0){
            playerSteam.put(player, 0.0);
        }
        return current > 0;
    }

    public static void RemoveSteam(Player player, double amount){
        double current = playerSteam.get(player);
        current -= amount;
        playerSteam.put(player, current);
    }

    public static void AddSteam(Player player, double amount){
        double current = playerSteam.get(player);
        current += amount;
        playerSteam.put(player, current);
    }

    public static void SetSteam(Player player, double amount){
        playerSteam.put(player, amount);
    }

    public static double GetSteam(Player player){
        return playerSteam.get(player);
    }

    public void ManageDurability(Player player, String target){
        String durabilityKey = "durability";
        ItemStack item = null;

        if (target.equals("hand")){
            item = player.getInventory().getItemInMainHand();
        }
        else if (target.equals("helmet")){
            item = player.getInventory().getHelmet();
        }
        else if (target.equals("chestplate")){
            item = player.getInventory().getChestplate();
        }
        else if (target.equals("leggings")) {
            item = player.getInventory().getLeggings();
        }
        else if (target.equals("boots")){
            item = player.getInventory().getBoots();
        }

        if (item != null && item.getType() != Material.AIR){
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();

            if (container.has(GetNamespacedKey("item"))){
                Double durability = container.get(GetNamespacedKey(durabilityKey), PersistentDataType.DOUBLE);
                if (durability != null){
                    if (durability > 0){
                        durability--;
                        container.set(GetNamespacedKey(durabilityKey), PersistentDataType.DOUBLE, durability);
                        item.setItemMeta(meta);
                    }
                }
            }
        }
    }

    public static void UpdateItem(Player player, String target){
        ItemStack item = null;

        if (target.equals("hand")){
            item = player.getInventory().getItemInMainHand();
        }
        else if (target.equals("helmet")){
            item = player.getInventory().getHelmet();
        }
        else if (target.equals("chestplate")){
            item = player.getInventory().getChestplate();
        }
        else if (target.equals("leggings")) {
            item = player.getInventory().getLeggings();
        }
        else if (target.equals("boots")){
            item = player.getInventory().getBoots();
        }
        Integer targetSlot = null;
        try{
            targetSlot = Integer.parseInt(target);
        }catch (Exception e){

        }
        if (targetSlot != null){
            item = player.getOpenInventory().getBottomInventory().getItem(targetSlot);
        }

        if (item != null && item.getType() != Material.AIR){
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();

            if (container.has(GetNamespacedKey(itemKey))){
                FactoryItem.Type item_type = FactoryItem.Type.parseType(container.get(GetNamespacedKey(typeKey), PersistentDataType.STRING));
                FactoryItem.SubType item_subType = FactoryItem.SubType.parseSubType(container.get(GetNamespacedKey(subTypeKey), PersistentDataType.STRING));
                Double attackDamage = container.get(GetNamespacedKey(baseKey+attackDamageKey), PersistentDataType.DOUBLE);
                Double attackRange = container.get(GetNamespacedKey(attackRangeKey), PersistentDataType.DOUBLE);
                Double attackSpeed = container.get(GetNamespacedKey(attackSpeedKey), PersistentDataType.DOUBLE);
                Double criticalChance = container.get(GetNamespacedKey(baseKey+criticalChanceKey), PersistentDataType.DOUBLE);
                Double steamConsumption = container.get(GetNamespacedKey(steamConsumptionKey), PersistentDataType.DOUBLE);
                Double durability = container.get(GetNamespacedKey(durabilityKey), PersistentDataType.DOUBLE);
                Double maxDurability = container.get(GetNamespacedKey(maxDurabilityKey), PersistentDataType.DOUBLE);

                Double health = container.get(GetNamespacedKey(baseKey+healthKey), PersistentDataType.DOUBLE);
                Double steam = container.get(GetNamespacedKey(baseKey+steamKey), PersistentDataType.DOUBLE);
                Double armor = container.get(GetNamespacedKey(baseKey+armorKey), PersistentDataType.DOUBLE);
                Double mutantDamage = container.get(GetNamespacedKey(baseKey+mutantDamageKey), PersistentDataType.DOUBLE);
                Double mutantDefense = container.get(GetNamespacedKey(baseKey+mutantDefenseKey), PersistentDataType.DOUBLE);
                Double undeadDamage = container.get(GetNamespacedKey(baseKey+undeadDamageKey), PersistentDataType.DOUBLE);
                Double undeadDefense = container.get(GetNamespacedKey(baseKey+undeadDefenseKey), PersistentDataType.DOUBLE);
                Double meleeDamage = container.get(GetNamespacedKey(baseKey+meleeDamageKey), PersistentDataType.DOUBLE);
                Double rangeDamage = container.get(GetNamespacedKey(baseKey+rangeDamageKey), PersistentDataType.DOUBLE);

                String attackEffect = container.get(GetNamespacedKey(attackEffectKey), PersistentDataType.STRING);

                Double toolPower = container.get(GetNamespacedKey(toolPowerKey), PersistentDataType.DOUBLE);
                Double toolSpeed = container.get(GetNamespacedKey(toolSpeedKey), PersistentDataType.DOUBLE);

                List<String> bonusStats = new ArrayList<>();
                String storedStats = container.get(GetNamespacedKey(bonusStatsKey), PersistentDataType.STRING);
                if (storedStats != null){
                    bonusStats = Arrays.asList(storedStats.split(","));
                }

                Rarity.RarityType rarity = Rarity.RarityType.parseRarity(container.get(GetNamespacedKey(rarityKey), PersistentDataType.STRING));
                String displayname = sendText(Rarity.getColor(rarity)+ uncolouredText(meta.getDisplayName()));
                Material material = item.getType();

                FactoryItem updatedItem = new FactoryItem();
                updatedItem.setType(item_type);
                updatedItem.setSubType(item_subType);
                updatedItem.setAttackDamage(attackDamage);
                updatedItem.setAttackRange(attackRange);
                updatedItem.setAttackSpeed(attackSpeed);
                updatedItem.setCriticalChance(criticalChance);
                updatedItem.setSteamConsumption(steamConsumption);

                updatedItem.setHealth(health);
                updatedItem.setArmor(armor);
                updatedItem.setMutantDamage(mutantDamage);
                updatedItem.setMutantDefense(mutantDefense);
                updatedItem.setUndeadDamage(undeadDamage);
                updatedItem.setUndeadDefense(undeadDefense);
                updatedItem.setSteam(steam);
                updatedItem.setMeleeDamage(meleeDamage);
                updatedItem.setRangeDamage(rangeDamage);

                updatedItem.setDurability(durability);
                updatedItem.setMaxDurability(maxDurability);
                updatedItem.setRarity(rarity);
                updatedItem.setBonusStats(bonusStats);
                updatedItem.setDisplayname(sendText(displayname));
                updatedItem.setMaterial(material);

                updatedItem.setAttackEffect(AttackEffect.parseEffect(attackEffect));

                updatedItem.setToolPower(toolPower);
                updatedItem.setToolSpeed(toolSpeed);

                item.setItemMeta(updatedItem.build().getItemMeta());
            }
        }
    }

    public double CalculateDamage(Player player, Entity entity, String type){

        double meleeDamage = playerAttributes.get(player.getName()+".attribute.total.meleedamage");
        double rangeDamage = playerAttributes.get(player.getName()+".attribute.total.rangedamage");
        double attackDamage = playerAttributes.get(player.getName()+".attribute.total.attackdamage");
        double criticalChance = playerAttributes.get(player.getName()+".attribute.total.criticalchance");

        double totalDamage = 0;

        if (type.equals("melee")){
            totalDamage = attackDamage+meleeDamage;
        }

        else if (type.equals("range")){
            totalDamage = attackDamage+rangeDamage;
        }

        return totalDamage;
    }

    @EventHandler
    public void OnPlayerKickedFromIsland(IslandKickEvent event) {
        SuperiorPlayer kickedPlayer = event.getTarget();
        Island island = event.getIsland();

        OfflinePlayer player = Bukkit.getOfflinePlayer(kickedPlayer.getUniqueId());
        List<Location> keysToRemove = new ArrayList<>();

        for (String key : placedMachines.keySet()) {
            if (key.endsWith(__ownerKey)) {
                String baseKey = key.replace(__ownerKey, "");
                Location location = parseLocationString(baseKey);
                assert location != null;
                keysToRemove.add(location);
            }
        }

        // Remove after iteration
        for (Location loc : keysToRemove) {
            //assert player != null;
            RemoveMachine(player.getUniqueId(), loc.getBlock(), loc);
        }
    }


    // station

    public boolean isDisabledBlock(Block block){
        if (block != null){
            return block.getType() == Material.ENCHANTING_TABLE || block.getType() == Material.ANVIL
                    || block.getType() == Material.CHIPPED_ANVIL|| block.getType() == Material.DAMAGED_ANVIL
                    || block.getType() == Material.BREWING_STAND;
        }
        return false;
    }

    @EventHandler
    public void OnOpenStation(PlayerInteractEvent event){
        Player player = event.getPlayer();

        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK){
            Block block = event.getClickedBlock();
            if (block != null){
                if (isDisabledBlock(block)){
                    event.setCancelled(true);

                    if (block.getType() == Material.ANVIL){
                        OpenMenu(player, GUIManager.MenuList.Anvil);
                    }
                }
            }
        }
    }
}
