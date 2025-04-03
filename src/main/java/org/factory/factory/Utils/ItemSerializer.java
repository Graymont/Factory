package org.factory.factory.Utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ItemSerializer {

    // Convert an ItemStack array to a Base64 string
    public static String ItemStackArrayToBase64(ItemStack[] items) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        // Write the number of items first
        dataOutput.writeInt(items.length);

        // Save every item in the array
        for (ItemStack item : items) {
            dataOutput.writeObject(item);
        }

        dataOutput.close();
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    // Convert a Base64 string back to an ItemStack array
    public static ItemStack[] ItemStackArrayFromBase64(String data) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

        // Read the number of items
        int size = dataInput.readInt();
        ItemStack[] items = new ItemStack[size];

        // Read the items back into the array
        for (int i = 0; i < size; i++) {
            items[i] = (ItemStack) dataInput.readObject();
        }

        dataInput.close();
        return items;
    }

    // Convert a single ItemStack to a Base64 string
    public static String ItemStackToBase64(ItemStack item) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        dataOutput.writeObject(item);
        dataOutput.close();

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    // Convert a Base64 string back to an ItemStack
    public static ItemStack ItemStackFromBase64(String data) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

        ItemStack item = (ItemStack) dataInput.readObject();
        dataInput.close();

        return item;
    }

    public static ItemStack loadSerializedItem(String storedData) {
        try {
            return ItemSerializer.ItemStackFromBase64(storedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if there's an error
    }

    public static ItemStack[] loadSerializedArrayItems(String storedData) {
        try {
            return ItemSerializer.ItemStackArrayFromBase64(storedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ItemStack[0]; // Return an empty array instead of null
    }


    //bukkit
    public static String BukkitItemToBase64(ItemStack item) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", item);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(config.saveToString().getBytes());
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack BukkitBase64ToItem(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(new String(inputStream.readAllBytes()));
            return config.getItemStack("item");
        } catch (org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String BukkitItemsToBase64Array(List<ItemStack> items) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("items", items);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(config.saveToString().getBytes());
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<ItemStack> BukkitBase64ArrayToItem(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(new String(inputStream.readAllBytes()));

            List<ItemStack> itemList = (List<ItemStack>) (List<?>) config.getList("items"); // Safe conversion
            return itemList; // Convert list to array
        } catch (org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
            return new ArrayList<>(); // Return empty array instead of null
        }
    }

}
