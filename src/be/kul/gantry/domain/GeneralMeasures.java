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

    public  static Slot zoekLeegSlot(List<Slot> toCheck) {

        List<Slot> niveauHoger = new ArrayList<>();

        //verder zoeken in de buurt van X;
        int offset = 0;

        while(offset < toCheck.size() ) {
            Slot rechts = null;

            if(offset < toCheck.size() ){
                rechts = check(toCheck,niveauHoger,offset);
            }
            if(rechts != null) return rechts;
            offset++;
        }

        //We hebben geen oplossing gevonden, dus alle parents toevoegen aan lijst en niveau hoger gaan zoeken
        return zoekLeegSlot(niveauHoger);
    }

    static Slot check(List<Slot> toCheck,List<Slot> niveauHoger,int offset)
    {
        Slot s = toCheck.get(offset);
        if(s.getItem() == null) return s;
        if(s.getParentL()!=null && !niveauHoger.contains(s.getParentL())) niveauHoger.add(s.getParentL());
        if(s.getParentR()!=null && !niveauHoger.contains(s.getParentR())) niveauHoger.add(s.getParentR());
        return null;
    }

    //key van plaats hashmaps veranderen naar 10, 20, ... ipv 1 2 3 (zodat ook 15, 25, ... kan)
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
            newSlot = zoekLeegSlot(new ArrayList<>(grondSlots.get(locatie).values()));

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
