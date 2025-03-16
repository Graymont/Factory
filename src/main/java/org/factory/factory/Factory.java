package org.factory.factory;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.factory.factory.Utils.GUIManager;
import org.factory.factory.Utils.ItemSerializer;
import org.factory.factory.Utils.SQLiteDatabase;
import org.factory.factory.Utils.VaultEconomy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.factory.factory.Database.SaveAllData;
import static org.factory.factory.Events.*;
import static org.factory.factory.Utils.CraftingManager.InitRecipes;
import static org.factory.factory.Utils.CraftingManager.InitSmeltings;
import static org.factory.factory.Utils.FactoryItem.InitFactoryItems;
import static org.factory.factory.Utils.FactoryMachine.*;
import static org.factory.factory.Utils.SQLiteDatabase.parseLocationString;
import static org.factory.factory.Utils.UserInterface.*;
import static org.factory.factory.Utils.VaultEconomy.setupEconomy;

public final class Factory extends JavaPlugin {

    public SQLiteDatabase sqLiteDatabase;
    public Events events = new Events(this);
    public FurnaceManager furnaceManager = new FurnaceManager(this, events);
    public Commands commands = new Commands(events, this);

    public GUIManager guiManager = new GUIManager();

    public static Factory getMainPlugin() {
        return Factory.getPlugin(Factory.class);
    }

    @Override
    public void onEnable() {
        sqLiteDatabase = new SQLiteDatabase();
        sqLiteDatabase.connect();

        if (!sqLiteDatabase.isConnected()) {
            getLogger().warning("SQLite Database failed to connect, disabling Factory");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        RegisterAllEvents();
        RegisterAllCommands();
        setupEconomy();

        Database.LoadAllData();
        events.placedMachines = sqLiteDatabase.LoadMachineData(sqLiteDatabase.connection);
        events.machineItems = sqLiteDatabase.LoadMachineItems(sqLiteDatabase.connection);
        events.storedMachines = sqLiteDatabase.LoadStoredMachines(sqLiteDatabase.connection);

        EverySeconds();
        GenerateMachineTags();

        InitFactoryItems();
        InitRecipes();
        InitSmeltings();
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        SaveAllData();
        sqLiteDatabase.SaveMachineData(events.placedMachines);
        sqLiteDatabase.SaveMachineItems(events.machineItems);
        sqLiteDatabase.SaveStoredMachines(events.storedMachines);
        sqLiteDatabase.disconnect();

        ClearMachineTags();
    }

    void RegisterAllCommands(){
        // Commands
        Objects.requireNonNull(getCommand("factory")).setExecutor(commands);
        Objects.requireNonNull(getCommand("refundmachine")).setExecutor(commands);
        Objects.requireNonNull(getCommand("shop")).setExecutor(commands);
        Objects.requireNonNull(getCommand("spawn")).setExecutor(commands);

        // Tab Completer
        Objects.requireNonNull(getCommand("factory")).setTabCompleter(commands);
    }
    void RegisterAllEvents(){
        getServer().getPluginManager().registerEvents(events, this);
        getServer().getPluginManager().registerEvents(furnaceManager, this);
        getServer().getPluginManager().registerEvents(guiManager, this);
    }

    void EverySeconds(){
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    //MachineLog(player);

                    events.InventoryItemCheck(player);
                    ActionBar(player);
                    RegenSteam(player);
                    ManageCooldown(player);
                    events.PlayerInventoryItems(player);
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    void MachineLog(Player player){
        for (String key : events.placedMachines.keySet()) {
            if (key.endsWith(__ownerKey)) {
                String owner = events.placedMachines.get(key);
                if (owner.equals(player.getName())) {
                    player.sendMessage(sendText("&aMachine at &b"
                            + key.replace(__ownerKey, "") + " &ais owned by you!"));
                }
            }
        }
    }

    void ClearMachineTags(){
        for (String key : events.placedMachines.keySet()){
            if (key.endsWith(__locationKey)){
                String loc = key.replace(__locationKey, "");
                Location location = parseLocationString(loc);
                int taskId = Integer.parseInt(events.placedMachines.get(location+__taskIdKey));
                assert location != null;
                World world = location.getWorld();
                for (Entity entity : Bukkit.getWorld(world.getName()).getEntities()){
                    if (entity instanceof TextDisplay){
                        if (entity.hasMetadata("MachineTag."+location)){
                            entity.remove();
                            //consoleLog(sendText("&aCleared tags with taskId tag: &6"+taskId));
                        }
                    }
                }
            }
        }

        consoleLog(sendText("&aCleared all &bMachine's Holograms"));
    }

    public void GenerateMachineTags() {
        Set<String> keys = new HashSet<>(events.placedMachines.keySet());

        for (String key : keys) {
            if (key.endsWith(__locationKey)) {
                String baseKey = key.replace(__locationKey, "");
                Location location = parseLocationString(baseKey);

                if (location == null) {
                    consoleLog(sendText("&cInvalid location key - " + baseKey));
                    continue;
                }

                String ownerKey = baseKey + __ownerKey;
                String machineNameKey = baseKey + __machineNameKey;
                String statusKey = baseKey + __machineStatusKey;
                String taskIdKey = baseKey + __taskIdKey;

                if (events.placedMachines.containsKey(ownerKey) && events.placedMachines.containsKey(machineNameKey) && events.placedMachines.containsKey(taskIdKey)) {
                    String owner = events.placedMachines.get(ownerKey);
                    String machineName = events.placedMachines.get(machineNameKey);

                    if (!events.placedMachines.get(location+__machineStatusKey).equals("Disabled")){
                        events.placedMachines.put(statusKey, "Inactive");
                    }

                    try {
                        int taskId = Integer.parseInt(events.placedMachines.get(taskIdKey));
                        events.SpawnMachineTag(owner, location, machineName, taskId);
                    } catch (NumberFormatException e) {
                        consoleLog(sendText("&cInvalid taskId format for " + baseKey));
                    }
                } else {
                    consoleLog(sendText("&cMissing machine data for " + baseKey));
                }
            }
        }

        consoleLog(sendText("&aGenerated all &bMachine's Holograms"));
    }


    public void RegenSteam(Player player){
        double steam = playerSteam.get(player);
        double maxSteam = playerMaxSteam.get(player);

        if (steam < maxSteam){
            steam ++;
            playerSteam.put(player, steam);
        }

        else if (steam > maxSteam){
            playerSteam.put(player, maxSteam);
        }

        else if (steam < 0){
            playerSteam.put(player, 0.0);
        }
    }

    public void ActionBar(Player player) {
        double steam = playerSteam.get(player);
        double maxSteam = playerMaxSteam.get(player);
        double armor = playerArmor.get(player);
        double health = player.getHealth();
        double maxHealth = player.getMaxHealth();
        SendActionBar(player, sendText("&4❤ &c"+FormatDouble(health)+"/"+FormatDouble(maxHealth)+" Health          "+"&7[&8\uD83D\uDD30&f"+FormatDouble(armor)+" &7Armor&7]"+"          &6\uD83C\uDF0A &e"+FormatDouble(steam)+"/"+FormatDouble(maxSteam)+" Steam"));
    }

    public void ManageCooldown(Player player){
        for (String cd : events.cooldownList){
            int cooldown = events.playerCooldown.get(player.getName()+".cooldown."+cd);
            if (cooldown > 0){
                cooldown--;
                events.playerCooldown.put(player.getName()+".cooldown."+cd, cooldown);
            }
        }
    }

}
