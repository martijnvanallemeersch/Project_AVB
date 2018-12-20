package be.kul.gantry.domain;

import java.util.*;

import static be.kul.gantry.domain.GeneralMeasures.doMoves;
import static be.kul.gantry.domain.GeneralMeasures.zoekLeegSlot;
import static be.kul.gantry.domain.Problem.*;

/**
 * Created by ruben on 23/11/18.
 */
public class MovementList {
    //DummyMovements hebben gantry, deze gantry zal het element opnemen uit zijn start slot
    // de gantry 1 en 2 in add(all) zegt welke gantries beschikbaar zijn, als nodig is kan tussentijds
    // een item ergens anders gezet worden en opgenomen worden door een van de beschikbare kranen

    private final List<Gantry> gantries;
    private List<DummyMovement> dummyMovements = new LinkedList<>();
    private List<DummyMovement> overschot = new LinkedList<>();

    private double time = 0;
    private List<ItemMovement> itemMovements = new ArrayList<>();

    public MovementList(List<Gantry> gantries) {
        this.gantries = gantries;
    }

    public void add(DummyMovement dummyMovement){
        dummyMovements.add(dummyMovement);
    }

    public void addAll(Collection<? extends DummyMovement> collections){
        dummyMovements.addAll(collections);
    }

    public List<ItemMovement> getDummyMovements() {
        try {
            while (!buildItemMovements()) {
            }

            return itemMovements;
        }
        catch (GeenPlaatsException g){
            g.printStackTrace();
        }
        return null;
    }

    private boolean buildItemMovements() throws GeenPlaatsException {
        boolean check = true;

        while(!dummyMovements.isEmpty() || !overschot.isEmpty()){
            //time = Math.min(gantries.get(0).getAvailableTime(), gantries.get(1).getAvailableTime());

            DummyMovement dummyMovement;
            if(check && !overschot.isEmpty())
                dummyMovement = overschot.remove(0);
            else if(!dummyMovements.isEmpty())
                dummyMovement = dummyMovements.remove(0);
            else{
                check = true;
                continue;
            }

            Gantry kraan = dummyMovement.getStartGantry();

/*            //TODO check dit!!!
            dummyMovement.getStart().setItem(dummyMovement.getItem());*/

            if(kraan != null) {
                //index is index van andere kraan
                int index = gantries.indexOf(kraan) == 0 ? 1 : 0;
                //check of startkraan is beschikbaar
                if (kraan.getAvailableTime() <= time) {

                    //check of conditionele moves gedaan zijn
                    if (!dummyMovement.checkConditional()) {
                        //conditionals nog niet gedaan => laat in queue staan
                        //DummyMovement d = dummyMovements.remove(0);
                        overschot.add(dummyMovement);
                        check = false;
                        checkTime(kraan);
                        continue;
                    }

                    //check of kraan wel aan bestemming kan
                    // if so ok
                    //if not => tussenslot zoeken en wisselen van kraan
                    //TODO: ook check voor links??
                    if(index == 1 && dummyMovement.getEind().getXMax() > maxX - safetyDistance){
                        Slot tussenSlot = zoekLeegSlot(getGrondSloten(), null, maxX - safetyDistance);
                        DummyMovement d1 = new DummyMovement(dummyMovement.getStart(), tussenSlot, dummyMovement.getItem(), gantries.get(0), dummyMovement.getPreDoneMovements(), dummyMovement.getGantry1(), dummyMovement.getGantry2(), dummyMovement.getOperatie());
                        Set<DummyMovement> dd = new HashSet<>(dummyMovement.getPreDoneMovements());
                        dd.add(d1);
                        DummyMovement d2 = new DummyMovement(tussenSlot, dummyMovement.getEind(), dummyMovement.getItem(), gantries.get(1), dd, dummyMovement.getGantry1(), dummyMovement.getGantry2(), dummyMovement.getOperatie());
                        dummyMovements.add(0, d1);
                        dummyMovements.add(1, d2);
                        return false;
                    }

                    //check of gaat botsen met andere kraan
                    //boolean[0] = collision, boolean[1] = is kraan op index in rust of is aan het bewegen
                    boolean[] collision = kraan.isCollision(gantries.get(index), dummyMovement);
                    //check of gaat botsen met andere kraan
                    if (!collision[0]) {
                        //alles ok => voer movement uit en verwijder uit queue, totale tijd opslaan
                        dummyMovement.setStartTijd(time);
                        itemMovements.addAll(doMoves(time, kraan, dummyMovement.getStart(), dummyMovement.getEind(), dummyMovement.getItem()));
                        //update(dummyMovement.getOperatie(), dummyMovement.getEind(), dummyMovement.getStart());
                        dummyMovement.setDone(true);
                        dummyMovement.setEindTijd(kraan.getAvailableTime());
                        kraan.addMovementInTime(dummyMovement);
                        check = true;
                        //dummyMovements.remove(0);
                    } else {
                        //wel botsing
                        //check of andere kraan aan het bewegen is
                        //if so => wacht
                        //else laat parallel bewegen
                        if (collision[1]) {
                            //TODO: geeft mss problemen
                            //DummyMovement d = dummyMovements.remove(0);
                            overschot.add(dummyMovement);
                            check = false;
                            checkTime(kraan);
                        } else {
                            int endSecondGantry = index == 0? dummyMovement.getEind().getXMin() - safetyDistance: dummyMovement.getEind().getXMax() + safetyDistance;
                            itemMovements.add(new ItemMovement(time, 0, endSecondGantry, gantries.get(index).getCurrentY(), null, gantries.get(index)));

                            dummyMovement.setStartTijd(time);
                            itemMovements.addAll(doMoves(time, kraan, dummyMovement.getStart(), dummyMovement.getEind(), dummyMovement.getItem()));
                            dummyMovement.setDone(true);
                            dummyMovement.setEindTijd(kraan.getAvailableTime());
                            kraan.addMovementInTime(dummyMovement);
                            check = true;
                        }
                    }
                } else {
                    //kraan is nog bezig => laat in queue staan
                    //DummyMovement d = dummyMovements.remove(0);
                    overschot.add(dummyMovement);
                    check = false;
                    time = kraan.getAvailableTime(); //TODO
                }
            }
            else{
                //kraan kiezen (eig enkel bij uitgraven
                //is kraan 2 beschikbaar
                if(gantries.get(1).getAvailableTime() <= time){
                    if(dummyMovement.getStart().getXMin() >= minX + safetyDistance) {
                        boolean[] collision = gantries.get(1).isCollision(gantries.get(0), dummyMovement);
                        if (!collision[0]) {
                            //alles ok => voer movement uit en verwijder uit queue
                            dummyMovement.setStartTijd(time);
                            itemMovements.addAll(doMoves(time, gantries.get(1), dummyMovement.getStart(), dummyMovement.getEind(), dummyMovement.getItem()));
                            //update(dummyMovement.getOperatie(), dummyMovement.getEind(), dummyMovement.getStart());
                            dummyMovement.setDone(true);
                            dummyMovement.setEindTijd(gantries.get(1).getAvailableTime());
                            gantries.get(1).addMovementInTime(dummyMovement);
                            check = true;
                        } else {
                            overschot.add(dummyMovement);
                            checkTime(gantries.get(1));
                            check = false;
                        }
                    }
                    else{
                        overschot.add(dummyMovement);
                        checkTime(gantries.get(1));
                        check = false;
                    }


                }
                else if(gantries.get(0).getAvailableTime() <= time){
                    if(dummyMovement.getStart().getXMax() <= maxX - safetyDistance) {
                        boolean[] collision = gantries.get(0).isCollision(gantries.get(1), dummyMovement);
                        if (!collision[0]) {
                            //alles ok => voer movement uit en verwijder uit queue
                            dummyMovement.setStartTijd(time);
                            itemMovements.addAll(doMoves(time, gantries.get(0), dummyMovement.getStart(), dummyMovement.getEind(), dummyMovement.getItem()));
                            //update(dummyMovement.getOperatie(), dummyMovement.getEind(), dummyMovement.getStart());
                            dummyMovement.setDone(true);
                            dummyMovement.setEindTijd(gantries.get(0).getAvailableTime());
                            gantries.get(0).addMovementInTime(dummyMovement);
                            check = true;
                        } else {
                            overschot.add(dummyMovement);
                            checkTime(gantries.get(1));
                            check = false;
                        }
                    }
                    else{
                        overschot.add(dummyMovement);
                        checkTime(gantries.get(0));
                        check = false;
                    }
                }

                //kan nog niet afhandelen => laat in q staan
                else{
                    //DummyMovement d = dummyMovements.remove(0);
                    overschot.add(dummyMovement);
                    check = false;
                    time = Math.min(gantries.get(0).getAvailableTime(), gantries.get(1).getAvailableTime());
                }

            }

        }

        return true;

        //als random leeg slot wordt gekozen waardoor kraan geblokt
        //wordt -> had een leeg slot kunnen zoeken in deel waar kraan wel aankan
    }


    private void checkTime(Gantry failed){
        Gantry otherGantry = gantries.indexOf(failed) == 0? gantries.get(1): gantries.get(0);
        if(otherGantry.getAvailableTime() > time)
            time = otherGantry.getAvailableTime();
    }



}
