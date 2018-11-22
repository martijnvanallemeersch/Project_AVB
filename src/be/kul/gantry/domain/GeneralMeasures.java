package be.kul.gantry.domain;
import java.util.*;

public class GeneralMeasures {

    public enum Richting {
        NaarVoor,
        NaarAchter
    }

    public static List<ItemMovement> doMoves(double pickupPlaceDuration, Gantry gantry, Slot start , Slot destination){

        ItemMovement moveToPickUpPlace = new ItemMovement(0, start.getCenterX(), start.getCenterY(), null,gantry);
        ItemMovement movePickUp = new ItemMovement(pickupPlaceDuration, gantry.getX(), gantry.getY(), start.getItem().getId(), gantry);
        ItemMovement moveToDestination = new ItemMovement(0, destination.getCenterX(), destination.getCenterY(), start.getItem().getId(), gantry);
        ItemMovement moveDropOnDestination = new ItemMovement(pickupPlaceDuration, gantry.getX(), gantry.getY(), null, gantry);

        //De tijd wordt in ItemMovement zelf berekend
        List<ItemMovement> movements = new ArrayList<>();
        movements.add(moveToPickUpPlace);
        movements.add(movePickUp);
        movements.add(moveToDestination);
        movements.add(moveDropOnDestination);


        return movements;
    }

    //geef laagste vrije slot weer met als grondvalk par:toCheck dat niet in par:blacklist zit
    public static Slot zoekLeegSlot(List<Slot> toCheck, Set<Slot> blacklist) throws GeenPlaatsException {

        if(toCheck.size() == 0)
            throw new GeenPlaatsException();

        List<Slot> niveauHoger = new ArrayList<>();

        //verder zoeken in de buurt van X;
        int offset = 0;

        while(offset < toCheck.size() ) {
            Slot slot = checkIfEmpty(toCheck.get(offset), niveauHoger, blacklist);
            if(slot != null)
                return slot;
            offset++;
        }

        //We hebben geen oplossing gevonden, dus alle parents toevoegen aan lijst en niveau hoger gaan zoeken
        return zoekLeegSlot(niveauHoger, blacklist);
    }

    private static Slot checkIfEmpty(Slot s, List<Slot> niveauHoger, Set<Slot> blacklist) throws GeenPlaatsException {

        if(s.getItem() == null) {
            if(blacklist == null || !blacklist.contains(s))
                return s;
        }

        if(s.getParentL()!=null && !niveauHoger.contains(s.getParentL()))
            niveauHoger.add(s.getParentL());
        if(s.getParentR()!=null && !niveauHoger.contains(s.getParentR()))
            niveauHoger.add(s.getParentR());
        return null;
    }

    public static Slot zoekLeegSlotInBuurt(Slot slot, HashMap<Integer, HashMap<Integer, Slot>> grondSlots, Set<Slot> blacklist) throws GeenPlaatsException {
        Richting richting = Richting.NaarVoor;

        //Slot in een zo dicht mogelijke rij zoeken
        boolean newSlotFound = false;
        Slot newSlot = null;
        int offset = 1;
        boolean t = false; //houd bij of al naarVoor al volledig gedaan is (nodig voor checkIfEmpty if opslag vol)
        do {
            // bij het NaarAchter lopen uw index telkens het negatieve deel nemen, dus deze wordt telkens groter negatief.
            Integer locatie = richting == Richting.NaarVoor ? (slot.getCenterY() / 10) + offset : (slot.getCenterY() / 10) - offset;

            //we overlopen eerst alle richtingen NaarVoor wanneer deze op zen einde komt en er geen plaats meer is van richting veranderen naar achter
            // index terug op 1 zetten omdat de indexen ervoor al gecontroleerd waren
            if (grondSlots.get(locatie) == null) {
                //checkIfEmpty of hele opslag al is afgelopen, if so => storage vol
                if(!t) {
                    //Grootte resetten en richting omdraaien
                    offset = 1;
                    richting = Richting.NaarAchter;
                    t = true;
                }
                else{
                    throw new GeenPlaatsException();
                }
                continue;
            }

            //begin bij onderste rij
            newSlot = zoekLeegSlot(new ArrayList<>(grondSlots.get(locatie).values()), blacklist);

            //telkens één slot verder gaan
            offset++;

            if(newSlot != null){
                newSlotFound = true;
            }

        }while(!newSlotFound);
        // vanaf er een nieuw vrij slot gevonden is deze functie verlaten

        return newSlot;
    }
}
