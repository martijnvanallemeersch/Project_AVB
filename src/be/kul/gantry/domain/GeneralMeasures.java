package be.kul.gantry.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class GeneralMeasures {

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
}
