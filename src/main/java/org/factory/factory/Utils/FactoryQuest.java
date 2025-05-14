package org.factory.factory.Utils;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.enginehub.linbus.stream.token.LinToken;

import java.util.*;

import static org.factory.factory.Database.GetItem;
import static org.factory.factory.Events.DropItem;
import static org.factory.factory.Events.globalRevision;
import static org.factory.factory.Utils.CooldownManager.hasCooldown;
import static org.factory.factory.Utils.CooldownManager.SetCooldown;
import static org.factory.factory.Utils.FactoryItem.*;
import static org.factory.factory.Utils.PersistentDataManager.GetNamespacedKey;
import static org.factory.factory.Utils.PlayerProgress.*;
import static org.factory.factory.Utils.QuestManager.questCount;
import static org.factory.factory.Utils.UserInterface.*;
import static org.factory.factory.Utils.VaultEconomy.AddPlayerBalance;
import static org.factory.factory.Utils.VaultEconomy.icon;

public class FactoryQuest {

    public static HashMap<UUID, Quest> quest = new HashMap<>();

    public enum Quest {
        None,
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

        public static Quest parseQuest(String t) {
            return switch (t.toLowerCase()) {
                case "miner_1" -> Miner_1;
                case "miner_2" -> Miner_2;
                case "miner_3" -> Miner_3;
                case "miner_4" -> Miner_4;
                case "miner_5" -> Miner_5;

                case "hunter_1" -> Hunter_1;
                case "hunter_2" -> Hunter_2;
                case "hunter_3" -> Hunter_3;
                case "hunter_4" -> Hunter_4;
                case "hunter_5" -> Hunter_5;

                case "fisherman_1" -> Fisherman_1;
                case "fisherman_2" -> Fisherman_2;
                case "fisherman_3" -> Fisherman_3;
                case "fisherman_4" -> Fisherman_4;
                case "fisherman_5" -> Fisherman_5;

                default -> None;
            };
        }

        public static int getLevel(Quest type) {

            int levelMin = (Integer.parseInt(numberInText(type.toString())))*10;
            if (levelMin > 1){
                return levelMin;
            }

            return 1;
        }

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

        public static HashMap<Material, Integer> getBlocks(Quest quest) {

            HashMap<Material, Integer> material = new HashMap<>();

            String questName = uncolouredText(quest.toString());
            int questLevel = Integer.parseInt(numberInText(quest.toString()));

            if (questLevel == 1) {
                material.put(Material.STONE, questLevel*32);
                material.put(Material.COAL_ORE, questLevel*10);
                material.put(Material.COPPER_ORE, questLevel*7);
                material.put(Material.IRON_ORE, questLevel*5);
            }
            else if (questLevel == 2) {
                material.put(Material.STONE, questLevel*32);
                material.put(Material.REDSTONE_ORE, questLevel*10);
                material.put(Material.LAPIS_ORE, questLevel*10);
                material.put(Material.COAL_ORE, questLevel*10);
                material.put(Material.COPPER_ORE, questLevel*7);
                material.put(Material.IRON_ORE, questLevel*5);
                material.put(Material.GOLD_ORE, questLevel*2);
            }
            else if (questLevel == 3) {
                material.put(Material.STONE, questLevel*64);
                material.put(Material.REDSTONE_ORE, questLevel*15);
                material.put(Material.LAPIS_ORE, questLevel*20);
                material.put(Material.COAL_ORE, questLevel*20);
                material.put(Material.COPPER_ORE, questLevel*15);
                material.put(Material.IRON_ORE, questLevel*9);
                material.put(Material.GOLD_ORE, questLevel*5);
                material.put(Material.DIAMOND_ORE, questLevel*2);
            }
            else if (questLevel > 3) {
                material.put(Material.STONE, questLevel*64);
                material.put(Material.REDSTONE_ORE, questLevel*15);
                material.put(Material.LAPIS_ORE, questLevel*20);
                material.put(Material.COAL_ORE, questLevel*20);
                material.put(Material.COPPER_ORE, questLevel*15);
                material.put(Material.IRON_ORE, questLevel*9);
                material.put(Material.GOLD_ORE, questLevel*5);
                material.put(Material.DIAMOND_ORE, questLevel*2);
                material.put(Material.EMERALD_ORE, questLevel);
                material.put(Material.ANCIENT_DEBRIS, questLevel);
            }
            return material;
        }

        public static HashMap<EntityType, Integer> getEntities(Quest quest) {

            HashMap<EntityType, Integer> entities = new HashMap<>();
            int questLevel = Integer.parseInt(numberInText(quest.toString()));

            if (questLevel == 1) {
                entities.put(EntityType.ZOMBIE, questLevel*10);
                entities.put(EntityType.SKELETON, questLevel*5);
            }
            else if (questLevel == 2) {
                entities.put(EntityType.ZOMBIE, questLevel*15);
                entities.put(EntityType.SKELETON, questLevel*7);
                entities.put(EntityType.SPIDER, questLevel*2);
            }
            else if (questLevel == 3) {
                entities.put(EntityType.ZOMBIE, questLevel*15);
                entities.put(EntityType.SKELETON, questLevel*7);
                entities.put(EntityType.SPIDER, questLevel*5);
                entities.put(EntityType.CREEPER, questLevel*3);
            }
            else if (questLevel > 3) {
                entities.put(EntityType.ZOMBIE, questLevel*25);
                entities.put(EntityType.SKELETON, questLevel*15);
                entities.put(EntityType.SPIDER, questLevel*8);
                entities.put(EntityType.CREEPER, questLevel*5);
                entities.put(EntityType.ENDERMAN, questLevel*3);
            }
            return entities;
        }

        public static HashMap<Material, Integer> getFishes(Quest quest) {

            HashMap<Material, Integer> material = new HashMap<>();

            String questName = uncolouredText(quest.toString());
            int questLevel = Integer.parseInt(numberInText(quest.toString()));

            if (questLevel == 1) {
                material.put(Material.COD, questLevel*10);
            }
            else if (questLevel == 2) {
                material.put(Material.COD, questLevel*15);
                material.put(Material.SALMON, questLevel*5);
            }
            else if (questLevel == 3) {
                material.put(Material.COD, questLevel*15);
                material.put(Material.SALMON, questLevel*10);
                material.put(Material.TROPICAL_FISH, questLevel*5);
            }
            else if (questLevel == 4) {
                material.put(Material.COD, questLevel*25);
                material.put(Material.SALMON, questLevel*20);
                material.put(Material.TROPICAL_FISH, questLevel*15);
                material.put(Material.PUFFERFISH, questLevel*5);
            }else{
                material.put(Material.COD, questLevel*30);
                material.put(Material.SALMON, questLevel*25);
                material.put(Material.TROPICAL_FISH, questLevel*18);
                material.put(Material.PUFFERFISH, questLevel*8);
            }

            return material;
        }

        public static Double getExpRewards(Quest quest) {
            int questLevel = (Integer.parseInt(numberInText(quest.toString())))*10;
            return maxExp.get(questLevel)*0.25+((double) questLevel / 100);
        }

        public static Double getMoneyRewards(Quest quest) {
            int questLevel = (Integer.parseInt(numberInText(quest.toString())))*10;
            return (double) (100*questLevel);
        }

        public static List<ItemStack> getItemRewards(Quest quest) {
            List<ItemStack> rewards = new ArrayList<>();

            int questLevel = Integer.parseInt(numberInText(quest.toString()));

            rewards.add(getQuestPendant(questLevel));
            rewards.add(new ItemStack(GetItem("expbonusbooster"+questLevel)));
            rewards.add(new ItemStack(GetItem("sellbonusbooster"+questLevel)));
            if (QuestType.parseType(quest.toString()) == QuestType.Break_Block){
                rewards.add(ProcessItemMeta(new ItemStack(Material.COPPER_INGOT, 10+questLevel*5)));
                rewards.add(ProcessItemMeta(new ItemStack(Material.IRON_INGOT, 10+questLevel*3)));
                rewards.add(ProcessItemMeta(new ItemStack(Material.GOLD_INGOT, 10+questLevel)));
            }
            else if (QuestType.parseType(quest.toString()) == QuestType.Kill_Mob){
                rewards.add(ProcessItemMeta(new ItemStack(Material.ROTTEN_FLESH, 10+questLevel*5)));
                rewards.add(ProcessItemMeta(new ItemStack(Material.BONE, 10+questLevel*3)));
                rewards.add(ProcessItemMeta(new ItemStack(Material.STRING, 10+questLevel)));
            }
            else if (QuestType.parseType(quest.toString()) == QuestType.Catch_Fish){
                rewards.add(ProcessItemMeta(new ItemStack(Material.COOKED_COD, 10+questLevel*5)));
                rewards.add(ProcessItemMeta(new ItemStack(Material.COOKED_SALMON, 10+questLevel*3)));
            }

            return rewards;
        }
    }

    public static ItemStack getQuestPendant(int tier){
        ItemStack item = new ItemStack(Material.FIRE_CHARGE);
        ItemMeta meta = item.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        //container.set(GetNamespacedKey(revisionCodeKey), PersistentDataType.INTEGER, globalRevision);
        container.set(GetNamespacedKey(itemKey), PersistentDataType.BOOLEAN, true);

        meta.setDisplayName(sendText("&fQuest Pendant &7Tier &l"+intToRoman(tier)));

        List<String> itemLore = new ArrayList<>();

        itemLore.add(sendText("&9Miscellaneous"));
        itemLore.add(sendText(" "));
        itemLore.add(sendText(" &7Obtained from completing quest"));
        itemLore.add(sendText(" &7Trade this item at &f&nTraders"));

        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }

    public enum QuestType {
        None, Break_Block, Catch_Fish, Delivery, Kill_Mob;

        public static QuestType parseType(String t) {
            return switch (t.toLowerCase()) {
                case "miner_1", "miner_2", "miner_3", "miner_4", "miner_5" -> Break_Block;
                case "hunter_1", "hunter_2", "hunter_3", "hunter_4", "hunter_5" -> Kill_Mob;
                case "fisherman_1", "fisherman_2", "fisherman_3", "fisherman_4", "fisherman_5" -> Catch_Fish;
                default -> None;
            };
        }
    }



    public static String getFormattedQuestName(Quest q) {

        String plain = q.toString().replaceAll("_", " ").trim();
        String name = uncolouredText(plain).replaceAll(" ", "").trim();
        String roman = intToRoman(Integer.parseInt(numberInText(plain)));

        return name+" "+roman;
    }

    public static void ClaimQuest(Player player, Quest q) {
        if (quest.get(player.getUniqueId()) != null) {
            player.sendMessage(sendText("&6You already claimed another quest! &b(you're currently doing a &3"
                    + getFormattedQuestName(quest.get(player.getUniqueId())) + " &bquest) &e/abandonquest &6to exit current quest!"));
            return;
        }

        if (hasCooldown(player, CooldownManager.CooldownType.parseCooldown(q.toString()))) {
            return;
        }

        if (playerLevel.get(player.getUniqueId()) < Quest.getLevel(q)){
            player.sendMessage(Notification_NoLevel(player));
            return;
        }

        quest.put(player.getUniqueId(), q);

        Quest takenQuest = quest.get(player.getUniqueId());

        HashMap<String, Integer> countRequirements = initQuestRequirements(takenQuest);
        for (String key : countRequirements.keySet()){
            String countKey = player.getUniqueId() + ":" + key;
            questCount.put(countKey, 0);
        }

        player.sendMessage(sendText("&6Claimed Quest &b" + getFormattedQuestName(q)));

        player.sendTitle(sendText("&6Claimed Quest"), sendText("&b"+getFormattedQuestName(q)));

        PlaySoundAt(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, player.getLocation(), 1, 0);
    }

    public static void AbandonQuest(Player player) {
        if (quest.get(player.getUniqueId()) == null) {
            player.sendMessage(sendText("&4You're not claiming any quest!"));
            return;
        }
        Quest q = quest.remove(player.getUniqueId());

        List<String> countList = new ArrayList<>(questCount.keySet());
        for (String key : countList){
            if (key.contains(player.getUniqueId().toString())){
                questCount.remove(key);
                //player.sendMessage("removed: "+key);
            }
        }

        player.sendMessage(sendText("&6Abandoned Quest &b" + getFormattedQuestName(q)));
        PlaySoundAt(Sound.ITEM_BOOK_PAGE_TURN, player.getLocation(), 1, 3);
        PlaySoundAt(Sound.BLOCK_NOTE_BLOCK_IMITATE_CREEPER, player.getLocation(), 1, 3);
    }

    public static void CompleteQuest(Player player, Quest q) {
        if (quest.get(player.getUniqueId()) != null){
            boolean canComplete = true;

            Quest takenQuest = quest.get(player.getUniqueId());


            List<String> uncompletedNote = new ArrayList<>();

            HashMap<String, Integer> countRequirements = initQuestRequirements(takenQuest);
            for (String key : countRequirements.keySet()){
                String countKey = player.getUniqueId() + ":" + key;
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

            SetCooldown(player, CooldownManager.CooldownType.parseCooldown(q.toString()),
                    Quest.getCooldown(q));


            List<ItemStack> rewardList = Quest.getItemRewards(q);
            List<String> rewardsNote = new ArrayList<>();
            rewardsNote.add(sendText("&a[Quest Rewards]"));
            for (int i = 0; i < rewardList.size(); i++) {
                Map<Integer, ItemStack> addedItem = player.getInventory().addItem(rewardList.get(i));
                if (!addedItem.isEmpty()){
                    DropItem(player.getLocation(), rewardList.get(i), 1);
                    player.sendTitle(sendText(" "), sendText("&cInventory full! items dropped..."));
                }
                rewardsNote.add(sendText(" &2+"+rewardList.get(i).getAmount()+" "+rewardList.get(i).getItemMeta().getDisplayName()));
            }
            AddPlayerBalance(player, Quest.getMoneyRewards(q));
            AddExp(player, Quest.getExpRewards(q), 0);

            rewardsNote.add(sendText(" &2+"+FormatDouble(Quest.getMoneyRewards(q))+" "+icon));
            rewardsNote.add(sendText(" &2+"+FormatDouble(Quest.getExpRewards(q))+" Exp"));

            for (String text : rewardsNote){
                player.sendMessage(sendText(text));
            }

            quest.remove(player.getUniqueId());
            ClearQuestProgress(player);

            player.sendMessage(sendText("&6Quest Completed &b"+getFormattedQuestName(takenQuest)));

            PlaySoundAt(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, player.getLocation(), 1, 1);
            PlaySoundAt(Sound.UI_TOAST_CHALLENGE_COMPLETE, player.getLocation(), 1, 1);
        }else{
            player.sendMessage(sendText("&4You haven't claimed any quest yet!"));
        }
    }

    public static HashMap<String, Integer> initQuestRequirements(Quest takenQuest){
        HashMap<String, Integer> countRequirements = new HashMap<>();

        if (QuestType.parseType(takenQuest.toString()) == FactoryQuest.QuestType.Kill_Mob){
            HashMap<EntityType, Integer> takenHashMap = FactoryQuest.Quest.getEntities(takenQuest);
            for (EntityType key : takenHashMap.keySet()){
                countRequirements.put(key.toString(), takenHashMap.get(key));
            }
        }

        else if (QuestType.parseType(takenQuest.toString()) == QuestType.Break_Block){
            HashMap<Material, Integer> takenHashMap = FactoryQuest.Quest.getBlocks(takenQuest);
            for (Material key : takenHashMap.keySet()){
                countRequirements.put(key.toString(), takenHashMap.get(key));
            }
        }

        else if (QuestType.parseType(takenQuest.toString()) == QuestType.Catch_Fish){
            HashMap<Material, Integer> takenHashMap = FactoryQuest.Quest.getFishes(takenQuest);
            for (Material key : takenHashMap.keySet()){
                countRequirements.put(key.toString(), takenHashMap.get(key));
            }
        }

        return countRequirements;
    }

    public static boolean isQuestCompleted(Player player, Quest q){
        HashMap<String, Integer> countRequirements = initQuestRequirements(q);
        for (String key : countRequirements.keySet()){
            String countKey = player.getUniqueId() + ":" + key;
            if (questCount.get(countKey) < countRequirements.get(key)){
                return false;
            }
        }

        return true;
    }


    public static void ForceCompleteQuest(Player player){
        if (quest.get(player.getUniqueId()) != null){
            Quest takenQuest = quest.get(player.getUniqueId());

            HashMap<String, Integer> countRequirements = initQuestRequirements(takenQuest);
            for (String key : countRequirements.keySet()){
                String countKey = player.getUniqueId() + ":" + key;
                questCount.put(countKey, 1000000);
            }
        }
    }

    public static void ClearQuestProgress(Player player){
        if (quest.get(player.getUniqueId()) != null){
            Quest takenQuest = quest.get(player.getUniqueId());

            HashMap<String, Integer> countRequirements = initQuestRequirements(takenQuest);
            for (String key : countRequirements.keySet()){
                String countKey = player.getUniqueId() + ":" + key;
                questCount.remove(countKey);
            }
        }
    }


}
