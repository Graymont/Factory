package org.factory.factory.Utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

import static org.factory.factory.Events.*;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.Factory.luckPerms;
import static org.factory.factory.GameHandler.PlayerProgress.playerExpMultiplier;
import static org.factory.factory.GameHandler.PlayerProgress.playerSellMultiplier;

public class Benefit {

    public static void ManageBenefit(Player player){
        playerMaxAcid.put(player.getUniqueId(), defaultMaxAcid);
        playerSellMultiplier.put(player.getUniqueId(), 1.0);
        playerExpMultiplier.put(player.getUniqueId(), 1.0);
        extraMaxMachines.put(player.getUniqueId(), 0);
        if (player.hasPermission("factory.benefit.adventurer")){
            playerMaxAcid.put(player.getUniqueId(), 3500);

            playerSellMultiplier.put(player.getUniqueId(), 1.1);
            playerExpMultiplier.put(player.getUniqueId(), 1.1);
            extraMaxMachines.put(player.getUniqueId(), 1);
        }

        if (player.hasPermission("factory.benefit.knight")){
            playerMaxAcid.put(player.getUniqueId(), 3700);

            playerSellMultiplier.put(player.getUniqueId(), 1.2);
            playerExpMultiplier.put(player.getUniqueId(), 1.2);
            extraMaxMachines.put(player.getUniqueId(), 5);
        }

        if (player.hasPermission("factory.benefit.champion")){
            playerMaxAcid.put(player.getUniqueId(), 3900);

            playerSellMultiplier.put(player.getUniqueId(), 1.3);
            playerExpMultiplier.put(player.getUniqueId(), 1.3);
            extraMaxMachines.put(player.getUniqueId(), 10);
        }




        if (player.hasPermission("factory.benefit.hero")){
            playerMaxAcid.put(player.getUniqueId(), 4100);

            playerSellMultiplier.put(player.getUniqueId(), 1.4);
            playerExpMultiplier.put(player.getUniqueId(), 1.4);
            extraMaxMachines.put(player.getUniqueId(), 15);
        }

        if (player.hasPermission("factory.benefit.legend")){
            playerMaxAcid.put(player.getUniqueId(), 4300);

            playerSellMultiplier.put(player.getUniqueId(), 1.5);
            playerExpMultiplier.put(player.getUniqueId(), 1.5);
            extraMaxMachines.put(player.getUniqueId(), 20);
        }

        if (player.hasPermission("factory.benefit.mythic")){
            playerMaxAcid.put(player.getUniqueId(), 4500);

            playerSellMultiplier.put(player.getUniqueId(), 1.6);
            playerExpMultiplier.put(player.getUniqueId(), 1.6);
            extraMaxMachines.put(player.getUniqueId(), 25);
        }




        if (player.hasPermission("factory.benefit.titan")){
            playerMaxAcid.put(player.getUniqueId(), 4700);

            playerSellMultiplier.put(player.getUniqueId(), 1.7);
            playerExpMultiplier.put(player.getUniqueId(), 1.7);
            extraMaxMachines.put(player.getUniqueId(), 30);
        }

        if (player.hasPermission("factory.benefit.immortal")){
            playerMaxAcid.put(player.getUniqueId(), 4900);

            playerSellMultiplier.put(player.getUniqueId(), 1.8);
            playerExpMultiplier.put(player.getUniqueId(), 1.8);
            extraMaxMachines.put(player.getUniqueId(), 35);
        }

        if (player.hasPermission("factory.benefit.oracle")){
            playerMaxAcid.put(player.getUniqueId(), 5100);

            playerSellMultiplier.put(player.getUniqueId(), 1.9);
            playerExpMultiplier.put(player.getUniqueId(), 1.9);
            extraMaxMachines.put(player.getUniqueId(), 40);
        }



        if (player.hasPermission("factory.benefit.celestial")){
            playerMaxAcid.put(player.getUniqueId(), 5300);

            playerSellMultiplier.put(player.getUniqueId(), 2.0);
            playerExpMultiplier.put(player.getUniqueId(), 2.0);
            extraMaxMachines.put(player.getUniqueId(), 45);
        }

        if (player.hasPermission("factory.benefit.ascendant")){
            playerMaxAcid.put(player.getUniqueId(), 5500);

            playerSellMultiplier.put(player.getUniqueId(), 2.1);
            playerExpMultiplier.put(player.getUniqueId(), 2.1);
            extraMaxMachines.put(player.getUniqueId(), 50);
        }

        if (player.hasPermission("factory.benefit.demigod")){
            playerMaxAcid.put(player.getUniqueId(), 5700);

            playerSellMultiplier.put(player.getUniqueId(), 2.2);
            playerExpMultiplier.put(player.getUniqueId(), 2.2);
            extraMaxMachines.put(player.getUniqueId(), 55);
        }



        if (player.hasPermission("factory.benefit.seraphim")){
            playerMaxAcid.put(player.getUniqueId(), 5900);

            playerSellMultiplier.put(player.getUniqueId(), 2.3);
            playerExpMultiplier.put(player.getUniqueId(), 2.3);
            extraMaxMachines.put(player.getUniqueId(), 60);
        }

        if (player.hasPermission("factory.benefit.gensuhu")){
            playerMaxAcid.put(player.getUniqueId(), 6100);

            playerSellMultiplier.put(player.getUniqueId(), 2.4);
            playerExpMultiplier.put(player.getUniqueId(), 2.4);
            extraMaxMachines.put(player.getUniqueId(), 65);
        }

        if (player.hasPermission("factory.benefit.gensone")){
            playerMaxAcid.put(player.getUniqueId(), 6300);

            playerSellMultiplier.put(player.getUniqueId(), 2.5);
            playerExpMultiplier.put(player.getUniqueId(), 2.5);
            extraMaxMachines.put(player.getUniqueId(), 70);
        }

    }

}
