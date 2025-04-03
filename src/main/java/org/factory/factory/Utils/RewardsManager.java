package org.factory.factory.Utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static org.factory.factory.Database.GetItem;
import static org.factory.factory.Utils.CooldownManager.hasCooldown;
import static org.factory.factory.Utils.CooldownManager.setCooldown;
import static org.factory.factory.Utils.FactoryMachine.serialCodeKey;
import static org.factory.factory.Utils.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.PlayerProgress.*;
import static org.factory.factory.Utils.UserInterface.*;
import static org.factory.factory.Utils.VaultEconomy.AddPlayerBalance;
import static org.factory.factory.Utils.VaultEconomy.icon;

public class RewardsManager implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            GiveReward(player, RewardType.Starter);

            Broadcast(sendText(" &b&lNew Player! &fplease welcome &e" + player.getName() + "!"));
            PlaySoundAt(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, player.getLocation(), 1, 1);
        }
    }

    public enum RewardType{
        None,
        Starter,
        Daily,
        Hazmat;

        public static RewardType parseReward(String r){
            return switch (r.toLowerCase()) {
                case "starter" -> RewardType.Starter;
                case "daily" -> RewardType.Daily;
                case "hazmat" -> RewardType.Hazmat;

                default -> RewardType.None;
            };
        }

        public static int getCooldown(RewardType type){
            return switch (type) {
                case RewardType.Daily -> 86400;

                default -> 0;
            };
        }
    }

    public static void GiveReward(Player player, RewardType type){
        if (type == RewardType.Starter){
            for (int i = 0; i < 3; i++) {
                ItemStack machine = new ItemStack(GetItem("wheatmachine")).clone();
                ItemMeta machineMeta = machine.getItemMeta();
                PersistentDataContainer container = machineMeta.getPersistentDataContainer();
                container.set(GetNamespacedKey(serialCodeKey), PersistentDataType.STRING, GenerateSerialCode());
                machine.setItemMeta(machineMeta);
                player.getInventory().addItem(machine.clone());
            }
            AddPlayerBalance(player, 1000);
            player.getInventory().addItem(new ItemStack(GetItem("hazmathelmet")));
            player.getInventory().addItem(new ItemStack(GetItem("hazmatvest")));
            player.getInventory().addItem(new ItemStack(GetItem("hazmatleggings")));
            player.getInventory().addItem(new ItemStack(GetItem("hazmatboots")));
        }

        else if (type == RewardType.Daily){
            AddPlayerBalance(player, 2500);
            double exp = maxExp.get(playerLevel.get(player))*0.25;
            AddExp(player, exp);
            player.sendMessage(sendText(" "));
            player.sendMessage(sendText("&a[Daily Rewards]"));
            player.sendMessage(sendText(" &7- &f2500"+icon));
            player.sendMessage(sendText(" &7- &f"+exp+" EXP"));
        }

        else if (type == RewardType.Hazmat){
            Inventory inventory = OpenChest(player, 1, "Hazmat Set");
            inventory.setItem(0, new ItemStack(GetItem("hazmathelmet")));
            inventory.setItem(1, new ItemStack(GetItem("hazmatvest")));
            inventory.setItem(2, new ItemStack(GetItem("hazmatleggings")));
            inventory.setItem(3, new ItemStack(GetItem("hazmatboots")));
            player.openInventory(inventory);
        }
    }

    public static void ClaimRewards(Player player, RewardType type){
        if (!hasCooldown(player, CooldownManager.CooldownType.parseCooldown(type.toString()))){
            GiveReward(player, type);
            int cooldownAmount = RewardType.getCooldown(type);
            setCooldown(player, CooldownManager.CooldownType.parseCooldown(type.toString()), cooldownAmount);
        }else{
            player.sendMessage(Notification_HasCooldown(player, CooldownManager.CooldownType.parseCooldown(type.toString())));
        }
    }



}
