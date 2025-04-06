package org.factory.factory.Utils;

import java.util.Random;

import static org.factory.factory.Utils.CooldownManager.*;
import static org.factory.factory.Utils.UserInterface.Broadcast;
import static org.factory.factory.Utils.UserInterface.formatItemName;

public class FactoryEvents {

    public static EventType currentEvent = EventType.None;

    public enum EventType{
        None,
        Double_Exp,
        Double_Sell,
        Double_Machine_Speed,
        Slower_Machine,
        Invincible_Items,
        Double_Steam_Regen,
        Slower_Steam_Regen
    }

    public static void ScheduleRollEvents(){
        if (!hasGlobalCooldown(CooldownManager.CooldownType.FactoryEvents)){
            RollEvents();
            setGlobalCooldown(CooldownType.FactoryEvents, 1800);
        }
    }

    public static void RollEvents() {
        Random random = new Random();
        int roll = random.nextInt(100) + 1;

        if (roll <= 3) {
            currentEvent = EventType.Double_Machine_Speed;
        } else if (roll <= 5) {
            currentEvent = EventType.Double_Sell;
        } else if (roll <= 10) {
            currentEvent = EventType.Double_Exp;
        } else if (roll <= 15) {
            currentEvent = EventType.Slower_Machine;
        } else if (roll <= 20) {
            currentEvent = EventType.Invincible_Items;
        } else if (roll <= 25) {
            currentEvent = EventType.Double_Steam_Regen;
        } else if (roll <= 30) {
            currentEvent = EventType.Slower_Steam_Regen;
        } else {
            currentEvent = EventType.None;
        }

        AnnounceEvent(currentEvent);
    }

    public static void AnnounceEvent(EventType type){
        Broadcast("&dNew Events: &b"+formatItemName(type.toString()));
    }

}
