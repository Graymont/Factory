package org.factory.factory.Utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

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
}
