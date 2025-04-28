package org.factory.factory.Utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.factory.factory.Events.isBlockInRegion;
import static org.factory.factory.Events.isPlayerInRegion;
import static org.factory.factory.Utils.FactoryQuest.quest;
import static org.factory.factory.Utils.UserInterface.*;

public class QuestManager implements Listener {

    @EventHandler
    public void OnJoin(PlayerJoinEvent event){
        /*for (String key : questCount.keySet()){
            consoleLog(key+" "+questCount.get(key));
        }
        for (UUID key : quest.keySet()){
            consoleLog(key+" "+quest.get(key));
        }*/
    }


    public static HashMap<String, Integer> questCount = new HashMap<>();

    @EventHandler
    public void OnBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();

        Block block = event.getBlock();
        if (isBlockInRegion(block, "mine")){
            if (!event.isCancelled()){
                TriggerMiningQuest(player, block);
            }
        }
    }

    public static void TriggerMiningQuest(Player player, Block block){
        if (quest.get(player.getUniqueId()) != null){
            if (FactoryQuest.QuestType.parseType(quest.get(player.getUniqueId()).toString()) == FactoryQuest.QuestType.Break_Block){
                HashMap<Material, Integer> blockList = FactoryQuest.Quest.getBlocks(quest.get(player.getUniqueId()));
                for (Material material : blockList.keySet()){
                    String key = player.getUniqueId()+ ":" +material.toString();
                    if (block.getType() == material){
                        questCount.putIfAbsent(key, 0);
                        int value = blockList.get(material);
                        int count = questCount.get(key);
                        if (count < value){
                            count++;
                            questCount.put(key, count);
                            player.sendMessage(sendText("&aMine "+formatItemName(material.toString())+" &2"+count+"/"+value));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void OnKillMob(EntityDeathEvent event){
        Player player = event.getEntity().getKiller();
        Entity entity = event.getEntity();
        if (player != null){
            if (quest.get(player.getUniqueId()) != null){
                if (FactoryQuest.QuestType.parseType(quest.get(player.getUniqueId()).toString()) == FactoryQuest.QuestType.Kill_Mob){
                    HashMap<EntityType, Integer> entityList = FactoryQuest.Quest.getEntities(quest.get(player.getUniqueId()));
                    for (EntityType entityType : entityList.keySet()) {
                        String key = player.getUniqueId()+ ":" +entityType.toString();
                        if (entity.getType() == entityType) {
                            questCount.putIfAbsent(key, 0);
                            int value = entityList.get(entityType);
                            int count = questCount.get(key);
                            if (count < value){
                                count++;
                                questCount.put(key, count);
                                player.sendMessage(sendText("&aKill "+formatItemName(entityType.toString())+" &2"+count+"/"+value));
                            }
                        }
                    }
                }
            }
        }
    }


    public static void TriggerFishingQuest(Player player, ItemStack fish){
        if (quest.get(player.getUniqueId()) != null){
            if (FactoryQuest.QuestType.parseType(quest.get(player.getUniqueId()).toString()) == FactoryQuest.QuestType.Catch_Fish){
                HashMap<Material, Integer> fishList = FactoryQuest.Quest.getFishes(quest.get(player.getUniqueId()));
                for (Material material : fishList.keySet()){
                    String key = player.getUniqueId()+ ":" +material.toString();
                    if (fish.getType() == material){
                        questCount.putIfAbsent(key, 0);
                        int value = fishList.get(material);
                        int count = questCount.get(key);
                        if (count < value){
                            count++;
                            questCount.put(key, count);
                            player.sendMessage(sendText("&aCatch Fish "+formatItemName(material.toString())+" &2"+count+"/"+value));
                        }
                    }
                }
            }
        }
    }

}
