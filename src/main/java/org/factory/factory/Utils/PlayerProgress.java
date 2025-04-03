package org.factory.factory.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;

import static org.factory.factory.Utils.UserInterface.*;

public class PlayerProgress {

    public static HashMap<Player, Integer> playerLevel = new HashMap<>();
    public static HashMap<Player, Double> playerExp = new HashMap<>();
    public static HashMap<Integer, Double> maxExp = new HashMap<>();

    static double baseExp = 100;
    static int maxLevel = 1000000;

    public static void init(){
        double start = baseExp+20;
        for (int i = 1; i < maxLevel+1; i++) {
            maxExp.put(i, start);
            start+=(baseExp/4);
        }
    }


    // - exp manager
    public static void AddExp(Player player, double amount){
        double current = playerExp.get(player);
        current += amount;

        playerExp.put(player, current);
    }

    public static void RemoveExp(Player player, double amount){
        double current = playerExp.get(player);
        current -= amount;

        playerExp.put(player, current);
    }

    public static void SetExp(Player player, double amount){
        playerExp.put(player, amount);
    }

    public static Double GetExp(Player player, double amount){
        return playerExp.get(player);
    }


    // - level manager
    public static void AddLevel(Player player, int amount){
        int current = playerLevel.get(player);
        current += amount;

        playerLevel.put(player, current);
    }

    public static void RemoveLevel(Player player, int amount){
        int current = playerLevel.get(player);
        current -= amount;

        playerLevel.put(player, current);
    }

    public static void SetLevel(Player player, int amount){
        playerLevel.put(player, amount);
    }

    public static Integer GetLevel(Player player, int amount){
        return playerLevel.get(player);
    }

 // --
    public static void ManageProgress(Player player){
        LevelingProgress(player);
    }

    public static void LevelingProgress(Player player){
        int currentLevel = playerLevel.get(player);
        double currentExp = playerExp.get(player);

        player.setLevel(currentLevel);
        float experienceProgress = (float) (playerExp.get(player) / maxExp.get(playerLevel.get(player))) * 100;
        experienceProgress = Math.max(0.0f, Math.min(100.0f, experienceProgress));
        player.setExp(experienceProgress / 100);

        if (currentExp >= maxExp.get(currentLevel)){
            currentExp -= maxExp.get(currentLevel);
            currentLevel++;
            playerLevel.put(player, currentLevel);
            playerExp.put(player, currentExp);
        }else{
            return;
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()){
            onlinePlayer.sendMessage(sendText("&6"+player.getName()+" &eis now level &6&l"+playerLevel.get(player)));
        }

        player.sendTitle(sendText("&6Levelup!"), sendText("&eYou are now level &6&l"+playerLevel.get(player)));
        PlaySoundAt(Sound.ENTITY_PLAYER_LEVELUP, player.getLocation(), 1,1);
    }

    public static void ViewMaxExp(){
        for (int i = 0; i < maxLevel+1; i++) {
            consoleLog(sendText("&aLevel "+i+": &2"+maxExp.get(i)));
        }
    }

    public static boolean hasLevel(Player player, int req){
        return playerLevel.get(player) >= req;
    }

}
