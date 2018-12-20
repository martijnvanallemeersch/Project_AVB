package be.kul.gantry.domain;

import java.util.Set;

/**
 * Created by ruben on 6/12/18.
 */
public class DummyMovement {

    private Slot start;
    private Slot eind;
    private double startTijd;
    private double eindTijd;
    private Gantry startGantry;
    private Gantry gantry1;
    private Gantry gantry2;
    private boolean done = false;
    private Problem.Operatie operatie;
    private Item item;

    private Set<DummyMovement> preDoneMovements;

    //startGantry =  met welke krqqn moet reauest gedqqn worden
    public DummyMovement(Slot start, Slot eind, Item item, Gantry startGantry, Set<DummyMovement> dummyMovements, Gantry gantry1, Gantry gantry2, Problem.Operatie operatie) {
        this.start = start;
        this.eind = eind;
        this.startGantry = startGantry;
        preDoneMovements = dummyMovements;
        this.gantry1 = gantry1;
        this.gantry2 = gantry2;
        this.operatie = operatie;
        this.item = item;
    }

    public boolean checkConditional(){
        if(preDoneMovements != null)
            for(DummyMovement dummyMovement: preDoneMovements) {
                if (!dummyMovement.isDone())
                    return false;
            }
        return true;
    }

    public Slot getStart() {
        return start;
    }

    public void setStart(Slot start) {
        this.start = start;
    }

    public Slot getEind() {
        return eind;
    }

    public void setEind(Slot eind) {
        this.eind = eind;
    }

    public Gantry getStartGantry() {
        return startGantry;
    }

    public void setStartGantry(Gantry startGantry) {
        this.startGantry = startGantry;
    }

    public Gantry getGantry1() {
        return gantry1;
    }

    public void setGantry1(Gantry gantry1) {
        this.gantry1 = gantry1;
    }

    public Gantry getGantry2() {
        return gantry2;
    }

    public void setGantry2(Gantry gantry2) {
        this.gantry2 = gantry2;
    }

    public Set<DummyMovement> getPreDoneMovements() {
        return preDoneMovements;
    }

    public void setPreDoneMovements(Set<DummyMovement> preDoneMovements) {
        this.preDoneMovements = preDoneMovements;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Problem.Operatie getOperatie() {
        return operatie;
    }

    public void setOperatie(Problem.Operatie operatie) {
        this.operatie = operatie;
    }

    public double getStartTijd() {
        return startTijd;
    }

    public void setStartTijd(double startTijd) {
        this.startTijd = startTijd;
    }

    public double getEindTijd() {
        return eindTijd;
    }

    public void setEindTijd(double eindTijd) {
        this.eindTijd = eindTijd;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
