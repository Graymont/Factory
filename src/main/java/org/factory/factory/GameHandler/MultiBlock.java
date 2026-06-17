package org.factory.factory.GameHandler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.factory.factory.Database.GetItem;
import static org.factory.factory.GameHandler.FactoryItem.*;
import static org.factory.factory.GameHandler.FactoryMachine.serialCodeKey;
import static org.factory.factory.GameManager.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.UserInterface.GenerateSerialCode;
import static org.factory.factory.Utils.UserInterface.sendText;

public class MultiBlock {

    public static boolean canProduceAcid(Block block){
        Location blockLocation = block.getLocation();

        for (BlockFace face : BlockFace.values()){
            if (block.getRelative(face).getType() == Material.MOSSY_COBBLESTONE_WALL){
                return true;
            }
        }

        return false;
    }

    public static boolean isAcidMaker(Block block){
        Location blockLocation = block.getLocation();

        if (block.getType() == Material.BREWING_STAND){
            if (block.getRelative(BlockFace.UP).getType() == Material.MOSSY_COBBLESTONE_WALL){
                return true;
            }
        }

        return false;
    }

    public static boolean isCarbonForge(Block block){
        Location blockLocation = block.getLocation();

        if (block.getType() == Material.FURNACE){
            if (block.getRelative(BlockFace.UP).getType() == Material.ANVIL){
                return true;
            }
        }

        return false;
    }

    public static boolean isArmorCrafter(Block block){
        Location blockLocation = block.getLocation();

        if (block.getType() == Material.DISPENSER){
            if (block.getRelative(BlockFace.UP).getType() == Material.ANVIL){
                return true;
            }
        }

        return false;
    }

    public static boolean isNetherSmelter(Block block){
        Location blockLocation = block.getLocation();

        if (block.getType() == Material.BLAST_FURNACE){
            if (block.getRelative(BlockFace.DOWN).getType() == Material.FIRE){
                return true;
            }
        }

        return false;
    }

    public static void ObtainMultiBlockGuide(Player player){
        player.getInventory().addItem(GetMultiBlockGuide());
        player.sendMessage(sendText("&aObtained &bMulti Block Guide! &3(right-click to view)"));
    }

    public static ItemStack GetMultiBlockGuide(){
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        meta.addEnchant(Enchantment.UNBREAKING, 10, true);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        PersistentDataContainer container = meta.getPersistentDataContainer();

        meta.setDisplayName(sendText("&bMultiBlock Guide"));

        List<String> itemLore = new ArrayList<>();
        itemLore.add(sendText("&9Miscellaneous"));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7One structure, limitless potential"));
        itemLore.add(sendText(" "));
        itemLore.add(sendText("&8➤ &7Right-Click to view"));

        meta.setLore(itemLore);

        container.set(GetNamespacedKey("multiBlockGuide"), PersistentDataType.BOOLEAN, true);
        container.set(GetNamespacedKey(serialCodeKey), PersistentDataType.STRING, GenerateSerialCode());

        item.setItemMeta(meta);

        return item;
    }



    public static String[] carbonMaterials = {
            // Crops & Fruits
            "wheat", "barley", "corn", "carrot", "potato", "beetroot", "whiteonion", "redonion",
            "lettuce", "cabbage", "broccoli", "cauliflower", "radish", "cucumber", "greenbeans",
            "eggplant", "chilipepper", "apple", "banana", "orange", "grape", "melon", "pumpkin",
            "strawberry", "blueberry", "blackberry", "kiwi", "lemon", "peach", "papaya", "pineapple",
            "mango",

            // Mushrooms
            "purplemushroom", "redmushroom", "bluemushroom", "brownmushroom", "greenmushroom",
            "pinkmushroom", "yellowmushroom",

            // Logs
            "cherrylog", "birchlog", "crimsonstem", "junglelog", "mangrovelog", "sprucelog",
            "warpedstem", "oaklog", "acacialog", "darkoaklog",

            // Leaves
            "oakleaves", "pinkleaves", "purpleleaves", "redleaves", "wisterialeaves", "fallleaves",
            "autumnleaves", "azalealeaves", "acacialeaves", "spruceleaves", "sakuraleaves",
            "birchleaves", "cherryleaves", "jungleleaves", "mangroveleaves",

            // Minerals & Gems
            "limestone", "marble", "basalt", "slate", "obsidian", "quartz", "granite", "coal",
            "copper", "tin", "iron", "lead", "aluminium", "silver", "gold", "platinum",

            // Crystals & Gems
            "amethyst", "garnet", "topaz", "jade", "aquamarine", "sapphire", "ruby", "emerald",
            "diamond", "netherite",


            // Powerful mineral
            "tungsten", "palladium", "cobalt",
            "mithril", "orichalcum", "titanium",
            "adamantine", "dragonite", "voidsteel", "etherium"
    };



    public static void OpenCarbonForge(Player player, String material) {
        Merchant merchant = Bukkit.createMerchant(sendText("&nCarbon Forge"));
        List<MerchantRecipe> trades = new ArrayList<>();

        // Machine Material Names (order preserved, no "_custom")

        // Armor Types & Required Materials
        String[] armorTypes = {"helmet", "chestplate", "leggings", "boots"};
        int[] materialCosts = {32, 64, 50, 25}; // Helmet = 5, Chestplate = 8, Leggings = 7, Boots = 4


        for (int i = 0; i < armorTypes.length; i++) {
            String armorType = armorTypes[i];
            int requiredAmount = materialCosts[i];

            // Create armor item as result
            ItemStack armorItem = GetItem("carbon"+material + armorType);

            // Create required material item
            ItemStack materialItem = GetItem("carbon"+material);
            materialItem.setAmount(requiredAmount);

            // Create trade
            MerchantRecipe trade = new MerchantRecipe(armorItem, 9999);
            trade.addIngredient(materialItem);
            trades.add(trade);
        }


        merchant.setRecipes(trades);
        player.openMerchant(merchant, true);
    }

    public static void OpenArmorCrafter(Player player) {
        Merchant merchant = Bukkit.createMerchant(sendText("&nArmor Crafter"));
        List<MerchantRecipe> trades = new ArrayList<>();

        // Machine Material Names (order preserved, no "_custom")
        String[] materials = {
                "leather", "chainmail", "iron", "golden", "diamond", "netherite"
        };

        // Armor Types & Required Materials
        String[] armorTypes = {"helmet", "chestplate", "leggings", "boots"};
        int[] materialCosts = {5, 8, 7, 4}; // Helmet = 5, Chestplate = 8, Leggings = 7, Boots = 4

        for (String material : materials) {
            for (int i = 0; i < armorTypes.length; i++) {
                String armorType = armorTypes[i];
                int requiredAmount = materialCosts[i];

                // Create armor item as result
                ItemStack armorItem = GetItem(material + armorType);

                // Create required material item
                ItemStack materialItem = ProcessItemMeta(new ItemStack(Material.LEATHER));

                if (material.contains("leather")){
                    materialItem = ProcessItemMeta(new ItemStack(Material.LEATHER));
                }
                else if (material.contains("chainmail")){
                    materialItem = ProcessItemMeta(new ItemStack(Material.CHAIN));
                }
                else if (material.contains("diamond")){
                    materialItem = ProcessItemMeta(new ItemStack(Material.DIAMOND));
                }
                else if (material.contains("golden")){
                    materialItem = ProcessItemMeta(new ItemStack(Material.GOLD_INGOT));
                }
                else{
                    String materialKey = material+"_ingot";
                    materialItem = ProcessItemMeta(new ItemStack(Material.getMaterial(materialKey.toUpperCase())));
                }

                materialItem.setAmount(requiredAmount);

                // Create trade
                MerchantRecipe trade = new MerchantRecipe(armorItem, 9999);
                trade.addIngredient(materialItem);
                trades.add(trade);
            }
        }

        merchant.setRecipes(trades);
        player.openMerchant(merchant, true);
    }


    public static void OpenNetherSmelter(Player player) {
        Merchant merchant = Bukkit.createMerchant(sendText("&nNether Smelter"));
        List<MerchantRecipe> trades = new ArrayList<>();

        List<String> materialList = Arrays.asList("tungsten", "palladium", "cobalt", "mithril", "orichalcum", "titanium", "adamantine", "dragonite",
                "voidsteel", "etherium");

        int roll = 0;
        int amount = 3;
        for (String mat : materialList){
            MerchantRecipe merchantRecipe = new MerchantRecipe(new ItemStack(GetItem(mat+"ingot")), 9999);
            if (roll == 0){
                merchantRecipe.addIngredient(new ItemStack(ProcessItemMeta(new ItemStack(Material.NETHERITE_INGOT, amount))));
            }else{
                ItemStack ingredient = new ItemStack(GetItem(materialList.get(roll-1)+"ingot"));
                ingredient.setAmount(amount);
                merchantRecipe.addIngredient(ingredient);
            }
            trades.add(merchantRecipe);
            roll++;
            amount += 5;
        }

        merchant.setRecipes(trades);
        player.openMerchant(merchant, true);
    }

    public static void OpenAcidMaker(Player player){
        Merchant merchant = Bukkit.createMerchant(sendText("&nAcid Maker"));
        List<MerchantRecipe> trades = new ArrayList<>();

        ItemStack waterBottle = new ItemStack(GetPotion(PotionType.WATER));
        waterBottle = new ItemStack(ProcessItemMeta(waterBottle));
        ItemStack acid = new ItemStack(GetItem("acid"));
        acid.setAmount(8);

        List<ItemStack> sugarIngredientList = Arrays.asList(
                new ItemStack(ProcessItemMeta(new ItemStack(Material.SUGAR_CANE, 4))),
                new ItemStack(ProcessItemMeta(new ItemStack(Material.CACTUS, 8))),
                new ItemStack(ProcessItemMeta(new ItemStack(Material.BAMBOO, 10))),
                new ItemStack(ProcessItemMeta(new ItemStack(Material.KELP, 5))));

        int startSugarAmount = 16;
        for (int i = 0; i < sugarIngredientList.size(); i++) {
            ItemStack sugar = new ItemStack(ProcessItemMeta(new ItemStack(Material.SUGAR, 10)));
            MerchantRecipe sugarRecipe = new MerchantRecipe(sugar, 9999);
            sugar.setAmount(startSugarAmount);
            if (i > 0){
                sugarRecipe.addIngredient(sugarIngredientList.get(i));
                sugarRecipe.addIngredient(sugarIngredientList.get(i-1));
            }else{
                sugarRecipe.addIngredient(sugarIngredientList.get(i));
            }
            trades.add(sugarRecipe);
            startSugarAmount+=4;
        }

        ItemStack sugar = new ItemStack(ProcessItemMeta(new ItemStack(Material.SUGAR, 10)));
        sugar.setAmount(4);
        MerchantRecipe acidRecipe = new MerchantRecipe(acid, 9999);
        acidRecipe.addIngredient(waterBottle);
        acidRecipe.addIngredient(sugar);

        trades.add(acidRecipe);

        merchant.setRecipes(trades);
        player.openMerchant(merchant, true);
    }


}
