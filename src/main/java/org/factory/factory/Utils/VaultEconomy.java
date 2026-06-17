package org.factory.factory.Utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getServer;
import static org.factory.factory.Utils.UserInterface.*;

public class VaultEconomy {

    public static Economy econ = null;

    public static String icon = "⛃";

    public static void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        econ = rsp.getProvider();
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Double GetPlayerBalance(Player player){
        return getEconomy().getBalance(player);
    }

    public static void AddPlayerBalance(Player player, double amount){
        getEconomy().depositPlayer(player, amount);

        if (amount > 0){
            player.sendMessage(sendRgbText("+"+FormatDouble(amount)+icon, color_brightGreenAcid));
        }
    }

    public static void RemovePlayerBalance(Player player, double amount){
        getEconomy().withdrawPlayer(player, amount);
        player.sendMessage(sendRgbText("-"+FormatDouble(amount)+icon, color_darkRed));
    }

    // test record 1 2 3

}
