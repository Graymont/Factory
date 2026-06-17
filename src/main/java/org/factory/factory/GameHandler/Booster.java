package org.factory.factory.GameHandler;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

import static org.factory.factory.Utils.UserInterface.FormatDouble;
import static org.factory.factory.Utils.UserInterface.numberInText;

public class Booster {

    public static final HashMap<UUID, BoosterType> boosters = new HashMap<>();
    public static final HashMap<UUID, Long> activeBooster = new HashMap<>();

    public enum BoosterType {
        None,

        _5_Percent_Exp_Bonus,
        _10_Percent_Exp_Bonus,
        _15_Percent_Exp_Bonus,
        _20_Percent_Exp_Bonus,
        _25_Percent_Exp_Bonus,
        _30_Percent_Exp_Bonus,

        _5_Percent_Sell_Bonus,
        _10_Percent_Sell_Bonus,
        _15_Percent_Sell_Bonus,
        _20_Percent_Sell_Bonus,
        _25_Percent_Sell_Bonus,
        _30_Percent_Sell_Bonus;

        public static BoosterType parseBooster(String r) {
            return switch (r.toLowerCase()) {
                //case "none" -> None;

                case "_5_percent_exp_bonus" -> _5_Percent_Exp_Bonus;
                case "_10_percent_exp_bonus" -> _10_Percent_Exp_Bonus;
                case "_15_percent_exp_bonus" -> _15_Percent_Exp_Bonus;
                case "_20_percent_exp_bonus" -> _20_Percent_Exp_Bonus;
                case "_25_percent_exp_bonus" -> _25_Percent_Exp_Bonus;
                case "_30_percent_exp_bonus" -> _30_Percent_Exp_Bonus;

                case "_5_percent_sell_bonus" -> _5_Percent_Sell_Bonus;
                case "_10_percent_sell_bonus" -> _10_Percent_Sell_Bonus;
                case "_15_percent_sell_bonus" -> _15_Percent_Sell_Bonus;
                case "_20_percent_sell_bonus" -> _20_Percent_Sell_Bonus;
                case "_25_percent_sell_bonus" -> _25_Percent_Sell_Bonus;
                case "_30_percent_sell_bonus" -> _30_Percent_Sell_Bonus;

                default -> None;
            };
        }
    }

    public static void ManageBooster(Player player){

        if (getRemainingBoosterTime(player) <= 0){
            if (boosters.get(player.getUniqueId()) != BoosterType.None){
                boosters.put(player.getUniqueId(), BoosterType.None);
            }
        }

    }

    public static String getFormattedBoosterName(BoosterType type){

        if (type == BoosterType.None){
            return "None";
        }

        String formattedPercent = FormatDouble(( (GetBoosterPercent(type)-1)*100 ));

        if (type.toString().contains("Sell_Bonus")){
            return formattedPercent + "% Sell Bonus";
        } else if (type.toString().contains("Exp_Bonus")){
            return formattedPercent + "% Exp Bonus";
        }

        return "None";
    }

    public static double GetBoosterPercent(BoosterType type){
        return 1+(Double.parseDouble(numberInText(type.toString()))/100);
    }

    public static void ApplyBooster(Player player, BoosterType type, int seconds) {
        boosters.put(player.getUniqueId(), type);
        activeBooster.put(player.getUniqueId(), System.currentTimeMillis() + (seconds * 1000L));

        long start = System.currentTimeMillis();
        boosterStart.put(player.getUniqueId(), start);
    }

    public static boolean hasBooster(Player player) {
        return activeBooster.getOrDefault(player.getUniqueId(), 0L) > System.currentTimeMillis();
    }

    public static long getRemainingBoosterTime(Player player) {
        return Math.max(0, (activeBooster.getOrDefault(player.getUniqueId(), 0L) - System.currentTimeMillis()) / 1000);
    }

    public static void ResetBooster(Player player) {
        boosters.remove(player.getUniqueId());
        activeBooster.remove(player.getUniqueId());
    }

    public static HashMap<UUID, Long> boosterStart = new HashMap<>();
    public static String getPercentRemainingBooster(Player player) {
        Long endTime = activeBooster.get(player.getUniqueId());
        Long startTime = boosterStart.get(player.getUniqueId());
        if (endTime == null || startTime == null) return "0";

        long currentTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;
        long remainingTime = endTime - currentTime;

        if (remainingTime <= 0) return "0";

        long percent = (remainingTime * 100) / totalDuration;

        return String.valueOf(percent);
    }

    public static String getFormattedRemainingBooster(Player player) {
        long seconds = getRemainingBoosterTime(player);

        if (seconds <= 0) {
            return "0s";
        }

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder formattedTime = new StringBuilder();
        if (days > 0) formattedTime.append(days).append("d ");
        if (hours > 0) formattedTime.append(hours).append("h ");
        if (minutes > 0) formattedTime.append(minutes).append("m ");
        if (secs > 0 || formattedTime.length() == 0) formattedTime.append(secs).append("s");

        return formattedTime.toString().trim();
    }




}
