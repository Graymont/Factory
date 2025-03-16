package org.factory.factory.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import static org.factory.factory.Database.GetItem;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.Utils.PersistentDataManager.GetNamespacedKey;

public class CraftingManager implements Listener {

    public static void InitRecipes() {
        Material[] woodenTools = {
                Material.WOODEN_PICKAXE,
                Material.WOODEN_AXE,
                Material.WOODEN_SHOVEL,
                Material.WOODEN_HOE,
                Material.WOODEN_SWORD
        };

        Material[] stoneTools = {
                Material.STONE_PICKAXE,
                Material.STONE_AXE,
                Material.STONE_SHOVEL,
                Material.STONE_HOE,
                Material.STONE_SWORD
        };

        Material[] ironTools = {
                Material.IRON_PICKAXE,
                Material.IRON_AXE,
                Material.IRON_SHOVEL,
                Material.IRON_HOE,
                Material.IRON_SWORD
        };

        Material[] goldenTools = {
                Material.GOLDEN_PICKAXE,
                Material.GOLDEN_AXE,
                Material.GOLDEN_SHOVEL,
                Material.GOLDEN_HOE,
                Material.GOLDEN_SWORD
        };

        Material[] diamondTools = {
                Material.DIAMOND_PICKAXE,
                Material.DIAMOND_AXE,
                Material.DIAMOND_SHOVEL,
                Material.DIAMOND_HOE,
                Material.DIAMOND_SWORD
        };

        Material[] netheriteTools = {
                Material.NETHERITE_PICKAXE,
                Material.NETHERITE_AXE,
                Material.NETHERITE_SHOVEL,
                Material.NETHERITE_HOE,
                Material.NETHERITE_SWORD
        };

        Material[] plankTypes = {
                Material.OAK_PLANKS,
                Material.SPRUCE_PLANKS,
                Material.BIRCH_PLANKS,
                Material.JUNGLE_PLANKS,
                Material.ACACIA_PLANKS,
                Material.DARK_OAK_PLANKS,
                Material.CHERRY_PLANKS,
                Material.BAMBOO_PLANKS,
                Material.MANGROVE_PLANKS
        };


        for (Material tool : woodenTools) {
            int variant = 1;
            for (Material plank : plankTypes) {
                String recipeKey = tool.toString().toLowerCase().replaceAll("_", "").trim();
                ItemStack result = GetItem(recipeKey);

                ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft(recipeKey+"_"+variant), result);

                // Set shape based on tool type
                if (tool.name().contains("PICKAXE")) {
                    recipe.shape("PPP", " S ", " S ");
                } else if (tool.name().contains("AXE")) {
                    recipe.shape("PP ", "PS ", " S ");
                } else if (tool.name().contains("SHOVEL")) {
                    recipe.shape(" P ", " S ", " S ");
                } else if (tool.name().contains("HOE")) {
                    recipe.shape("PP ", " S ", " S ");
                } else if (tool.name().contains("SWORD")) {
                    recipe.shape(" P ", " P ", " S ");
                }

                recipe.setIngredient('P', plank);
                recipe.setIngredient('S', Material.STICK);
                Bukkit.getServer().addRecipe(recipe);
                variant++;
            }

        }

        for (Material tool : stoneTools) {
            String recipeKey = tool.toString().toLowerCase().replaceAll("_", "").trim();
            ItemStack result = GetItem(recipeKey);

            ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft(recipeKey), result);

            // Set shape based on tool type
            if (tool.name().contains("PICKAXE")) {
                recipe.shape("PPP", " S ", " S ");
            } else if (tool.name().contains("AXE")) {
                recipe.shape("PP ", "PS ", " S ");
            } else if (tool.name().contains("SHOVEL")) {
                recipe.shape(" P ", " S ", " S ");
            } else if (tool.name().contains("HOE")) {
                recipe.shape("PP ", " S ", " S ");
            } else if (tool.name().contains("SWORD")) {
                recipe.shape(" P ", " P ", " S ");
            }

            recipe.setIngredient('P', Material.COBBLESTONE);
            recipe.setIngredient('S', Material.STICK);
            Bukkit.getServer().addRecipe(recipe);
        }

        for (Material tool : ironTools) {
            String recipeKey = tool.toString().toLowerCase().replaceAll("_", "").trim();
            ItemStack result = GetItem(recipeKey);

            ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft(recipeKey), result);

            // Set shape based on tool type
            if (tool.name().contains("PICKAXE")) {
                recipe.shape("PPP", " S ", " S ");
            } else if (tool.name().contains("AXE")) {
                recipe.shape("PP ", "PS ", " S ");
            } else if (tool.name().contains("SHOVEL")) {
                recipe.shape(" P ", " S ", " S ");
            } else if (tool.name().contains("HOE")) {
                recipe.shape("PP ", " S ", " S ");
            } else if (tool.name().contains("SWORD")) {
                recipe.shape(" P ", " P ", " S ");
            }

            recipe.setIngredient('P', Material.IRON_INGOT);
            recipe.setIngredient('S', Material.STICK);
            Bukkit.getServer().addRecipe(recipe);
        }

        for (Material tool : goldenTools) {
            String recipeKey = tool.toString().toLowerCase().replaceAll("_", "").trim();
            ItemStack result = GetItem(recipeKey);

            ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft(recipeKey), result);

            // Set shape based on tool type
            if (tool.name().contains("PICKAXE")) {
                recipe.shape("PPP", " S ", " S ");
            } else if (tool.name().contains("AXE")) {
                recipe.shape("PP ", "PS ", " S ");
            } else if (tool.name().contains("SHOVEL")) {
                recipe.shape(" P ", " S ", " S ");
            } else if (tool.name().contains("HOE")) {
                recipe.shape("PP ", " S ", " S ");
            } else if (tool.name().contains("SWORD")) {
                recipe.shape(" P ", " P ", " S ");
            }

            recipe.setIngredient('P', Material.GOLD_INGOT);
            recipe.setIngredient('S', Material.STICK);
            Bukkit.getServer().addRecipe(recipe);
        }

        for (Material tool : diamondTools) {
            String recipeKey = tool.toString().toLowerCase().replaceAll("_", "").trim();
            ItemStack result = GetItem(recipeKey);

            ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft(recipeKey), result);

            // Set shape based on tool type
            if (tool.name().contains("PICKAXE")) {
                recipe.shape("PPP", " S ", " S ");
            } else if (tool.name().contains("AXE")) {
                recipe.shape("PP ", "PS ", " S ");
            } else if (tool.name().contains("SHOVEL")) {
                recipe.shape(" P ", " S ", " S ");
            } else if (tool.name().contains("HOE")) {
                recipe.shape("PP ", " S ", " S ");
            } else if (tool.name().contains("SWORD")) {
                recipe.shape(" P ", " P ", " S ");
            }

            recipe.setIngredient('P', Material.DIAMOND);
            recipe.setIngredient('S', Material.STICK);
            Bukkit.getServer().addRecipe(recipe);
        }

        for (Material tool : netheriteTools) {
            String recipeKey = tool.toString().toLowerCase().replaceAll("_", "").trim();
            ItemStack result = GetItem(recipeKey);

            ShapedRecipe recipe = new ShapedRecipe(NamespacedKey.minecraft(recipeKey), result);

            // Set shape based on tool type
            if (tool.name().contains("PICKAXE")) {
                recipe.shape("PPP", " S ", " S ");
            } else if (tool.name().contains("AXE")) {
                recipe.shape("PP ", "PS ", " S ");
            } else if (tool.name().contains("SHOVEL")) {
                recipe.shape(" P ", " S ", " S ");
            } else if (tool.name().contains("HOE")) {
                recipe.shape("PP ", " S ", " S ");
            } else if (tool.name().contains("SWORD")) {
                recipe.shape(" P ", " P ", " S ");
            }

            recipe.setIngredient('P', Material.NETHERITE_INGOT);
            recipe.setIngredient('S', Material.STICK);
            Bukkit.getServer().addRecipe(recipe);
        }
    }

    public static void InitSmeltings(){
        ItemStack copperingot = new ItemStack(GetItem("copperingot"));
        FurnaceRecipe copperingotrecipe = new FurnaceRecipe(GetNamespacedKey("copperingotkey"), copperingot, Material.RAW_COPPER, 0f, 60);
        Bukkit.addRecipe(copperingotrecipe);

        ItemStack ironingot = new ItemStack(GetItem("ironingot"));
        FurnaceRecipe ironingotrecipe = new FurnaceRecipe(GetNamespacedKey("ironingotkey"), ironingot, Material.RAW_IRON, 0f, 100);
        Bukkit.addRecipe(ironingotrecipe);

        ItemStack goldingot = new ItemStack(GetItem("goldingot"));
        FurnaceRecipe goldingotrecipe = new FurnaceRecipe(GetNamespacedKey("goldingotkey"), goldingot, Material.RAW_GOLD, 0f, 200);
        Bukkit.addRecipe(goldingotrecipe);

        ItemStack copperingot2 = new ItemStack(GetItem("copperingot"));
        FurnaceRecipe copperingotrecipe2 = new FurnaceRecipe(GetNamespacedKey("copperingotkey2"), copperingot2, Material.COPPER_ORE, 0f, 60);
        Bukkit.addRecipe(copperingotrecipe2);

        ItemStack ironingot2 = new ItemStack(GetItem("ironingot"));
        FurnaceRecipe ironingotrecipe2 = new FurnaceRecipe(GetNamespacedKey("ironingotkey2"), ironingot2, Material.IRON_ORE, 0f, 100);
        Bukkit.addRecipe(ironingotrecipe2);

        ItemStack goldingot2 = new ItemStack(GetItem("goldingot"));
        FurnaceRecipe goldingotrecipe2 = new FurnaceRecipe(GetNamespacedKey("goldingotkey2"), goldingot2, Material.GOLD_ORE, 0f, 200);
        Bukkit.addRecipe(goldingotrecipe2);
    }
}
