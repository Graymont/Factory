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
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

import static org.factory.factory.Utils.FactoryQuest.quest;
import static org.factory.factory.Utils.UserInterface.formatItemName;
import static org.factory.factory.Utils.UserInterface.sendText;

public class QuestManager implements Listener {

    public static HashMap<String, Integer> questCount = new HashMap<>();

    @EventHandler
    public void OnBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();

        Block block = event.getBlock();

        if (quest.get(player) != null){
            if (FactoryQuest.QuestType.parseType(quest.get(player).toString()) == FactoryQuest.QuestType.Break_Block){
                HashMap<Material, Integer> blockList = FactoryQuest.Quest.getBlocks(quest.get(player));
                for (Material material : blockList.keySet()){
                    String key = player.getName()+material;
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
            if (quest.get(player) != null){
                if (FactoryQuest.QuestType.parseType(quest.get(player).toString()) == FactoryQuest.QuestType.Kill_Mob){
                    HashMap<EntityType, Integer> entityList = FactoryQuest.Quest.getEntities(quest.get(player));
                    for (EntityType entityType : entityList.keySet()) {
                        String key = player.getName()+entityType.toString();
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
        if (quest.get(player) != null){
            if (FactoryQuest.QuestType.parseType(quest.get(player).toString()) == FactoryQuest.QuestType.Catch_Fish){
                HashMap<Material, Integer> fishList = FactoryQuest.Quest.getFishes(quest.get(player));
                for (Material material : fishList.keySet()){
                    String key = player.getName()+material;
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
