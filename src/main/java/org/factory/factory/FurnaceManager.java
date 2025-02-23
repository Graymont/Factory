package org.factory.factory;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class FurnaceManager implements Listener {

    Factory plugin;
    Events events;

    public FurnaceManager(Factory pl, Events e) {
        plugin = pl;
        events = e;
    }

    public HashMap<Location, Integer> smeltSpeed = new HashMap<>();

    /*@EventHandler
    public void onFurnacePlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == Material.FURNACE) {
            Player player = event.getPlayer();
            int cookTimeInSeconds = 10; // Default 10 seconds per item

            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasLore()) {
                for (String lore : meta.getLore()) {
                    if (lore != null && events.uncolouredText(lore).contains("Cook Time")) {
                        cookTimeInSeconds = Integer.parseInt(events.numberInText(lore));
                        break;
                    }
                }
            }

            int cookTimeInTicks = cookTimeInSeconds * 20; // Convert seconds to ticks
            smeltSpeed.put(event.getBlockPlaced().getLocation(), cookTimeInTicks);
        }
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof Furnace furnace)) return;

        int cookTime = smeltSpeed.getOrDefault(block.getLocation(), 200); // Default: 10 sec

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            furnace.setCookTimeTotal(cookTime);
            furnace.update();
        }, 1L);
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        Block block = event.getBlock();
        int burnTime = event.getBurnTime();
        int cookTime = smeltSpeed.getOrDefault(block.getLocation(), 200);

        event.setBurnTime(Math.max(1, burnTime / (cookTime / 200))); // Adjust burn time dynamically
    }*/
}
