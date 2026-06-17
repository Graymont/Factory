package org.factory.factory.GameHandler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.factory.factory.GameManager.CooldownManager;

import java.util.Random;

import static org.factory.factory.GameManager.CooldownManager.*;
import static org.factory.factory.Utils.UserInterface.*;

public class FactoryEvents {

    public static EventType currentEvent = EventType.None;

    public static Double events_machineSpeedMultiplier = 1.0;
    public static Double events_expMultiplier = 1.0;
    public static Double events_sellMultiplier = 1.0;
    public static Double events_steamRegenMultiplier = 1.0;

    public static void ResetEventBenefits(){
        events_machineSpeedMultiplier = 1.0;
        events_expMultiplier = 1.0;
        events_sellMultiplier = 1.0;
        events_steamRegenMultiplier = 1.0;
        consoleLog(sendText("&aAll Events benefits has been reset!"));
    }

    public enum EventType{
        None,
        Double_Exp,
        Double_Sell,
        Faster_Machine,
        Slower_Machine,
        Invincible_Items,
        Faster_Steam_Regen,
        Slower_Steam_Regen;

        public static EventType parseEvent(String type){
            return switch (type.toLowerCase()){

                case "double_exp" -> EventType.Double_Exp;
                case "double_sell" -> EventType.Double_Sell;
                case "faster_machine" -> EventType.Faster_Machine;
                case "slower_machine" -> EventType.Slower_Machine;
                case "invincible_items" -> EventType.Invincible_Items;
                case "faster_steam_regen" -> EventType.Faster_Steam_Regen;
                case "slower_steam_regen" -> EventType.Slower_Steam_Regen;

                default -> EventType.None;
            };
        }
    }

    public static void ManageEventBenefits(){
        if (currentEvent == EventType.Faster_Machine){
            events_machineSpeedMultiplier = 0.5;
        }
        else if (currentEvent == EventType.Slower_Machine){
            events_machineSpeedMultiplier = 2.0;
        }

        else if (currentEvent == EventType.Faster_Steam_Regen){
            events_steamRegenMultiplier = 2.0;
        }
        else if (currentEvent == EventType.Slower_Steam_Regen){
            events_steamRegenMultiplier = 1.0;
        }

        else if (currentEvent == EventType.Double_Sell){
            events_sellMultiplier = 2.0;
        }
        else if (currentEvent == EventType.Double_Exp){
            events_expMultiplier = 2.0;
        }
    }

    public static void ScheduleRollEvents(){
        if (!hasGlobalCooldown(CooldownManager.CooldownType.FactoryEvents)){
            RollEvents();
            SetGlobalCooldown(CooldownType.FactoryEvents, 1800);

        }
    }

    public static void RollEvents() {
        ResetEventBenefits();

        Random random = new Random();
        int roll = random.nextInt(100) + 1;

        if (roll <= 3) {
            SetEvent(EventType.Faster_Machine);
        } else if (roll <= 5) {
            SetEvent(EventType.Double_Sell);
        } else if (roll <= 10) {
            SetEvent(EventType.Double_Exp);
        } else if (roll <= 15) {
            SetEvent(EventType.Slower_Machine);
        } else if (roll <= 20) {
            SetEvent(EventType.Invincible_Items);
        } else if (roll <= 25) {
            SetEvent(EventType.Faster_Steam_Regen);
        } else if (roll <= 30) {
            SetEvent(EventType.Slower_Steam_Regen);
        } else {
            SetEvent(EventType.None);
        }

        if (currentEvent != EventType.None){
            AnnounceEvent(currentEvent);
        }else{
            Broadcast(sendText("&eOops! There's no event happening right now... :("));
        }
    }

    public static void AnnounceEvent(EventType type){
        Broadcast("&dNew Event: &b"+formatItemName(type.toString()));

        for (Player player : Bukkit.getOnlinePlayers()){
            SendTitle(player, "&dNew Event!", "&b"+formatItemName(type.toString()), 3, 5, 3);
        }
    }

    public static void SetEvent(EventType type){
        ResetEventBenefits();
        currentEvent = type;
        consoleLog(sendText("&aEvent has been set to &2"+type));
    }

}
