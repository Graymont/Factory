package org.factory.factory;

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
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.factory.factory.Utils.*;

import javax.naming.Name;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.*;
import static org.factory.factory.Database.GetItem;
import static org.factory.factory.Utils.FactoryItem.*;

import static org.factory.factory.Utils.FactoryMachine.*;
import static org.factory.factory.Utils.GUIManager.OpenMachineUpgrades;
import static org.factory.factory.Utils.GUIManager.OpenMenu;
import static org.factory.factory.Utils.PersistentDataManager.*;
import static org.factory.factory.Utils.SQLiteDatabase.parseLocationString;
import static org.factory.factory.Utils.UserInterface.*;

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

    public HashMap<Player, Double> playerSteam = new HashMap<>();
    public HashMap<Player, Double> playerMaxSteam = new HashMap<>();

    public HashMap<String, String> placedMachines = new HashMap<>();
    public HashMap<Location, ItemStack> machineItems = new HashMap<>();

    public HashMap<Player, Integer> machineCount = new HashMap<>();
    public int defaultMaxMachine = 15;

    double defaultSteam = 1000;
    double defaultMaxSteam = 1000;

    public void InitAttributes(Player player){
        for (String attr : attributeList){
            playerAttributes.putIfAbsent(player.getName()+".attribute."+attr, 0.0);
        }
        for (String cd : cooldownList){
            playerCooldown.putIfAbsent(player.getName()+".cooldown."+cd, 0);
        }

        playerSteam.put(player, defaultSteam);
        playerMaxSteam.put(player, defaultMaxSteam);



        consoleLog(sendText("&aAttributes of &2"+player.getName()+" &ahas been Initialized Successfully!"));
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
                             String machineName
    ){
        Location location = block.getLocation();
        placedMachines.put(location+__speedKey, ""+speed);

        if (placedMachines.get(location+__taskIdKey) != null){
            int taskIdToRemove = Integer.parseInt(placedMachines.get(location+__taskIdKey));
            Bukkit.getScheduler().cancelTask(taskIdToRemove);
            //PlayerDebug(player, "Removed taskId: "+taskIdToRemove);
        }


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

                if (steam < steamConsumption){
                    return;
                }

                if (_durability < 5){
                    if (placedMachines.get(location+__taskIdKey) != null){
                        if (!placedMachines.get(location+__statusKey).equals("Broken")){
                            placedMachines.put(location+__statusKey, "Broken");
                            UpdateMachineTag(player, location, machineName, Integer.parseInt(placedMachines.get(location+__taskIdKey)));
                        }
                    }
                    return;
                }else{
                    if (!placedMachines.get(location+__statusKey).equals("Active")){
                        placedMachines.put(location+__statusKey, "Broken");
                        UpdateMachineTag(player, location, machineName, Integer.parseInt(placedMachines.get(location+__taskIdKey)));
                    }
                }

                steam -= steamConsumption;
                if (steam < 0 ){
                    steam = 0;
                }
                playerSteam.put(player, steam);

                _durability -= 1;
                placedMachines.put(location+__durabilityKey, ""+_durability);

                if (dropChance <= 5 && potentialDrop >= 5){
                    ItemStack drop2 = new ItemStack(drop.clone());
                    ItemMeta meta = drop2.getItemMeta().clone();
                    meta.setDisplayName(sendText("&bMajestic "+uncolouredText(meta.getDisplayName())));
                    drop2.setItemMeta(meta);
                    DropItem(dropLocation, drop2.clone(), productionRate);
                }
                else if (dropChance <= 15 && potentialDrop >= 4){
                    ItemStack drop2 = new ItemStack(drop.clone());
                    ItemMeta meta = drop2.getItemMeta().clone();
                    meta.setDisplayName(sendText("&4Special "+uncolouredText(meta.getDisplayName())));
                    drop2.setItemMeta(meta);
                    DropItem(dropLocation, drop2.clone(), productionRate);
                }

                else if (dropChance <= 30 && potentialDrop >= 3){
                    ItemStack drop2 = new ItemStack(drop.clone());
                    ItemMeta meta = drop2.getItemMeta().clone();
                    meta.setDisplayName(sendText("&6Good "+uncolouredText(meta.getDisplayName())));
                    drop2.setItemMeta(meta);
                    DropItem(dropLocation, drop2.clone(), productionRate);
                }

                else if (dropChance <= 50 && potentialDrop >= 2){
                    ItemStack drop2 = new ItemStack(drop.clone());
                    ItemMeta meta = drop2.getItemMeta().clone();
                    meta.setDisplayName(sendText("&2Fine "+uncolouredText(meta.getDisplayName())));
                    drop2.setItemMeta(meta);
                    DropItem(dropLocation, drop2.clone(), productionRate);
                }

                else{
                    DropItem(dropLocation, drop.clone(), productionRate);
                }
                PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, block.getLocation(), 0.3f, 1);
            }
        }.runTaskTimer(plugin, 0L, speed*20).getTaskId();

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
        placedMachines.put(location+__statusKey, "Active");
        machineItems.put(location, machine.clone());

        UpdateMachineTag(player, location, machineName, taskId);
    }

    @EventHandler
    public void OnJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        machineCount.putIfAbsent(player, 0);

        InitAttributes(player);
        StartAllMachines(player);
    }

    @EventHandler
    public void OnQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        StopAllMachines(player);

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
                                durability, maxDurability, machineLevel, Rarity.RarityType.parseRarity(rarity), machineName);

                    } catch (NumberFormatException e) {
                        consoleLog(sendText("&cError: Invalid number format in machine data for " + location));
                    }
                }
            }
        }

        machineCount.put(player, mCount);
        consoleLog(sendText("&aAll Machines owned by &2" + player.getName() + " &ahas been started with &6" + machineCount.get(player) + " Machines!"));
    }


    public void StopAllMachines(Player player){
        for (String key : placedMachines.keySet()) {
            if (key.endsWith(__locationKey)) {
                String location = key.replace(__locationKey, "");
                int taskId = Integer.parseInt(placedMachines.get(location+__taskIdKey));
                placedMachines.put(location+__statusKey, "Inactive");
                Bukkit.getScheduler().cancelTask(taskId);
                String machineName = placedMachines.get(location+__machineNameKey);
                Location parsedLocation = parseLocationString(location);
                assert parsedLocation != null;
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
            int mc = machineCount.get(player);
            if (mc >= defaultMaxMachine){
                event.setCancelled(true);
                player.sendMessage(sendText("&4You already reach the maximum machine placement!"));
                PlaySoundAt(Sound.BLOCK_MUD_BRICKS_PLACE, location, 1, 3);
                return;
            }

            mc++;
            machineCount.put(player, mc);

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

            StartMachine(player, speed, block, dropName, potentialDrop, item.clone(), productionRate,
                    steamConsumption, durability, maxDurability, machineLevel, Rarity.RarityType.parseRarity(rarity), machineName);

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
                            OpenMachineUpgrades(player, location);
                            event.setCancelled(true);
                            return;
                        }

                        playerCooldown.put(player.getName()+".cooldown.Place Machine", 2);
                        RemoveMachine(player, block, location);
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

    public void RemoveMachine(Player player, Block block, Location location){
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

        obtainedMachine = new ItemStack(CreateMachine(name, machineLevel, speed, productionRate
                , steamConsumption, durability, maxDurability, material, dropName, potentialDrop, Rarity.RarityType.parseRarity(rarity)));

        Map<Integer, ItemStack> mapItem = player.getInventory().addItem(obtainedMachine.clone());

        if (!mapItem.isEmpty()){
            player.sendMessage(sendText("&4Your inventory is full!"));
            return;
        }

        Bukkit.getScheduler().cancelTask(taskId);
        //long speed = Long.parseLong(placedMachines.get(location+".speed"));

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
        machineItems.remove(location);

        block.setType(Material.AIR);

        int mc = machineCount.get(player);
        mc--;
        if (mc<0) mc = 0;
        machineCount.put(player, mc);

        /*PlayerDebug(player, "Machine Owner: "+player.getName());
        PlayerDebug(player, "Machine TaskId: "+taskIdString);
        PlayerDebug(player, "Machine Speed: "+speed);*/

        player.sendMessage(" ");
        player.sendMessage(sendText("&bRemoved Machine "+obtainedMachine.getItemMeta().getDisplayName()+" &6["+mc+"&e/&6"+defaultMaxMachine+"]"));

        RemoveMachineTags(location, taskId);
        PlaySoundAt(Sound.BLOCK_CHEST_CLOSE, location, 1, 3);
    }

    public void RemoveMachineTags(Location location, int taskId){
        World world = location.getWorld();
        for (Entity entity : Bukkit.getWorld(world.getName()).getEntities()){
            if (entity instanceof ArmorStand){
                if (entity.hasMetadata("MachineTag."+location)){
                    entity.remove();
                }
            }
        }
    }
    public void SpawnMachineTag(String player, Location location, String machineName, int taskId){
        Vector tagOffset = new Vector(0.5, 2.0, 0.5);
        Location spawnLocation = location.clone().add(tagOffset);
        Location spawnLocation2 = spawnLocation.clone().add(0, -0.3, 0);
        Location spawnLocation3 = spawnLocation2.clone().add(0, -0.3, 0);
        Location spawnLocation4 = spawnLocation3.clone().add(0, -0.3, 0);
        Location spawnLocation5 = spawnLocation4.clone().add(0, -0.3, 0);

        String machineStatus = placedMachines.get(location+".status");
        String statusColor = "&a✔ ";
        spawnMachineTag(spawnLocation, sendText(machineName), new Vector(0, 0, 0), location);
        if (machineStatus.equals("Broken")){
            statusColor = "&c✗ ";
        }
        else if (machineStatus.equals("Inactive")){
            statusColor = "&4\uD83D\uDC80 ";
        }

        spawnMachineTag(spawnLocation2, "&8[ "+statusColor+machineStatus+" &8]", new Vector(0, 0, 0), location);
        spawnMachineTag(spawnLocation3, "&8[ &fOwner: &e"+player+" &8]", new Vector(0, 0, 0), location);
        spawnMachineTag(spawnLocation4, "&8[ &9Left-Click &fto &3Take &8]", new Vector(0, 0, 0), location);
        spawnMachineTag(spawnLocation5, "&8[ &9Sneak &f+ &9Left-Click &fto &3Open &8]", new Vector(0, 0, 0), location);
    }

    public void UpdateMachineTag(Player player, Location location, String machineName, int taskId){
        World world = location.getWorld();
        for (Entity entity : Bukkit.getWorld(world.getName()).getEntities()){
            if (entity instanceof ArmorStand){
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


    public ItemStack UpdateMachineItem(ItemStack item){

        ItemMeta meta = item.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        String name = uncolouredText(meta.getDisplayName());
        Integer machineLevel = container.get(GetNamespacedKey(machineLevelKey), PersistentDataType.INTEGER);
        Long speed = container.get(GetNamespacedKey(speedKey), PersistentDataType.LONG);
        Integer productionRate = container.get(GetNamespacedKey(productionRateKey), PersistentDataType.INTEGER);
        Integer steamConsumption = container.get(GetNamespacedKey(steamConsumptionKey), PersistentDataType.INTEGER);
        Integer durability = container.get(GetNamespacedKey(durabilityKey), PersistentDataType.INTEGER);
        Integer maxDurability = container.get(GetNamespacedKey(maxDurabilityKey), PersistentDataType.INTEGER);
        Material material = item.getType();
        String dropName = container.get(GetNamespacedKey(dropNameKey), PersistentDataType.STRING);
        Integer potentialDrop = container.get(GetNamespacedKey(potentialDropKey), PersistentDataType.INTEGER);
        String rarity = container.get(GetNamespacedKey(rarityKey), PersistentDataType.STRING);

        item = new ItemStack(CreateMachine(name, machineLevel, speed, productionRate
        , steamConsumption, durability, maxDurability, material, dropName, potentialDrop, Rarity.RarityType.parseRarity(rarity)));

        return item;
    }

    public void spawnMachineTag(Location location, String name, Vector velocity, Location machineLocation) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName(sendText(name));
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            armorStand.setInvulnerable(true);
            armorStand.setVelocity(velocity);
            armorStand.setMetadata("MachineTag."+machineLocation, new FixedMetadataValue(plugin, true));
        }, 5L);
    }

    public void PlayerInventoryItems(Player player){
        PlayerItemAttributes(player);
        PlayerMiningSpeed(player);
    }

    public void PlayerItemAttributes(Player player){
        for (ItemStack item : player.getInventory()) {
            if (item != null && item.getType() != Material.AIR && isTool(item)) {
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
            }
        }
    }

    public void PlayerMiningSpeed(Player player){
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.AIR){
            if (item.getItemMeta().hasLore()){
                List<String> itemLore = item.getItemMeta().getLore();
                ItemMeta itemMeta = item.getItemMeta();

                for (String lore : itemLore){
                    if (lore != null && uncolouredText(lore).contains("Pickaxe Power")
                            || uncolouredText(Objects.requireNonNull(lore)).contains("Axe Power") ||
                            uncolouredText(lore).contains("Shovel Power")){

                        int defaultPower = 0;
                        int power = Integer.parseInt(numberInText(lore));
                        double enchantValue = 1;

                        for (String listEnchant : itemLore){
                            if (listEnchant != null && uncolouredText(listEnchant).contains("Efficiency")){
                                int enchantLevel = Integer.parseInt(numberInText(lore));
                                enchantValue = 1.0+((double) enchantLevel / 10);
                            }
                        }

                        if (item.getType() == Material.GOLDEN_PICKAXE){
                            defaultPower = 25;
                        }

                        int powerValue = (int) ((defaultPower+power)*enchantValue);

                        if (isPickaxe(item) || isAxe(item) || isShovel(item)){
                            AttributeInstance miningSpeed = player.getAttribute(Attribute.PLAYER_MINING_EFFICIENCY);
                            if (miningSpeed != null) {
                                if (miningSpeed.getValue() != powerValue){
                                    miningSpeed.setBaseValue(powerValue);
                                    PlaySound(Sound.BLOCK_NOTE_BLOCK_CHIME, player, 1, 2);
                                }else{
                                    return;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public List<String> attributeList = Arrays.asList(
      "Steam", "Movement Speed", "Steam Regen", "Attack Damage", "Attack Range", "Attack Speed", "Critical Chance", "Critical Damage"
            , "Accuracy", "Defense", "Undead Damage", "Undead Defense", "Mutant Damage", "Mutant Defense",
            "Melee Damage", "Range Damage", "Steam Consumption"
    );

    public List<String> cooldownList = Arrays.asList(
            "Place Machine"
    );

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

            if (isWeapon(item)){
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

            if (isWeapon(item)){
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

            if (isWeapon(item)){
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

            if (isWeapon(item)){
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

        playerMaxSteam.put(player, 1000+playerAttributes.get(player.getName()+".attribute.total.steam"));

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



    public Boolean ItemNotBroken(ItemStack item){
        if (item.getType() != Material.AIR){
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(GetNamespacedKey("item"))){
                double durability = 0;
                Double durabilityValue = container.get(GetNamespacedKey("durability"), PersistentDataType.DOUBLE);
                if (durabilityValue != null){
                    durability = durabilityValue;
                }
                return durability > 0;
            }
        }
        return false;
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
    public void OnArmSwing(PlayerArmSwingEvent event){
        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR){

            ItemMeta meta = item.getItemMeta();
            if (!isWeapon(item)){
                return;
            }

            PersistentDataContainer container = meta.getPersistentDataContainer();
            Double attackRangeContainer = container.get(GetNamespacedKey(attackRangeKey), PersistentDataType.DOUBLE);
            assert attackRangeContainer != null;
            double attackRange = attackRangeContainer;
            Entity entity = player.getTargetEntity((int)attackRange);
            if (entity instanceof LivingEntity){
                PlayerMeleeAttack(player, entity);
            }
        }
    }

    public void PlayerMeleeAttack(Player player, Entity target){
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR){
            ItemMeta meta = item.getItemMeta();

            if (meta == null){
                return;
            }

            if (!ItemNotBroken(item)){return;}

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(GetNamespacedKey("item"))){return;}

            double totalDamage = CalculateDamage(player, target, "melee");

            if (target instanceof Mob) {
                ((Mob) target).damage(totalDamage, player);
                player.sendMessage(sendText("(mob) You damage enemy with: "+FormatDouble(totalDamage)));
            }
            else if (target instanceof Player){
                if (target != player){
                    ((Player) target).damage(totalDamage, player);
                    player.sendMessage(sendText("(player) You damage enemy with: "+FormatDouble(totalDamage)));
                }
            }

            ManageDurability(player, "hand");
            UpdateItem(player, "hand");
        }
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

    public void UpdateItem(Player player, String target){
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

            if (container.has(GetNamespacedKey(itemKey))){
                FactoryItem.Type item_type = FactoryItem.Type.parseType(container.get(GetNamespacedKey(typeKey), PersistentDataType.STRING));
                FactoryItem.SubType item_subType = FactoryItem.SubType.parseSubType(container.get(GetNamespacedKey(subTypeKey), PersistentDataType.STRING));
                Double attackDamage = container.get(GetNamespacedKey(attackDamageKey), PersistentDataType.DOUBLE);
                Double attackRange = container.get(GetNamespacedKey(attackRangeKey), PersistentDataType.DOUBLE);
                Double attackSpeed = container.get(GetNamespacedKey(attackSpeedKey), PersistentDataType.DOUBLE);
                Double criticalChance = container.get(GetNamespacedKey(criticalChanceKey), PersistentDataType.DOUBLE);
                Double steamConsumption = container.get(GetNamespacedKey(steamConsumptionKey), PersistentDataType.DOUBLE);
                Double durability = container.get(GetNamespacedKey(durabilityKey), PersistentDataType.DOUBLE);
                Double maxDurability = container.get(GetNamespacedKey(maxDurabilityKey), PersistentDataType.DOUBLE);
                Rarity.RarityType rarity = Rarity.RarityType.parseRarity(container.get(GetNamespacedKey(rarityKey), PersistentDataType.STRING));
                String displayname = sendText(meta.getDisplayName());
                Material material = item.getType();

                FactoryItem updatedItem = new FactoryItem();
                updatedItem.setType(item_type);
                updatedItem.setSubType(item_subType);
                updatedItem.setAttackDamage(attackDamage);
                updatedItem.setAttackRange(attackRange);
                updatedItem.setAttackSpeed(attackSpeed);
                updatedItem.setCriticalChance(criticalChance);
                updatedItem.setSteamConsumption(steamConsumption);
                updatedItem.setDurability(durability);
                updatedItem.setMaxDurability(maxDurability);
                updatedItem.setRarity(rarity);
                updatedItem.setDisplayname(sendText(displayname));
                updatedItem.setMaterial(material);
                item.setItemMeta(updatedItem.build().getItemMeta());
            }
        }
    }

    public double CalculateDamage(Player player, Entity entity, String type){

        double meleeDamage = playerAttributes.get(player.getName()+".attribute.total.meleedamage");
        double rangeDamage = playerAttributes.get(player.getName()+".attribute.total.rangedamage");
        double attackDamage = playerAttributes.get(player.getName()+".attribute.total.attackdamage");

        double totalDamage = 0;

        if (type.equals("melee")){
            totalDamage = attackDamage+meleeDamage;
        }

        else if (type.equals("range")){
            totalDamage = attackDamage+rangeDamage;
        }

        return totalDamage;
    }

}
