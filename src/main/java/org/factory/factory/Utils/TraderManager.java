package org.factory.factory.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.factory.factory.Database.GetItem;
import static org.factory.factory.Utils.FactoryItem.ProcessItemMeta;
import static org.factory.factory.Utils.UserInterface.formatItemName;
import static org.factory.factory.Utils.UserInterface.sendText;

public class TraderManager {

    public enum TraderType{
        None,
        Dungeon_Loot_Box,
        Dungeon_Junk_Collector,
        Dungeon_Loot_Master,
        Quarter_Master;

        public static TraderType parseTrader(String type){
            return switch (type.toLowerCase()){

                case "dungeon_loot_box" -> TraderType.Dungeon_Loot_Box;
                case "dungeon_junk_collector" -> TraderType.Dungeon_Junk_Collector;
                case "dungeon_loot_master" -> TraderType.Dungeon_Loot_Master;
                case "quarter_master" -> TraderType.Quarter_Master;

                default -> TraderType.None;
            };
        }
    }


    public static void OpenTrader(Player player, TraderType traderType){
        if (traderType == TraderType.Dungeon_Loot_Master){
            Merchant merchant = Bukkit.createMerchant(sendText("&nLoot Master"));

            List<MerchantRecipe> trades = new ArrayList<>();

            List<String> itemLootTypes = Arrays.asList("weapon", "equipment");

            int tier = 1;
            int count = 0;
            for (int i = 25; i < 115; i += 5) {
                for (String lootType : itemLootTypes){
                    MerchantRecipe merchantRecipe = new MerchantRecipe(new ItemStack(GetItem(lootType+"dungeonlootbox"+i)), 9999);

                    ItemStack questPendant = new ItemStack(new ItemStack(GetItem("questpendanttier"+tier)));
                    ItemStack dungeonPendant = new ItemStack(new ItemStack(GetItem("dungeonpendanttier"+tier)));

                    questPendant.setAmount(5+(tier*2));
                    dungeonPendant.setAmount(3+(tier*2));

                    merchantRecipe.addIngredient(questPendant);
                    merchantRecipe.addIngredient(dungeonPendant);

                    trades.add(merchantRecipe);
                }
                count++;

                if (count % 2 == 0) {
                    tier++;
                }

                if (tier > 10) break;
            }

            merchant.setRecipes(trades);
            player.openMerchant(merchant, true);
        }

        else if (traderType == TraderType.Dungeon_Junk_Collector){
            Merchant merchant = Bukkit.createMerchant(sendText("&nJunk Collector"));

            List<MerchantRecipe> trades = new ArrayList<>();
            List<String> junkList = Arrays.asList("alien", "mutant", "undead");

            for (int i = 1; i < 11; i++) {
                for (String junkType : junkList){
                    MerchantRecipe merchantRecipe = new MerchantRecipe(new ItemStack(GetItem("dungeonpendanttier"+i)), 9999);
                    ItemStack membrane = new ItemStack(new ItemStack(GetItem(junkType+"membrane"+i)));
                    membrane.setAmount(48);
                    merchantRecipe.addIngredient(membrane);
                    trades.add(merchantRecipe);
                }
            }

            merchant.setRecipes(trades);
            player.openMerchant(merchant, true);
        }

        else if (traderType == TraderType.Quarter_Master){
            Merchant merchant = Bukkit.createMerchant(sendText("&nQuarter Master"));

            List<MerchantRecipe> trades = new ArrayList<>();

            MerchantRecipe merchantRecipe = new MerchantRecipe(new ItemStack(GetItem("sharpnetheritesword")), 9999);
            ItemStack ingot = new ItemStack(ProcessItemMeta(new ItemStack(Material.NETHERITE_INGOT, 20)));
            merchantRecipe.addIngredient(ingot);
            trades.add(merchantRecipe);

            merchant.setRecipes(trades);
            player.openMerchant(merchant, true);
        }
    }

}
