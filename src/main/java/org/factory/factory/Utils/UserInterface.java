package org.factory.factory.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.awt.*;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getServer;
import static org.factory.factory.Utils.CooldownManager.*;
import static org.factory.factory.Utils.VaultEconomy.icon;

public class UserInterface {

    public static String checkSymbol = "✔";
    public static String xSymbol = "✘";

    public static String usageArrowSymbol = "➤";

    // color

    public static String color_darkGreenAcid = "#538E1F";
    public static String color_brightGreenAcid = "#6EDA10";

    public static String color_darkRed = "#A4162D";

    //

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

    public static String intToRoman(int number) {
        if (number <= 0 || number > 3999) {
            return "-";
        }

        String[] romans = {
                "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"
        };
        int[] values = {
                1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1
        };

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                number -= values[i];
                result.append(romans[i]);
            }
        }
        return result.toString();
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
        Inventory gui = Bukkit.createInventory(new CustomInventoryHolder(null), size*9, sendText("&n"+name));

        return gui;
    }

    public static Inventory OpenChest(Player p, int size, String name) {
        Inventory gui = Bukkit.createInventory(p, size*9, sendText("&n"+name));

        return gui;
    }

    public static Inventory OpenBackpack(int size, String name) {
        Inventory gui = Bukkit.createInventory(new BackpackHolder(null), size*9, sendText("&n"+name));

        return gui;
    }

    public static String Notification_NoMoney(Player player, double amount){
        PlaySoundAt(Sound.ENTITY_VILLAGER_NO, player.getLocation(), 1, 1);

        return sendText("&4You don't have enough money! &c(you need "+FormatDouble(amount)+icon+" more!)");
    }

    public static String Notification_NoItem(Player player){
        PlaySoundAt(Sound.ENTITY_VILLAGER_NO, player.getLocation(), 1, 1);

        return sendText("&4You don't have enough ingredient!");
    }

    public static String Notification_InventoryFull(Player player){
        PlaySoundAt(Sound.BLOCK_ENDER_CHEST_OPEN, player.getLocation(), 1, 0);

        return sendText("&4Your inventory is full!");
    }

    public static String Notification_NoSteam(Player player){
        PlaySoundAt(Sound.BLOCK_MUD_BRICKS_PLACE, player.getLocation(), 1, 0);

        return sendText("&4You don't have enough steam!");
    }

    public static void Notification_ItemBroken(Player player){
        PlaySoundAt(Sound.ENTITY_ITEM_BREAK, player.getLocation(), 1, 0);
        player.sendTitle(" ", sendText("&cYour item is broken!"));
        player.sendMessage(sendText("&4Your item is broken!"));
    }

    public static String Notification_NoLevel(Player player){
        return sendText("&4Your level is too low!");
    }

    public static String Notification_HasCooldown(Player player, CooldownManager.CooldownType type){
        return sendText("&4Still on cooldown! &c("+getFormattedRemainingTime(player, type)+" left)");
    }

    public static String formatItemName(String itemName) {
        String[] words = itemName.split("_"); // Split by underscore
        StringBuilder formattedName = new StringBuilder();

        for (String word : words) {
            formattedName.append(word.substring(0, 1).toUpperCase()) // Capitalize first letter
                    .append(word.substring(1).toLowerCase()) // Lowercase rest
                    .append(" "); // Add space
        }

        return formattedName.toString().trim(); // Trim trailing space
    }

    public static String GenerateSerialCode(){
        return UUID.randomUUID().toString();
    }

    public static String GenerateShortSerialCode() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
    }

    public static String GetDurabilityPercent(double durability, double maxDurability){
        double totalPercent = durability/maxDurability*100;

        if (totalPercent == 0){
            return sendText(" &8[ &4"+FormatDouble(totalPercent)+"% &8]");
        }

        if (totalPercent <= 5){
            return sendText(" &8[ &5"+FormatDouble(totalPercent)+"% &8]");
        }
        else if (totalPercent <= 25){
            return sendText(" &8[ &c"+FormatDouble(totalPercent)+"% &8]");
        }
        else if (totalPercent <= 35){
            return sendText(" &8[ &6"+FormatDouble(totalPercent)+"% &8]");
        }
        else if (totalPercent <= 45){
            return sendText(" &8[ &e"+FormatDouble(totalPercent)+"% &8]");
        }
        else if (totalPercent <= 55){
            return sendText(" &8[ &2"+FormatDouble(totalPercent)+"% &8]");
        }

        return sendText(" &8[ &a"+FormatDouble(totalPercent)+"% &8]");
    }

    public static String sendRgbText(String text, String hex) {
        return net.md_5.bungee.api.ChatColor.of(hex)+sendText(text);
    }

    public static String LocationDisplay(Location location){
        return sendText("x: "+location.getBlockX()+" y:"+location.getBlockY()+" z:"+location.getBlockZ());
    }

    public static void Broadcast(String text){
        for (Player player : Bukkit.getOnlinePlayers()){
            player.sendMessage(sendText(text));
        }
    }

    public static String getFormattedTime(long seconds) {
        if (seconds <= 0) {
            return "0s";
        }

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder formattedTime = new StringBuilder();

        if (days > 0) {
            formattedTime.append(days).append(" ").append(days == 1 ? "day " : "days ");
        }
        if (hours > 0) {
            formattedTime.append(hours).append(" ").append(hours == 1 ? "hour " : "hours ");
        }
        if (minutes > 0) {
            formattedTime.append(minutes).append(" ").append(minutes == 1 ? "minute " : "minutes ");
        }
        if (secs > 0 || formattedTime.length() == 0) {
            formattedTime.append(secs).append(" ").append(secs == 1 ? "second" : "seconds");
        }

        return formattedTime.toString().trim();
    }

    public static void AnnouncePayment(Player player, String text, String price){

        Broadcast("&8[&6!&8] &e"+player.getName()+" &fhas bought &6x1 "+text+" &ffrom &6&lCredit Shop &ffor &e"+price+" credits!");

    }

    public static void PlayParticleAtBlock(Block block, Particle particle) {
        Location loc = block.getLocation().add(0, 0.5, 0);
        block.getWorld().spawnParticle(particle, loc, 10);
    }

    public static void SpawnBlockCrackParticle(Block block) {
        Location loc = block.getLocation().add(0.5, 1.0, 0.5); // top surface

        BlockData data = block.getBlockData(); // block appearance
        block.getWorld().spawnParticle(
                Particle.BLOCK,
                loc,
                20,       // count
                0.3, 0, 0.3, // offset x/y/z
                data      // material to simulate breaking
        );
    }


    public static void SpawnBlockRedstoneParticle(Block block, Color color) {
        Location loc = block.getLocation().add(0.5, 1.0, 0.5); // top surface

        Particle.DustOptions dust = new Particle.DustOptions(color, 1.0F); // Color and size
        block.getWorld().spawnParticle(
                Particle.DUST,
                loc,
                20,       // count
                0.3, 0, 0.3, // offset x/y/z
                0,
                dust
        );
    }

    public static void SendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut){
        player.sendTitle(sendText(title), sendText(subtitle), fadeIn*20, stay*20, fadeOut*20);
    }

}
