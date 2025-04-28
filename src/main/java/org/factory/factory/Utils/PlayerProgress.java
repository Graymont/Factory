package org.factory.factory.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;


import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.Utils.Booster.GetBoosterPercent;
import static org.factory.factory.Utils.Booster.boosters;
import static org.factory.factory.Utils.FactoryEvents.events_expMultiplier;
import static org.factory.factory.Utils.UserInterface.*;
import static org.factory.factory.Utils.VaultEconomy.*;

public class PlayerProgress {

    public static HashMap<UUID, Integer> playerLevel = new HashMap<>();
    public static HashMap<UUID, Double> playerExp = new HashMap<>();
    public static HashMap<Integer, Double> maxExp = new HashMap<>();
    public static HashMap<Integer, Integer> prestigeRequirement = new HashMap<>();

    public static HashMap<UUID, Double> playerSellMultiplier = new HashMap<>();
    public static HashMap<UUID, Double> playerExpMultiplier = new HashMap<>();

    public static HashMap<UUID, Integer> playerPrestige = new HashMap<>();

    static double baseExp = 100;

    static int maxLevel = 1000000;

    static int maxPrestige = 10;

    public static void init(){
        double start = baseExp+20;
        for (int i = 1; i < maxLevel+1; i++) {
            maxExp.put(i, start);
            start+=(baseExp/4);
        }

        int start2 = 50;
        for (int i = 0; i < maxPrestige; i++) {
            prestigeRequirement.put(i, start2);
            start2+=50;
        }
    }

    public static HashMap<Player, List<String>> progressNotifications = new HashMap<>();

    // - exp manager
    public static Double AddExp(Player player, double amount, double proficiency){
        double current = playerExp.get(player.getUniqueId());
        double multiplier = playerExpMultiplier.get(player.getUniqueId());

        double boosterPercent = 1.0;

        if (boosters.get(player.getUniqueId()) != null) {
            if (boosters.get(player.getUniqueId()).name().contains("Exp_Bonus")) {
                boosterPercent = GetBoosterPercent(boosters.get(player.getUniqueId()));
            }
        }

        double prestigeMultiplier = 1.0;

        double currentPrestige = playerPrestige.get(player.getUniqueId());
        if (currentPrestige > 0){
            prestigeMultiplier = 1 + ( currentPrestige / 10 );
            //consoleLog(prestigeMultiplier+"");
        }

        double calculation = (((amount*boosterPercent)*multiplier)*events_expMultiplier)*prestigeMultiplier;

        current += calculation;
        current += proficiency;

        playerExp.put(player.getUniqueId(), current);

        //player.sendMessage(sendText(""+calculation));

        return calculation+proficiency;
    }

    public static void RemoveExp(Player player, double amount){
        double current = playerExp.get(player.getUniqueId());
        current -= amount;

        playerExp.put(player.getUniqueId(), current);
    }

    public static void SetExp(Player player, double amount){
        playerExp.put(player.getUniqueId(), amount);
    }

    public static Double GetExp(Player player, double amount){
        return playerExp.get(player.getUniqueId());
    }


    // - level manager
    public static void AddLevel(Player player, int amount){
        int current = playerLevel.get(player.getUniqueId());
        current += amount;

        playerLevel.put(player.getUniqueId(), current);
    }

    public static void RemoveLevel(Player player, int amount){
        int current = playerLevel.get(player.getUniqueId());
        current -= amount;

        playerLevel.put(player.getUniqueId(), current);
    }

    public static void SetLevel(Player player, int amount){
        playerLevel.put(player.getUniqueId(), amount);
    }

    public static Integer GetLevel(Player player, int amount){
        return playerLevel.get(player.getUniqueId());
    }

 // --
    public static void ManageProgress(Player player){
        LevelingProgress(player);
    }

    public static void LevelingProgress(Player player){
        int currentLevel = playerLevel.get(player.getUniqueId());

        if (currentLevel <= 0){
            SetLevel(player, 1);
        }

        double currentExp = playerExp.get(player.getUniqueId());

        player.setLevel(currentLevel);
        float experienceProgress = (float) (playerExp.get(player.getUniqueId()) / maxExp.get(playerLevel.get(player.getUniqueId()))) * 100;
        experienceProgress = Math.max(0.0f, Math.min(100.0f, experienceProgress));
        player.setExp(experienceProgress / 100);

        if (currentExp >= maxExp.get(currentLevel)){
            currentExp -= maxExp.get(currentLevel);
            currentLevel++;
            playerLevel.put(player.getUniqueId(), currentLevel);
            playerExp.put(player.getUniqueId(), currentExp);
        }else{
            return;
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()){
            onlinePlayer.sendMessage(sendText("&6"+player.getName()+" &eis now level &6&l"+playerLevel.get(player.getUniqueId())));
        }

        player.sendTitle(sendText("&6Levelup!"), sendText("&eYou are now level &6&l"+playerLevel.get(player.getUniqueId())));
        PlaySoundAt(Sound.ENTITY_PLAYER_LEVELUP, player.getLocation(), 1,1);
    }

    public static void ViewMaxExp(){
        for (int i = 0; i < maxLevel+1; i++) {
            consoleLog(sendText("&aLevel "+i+": &2"+maxExp.get(i)));
        }
    }

    public static boolean hasLevel(Player player, int req){
        return playerLevel.get(player.getUniqueId()) >= req;
    }

    public static void SetPlayerSellMultiplier(Player player, double amount){
        playerSellMultiplier.put(player.getUniqueId(), amount);
    }

    public static void SetPlayerExpMultiplier(Player player, double amount){
        playerExpMultiplier.put(player.getUniqueId(), amount);
    }


    public static void ManageProgressNotification(Player player){
        for (int i = 0; i < 250; i++) {
            player.sendMessage(" ");
        }

        List<String> progressNotifList = progressNotifications.get(player);

        for (String msg : progressNotifList){
            player.sendMessage(sendText(msg));
        }

        Bukkit.getScheduler().runTaskLater(getMainPlugin(), () -> {
            //ShowChatList(player);
        }, 100L);
    }

    public static void spawnExpHologram(Location location, double exp) {
        Random random = new Random();
        //double randomX = -0.5 + (2 * random.nextDouble());
        //double randomY = 1 * (3 * random.nextDouble());
        //double randomZ = -0.5 + (2 * random.nextDouble());

        World world = location.getWorld();

        Location spawnLocation = location.clone().add(0, 1.5, 0);

        TextDisplay textDisplay = (TextDisplay) world.spawnEntity(spawnLocation, EntityType.TEXT_DISPLAY);


        textDisplay.setText(sendText("&e+"+(FormatDouble(exp))+" &6&lExp"));

        textDisplay.setBillboard(Display.Billboard.CENTER);
        textDisplay.setShadowed(false);
        textDisplay.setSeeThrough(false);
        textDisplay.setLineWidth(300);
        textDisplay.setViewRange(30.0f);

        textDisplay.addScoreboardTag("ExperienceHologram");

        Bukkit.getScheduler().runTaskLater(getMainPlugin(), textDisplay::remove, 35L);
    }

    public static void SetPrestige(Player player, int amount){
        playerPrestige.put(player.getUniqueId(), amount);
    }

    public static void PerformPrestige(Player player){
        int current = playerPrestige.get(player.getUniqueId());
        if (current >= maxPrestige){
            player.sendMessage(sendText("&4You already reached the maximum prestige! &c("+current+")"));
            return;
        }
        int req = prestigeRequirement.get(playerPrestige.get(player.getUniqueId()));

        double moneyReq = req*5000;

        if (!hasLevel(player, req)){
            player.sendMessage(Notification_NoLevel(player));
            player.sendMessage(sendText("&cYou need &6"+req+" Level &cto prestige!"));
            return;
        }
        if (GetPlayerBalance(player) < moneyReq){
            player.sendMessage(Notification_NoMoney(player, req));
            player.sendMessage(sendText("&cYou need &6"+moneyReq+icon+" &cto prestige!"));
            return;
        }

        RemoveLevel(player, req);
        if (playerLevel.get(player.getUniqueId()) <= 0){
            SetLevel(player, 1);
        }
        SetExp(player, 0.0);
        RemovePlayerBalance(player, moneyReq);
        SetPrestige(player, current+1);

        SendTitle(player, "&bPrestige!", "&fYou are now prestige &3"+playerPrestige.get(player.getUniqueId()), 5, 5, 5);

        Broadcast("&3"+player.getName()+" &bhas reached &3Prestige &3"+playerPrestige.get(player.getUniqueId())+"! &bsay 'GG' in the chat!");

        PlaySoundAt(Sound.UI_TOAST_CHALLENGE_COMPLETE, player.getLocation(), 1, 1);
    }

    public static int GetPrestigeRequirement(int level) {
        if (level < 50) return 0;
        return level / 50;
    }

}
