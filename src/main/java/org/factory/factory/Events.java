package org.factory.factory;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.IslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import it.unimi.dsi.fastutil.Hash;
import net.bytebuddy.implementation.bind.annotation.Super;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.factory.factory.Utils.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.naming.Name;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.*;
import static org.factory.factory.Database.*;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.Utils.Booster.*;
import static org.factory.factory.Utils.CooldownManager.SetCooldown;
import static org.factory.factory.Utils.CooldownManager.hasCooldown;
import static org.factory.factory.Utils.CraftingManager.isDisabledItem;
import static org.factory.factory.Utils.FactoryEvents.*;
import static org.factory.factory.Utils.FactoryItem.*;

import static org.factory.factory.Utils.FactoryMachine.*;
import static org.factory.factory.Utils.FactoryMob.*;
import static org.factory.factory.Utils.GUIManager.*;
import static org.factory.factory.Utils.MultiBlock.*;
import static org.factory.factory.Utils.PersistentDataManager.*;
import static org.factory.factory.Utils.PlayerProgress.*;
import static org.factory.factory.Utils.PlayerProgressManager.*;
import static org.factory.factory.Utils.SQLiteDatabase.*;
import static org.factory.factory.Utils.UserInterface.*;
import static org.factory.factory.Utils.VaultEconomy.*;

public class Events implements Listener {

    Factory plugin;
    SQLiteDatabase sqLiteDatabase;

    public Events (Factory pl){
        plugin = pl;
    }



    public String checkName(String text){
        return text.replaceAll("§.", "").trim().replaceAll("'s", "").trim();
    }

    public static void DropItem(Location location, ItemStack item, int amount) {
        Bukkit.getScheduler().runTaskAsynchronously(getMainPlugin(), () -> {
            List<ItemStack> itemsToDrop = new ArrayList<>();
            for (int i = 0; i < amount; i++) {
                itemsToDrop.add(new ItemStack(item));
            }

            Bukkit.getScheduler().runTask(getMainPlugin(), () -> {
                for (ItemStack stack : itemsToDrop) {
                    location.getWorld().dropItem(location, stack);
                    //droppedItem.setVelocity(new Vector(0, 0.2, 0));
                }
            });
        });
    }

    public static void DropSingleItem(Location location, ItemStack item) {
        Item droppedItem = location.getWorld().dropItem(location, item);
        Vector upward = new Vector(0, 0.2, 0);
        droppedItem.setVelocity(upward);
    }

    void PlayerDebug(Player player, String message){
        player.sendMessage(sendText("&a"+message));
    }

    void ConsoleDebug(String message){
        consoleLog("&a"+message);
    }

    public static HashMap<String, Double> playerAttributes = new HashMap<>();
    public static HashMap<String, Integer> playerCooldown = new HashMap<>();

    public static HashMap<Player, Double> playerSteam = new HashMap<>();
    public static HashMap<Player, Double> playerMaxSteam = new HashMap<>();
    public static HashMap<Player, Double> playerArmor = new HashMap<>();

    public static HashMap<String, String> placedMachines = new HashMap<>();
    public static HashMap<Location, ItemStack> machineItems = new HashMap<>();
    public static HashMap<UUID, List<ItemStack>> storedMachines = new HashMap<>();

    public static HashMap<UUID, List<Location>> playerPlacedMachines = new HashMap<>();

    public static HashMap<UUID, Integer> machineCount = new HashMap<>();
    public int defaultMaxMachine = 15;

    static double defaultSteam = 20;
    static double defaultMaxSteam = 20;


    public static HashMap<Player, List<String>> chatMessages = new HashMap<>();

    /*public static void ShowChatList (Player player){

        for (int i = 0; i < 250; i++) {
            player.sendMessage(" ");
        }

        List<String> chatList = chatMessages.get(player);

        for (String msg : chatList){
            player.sendMessage(sendText(msg));
        }

    }

    @EventHandler
    public void OnChat(PlayerChatEvent event){
        String message = event.getMessage();

        for (Player player : Bukkit.getOnlinePlayers()){
            chatMessages.putIfAbsent(player, new ArrayList<>());

            chatMessages.get(player).add(sendText(message));
        }
    }*/


    public void InitAttributes(Player player){
        for (String attr : attributeList){
            playerAttributes.putIfAbsent(player.getName()+".attribute.total."+FixedAttributes(attr), 0.0);
            playerAttributes.putIfAbsent(player.getName()+".attribute.helmet."+FixedAttributes(attr), 0.0);
            playerAttributes.putIfAbsent(player.getName()+".attribute.chestplate."+FixedAttributes(attr), 0.0);
            playerAttributes.putIfAbsent(player.getName()+".attribute.leggings."+FixedAttributes(attr), 0.0);
            playerAttributes.putIfAbsent(player.getName()+".attribute.boots."+FixedAttributes(attr), 0.0);
        }
        for (String cd : cooldownList){
            playerCooldown.putIfAbsent(player.getName()+".cooldown."+cd, 0);
        }

        playerSteam.putIfAbsent(player, defaultSteam);
        playerMaxSteam.putIfAbsent(player, defaultMaxSteam);
        playerArmor.putIfAbsent(player, 0.0);
        storedMachines.putIfAbsent(player.getUniqueId(), new ArrayList<>());
        isBackpack.putIfAbsent(player, false);

        maxMachines.putIfAbsent(player.getUniqueId(), defaultMaxMachine);

        playerLevel.putIfAbsent(player.getUniqueId(), 1);
        playerExp.putIfAbsent(player.getUniqueId(), 0.0);
        playerSellMultiplier.putIfAbsent(player.getUniqueId(), 1.0);
        playerExpMultiplier.putIfAbsent(player.getUniqueId(), 1.0);
        playerPrestige.putIfAbsent(player.getUniqueId(), 0);

        progressNotifications.putIfAbsent(player, new ArrayList<>());
        chatMessages.putIfAbsent(player, new ArrayList<>());

        playerPlacedMachines.putIfAbsent(player.getUniqueId(), new ArrayList<>());

        playerSelector.putIfAbsent(player, 0);

        boosters.putIfAbsent(player.getUniqueId(), Booster.BoosterType.None);
        activeBooster.putIfAbsent(player.getUniqueId(), 0L);

        canRemoveMachine.put(player, true);

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


    public static void StartMachine(Player player,
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
            //getScheduler().cancelTask(taskIdToRemove);
            //PlayerDebug(player, "Removed taskId: "+taskIdToRemove);
        }

        /*Bukkit.getScheduler().runTaskLater(plugin, () -> {}, speed);*/
        /*int interval = (int) Math.max(speed / 20, 1);
        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack drop = new ItemStack(GetItem(dropName));
                Random random = new Random();
                int dropChance = random.nextInt(100)+1;
                Vector offset = new Vector(0.5, 1, 0.5);
                Location dropLocation = block.getRelative(BlockFace.UP).getLocation().add(offset);

                if (!dropLocation.getWorld().isChunkLoaded(dropLocation.getBlockX() >> 4, dropLocation.getBlockZ() >> 4)) {
                    return;
                }

                getWorld(location.getWorld().getName()).getBlockAt(location).setType(machine.getType());

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
                        //placedMachines.put(location+__machineStatusKey, "Active");
                        //UpdateMachineTag(player, location, machineName, Integer.parseInt(placedMachines.get(location+__taskIdKey)));
                        return;
                    }

                    else if (placedMachines.get(location+__machineStatusKey).equals("Inactive")){
                        placedMachines.put(location+__machineStatusKey, "Active");
                        UpdateMachineTag(player, location, machineName, Integer.parseInt(placedMachines.get(location+__taskIdKey)));
                    }

                    else if (placedMachines.get(location+__machineStatusKey).equals("Disabled")){
                        return;
                    }
                }

                *//*steam -= steamConsumption;
                if (steam < 0 ){
                    steam = 0;
                }
                playerSteam.put(player, steam);*//*

                int acidDropChance = random.nextInt(100)+1;


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
                    _durability -= 1;
                    placedMachines.put(location+__durabilityKey, ""+_durability);
                    PlaySoundAt(Sound.BLOCK_LAVA_EXTINGUISH, block.getLocation(), 0.3f, 1);
                    return;
                }


                Bukkit.getScheduler().runTaskAsynchronously(getMainPlugin(), () -> {

                    Rarity.RarityType rarity;
                    String tag;

                    if (dropChance <= 5 && potentialDrop >= 5) {
                        rarity = Rarity.RarityType.Legendary;
                        tag = "Majestic";
                    } else if (dropChance <= 15 && potentialDrop >= 4) {
                        rarity = Rarity.RarityType.Epic;
                        tag = "Special";
                    } else if (dropChance <= 30 && potentialDrop >= 3) {
                        rarity = Rarity.RarityType.Rare;
                        tag = "Good";
                    } else if (dropChance <= 50 && potentialDrop >= 2) {
                        rarity = Rarity.RarityType.Uncommon;
                        tag = "Fine";
                    } else {
                        rarity = Rarity.RarityType.Common;
                        tag = "";
                    }


                    ItemStack dropItem = new ItemStack(createDropItem(drop, rarity, 1.0, machineLevel, tag));
                    ItemStack acidItem;
                    ItemStack carbonItem;

                    if (acidDropChance <= 50 && canProduceAcid(block)) {
                        String acidId = switch (rarity) {
                            case Legendary -> "adaptiveacid";
                            case Epic -> "mutagenicacid";
                            case Rare -> "energeticacid";
                            case Uncommon -> "corrosiveacid";
                            case Common -> "refiningacid";
                            default -> "refiningacid";
                        };

                        acidItem = new ItemStack(GetItem(acidId));
                        carbonItem = new ItemStack(GetItem("carbon" + dropName));
                    } else {
                        carbonItem = null;
                        acidItem = null;
                    }


                    Bukkit.getScheduler().runTask(getMainPlugin(), () -> {
                        DropItem(dropLocation, dropItem, productionRate);

                        if (acidItem != null) {
                            DropItem(dropLocation, acidItem, productionRate);
                            DropItem(dropLocation, carbonItem, productionRate);
                            PlaySoundAt(Sound.ENTITY_SLIME_DEATH, block.getLocation(), 1, 2);
                        }

                        String key = location + __totalProductionKey;
                        int currentProduction = Integer.parseInt(placedMachines.getOrDefault(key, "0"));
                        placedMachines.put(key, String.valueOf(currentProduction + 1));
                    });
                });


                _durability -= 1;
                placedMachines.put(location+__durabilityKey, ""+_durability);
                PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, block.getLocation(), 0.3f, 1);
            }
        }.runTaskTimer(getMainPlugin(), 0L, interval * 20L).getTaskId();*/

        placedMachines.put(location+__locationKey, ""+location);
        placedMachines.put(location+__ownerKey, player.getName());
        placedMachines.put(location+__uuidKey, ""+player.getUniqueId());
        placedMachines.put(location+__taskIdKey, ""+0);
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



        UpdateMachineTag(player.getName(), location, machineName, 0);
    }

    public static int machineBehaviourId;

    public static void MachineBehaviour(){
        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                HashMap<String, String> storedPlacedMachines = new HashMap<>(placedMachines);
                //HashMap<String, String> itterator = new HashMap<>(placedMachines);
                for (Player player : Bukkit.getOnlinePlayers()){
                    for (Location location : playerPlacedMachines.get(player.getUniqueId())){
                        //Location location = parseLocationString(hashKey.replace(__locationKey, ""));
                        //assert location != null;
                        String owner = storedPlacedMachines.get(location+__ownerKey);
                        if (!player.getName().equals(owner)){
                            continue;
                        }

                        Block block = location.getBlock();

                        int speed = Integer.parseInt(storedPlacedMachines.get(location+__speedKey));
                        int machineLevel = Integer.parseInt(storedPlacedMachines.get(location+__machineLevelKey));
                        int productionRate = Integer.parseInt(storedPlacedMachines.get(location+__productionRateKey));
                        int potentialDrop = Integer.parseInt(storedPlacedMachines.get(location+__potentialDropKey));
                        int steamConsumption = Integer.parseInt(storedPlacedMachines.get(location+__steamConsumptionKey));
                        String dropName = storedPlacedMachines.get(location+__dropNameKey);
                        String machineName = storedPlacedMachines.get(location+__machineNameKey);

                        ItemStack machine = machineItems.get(location);
                        ItemMeta machineMeta = machine.getItemMeta();
                        PersistentDataContainer container = machineMeta.getPersistentDataContainer();

                        Integer levelMinimum = container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER);
                        if (levelMinimum == null){
                            levelMinimum = 1;
                        }
                        Integer prestigeMinimum = GetPrestigeRequirement(levelMinimum);


                        ItemStack drop = new ItemStack(GetItem(dropName));
                        Random random = new Random();
                        int dropChance = random.nextInt(100)+1;
                        Vector offset = new Vector(0.5, 1, 0.5);
                        Location dropLocation = block.getRelative(BlockFace.UP).getLocation().add(offset);

                        if (!dropLocation.getWorld().isChunkLoaded(dropLocation.getBlockX() >> 4, dropLocation.getBlockZ() >> 4)) {
                            continue;
                        }

                        getWorld(location.getWorld().getName()).getBlockAt(location).setType(machine.getType());

                        if (playerLevel.get(player.getUniqueId()) < levelMinimum || playerPrestige.get(player.getUniqueId()) < prestigeMinimum){
                            //player.sendMessage(sendText(" &c&lWarnings!"));
                            //player.sendMessage(sendText("  &eYou don't have enough requirements to produce item from &6"+location+" &eMachines!"));
                            continue;
                        }

                        double steam = playerSteam.get(player);

                        int _durability = Integer.parseInt(storedPlacedMachines.get(location+__durabilityKey));
                        int _maxDurability = Integer.parseInt(storedPlacedMachines.get(location+__maxDurabilityKey));
                        String mType = storedPlacedMachines.get(location+__machineTypeKey);

                        if (!mType.equals("steam")){
                            if (!hasSteam(player, playerSteam.get(player))){
                                continue;
                            }

                            if (!hasSteam(player, (double) steamConsumption)){
                                SpawnBlockCrackParticle(block);
                                PlaySoundAt(Sound.BLOCK_NOTE_BLOCK_SNARE, location, 0.3f, 2);
                                continue;
                            }
                            /*if (GetSteam(player) < steamConsumption){
                                continue;
                            }*/
                        }

                        if (_durability <= 0){
                            if (!storedPlacedMachines.get(location+__machineStatusKey).equals("Disabled")){
                                if (!storedPlacedMachines.get(location+__machineStatusKey).equals("Broken")){
                                    placedMachines.put(location+__machineStatusKey, "Broken");
                                    storedPlacedMachines.put(location+__machineStatusKey, "Broken");
                                    UpdateMachineTag(player.getName(), location, machineName, 0);
                                    player.sendMessage(sendText(" "));
                                    player.sendMessage(sendText(" &4⚠ &c&lWarning"));
                                    player.sendMessage(sendText(" &fOne of your machine"));
                                    player.sendMessage(sendText(" &fat &e"+LocationDisplay(location)+" &fis broken"));
                                    player.sendMessage(sendText(" &fyou need to fix it so it can produce"));
                                    player.sendMessage(sendText(" &fthings again!"));
                                    player.sendMessage(sendText(" "));
                                    continue;
                                }
                            }
                            continue;
                        }else{
                            if (storedPlacedMachines.get(location+__machineStatusKey).equals("Broken")){
                                continue;
                            }
                        }

                        int acidDropChance = random.nextInt(100)+1;

                        storedPlacedMachines.putIfAbsent(location+__countdownKey, ""+speed);
                        int countdown = Integer.parseInt(storedPlacedMachines.get(location+__countdownKey));
                        if (countdown <= 0){
                            countdown = Math.toIntExact((long) (speed*events_machineSpeedMultiplier));
                            storedPlacedMachines.put(location+__countdownKey, ""+countdown);
                            if (!mType.equals("steam")){
                                RemoveSteam(player, steamConsumption);
                            }
                        }
                        else{
                            countdown--;
                            storedPlacedMachines.put(location+__countdownKey, ""+countdown);
                            continue;
                        }

                        if (mType.equals("steam")){
                            int steamProduction = Integer.parseInt(storedPlacedMachines.get(location+__steamProductionKey));
                            AddSteam(player, steamProduction);

                            int currentProduction = Integer.parseInt(storedPlacedMachines.get(location+__totalProductionKey));
                            currentProduction++;
                            storedPlacedMachines.put(location+__totalProductionKey, ""+currentProduction);
                            _durability -= 1;
                            storedPlacedMachines.put(location+__durabilityKey, ""+_durability);
                            PlaySoundAt(Sound.BLOCK_LAVA_EXTINGUISH, block.getLocation(), 0.3f, 1);
                            SpawnBlockRedstoneParticle(block, Color.YELLOW);
                            continue;
                        }


                        Bukkit.getScheduler().runTaskAsynchronously(getMainPlugin(), () -> {

                            Rarity.RarityType rarity;
                            String tag;

                            if (dropChance <= 5 && potentialDrop >= 5) {
                                rarity = Rarity.RarityType.Legendary;
                                tag = "Majestic ";
                            } else if (dropChance <= 15 && potentialDrop >= 4) {
                                rarity = Rarity.RarityType.Epic;
                                tag = "Special ";
                            } else if (dropChance <= 30 && potentialDrop >= 3) {
                                rarity = Rarity.RarityType.Rare;
                                tag = "Good ";
                            } else if (dropChance <= 50 && potentialDrop >= 2) {
                                rarity = Rarity.RarityType.Uncommon;
                                tag = "Fine ";
                            } else {
                                rarity = Rarity.RarityType.Common;
                                tag = "";
                            }


                            ItemStack dropItem = new ItemStack(createDropItem(drop, rarity, 1.0, machineLevel, tag));
                            ItemStack acidItem;
                            ItemStack carbonItem;

                            if (acidDropChance <= 50 && canProduceAcid(block)) {
                                String acidId = switch (rarity) {
                                    case Legendary -> "adaptiveacid";
                                    case Epic -> "mutagenicacid";
                                    case Rare -> "energeticacid";
                                    case Uncommon -> "corrosiveacid";
                                    case Common -> "refiningacid";
                                    default -> "refiningacid";
                                };

                                acidItem = new ItemStack(GetItem(acidId));
                                carbonItem = new ItemStack(GetItem("carbon" + dropName));
                            } else {
                                carbonItem = null;
                                acidItem = null;
                            }


                            Bukkit.getScheduler().runTask(getMainPlugin(), () -> {
                                DropItem(dropLocation, dropItem, productionRate);

                                if (acidItem != null) {
                                    DropItem(dropLocation, acidItem, productionRate);
                                    DropItem(dropLocation, carbonItem, productionRate);
                                    PlaySoundAt(Sound.ENTITY_SLIME_DEATH, block.getLocation(), 1, 2);
                                    SpawnBlockRedstoneParticle(block, Color.LIME);
                                    SpawnBlockRedstoneParticle(block, Color.GREEN);
                                }
                                SpawnBlockRedstoneParticle(block, Color.BLACK);

                            });
                        });


                        String key = location + __totalProductionKey;
                        int currentProduction = Integer.parseInt(storedPlacedMachines.get(key));
                        currentProduction++;
                        storedPlacedMachines.put(key, ""+currentProduction);
                        _durability -= 1;
                        storedPlacedMachines.put(location+__durabilityKey, ""+_durability);
                        PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, block.getLocation(), 0.3f, 1);

                    }
                }
                placedMachines = new HashMap<>(storedPlacedMachines);
            }
        }.runTaskTimer(getMainPlugin(), 0L,  20L).getTaskId();

        machineBehaviourId = taskId;

        consoleLog(sendText("&bMachine Behaviour has been executed!"));
    }

    public static void StopMachineBehaviour(){
        consoleLog(sendText("&aStopping Machine Behaviour..."));
        Bukkit.getScheduler().cancelTask(machineBehaviourId);
    }

    public static void StartMachineBehaviour(){
        consoleLog(sendText("&aExecuting Machine Behaviour..."));
        MachineBehaviour();
    }


    public static ItemStack createDropItem(ItemStack baseItem, Rarity.RarityType rarity,
                                           double worthMultiplier, int machineLevel, String dropName) {
        ItemStack item = new ItemStack(baseItem);
        ItemMeta meta = item.getItemMeta();

        String rarityColor = Rarity.getColor(rarity);

        // Uncolored base name
        String baseName = uncolouredText(meta.getDisplayName());
        meta.setDisplayName(sendText(rarityColor + dropName+ baseName));

        PersistentDataContainer container = meta.getPersistentDataContainer();
        double baseWorth = container.getOrDefault(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE, 0.0);
        double finalWorth = (baseWorth + machineLevel) * worthMultiplier;
        container.set(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE, finalWorth);

        List<String> lore = new ArrayList<>();
        lore.add(sendText("&9Machine Product"));
        lore.add(sendText(" "));
        lore.add(sendText(" &7Worth: &f" + FormatDouble(finalWorth) + icon + (worthMultiplier > 1.0 ? " &7(+" + (int)((worthMultiplier - 1) * 100) + "%)" : "")));
        lore.add(sendText(" &8✧ &7Sell this item at &e/sellitem"));
        lore.add(sendText(" &8✧ &e/sellall &7to sell all from your inventory"));
        lore.add(sendText(" &8✧ &7or put in a chest for &f&nSell Wand&7 Multiplier"));
        lore.add(sendText(" "));
        lore.add(sendText(rarityColor + rarity));

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent event){
        Player player = event.getPlayer();
        event.setCancelled(true);

        double lostMinimum = 10000;

        Location respawnLocation = GetLocation("spawn");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            event.getPlayer().teleport(respawnLocation);
        }, 5L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendTitle(sendText("&cYou Died!"), sendText(" "));
        }, 20L);

        double playerMoney = GetPlayerBalance(player);

        if (playerMoney < lostMinimum){
            return;
        }

        double removedMoney = playerMoney*0.02;
        if (playerMoney > 0){
            if (playerMoney >= removedMoney){
                RemovePlayerBalance(player, removedMoney);
            }
        }else{
            return;
        }

        player.sendMessage(sendText("&7You died and lost &c"+FormatDouble(removedMoney)+icon+" (2% of your money) &7becareful next time!"));
    }

    public static void PreventPlayer(Player player){
        if (player.getWorld().getName().equals("world")){
            if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE){
                Location spawnLocation = GetLocation("spawn");
                assert spawnLocation != null;
                player.teleport(spawnLocation);
            }
        }
    }

    @EventHandler
    public void OnJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        machineCount.putIfAbsent(player.getUniqueId(), 0);

        InitAttributes(player);
        StartAllMachines(player);
        LoadPlayerProgress(player);

        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player.getUniqueId());

        Location spawnLocation = GetLocation("spawn");
        if (!player.hasPlayedBefore()){
            assert spawnLocation != null;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.teleport(spawnLocation);
            }, 5L);
            //player.setBedSpawnLocation(spawnLocation, true);
        }else{
            //player.setBedSpawnLocation(spawnLocation, true);
        }

        SetPlayerIslandLimit(superiorPlayer);
    }

    @EventHandler
    public void OnQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        StopAllMachines(player);

        SavePlayerProgress(event.getPlayer());
        HandleCloseEvent(player);
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

        UpdateMachineTag(player.getName(), location, machineName, 0);
        player.sendMessage(sendText("&aMachine has been enabled!"));
    }

    public static int globalMaxMachine = 50;
    public static HashMap<UUID, Integer> maxMachines = new HashMap<>();

    public static void StartAllMachines(Player player){
        int mCount = 0;

        playerPlacedMachines.putIfAbsent(player.getUniqueId(), new ArrayList<>());

        playerPlacedMachines.get(player.getUniqueId()).clear();

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

                        playerPlacedMachines.get(player.getUniqueId()).add(locationParsed);

                    } catch (NumberFormatException e) {
                        consoleLog(sendText("&cError: Invalid number format in machine data for " + location));
                    }
                }
            }
        }

        machineCount.put(player.getUniqueId(), mCount);
        consoleLog(sendText("&aAll Machines owned by &2" + player.getName() + " &ahas been started with &6" + machineCount.get(player.getUniqueId()) + " Machines!"));
    }

    public static void RefreshAllMachines(Player player){
        for (Location location : playerPlacedMachines.get(player.getUniqueId())){
            RefreshMachine(location);
        }

        //consoleLog(sendText("&aSuccesfully refreshed &2"+player.getName()+"'s &aPlaced Machines!"));
    }

    public static void InspectMachine(Player player){
        Location location = player.getTargetBlockExact(10).getLocation();
        if (placedMachines.get(location+__ownerKey) != null){
            OpenMachineEngines(player, location);
        }
    }

    public void DisableMachine(Player player, Location location){
        assert location != null;
        int taskId = Integer.parseInt(placedMachines.get(location+__taskIdKey));
        placedMachines.put(location+__machineStatusKey, "Disabled");
        getScheduler().cancelTask(taskId);
        String machineName = placedMachines.get(location+__machineNameKey);
        UpdateMachineTag(player.getName(), location, machineName, taskId);
        player.sendMessage(sendText("&cMachine has stopped!"));
    }

    public static void StopAllMachines(Player player){
        for (String key : placedMachines.keySet()) {
            if (key.endsWith(__locationKey)) {
                String location = key.replace(__locationKey, "");

                String owner = placedMachines.get(location+__ownerKey);
                if (owner.equals(player.getName())){
                    Location parsedLocation = parseLocationString(location);
                    assert parsedLocation != null;
                    int taskId = Integer.parseInt(placedMachines.get(location+__taskIdKey));
                    if (!placedMachines.get(location+__machineStatusKey).equals("Disabled")
                    && !placedMachines.get(location+__machineStatusKey).equals("Broken")){
                        placedMachines.put(location+__machineStatusKey, "Inactive");
                    }
                    getScheduler().cancelTask(taskId);
                    String machineName = placedMachines.get(location+__machineNameKey);
                    UpdateMachineTag(player.getName(), parsedLocation, machineName, taskId);
                }
            }
        }
        consoleLog(sendText("&aAll Machines owner by &2"+player.getName()+" &ahas been stopped!"));
    }

    @EventHandler
    public void BlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.AIR){
            ItemMeta meta = item.getItemMeta();
            if (isFactoryItem(item)){
                if (item.getType() == Material.SPAWNER){
                    return;
                }
                event.setCancelled(true);
            }
        }
    }
    
    public static HashMap<Player, Boolean> canRemoveMachine = new HashMap<>();

    @EventHandler
    public void MachinePlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        Block block = event.getBlock();
        Location location = block.getLocation();

        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (isMachine(offHandItem)){
            event.setCancelled(true);
            player.sendMessage(sendText("&4Please empty your offhand before placing machine!"));
            return;
        }

        if (block.getType() != Material.AIR){
            ItemStack item = player.getInventory().getItemInMainHand().clone();

            if (!isMachine(item)){
                return;
            }
            if (event.isCancelled()){
                return;
            }

            if (offHandItem.getType() != Material.AIR){
                event.setCancelled(true);
                player.sendMessage(sendText("&4Please empty your offhand before placing machine!"));
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

            if (container.has(GetNamespacedKey(levelMinimumKey))){
                Integer levelMinimum = container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER);
                if (levelMinimum == null){
                    levelMinimum = 100;
                }

                Integer prestigeMinimum = GetPrestigeRequirement(levelMinimum);

                if (playerLevel.get(player.getUniqueId()) < levelMinimum){
                    event.setCancelled(true);
                    player.sendMessage(sendText(Notification_NoLevel(player)));
                }

                if (playerPrestige.get(player.getUniqueId()) < prestigeMinimum){
                    event.setCancelled(true);
                    player.sendMessage(sendText("&cYou need to be prestige &4&l"+intToRoman(prestigeMinimum)+" &cto place thie machine!"));
                }

                if (playerLevel.get(player.getUniqueId()) < levelMinimum || playerPrestige.get(player.getUniqueId()) < prestigeMinimum){
                    event.setCancelled(true);
                    //player.sendMessage(sendText(Notification_NoLevel(player)));
                    return;
                }
            }

            /*if (playerCooldown.get(player.getName()+".cooldown.Place Machine") > 0){
                event.setCancelled(true);
                int cooldown = playerCooldown.get(player.getName()+".cooldown.Place Machine");
                player.sendMessage(sendText("&4Please wait &6"+cooldown+"s &4before placing machine again!"));
                PlaySoundAt(Sound.ENTITY_VILLAGER_NO, player.getLocation(), 1, 2);
                return;
            }*/

            playerCooldown.put(player.getName()+".cooldown.Place Machine", 2);
            int mc = machineCount.get(player.getUniqueId());
            if (mc >= maxMachines.get(player.getUniqueId())){
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

            playerPlacedMachines.get(player.getUniqueId()).add(location);

            RefreshAllMachines(player);

            Bukkit.getScheduler().runTaskLater(getMainPlugin(), () -> {
                if (location.getBlock().getType() != Material.AIR){
                    RefreshNearbyTags(player.getName());
                }
            }, 10L);


            boolean canRemove = canRemoveMachine.get(player);
            
            if (canRemove){
                canRemoveMachine.put(player, false);

                Bukkit.getScheduler().runTaskLater(getMainPlugin(), () -> {
                    if (!canRemoveMachine.get(player)){
                        canRemoveMachine.put(player, true);
                    }
                }, 60L);
            }

            PlaySoundAt(Sound.BLOCK_ANVIL_PLACE, location, 1, 3);
            PlayParticleAtBlock(block, Particle.SMOKE);
            SpawnBlockCrackParticle(block);
            player.sendMessage(sendText("&bPlaced Machine "+meta.getDisplayName()+" &6["+mc+"&e/&6"+maxMachines.get(player.getUniqueId())+"]"));
            SendTitle(player, "&3Left-Click &fto Remove", "&3Shift-Left &fto Open Engine", 0, 1, 1);
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

                    /*if (playerCooldown.get(player.getName()+".cooldown.Place Machine") > 0){
                        event.setCancelled(true);
                        int cooldown = playerCooldown.get(player.getName()+".cooldown.Place Machine");
                        player.sendMessage(sendText("&4Please wait &6"+cooldown+"s &4before interacting with machine again!"));
                        PlaySoundAt(Sound.ENTITY_VILLAGER_NO, player.getLocation(), 1, 2);
                        return;
                    }*/
                    String owner = placedMachines.get(location+__ownerKey);
                    int machineLevel = Integer.parseInt(placedMachines.get(location+__machineLevelKey));
                    if (owner.equals(player.getName())){

                        boolean canRemove = canRemoveMachine.get(player);

                        if (!canRemove){
                            event.setCancelled(true);
                            //SendTitle(player, "", "&cPlease wait ")
                            return;
                        }

                        if (player.isSneaking()){
                            OpenMachineEngines(player, location);
                            event.setCancelled(true);
                            return;
                        }

                        if (player.getInventory().firstEmpty() == -1){
                            event.setCancelled(true);
                            player.sendMessage(Notification_InventoryFull(player));
                            return;
                        }

                        playerCooldown.put(player.getName()+".cooldown.Place Machine", 2);
                        RemoveMachine(player.getUniqueId(), block, location);
                        //RefreshNearbyTags(player.getName());
                    }else{
                        player.sendMessage(sendText("&4This is not your machine! &c(owned by: "+owner+")"));
                        PlaySoundAt(Sound.BLOCK_NOTE_BLOCK_BIT, location, 1, 0);
                        RefreshNearbyTags(owner);
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

        Integer levelMinimum = GetLevelMinimum(name.toLowerCase()
                        .replaceAll(" ", "").trim());

        if (levelMinimum == null){
            levelMinimum = 1;
        }

        obtainedMachine = new ItemStack(CreateMachine(name, machineLevel, speed, productionRate
                , steamConsumption, durability, maxDurability, material, dropName, potentialDrop, Rarity.RarityType.parseRarity(rarity),
                status, totalProduction, MachineType.parseType(machineType), steamProduction, true, levelMinimum));

        Player onlinePlayer = getPlayer(player);

        if (onlinePlayer != null){
            Map<Integer, ItemStack> mapItem = onlinePlayer.getInventory().addItem(obtainedMachine.clone());

            if (!mapItem.isEmpty()){
                StoreMachine(player, obtainedMachine);
                onlinePlayer.sendMessage(Notification_InventoryFull(onlinePlayer));
            }
        }else{
            StoreMachine(player, obtainedMachine);
        }

        getScheduler().cancelTask(taskId);
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
            //onlinePlayer.sendMessage(" ");
            onlinePlayer.sendMessage(sendText("&bRemoved Machine "+obtainedMachine.getItemMeta().getDisplayName()+" &6["+mc+"&e/&6"+maxMachines.get(player)+"]"));
        }

        playerPlacedMachines.get(player).remove(location);

        RemoveMachineTags(location, taskId);
        PlaySoundAt(Sound.BLOCK_CHEST_CLOSE, location, 1, 3);
    }

    public void StoreMachine(UUID player, ItemStack obtainedMachine){
        storedMachines.putIfAbsent(player, new ArrayList<>());
        List<ItemStack> storedMachineList = new ArrayList<>(storedMachines.get(player));
        storedMachineList.add(obtainedMachine.clone());
        storedMachines.put(player, storedMachineList);
        consoleLog(sendText("&2"+ getOfflinePlayer(player).getName()+" &ais offline or having full inventory so machine items are stored in &6[StoredMachines]"));
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

    public static void RemoveMachineTags(Location location, int taskId){
        World world = location.getWorld();
        for (Entity entity : getWorld(world.getName()).getEntities()){
            if (entity instanceof TextDisplay){
                if (entity.getScoreboardTags().contains("MachineTag."+location)){
                    entity.remove();
                }
            }
        }
    }

    public static void SpawnMachineTag(String player, Location location, String machineName, int taskId) {
        //RemoveMachineTags(location, 0);
        Vector tagOffset = new Vector(0.5, 1.5, 0.5);
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

        spawnMachineTag(spawnLocation, sendText(machineName)+" &8[&9"+player+"&8]", location);
        spawnMachineTag(spawnLocation2, statusColor + machineStatus, location);
        //spawnMachineTag(spawnLocation3, "&8[&9"+player+"&8]", location);
        //spawnMachineTag(spawnLocation4, "&8[ &9Left-Click &fto &3Take &8]", location);
        //spawnMachineTag(spawnLocation5, "&8[ &9Sneak &f+ &9Left-Click &fto &3Open &8]", location);
    }

    public static void RefreshMachineTag(String type, String target){
        if (type.equals("all")){
            for (String key : placedMachines.keySet()){
                if (key.endsWith(__locationKey)){
                    Location parsedLocation = parseLocationString(placedMachines.get(key));
                    String owner = placedMachines.get(parsedLocation+__ownerKey);
                    String machineName = placedMachines.get(parsedLocation+__machineNameKey);

                    for (Entity display : parsedLocation.getWorld().getEntities()){
                        if (display instanceof TextDisplay){
                            if (display.getScoreboardTags().contains("MachineTag."+parsedLocation)){
                                display.remove();
                            }
                        }
                    }

                    SpawnMachineTag(owner, parsedLocation, machineName, 0);
                }
            }
        }else{
            for (String key : placedMachines.keySet()){
                if (key.endsWith(__locationKey)){
                    Location parsedLocation = parseLocationString(placedMachines.get(key));
                    String owner = placedMachines.get(parsedLocation+__ownerKey);
                    String machineName = placedMachines.get(parsedLocation+__machineNameKey);

                    if (target.equals(owner)){
                        for (Entity display : parsedLocation.getWorld().getEntities()){
                            if (display instanceof TextDisplay){
                                if (display.getScoreboardTags().contains("MachineTag."+parsedLocation)){
                                    display.remove();
                                }
                            }
                        }
                        SpawnMachineTag(owner, parsedLocation, machineName, 0);
                    }
                }
            }
        }

    }

    public static void DelayedRefreshMachineTag(Player player){

        Bukkit.getScheduler().runTaskLater(getMainPlugin(), () -> {
            SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
            Island island = SuperiorSkyblockAPI.getIslandAt(superiorPlayer.getLocation());

            if (island == null){
                return;
            }

            SuperiorPlayer owner = island.getOwner();

            if (player.getWorld().getName().equals("SuperiorWorld")){

                RefreshMachineTag("none", owner.getName());
                //consoleLog("Refreshed Owner: "+owner.getName()+" tags");

                for (SuperiorPlayer p : island.getIslandMembers()){
                    if (!p.getName().equals(owner.getName())){
                        RefreshMachineTag("none", p.getName());
                        //consoleLog("Refreshed Member: "+p.getName()+" tags");
                    }
                }

            }
        }, 35L);

    }

    @EventHandler
    public void OnIslandTeleport(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        if (command.startsWith("/is") ||
                command.startsWith("/island") ||
                command.startsWith("/factory") ||
                command.startsWith("/fac") ||
                command.startsWith("/is admin teleport")) {
            //DelayedRefreshMachineTag(player);
        }
    }

    @EventHandler
    public void OnTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN){
            DelayedRefreshMachineTag(event.getPlayer());

            SetIslandUpgrades(event.getPlayer());
        }
    }

    public static void SetIslandUpgrades(Player player) {
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player.getUniqueId());
        Island island = superiorPlayer.getIsland();

        if (island == null) {
            player.sendMessage("You don't have an island.");
            return;
        }
        //island.setBlockLimit(Key.of(Material.SPAWNER), 15);

        SetIslandDefaultUpgrades(island);

        Map<String, Integer> upgrades = island.getUpgrades();
        /*if (upgrades.isEmpty()) {
            return;
        }*/

        for (Map.Entry<String, Integer> entry : upgrades.entrySet()) {
            String upgradeId = entry.getKey();
            int level = entry.getValue();

            if (upgradeId.equals("members-limit")){
                if (level == 2){
                    island.setTeamLimit(4);
                    consoleLog(sendText("&aTeam Limit of "+player.getName()+" &ahas been set to 4"));
                }
                else if (level == 3){
                    island.setTeamLimit(7);
                    consoleLog(sendText("&aTeam Limit of "+player.getName()+" &ahas been set to 7"));
                }
                else if (level == 4){
                    island.setTeamLimit(10);
                    consoleLog(sendText("&aTeam Limit of "+player.getName()+" &ahas been set to 10"));
                }
            }

            else if (upgradeId.equals("hoppers-limit")){
                if (level == 2){
                    island.setBlockLimit(Key.of(Material.HOPPER), 45);
                    consoleLog(sendText("&aHopper Limit of "+player.getName()+" &ahas been set to 45"));
                }
                else if (level == 3){
                    island.setBlockLimit(Key.of(Material.HOPPER), 75);
                    consoleLog(sendText("&aHopper Limit of "+player.getName()+" &ahas been set to 75"));
                }
                else if (level == 4){
                    island.setBlockLimit(Key.of(Material.HOPPER), 154);
                    consoleLog(sendText("&aHopper Limit of "+player.getName()+" &ahas been set to 154"));
                }
            }

            else if (upgradeId.equals("minecarts-limit")){
                if (level == 2){
                    island.setEntityLimit(Key.of(EntityType.MINECART), 10);
                    island.setEntityLimit(Key.of(EntityType.HOPPER_MINECART), 10);
                    island.setEntityLimit(Key.of(EntityType.CHEST_MINECART), 10);
                    island.setEntityLimit(Key.of(EntityType.TNT_MINECART), 10);
                    island.setEntityLimit(Key.of(EntityType.FURNACE_MINECART), 10);
                    consoleLog(sendText("&aMinecart Limit of "+player.getName()+" &ahas been set to 10"));
                }
                else if (level == 3){
                    island.setEntityLimit(Key.of(EntityType.MINECART), 15);
                    island.setEntityLimit(Key.of(EntityType.HOPPER_MINECART), 15);
                    island.setEntityLimit(Key.of(EntityType.CHEST_MINECART), 15);
                    island.setEntityLimit(Key.of(EntityType.TNT_MINECART), 15);
                    island.setEntityLimit(Key.of(EntityType.FURNACE_MINECART), 15);
                    consoleLog(sendText("&aMinecart Limit of "+player.getName()+" &ahas been set to 15"));
                }
                else if (level == 4){
                    island.setEntityLimit(Key.of(EntityType.MINECART), 20);
                    island.setEntityLimit(Key.of(EntityType.HOPPER_MINECART), 20);
                    island.setEntityLimit(Key.of(EntityType.CHEST_MINECART), 20);
                    island.setEntityLimit(Key.of(EntityType.TNT_MINECART), 20);
                    island.setEntityLimit(Key.of(EntityType.FURNACE_MINECART), 20);
                    consoleLog(sendText("&aMinecart Limit of "+player.getName()+" &ahas been set to 20"));
                }
            }

            else if (upgradeId.equals("border-size")){
                if (level == 2){
                    island.setIslandSize(75);
                    consoleLog(sendText("&aBorder Size of "+player.getName()+" &ahas been set to 75"));
                }
                else if (level == 3){
                    island.setIslandSize(100);
                    consoleLog(sendText("&aBorder Size of "+player.getName()+" &ahas been set to 100"));
                }
                else if (level == 4){
                    island.setIslandSize(200);
                    consoleLog(sendText("&aBorder Size of "+player.getName()+" &ahas been set to 200"));
                }else{
                    island.setIslandSize(25);
                }
            }

            //player.sendMessage("Upgrade: " + upgradeId + ", Level: " + level);
        }
    }

    public static void RefreshNearbyTags(String player){
        for (String key : placedMachines.keySet()){
            if (key.endsWith(__locationKey)){
                String loc = key.replace(__locationKey, "");
                Location parsedLocation = parseLocationString(loc);
                String owner = placedMachines.get(parsedLocation+__ownerKey);
                if (player.equals(owner)){
                    UpdateMachineTag(player, parsedLocation, placedMachines.get(parsedLocation+__machineNameKey), 0);
                }
            }
        }
    }

    public static void UpdateMachineTag(String player, Location location, String machineName, int taskId){
       /* World world = location.getWorld();
        for (Entity entity : Bukkit.getWorld(world.getName()).getEntities()){
            if (entity instanceof TextDisplay){
                if (entity.hasMetadata("MachineTag."+location)){
                    entity.remove();
                }
            }
        }*/
        RemoveMachineTags(location, 0);
        SpawnMachineTag(player, location, machineName, taskId);
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




    public static void spawnMachineTag(Location location, String name, Location machineLocation) {

        World world = location.getWorld();
        if (world == null) return;

        TextDisplay textDisplay = (TextDisplay) world.spawnEntity(location, EntityType.TEXT_DISPLAY);

        textDisplay.setText(sendText(name));
        textDisplay.setBillboard(Display.Billboard.CENTER);
        textDisplay.setShadowed(false);
        textDisplay.setSeeThrough(false);
        textDisplay.setLineWidth(300);
        textDisplay.setViewRange(5.0f);

        textDisplay.addScoreboardTag("MachineTag." + machineLocation);



        /*Bukkit.getScheduler().runTaskLater(plugin, () -> {

        }, 5L);*/
    }



    public static void PlayerInventoryItems(Player player){
        //PlayerItemAttributes(player);
        PlayerMiningSpeed(player);
        UpdatePlayerItem(player);
    }

    @EventHandler
    public void OnOpenInventory(InventoryOpenEvent event){
        Bukkit.getScheduler().runTaskLater(getMainPlugin(), () -> PlayerItemAttributes((Player) event.getPlayer()), 5L);
    }

    @EventHandler
    public void OnInventoryClick(InventoryClickEvent event){
        Bukkit.getScheduler().runTaskLater(getMainPlugin(), () -> PlayerItemAttributes((Player) event.getWhoClicked()), 5L);

        if (event.getSlot() != 8){
            if (event.getCurrentItem() != null){
                if (event.getCurrentItem().getType() != Material.AIR){
                    if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(GetNamespacedKey(gameMenuKey))){
                        event.setCurrentItem(new ItemStack(Material.AIR));
                    }
                }
            }
        }
    }

    @EventHandler
    public void OnInventoryClose(InventoryCloseEvent event){
        Bukkit.getScheduler().runTaskLater(getMainPlugin(), () -> PlayerItemAttributes((Player) event.getPlayer()), 5L);
    }

    @EventHandler
    public void OnItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack pickedItem = event.getItem().getItemStack();
        if (isTool(pickedItem) || isMachine(pickedItem) || isFactoryItem(pickedItem)) {
            Bukkit.getScheduler().runTaskLater(getMainPlugin(), () -> PlayerItemAttributes(player), 5L);
        }
    }

    public static void PlayerItemAttributes(Player player){

        PlayerInventory inventory = player.getInventory();

        for (ItemStack item : inventory) {
            if (item != null && item.getType() != Material.AIR ) {
                if (isTool(item)){
                    ItemMeta meta = item.getItemMeta();

                    if (!meta.isUnbreakable()) {
                        meta.setUnbreakable(true);
                    }

                    if (!meta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES) || !meta.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)) {


                        NamespacedKey damageKey = new NamespacedKey(getMainPlugin(), "attack_damage");
                        AttributeModifier damageModifier = new AttributeModifier(damageKey, -100, AttributeModifier.Operation.ADD_NUMBER);

                        NamespacedKey speedKey = new NamespacedKey(getMainPlugin(), "attack_speed");
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
                    ItemMeta meta = item.getItemMeta();
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    if (!isMachine(item) && !isFactoryItem(item)){
                        if (!item.getItemMeta().hasLore() && !item.getItemMeta().hasDisplayName()){
                            item.setItemMeta(ProcessItemMeta(item).getItemMeta());
                        }
                    }
                }


                if (isMachine(item) || isFactoryItem(item)){
                    ItemMeta meta = item.getItemMeta();
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    if (!container.has(GetNamespacedKey(canUseKey))){
                        container.set(GetNamespacedKey(canUseKey), PersistentDataType.BOOLEAN, true);
                    }

                    if (container.has(GetNamespacedKey(levelMinimumKey))){
                        Integer levelMinimum = container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER);

                        if (!isFactoryItem(item)){
                            Integer machineLevelMinimum = GetLevelMinimum(uncolouredText(meta.getDisplayName()).toLowerCase().replaceAll(" ", "").trim());
                            if (machineLevelMinimum == null){
                                machineLevelMinimum = 1;
                            }


                            if (levelMinimum < machineLevelMinimum || levelMinimum > machineLevelMinimum){
                                container.set(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER, machineLevelMinimum);
                                item.setItemMeta(meta);
                                item.setItemMeta(UpdateMachineItem(item).getItemMeta());
                            }
                        }
                        Integer prestigeMinimum = GetPrestigeRequirement(levelMinimum);

                        Boolean canUseIndicator = container.get(GetNamespacedKey(canUseKey), PersistentDataType.BOOLEAN);
                        boolean cont = true;

                        if (isMachine(item)){
                            if (playerLevel.get(player.getUniqueId()) < levelMinimum || playerPrestige.get(player.getUniqueId()) < prestigeMinimum ){
                                cont = false;
                            }
                            /*else if (playerLevel.get(player.getUniqueId()) >= levelMinimum && playerPrestige.get(player.getUniqueId()) >= prestigeMinimum ){
                                cont = true;
                            }*/
                        }

                        else if (isFactoryItem(item)){
                            if (playerLevel.get(player.getUniqueId()) < levelMinimum){
                                cont = false;
                            }
                            /*else if (playerLevel.get(player.getUniqueId()) >= levelMinimum){
                                cont = true;
                            }*/
                        }

                        if (!cont){
                            if (canUseIndicator){
                                container.set(GetNamespacedKey(canUseKey), PersistentDataType.BOOLEAN, false);
                                item.setItemMeta(meta);
                                if (!isFactoryItem(item)){
                                    item.setItemMeta(UpdateMachineItem(item).getItemMeta());
                                }else{
                                    UpdateItem(player, "none", item);
                                    //consoleLog("updated true -> false");
                                }
                            }
                        }

                        if (cont){
                            if (!canUseIndicator){
                                container.set(GetNamespacedKey(canUseKey), PersistentDataType.BOOLEAN, true);
                                item.setItemMeta(meta);
                                if (!isFactoryItem(item)){
                                    item.setItemMeta(UpdateMachineItem(item).getItemMeta());
                                }else{
                                    UpdateItem(player, "none", item);
                                    //consoleLog("updated false -> true");
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    public static void PlayerMiningSpeed(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isPickaxe(item) && !isAxe(item) && !isShovel(item)){
            if (player.hasPotionEffect(PotionEffectType.HASTE)){
                player.removePotionEffect(PotionEffectType.HASTE);
            }
        }
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
            if (power == null){
                power = 1.0;
            }
            Double speed = container.get(GetNamespacedKey(toolSpeedKey), PersistentDataType.DOUBLE);
            if (speed == null){
                speed = 0.0;
            }
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
                if (!player.hasPotionEffect(PotionEffectType.HASTE)){
                    if (speed > 9){
                        PotionEffect haste = new PotionEffect(PotionEffectType.HASTE, 1000000000, ((int) (speed/10))-1, false, false);
                        player.addPotionEffect(haste);
                    }
                }else{
                    if (speed < 10){
                        player.removePotionEffect(PotionEffectType.HASTE);
                    }
                }
            }

            else if (isFishingRod(item)){
                if (speed >= 10){
                    itemMeta.addEnchant(Enchantment.LURE, (int) (speed/10), true);
                    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(itemMeta);
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

                if (!isFactoryItem(item)){
                    return;
                }


                if (!isTool(item)){
                    return;
                }


                if (!ItemNotBroken(item)){
                    if (!isCropBlock(event.getBlock())
                            && !isFruit(event.getBlock())){
                        Notification_ItemBroken(player);
                        event.setCancelled(true);
                        return;
                    }
                }

                if (container.has(GetNamespacedKey(levelMinimumKey))){
                    if (!hasLevel(player, container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER))){
                        event.setCancelled(true);
                        player.sendMessage(Notification_NoLevel(player));
                        return;
                    }
                }

                if (currentEvent != EventType.Invincible_Items){
                    ManageDurability(player, "hand");
                }
                UpdateItem(player, "hand", item);
            }
        }

    }

    public static boolean isSoil(Block block){
        return block.getType() == Material.DIRT ||
                block.getType() == Material.GRASS_BLOCK ||
                block.getType() == Material.PODZOL ||
                block.getType() == Material.COARSE_DIRT;
    }

    @EventHandler
    public void OnGrassHoe(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.AIR) {

            if (!player.getWorld().getName().equals("SuperiorWorld")){
                return;
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK){
                if (event.getClickedBlock() == null){
                    return;
                }

                ItemMeta meta = item.getItemMeta();

                if (!hasIslandAccess(player)) {
                    return;
                }

                if (!isSoil(event.getClickedBlock())){
                    return;
                }

                PersistentDataContainer container = meta.getPersistentDataContainer();

                if (container.has(GetNamespacedKey(itemKey))) {

                    if (!isFactoryItem(item)) {
                        return;
                    }

                    if (!isTool(item)) {
                        return;
                    }

                    if (!isHoe(item)){
                        return;
                    }


                    if (!ItemNotBroken(item)) {
                        Notification_ItemBroken(player);
                        event.setCancelled(true);
                        return;
                    }

                    if (container.has(GetNamespacedKey(levelMinimumKey))) {
                        if (!hasLevel(player, container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER))) {
                            event.setCancelled(true);
                            player.sendMessage(Notification_NoLevel(player));
                            return;
                        }
                    }

                    event.getClickedBlock().setType(Material.FARMLAND);

                    if (currentEvent != EventType.Invincible_Items){
                        ManageDurability(player, "hand");
                    }

                    UpdateItem(player, "hand", item);

                    PlaySoundAt(Sound.ENTITY_VILLAGER_WORK_FARMER, event.getClickedBlock().getLocation(), 1, 2);
                }
            }
        }
    }


    // Armor Equip Manager

    public static boolean CheckArmor(Player player, ItemStack item){
        if (item == null){
            return false;
        }
        if (item.getType() == Material.AIR){
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(GetNamespacedKey(itemKey))){
            if (!ItemNotBroken(item)){
                Notification_ItemBroken(player);
                return false;
            }
            if (container.has(GetNamespacedKey(levelMinimumKey))){
                if (!hasLevel(player, container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER))){
                    player.sendMessage(Notification_NoLevel(player));
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    @EventHandler
    public void OnConsume(PlayerItemConsumeEvent event){
        if (isFactoryItem(event.getItem())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorEquipViaInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemStack item2 = player.getInventory().getItemInOffHand();

        if (event.getAction().isRightClick()) {
            if (item.getType() != Material.AIR || item2.getType() != Material.AIR) {

                boolean mainHandIsArmor = isArmor(item);
                boolean offHandIsArmor = isArmor(item2);

                if (!mainHandIsArmor && !offHandIsArmor) {
                    return;
                }

                if (mainHandIsArmor && !CheckArmor(player, item)) {
                    event.setCancelled(true);
                    return;
                }

                if (offHandIsArmor && !CheckArmor(player, item2)) {
                    event.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void onArmorEquip(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        InventoryType.SlotType slotType = event.getSlotType();
        if (slotType == InventoryType.SlotType.ARMOR) {
            ItemStack item = player.getItemOnCursor();
            if (item.getType() != Material.AIR){
                if (!isArmor(item)){
                    return;
                }

                if (event.isCancelled()){
                    return;
                }
                if (!CheckArmor(player, item)) {
                    event.setCancelled(true);
                }
            }
        }else{
            if (event.getClick() == ClickType.SHIFT_RIGHT || event.getClick() == ClickType.SHIFT_LEFT){

                if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CHEST
                || player.getOpenInventory().getTopInventory().getType() == InventoryType.ENDER_CHEST ||
                        player.getOpenInventory().getTopInventory().getType() == InventoryType.DISPENSER||
                        player.getOpenInventory().getTopInventory().getType() == InventoryType.DROPPER ||
                        player.getOpenInventory().getTopInventory().getType() == InventoryType.BARREL){
                    return;
                }

                ItemStack item = event.getCurrentItem();
                if (item.getType() != Material.AIR){
                    if (!isArmor(item)){
                        return;
                    }

                    if (event.isCancelled()){
                        return;
                    }
                    if (!CheckArmor(player, item)){
                        event.setCancelled(true);
                    }
                }
            }
        }
    }



    public static List<String> attributeList = Arrays.asList(
      "Steam", "Movement Speed", "Steam Regen", "Attack Damage", "Attack Range", "Attack Speed", "Critical Chance", "Critical Damage"
            , "Accuracy", "Armor", "Undead Damage", "Undead Defense", "Mutant Damage", "Mutant Defense", "Alien Damage", "Alien Defense",
            "Melee Damage", "Range Damage", "Steam Consumption", "Health"
    );

    public static List<String> cooldownList = Arrays.asList(
            "Place Machine"
    );

    @EventHandler
    public void OnItemSpawn(ItemSpawnEvent event){
        ItemStack item = event.getEntity().getItemStack();
        if (!isTool(item) && !isFactoryItem(item)){
            if (item.getType() != Material.AIR){
                ItemMeta meta = item.getItemMeta();
                if (!meta.hasLore() && !meta.hasDisplayName()){
                    if (isCropItem(item)){
                        event.setCancelled(true);
                    }else{
                        event.getEntity().getItemStack().setItemMeta(ProcessItemMeta(event.getEntity().getItemStack()).getItemMeta());
                    }
                }
            }
        }

        if (item.getType() == Material.SPAWNER){
            if (item.getItemMeta().getLore() == null){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void OnSpawnerEgg(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK){
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType().toString().contains("SPAWN_EGG")){
                if (event.getClickedBlock() != null){
                    if (event.getClickedBlock().getType() == Material.SPAWNER){
                        event.setCancelled(true);
                        player.sendMessage(sendText("&4You can't change spawner type!"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void OnItemCraft(PrepareItemCraftEvent event) {
        if (event.getInventory().getResult() != null) {
            ItemStack item = event.getInventory().getResult();

            // If the item is disallowed (armor or END_CRYSTAL/RESPAWN_ANCHOR), cancel it
            if (isDisabledItem(item)) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
            }
            // If it's not a tool, but allowed, process meta
            else if (!isTool(item)) {
                item.setItemMeta(ProcessItemMeta(item).getItemMeta());
                event.getInventory().setResult(item);
            }
        }
    }


    public static void InventoryItemCheck(Player player){
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();

            String attributesKey = "hand";

            boolean canUse = true;

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(GetNamespacedKey("item"))){
                canUse = false;
            }

            Integer levelMinimum = container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER);
            if (levelMinimum == null){
                levelMinimum = 1;
            }
            if (playerLevel.get(player.getUniqueId()) < levelMinimum){
                canUse = false;
            }

            if (isWeapon(item)){
                if (!ItemNotBroken(item)){
                    canUse = false;
                }
                //player.sendMessage("this is weapon");

                for (String attr : attributeList) {
                    playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + FixedAttributes(attr), 0.0);
                }

                for (NamespacedKey key : container.getKeys()) {
                    String fixedKey = key.getKey().toLowerCase().replaceAll(" ", "").trim();
                    /*consoleLog("key: "+key);
                    consoleLog("fixedKey: "+fixedKey);*/
                    if (isValidAttributes(fixedKey) && canUse) {
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

            boolean canUse = true;

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(GetNamespacedKey("item"))){
                canUse = false;
            }

            Integer levelMinimum = container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER);
            if (levelMinimum == null){
                levelMinimum = 1;
            }
            if (playerLevel.get(player.getUniqueId()) < levelMinimum){
                canUse = false;
            }

            if (isHelmet(item)){
                if (!ItemNotBroken(item)){
                    canUse = false;
                }

                for (String attr : attributeList) {
                    playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + FixedAttributes(attr), 0.0);
                }

                for (NamespacedKey key : container.getKeys()) {
                    String fixedKey = key.getKey().toLowerCase().replaceAll(" ", "").trim();
                    if (isValidAttributes(fixedKey) && canUse) {
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

            boolean canUse = true;

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(GetNamespacedKey("item"))){
                canUse = false;
            }

            Integer levelMinimum = container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER);
            if (levelMinimum == null){
                levelMinimum = 1;
            }
            if (playerLevel.get(player.getUniqueId()) < levelMinimum){
                canUse = false;
            }

            if (isChestplate(item)){
                if (!ItemNotBroken(item)){
                    canUse = false;
                }

                for (String attr : attributeList) {
                    playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + FixedAttributes(attr), 0.0);
                }

                for (NamespacedKey key : container.getKeys()) {
                    String fixedKey = key.getKey().toLowerCase().replaceAll(" ", "").trim();
                    if (isValidAttributes(fixedKey) && canUse) {
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

            boolean canUse = true;

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(GetNamespacedKey("item"))){
                canUse = false;
            }

            Integer levelMinimum = container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER);
            if (levelMinimum == null){
                levelMinimum = 1;
            }
            if (playerLevel.get(player.getUniqueId()) < levelMinimum){
                canUse = false;
            }

            if (isLeggings(item)){
                if (!ItemNotBroken(item)){
                    canUse = false;
                }

                for (String attr : attributeList) {
                    playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + FixedAttributes(attr), 0.0);
                }

                for (NamespacedKey key : container.getKeys()) {
                    String fixedKey = key.getKey().toLowerCase().replaceAll(" ", "").trim();
                    if (isValidAttributes(fixedKey) && canUse) {
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

            boolean canUse = true;

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(GetNamespacedKey("item"))){
                canUse = false;
            }

            Integer levelMinimum = container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER);
            if (levelMinimum == null){
                levelMinimum = 1;
            }
            if (playerLevel.get(player.getUniqueId()) < levelMinimum){
                canUse = false;
            }

            if (isBoots(item)){
                if (!ItemNotBroken(item)){
                    canUse = false;
                }

                for (String attr : attributeList) {
                    playerAttributes.put(player.getName() + ".attribute."+attributesKey+"." + FixedAttributes(attr), 0.0);
                }

                for (NamespacedKey key : container.getKeys()) {
                    String fixedKey = key.getKey().toLowerCase().replaceAll(" ", "").trim();
                    if (isValidAttributes(fixedKey) && canUse) {
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

    public static String FixedAttributes(String attr){
        return attr.toLowerCase().replaceAll(" ", "").trim();
    }

    public static void CalculateAttributes(Player player) {
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
        playerMaxSteam.put(player, defaultMaxSteam+playerAttributes.get(player.getName()+".attribute.total.steam"));

        double totalMovementSpeed = playerAttributes.get(player.getName() + ".attribute.total.movementspeed");
        AttributeInstance movementSpeed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.setBaseValue(0.1 + (totalMovementSpeed / 10000));
        }
        player.setHealthScale(20);

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



    public static Boolean isValidAttributes(String value) {
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

            if (item.getType().toString().contains("SPAWN_EGG") ||
                    item.getType().toString().contains("BONE") ||
                    item.getType().toString().contains("WHEAT") ||
                    item.getType().toString().contains("INGOT") ||
            item.getType().toString().contains("SEEDS") ||
            item.getType().isEdible()){
                return;
            }

            ItemMeta meta = item.getItemMeta();

            /*if (!isFactoryItem(item)){
                return;
            }*/


            if (!isFactoryItem(item) || !isWeapon(item)){
                Entity target = player.getTargetEntity(3);

                if (player.isSneaking()){
                    RemoveEntityObject(target, player);
                }

                if (target instanceof LivingEntity){
                    if (!(target instanceof ArmorStand)){
                        ManageDamage(player, target, 2, false);
                    }
                }
                return;
            }

            PersistentDataContainer container = meta.getPersistentDataContainer();

            if (!hasLevel(player, container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER))){
                player.sendMessage(Notification_NoLevel(player));
                return;
            }

            Double attackRangeContainer = container.get(GetNamespacedKey(attackRangeKey), PersistentDataType.DOUBLE);
            assert attackRangeContainer != null;
            double attackRange = attackRangeContainer;

            Entity entity = player.getTargetEntity((int)attackRange);

            if (isSword(item) || isHammer(item)){
                PlayerBasicAttack(player, entity, "melee");
            }

            else if (isBow(item) || isGun(item) || isBlast(item)){
                PlayerBasicAttack(player, entity, "range");
            }

        }else{
            Entity target = player.getTargetEntity(3);
            if (player.isSneaking()){
                RemoveEntityObject(target, player);
            }
            if (target instanceof LivingEntity){
                if (!(target instanceof ArmorStand)){
                    ManageDamage(player, target, 2, false);
                }
            }
        }
    }

    public static void RemoveEntityObject(Entity target, Player player){

        if (!player.hasPermission("admin")){
            if (!hasIslandAccess(player)){
                return;
            }
        }

        if (!player.isSneaking()){
            player.sendMessage(sendText("&eSneak + Left-Click to remove Armor Stand/Minecart"));
            return;
        }

        if (target instanceof Minecart){
            if (target.getType() == EntityType.MINECART){
                DropItem(target.getLocation(), new ItemStack(Material.MINECART), 1);
            }
            else if (target.getType() == EntityType.CHEST_MINECART){
                DropItem(target.getLocation(), new ItemStack(Material.CHEST_MINECART), 1);
            }
            else if (target.getType() == EntityType.FURNACE_MINECART){
                DropItem(target.getLocation(), new ItemStack(Material.FURNACE_MINECART), 1);
            }
            else if (target.getType() == EntityType.TNT_MINECART){
                DropItem(target.getLocation(), new ItemStack(Material.TNT_MINECART), 1);
            }
            else if (target.getType() == EntityType.HOPPER_MINECART){
                DropItem(target.getLocation(), new ItemStack(Material.HOPPER_MINECART), 1);
            }

            target.remove();
        }

        if (target instanceof ArmorStand){

            if (((ArmorStand) target).getHelmet().getType() != Material.AIR){
                player.sendMessage(sendText("&4Can't remove this Armor Stand! &c(remove helmet)"));
                return;
            }
            if (((ArmorStand) target).getChestplate().getType() != Material.AIR){
                player.sendMessage(sendText("&4Can't remove this Armor Stand! &c(remove chestplate)"));
                return;
            }
            if (((ArmorStand) target).getLeggings().getType() != Material.AIR){
                player.sendMessage(sendText("&4Can't remove this Armor Stand! &c(remove leggings)"));
                return;
            }
            if (((ArmorStand) target).getBoots().getType() != Material.AIR){
                player.sendMessage(sendText("&4Can't remove this Armor Stand! &c(remove boots)"));
                return;
            }

            DropItem(target.getLocation(), new ItemStack(Material.ARMOR_STAND), 1);

            target.remove();
        }
    }

    @EventHandler
    public void OnEntityDamagePlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Entity damager) {
            if (event.getEntity() instanceof Player player) {
                if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
                        event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {

                    if (damager instanceof Mob ||
                            (damager instanceof Projectile projectile && projectile.getShooter() instanceof Mob)) {

                        event.setCancelled(true);
                        EntityDamagePlayer(player, damager);
                    }
                }
            }
        }
    }

    /*@EventHandler
    public void OnEntityDamagePlayerProjectile(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Mob){
            if (event.getEntity() instanceof Player player){
                event.setCancelled(true);
                Entity entity = (Entity) event.getEntity().getShooter();
                EntityDamagePlayer(player, entity);
            }
        }
    }*/

    public static void EntityDamagePlayer(Player player, Entity entity){

        Random random = new Random();

        int mobLevel = GetMobLevel(entity);
        double attackDamageValue = mobLevel + (mobLevel * random.nextDouble());

        if (GetEntityDungeonTier(entity.getLocation()) == 0){
            attackDamageValue = 3;
        }

        double armor = playerAttributes.get(player.getName()+".attribute.total.armor");
        double alienDefense = playerAttributes.get(player.getName()+".attribute.total.aliendefense");
        double mutantDefense = playerAttributes.get(player.getName()+".attribute.total.mutantdefense");
        double undeadDefense = playerAttributes.get(player.getName()+".attribute.total.undeaddefense");

        double damage = attackDamageValue + (attackDamageValue * random.nextDouble());

        if (isAlien(entity)){
            alienDefense = alienDefense+armor;
            damage = (attackDamageValue + alienDefense > 0) ? attackDamageValue / 2 * attackDamageValue / (attackDamageValue + alienDefense) : 0;
        }
        else if (isMutant(entity)){
            mutantDefense = mutantDefense+armor;
            damage = (attackDamageValue + mutantDefense > 0) ? attackDamageValue / 2 * attackDamageValue / (attackDamageValue + mutantDefense) : 0;
        }
        else if (isUndead(entity)){
            undeadDefense = undeadDefense+armor;
            damage = (attackDamageValue + undeadDefense > 0) ? attackDamageValue / 2 * attackDamageValue / (attackDamageValue + undeadDefense) : 0;
        }else{
            damage = (attackDamageValue + armor > 0) ? attackDamageValue / 2 * attackDamageValue / (attackDamageValue + armor) : 0;
        }

        double totalDamage = damage;

        int critRandom = random.nextInt(100)+1;
        if (critRandom <= 30){
            player.damage(totalDamage*2);
            spawnDamageIndicator(player.getLocation(), totalDamage*2, true);
        }else{
            player.damage(totalDamage);
            spawnDamageIndicator(player.getLocation(), totalDamage, false);
        }

        if (currentEvent != FactoryEvents.EventType.Invincible_Items){
            ManageDurability(player, "helmet");
            ManageDurability(player, "chestplate");
            ManageDurability(player, "leggings");
            ManageDurability(player, "boots");
        }

        UpdateItem(player, "helmet", player.getInventory().getHelmet());
        UpdateItem(player, "chestplate", player.getInventory().getChestplate());
        UpdateItem(player, "leggings", player.getInventory().getLeggings());
        UpdateItem(player, "boots", player.getInventory().getBoots());

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
            player.setCooldown(item.getType(), (int) (attackSpeed*20));

            Double steamConsumptionContainer = container.get(GetNamespacedKey(steamConsumptionKey), PersistentDataType.DOUBLE);
            assert steamConsumptionContainer != null;
            double steamConsumption = steamConsumptionContainer;

            if (!hasSteam(player, steamConsumption)){
                player.sendMessage(Notification_NoSteam(player));
                return;
            }

            if (isBow(item) || isGun(item) || isBlast(item)){
                RemoveSteam(player, steamConsumption);
                if (isBow(item)){
                    Arrow arrow = player.launchProjectile(Arrow.class);
                    arrow.setVelocity(player.getLocation().getDirection().multiply(3));
                    arrow.setShooter(player);

                    getScheduler().runTaskLater(plugin, () -> {
                        if (arrow.isValid()){
                            arrow.remove();
                            PlaySoundAt(Sound.ENTITY_ITEM_PICKUP, arrow.getLocation(), 1, 3);
                        }
                    }, (long) attackRange);
                }
                else if (isGun(item)){
                    Snowball ammo = player.launchProjectile(Snowball.class);
                    ammo.setVelocity(player.getLocation().getDirection().multiply(3));
                    ammo.setShooter(player);

                    getScheduler().runTaskLater(plugin, () -> {
                        if (ammo.isValid()){
                            ammo.remove();
                            PlaySoundAt(Sound.ENTITY_ITEM_PICKUP, ammo.getLocation(), 1, 3);
                        }
                    }, (long) attackRange);
                }
                else if (isBlast(item)){
                    ManageDamage(player, target, totalDamage*2, true);
                }
                if (currentEvent != FactoryEvents.EventType.Invincible_Items){
                    ManageDurability(player, "hand");
                }
                UpdateItem(player, "hand", item);
            }

            if (target instanceof LivingEntity){
                if (isSword(item) || isHammer(item)){
                    ManageDamage(player, target, totalDamage*2, true);

                    if (isSword(item)){
                        for (Entity entity : player.getNearbyEntities(5,5,5)){
                            if (entity instanceof Mob){
                                ManageDamage(player, entity, totalDamage*0.5, false);
                            }
                        }
                    }

                    else if (isHammer(item)){
                        for (Entity entity : target.getNearbyEntities(6,3,6)){
                            if (entity instanceof Mob){
                                if (entity != target){
                                    ManageDamage(player, entity, totalDamage*0.3, false);
                                }
                            }
                        }
                    }

                }
                RemoveSteam(player, steamConsumption);
                if (currentEvent != FactoryEvents.EventType.Invincible_Items){
                    ManageDurability(player, "hand");
                }
                UpdateItem(player, "hand", item);
            }

            String attackEffect = container.get(GetNamespacedKey(attackEffectKey), PersistentDataType.STRING);
            assert attackEffect != null;
            SpawnAttackEffect(player, AttackEffect.parseEffect(attackEffect), attackRange);

        }
    }

    public void ManageDamage(Player player, Entity target, double totalDamage, boolean main){

        /*
        formula

        double physicalDamageCalculation = (physicalDamageR + mobArmor > 0) ? physicalDamageR / 2 * physicalDamageR / (physicalDamageR + mobArmor) : 0;

         */


        if (!(target instanceof LivingEntity)){
            return;
        }

        double criticalDamage = playerAttributes.get(player.getName()+".attribute.total.criticaldamage");

        double alienDamage = playerAttributes.get(player.getName()+".attribute.total.aliendamage");
        double mutantDamage = playerAttributes.get(player.getName()+".attribute.total.mutantdamage");
        double undeadDamage = playerAttributes.get(player.getName()+".attribute.total.undeaddamage");

        if (target instanceof Mob) {
            double entityArmor = GetMobLevel(target);

            if (isAlien(target)){
                totalDamage = totalDamage+alienDamage;
            }
            else if (isMutant(target)){
                totalDamage = totalDamage+mutantDamage;
            }
            else if (isUndead(target)){
                totalDamage = totalDamage+undeadDamage;
            }

            double damageCalculation = (totalDamage + entityArmor > 0) ? totalDamage / 2 * totalDamage / (totalDamage + entityArmor) : 0;
            if (isCrit(player) && main){


                ((Mob) target).damage((damageCalculation*2)+criticalDamage, player);
                spawnDamageIndicator(target.getLocation(), (damageCalculation*2)+criticalDamage, true);
                //player.sendMessage(sendText("(mob) CRIT You damage enemy with: "+FormatDouble(totalDamage*2)));
                return;
            }
            ((Mob) target).damage(damageCalculation, player);
            spawnDamageIndicator(target.getLocation(), damageCalculation, false);
            //player.sendMessage(sendText("(mob) You damage enemy with: "+FormatDouble(totalDamage)));

            ManageMobHealthBar(target);
        }
        else if (target instanceof Player){
            if (target != player){
                double entityArmor = playerArmor.getOrDefault(target, 0.0);
                double damageCalculation = (totalDamage + entityArmor > 0) ? totalDamage / 2 * totalDamage / (totalDamage + entityArmor) : 0;

                if (isCrit(player) && main){
                    ((Player) target).damage((damageCalculation*2)+criticalDamage, player);
                    spawnDamageIndicator(target.getLocation(), (damageCalculation*2)+criticalDamage, true);
                    //player.sendMessage(sendText("(player) CRIT You damage enemy with: "+FormatDouble(totalDamage*2)));
                    return;
                }

                ((Player) target).damage(damageCalculation, player);
                spawnDamageIndicator(target.getLocation(), damageCalculation, false);
                //player.sendMessage(sendText("(player) You damage enemy with: "+FormatDouble(totalDamage)));
            }
        }
    }

    @EventHandler
    public void OnProjectileHit(ProjectileHitEvent event){
        Entity entity = event.getEntity();
        if (entity instanceof Arrow || entity instanceof Snowball) {
            if (event.getEntity().getShooter() instanceof Player player){
                Entity target = event.getHitEntity();
                if (target instanceof LivingEntity){
                    double totalDamage = CalculateDamage(player, target, "range");
                    ManageDamage(player, target, totalDamage*2, true);
                }
                entity.remove();
            }
        }
    }

    public static void spawnDamageIndicator(Location location, double damage, boolean crit) {
        Random random = new Random();
        double randomX = -0.5 + (2 * random.nextDouble());
        double randomY = 1 * (3 * random.nextDouble());
        double randomZ = -0.5 + (2 * random.nextDouble());

        World world = location.getWorld();

        Location spawnLocation = location.clone().add(randomX, randomY, randomZ);

        TextDisplay textDisplay = (TextDisplay) world.spawnEntity(spawnLocation, EntityType.TEXT_DISPLAY);

        if (!crit){
            textDisplay.setText(sendText("&f"+FormatDouble(damage)));
        }else{
            textDisplay.setText(sendText("&c&l"+FormatDouble(damage)));
        }
        textDisplay.setBillboard(Display.Billboard.CENTER);
        textDisplay.setShadowed(false);
        textDisplay.setSeeThrough(false);
        textDisplay.setLineWidth(300);
        textDisplay.setViewRange(30.0f);

        textDisplay.addScoreboardTag("DamageIndicator");

        Bukkit.getScheduler().runTaskLater(getMainPlugin(), textDisplay::remove, 35L);
    }

    public boolean isCrit(Player player){
        boolean crit = false;
        double criticalChance = playerAttributes.get(player.getName()+".attribute.total.criticalchance");
        if (criticalChance > 50){
            criticalChance = 50;
        }
        Random random = new Random();
        int criticalGet = random.nextInt(100)+1;
        if (criticalGet <= criticalChance){
            crit = true;
        }
        return crit;
    }

    public static boolean hasSteam(Player player, Double req){
        double current = playerSteam.get(player);
        if (playerSteam.get(player) < 0){
            playerSteam.put(player, 0.0);
        }
        return current >= req;
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

    public static void ManageDurability(Player player, String target){
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

    public static void UpdateItem(Player player, String target, ItemStack item){
        //ItemStack item = null;

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

                Color color = Color.NAVY;
                if (item.getType() == Material.LEATHER_HELMET || item.getType() == Material.LEATHER_CHESTPLATE
                        || item.getType() == Material.LEATHER_LEGGINGS || item.getType() == Material.LEATHER_BOOTS){
                    LeatherArmorMeta leatherMeta = (LeatherArmorMeta) item.getItemMeta();
                    color = leatherMeta.getColor();
                }

                Type item_type = Type.parseType(container.get(GetNamespacedKey(typeKey), PersistentDataType.STRING));
                SubType item_subType = SubType.parseSubType(container.get(GetNamespacedKey(subTypeKey), PersistentDataType.STRING));
                Double attackDamage = container.get(GetNamespacedKey(baseKey+attackDamageKey), PersistentDataType.DOUBLE);
                Double attackRange = container.get(GetNamespacedKey(attackRangeKey), PersistentDataType.DOUBLE);
                Double attackSpeed = container.get(GetNamespacedKey(attackSpeedKey), PersistentDataType.DOUBLE);
                Double criticalChance = container.get(GetNamespacedKey(baseKey+criticalChanceKey), PersistentDataType.DOUBLE);
                Double criticalDamage = container.get(GetNamespacedKey(baseKey+criticalDamageKey), PersistentDataType.DOUBLE);
                //player.sendMessage(""+criticalDamage);
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

                Double wandMultiplier = container.get(GetNamespacedKey(multiplierKey), PersistentDataType.DOUBLE);

                Integer levelMinimum = container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER);
                Double proficiency = container.get(GetNamespacedKey(proficiencyKey), PersistentDataType.DOUBLE);
                Boolean canUse = container.get(GetNamespacedKey(canUseKey), PersistentDataType.BOOLEAN);


                //List<String> bonusStats = new ArrayList<>();
                String bonusStats = container.get(GetNamespacedKey(bonusStatsKey), PersistentDataType.STRING);
                /*if (storedStats != null){
                    bonusStats = Arrays.asList(storedStats.split(","));
                }*/

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
                updatedItem.setCriticalDamage(criticalDamage);
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
                if (bonusStats == null){
                    bonusStats = "";
                }
                updatedItem.setBonusStats(bonusStats);
                updatedItem.setDisplayname(sendText(displayname));
                updatedItem.setMaterial(material);

                updatedItem.setLevelMinimum(levelMinimum);

                updatedItem.canUse(canUse);

                updatedItem.setAttackEffect(AttackEffect.parseEffect(attackEffect));

                updatedItem.setToolPower(toolPower);
                if (toolSpeed == null){
                    toolSpeed = 1.0;
                }
                updatedItem.setToolSpeed(toolSpeed);

                updatedItem.setColor(color);

                if (wandMultiplier == null){
                    wandMultiplier = 1.0;
                }
                updatedItem.setMultiplier(wandMultiplier);

                if (proficiency == null){
                    proficiency = 0.0;
                }
                updatedItem.setProficiency(proficiency);

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


    // Island disband kick or join manager to prevent machine lost

    @EventHandler
    public void OnPlayerKickedFromIsland(IslandKickEvent event) {
        SuperiorPlayer kickedPlayer = event.getTarget();
        Island island = event.getIsland();
        ManageIslandDisband(kickedPlayer);
    }

    @EventHandler
    public void OnPlayerJoinIsland(IslandJoinEvent event) {
        SuperiorPlayer player = event.getPlayer();
        Player bukkitPlayer = Bukkit.getPlayer(player.getUniqueId());
        Island island = event.getIsland();
        if (machineCount.get(player.getUniqueId()) > 0){
            event.setCancelled(true);
            bukkitPlayer.sendMessage(sendText("&4Remove all machine before joining others!"));
        }
    }

    @EventHandler
    public void OnPlayerIslandDisband(IslandDisbandEvent event) {
        SuperiorPlayer player = event.getPlayer();
        Player bukkitPlayer = Bukkit.getPlayer(player.getUniqueId());
        Island island = event.getIsland();
        if (machineCount.get(player.getUniqueId()) > 0){
            event.setCancelled(true);
            bukkitPlayer.sendMessage(sendText("&4Remove all machine before disband!"));
        }
    }


    @EventHandler
    public void IslandCreate(IslandCreateEvent event) {
        SuperiorPlayer player = event.getPlayer();
        Island island = event.getIsland();

        SetIslandDefaultUpgrades(island);

        island.setIslandSize(25);
        /*island.setIslandSize(25);
        island.setBlockLimit(Key.of(Material.HOPPER), 10);
        island.setBlockLimit(Key.of(Material.MINECART), 5);
        island.setBlockLimit(Key.of(Material.SPAWNER), 15);

        int defaultEntityMax = 20;
        for (EntityType entityType : EntityType.values()){
            island.setEntityLimit(Key.of(entityType), defaultEntityMax);
        }

        island.setTeamLimit(3);
        island.setCoopLimit(10);*/
    }

    public static int defaultMaxHopper = 10;
    public static int defaultMaxMinecart = 5;
    public static int defaultMaxSpawner = 15;

    public static int defaultMaxObserver = 150;
    public static int defaultMaxPiston = 250;
    public static int defaultMaxStickyPiston = 250;


    public static void SetIslandDefaultUpgrades(Island island){
        //island.setIslandSize(25);

        //island.setBlockLimit(Key.of(Material.HOPPER), defaultMaxHopper);
        //island.setBlockLimit(Key.of(Material.MINECART), defaultMaxMinecart);
        island.setBlockLimit(Key.of(Material.SPAWNER), defaultMaxSpawner);
        island.setBlockLimit(Key.of(Material.OBSERVER), defaultMaxObserver);
        island.setBlockLimit(Key.of(Material.PISTON), defaultMaxPiston);
        island.setBlockLimit(Key.of(Material.STICKY_PISTON), defaultMaxStickyPiston);

        int defaultEntityMax = 20;
        for (EntityType entityType : EntityType.values()){
            island.setEntityLimit(Key.of(entityType), defaultEntityMax);
        }

        island.setTeamLimit(3);
        island.setCoopLimit(10);

        consoleLog("&aGenerating default island upgrades for &2"+island.getOwner()+"'s &aisland");
    }

    public static void SetPlayerIslandLimit(SuperiorPlayer player){
        Island island = player.getIsland();
        if (island == null){
            return;
        }
        int defaultEntityMax = 20;
        for (EntityType entityType : EntityType.values()){
            island.setEntityLimit(Key.of(entityType), defaultEntityMax);
        }
    }

    public void ManageIslandDisband(SuperiorPlayer p){

        OfflinePlayer player = getOfflinePlayer(p.getUniqueId());
        List<Location> keysToRemove = new ArrayList<>();

        HashMap<String, String> storedPlacedMachines = new HashMap<>(placedMachines);

        for (String key : storedPlacedMachines.keySet()) {
            if (key.endsWith(__locationKey)) {
                String stringLocation = key.replace(__locationKey, "");
                Location location = parseLocationString(stringLocation);
                String owner = storedPlacedMachines.get(location+__ownerKey);
                if (owner.equals(player.getName())){
                    assert location != null;
                    keysToRemove.add(location);
                }
            }
        }

        for (Location loc : keysToRemove) {
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

    public boolean isDisabledMachineBlock(Block block){
        if (block != null){
            return block.getType() == Material.ENCHANTING_TABLE || block.getType() == Material.ANVIL
                    || block.getType() == Material.CHIPPED_ANVIL|| block.getType() == Material.DAMAGED_ANVIL
                    || block.getType() == Material.BREWING_STAND || block.getType() == Material.SMOKER;
        }
        return false;
    }

    @EventHandler
    public void ItemSwap(PlayerSwapHandItemsEvent event) {
        ItemStack item = event.getOffHandItem();

        if (item == null || item.getType().isAir()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(GetNamespacedKey(gameMenuKey))) {
            event.setCancelled(true);
        }

        if (isMachine(item)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void OnOpenStation(PlayerInteractEvent event){
        Player player = event.getPlayer();

        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK){
            ItemStack item = player.getInventory().getItemInMainHand();
            Block block = event.getClickedBlock();
            if (block != null){
                if (item.getType() != Material.AIR){
                    ItemMeta meta = item.getItemMeta();
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    if (block.getType() == Material.ITEM_FRAME){
                        if (container.has(GetNamespacedKey(gameMenuKey))){
                            event.setCancelled(true);
                        }
                    }
                }
                if (isDisabledBlock(block)){
                    event.setCancelled(true);

                    if (block.getType() == Material.ANVIL){
                        OpenMenu(player, MenuList.Anvil);
                    }
                }else{
                    if (placedMachines.get(block.getLocation()+__locationKey) != null){
                        if (isDisabledMachineBlock(block)){
                            event.setCancelled(true);
                        }
                    }
                }

                if (isAcidMaker(block)){
                    openedMenu.put(player, MenuList.AcidMaker);
                    Inventory inventory = OpenGUI(player, 3, "Acid Maker");

                    SetHeaderFooter(inventory);

                    inventory.setItem(13, getBasicUi("acidmaker_acid"));
                    player.openInventory(inventory);
                    player.updateInventory();

                    PlaySoundAt(Sound.ENTITY_SLIME_DEATH_SMALL, block.getLocation(),1,1);
                }
                else if (isCarbonForge(block)){
                    event.setCancelled(true);
                    OpenMenu(player, MenuList.Carbon_Forge);
                }
                else if (isArmorCrafter(block)){
                    event.setCancelled(true);
                    OpenArmorCrafter(player);
                }
                else if (isNetherSmelter(block)){
                    event.setCancelled(true);
                    OpenNetherSmelter(player);
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void OnSellAllWand(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        //player.sendMessage("sell wand event");
        Block block = event.getClickedBlock();

        if (block != null) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (block.getType() == Material.CHEST) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    assert item.getType() != Material.AIR;
                    if (!isSellWand(item)){
                        //player.sendMessage("its not wand");
                        return;
                    }
                    if (hasIslandAccess(player)){
                        if (!ItemNotBroken(item)){
                            event.setCancelled(true);
                            Notification_ItemBroken(player);
                            return;
                        }

                        if (item.getAmount() > 1){
                            event.setCancelled(true);
                            player.sendMessage(sendText("&4Unstack the item first!"));
                            return;
                        }

                        event.setCancelled(true);
                        Chest chest = (Chest) block.getState();
                        Inventory chestInventory = chest.getInventory();
                        PerformSellWand(player, chestInventory);

                        ManageDurability(player, "hand");
                        UpdateItem(player, "hand", item);

                        PlaySoundAt(Sound.BLOCK_NOTE_BLOCK_BELL, block.getLocation(), 1, 3);
                    }
                }
            }
        }
    }


    public static void PerformSellWand(Player player, Inventory inventory){
        int soldItem = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null){
                int itemAmount = item.getAmount();
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (container.has(GetNamespacedKey(worthKey))){
                    Double worth = container.get(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE);
                    String itemDisplay = item.getItemMeta().getDisplayName();
                    assert worth != null;
                    inventory.setItem(i, new ItemStack(Material.AIR));
                    double moneyGet = worth*itemAmount;

                    double moneyBooster = 1;
                    String boosterText = "";
                    if (boosters.get(player.getUniqueId()) != null){
                        if (boosters.get(player.getUniqueId()).name().contains("Sell_Bonus")){
                            moneyBooster = GetBoosterPercent(boosters.get(player.getUniqueId()));
                            boosterText = " &3(x"+FormatDouble(moneyBooster)+" SB)";
                        }
                    }

                    double multiplier = playerSellMultiplier.get(player.getUniqueId());

                    String multiplierText = "";
                    if (multiplier > 1){
                        multiplierText = "&e(x"+FormatDouble(multiplier)+" PM)";
                    }

                    ItemStack sellWand = player.getInventory().getItemInMainHand();
                    ItemMeta sellWandMeta = sellWand.getItemMeta();
                    PersistentDataContainer sellWandContainer = sellWandMeta.getPersistentDataContainer();
                    Double sellWandMultiplier = 1.0;
                    sellWandMultiplier = sellWandContainer.get(GetNamespacedKey(multiplierKey), PersistentDataType.DOUBLE);
                    String sellWandMultiplierText = "";
                    if (sellWandMultiplier != null && sellWandMultiplier > 1){
                        sellWandMultiplierText = " &d(x"+FormatDouble(sellWandMultiplier)+" SWM)";
                    }

                    double calculation = (((moneyGet*multiplier)*moneyBooster)*sellWandMultiplier)*events_sellMultiplier;

                    AddPlayerBalance(player, calculation);
                    player.sendMessage(sendText("&aSuccessfully sold &3x"+itemAmount+" "+itemDisplay+" &afor &2"+FormatDouble(calculation)+icon+" "+multiplierText+sellWandMultiplierText+boosterText));
                    soldItem += itemAmount;
                }
            }
        }
        if (soldItem > 0){
            player.sendMessage(sendText("&aAll available items inside chest has been sold! &6(x"+soldItem+" items sold)"));
        }else{
            player.sendMessage(sendText("&cYou don't have anything to sell in that chest!"));
        }
    }


    public static void SellAll(Player player){
        PlayerInventory inventory = player.getInventory();
        int soldItem = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null){
                int itemAmount = item.getAmount();
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (container.has(GetNamespacedKey(worthKey))){
                    Double worth = container.get(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE);
                    String itemDisplay = item.getItemMeta().getDisplayName();
                    assert worth != null;
                    player.getInventory().setItem(i, new ItemStack(Material.AIR));
                    double moneyGet = worth*itemAmount;

                    double moneyBooster = 1;
                    String boosterText = "";
                    if (boosters.get(player.getUniqueId()) != null){
                        if (boosters.get(player.getUniqueId()).name().contains("Sell_Bonus")){
                            moneyBooster = GetBoosterPercent(boosters.get(player.getUniqueId()));
                            boosterText = " &3(x"+FormatDouble(moneyBooster)+" SB)";
                        }
                    }

                    double multiplier = playerSellMultiplier.get(player.getUniqueId());
                    double calculation = ((moneyGet*multiplier)*moneyBooster)*events_sellMultiplier;

                    String multiplierText = "";
                    if (multiplier > 1){
                        multiplierText = "&e(x"+FormatDouble(multiplier)+" PM)";
                    }

                    AddPlayerBalance(player, calculation);
                    player.sendMessage(sendText("&aSuccessfully sold &3x"+itemAmount+" "+itemDisplay+" &afor &2"+FormatDouble(calculation)+icon+" "+multiplierText+boosterText));
                    soldItem += itemAmount;
                }
            }
        }
        if (soldItem > 0){
            //player.sendMessage(sendText(" "));
            player.sendMessage(sendText("&aAll available items inside your inventory has been sold! &6(x"+soldItem+" items sold)"));
        }else{
            player.sendMessage(sendText("&cYou don't have anything to sell in your inventory!"));
        }
    }

    public static HashMap<Player, Boolean> sellGui = new HashMap<>();

    public static void SellGui(Player player){
        Inventory openedInventory = player.getOpenInventory().getTopInventory();
        int soldItem = 0;

        for (ItemStack item : openedInventory.getContents()){
            if (item != null){
                int itemAmount = item.getAmount();
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (container.has(GetNamespacedKey(worthKey))){
                    Double worth = container.get(GetNamespacedKey(worthKey), PersistentDataType.DOUBLE);
                    String itemDisplay = item.getItemMeta().getDisplayName();
                    assert worth != null;
                    double moneyGet = worth*itemAmount;

                    double moneyBooster = 1;
                    String boosterText = "";
                    if (boosters.get(player.getUniqueId()) != null){
                        if (boosters.get(player.getUniqueId()).name().contains("Sell_Bonus")){
                            moneyBooster = GetBoosterPercent(boosters.get(player.getUniqueId()));
                            boosterText = " &3(x"+FormatDouble(moneyBooster)+" SB)";
                        }
                    }

                    double multiplier = playerSellMultiplier.get(player.getUniqueId());
                    double calculation = ((moneyGet*multiplier)*moneyBooster)*events_sellMultiplier;

                    String multiplierText = "";
                    if (multiplier > 1){
                        multiplierText = "&e(x"+FormatDouble(multiplier)+" PM)";
                    }

                    AddPlayerBalance(player, calculation);
                    player.sendMessage(sendText("&aSuccessfully sold &3x"+itemAmount+" "+itemDisplay+" &afor &2"+FormatDouble(calculation)+icon+" "+multiplierText+boosterText));
                    soldItem += itemAmount;
                }else{
                    if (meta.hasDisplayName()){
                        player.sendMessage(sendText("&c"+uncolouredText(meta.getDisplayName())+" &4can't be sold!"));
                    }
                    Map<Integer, ItemStack> addedItem = player.getInventory().addItem(item);
                    if (!addedItem.isEmpty()){
                        DropSingleItem(player.getLocation(), item);
                    }
                }
            }
        }
        if (soldItem > 0){
            //player.sendMessage(sendText(" "));
            player.sendMessage(sendText("&aAll available items inside gui has been sold! &6(x"+soldItem+" items sold)"));
        }else{
            player.sendMessage(sendText("&cYou don't have anything to sell in the gui!"));
        }
    }

    public static void OpenSellGui(Player player){
        sellGui.put(player, true);

        Inventory inventory = OpenChest(player, 6, "Sell Gui");
        player.openInventory(inventory);
    }


    @EventHandler
    public void OnInventoryCloseEvent(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        HandleCloseEvent(player);
    }

    @EventHandler
    public void ItemDropEvent(PlayerDropItemEvent event){
        Player player = (Player) event.getPlayer();
        if (isBackpack.get(player) != null) {
            if (isBackpack.get(player)) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void InteractEventForBackpack(PlayerInteractEvent event){
        Player player = (Player) event.getPlayer();
        if (isBackpack.get(player) != null) {
            if (isBackpack.get(player)) {
                event.setCancelled(true);
            }
        }
    }


    public static void HandleCloseEvent(Player player){
        if (sellGui.get(player) != null){
            if (sellGui.get(player)){
                sellGui.put(player, false);
                SellGui(player);
            }
        }

        if (isBackpack.get(player) != null){
            if (isBackpack.get(player)){
                isBackpack.put(player, false);
                SaveBackpackItem(player);
                player.sendMessage(sendText(" &8☐ &3Backpack Closed"));
                PlaySoundAt(Sound.ENTITY_HORSE_ARMOR, player.getLocation(), 1, 0);
            }
        }

        //Bukkit.getScheduler().cancelTask(backpackTask.get(player));
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
        if (isBackpack(item)){
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

            String serialCode = container.get(GetNamespacedKey(serialCodeKey), PersistentDataType.STRING);

            DeleteBackpack(serialCode);
        }
    }


    @EventHandler
    public void onItemBurn(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.ITEM) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.LAVA ||
                event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                ||
                event.getCause() == EntityDamageEvent.DamageCause.CONTACT) {

            Item item = (Item) event.getEntity();
            ItemStack stack = item.getItemStack();

            if (isBackpack(stack)){
                PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
                String serialCode = container.get(GetNamespacedKey(serialCodeKey), PersistentDataType.STRING);
                DeleteBackpack(serialCode);
            }
        }
    }


    public static HashMap<Player, Boolean> isBackpack = new HashMap<>();
    public static HashMap<Player, Integer> backpackItemSlot = new HashMap<>();

    public static HashMap<UUID, Inventory> backpacks = new HashMap<>();
    public static HashMap<Player, Integer> backpackTask = new HashMap<>();

    /*@EventHandler
    public void OnBackpackOpen(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();

                if (container.has(GetNamespacedKey(itemKey)) && container.has(GetNamespacedKey(backpackSizeKey))) {
                    if (isBackpack.get(player)){
                        return;
                    }
                    isBackpack.put(player, true);

                    PlayerInventory playerInventory = player.getInventory();

                    for (int i = 0; i < playerInventory.getSize(); i++) {
                        if (item.isSimilar(playerInventory.getItem(i))){
                            backpackItemSlot.put(player, i);
                        }
                    }

                    int backpackSize = container.get(GetNamespacedKey(backpackSizeKey), PersistentDataType.INTEGER);
                    String serialCode = container.get(GetNamespacedKey(serialCodeKey), PersistentDataType.STRING);

                    Inventory backpack = OpenBackpack(backpackSize, "Backpack");
                    backpacks.put(player.getUniqueId(), backpack);

                    String serializedKey = GetBackpackItem(serialCode);

                    if (serializedKey != null){
                        try {
                            List<ItemStack> storedItems = ItemSerializer.BukkitBase64ArrayToItem(serializedKey);

                            for (int i = 0; i < backpack.getSize(); i++) {
                                backpack.setItem(i, storedItems.get(i));
                            }
                        }catch (Exception e){
                            player.sendMessage(e.getMessage());
                        }
                    }

                    player.openInventory(backpacks.get(player.getUniqueId()));


                    player.sendMessage(sendText(" &8☐ &bOpening Backpack"));
                    PlaySoundAt(Sound.ENTITY_HORSE_ARMOR, player.getLocation(), 1, 3);
                }
            }
        }
    }*/



    public static void SaveBackpackItem(Player player) {
        Inventory openedBackpack = backpacks.get(player.getUniqueId());
        ItemStack item = player.getInventory().getItem(backpackItemSlot.get(player));
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();

            if (container.has(GetNamespacedKey(backpackSizeKey))) {
                int size = container.get(GetNamespacedKey(backpackSizeKey), PersistentDataType.INTEGER);
                String serialCode = container.get(GetNamespacedKey(serialCodeKey), PersistentDataType.STRING);

                List<ItemStack> backpackItems = Arrays.asList(openedBackpack.getContents());

                String serializedItem = ItemSerializer.BukkitItemsToBase64Array(backpackItems);

                SaveBackpack(serialCode, serializedItem);

                Map<String, Integer> itemCounts = new HashMap<>();
                Set<String> storedItems = new HashSet<>();

                for (ItemStack backpackItem : openedBackpack.getContents()) {
                    if (backpackItem != null && backpackItem.hasItemMeta()) {
                        ItemMeta backpackItemMeta = backpackItem.getItemMeta();
                        if (backpackItemMeta.hasDisplayName()) {
                            String displayName = uncolouredText(backpackItemMeta.getDisplayName());
                            int currentAmount = itemCounts.getOrDefault(displayName, 0);
                            itemCounts.put(displayName, currentAmount + backpackItem.getAmount());
                            storedItems.add(backpackItem.getItemMeta().getDisplayName());
                        }
                    }
                }

                //List<ItemStack> serializedItemList = ItemSerializer.BukkitBase64ArrayToItem(serializedItem);

                ItemStack originalBackpack = new ItemStack(CreateBackpack(size));
                ItemMeta originalBackpackMeta = originalBackpack.getItemMeta();

                meta.setDisplayName(sendText(originalBackpackMeta.getDisplayName()));

                List<String> itemListLore = originalBackpackMeta.getLore();

                if (!storedItems.isEmpty()){
                    itemListLore.add(sendText(" "));
                    itemListLore.add(sendText("&8Contents:"));
                }

                for (String backpackItem : storedItems){
                    itemListLore.add(sendText("&7- &9x"+itemCounts.get(uncolouredText(backpackItem))
                            +" "+backpackItem));
                }

                meta.setLore(itemListLore);
                item.setItemMeta(meta);

                /*player.sendMessage(sendText(" &8☐ &3Backpack Closed"));
                PlaySoundAt(Sound.ENTITY_HORSE_ARMOR, player.getLocation(), 1, 0);*/
            }
        }
    }



    @EventHandler
    public void OnBackpackItemClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();

        ItemStack item = event.getCurrentItem();

        if(item != null){
            if (item.getType() != Material.AIR){
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (container.has(GetNamespacedKey(backpackSizeKey))){
                    if (isBackpack.get(player)){
                        event.setCancelled(true);
                    }
                }
            }
        }



        if (event.getInventory().getHolder() instanceof BackpackHolder){
            if (isBackpack.getOrDefault(player, false)) {
                SaveBackpackItem(player);
                //player.sendMessage("Clicked backpack and save all items!");
            }
        }

    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getInventory().getHolder() instanceof BackpackHolder){
            if (isBackpack.getOrDefault(player, false)) {
                SaveBackpackItem(player);
                //player.sendMessage("Drag items in backpack and save all items!");
            }
        }
    }

    private static WorldGuardPlugin getWorldGuardPlugin() {
        WorldGuardPlugin worldGuardPlugin = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin != null && worldGuardPlugin.isEnabled()) {
            return worldGuardPlugin;
        } else {
            // WorldGuard plugin not found or not enabled
            return null;
        }
    }

    public static Set<String> getRegionNames(Player player) {
        Location location = player.getLocation();
        World world = location.getWorld();

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));

        if (regions == null) return Set.of(); // No regions in this world

        BlockVector3 pt = BukkitAdapter.asBlockVector(location);
        ApplicableRegionSet set = regions.getApplicableRegions(pt);

        return set.getRegions().stream()
                .map(ProtectedRegion::getId)
                .collect(Collectors.toSet());
    }

    public static Set<String> getRegionByLocation(Location location) {
        World world = location.getWorld();

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));

        if (regions == null) return Set.of(); // No regions in this world

        BlockVector3 pt = BukkitAdapter.asBlockVector(location);
        ApplicableRegionSet set = regions.getApplicableRegions(pt);

        return set.getRegions().stream()
                .map(ProtectedRegion::getId)
                .collect(Collectors.toSet());
    }

    public static void GetPlayerRegion(Player player){
        Set<String> regionNames = getRegionNames(player);
        for (String region : regionNames) {
            player.sendMessage("You're in region: " + region);
        }
    }

    public static boolean isPlayerInRegion(Player player, String regionName) {
        Location playerLocation = player.getLocation();
        WorldGuardPlugin worldGuard = getWorldGuardPlugin();

        if (worldGuard != null) {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
            if (regionManager != null) {
                ProtectedRegion region = regionManager.getRegion(regionName);
                if (region != null && region.contains(playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ())) {
                    // Player is inside the specified WorldGuard region
                    return true;
                }
            }
        }
        // Player is not inside the specified WorldGuard region, or WorldGuard is not found/enabled
        return false;
    }

    public boolean isEntityInRegion(Entity entity, String regionName) {
        Location entityLocation = entity.getLocation(); // Get the entity's location
        WorldGuardPlugin worldGuard = getWorldGuardPlugin();

        if (worldGuard != null) {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(entity.getWorld()));
            if (regionManager != null) {
                ProtectedRegion region = regionManager.getRegion(regionName);
                if (region != null && region.contains(entityLocation.getBlockX(), entityLocation.getBlockY(), entityLocation.getBlockZ())) {
                    // Entity is inside the specified WorldGuard region
                    return true;
                }
            }
        }
        // Entity is not inside the specified WorldGuard region, or WorldGuard is not found/enabled
        return false;
    }

    public static boolean isBlockInRegion(Block block, String regionName) {
        Location blockLocation = block.getLocation();
        WorldGuardPlugin worldGuard = getWorldGuardPlugin();

        if (worldGuard != null) {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(block.getWorld()));
            if (regionManager != null) {
                ProtectedRegion region = regionManager.getRegion(regionName);
                if (region != null && region.contains(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ())) {
                    // Block is inside the specified WorldGuard region
                    //System.out.println("The Block you mine is on Region " + regionName);
                    return true;
                }
            }
        }
        // Block is not inside the specified WorldGuard region, or WorldGuard is not found/enabled
        return false;
    }

    // Mob Spawning

    @EventHandler
    public void OnMobSpawn(EntitySpawnEvent event){
        Entity entity = event.getEntity();
        Location location = entity.getLocation();



        if (entity.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM ||
                entity.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING ||
                entity.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.BUILD_WITHER){

                event.setCancelled(true);
        }

        else if (entity.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER ||
                entity.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL
                || entity.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
        ){

            int level = 1;

            if (entity.getWorld().getName().equals("SuperiorWorld")){

                if (entity instanceof Phantom){
                    entity.remove();
                    return;
                }

                Island island = SuperiorSkyblockAPI.getIslandAt(entity.getLocation());
                SuperiorPlayer owner = island.getOwner();
                OfflinePlayer player = Bukkit.getOfflinePlayer(owner.getUniqueId());
                if (playerLevel.get(player.getUniqueId()) != null){
                    level = playerLevel.get(player.getUniqueId());
                }
            }

            if (level > 10){
                level = 10;
            }

            if (entity.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG){
                entity.addScoreboardTag("EggMob");
            }else{
                entity.addScoreboardTag("VanillaMob");
            }
            entity.setCustomNameVisible(true);

            Bukkit.getScheduler().runTaskLater(getMainPlugin(), () -> {
                if (entity instanceof Mob) {
                    ((Mob) entity).setAI(true);
                    if (entity instanceof Monster){
                        ((Mob) entity).setAggressive(true);
                    }
                }
            }, 1L);

            ((Mob) entity).setMaxHealth(level*5);
            ((Mob) entity).heal(((Mob) entity).getMaxHealth());

            double entityMaxHealth = ((Mob) entity).getMaxHealth();

            entity.setCustomName(sendText("&8[&9Lv."+level+"&8] &f"+formatItemName(entity.getType().toString())+" &8- &a"+entityMaxHealth+"&8/&a"+entityMaxHealth+"❤"));

            //consoleLog("mob level : "+GetMobLevel(entity));
            PlaySoundAt(Sound.ENTITY_PLAYER_LEVELUP, location, 1, 3);

            if (entity instanceof Animals){
                ((Animals) entity).setAdult();
                ((Animals) entity).setAgeLock(true);
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (entity.isValid()){
                    if (entity.getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG){
                        entity.remove();
                        PlaySoundAt(Sound.ENTITY_ITEM_PICKUP, location, 1, 3);
                    }
                }
            }, 1200L);
        }


        if (entity instanceof ExperienceOrb){
            event.setCancelled(true);
        }

    }

    public void ManageMobHealthBar(Entity entity){
        Bukkit.getScheduler().runTaskLater(getMainPlugin(), () -> {
            if (entity.isValid()){
                String[] name = entity.getCustomName().split(" ");
                String replacer = "";
                String displayname = entity.getCustomName();
                String barColor = "&a";
                double healthAmount = ((Mob) entity).getHealth();
                double formattedHealth = Math.round(healthAmount * 10.0) / 10.0;
                double maxHealthAmount = 1;
                double formattedMaxHealth = Math.round(maxHealthAmount * 10.0) / 10.0;
                boolean containsHealth = false;
                for (String s : name){
                    if (s.contains("❤")){
                        replacer = s;
                        displayname = entity.getCustomName().replaceAll(replacer, "").trim();
                        healthAmount = ((LivingEntity) entity).getHealth();
                        formattedHealth = Math.round(healthAmount * 10.0) / 10.0;
                        maxHealthAmount = ((LivingEntity) entity).getMaxHealth();
                        formattedMaxHealth = Math.round(maxHealthAmount * 10.0) / 10.0;

                        double healthPercentage = healthAmount/maxHealthAmount * 100;

                        if (healthPercentage < 100){
                            barColor = "&a";
                        }
                        if (healthPercentage < 70){
                            barColor = "&e";
                        }
                        if (healthPercentage < 30){
                            barColor = "&c";
                        }
                        if (healthPercentage < 20){
                            barColor = "&4";
                        }
                        containsHealth = true;
                        break;
                    }
                }

                if (!containsHealth){
                    entity.setCustomName(sendText(displayname+" "+barColor+FormatDouble(healthAmount)+"&8/&a"+FormatDouble(maxHealthAmount)+"❤"));
                }

                else{
                    entity.setCustomName(sendText(displayname+" "+barColor+FormatDouble(healthAmount)+"&8/&a"+FormatDouble(maxHealthAmount)+"❤"));
                }
            }
        }, 10L);
    }

    public static Integer ClearMobs(World world){
        int amount = 0;
        for (Entity entity : world.getEntities()){
            if (entity instanceof Mob){
                if (entity.getScoreboardTags().contains("VanillaMob") || entity.getScoreboardTags().contains("DungeonMob")){
                    entity.remove();
                    amount++;
                }
            }
        }

        return amount;
    }

    public static int GetMobLevel(Entity entity) {

        if (entity instanceof LivingEntity){
            if (entity instanceof Mob){
                if (entity.getCustomName() != null){

                    String[] nameSplit = entity.getCustomName().split(" ");

                    int level = 1;

                    for (String name : nameSplit){
                        if (uncolouredText(name).contains("Lv")){
                            level = Integer.parseInt(numberInText(name));
                            break;
                        }
                    }
                    return level;
                }
            }
        }

        return 1;
    }

    @EventHandler
    public void OnEntityDeath(EntityDeathEvent event){
        Entity entity = event.getEntity();

        if (!(entity instanceof Mob)){
            return;
        }

        event.setDroppedExp(0);
        event.getDrops().clear();

        if (entity.getScoreboardTags().contains("DungeonMob")){
            return;
        }

        Random random = new Random();
        int randomDropAmount = 1;
        int randomDropAmount2 = 1;

        ItemStack item = new ItemStack(Material.AIR);
        ItemStack item2 = new ItemStack(Material.AIR);

        if (entity instanceof Pig){
            randomDropAmount = random.nextInt(2)+1;
            item = new ItemStack(Material.PORKCHOP);
        }
        else if (entity instanceof Chicken){
            randomDropAmount = random.nextInt(2)+1;
            item = new ItemStack(Material.CHICKEN);
            item2= new ItemStack(Material.FEATHER);
        }
        else if (entity instanceof Sheep){
            randomDropAmount = random.nextInt(2)+1;
            item = new ItemStack(Material.MUTTON);
            item2= new ItemStack(Material.WHITE_WOOL);
        }
        else if (entity instanceof Cow){
            randomDropAmount = random.nextInt(2)+1;
            item = new ItemStack(Material.BEEF);
            item2= new ItemStack(Material.LEATHER);
        }
        else if (entity instanceof Rabbit){
            randomDropAmount = random.nextInt(2)+1;
            item = new ItemStack(Material.RABBIT);
            item2= new ItemStack(Material.RABBIT_HIDE);
        }
        else if (entity instanceof Zombie){
            randomDropAmount = random.nextInt(2)+1;
            item = new ItemStack(Material.ROTTEN_FLESH);
        }
        else if (entity instanceof Skeleton){
            randomDropAmount = random.nextInt(2)+1;
            item = new ItemStack(Material.BONE);
        }
        else if (entity instanceof Spider){
            randomDropAmount = random.nextInt(2)+1;
            item = new ItemStack(Material.STRING);
            item2= new ItemStack(Material.SPIDER_EYE);
        }
        else if (entity instanceof Creeper){
            randomDropAmount = random.nextInt(2)+1;
            item = new ItemStack(Material.GUNPOWDER);
        }
        else if (entity instanceof Enderman){
            randomDropAmount = random.nextInt(2)+1;
            item = new ItemStack(Material.ENDER_PEARL);
            item2= new ItemStack(Material.ENDER_EYE);
        }
        else if (entity instanceof Blaze){
            randomDropAmount = random.nextInt(2)+1;
            item = new ItemStack(Material.BLAZE_ROD);
            //item2= new ItemStack(Material.ENDER_EYE);
        }
        else if (entity instanceof Guardian){
            randomDropAmount = random.nextInt(2)+1;
            item = new ItemStack(Material.PRISMARINE_SHARD);
            item2= new ItemStack(Material.PRISMARINE_CRYSTALS);
        }
        Location location = entity.getLocation();

        int randomSecondDrop = random.nextInt(100)+1;

        DropItem(location, item, randomDropAmount);

        if (randomSecondDrop <= 50){
            DropItem(location, item2, randomDropAmount2);
        }
    }

    public static HashMap<String, Entity> entityList = new HashMap<>();

    public static void InitMobs(){

    }

    @EventHandler
    public void OnCombust(EntityCombustEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void SilkSpawnerBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SURVIVAL){
            if (event.getBlock().getType() == Material.SPAWNER){
                //event.setCancelled(true);
                if (!hasIslandAccess(player)){
                    if (!player.hasPermission("admin")){
                        event.setCancelled(true);
                        player.sendMessage(sendText("&4You don't have access to this island!"));
                    }
                    return;
                }

                ItemStack item = player.getInventory().getItemInMainHand();
                ItemMeta meta = item.getItemMeta();

                if (item.getType() != Material.AIR){
                    if (isJobTool(item)){
                        if (!ItemNotBroken(item)){
                            event.setCancelled(true);
                            return;
                        }
                    }
                }

                Block block = event.getBlock();
                //event.setCancelled(true);

                CreatureSpawner blockSpawner = (CreatureSpawner) block.getState();

                ItemStack getItem = new ItemStack(CreateSpawner(blockSpawner.getSpawnedType()));

                //block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(block.getLocation(), getItem);
                PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, block.getLocation(), 1, 3);
            }
        }
    }

    /*@EventHandler
    public void SilkSpawner(PlayerInteractEvent event){
        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();


        if (item.getType() != Material.AIR){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking()){
                ItemMeta meta = item.getItemMeta();
                Block block = event.getClickedBlock();
                if (block.getType() == Material.SPAWNER){
                    event.setCancelled(true);
                    if (!hasIslandAccess(player)){
                        if (!player.hasPermission("admin")){
                            player.sendMessage(sendText("&4You don't have access to remove this spawner"));
                            return;
                        }else{
                            consoleLog(sendText("&e"+player.getName()+" &6doesn't has access to the island's spawner but this player is an admin!"));
                        }
                    }

                    if (meta.hasEnchant(Enchantment.SILK_TOUCH)){
                        CreatureSpawner blockSpawner = (CreatureSpawner) block.getState();

                        ItemStack getItem = new ItemStack(CreateSpawner(blockSpawner.getSpawnedType()));

                        Map<Integer, ItemStack> addedItem = player.getInventory().addItem(getItem.clone());

                        if (addedItem.isEmpty()){
                            event.getClickedBlock().setType(Material.AIR);
                            PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, event.getClickedBlock().getLocation(), 1, 3);

                            block.breakNaturally();

                        }else{
                            event.setCancelled(true);
                            player.sendMessage(sendText("&4Your inventory is full!"));
                        }

                    }else{
                        event.setCancelled(true);
                        player.sendMessage(sendText("&4You must Use silktouch to break the spawner!"));
                    }
                }
            }
        }
    }*/

    @EventHandler
    public void OnSpawnerPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        if (item.getType() == Material.SPAWNER) {
            Block block = event.getBlockPlaced();
            if (block.getType() == Material.SPAWNER) {
                BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
                if (meta != null) {
                    CreatureSpawner itemSpawner = (CreatureSpawner) meta.getBlockState();
                    EntityType type = itemSpawner.getSpawnedType();

                    CreatureSpawner blockSpawner = (CreatureSpawner) block.getState();
                    blockSpawner.setSpawnedType(type);
                    blockSpawner.update();
                }
            }
        }
    }

    public static boolean hasIslandAccess(Player player) {
        // Get the island at the given location
        Island island = SuperiorSkyblockAPI.getGrid().getIslandAt(player.getLocation());

        // Get the SuperiorPlayer instance for the given player
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);

        // If the island is null, the block isn't part of any island
        if (island == null) {
            return false;
        }

        // Check if the player is a member or coop member of the island
        return island.isMember(superiorPlayer) || island.isCoop(superiorPlayer);
    }

    public static void PushEntityForwards(Entity entity, double power) {
        Location location = entity.getLocation();
        Vector direction = location.getDirection().normalize();
        Vector forward = direction.multiply(power);
        entity.setVelocity(forward);
    }

    public static void PushEntityBackwards(Entity entity, double power) {
        Location location = entity.getLocation();
        Vector direction = location.getDirection().normalize();
        Vector backward = direction.multiply(-power);
        entity.setVelocity(backward);
    }

    public static void PushEntityUpwards(Entity entity, double power) {
        Vector upward = new Vector(0, power, 0);
        entity.setVelocity(upward);
    }


    @EventHandler
    public void OnHookReelIn(PlayerFishEvent event){
        if (event.getState() == PlayerFishEvent.State.IN_GROUND){
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() != Material.AIR){
                if (isHook(item)){

                    if (hasCooldown(player, CooldownManager.CooldownType.Hook)){
                        player.sendTitle(sendText("&4&l<!>"), sendText("&cHook is on cooldown!"));
                        player.sendMessage(Notification_HasCooldown(player, CooldownManager.CooldownType.Hook));
                        return;
                    }

                    ItemMeta meta = item.getItemMeta();
                    PersistentDataContainer container = meta.getPersistentDataContainer();

                    if (playerLevel.get(player.getUniqueId()) < container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER)){
                        player.sendMessage(sendText(Notification_NoLevel(player)));
                        return;
                    }

                    Double power = container.get(GetNamespacedKey(toolPowerKey), PersistentDataType.DOUBLE);
                    Double speed = container.get(GetNamespacedKey(toolSpeedKey), PersistentDataType.DOUBLE);

                    Double steamConsumption = container.get(GetNamespacedKey(steamConsumptionKey), PersistentDataType.DOUBLE);

                    if (!hasSteam(player, steamConsumption)){
                        player.sendMessage(sendText(Notification_NoSteam(player)));
                        return;
                    }

                    if (!ItemNotBroken(item)){
                        Notification_ItemBroken(player);
                        return;
                    }

                    Double delayContainer = container.get(GetNamespacedKey(toolSpeedKey), PersistentDataType.DOUBLE);
                    double delay = 9;
                    if (delayContainer != null){
                        delay = 9-delayContainer;
                    }

                    if (delay <= 0){
                        delay = 1.0;
                    }

                    SetCooldown(player, CooldownManager.CooldownType.Hook, (int) delay);
                    RemoveSteam(player, steamConsumption);

                    PushEntityUpwards(player, power*0.12);

                    Bukkit.getScheduler().runTaskLater(getMainPlugin(), () -> {
                        PushEntityForwards(player, (speed*0.12)*2);
                        PlaySoundAt(Sound.BLOCK_PISTON_CONTRACT, player.getLocation(), 1, 1);

                        PotionEffect effect = new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 3, false, false);
                        player.addPotionEffect(effect);
                    }, 2L);

                    ManageDurability(player, "hand");
                    UpdateItem(player, "hand", item);

                }
            }
        }
    }

    public static int globalRevision = 3;

    public static void UpdatePlayerItem(Player player) {
        checkRevision(player, "hand", player.getInventory().getItemInMainHand());
        checkRevision(player, "offhand", player.getInventory().getItemInOffHand());
        checkRevision(player, "helmet", player.getInventory().getHelmet());
        checkRevision(player, "chestplate", player.getInventory().getChestplate());
        checkRevision(player, "leggings", player.getInventory().getLeggings());
        checkRevision(player, "boots", player.getInventory().getBoots());

        updateRevision(player, "hand", player.getInventory().getItemInMainHand());
        updateRevision(player, "offhand", player.getInventory().getItemInOffHand());
        updateRevision(player, "helmet", player.getInventory().getHelmet());
        updateRevision(player, "chestplate", player.getInventory().getChestplate());
        updateRevision(player, "leggings", player.getInventory().getLeggings());
        updateRevision(player, "boots", player.getInventory().getBoots());
    }

    private static void checkRevision(Player player, String slot, ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !isFactoryItem(item)) return;

        if (!isWeapon(item) && !isJobTool(item) && !isArmor(item)){
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (!container.has(GetNamespacedKey(revisionCodeKey))) {
            container.set(GetNamespacedKey(revisionCodeKey), PersistentDataType.INTEGER, 0);
            item.setItemMeta(meta);
        }
    }

    private static void updateRevision(Player player, String slot, ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !isFactoryItem(item)) return;

        if (!isWeapon(item) && !isJobTool(item) && !isArmor(item)){
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(GetNamespacedKey(revisionCodeKey))) {
            if (container.get(GetNamespacedKey(revisionCodeKey), PersistentDataType.INTEGER) < globalRevision
            || container.get(GetNamespacedKey(revisionCodeKey), PersistentDataType.INTEGER) > globalRevision){

                if (container.get(GetNamespacedKey(baseKey+criticalDamageKey), PersistentDataType.DOUBLE) != null
                && container.get(GetNamespacedKey(baseKey+criticalDamageKey), PersistentDataType.DOUBLE) > 90 &&
                        container.get(GetNamespacedKey(baseKey+criticalDamageKey), PersistentDataType.DOUBLE) < 100){
                    container.set(GetNamespacedKey(baseKey+criticalDamageKey), PersistentDataType.DOUBLE, 0.0);
                    item.setItemMeta(meta);
                    //player.sendMessage("update: "+container.get(GetNamespacedKey(baseKey+criticalDamageKey), PersistentDataType.DOUBLE));
                }
                UpdateItem(player, slot, item);

                meta = item.getItemMeta();
                container = meta.getPersistentDataContainer();
                container.set(GetNamespacedKey(revisionCodeKey), PersistentDataType.INTEGER, globalRevision);
                item.setItemMeta(meta);

                //player.sendMessage("after update: "+container.get(GetNamespacedKey(baseKey+criticalDamageKey), PersistentDataType.DOUBLE));

                player.sendMessage(sendText("&aUpdated &2"+slot+" &asuccessfully!"));
            }
        }
    }



}
