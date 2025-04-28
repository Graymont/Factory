package org.factory.factory.Utils;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class CooldownManager {

    public static final HashMap<String, Long> cooldowns = new HashMap<>();

    public enum CooldownType {
        None,
        Hourly,
        Daily,
        Weekly,
        Monthly,

        Hazmat,
        Hook,

        Miner_1,
        Miner_2,
        Miner_3,
        Miner_4,
        Miner_5,
        Hunter_1,
        Hunter_2,
        Hunter_3,
        Hunter_4,
        Hunter_5,
        Fisherman_1,
        Fisherman_2,
        Fisherman_3,
        Fisherman_4,
        Fisherman_5,

        FactoryEvents;

        public static CooldownType parseCooldown(String r) {
            return switch (r.toLowerCase()) {
                case "hourly" -> Hourly;
                case "daily" -> Daily;
                case "weekly" -> Weekly;
                case "monthly" -> Monthly;

                case "hazmat" -> Hazmat;
                case "hook" -> Hook;

                case "miner_1" -> Miner_1;
                case "miner_2" -> Miner_2;
                case "miner_3" -> Miner_3;
                case "miner_4" -> Miner_4;
                case "miner_5" -> Miner_5;

                case "hunter_1" -> Hunter_1;
                case "hunter_2" -> Hunter_2;
                case "hunter_3" -> Hunter_3;
                case "hunter_4" -> Hunter_4;
                case "hunter_5" -> Hunter_5;

                case "fisherman_1" -> Fisherman_1;
                case "fisherman_2" -> Fisherman_2;
                case "fisherman_3" -> Fisherman_3;
                case "fisherman_4" -> Fisherman_4;
                case "fisherman_5" -> Fisherman_5;

                case "factoryevents" -> FactoryEvents;

                default -> None;
            };
        }
    }

    public static void SetCooldown(Player player, CooldownType type, int seconds) {
        String key = player.getName() + "_" + type.toString();
        cooldowns.put(key, System.currentTimeMillis() + (seconds * 1000L));
    }

    public static boolean hasCooldown(Player player, CooldownType type) {
        String key = player.getName() + "_" + type.toString();
        return cooldowns.getOrDefault(key, 0L) > System.currentTimeMillis();
    }

    public static long getRemainingTime(Player player, CooldownType type) {
        String key = player.getName() + "_" + type.toString();
        return Math.max(0, (cooldowns.getOrDefault(key, 0L) - System.currentTimeMillis()) / 1000);
    }

    public static void ResetCooldown(Player player, CooldownType type) {
        String key = player.getName() + "_" + type.toString();
        cooldowns.remove(key);
    }

    public static String getFormattedRemainingTime(Player player, CooldownType type) {
        long seconds = getRemainingTime(player, type);

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


    // global
    public static void SetGlobalCooldown(CooldownType type, int seconds) {
        String key = type.toString();
        cooldowns.put(key, System.currentTimeMillis() + (seconds * 1000L));
    }

    public static boolean hasGlobalCooldown(CooldownType type) {
        String key = type.toString();
        return cooldowns.getOrDefault(key, 0L) > System.currentTimeMillis();
    }

    public static long getGlobalRemainingTime(CooldownType type) {
        String key = type.toString();
        return Math.max(0, (cooldowns.getOrDefault(key, 0L) - System.currentTimeMillis()) / 1000);
    }

    public static void ResetGlobalCooldown(CooldownType type) {
        String key = type.toString();
        cooldowns.remove(key);
    }

    public static String getGlobalFormattedRemainingTime(CooldownType type) {
        long seconds = getGlobalRemainingTime(type);

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
