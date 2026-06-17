package org.factory.factory.GameManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.factory.factory.GameHandler.FactoryEvents;

import java.util.*;

import static org.factory.factory.Events.*;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.GameHandler.FactoryEvents.currentEvent;
import static org.factory.factory.GameHandler.FactoryItem.*;
import static org.factory.factory.GameHandler.FactoryMob.GetEntityDungeonTier;
import static org.factory.factory.GameManager.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.GameHandler.PlayerProgress.*;
import static org.factory.factory.GameManager.QuestManager.TriggerFishingQuest;
import static org.factory.factory.Utils.UserInterface.*;

public class PlayerProgressManager implements Listener {

    public static HashMap<Location, Boolean> placedBlocks = new HashMap<>();

    public static double GetBlockValue(Player player, Block block) {
        Random random = new Random();

        Location location = block.getLocation();

        double getExp = 0.0;
        int dropAmount = 1;
        ItemStack dropItem = new ItemStack(ProcessItemMeta(new ItemStack(block.getType())));

        Double miningFortune = playerAttributes.get(player.getName()+".attribute.total.miningfortune");
        Double foragingFortune = playerAttributes.get(player.getName()+".attribute.total.foragingfortune");

        if (block.getType() == Material.DIORITE) {
            getExp = 1;
        } else if (block.getType() == Material.ANDESITE) {
            getExp = 1;
        } else if (block.getType() == Material.GRANITE) {
            getExp = 1;
        }
        else if (block.getType() == Material.GRAVEL) {
            dropItem = new ItemStack(ProcessItemMeta(new ItemStack(Material.FLINT)));
            getExp = 2;
        }
        else if (block.getType() == Material.SAND) {
            dropItem = new ItemStack(ProcessItemMeta(new ItemStack(Material.GLOWSTONE_DUST)));
            getExp = 3;
        }
        else if (block.getType() == Material.RED_SAND) {
            dropItem = new ItemStack(ProcessItemMeta(new ItemStack(Material.GLOWSTONE_DUST)));
            getExp = 4;
        }
        else if (block.getType() == Material.COAL_ORE) {
            dropItem = new ItemStack(ProcessItemMeta(new ItemStack(Material.COAL)));
            getExp = 5;
        } else if (block.getType() == Material.COPPER_ORE) {
            dropItem = new ItemStack(ProcessItemMeta(new ItemStack(Material.RAW_COPPER)));
            getExp = 8;
        } else if (block.getType() == Material.LAPIS_ORE) {
            dropAmount = random.nextInt(3) + 1;
            dropItem = new ItemStack(ProcessItemMeta(new ItemStack(Material.LAPIS_LAZULI)));
            getExp = 8;
        } else if (block.getType() == Material.REDSTONE_ORE) {
            dropAmount = random.nextInt(3) + 1;
            dropItem = new ItemStack(ProcessItemMeta(new ItemStack(Material.REDSTONE)));
            getExp = 8;
        } else if (block.getType() == Material.IRON_ORE) {
            dropItem = new ItemStack(ProcessItemMeta(new ItemStack(Material.RAW_IRON)));
            getExp = 12;
        } else if (block.getType() == Material.GOLD_ORE) {
            dropItem = new ItemStack(ProcessItemMeta(new ItemStack(Material.RAW_GOLD)));
            getExp = 18;
        } else if (block.getType() == Material.DIAMOND_ORE) {
            dropItem = new ItemStack(ProcessItemMeta(new ItemStack(Material.DIAMOND)));
            getExp = 20;
        } else if (block.getType() == Material.EMERALD_ORE) {
            dropItem = new ItemStack(ProcessItemMeta(new ItemStack(Material.EMERALD)));
            getExp = 25;
        } else if (block.getType() == Material.ANCIENT_DEBRIS) {
            getExp = 35;
        } else if (block.getType() == Material.NETHER_QUARTZ_ORE) {
            dropAmount = random.nextInt(3) + 1;
            dropItem = new ItemStack(ProcessItemMeta(new ItemStack(Material.QUARTZ)));
            getExp = 10;
        } else if (block.getType() == Material.OAK_LOG) {
            getExp = 1;
        } else if (block.getType() == Material.SPRUCE_LOG) {
            getExp = 2;
        } else if (block.getType() == Material.BIRCH_LOG) {
            getExp = 3;
        } else if (block.getType() == Material.ACACIA_LOG) {
            getExp = 5;
        } else if (block.getType() == Material.DARK_OAK_LOG) {
            getExp = 7;
        } else if (block.getType() == Material.MANGROVE_LOG) {
            getExp = 8;
        } else if (block.getType() == Material.CHERRY_LOG) {
            getExp = 9;
        } else if (block.getType() == Material.CRIMSON_STEM) {
            getExp = 10;
        } else if (block.getType() == Material.WARPED_STEM) {
            getExp = 11;
        }

        /*else if (block.getType() == Material.WHEAT){
            getExp = 1;
        }
        else if (block.getType() == Material.BEETROOTS){
            getExp = 2;
        }
        else if (block.getType() == Material.CARROTS){
            getExp = 3;
        }
        else if (block.getType() == Material.POTATOES){
            getExp = 4;
        }
        else if (block.getType() == Material.MELON){
            getExp = 5;
        }
        else if (block.getType() == Material.PUMPKIN){
            getExp = 6;
        }*/

        if (getExp > 0) {

            int randomFortuneDrop = random.nextInt(100)+1;

            int fortuneChance = 5;
            if (miningFortune > 0){
                fortuneChance = (int) (fortuneChance + miningFortune);
            }
            else if (foragingFortune > 0){
                fortuneChance = (int) (fortuneChance + foragingFortune);
            }

            if (randomFortuneDrop <= fortuneChance){
                if (!block.getType().toString().contains("LOG") && !block.getType().toString().contains("WOOD")){
                    if (miningFortune > 0){
                        dropAmount = random.nextInt((int) ((dropAmount*2)+miningFortune))+dropAmount;
                    }
                }

                else if (block.getType().toString().contains("LOG") || block.getType().toString().contains("WOOD")){
                    if (foragingFortune > 0){
                        dropAmount = random.nextInt((int) ((dropAmount*2)+foragingFortune))+dropAmount;
                    }
                }

                RandomGetAcid(player);

                PlaySoundAt(Sound.ENTITY_ENDER_DRAGON_FLAP, block.getLocation(), 0.5f, 3);
                PlaySoundAt(Sound.BLOCK_NOTE_BLOCK_FLUTE, block.getLocation(), 0.5f, 3);
            }

            ItemStack finalDropItem = new ItemStack(dropItem);
            finalDropItem.setAmount(dropAmount);

            location.getWorld().dropItemNaturally(location, finalDropItem);
        }
        return getExp;
    }

    public static double GetFishValue(ItemStack item) {

        if (item != null) {
            if (item.getType() == Material.COD) {
                return 10;
            } else if (item.getType() == Material.SALMON) {
                return 15;
            } else if (item.getType() == Material.TROPICAL_FISH) {
                return 20;
            } else if (item.getType() == Material.PUFFERFISH) {
                return 25;
            }
        }

        return 0;
    }

    public static double GetMobValue(Entity entity) {

        if (entity instanceof LivingEntity) {
            if (entity instanceof Mob) {
                if (entity instanceof Zombie) {
                    return 10 + GetMobLevel(entity);
                } else if (entity instanceof Skeleton) {
                    return 15 + GetMobLevel(entity);
                } else if (entity instanceof Spider) {
                    return 20 + GetMobLevel(entity);
                } else if (entity instanceof Creeper) {
                    return 25 + GetMobLevel(entity);
                } else if (entity instanceof Enderman) {
                    return 30 + GetMobLevel(entity);
                } else if (entity instanceof Blaze) {
                    return 40 + GetMobLevel(entity);
                } else if (entity instanceof Guardian) {
                    return 50 + GetMobLevel(entity);
                } else if (entity instanceof Pig) {
                    return 5 + GetMobLevel(entity);
                } else if (entity instanceof Chicken) {
                    return 7 + GetMobLevel(entity);
                } else if (entity instanceof Sheep) {
                    return 8 + GetMobLevel(entity);
                } else if (entity instanceof Cow) {
                    return 10 + GetMobLevel(entity);
                } else if (entity instanceof Rabbit) {
                    return 15 + GetMobLevel(entity);
                }
            }

        }

        return 0;
    }

    public static void RandomGetAcid(Player player){
        Random random = new Random();
        int randomGet = random.nextInt(100)+1;
        if (randomGet <= 20){
            AddAcid(player, 1, false);
            SendTitle(player, sendText(" "), sendText("&a+1 "+acidSymbol), 1, 1, 1);
            PlaySoundAt(Sound.ENTITY_SLIME_DEATH_SMALL, player.getLocation(), 1, 0);
        }
    }

    @EventHandler
    public void OnBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (event.isCancelled()) {
            return;
        }

        if (item.getType() == Material.AIR) {
            return;
        }

        if (!isBlockInRegion(block, "mine")) {
            return;
        }

        if (placedBlocks.get(block.getLocation()) == null) {

            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

            Double proficiency = container.get(GetNamespacedKey(proficiencyKey), PersistentDataType.DOUBLE);
            if (proficiency == null) {
                proficiency = 0.0;
            }

            /*Double fortune = container.get(GetNamespacedKey(miningFortuneKey), PersistentDataType.DOUBLE);
            if (fortune == null) {
                fortune = 0.0;
            }
            if (isPickaxe(item)){
                playerAttributes.put(player.getName()+".attribute.total.miningfortune", fortune);
            }
            else if (isAxe(item)){
                playerAttributes.put(player.getName()+".attribute.total.foragingfortune", fortune);
            }*/

            if (!ItemNotBroken(item)) {
                return;
            }

            if (!hasLevel(player, container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER))) {
                return;
            }

            if (isPickaxe(item)) {
                if (item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)) {
                    return;
                }
            }

            double gain = GetBlockValue(player, block);

            /*consoleLog(" ");
            consoleLog("Block Value : "+GetBlockValue(block));
            consoleLog("Proficiency : "+proficiency);
            consoleLog("Total Gain  : "+gain);*/

            if (gain > 0) {
                //event.setCancelled(true);
                //block.setType(Material.AIR);

                event.setDropItems(false);
                event.setExpToDrop(0);

                double totalGain = AddExp(player, gain, proficiency);
                //TriggerMiningQuest(player, block);
                spawnExpHologram(block.getLocation(), totalGain);
                PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, block.getLocation(), 1, 1);
            }

        } else {
            placedBlocks.remove(block.getLocation());
        }

    }

    @EventHandler
    public void OnBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR) {
            placedBlocks.put(block.getLocation(), true);
        }
    }

    @EventHandler
    public void EntityDeathEvent(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (event.getEntity().getKiller() == null) {
            return;
        }

        Player player = event.getEntity().getKiller();
        assert player != null;
        assert entity instanceof Mob;

        ItemStack item = player.getInventory().getItemInMainHand();

        if (GetMobValue(entity) > 0) {
            Double proficiency = 0.0;

            if (item.getType() != Material.AIR) {
                PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

                proficiency = container.get(GetNamespacedKey(proficiencyKey), PersistentDataType.DOUBLE);
                if (proficiency == null) {
                    proficiency = 0.0;
                }
            }

            double gain = GetMobValue(entity);

            int currentLevel = playerLevel.get(player.getUniqueId());
            int mobLevel = GetMobLevel(entity);

            if (GetEntityDungeonTier(entity.getLocation()) > 0) {
                /*if (currentLevel-mobLevel > 2){
                    gain = (gain*0.7);
                }
                if (currentLevel-mobLevel > 5){
                    gain = (gain*0.5);
                }*/
                if (currentLevel - mobLevel > 15) {
                    gain = (gain * 0.8);
                }
                if (currentLevel - mobLevel >= 20) {
                    gain = (gain * 0.5);
                }
            }

            double totalGain = AddExp(player, gain, proficiency);
            if (gain > 0) {
                spawnExpHologram(entity.getLocation(), totalGain);
                PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, entity.getLocation(), 1, 1);
            }
        }
    }


    @EventHandler
    public void OnJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ResetFishing(player);
    }

    public static void ResetFishing(Player player) {
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
    public void OnFishing(PlayerFishEvent event) {
        Player player = event.getPlayer();

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            event.setCancelled(true);
            ProcessFishingState(player);
        } else if (event.getState() == PlayerFishEvent.State.REEL_IN) {
            if (event.getState() != PlayerFishEvent.State.IN_GROUND) {
                if (isFishing.get(player)) {
                    if (!player.isSneaking()) {
                        event.setCancelled(true);
                        player.sendMessage(sendText("&cSneak+Right-Click &4to cancel fishing!"));
                    } else {
                        isFishing.put(player, false);
                    }
                }
            }
        }
    }

    public static void ProcessFishingState(Player player) {
        if (!isFishing.get(player)) {
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!isFishingRod(item)) {
                return;
            }

            if (!isPlayerInRegion(player, "lake")) {
                player.sendMessage(sendText("&4You must be in a lake to catch a fish! &6type /warp (select -> 'lake')"));
                return;
            }


            double fishingPowerRetriever = 1;
            Double fishingPowerContainer = container.get(GetNamespacedKey(toolPowerKey), PersistentDataType.DOUBLE);
            if (fishingPowerContainer != null) {
                fishingPowerRetriever = fishingPowerContainer;
            }
            int _fishingPower = (int) fishingPowerRetriever;


            isFishing.put(player, true);
            player.sendTitle(sendText("&e\uD83D\uDC1F BITE!"), "");

            ItemStack fishGet = new ItemStack(Material.COD);

            Random random = new Random();
            int randomFish = random.nextInt(100) + 1;
            if (randomFish <= 20) {
                fishGet.setType(Material.PUFFERFISH);
            } else if (randomFish <= 30) {
                fishGet.setType(Material.TROPICAL_FISH);
            } else if (randomFish <= 50) {
                fishGet.setType(Material.SALMON);
            } else {
                fishGet.setType(Material.COD);
            }

            fishGet.setItemMeta(ProcessItemMeta(fishGet).getItemMeta());

            fishHealth.put(player, (int) GetFishValue(fishGet));
            fishingItem.put(player, fishGet);
            fishingPower.put(player, _fishingPower);

            PlaySoundAt(Sound.BLOCK_NOTE_BLOCK_PLING, player.getLocation(), 0, 0);
            PlaySoundAt(Sound.BLOCK_GLASS_BREAK, player.getLocation(), 1, 1);

            if (currentEvent != FactoryEvents.EventType.Invincible_Items) {
                ManageDurability(player, "hand");
            }
            UpdateItem(player, "hand", item);
        }
    }

    public static void TriggerFishing(Player player) {
        if (isFishing.get(player)) {
            int _fishHealth = fishHealth.get(player);
            int _fishingPower = fishingPower.get(player);
            if (_fishHealth > 0) {
                _fishHealth -= _fishingPower;
                fishHealth.put(player, _fishHealth);

                player.swingHand(EquipmentSlot.HAND);
                player.sendTitle(sendText("&bFishing..."), sendText("&f[ &3" + _fishHealth + " &f]"));
                PlaySoundAt(Sound.ENTITY_FISHING_BOBBER_RETRIEVE, player.getLocation(), 1, 1);
            } else {
                isFishing.put(player, false);

                Double proficiency = 0.0;
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() != Material.AIR) {
                    PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
                    proficiency = container.get(GetNamespacedKey(proficiencyKey), PersistentDataType.DOUBLE);
                    if (proficiency == null) {
                        proficiency = 0.0;
                    }
                }

                double gain = GetFishValue(fishingItem.get(player));

                double totalGain = AddExp(player, gain, proficiency);
                Map<Integer, ItemStack> addedItem = player.getInventory().addItem(fishingItem.get(player));
                if (!addedItem.isEmpty()) {
                    DropItem(player.getLocation(), fishingItem.get(player), 1);
                    player.sendMessage(sendText("&eYour inventory is full, the fish has been dropped..."));
                    PlaySoundAt(Sound.ENTITY_ITEM_PICKUP, player.getLocation(), 1, 1);
                }

                TriggerFishingQuest(player, fishingItem.get(player));

                if (gain > 0) {
                    spawnExpHologram(player.getEyeLocation(), totalGain);
                }

                player.sendTitle(sendText("&aCaught!"), sendText("&2+&f" + fishingItem.get(player).getItemMeta().getDisplayName()));
                PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, player.getLocation(), 1, 1);
            }
        }
    }


    // farming manager
    public static boolean isCropBlock(Block block) {
        return block.getType() == Material.WHEAT || block.getType() == Material.CARROTS
                || block.getType() == Material.POTATOES || block.getType() == Material.BEETROOTS
                || block.getType() == Material.MELON_STEM || block.getType() == Material.PUMPKIN_STEM
                || block.getType() == Material.COCOA || block.getType() == Material.NETHER_WART;
    }

    public static boolean isCropItem(ItemStack item) {

        if (item == null) {
            return false;
        }

        if (item.getType() == Material.AIR) {
            return false;
        }

        return item.getType() == Material.WHEAT || item.getType() == Material.WHEAT_SEEDS || item.getType() == Material.CARROT
                || item.getType() == Material.POTATO || item.getType() == Material.BEETROOT || item.getType() == Material.BEETROOT_SEEDS
                || item.getType() == Material.MELON_SEEDS || item.getType() == Material.PUMPKIN_SEEDS || item.getType() == Material.COCOA_BEANS;
    }


    public static boolean isFruit(Block block) {
        return block.getType() == Material.MELON || block.getType() == Material.PUMPKIN;
    }


    @EventHandler
    public void OnFarming(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!event.isCancelled()) {
            if (getRegionByLocation(block.getLocation()).contains("farm")) {
                if (isCropBlock(block)) {
                    event.setCancelled(true);
                    ManageFarm(player, block);
                    return;
                } else {
                    if (!player.hasPermission("admin")) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            if (isCropBlock(block)) {
                event.setCancelled(true);
                PlayerFarming(player, item, block);
            } else if (isFruit(block)) {
                event.setCancelled(true);
                PlayerFarming(player, item, block);
            }
        }
    }

    @EventHandler
    public void OnFarminInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        assert block != null;
        /*if (getRegionByLocation(block.getLocation()).contains("farm")){
            if (isCropBlock(block)){
                event.setCancelled(true);
                ManageFarm(player, block);
                return;
            }
        }*/
        if (hasIslandAccess(player)) {
            if (isHoe(item)) {
                if (isFruit(block)) {
                    PlayerFarming(player, item, block);
                    event.setCancelled(true);
                } else {
                    if (block.getType() == Material.COCOA) {
                        event.setCancelled(true);
                        PlayerFarming(player, item, block);
                    }
                }
            }
        }
    }

    public static void PlayerFarming(Player player, ItemStack item, Block block) {
        boolean useHoe = isHoe(item);

        if (useHoe) {
            if (!ItemNotBroken(item)) {
                Notification_ItemBroken(player);
                return;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return;
            }

            PersistentDataContainer container = meta.getPersistentDataContainer();

            if (!hasLevel(player, container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER))) {
                player.sendMessage(Notification_NoLevel(player));
                return;
            }

            ManageDurability(player, "hand");
            UpdateItem(player, "hand", item);
        }

        Location location = block.getLocation();
        Material type = block.getType();
        BlockData data = block.getBlockData();

        if (data instanceof Ageable ageable) {

            if (type == Material.PUMPKIN_STEM || type == Material.MELON_STEM) {
                if (useHoe) {
                    return;
                }
            }

            if (ageable.getAge() != ageable.getMaximumAge()) {
                if (!useHoe) {
                    block.setType(Material.AIR);
                }
                return;
            }
        } else {
            switch (type) {
                case PUMPKIN -> replantCrop(player, block, Material.AIR, new ItemStack(Material.PUMPKIN));
                case MELON -> replantCrop(player, block, Material.AIR, new ItemStack(Material.MELON_SLICE));
            }
            PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, location, 1, 1);
            return;
        }

        switch (type) {
            case WHEAT -> replantCrop(player, block, Material.WHEAT, new ItemStack(Material.WHEAT));
            case CARROTS -> replantCrop(player, block, Material.CARROTS, new ItemStack(Material.CARROT));
            case POTATOES -> replantCrop(player, block, Material.POTATOES, new ItemStack(Material.POTATO));
            case BEETROOTS -> replantCrop(player, block, Material.BEETROOTS, new ItemStack(Material.BEETROOT));
            case COCOA -> replantCrop(player, block, Material.COCOA, new ItemStack(Material.COCOA_BEANS));
            case NETHER_WART -> replantCrop(player, block, Material.NETHER_WART, new ItemStack(Material.NETHER_WART));
            case MELON_STEM, PUMPKIN_STEM -> replantCrop(player, block, Material.AIR, new ItemStack(Material.AIR));
        }

        PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, location, 1, 1);
    }

    private static void replantCrop(Player player, Block block, Material material, ItemStack drop) {
        Location location = block.getLocation();

        if (drop.getType() != Material.AIR) {
            block.getWorld().dropItem(location, new ItemStack(ProcessItemMeta(new ItemStack(drop.getType(), 1))));
        }

        if (drop.getType().toString().contains("WHEAT")) {
            block.getWorld().dropItem(location, new ItemStack(ProcessItemMeta(new ItemStack(Material.WHEAT_SEEDS, 1))));
        }
        if (drop.getType().toString().contains("BEETROOT")) {
            block.getWorld().dropItem(location, new ItemStack(ProcessItemMeta(new ItemStack(Material.BEETROOT_SEEDS, 1))));
        }

        BlockData newData = block.getBlockData();
        if (newData instanceof Ageable ageable) {
            ageable.setAge(0);
            block.setBlockData(ageable);
            ManageCropExp(player, block);
            if (block.getType() == Material.MELON_STEM || block.getType() == Material.PUMPKIN_STEM) {
                block.setType(material);
            }
        } else {
            if (isFruit(block)) {
                if (placedBlocks.get(block.getLocation()) != null) {
                    placedBlocks.remove(block.getLocation());
                    block.setType(material);
                } else {
                    ManageCropExp(player, block);
                    block.setType(material);
                }
            }
        }
    }

    public static double GetCropValue(Block block) {
        double getExp = 0;

        if (block == null) {
            return getExp;
        }

        if (block.getType() == Material.AIR) {
            return getExp;
        }

        if (block.getType() == Material.WHEAT) {
            getExp = 0.05;
        } else if (block.getType() == Material.BEETROOTS) {
            getExp = 0.06;
        } else if (block.getType() == Material.CARROTS) {
            getExp = 0.07;
        } else if (block.getType() == Material.POTATOES) {
            getExp = 0.08;
        } else if (block.getType() == Material.NETHER_WART) {
            getExp = 0.1;
        } else if (block.getType() == Material.COCOA) {
            getExp = 0.8;
        } else if (block.getType() == Material.MELON) {
            getExp = 1.5;
        } else if (block.getType() == Material.PUMPKIN) {
            getExp = 2.5;
        }

        return getExp;
    }

    public static void ManageCropExp(Player player, Block block) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            return;
        }

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

        Double proficiency = container.get(GetNamespacedKey(proficiencyKey), PersistentDataType.DOUBLE);
        if (proficiency == null) {
            proficiency = 0.0;
        }

        if (!ItemNotBroken(item)) {
            return;
        }

        if (!hasLevel(player, container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER))) {
            return;
        }

        if (isHoe(item)) {
            if (item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)) {
                return;
            }
        }

        double gain = GetCropValue(block);

        if (gain > 0) {
            double totalGain = AddExp(player, gain, proficiency / 10);
            spawnExpHologram(block.getLocation(), totalGain);
            PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, block.getLocation(), 0.2f, 1);
        }
    }

    public static int cropDropChance = 7;

    public static void ManageFarm(Player player, Block block) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            player.sendMessage(sendText("&4Use hoe to harvest!"));
            return;
        }

        if (item.getType() != Material.AIR) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();

            if (!isFactoryItem(item)) {
                player.sendMessage(sendText("&4Use hoe to harvest!"));
                return;
            }

            if (!isHoe(item)) {
                player.sendMessage(sendText("&4Use hoe to harvest!"));
            }

            if (!ItemNotBroken(item)) {
                Notification_ItemBroken(player);
                return;
            }

            if (!hasLevel(player, container.get(GetNamespacedKey(levelMinimumKey), PersistentDataType.INTEGER))) {
                return;
            }
        }

        BlockData newData = block.getBlockData();
        if (newData instanceof Ageable ageable) {

            if (ageable.getAge() != ageable.getMaximumAge()) {
                return;
            }

            ageable.setAge(0);
            block.setBlockData(ageable);
            //ManageCropExp(player, block);
            if (isCropBlock(block)) {
                new BukkitRunnable() {
                    int currentAge = 0;
                    final int maxAge = ageable.getMaximumAge();

                    @Override
                    public void run() {
                        if (currentAge >= maxAge) {
                            this.cancel();
                            return;
                        }
                        currentAge++;
                        if (block.getBlockData() instanceof Ageable growable) {
                            growable.setAge(currentAge);
                            block.setBlockData(growable.clone());
                        }
                    }
                }.runTaskTimer(getMainPlugin(), 20L, 10L);

                Random random = new Random();
                int randomDrop = random.nextInt(100) + 1;
                int randomDrop2 = random.nextInt(100) + 1;
                int randomDropAmount = random.nextInt(3) + 1;

                double farmingFortune = playerAttributes.get(player.getName()+".attribute.total.farmingfortune");

                int dropChance = (int) (cropDropChance+farmingFortune);
                if (dropChance > 60){
                    dropChance = 60;
                }

                if (randomDrop <= dropChance) {
                    if (block.getType() == Material.WHEAT) {
                        DropItem(block.getLocation(), new ItemStack(ProcessItemMeta(new ItemStack(Material.WHEAT))), 1);
                        if (randomDrop2 <= 50) {
                            DropItem(block.getLocation(), new ItemStack(ProcessItemMeta(new ItemStack(Material.WHEAT_SEEDS))), 1);
                        }
                    } else if (block.getType() == Material.BEETROOTS) {
                        DropItem(block.getLocation(), new ItemStack(ProcessItemMeta(new ItemStack(Material.BEETROOT))), 1);
                        if (randomDrop2 <= 50) {
                            DropItem(block.getLocation(), new ItemStack(ProcessItemMeta(new ItemStack(Material.BEETROOT_SEEDS))), 1);
                        }
                    } else if (block.getType() == Material.CARROTS) {
                        DropItem(block.getLocation(), new ItemStack(ProcessItemMeta(new ItemStack(Material.CARROT))), randomDropAmount);
                    } else if (block.getType() == Material.POTATOES) {
                        DropItem(block.getLocation(), new ItemStack(ProcessItemMeta(new ItemStack(Material.POTATO))), randomDropAmount);
                    } else if (block.getType() == Material.NETHER_WART) {
                        DropItem(block.getLocation(), new ItemStack(ProcessItemMeta(new ItemStack(Material.NETHER_WART))), randomDropAmount + 1);
                    }
                }
            }
            Random random = new Random();
            int randomConsumeDurability = random.nextInt(100) + 1;
            ManageCropExp(player, block);
            if (randomConsumeDurability <= 10) {
                if (currentEvent != FactoryEvents.EventType.Invincible_Items) {
                    ManageDurability(player, "hand");
                    UpdateItem(player, "hand", item);
                }
            }
        }
    }

}
