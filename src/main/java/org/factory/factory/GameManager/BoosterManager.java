package org.factory.factory.GameManager;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.factory.factory.GameHandler.Booster;

import static org.factory.factory.Events.globalMaxMachine;
import static org.factory.factory.Events.maxMachines;
import static org.factory.factory.GameHandler.Booster.ApplyBooster;
import static org.factory.factory.GameHandler.Booster.getFormattedBoosterName;
import static org.factory.factory.GameHandler.FactoryItem.*;
import static org.factory.factory.GameHandler.FactoryMachine.AddMaxMachine;
import static org.factory.factory.GameManager.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.UserInterface.PlaySoundAt;
import static org.factory.factory.Utils.UserInterface.sendText;

public class BoosterManager implements Listener {

    @EventHandler
    public void OnBoosterConsume(PlayerInteractEvent event){
        if (event.getAction().isRightClick()){

            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item.getType() != Material.AIR){

                if (!isFactoryItem(item)){
                    return;
                }

                if (!isPotion(item)){
                    return;
                }

                event.setCancelled(true);
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();

                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                Booster.BoosterType boosterType = Booster.BoosterType.parseBooster(container.get(GetNamespacedKey(boosterTypeKey), PersistentDataType.STRING));
                Integer boosterDuration = container.get(GetNamespacedKey(boosterDurationKey), PersistentDataType.INTEGER);

                ApplyBooster(player, boosterType, boosterDuration);

                player.sendMessage(sendText("&dConsumed Booster &b"+getFormattedBoosterName(boosterType)));

                //PlaySoundAt(Sound.ENTITY_WANDERING_TRADER_DRINK_POTION, player.getLocation(), 1, 1);

                player.sendTitle(sendText("&dConsumed Booster"), sendText("&b"+getFormattedBoosterName(boosterType)));

                PlaySoundAt(Sound.ENTITY_GENERIC_DRINK, player.getLocation(), 1, 1);
            }

        }
    }

    @EventHandler
    public void OnLicenseConsume(PlayerInteractEvent event){
        if (event.getAction().isRightClick()){

            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item.getType() != Material.AIR){
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();

                if (container.has(GetNamespacedKey("maxMachineAmount"), PersistentDataType.INTEGER)){
                    int amount = container.get(GetNamespacedKey("maxMachineAmount"), PersistentDataType.INTEGER);

                    if (maxMachines.get(player.getUniqueId()) >= globalMaxMachine){
                        player.sendMessage(sendText("&4You have reached global max machines &6("+globalMaxMachine+")"));
                        return;
                    }

                    AddMaxMachine(player, amount);

                    player.sendMessage(sendText("&a+"+amount+" &fMax Machine"));
                    player.sendMessage(sendText("&aYou now have &2"+maxMachines.get(player.getUniqueId())+" &aMax Machine!"));

                    item.setAmount(item.getAmount()-1);

                    player.sendTitle(sendText("&aLicense Used"), sendText("&a+"+amount+" &fMax Machine"));

                    PlaySoundAt(Sound.ENTITY_PLAYER_LEVELUP, player.getLocation(), 1, 0);
                }
            }

        }
    }

}
