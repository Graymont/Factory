package org.factory.factory;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.factory.factory.Utils.SQLiteDatabase.parseLocationString;
import static org.factory.factory.Utils.UserInterface.*;

public final class Factory extends JavaPlugin {

    public SQLiteDatabase sqLiteDatabase;
    public Events events = new Events(this);
    public FurnaceManager furnaceManager = new FurnaceManager(this, events);
    public Commands commands = new Commands(events, this);
    public Database database = new Database(events, this);

    public GUIManager guiManager = new GUIManager();

    @Override
    public void onEnable() {
        sqLiteDatabase = new SQLiteDatabase();
        sqLiteDatabase.connect();

        if (!sqLiteDatabase.isConnected()) {
            getLogger().warning("Failed to connect to SQLite database. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        RegisterAllEvents();
        RegisterAllCommands();

        database.LoadAllData();
        events.placedMachines = sqLiteDatabase.LoadMachineData(sqLiteDatabase.connection);
        events.machineItems = sqLiteDatabase.LoadMachineItems(sqLiteDatabase.connection);

        EverySeconds();
        GenerateMachineTags();
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        database.SaveAllData();
        sqLiteDatabase.SaveMachineData(events.placedMachines);
        sqLiteDatabase.SaveMachineItems(events.machineItems);
        sqLiteDatabase.disconnect();

        ClearMachineTags();
    }

    void RegisterAllCommands(){
        // Commands
        Objects.requireNonNull(getCommand("factory")).setExecutor(commands);

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
            if (key.endsWith(".owner")) {
                String owner = events.placedMachines.get(key);
                if (owner.equals(player.getName())) {
                    player.sendMessage(sendText("&aMachine at &b"
                            + key.replace(".owner", "") + " &ais owned by you!"));
                }
            }
        }
    }

    void ClearMachineTags(){
        for (String key : events.placedMachines.keySet()){
            if (key.endsWith(".location")){
                String loc = key.replace(".location", "");
                Location location = parseLocationString(loc);
                int taskId = Integer.parseInt(events.placedMachines.get(location+".taskId"));
                assert location != null;
                World world = location.getWorld();
                for (Entity entity : Bukkit.getWorld(world.getName()).getEntities()){
                    if (entity instanceof ArmorStand){
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
            if (key.endsWith(".location")) {
                String baseKey = key.replace(".location", "");
                Location location = parseLocationString(baseKey);

                if (location == null) {
                    consoleLog(sendText("&cError: Invalid location key - " + baseKey));
                    continue;
                }

                String ownerKey = baseKey + ".owner";
                String machineNameKey = baseKey + ".machineName";
                String statusKey = baseKey + ".status";
                String taskIdKey = baseKey + ".taskId";

                if (events.placedMachines.containsKey(ownerKey) && events.placedMachines.containsKey(machineNameKey) && events.placedMachines.containsKey(taskIdKey)) {
                    String owner = events.placedMachines.get(ownerKey);
                    String machineName = events.placedMachines.get(machineNameKey);

                    events.placedMachines.put(statusKey, "Inactive");

                    try {
                        int taskId = Integer.parseInt(events.placedMachines.get(taskIdKey));
                        events.SpawnMachineTag(owner, location, machineName, taskId);
                    } catch (NumberFormatException e) {
                        consoleLog(sendText("&cError: Invalid taskId format for " + baseKey));
                    }
                } else {
                    consoleLog(sendText("&cError: Missing machine data for " + baseKey));
                }
            }
        }

        consoleLog(sendText("&aGenerated all &bMachine's Holograms"));
    }


    public void RegenSteam(Player player){
        double steam = events.playerSteam.get(player);
        double maxSteam = events.playerMaxSteam.get(player);

        if (steam < maxSteam){
            steam ++;
            events.playerSteam.put(player, steam);
        }

        else if (steam > maxSteam){
            events.playerSteam.put(player, maxSteam);
        }

        else if (steam < 0){
            events.playerSteam.put(player, 0.0);
        }
    }

    public void ActionBar(Player player) {
        double steam = events.playerSteam.get(player);
        double maxSteam = events.playerMaxSteam.get(player);
        SendActionBar(player, sendText("&6⚡ &e"+FormatDouble(steam)+"/"+FormatDouble(maxSteam)+" Steam"));
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
