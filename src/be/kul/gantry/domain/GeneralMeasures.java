package be.kul.gantry.domain;

import java.util.*;

public abstract class GeneralMeasures {

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

    //tijdens toevoegen van itemMovements ook de te moven items toevoegen aan hasmap per niveau = verbodenSlots
    // tijdens uitgraven mogen eventuele lege slots uit verboden slots niet geselcteerd worden
    // => "copy" van niveaus + remove alle verbodenSlots, daarna uit overschot een leeg slot zoeken
    // x y z zijn coordinaten van te verplaatsen item => leeg slot zoeken zo dicht mogelijk bij x y z
    public static Slot zoekLeegSlot(Map<Integer, HashMap<Integer, HashMap<Integer, Slot>>> niveaus, Map<Integer, HashMap<Integer, HashMap<Integer, Slot>>> verbodenSloten, int x, int y, int z){
        Map<Integer, HashMap<Integer, HashMap<Integer, Slot>>> overschot = new HashMap<>();

        for(Integer i: niveaus.keySet()){
            overschot.computeIfAbsent(i, k -> new HashMap<>());
            HashMap<Integer, HashMap<Integer, Slot>> niveau = niveaus.get(i);
            HashMap<Integer, HashMap<Integer, Slot>> verbodenNiveau = verbodenSloten.get(i);

            for(Integer j: niveau.keySet()){
                overschot.computeIfAbsent(j, k -> new HashMap<>());
                HashMap<Integer, Slot> niveauRichting = niveau.get(j);
                HashMap<Integer, Slot> verbodenNiveauRichting = verbodenNiveau.get(j);

                for(Integer t: niveauRichting.keySet()){
                    Slot slot = niveauRichting.get(t);
                    Slot verbodenSlot = verbodenNiveauRichting.get(t);

                    if(!slot.equals(verbodenSlot)){
                        overschot.get(i).get(j).put(t, slot);
                    }

                }
            }
        }


        return zoekLeegSlot(parents);
    }

    //key van plaats hashmaps veranderen naar 10, 20, ... ipv 1 2 3 (zodat ook 15, 25, ... kan)
    public static Slot zoekSlot(HashMap<Integer, HashMap<Integer, HashMap<Integer, Slot>> mogelijkeSloten, int x, int y, int z)
    {
        Richting richting = Richting.NaarVoor;

        //for voor elke elke rij: itr var = k
        //Slot in een zo dicht mogelijke plaats zoeken
        boolean newSlotFound = false;
        Slot newSlot = null;
        int offset = 1;
        do { //TODO: als storage vol zit en NaarVoor en NaarAchter vinden geen vrije plaats => inf loop
            // bij het NaarAchter lopen uw index telkens het negatieve deel nemen, dus deze wordt telkens groter negatief.
            //AANPASSING
            Integer locatie = richting == Richting.NaarVoor ? x + offset*10 : y - offset*10;
            //we overlopen eerst alle richtingen NaarVoor wanneer deze op zen einde komt en er geen plaats meer is van richting veranderen naar achter
            // index terug op 1 zetten omdat de indexen ervoor al gecontroleerd waren
            Slot sl = mogelijkeSloten.get(k).get(locatie);
            if (sl == null) {
                //Grootte resetten en richting omdraaien
                offset = 1;
                richting = Richting.NaarAchter;
                continue;
            }

            if(sl.getItem() == null) {
                newSlot = sl;
                newSlotFound = true;
            }


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
