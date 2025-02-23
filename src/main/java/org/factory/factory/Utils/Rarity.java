package org.factory.factory.Utils;

public class Rarity {

    public enum RarityType{
        Common,
        Uncommon,
        Rare,
        Epic,
        Legendary,
        Immortal,
        Divine, COMMON;

        public static RarityType parseRarity(String rarity){
            return switch (rarity) {
                case "common" -> RarityType.Common;
                case "uncommon" -> RarityType.Uncommon;
                case "rare" -> RarityType.Rare;
                case "epic" -> RarityType.Epic;
                case "legendary" -> RarityType.Legendary;
                case "immortal" -> RarityType.Immortal;
                case "divine" -> RarityType.Divine;
                default -> RarityType.Common;
            };
        }
    }

    public static String setRarity(RarityType rarity){

        if (rarity == RarityType.Common){
            return "&aCommon";
        }
        else if (rarity == RarityType.Uncommon){
            return "&2Uncommon";
        }
        else if (rarity == RarityType.Rare){
            return "&6Rare";
        }
        else if (rarity == RarityType.Epic){
            return "&4Epic";
        }
        else if (rarity == RarityType.Legendary){
            return "&bLegendary";
        }
        else if (rarity == RarityType.Immortal){
            return "&5Immortal";
        }
        else if (rarity == RarityType.Divine){
            return "&dDivine";
        }
        return "&8";
    }

    public static String getColor(RarityType rarity){

        if (rarity == RarityType.Common){
            return "&a";
        }
        else if (rarity == RarityType.Uncommon){
            return "&2";
        }
        else if (rarity == RarityType.Rare){
            return "&6";
        }
        else if (rarity == RarityType.Epic){
            return "&4";
        }
        else if (rarity == RarityType.Legendary){
            return "&b";
        }
        else if (rarity == RarityType.Immortal){
            return "&5";
        }
        else if (rarity == RarityType.Divine){
            return "&d";
        }
        return "&8";
    }

}

