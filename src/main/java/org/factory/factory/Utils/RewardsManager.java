package org.factory.factory.Utils;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static org.factory.factory.Database.GetItem;
import static org.factory.factory.Utils.CooldownManager.hasCooldown;
import static org.factory.factory.Utils.CooldownManager.setCooldown;
import static org.factory.factory.Utils.FactoryItem.CreateSpawner;
import static org.factory.factory.Utils.FactoryItem.ProcessItemMeta;
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
        Hourly,
        Daily,
        Hazmat,
        Weekly,
        Monthly;

        public static RewardType parseReward(String r){
            return switch (r.toLowerCase()) {
                case "starter" -> RewardType.Starter;

                case "daily" -> RewardType.Daily;
                case "hourly" -> RewardType.Hourly;
                case "weekly" -> RewardType.Weekly;
                case "monthly" -> RewardType.Monthly;

                case "hazmat" -> RewardType.Hazmat;

                default -> RewardType.None;
            };
        }

        public static int getCooldown(RewardType type){
            return switch (type) {
                case RewardType.Hourly -> 3600;
                case RewardType.Daily -> 86400;
                case RewardType.Weekly -> 604800;
                case RewardType.Monthly -> 2419200;

                default -> 0;
            };
        }

        public static int getLevel(RewardType type){
            return switch (type) {
                case RewardType.Hourly -> 3;
                case RewardType.Daily -> 10;
                case RewardType.Weekly -> 20;
                case RewardType.Monthly -> 30;

                default -> 0;
            };
        }

        public static double getMoney(RewardType type){
            return switch (type) {
                case RewardType.Hourly -> 500;
                case RewardType.Daily -> 2500;
                case RewardType.Weekly -> 10000;
                case RewardType.Monthly -> 30000;

                default -> 0;
            };
        }

        public static double getExp(RewardType type){
            return switch (type) {
                case RewardType.Hourly -> maxExp.get(3)*0.25;
                case RewardType.Daily -> maxExp.get(10)*0.75;
                case RewardType.Weekly -> maxExp.get(20)*0.75;
                case RewardType.Monthly -> maxExp.get(30)*0.75;

                default -> 0;
            };
        }

        public static List<ItemStack> getItems(RewardType type){
            List<ItemStack> rewardsList = new ArrayList<>();

            if (type == RewardType.Hourly) {
                rewardsList.add(new ItemStack(ProcessItemMeta(new ItemStack(Material.COOKED_BEEF, 24))));
            }
            else if (type == RewardType.Daily) {
                rewardsList.add(new ItemStack(ProcessItemMeta(new ItemStack(Material.DIAMOND, 8))));
            }
            else if (type == RewardType.Weekly) {
                rewardsList.add(CreateSpawner(EntityType.ZOMBIE));
            }
            else if (type == RewardType.Monthly) {
                rewardsList.add(CreateSpawner(EntityType.BLAZE));
            }

            return rewardsList;
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

        else if (
                type == RewardType.Hourly
                || type == RewardType.Daily ||
                type == RewardType.Weekly ||
                type == RewardType.Monthly){
            double money = RewardType.getMoney(type);
            AddPlayerBalance(player, money);
            double exp = RewardType.getExp(type);
            AddExp(player, exp);
            player.sendMessage(sendText(" "));
            player.sendMessage(sendText("&a["+type+" Rewards]"));
            player.sendMessage(sendText(" &7- &f"+money+icon));
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

            if (playerLevel.get(player.getUniqueId()) < RewardType.getLevel(type)){
                player.sendMessage(sendText(Notification_NoLevel(player)));
                return;
            }

            GiveReward(player, type);
            int cooldownAmount = RewardType.getCooldown(type);
            setCooldown(player, CooldownManager.CooldownType.parseCooldown(type.toString()), cooldownAmount);

            player.sendMessage(sendText("&6Claimed &b"+type+" Rewards!"));
        }else{
            player.sendMessage(Notification_HasCooldown(player, CooldownManager.CooldownType.parseCooldown(type.toString())));
        }
    }



}
