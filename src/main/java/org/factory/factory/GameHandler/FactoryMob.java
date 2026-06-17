package org.factory.factory.GameHandler;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import static org.factory.factory.Database.GetItem;
import static org.factory.factory.Database.spawnerList;
import static org.factory.factory.Events.*;
import static org.factory.factory.Factory.getMainPlugin;
import static org.factory.factory.Utils.UserInterface.*;

public class FactoryMob implements Listener {

    @EventHandler
    public void OnMobDeath(EntityDeathEvent event){
        Entity entity = event.getEntity();
        Location location = entity.getLocation();

        Random random = new Random();

        assert entity instanceof Mob;

        int randomDropAmount = random.nextInt(2)+1;

        if (GetEntityDungeonTier(location) < 1){
            return;
        }

        int randomChance = random.nextInt(100)+1;
        if (randomChance <= 50){
            if (isAlien(entity)){
                ItemStack drop = new ItemStack(GetItem("alienmembrane"+GetEntityDungeonTier(location)));
                drop.setAmount(randomDropAmount);
                location.getWorld().dropItemNaturally(location, drop);
            }
            else if (isMutant(entity)){
                ItemStack drop = new ItemStack(GetItem("mutantmembrane"+GetEntityDungeonTier(location)));
                drop.setAmount(randomDropAmount);
                location.getWorld().dropItemNaturally(location, drop);
            }
            else if (isUndead(entity)){
                ItemStack drop = new ItemStack(GetItem("undeadmembrane"+GetEntityDungeonTier(location)));
                drop.setAmount(randomDropAmount);
                location.getWorld().dropItemNaturally(location, drop);
            }
        }
    }

    public static void SpawnMob(Location location){
        Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
        Disguise disguise = DisguiseAPI.getCustomDisguise("martianman1");
        DisguiseAPI.disguiseToAll(zombie, disguise);
    }

    public static int GetEntityDungeonTier(Location location){
        Set<String> regionNames = getRegionByLocation(location);
        for (String region : regionNames) {
            if (uncolouredText(region).contains("dungeon")){
                return (Integer.parseInt(numberInText(region)));
            }
        }
        return 0;
    }

    public static boolean isAlien(Entity entity){
        if (entity.getCustomName() == null){
            return false;
        }

        String name = uncolouredText(entity.getCustomName()).toLowerCase();
        return name.contains("martian") || name.contains("venusian") ||
                name.contains("mercurian") ;
    }

    public static boolean isUndead(Entity entity){
        if (entity.getCustomName() == null){
            return false;
        }

        String name = uncolouredText(entity.getCustomName()).toLowerCase();
        return name.contains("zombie") || name.contains("skeleton") ||
                name.contains("stray") ||
                name.contains("husk")||
                name.contains("wither");
    }

    public static boolean isMutant(Entity entity){
        if (entity.getCustomName() == null){
            return false;
        }

        String name = uncolouredText(entity.getCustomName()).toLowerCase();
        return name.contains("tarantlas") || name.contains("leech") ||
                name.contains("fangveil") ||
                name.contains("pteranodon")||
                name.contains("werewolf");
    }

    public static void SpawnDungeonMob(Location location, String name, int level){
        Mob summonedEntity = null;

        Disguise disguise = null;
        if (name.contains("tarantlas")){
            summonedEntity = (Spider) location.getWorld().spawnEntity(location, EntityType.SPIDER);
            disguise = new MobDisguise(DisguiseType.SPIDER);
        }
        else if (name.contains("leech")){
            summonedEntity = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
            disguise = new MobDisguise(DisguiseType.SILVERFISH);
        }
        else if (name.contains("fangveil")){
            summonedEntity = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
            disguise = new MobDisguise(DisguiseType.BAT);
        }
        else if (name.contains("pteranodon")){
            summonedEntity = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
            disguise = new MobDisguise(DisguiseType.PHANTOM);
        }
        else if (name.contains("werewolf")){
            summonedEntity = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
            disguise = new MobDisguise(DisguiseType.WOLF);
        }else{
            disguise = DisguiseAPI.getCustomDisguise(name);
            if (disguise == null){
                //consoleLog(sendText("&cDisguise with name &4"+name+" &cis not exist!"));
                return;
            }
            summonedEntity = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
            ((Zombie) summonedEntity).setAdult();
        }
        DisguiseAPI.disguiseEntity(summonedEntity, disguise);

        AttributeInstance scale = summonedEntity.getAttribute(Attribute.SCALE);
        if (scale != null) {
            double scaleValue = 1.0+ (double) (GetEntityDungeonTier(location)) /10;
            if (scaleValue > 2){
                scaleValue = 2.0;
            }
            scale.setBaseValue(scaleValue);
        }


        summonedEntity.setCustomNameVisible(true);

        summonedEntity.setMaxHealth(level*8);
        double maxHealth = summonedEntity.getMaxHealth();
        summonedEntity.setHealth(maxHealth);

        /*AttributeInstance attackDamage = summonedEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(level*2);
        }*/

        summonedEntity.setCustomName(sendText("&8[&9Lv."+level+"&8] &f"+uncolouredText(formatItemName(name))+" &8- &a"+maxHealth+"&8/&a"+maxHealth+"❤"));

        summonedEntity.addScoreboardTag("DungeonMob");

        if (summonedEntity.getVehicle() != null){
            summonedEntity.getVehicle().remove();
        }

        final Mob finalSummonedEntity = summonedEntity;
        Bukkit.getScheduler().runTaskLater(getMainPlugin(), () -> {
            if (finalSummonedEntity.isValid()){
                finalSummonedEntity.remove();
                PlaySoundAt(Sound.ENTITY_ITEM_PICKUP, finalSummonedEntity.getLocation(), 1, 0);
                PlayParticleAtBlock(finalSummonedEntity.getLocation().getBlock(), Particle.SOUL_FIRE_FLAME);
            }
        }, 1200L);
    }



    public static void ManageMobSpawning(){
        Random random = new Random();
        HashMap<String, Location> storedSpawnerList = new HashMap<>(spawnerList);
        for (String key : storedSpawnerList.keySet()){
            Location location = storedSpawnerList.get(key);

            int playerCount = 0;
            for (Player player : location.getNearbyPlayers(25)){
                playerCount++;
            }
            int maxVariant = 3;

            String variantKey = uncolouredText(key);

            if (playerCount > 0){
                for (int i = 1; i < maxVariant+1; i++) {
                    String disguiseName = variantKey+i;
                    int dungeonTier = GetEntityDungeonTier(location);
                    SpawnDungeonMob(location, disguiseName, dungeonTier*10);
                }
            }else{
                //consoleLog("There's no player in here! ("+key+")");
            }
        }
    }

    public static void Every30SecondsDungeonMobSpawn(){
        new BukkitRunnable() {
            @Override
            public void run() {
                ManageMobSpawning();
            }
        }.runTaskTimer(getMainPlugin(), 0L, 600L);

        consoleLog(sendText("&bDungeon Mob Spawning enabled! &3(30s)"));
    }

    public static void Every1SecondsSpawnerParticle(){
        new BukkitRunnable() {
            @Override
            public void run() {
                HashMap<String, Location> storedSpawnerList = new HashMap<>(spawnerList);
                for (String key : storedSpawnerList.keySet()){
                    Location location = storedSpawnerList.get(key);
                    SpawnBlockRedstoneParticle(location.getBlock(), Color.RED);
                }
            }
        }.runTaskTimer(getMainPlugin(), 0L, 20L);

        consoleLog(sendText("&bDungeon Spawner Particle enabled! &3(30s)"));
    }

}
