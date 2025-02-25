package org.factory.factory.Utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getServer;
import static org.factory.factory.Utils.VaultEconomy.icon;

public class UserInterface {

    public static String checkSymbol = "✔";
    public static String xSymbol = "✘";

    public static String sendText(String text) {
        return text.replaceAll("&", "§");
    }

    public static void sendClickableLink(Player player, String url, String message) {
        // Create the text component with the message text
        TextComponent textComponent = new TextComponent(message);

        // Set the click event to open a URL
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

        // Send the message to the player
        player.spigot().sendMessage(textComponent);
    }

    public static void executeConsoleCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public static void SendActionBar(Player p, String m) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(m));
    }

    public static void PlaySound(Sound s, Entity e, float volume, float pitch) {
        e.getWorld().playSound(e.getLocation(), s, volume, pitch);
    }

    public static void PlaySoundAt(Sound sound, Location location, float volume, float pitch) {
        if (location.getWorld() != null) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }
    }

    public static String uncolouredText(String text) {
        return text.replaceAll("§.|[^\\x00-\\x7F]|\\d+|[^a-zA-Z_ ]", "").trim();
    }

    public static String numberInText(String text) {
        String cleaned = text.replaceAll("§.|[^\\x00-\\x7F]", "").trim();
        // Replace text
        String pattern = "\\d+";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(cleaned);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            // Append matched substring to result
            result.append(matcher.group());
        }
        return result.toString().trim();
    }

    public static void consoleLog(String message) {
        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private static final String defaultFormat = "#,###.##";

    public static String FormatDouble(double d){
        DecimalFormat formatter = new DecimalFormat(defaultFormat);
        return formatter.format(d);

    }

    public static Inventory OpenGUI(Player p, int size, String name) {
        Inventory gui = Bukkit.createInventory(new CustomInventoryHolder(null), size*9, name);

        return gui;
    }

    public static String Notification_NoMoney(Player player, double amount){
        PlaySoundAt(Sound.ENTITY_VILLAGER_NO, player.getLocation(), 1, 1);

        return sendText("&4You don't have enough money! &c(you need "+FormatDouble(amount)+icon+" more!)");
    }

}
