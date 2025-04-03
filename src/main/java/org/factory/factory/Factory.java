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
import org.factory.factory.Utils.*;

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
import static org.factory.factory.Utils.GUIManager.GameMenu;
import static org.factory.factory.Utils.PlayerProgress.ManageProgress;
import static org.factory.factory.Utils.PlayerProgressManager.TriggerFishing;
import static org.factory.factory.Utils.SQLiteDatabase.*;
import static org.factory.factory.Utils.UserInterface.*;
import static org.factory.factory.Utils.VaultEconomy.setupEconomy;

public final class Factory extends JavaPlugin {

    public SQLiteDatabase sqLiteDatabase = new SQLiteDatabase();
    public Events events = new Events(this);
    public FurnaceManager furnaceManager = new FurnaceManager(this, events);
    public Commands commands = new Commands(events, this);

    public GUIManager guiManager = new GUIManager();
    public RewardsManager rewardsManager = new RewardsManager();

    public QuestManager questManager = new QuestManager();

    public PlayerProgressManager playerProgressManager = new PlayerProgressManager();

    public static Factory getMainPlugin() {
        return Factory.getPlugin(Factory.class);
    }

    @Override
    public void onEnable() {

        sqLiteDatabase.connect();

        if (!SQLiteDatabase.isConnected()) {
            getLogger().warning("SQLite Database failed to connect, disabling Factory");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        RegisterAllEvents();
        RegisterAllCommands();
        setupEconomy();
        Database.LoadAllData();
        InitFactoryItems();
        InitRecipes();
        InitSmeltings();

        placedMachines = SQLiteDatabase.LoadMachineData(connection);
        machineItems = SQLiteDatabase.LoadMachineItems(connection);
        storedMachines = SQLiteDatabase.LoadStoredMachines(connection);



        Every10Tick();
        EverySeconds();
        EveryMinutes();
        Every5Minutes();
        //GenerateMachineTags();


        PlayerProgress.init();

        InitMobs();

        //registerPacketListener(this);
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic

        SaveAllData();
        /*SQLiteDatabase.SaveMachineData(placedMachines);
        SQLiteDatabase.SaveMachineItems(machineItems);
        SQLiteDatabase.SaveStoredMachines(events.storedMachines);*/
        SaveAllProgress();
        SQLiteDatabase.disconnect();

        //ClearMachineTags();
    }

    void RegisterAllCommands(){
        // Commands
        Objects.requireNonNull(getCommand("factoryutils")).setExecutor(commands);
        Objects.requireNonNull(getCommand("refundmachine")).setExecutor(commands);
        Objects.requireNonNull(getCommand("shop")).setExecutor(commands);
        Objects.requireNonNull(getCommand("spawn")).setExecutor(commands);
        Objects.requireNonNull(getCommand("sellall")).setExecutor(commands);
        Objects.requireNonNull(getCommand("sellgui")).setExecutor(commands);
        Objects.requireNonNull(getCommand("catalog")).setExecutor(commands);
        Objects.requireNonNull(getCommand("multiblock")).setExecutor(commands);
        Objects.requireNonNull(getCommand("warp")).setExecutor(commands);
        Objects.requireNonNull(getCommand("profile")).setExecutor(commands);
        Objects.requireNonNull(getCommand("hazmat")).setExecutor(commands);
        Objects.requireNonNull(getCommand("rewards")).setExecutor(commands);
        Objects.requireNonNull(getCommand("abandonquest")).setExecutor(commands);
        Objects.requireNonNull(getCommand("quest")).setExecutor(commands);

        // Tab Completer
        Objects.requireNonNull(getCommand("factoryutils")).setTabCompleter(commands);
    }
    void RegisterAllEvents(){
        getServer().getPluginManager().registerEvents(events, this);
        getServer().getPluginManager().registerEvents(furnaceManager, this);
        getServer().getPluginManager().registerEvents(guiManager, this);
        getServer().getPluginManager().registerEvents(playerProgressManager, this);
        getServer().getPluginManager().registerEvents(rewardsManager, this);
        getServer().getPluginManager().registerEvents(questManager, this);
    }

    static void EverySeconds(){
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    //MachineLog(player);
                    InventoryItemCheck(player);
                    ActionBar(player);
                    RegenSteam(player);
                    ManageCooldown(player);
                    PlayerInventoryItems(player);
                    ManageProgress(player);
                    GameMenu(player);
                }
            }
        }.runTaskTimer(getMainPlugin(), 0L, 20L);
    }

    static void Every10Tick(){
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    TriggerFishing(player);
                }
            }
        }.runTaskTimer(getMainPlugin(), 0L, 10L);
    }

    static void Every5Minutes(){
        new BukkitRunnable() {
            @Override
            public void run() {
                SaveAllProgress();
            }
        }.runTaskTimer(getMainPlugin(), 0L, 6000L);
    }

    static void EveryMinutes(){
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()){
                    DelayedRefreshMachineTag(player);
                }
            }
        }.runTaskTimer(getMainPlugin(), 0L, 1200L);
    }

    void MachineLog(Player player){
        for (String key : placedMachines.keySet()) {
            if (key.endsWith(__ownerKey)) {
                String owner = placedMachines.get(key);
                if (owner.equals(player.getName())) {
                    player.sendMessage(sendText("&aMachine at &b"
                            + key.replace(__ownerKey, "") + " &ais owned by you!"));
                }
            }
        }
    }

    void ClearMachineTags(){
        for (String key : placedMachines.keySet()){
            if (key.endsWith(__locationKey)){
                String loc = key.replace(__locationKey, "");
                Location location = parseLocationString(loc);
                int taskId = Integer.parseInt(placedMachines.get(location+__taskIdKey));
                assert location != null;
                World world = location.getWorld();
                for (Entity entity : Bukkit.getWorld(world.getName()).getEntities()){
                    if (entity instanceof TextDisplay){
                        if (entity.getScoreboardTags().contains("MachineTag."+location)){
                            entity.remove();
                            consoleLog(sendText("&aCleared tags with taskId tag: &6"+taskId));
                        }
                    }
                }
            }
        }

        consoleLog(sendText("&aCleared all &bMachine's Holograms"));
    }

    public void GenerateMachineTags() {
        Set<String> keys = new HashSet<>(placedMachines.keySet());

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

                if (placedMachines.containsKey(ownerKey) && placedMachines.containsKey(machineNameKey) && placedMachines.containsKey(taskIdKey)) {
                    String owner = placedMachines.get(ownerKey);
                    String machineName = placedMachines.get(machineNameKey);

                    if (!placedMachines.get(location+__machineStatusKey).equals("Disabled")
                    && !placedMachines.get(location+__machineStatusKey).equals("Broken")){
                        placedMachines.put(statusKey, "Inactive");
                    }

                    try {
                        int taskId = Integer.parseInt(placedMachines.get(taskIdKey));
                        SpawnMachineTag(owner, location, machineName, taskId);
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


    public static void RegenSteam(Player player){
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

    public static void ActionBar(Player player) {
        double steam = playerSteam.get(player);
        double maxSteam = playerMaxSteam.get(player);
        double armor = playerArmor.get(player);
        double health = player.getHealth();
        double maxHealth = player.getMaxHealth();
        SendActionBar(player, sendText("&4❤ &c"+FormatDouble(health)+"/"+FormatDouble(maxHealth)+" Health          "+"&7[&8\uD83D\uDD30&f"+FormatDouble(armor)+" &7Armor&7]"+"          &6\uD83C\uDF0A &e"+FormatDouble(steam)+"/"+FormatDouble(maxSteam)+" Steam"));
    }

    public static void ManageCooldown(Player player){
        for (String cd : cooldownList){
            int cooldown = playerCooldown.get(player.getName()+".cooldown."+cd);
            if (cooldown > 0){
                cooldown--;
                playerCooldown.put(player.getName()+".cooldown."+cd, cooldown);
            }
        }
    }

}
