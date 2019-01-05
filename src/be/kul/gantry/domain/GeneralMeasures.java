package be.kul.gantry.domain;
import org.ietf.jgss.GSSName;

import java.util.*;
import java.util.stream.Collectors;

import static be.kul.gantry.domain.Problem.*;

public class GeneralMeasures {

    public enum Richting {
        NaarVoor,
        NaarAchter
    }

    public static List<ItemMovement> doMoves(double startTijd, double pickupPlaceDuration, Gantry gantry, Slot start , Slot destination, Problem.Operatie operatie) throws GeenPlaatsException {

        //save state
        double revertTime = gantry.getTime();
        int revertX = gantry.getX();
        int revertY = gantry.getY();

        List<ItemMovement> movements = executeMoves(startTijd, pickupPlaceDuration, gantry, start, destination);

        if(gantries.size() > 1) {
            //check of niet botst
            Gantry g = gantries.get(gantries.indexOf(gantry) == 0 ? 1 : 0); // = andere kraan
            boolean[] result = isCollision(gantry, g, movements);
            if (!result[0]) {
                //movements.addAll(executeMoves(startTijd, pickupPlaceDuration, gantry, start, destination));
                update(operatie, destination, start);
            } else {
                //revert
                gantry.setTime(revertTime);
                gantry.setX(revertX);
                gantry.setY(revertY);
                gantry.getMovements().removeAll(movements);
                movements = new ArrayList<>();


                //als wel botst => als andere kraan stil staat -> doorgeven
                //zorgen dat niet zelfde slot pakt
                Operatie operatie1;
                Operatie operatie2;
                if (operatie == Operatie.VerplaatsNaarBinnen) {
                    operatie1 = Operatie.VerplaatsNaarBinnen;
                    operatie2 = Operatie.VerplaatsIntern;
                } else if (operatie == Operatie.VerplaatsNaarOutput) {
                    operatie1 = Operatie.VerplaatsIntern;
                    operatie2 = Operatie.VerplaatsNaarOutput;
                } else {
                    operatie1 = Operatie.VerplaatsIntern;
                    operatie2 = Operatie.VerplaatsIntern;
                }

                //wissel als botst bij oppakken van item
                //kraan 0 moet oppak doen???
                if (result[1]) {
                    int a = gantries.indexOf(gantry);
                    if (a != 0) {
                        Gantry gt = gantry;
                        gantry = g;
                        g = gt;
                    }
                }

                Set<Slot> set = new HashSet<>();
                set.add(destination);
                Slot tussenSlot = GeneralMeasures.zoekLeegSlot(getGrondSloten(), set, maxX);
                movements.addAll(GeneralMeasures.executeMoves(time, pickupPlaceDuration, gantry, start, tussenSlot));
                update(operatie1, tussenSlot, start);
                //zet gantry aan de kant
                movements.add(new ItemMovement(time, 0, gantry.getX(), gantry.getY(), gantry.getKant(), gantry.getY(), null, gantry));
                updateTime();
                //verzet item met andere kraan naar destination
                movements.addAll(GeneralMeasures.executeMoves(time, pickupPlaceDuration, g, tussenSlot, destination));
                update(operatie2, destination, tussenSlot);
            }
        }
        else{
            update(operatie, destination, start);
        }

        return movements;
    }

    private static void update(Operatie operatie, Slot destination, Slot startSlot){
        updateTime();

        switch (operatie) {
            case VerplaatsIntern:
                destination.setItem(startSlot.getItem());
                itemSlotLocation.put(destination.getItem().getId(), destination);
                break;
            case VerplaatsNaarBinnen:
                destination.setItem(startSlot.getItem());
                itemSlotLocation.put(destination.getItem().getId(), destination);
                break;
            case VerplaatsNaarOutput:
                itemSlotLocation.remove(startSlot.getItem().getId());
                break;
        }

        startSlot.setItem(null);
    }

    public static void updateTime(){
        if(gantries.size() == 2)
            time = Math.max(gantries.get(0).getTime(), gantries.get(1).getTime());
        else
            time = gantries.get(0).getTime();
    }

    private static List<ItemMovement> executeMoves(double startTijd, double pickupPlaceDuration, Gantry gantry, Slot start , Slot destination){
        List<ItemMovement> movements = new ArrayList<>();

        ItemMovement moveToPickUpPlace = new ItemMovement(startTijd, 0, gantry.getX(), gantry.getY(), start.getCenterX(), start.getCenterY(), null, gantry);
        ItemMovement movePickUp = new ItemMovement(-1, pickupPlaceDuration, gantry.getX(), gantry.getY(), gantry.getX(), gantry.getY(), start.getItem().getId(), gantry);
        ItemMovement moveToDestination = new ItemMovement(-1, 0, gantry.getX(), gantry.getY(), destination.getCenterX(), destination.getCenterY(), start.getItem().getId(), gantry);
        ItemMovement moveDropOnDestination = new ItemMovement(-1, pickupPlaceDuration, gantry.getX(), gantry.getY(), gantry.getX(), gantry.getY(), null, gantry);

        //De tijd wordt in ItemMovement zelf berekend
        movements.add(moveToPickUpPlace);
        movements.add(movePickUp);
        movements.add(moveToDestination);
        movements.add(moveDropOnDestination);

        return movements;
    }



    //geef laagste vrije slot weer met als grondvalk par:toCheck dat niet in par:blacklist zit
    public static Slot zoekLeegSlot(List<Slot> toCheck, Set<Slot> blacklist, int boundX) throws GeenPlaatsException {

        if(toCheck.size() == 0)
            throw new GeenPlaatsException();

        List<Slot> niveauHoger = new ArrayList<>();

        //verder zoeken in de buurt van X;
        int offset = 0;

        while(offset < toCheck.size() ) {
            Slot slot = checkIfEmpty(toCheck.get(offset), niveauHoger, blacklist, boundX);
            if(slot != null)
                return slot;
            offset++;
        }

        //We hebben geen oplossing gevonden, dus alle parents toevoegen aan lijst en niveau hoger gaan zoeken
        return zoekLeegSlot(niveauHoger, blacklist, boundX);
    }

    private static Slot checkIfEmpty(Slot s, List<Slot> niveauHoger, Set<Slot> blacklist, int maxBoundX) throws GeenPlaatsException {

        if(s.getItem() == null && s.getXMax() < maxBoundX) {
            if(blacklist == null || !blacklist.contains(s))
                return s;
        }

        //links zal altijd aan voldoen
        if(s.getParents().get(0)!=null && !niveauHoger.contains(s.getParents().get(0)) && s.getParents().get(0).getXMax() < maxBoundX)
            niveauHoger.add(s.getParents().get(0));
        if(s.getParents().get(1)!=null && !niveauHoger.contains(s.getParents().get(1)) && s.getParents().get(1).getXMax() < maxBoundX)
            niveauHoger.add(s.getParents().get(1));
        return null;
    }

    public static Slot zoekLeegSlotInBuurt(Slot slot, HashMap<Integer, HashMap<Integer, Slot>> grondSlots, Set<Slot> blacklist, int maxBoundX) throws GeenPlaatsException {
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
            newSlot = zoekLeegSlot(new ArrayList<>(grondSlots.get(locatie).values()), blacklist, maxBoundX);

            //telkens één slot verder gaan
            offset++;

            if(newSlot != null){
                newSlotFound = true;
            }

        }while(!newSlotFound);
        // vanaf er een nieuw vrij slot gevonden is deze functie verlaten

        return newSlot;
    }

    //check of par:movements kan toevoegen aan par:gantry1 zonder dat botst met par:gantry2
    public static boolean[] isCollision(Gantry gantry1, Gantry gantry2, List<ItemMovement> movements){

        for(int ir = 0; ir<movements.size(); ir++) {
            ItemMovement itemMovement = movements.get(ir);

            double startTijd1 = itemMovement.getStartTime();
            double eindTijd1 = itemMovement.getEndTime();

            //alle movements van andere kraan die overlappen met deze movement
            List<ItemMovement> t = gantry2.getMovements().stream().filter(e -> (e.getStartTime() >= startTijd1 && e.getStartTime() <= eindTijd1) || (e.getEndTime() <= eindTijd1 && e.getEndTime() >= startTijd1)).collect(Collectors.toList());

            if(t.size() == 0){
                t = new ArrayList<>();
                ItemMovement s = new ItemMovement(startTijd1, 0, gantry2.getX(), gantry2.getY(), gantry2.getX(), gantry2.getY(), null, null);
                s.setEndTime(eindTijd1);
                t.add(s);

            }
            else {
                //gaten opvullen (als kraan stil staat)
                for (int i = 0; i < t.size() - 1; i++) {
                    ItemMovement c = t.get(i);
                    ItemMovement n = t.get(i + 1);

                    if (c.getEndTime() != n.getStartTime()) {
                        ItemMovement it = new ItemMovement(c.getEndTime(), 0, gantry2.getX(), gantry2.getY(), gantry2.getX(), gantry2.getY(), null, null);
                        it.setEndTime(n.getStartTime());
                        t.add(i + 1, it);
                    }
                }

                //start/end opvullen
                if (t.get(0).getStartTime() > startTijd1) {
                    ItemMovement d = new ItemMovement(startTijd1, 0, t.get(0).getX_start(), t.get(0).getY_start(), t.get(0).getX_start(), t.get(0).getY_start(), null, null);
                    d.setEndTime(t.get(0).getStartTime());
                    t.add(0, d);
                }
                if (t.get(t.size() - 1).getEndTime() < eindTijd1) {
                    ItemMovement d = new ItemMovement(t.get(t.size() - 1).getEndTime(), 0, t.get(t.size() - 1).getX_start(), t.get(t.size() - 1).getY_start(), t.get(t.size() - 1).getX_start(), t.get(t.size() - 1).getY_start(), null, null);
                    d.setEndTime(eindTijd1);
                    t.add(t.size(), d);
                }
            }


            //tussenliggende move collision checken
            for (ItemMovement d: t) {
                boolean b = checkCollision(itemMovement.getX_start(), startTijd1, itemMovement.getX_destination(), eindTijd1, d.getX_start(), d.getStartTime(), d.getX_destination(), d.getEndTime(), gantry1.getXSpeed());
                if (b) {
                    return new boolean[]{true, ir < 2};
                }
            }

        }
        return new boolean[]{false, false};
    }

    public static boolean checkCollision(int xA1v, double tA1v, int xB1v, double tB1v, int xA2v, double tA2v, int xB2v, double tB2v, double xSpeed){
        //lijn 1 beweegt
        int xA1, xB1, xA2, xB2;
        double tB1, tA1, tA2 ,tB2;

        //zoek welke er niet stil staat (altijd minstens een die beweegt)
        if(xA1v != xB1v) {
            xA1 = xA1v;
            xA2 = xA2v;
            xB1 = xB1v;
            xB2 = xB2v;
            tA1 = tA1v;
            tA2 = tA2v;
            tB1 = tB1v;
            tB2 = tB2v;
        }
        else {
            //niet botsen als allebei stil staan
            if(xA2v == xB2v)
                return false;

            xA2 = xA1v;
            xA1 = xA2v;
            xB2 = xB1v;
            xB1 = xB2v;
            tA2 = tA1v;
            tA1 = tA2v;
            tB2 = tB1v;
            tB1 = tB2v;
        }

        double speed1;
        if (xA1 > xB1) {
            speed1 = -xSpeed;
            double tmp = tA1;
            tA1 = tB1;
            tB1 = tmp;

            int temp = xA1;
            xA1 = xB1;
            xB1 = temp;

            int t = xA2;
            xA2 = xB2;
            xB2 = t;
        } else {
            speed1 = xSpeed;
        }

        double speed2;
        if (xA2 > xB2) {
            speed2 = -xSpeed;
        } else if(xB1 < xB2) {
            speed2 = xSpeed;
        } else {
            speed2 = 0;
        }

        if((xA2 <= speed2*(tA2 - tB1) + (xB1 + safetyDistance) && xA2 >= speed2*(tA2 - tA1) + (xA1 - safetyDistance)) || speed1 == speed2) {
            if (xA2 >= speed1 * (tA2 - tA1) + (xA1 - safetyDistance) && xB2 <= speed1 * (tB2 - tA2) + (xA1 + safetyDistance)) {
                return true;
            }
        }

        return false;

    }
}
