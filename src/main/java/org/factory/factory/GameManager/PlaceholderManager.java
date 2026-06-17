package org.factory.factory.GameManager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static org.factory.factory.Events.*;
import static org.factory.factory.GameHandler.Booster.*;
import static org.factory.factory.GameManager.CooldownManager.getGlobalFormattedRemainingTime;
import static org.factory.factory.GameHandler.FactoryEvents.currentEvent;
import static org.factory.factory.GameHandler.PlayerProgress.*;
import static org.factory.factory.Utils.UserInterface.*;

public class PlaceholderManager extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "factory";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Graymontt";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";

        if (identifier.equalsIgnoreCase("level")) {
            return String.valueOf(playerLevel.get(player.getUniqueId()));
        }

        if (identifier.equalsIgnoreCase("prestige")) {
            return String.valueOf(intToRoman(playerPrestige.get(player.getUniqueId())));
        }

        if (identifier.equalsIgnoreCase("exp")) {
            return String.valueOf(FormatDouble(playerExp.get(player.getUniqueId())));
        }

        if (identifier.equalsIgnoreCase("maxexp")) {
            return String.valueOf(FormatDouble(maxExp.get(playerLevel.get(player.getUniqueId()))));
        }

        if (identifier.equalsIgnoreCase("totalmachine")) {
            return String.valueOf(machineCount.get(player.getUniqueId()));
        }

        if (identifier.equalsIgnoreCase("maxmachine")) {
            return String.valueOf(maxMachines.get(player.getUniqueId()));
        }
        if (identifier.equalsIgnoreCase("extramaxmachine")) {
            return String.valueOf(extraMaxMachines.get(player.getUniqueId()));
        }

        if (identifier.equalsIgnoreCase("events")) {
            return String.valueOf(formatItemName(currentEvent.toString()));
        }

        if (identifier.equalsIgnoreCase("eventstimer")) {
            return String.valueOf(getGlobalFormattedRemainingTime(CooldownManager.CooldownType.FactoryEvents));
        }

        if (identifier.equalsIgnoreCase("sellmultiplier")) {
            return String.valueOf(FormatDouble(playerSellMultiplier.get(player.getUniqueId())));
        }

        if (identifier.equalsIgnoreCase("expmultiplier")) {
            return String.valueOf(FormatDouble(playerExpMultiplier.get(player.getUniqueId())));
        }

        if (identifier.equalsIgnoreCase("activebooster")) {
            return String.valueOf(getFormattedBoosterName(boosters.get(player.getUniqueId())));
        }

        if (identifier.equalsIgnoreCase("boosterduration")) {
            return String.valueOf(getFormattedRemainingBooster(player));
        }

        if (identifier.equalsIgnoreCase("remainingbooster")) {
            return String.valueOf(getPercentRemainingBooster(player));
        }

        return null;
    }
}
