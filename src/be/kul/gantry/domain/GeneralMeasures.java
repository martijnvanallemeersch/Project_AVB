package be.kul.gantry.domain;

import java.util.*;

public abstract class GeneralMeasures {

    public enum Richting {
        NaarVoor,
        NaarAchter
    }

    public static List<ItemMovement> doMoves(double pickupPlaceDuration, Gantry gantry, Slot start , Slot destination){

        //De tijd wordt in ItemMovement zelf berekend
        List<ItemMovement> movements = new ArrayList<>();

        movements.add(new ItemMovement(0, start.getCenterX(), start.getCenterY(), null,gantry));
        //Item heffen;
        movements.add(new ItemMovement(pickupPlaceDuration, gantry.getX(), gantry.getY(), start.getItem().getId(), gantry));
        //Item vervoeren naar destination;
        movements.add(new ItemMovement(0, destination.getCenterX(), destination.getCenterY(), start.getItem().getId(), gantry));
        //Item plaatsen op destination;
        movements.add(new ItemMovement(pickupPlaceDuration, gantry.getX(), gantry.getY(), null, gantry));

        return movements;
    }

    public static Slot zoekLeegSlot(Set<Slot> toCheck){
        //in een rij een slot zoeken we beginnen zo laag mogelijk
        Set<Slot> parents = new HashSet<>();
        for(Slot slot: toCheck){
            if (slot.getItem() == null){
                return slot;
            }
            else if(slot.getParent() != null) {
                parents.add(slot.getParent());
            }
        }
        return zoekLeegSlot(parents);
    }

    public static Slot zoekSlot(Slot slot,HashMap<Integer, HashMap<Integer, Slot>> grondSlots)
    {
        Richting richting = Richting.NaarVoor;

        //Slot in een zo dicht mogelijke rij zoeken
        boolean newSlotFound = false;
        Slot newSlot = null;
        int offset = 1;
        do { //TODO: als storage vol zit en NaarVoor en NaarAchter vinden geen vrije plaats => inf loop
            // bij het NaarAchter lopen uw index telkens het negatieve deel nemen, dus deze wordt telkens groter negatief.
            //AANPASSING
            Integer locatie = richting== Richting.NaarVoor ? (slot.getCenterY() / 10) + offset : (slot.getCenterY() / 10) - offset;
            //we overlopen eerst alle richtingen NaarVoor wanneer deze op zen einde komt en er geen plaats meer is van richting veranderen naar achter
            // index terug op 1 zetten omdat de indexen ervoor al gecontroleerd waren
            if (grondSlots.get(locatie) == null) {
                //Grootte resetten en richting omdraaien
                offset = 1;
                richting = Richting.NaarAchter;
                continue;
            }

            //begin bij onderste rij
            newSlot = zoekLeegSlot(new HashSet<>(grondSlots.get(locatie).values()));

            //telkens één slot verder gaan
            offset += 1;

            if(newSlot != null){
                newSlotFound = true;
            }

        }while(!newSlotFound);
        // vanaf er een nieuw vrij slot gevonden is deze functie verlaten

        return newSlot;

    }
}
