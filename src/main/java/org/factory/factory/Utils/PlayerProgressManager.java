package org.factory.factory.Utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.factory.factory.Events.*;
import static org.factory.factory.Utils.FactoryItem.*;
import static org.factory.factory.Utils.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.PlayerProgress.AddExp;
import static org.factory.factory.Utils.PlayerProgress.hasLevel;
import static org.factory.factory.Utils.QuestManager.TriggerFishingQuest;
import static org.factory.factory.Utils.UserInterface.*;

public class PlayerProgressManager implements Listener {

    public static HashMap<Location, Boolean> placedBlocks = new HashMap<>();

    public static double GetBlockValue(Block block){
        if (block.getType() != Material.AIR){
            if (block.getType() == Material.DIORITE){
                return 1;
            }
            else if (block.getType() == Material.ANDESITE){
                return 1;
            }
            else if (block.getType() == Material.GRANITE){
                return 1;
            }
            else if (block.getType() == Material.COAL_ORE){
                return 5;
            }
            else if (block.getType() == Material.COPPER_ORE){
                return 8;
            }
            else if (block.getType() == Material.LAPIS_ORE){
                return 8;
            }
            else if (block.getType() == Material.REDSTONE_ORE){
                return 8;
            }
            else if (block.getType() == Material.IRON_ORE){
                return 12;
            }
            else if (block.getType() == Material.GOLD_ORE){
                return 18;
            }
            else if (block.getType() == Material.DIAMOND_ORE){
                return 20;
            }
            else if (block.getType() == Material.EMERALD_ORE){
                return 25;
            }
            else if (block.getType() == Material.ANCIENT_DEBRIS){
                return 35;
            }
            else if (block.getType() == Material.NETHER_QUARTZ_ORE){
                return 10;
            }

            else if (block.getType() == Material.OAK_LOG){
                return 1;
            }
            else if (block.getType() == Material.SPRUCE_LOG){
                return 2;
            }
            else if (block.getType() == Material.BIRCH_LOG){
                return 3;
            }
            else if (block.getType() == Material.ACACIA_LOG){
                return 5;
            }
            else if (block.getType() == Material.DARK_OAK_LOG){
                return 7;
            }
            else if (block.getType() == Material.MANGROVE_LOG){
                return 8;
            }
            else if (block.getType() == Material.CHERRY_LOG){
                return 9;
            }
            else if (block.getType() == Material.CRIMSON_STEM){
                return 10;
            }
            else if (block.getType() == Material.WARPED_STEM){
                return 11;
            }
        }
        return 0;
    }

    public static double GetFishValue(ItemStack item){

        if (item != null){
            if (item.getType() == Material.COD){
                return 10;
            }
            else if (item.getType() == Material.SALMON){
                return 15;
            }
            else if (item.getType() == Material.TROPICAL_FISH){
                return 20;
            }
            else if (item.getType() == Material.PUFFERFISH){
                return 25;
            }
        }

        return 0;
    }

    public static double GetMobValue(Entity entity){

        if (entity instanceof Mob){
            if (entity instanceof Zombie){
                return 10;
            }
            else if (entity instanceof Skeleton){
                return 15;
            }
            else if (entity instanceof Spider){
                return 20;
            }
            else if (entity instanceof Creeper){
                return 25;
            }
            else if (entity instanceof Enderman){
                return 30;
            }
            else if (entity instanceof Blaze){
                return 30;
            }

            else if (entity instanceof Pig){
                return 5;
            }
            else if (entity instanceof Chicken){
                return 7;
            }
            else if (entity instanceof Sheep){
                return 8;
            }
            else if (entity instanceof Cow){
                return 10;
            }
            else if (entity instanceof Rabbit){
                return 15;
            }
        }

        return 0;
    }

    @EventHandler
    public void OnBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR){
            return;
        }

        if (GetBlockValue(block) > 0){
            if (placedBlocks.get(block.getLocation()) == null){

                PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

                if (!ItemNotBroken(item)){
                    return;
                }

                if (!hasLevel(player, container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER))){
                    return;
                }

                if (isPickaxe(item)){
                    if (item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)){
                        return;
                    }
                }

                AddExp(player, GetBlockValue(block));
                PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, block.getLocation(), 1, 1);
            }else{
                placedBlocks.remove(block.getLocation());
            }
        }
    }

    @EventHandler
    public void OnBlockPlace(BlockPlaceEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR){
            if (GetBlockValue(block) > 0){
                placedBlocks.put(block.getLocation(), true);
            }
        }
    }

    @EventHandler
    public void EntityDeathEvent(EntityDeathEvent event){
        Entity entity = event.getEntity();
        Player player = event.getEntity().getKiller();
        assert player != null;
        assert entity instanceof Mob;
        if (GetMobValue(entity) > 0){
            AddExp(player, GetMobValue(entity));
            PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, entity.getLocation(), 1, 1);
        }
    }


    @EventHandler
    public void OnJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        ResetFishing(player);
    }

    public static void ResetFishing(Player player){
        isFishing.put(player, false);
        fishHealth.put(player, 0);
        fishingPower.put(player, 0);
        fishingItem.put(player, new ItemStack(Material.STICK));
    }


    public static HashMap<Player, Boolean> isFishing = new HashMap<>();
    public static HashMap<Player, Integer> fishHealth = new HashMap<>();
    public static HashMap<Player, Integer> fishingPower = new HashMap<>();
    public static HashMap<Player, ItemStack> fishingItem = new HashMap<>();

    @EventHandler
    public void OnFishing(PlayerFishEvent event){
        Player player = event.getPlayer();

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH){
            event.setCancelled(true);
            ProcessFishingState(player);
        }

        else if (event.getState() == PlayerFishEvent.State.REEL_IN){
            if (isFishing.get(player)){
                if (!player.isSneaking()){
                    event.setCancelled(true);
                    player.sendMessage(sendText("&cSneak+Right-Click &4to cancel fishing!"));
                }else{
                    isFishing.put(player, false);
                }
            }
        }
    }

    public static void ProcessFishingState(Player player){
        if (!isFishing.get(player)){
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!isFishingRod(item)){
                return;
            }

            if (!isPlayerInRegion(player, "lake")){
                player.sendMessage(sendText("&4You must be in a lake to catch a fish! &6type /warp (select -> 'lake')"));
                return;
            }

            ManageDurability(player, "hand");
            UpdateItem(player, "hand", item);

            double fishingPowerRetriever = 1;
            Double fishingPowerContainer = container.get(GetNamespacedKey(toolPowerKey), PersistentDataType.DOUBLE);
            if (fishingPowerContainer != null){
                fishingPowerRetriever = fishingPowerContainer;
            }
            int _fishingPower = (int) fishingPowerRetriever;


            isFishing.put(player, true);
            player.sendTitle(sendText("&e\uD83D\uDC1F BITE!"), "");

            ItemStack fishGet = new ItemStack(Material.COD);

            Random random = new Random();
            int randomFish = random.nextInt(100)+1;
            if (randomFish <= 20){
                fishGet.setType(Material.PUFFERFISH);
            }
            else if (randomFish <= 30){
                fishGet.setType(Material.TROPICAL_FISH);
            }
            else if (randomFish <= 50){
                fishGet.setType(Material.SALMON);
            }else {
                fishGet.setType(Material.COD);
            }

            fishGet.setItemMeta(ProcessItemMeta(fishGet).getItemMeta());

            fishHealth.put(player, (int) GetFishValue(fishGet));
            fishingItem.put(player, fishGet);
            fishingPower.put(player, _fishingPower);

            PlaySoundAt(Sound.BLOCK_NOTE_BLOCK_PLING, player.getLocation(), 0, 0);
            PlaySoundAt(Sound.BLOCK_GLASS_BREAK, player.getLocation(), 1, 1);
        }
    }

    public static void TriggerFishing(Player player){
        if (isFishing.get(player)){
            int _fishHealth = fishHealth.get(player);
            int _fishingPower = fishingPower.get(player);
            if (_fishHealth > 0){
                _fishHealth -= _fishingPower;
                fishHealth.put(player, _fishHealth);

                player.swingHand(EquipmentSlot.HAND);
                player.sendTitle(sendText("&bFishing..."), sendText("&f[ &3"+_fishHealth+" &f]"));
                PlaySoundAt(Sound.ENTITY_FISHING_BOBBER_RETRIEVE, player.getLocation(), 1, 1);
            }

            else {
                isFishing.put(player, false);
                AddExp(player, GetFishValue(fishingItem.get(player)));
                Map<Integer, ItemStack> addedItem = player.getInventory().addItem(fishingItem.get(player));
                if (!addedItem.isEmpty()){
                    DropItem(player.getLocation(), fishingItem.get(player), 1);
                    player.sendMessage(sendText("&eYour inventory is full, the fish has been dropped..."));
                    PlaySoundAt(Sound.ENTITY_ITEM_PICKUP, player.getLocation(), 1, 1);
                }

                TriggerFishingQuest(player, fishingItem.get(player));

                player.sendTitle(sendText("&aCaught!"), sendText("&2+&f"+fishingItem.get(player).getItemMeta().getDisplayName()));
                PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, player.getLocation(), 1, 1);
            }
        }
    }

}
