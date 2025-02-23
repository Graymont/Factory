package org.factory.factory.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.factory.factory.Factory;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.List;

public class SQLiteDatabase {

    public Connection connection;

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

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS PlacedMachines (location TEXT PRIMARY KEY, " +
                "owner TEXT, uuid TEXT, taskId INTEGER, machineLevel INTEGER, speed INTEGER, productionRate INTEGER, steamConsumption INTEGER, durability INTEGER, maxDurability INTEGER, dropName TEXT, potentialDrop INTEGER, rarity TEXT, machineName TEXT)";
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
    }

    public void SaveMachineData(HashMap<String, String> placedMachines){
        if (connection == null || !isConnected()) {
            System.err.println("SQLite connection is null or closed.");
            return;
        }

        clearPlacedMachineTable(); // clear table supaya refresh

        createTable();

        String sqlInsert = "INSERT OR REPLACE INTO PlacedMachines (location, owner, uuid, taskId, machineLevel, speed, productionRate, steamConsumption, durability, maxDurability, dropName, potentialDrop, rarity, machineName) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> LoadMachineData(Connection connection) {
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
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return placedMachines;
    }

    public void SaveMachineItems(HashMap<Location, ItemStack> machineItems){
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

    public HashMap<Location, ItemStack> LoadMachineItems(Connection connection) {
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

    public void clearPlacedMachineTable() {
        String sql = "DELETE FROM PlacedMachines";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
            System.out.println("PlacedMachine table cleared successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void clearMachineItemsTable() {
        String sql = "DELETE FROM MachineItems";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
            System.out.println("MachineItems table cleared successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("SQLite disconnected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
