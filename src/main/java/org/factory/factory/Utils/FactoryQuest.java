package org.factory.factory.Utils;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.enginehub.linbus.stream.token.LinToken;

import java.util.*;

import static org.factory.factory.Utils.CooldownManager.hasCooldown;
import static org.factory.factory.Utils.QuestManager.questCount;
import static org.factory.factory.Utils.UserInterface.*;

public class FactoryQuest {

    public static HashMap<Player, Quest> quest = new HashMap<>();

    public enum Quest {
        Miner_1,
        Miner_2,
        Miner_3,
        Miner_4,
        Miner_5,
        Hunter_1,
        Hunter_2,
        Hunter_3,
        Hunter_4,
        Hunter_5,
        Fisherman_1,
        Fisherman_2,
        Fisherman_3,
        Fisherman_4,
        Fisherman_5;

        public static int getCooldown(Quest type) {
            return switch (type) {
                case Miner_1 -> 900;
                case Hunter_1 -> 900;
                case Fisherman_1 -> 900;

                case Miner_2 -> 1200;
                case Hunter_2 -> 1200;
                case Fisherman_2 -> 1200;

                case Miner_3 -> 2000;
                case Hunter_3 -> 2000;
                case Fisherman_3 -> 2000;

                case Miner_4 -> 2880;
                case Hunter_4 -> 2880;
                case Fisherman_4 -> 2880;

                case Miner_5 -> 3200;
                case Hunter_5 -> 3200;
                case Fisherman_5 -> 3200;
                default -> 1;
            };
        }
    }

    public enum QuestType {
        None, BreakBlock, CatchFish, Delivery, KillMob;

        public static QuestType parseType(String t) {
            return switch (t.toLowerCase()) {
                case "miner_1", "miner_2", "miner_3", "miner_4", "miner_5" -> BreakBlock;
                case "hunter_1", "hunter_2", "hunter_3", "hunter_4", "hunter_5" -> KillMob;
                case "fisherman_1", "fisherman_2", "fisherman_3", "fisherman_4", "fisherman_5" -> CatchFish;
                default -> None;
            };
        }
    }

    public static class QuestRequirements {

        public static HashMap<Material, Integer> getBlocks(Quest quest) {

            HashMap<Material, Integer> material = new HashMap<>();
            if (quest == Quest.Miner_1) {
                material.put(Material.COBBLESTONE, 10);
                material.put(Material.IRON_ORE, 5);
                return material;
            }
            return new HashMap<>();
        }

        public static HashMap<EntityType, Integer> getEntities(Quest quest) {

            HashMap<EntityType, Integer> entities = new HashMap<>();
            if (quest == Quest.Hunter_1) {
                entities.put(EntityType.ZOMBIE, 10);
                entities.put(EntityType.SKELETON, 5);
                return entities;
            }
            return new HashMap<>();
        }
    }

    public static String getFormattedQuestName(Quest q) {
        return q.toString().replaceAll("_", " ").trim();
    }

    public static void ClaimQuest(Player player, Quest q) {
        if (quest.containsKey(player)) {
            player.sendMessage(sendText("&6You already claimed another quest! &b(you're currently doing a &3"
                    + getFormattedQuestName(quest.get(player)) + " &bquest) &e/abandonquest &6to exit current quest!"));
            return;
        }
        if (hasCooldown(player, CooldownManager.CooldownType.parseCooldown(q.toString()))) {

            return;
        }
        quest.put(player, q);

        Quest takenQuest = quest.get(player);

        HashMap<String, Integer> countRequirements = initQuestRequirements(takenQuest);
        for (String key : countRequirements.keySet()){
            String countKey = player.getName() + key;
            questCount.put(countKey, 0);
        }

        player.sendMessage(sendText("&6Claimed Quest &b" + getFormattedQuestName(q)));
        PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, player.getLocation(), 1, 0);
    }

    public static void AbandonQuest(Player player) {
        if (!quest.containsKey(player)) {
            player.sendMessage(sendText("&4You're not claiming any quest!"));
            return;
        }
        Quest q = quest.remove(player);

        for (String key : questCount.keySet()){
            if (key.contains(player.getName())){
                questCount.remove(key);
                player.sendMessage("removed: "+key);
            }
        }

        player.sendMessage(sendText("&6Abandoned Quest &b" + getFormattedQuestName(q)));
        PlaySoundAt(Sound.ITEM_BOOK_PAGE_TURN, player.getLocation(), 1, 3);
        PlaySoundAt(Sound.BLOCK_NOTE_BLOCK_IMITATE_CREEPER, player.getLocation(), 1, 3);
    }

    public static void CompleteQuest(Player player, Quest q) {
        if (quest.get(player) != null){
            boolean canComplete = true;

            Quest takenQuest = quest.get(player);


            List<String> uncompletedNote = new ArrayList<>();

            HashMap<String, Integer> countRequirements = initQuestRequirements(takenQuest);

            for (String key : countRequirements.keySet()){
                String countKey = player.getName() + key;
                if (questCount.get(countKey) < countRequirements.get(key)){
                    canComplete = false;
                    uncompletedNote.add(sendText("&aUncompleted: &2"+formatItemName(key)+" ("+questCount.get(countKey)+"/"+countRequirements.get(key)+")"));
                }
            }

            if (!canComplete){
                player.sendMessage(sendText("&4You haven't completed all task from this quest!"));
                for (String uc : uncompletedNote){
                    player.sendMessage(uc);
                }
                return;
            }

            // give rewards
            player.sendMessage(sendText("&6Quest Completed &b"+getFormattedQuestName(takenQuest)));


        }else{
            player.sendMessage(sendText("&4You haven't claimed any quest yet!"));
        }
    }

    public static HashMap<String, Integer> initQuestRequirements(Quest takenQuest){
        HashMap<String, Integer> countRequirements = new HashMap<>();

        if (QuestType.parseType(takenQuest.toString()) == FactoryQuest.QuestType.KillMob){
            HashMap<EntityType, Integer> takenHashMap = FactoryQuest.QuestRequirements.getEntities(takenQuest);
            for (EntityType key : takenHashMap.keySet()){
                countRequirements.put(key.toString(), takenHashMap.get(key));
            }
        }

        else if (QuestType.parseType(takenQuest.toString()) == QuestType.BreakBlock){
            HashMap<Material, Integer> takenHashMap = FactoryQuest.QuestRequirements.getBlocks(takenQuest);
            for (Material key : takenHashMap.keySet()){
                countRequirements.put(key.toString(), takenHashMap.get(key));
            }
        }

        return countRequirements;
    }
}
