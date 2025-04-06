package org.factory.factory.Utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static org.factory.factory.Events.machineCount;
import static org.factory.factory.Events.maxMachines;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.Utils.CooldownManager.getGlobalFormattedRemainingTime;
import static org.factory.factory.Utils.CooldownManager.getGlobalRemainingTime;
import static org.factory.factory.Utils.FactoryEvents.currentEvent;
import static org.factory.factory.Utils.PlayerProgress.*;
import static org.factory.factory.Utils.UserInterface.formatItemName;

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

        if (identifier.equalsIgnoreCase("exp")) {
            return String.valueOf(playerExp.get(player.getUniqueId()));
        }

        if (identifier.equalsIgnoreCase("maxexp")) {
            return String.valueOf(maxExp.get(playerLevel.get(player.getUniqueId())));
        }

        if (identifier.equalsIgnoreCase("totalmachine")) {
            return String.valueOf(machineCount.get(player.getUniqueId()));
        }

        if (identifier.equalsIgnoreCase("maxmachine")) {
            return String.valueOf(maxMachines.get(player.getUniqueId()));
        }

        if (identifier.equalsIgnoreCase("events")) {
            return String.valueOf(formatItemName(currentEvent.toString()));
        }

        if (identifier.equalsIgnoreCase("eventstimer")) {
            return String.valueOf(getGlobalFormattedRemainingTime(CooldownManager.CooldownType.FactoryEvents));
        }

        return null;
    }
}
