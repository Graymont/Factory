package org.factory.factory.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.factory.factory.Factory;

import java.io.File;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.factory.factory.Events.*;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.Utils.CooldownManager.*;
import static org.factory.factory.Utils.PlayerProgress.*;
import static org.factory.factory.Utils.UserInterface.*;

public class SQLiteDatabase {

    public static Connection connection;

    public void connect() {
        try {
            File dbFile = new File("plugins/Factory/Game Database/SQLite/database.db");
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }

            // Connect to SQLite database
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            System.out.println("Connected to SQLite database.");

            // Create a sample table
            createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS PlacedMachines (location TEXT PRIMARY KEY, " +
                "owner TEXT, uuid TEXT, taskId INTEGER, machineLevel INTEGER, speed INTEGER, productionRate INTEGER, " +
                "steamConsumption INTEGER, durability INTEGER, maxDurability INTEGER, dropName TEXT, " +
                "potentialDrop INTEGER, rarity TEXT, machineName TEXT, status TEXT, totalProduction INTEGER, " +
                "machineType TEXT, steamProduction INTEGER)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql2 = "CREATE TABLE IF NOT EXISTS MachineItems (location TEXT PRIMARY KEY, " +
                "item TEXT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql2);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql3 = "CREATE TABLE IF NOT EXISTS StoredMachines (uuid TEXT PRIMARY KEY, " +
                "item TEXT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql3);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql4 = "CREATE TABLE IF NOT EXISTS Backpack (id TEXT PRIMARY KEY, " +
                "item TEXT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql4);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql5 = "CREATE TABLE IF NOT EXISTS PlayerAttributes (uuid TEXT PRIMARY KEY, " +
                "playerName TEXT, level INTEGER, exp INTEGER, maxMachine INTEGER, sellMultiplier REAL, expMultiplier REAL)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql5);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql6 = "CREATE TABLE IF NOT EXISTS PlayerCooldown (" +
                "uuid TEXT, " +
                "playerName TEXT, " +
                "cooldownName TEXT, " +
                "cooldown INTEGER, " +
                "PRIMARY KEY (uuid, cooldownName))";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql6);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void SavePlayerProgress(Player player) {
        String sql = "INSERT OR REPLACE INTO PlayerAttributes (uuid, playerName, level, exp, maxMachine, sellMultiplier, expMultiplier) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.setString(2, player.getName());
            pstmt.setInt(3, playerLevel.get(player.getUniqueId()));
            pstmt.setDouble(4, playerExp.get(player.getUniqueId()));
            pstmt.setDouble(5, maxMachines.get(player.getUniqueId()));
            pstmt.executeUpdate();

            consoleLog(sendText("&aSaved Player Leveling of &2" + player.getName() + " &a(level: " + playerLevel.get(player.getUniqueId()) + " exp: " + playerExp.get(player.getUniqueId()) + ")"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sql = "INSERT OR REPLACE INTO PlayerCooldown (uuid, playerName, cooldownName, cooldown) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (CooldownManager.CooldownType cooldownType : CooldownManager.CooldownType.values()){
                pstmt.setString(1, player.getUniqueId().toString());
                pstmt.setString(2, player.getName());
                pstmt.setString(3, cooldownType.toString());
                pstmt.setLong(4, getRemainingTime(player, cooldownType));
                consoleLog(sendText("&aSaved Player Cooldown of &2" + player.getName() + " &a(cooldownType: " + cooldownType.toString() + " cooldown: " + getRemainingTime(player, cooldownType) + ")"));
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void LoadPlayerProgress(Player player) {
        String sql = "SELECT level, exp, maxMachine, sellMultiplier, expMultiplier FROM PlayerAttributes WHERE uuid = ?"; // Fixed column names

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                playerLevel.put(player.getUniqueId(), rs.getInt("level"));
                playerExp.put(player.getUniqueId(), rs.getDouble("exp"));
                maxMachines.put(player.getUniqueId(), rs.getInt("maxMachine"));
                playerSellMultiplier.put(player.getUniqueId(), rs.getDouble("sellMultiplier"));
                playerExpMultiplier.put(player.getUniqueId(), rs.getDouble("expMultiplier"));

                consoleLog(sendText("&aLoaded Player Leveling of &2" + player.getName() + " &a(level: " + playerLevel.get(player.getUniqueId()) + " exp: " + playerExp.get(player.getUniqueId()) + ")"));
            } else {
                consoleLog(sendText("&cNo progress found for &4" + player.getName()));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        sql = "SELECT cooldownName, cooldown FROM PlayerCooldown WHERE uuid = ?"; // Fixed column names

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = pstmt.executeQuery();

            boolean hasCooldown = false;
            while (rs.next()) {
                String cooldownName = rs.getString("cooldownName");
                long cooldown = rs.getLong("cooldown");

                setCooldown(player, CooldownType.parseCooldown(cooldownName), (int) cooldown);

                consoleLog(sendText("&aLoaded Player Cooldown of &2" + player.getName() + " &a(cooldownName: " + cooldownName + " cooldown: " + cooldown + ")"));
                hasCooldown = true;
            }

            if (!hasCooldown) {
                consoleLog(sendText("&cNo cooldown progress found for &4" + player.getName()));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void SaveBackpack(String id, String itemData) {
        String sql = "INSERT INTO Backpack (id, item) VALUES (?, ?) ON CONFLICT(id) DO UPDATE SET item = excluded.item";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, itemData);
            pstmt.executeUpdate();

            consoleLog(sendText("&aSaved Backpack with id: &2"+id));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String GetBackpackItem(String id) {
        String sql = "SELECT item FROM Backpack WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id); // Set the backpack ID
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("item"); // Return the stored serialized item data
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void SaveMachineData(HashMap<String, String> placedMachines){
        if (connection == null || !isConnected()) {
            System.err.println("SQLite connection is null or closed.");
            return;
        }

        clearPlacedMachineTable(); // clear table supaya refresh

        createTable();

        String sqlInsert = "INSERT OR REPLACE INTO PlacedMachines (location, owner, uuid, taskId, machineLevel, " +
                "speed, productionRate, steamConsumption, durability, maxDurability, dropName, potentialDrop, rarity, machineName, " +
                "status, totalProduction, machineType, steamProduction) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sqlInsert)) {
            for (String key : placedMachines.keySet()) {
                if (key.endsWith(".location")) {
                    String locationKey = key.replace(".location", "");
                    String location = placedMachines.get(locationKey + ".location");
                    String owner = placedMachines.get(locationKey + ".owner");
                    String UUID = placedMachines.get(locationKey + ".uuid");
                    int taskId = Integer.parseInt(placedMachines.getOrDefault(locationKey + ".taskId", "0"));
                    int machineLevel = Integer.parseInt(placedMachines.getOrDefault(locationKey + ".machineLevel", "0"));
                    int speed = Integer.parseInt(placedMachines.getOrDefault(locationKey + ".speed", "0"));
                    int productionRate = Integer.parseInt(placedMachines.getOrDefault(locationKey + ".productionRate", "0"));
                    int steamConsumption = Integer.parseInt(placedMachines.getOrDefault(locationKey + ".steamConsumption", "0"));
                    int durability = Integer.parseInt(placedMachines.getOrDefault(locationKey + ".durability", "0"));
                    int maxDurability = Integer.parseInt(placedMachines.getOrDefault(locationKey + ".maxDurability", "0"));
                    String machineName = placedMachines.get(locationKey + ".machineName");
                    String rarity = placedMachines.get(locationKey + ".rarity");

                    String machineType = placedMachines.get(locationKey + ".machineType");
                    int steamProduction = Integer.parseInt(placedMachines.getOrDefault(locationKey + ".steamProduction", "0"));

                    String status = placedMachines.get(locationKey + ".status");
                    int totalProduction = Integer.parseInt(placedMachines.getOrDefault(locationKey + ".totalProduction", "0"));


                    String drop = placedMachines.get(locationKey + ".dropName");
                    int potentialDrop = Integer.parseInt(placedMachines.getOrDefault(locationKey + ".potentialDrop", "1"));
                    pstmt.setString(1, location);
                    pstmt.setString(2, owner);
                    pstmt.setString(3, UUID);
                    pstmt.setInt(4, taskId);
                    pstmt.setInt(5, machineLevel);
                    pstmt.setInt(6, speed);
                    pstmt.setInt(7, productionRate);
                    pstmt.setInt(8, steamConsumption);
                    pstmt.setInt(9, durability);
                    pstmt.setInt(10, maxDurability);
                    pstmt.setString(11, drop);
                    pstmt.setInt(12, potentialDrop);
                    pstmt.setString(13, rarity);
                    pstmt.setString(14, machineName);
                    pstmt.setString(15, status);
                    pstmt.setInt(16, totalProduction);
                    pstmt.setString(17, machineType);
                    pstmt.setInt(18, steamProduction);

                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, String> LoadMachineData(Connection connection) {
        if (connection == null || !isConnected()) {
            System.err.println("SQLite connection is null or closed.");
            return new HashMap<>();
        }

        createTable();
        HashMap<String, String> placedMachines = new HashMap<>();
        String query = "SELECT * FROM PlacedMachines";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String location = rs.getString("location");
                placedMachines.put(location + ".owner", rs.getString("owner"));
                placedMachines.put(location + ".uuid", rs.getString("uuid"));
                placedMachines.put(location + ".location", rs.getString("location"));
                placedMachines.put(location + ".taskId", String.valueOf(rs.getInt("taskId")));
                placedMachines.put(location + ".machineLevel", String.valueOf(rs.getInt("machineLevel")));
                placedMachines.put(location + ".speed", String.valueOf(rs.getInt("speed")));
                placedMachines.put(location + ".productionRate", String.valueOf(rs.getInt("productionRate")));
                placedMachines.put(location + ".steamConsumption", String.valueOf(rs.getInt("steamConsumption")));
                placedMachines.put(location + ".durability", String.valueOf(rs.getInt("durability")));
                placedMachines.put(location + ".maxDurability", String.valueOf(rs.getInt("maxDurability")));
                placedMachines.put(location + ".dropName", rs.getString("dropName"));
                placedMachines.put(location + ".potentialDrop", String.valueOf(rs.getInt("potentialDrop")));
                placedMachines.put(location + ".rarity", rs.getString("rarity"));
                placedMachines.put(location + ".machineName", rs.getString("machineName"));
                placedMachines.put(location + ".status", rs.getString("status"));
                placedMachines.put(location + ".totalProduction", String.valueOf(rs.getInt("totalProduction")));
                placedMachines.put(location + ".machineType", rs.getString("machineType"));
                placedMachines.put(location + ".steamProduction", String.valueOf(rs.getInt("steamProduction")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return placedMachines;
    }

    public static void SaveMachineItems(HashMap<Location, ItemStack> machineItems){
        if (connection == null || !isConnected()) {
            System.err.println("SQLite connection is null or closed.");
            return;
        }

        clearMachineItemsTable(); // clear table supaya refresh

        createTable();

        String sqlInsert = "INSERT OR REPLACE INTO MachineItems (location, item) " +
                "VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sqlInsert)) {
            String serializedItem = "";
            for (Location location : machineItems.keySet()) {
                try {
                    serializedItem = ItemSerializer.ItemStackArrayToBase64(new ItemStack[]{machineItems.get(location)});
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pstmt.setString(1, ""+location);
                pstmt.setString(2, serializedItem);

                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<Location, ItemStack> LoadMachineItems(Connection connection) {
        if (connection == null || !isConnected()) {
            System.err.println("SQLite connection is null or closed.");
            return new HashMap<>();
        }

        createTable();
        HashMap<Location, ItemStack> machineItems = new HashMap<>();
        String query = "SELECT * FROM MachineItems";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String locationString = rs.getString("location");
                Location location = parseLocationString(locationString);

                String serializedItem = rs.getString("item");
                ItemStack[] items = ItemSerializer.ItemStackArrayFromBase64(serializedItem);
                ItemStack loadedItem = items[0];

                machineItems.put(location, loadedItem);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return machineItems;
    }

    public static void SaveStoredMachines(HashMap<UUID, List<ItemStack>> storedMachines){
        if (connection == null || !isConnected()) {
            System.err.println("SQLite connection is null or closed.");
            return;
        }

        clearStoredMachinesTable(); // clear table supaya refresh

        createTable();

        String sqlInsert = "INSERT OR REPLACE INTO StoredMachines (uuid, item) " +
                "VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sqlInsert)) {
            String serializedItem = "";
            for (UUID uuid : storedMachines.keySet()) {
                try {
                    ItemStack[] itemToAdd = storedMachines.get(uuid).toArray(new ItemStack[0]);
                    serializedItem = ItemSerializer.ItemStackArrayToBase64(itemToAdd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pstmt.setString(1, ""+uuid);
                pstmt.setString(2, serializedItem);

                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<UUID, List<ItemStack>> LoadStoredMachines(Connection connection) {
        if (connection == null || !isConnected()) {
            System.err.println("SQLite connection is null or closed.");
            return new HashMap<>();
        }

        createTable();
        HashMap<UUID, List<ItemStack>> storedMachines = new HashMap<>();
        String query = "SELECT * FROM StoredMachines";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));

                String serializedItem = rs.getString("item");
                ItemStack[] items = ItemSerializer.ItemStackArrayFromBase64(serializedItem);
                //ItemStack loadedItem = items[0];

                storedMachines.put(uuid, Arrays.asList(items));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return storedMachines;
    }

    public static Location parseLocationString(String locString) {
        try {
            // Extract the world name
            String worldName = locString.substring(locString.indexOf("name=") + 5, locString.indexOf("},x="));

            // Extract the coordinates and rotation values
            String[] parts = locString.substring(locString.indexOf("x=")).replace("x=", "")
                    .replace("y=", "")
                    .replace("z=", "")
                    .replace("pitch=", "")
                    .replace("yaw=", "")
                    .replace("}", "")
                    .split(",");

            if (parts.length < 5) {
                throw new IllegalArgumentException("Invalid location string format.");
            }

            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            float pitch = Float.parseFloat(parts[3]);
            float yaw = Float.parseFloat(parts[4]);

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                throw new IllegalArgumentException("World not found: " + worldName);
            }

            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null if parsing fails
        }
    }

    public static void clearPlacedMachineTable() {
        String sql = "DELETE FROM PlacedMachines";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
            System.out.println("PlacedMachine table cleared successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void clearMachineItemsTable() {
        String sql = "DELETE FROM MachineItems";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
            System.out.println("MachineItems table cleared successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void clearStoredMachinesTable() {
        String sql = "DELETE FROM StoredMachines";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
            System.out.println("StoredMachines table cleared successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("SQLite disconnected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void SaveAllPlayerProgress(){
        for (Player player : Bukkit.getOnlinePlayers()){
            SavePlayerProgress(player);
            HandleCloseEvent(player);
        }
    }


    public static void SaveAllProgress(){
        Broadcast(" ");
        Broadcast(" &8☗ &7Saving&8...");
        //Broadcast(" &4⚠ &7&o(please do not quit until saving process completed)");
        SaveMachineData(placedMachines);
        SaveMachineItems(machineItems);
        SaveStoredMachines(storedMachines);
        SaveAllPlayerProgress();
        Broadcast(" &8☗ &8[&aSaving Completed&8]");
    }
}
