package be.kul.gantry.domain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static be.kul.gantry.domain.Problem.safetyDistance;

/**
 * Created by Wim on 27/04/2015.
 */
public class Gantry {

    private final int id;
    private final int xMin,xMax;
    private final int startX,startY;
    private final double xSpeed,ySpeed;

    private int currentX, currentY;
    private int endX, endY;

    private int x,y;
    private double availableTime;
    private Item inGantry;

    private List<DummyMovement> movementlist = new LinkedList<>();

    public Gantry(int id, int xMin, int xMax, int startX, int startY, double xSpeed, double ySpeed) {
        this.id = id;
        this.xMin = xMin;
        this.xMax = xMax;
        this.startX = startX;
        this.startY = startY;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
    }

    public int getId() {
        return id;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void addAvailableTime(double t){
        availableTime += t;
    }

    public double getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(double availableTime) {
        this.availableTime = availableTime;
    }

    public int getXMax() {
        return xMax;
    }

    public int getXMin() {
        return xMin;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public double getXSpeed() {
        return xSpeed;
    }

    public double getYSpeed() {
        return ySpeed;
    }


    public int getX() {
        return x;
    }

    public List<DummyMovement> getMovements(){
        return movementlist;
    }

    public boolean[] isCollision(Gantry gantry, DummyMovement dummyMovement){

        double startTijd1 = dummyMovement.getStartTijd(); //availableTime; //normaal maakt niet uit of av time of current time neemt, vanaf av time staat kraan toch stil
        double eindTijd1 = dummyMovement.getEindTijd(); //startTijd1 + (currentX - dummyMovement.getEind().getCenterX())/xSpeed;

        //alle movements van andere kraan die overlappen met deze movement
        List<DummyMovement> t = gantry.getMovements().stream().filter(e -> (e.getStartTijd() >= startTijd1 && e.getStartTijd() <= eindTijd1) || (e.getEindTijd() <= eindTijd1 && e.getEindTijd() >= startTijd1)).collect(Collectors.toList());

        //als geen moves heeft => niet bewegen, voor collision moet checken waar kraan staat
        if (t.size() == 0) {
            boolean b = checkCollision(dummyMovement.getStart().getCenterX(), dummyMovement.getStartTijd(), dummyMovement.getEind().getCenterX(), dummyMovement.getEindTijd(), gantry.getCurrentX(), dummyMovement.getStartTijd(), gantry.getCurrentX(), dummyMovement.getEindTijd());
            return new boolean[]{b, false};
        }


        //gaten opvullen (als kraan stil staat)
        for(int i = 0; i<t.size()-1; i++){
               DummyMovement c = t.get(i);
               DummyMovement n = t.get(i+1);

               if(c.getEindTijd() != n.getStartTijd()){
                   DummyMovement d = new DummyMovement(n.getStart(), n.getStart(), null, gantry, null, null, null, null);
                   d.setStartTijd(c.getEindTijd());
                   d.setEindTijd(n.getStartTijd());
                   t.add(i+1, d);
               }
        }
        if(t.get(0).getStartTijd() > startTijd1){
            DummyMovement d = new DummyMovement(t.get(0).getStart(), t.get(0).getStart(), null, gantry, null, null, null, null);
            d.setStartTijd(startTijd1);
            d.setEindTijd(t.get(0).getStartTijd());
            t.add(0, d);
        }
        if(t.get(t.size()-1).getEindTijd() < eindTijd1){
            DummyMovement d = new DummyMovement(t.get(t.size()-1).getStart(), t.get(t.size()-1).getStart(), null, gantry, null, null, null, null);
            d.setStartTijd(t.get(t.size()-1).getEindTijd());
            d.setEindTijd(eindTijd1);
            t.add(t.size(), d);
        }

        /*//als 1e en laatste dummy resp starten en eindigen buiten par:dummyMovement => kraan stond stil
        // => ook checken of hier niet botst
        if(t.get(0).getStartTijd() > startTijd1) {
            boolean b = checkCollision(dummyMovement.getStart().getCenterX(), startTijd1, dummyMovement.getEind().getCenterX(), eindTijd1, t.get(0).getStart().getCenterX(), dummyMovement.getStartTijd(), t.get(0).getStart().getCenterX(), t.get(0).getStartTijd());
            if(b)
                return new boolean[]{true, false};
        }

        if(t.get(0).getEindTijd() < eindTijd1) {
            boolean b = checkCollision(dummyMovement.getStart().getCenterX(), startTijd1, dummyMovement.getEind().getCenterX(), eindTijd1, t.get(t.size()-1).getStart().getCenterX(), t.get(t.size()-1).getStartTijd(), t.get(t.size()-1).getStart().getCenterX(), dummyMovement.getEindTijd());
            if(b){
                if(t.get(0).getStartTijd() > startTijd1)
                    return new boolean[]{true, false};
                else{
                    return new boolean[]{true, true};
                }
            }
        }*/

        //tussenliggende move collision checken
        for(DummyMovement d: t) {
            boolean b = checkCollision(dummyMovement.getStart().getCenterX(), startTijd1, dummyMovement.getEind().getCenterX(), eindTijd1, d.getStart().getCenterX(), d.getStartTijd(), d.getEind().getCenterX(), d.getEindTijd());
            if(b){
                if(d.getStart().getCenterX() == d.getEind().getCenterX())
                    return new boolean[]{true, false};
                return new boolean[]{true, true};
            }
        }
        return new boolean[]{false, false};
    }

    private boolean checkCollision(int xA1v, double tA1v, int xB1v, double tB1v, int xA2v, double tA2v, int xB2v, double tB2v){
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

        if((xA2 <= speed1*(tA2 - tB1) + (xB1 + safetyDistance) && xA2 >= speed1*(tA2 - tA1) + (xA1 - safetyDistance)) || speed1 == speed2) {
            if (xA2 >= speed2 * (tA2 - tA1) + (xA1 - safetyDistance) && xB2 <= speed2 * (tB2 - tA2) + (xA1 + safetyDistance)) {
                return true;
            }
        }

        return false;

    }

    public void addMovementInTime(DummyMovement dummyMovement){
        movementlist.add(dummyMovement);
    }

    public void removeUntilTime(double time){

    }


    public boolean overlapsGantryArea(Gantry g) {
        return g.xMin < xMax && xMin < g.xMax;
    }

    public int[] getOverlapArea(Gantry g) {

        int maxmin = Math.max(xMin, g.xMin);
        int minmax = Math.min(xMax, g.xMax);

        if (minmax < maxmin)
            return null;
        else
            return new int[]{maxmin, minmax};
    }

    public boolean canReachSlot(Slot s) {
        return xMin <= s.getCenterX() && s.getCenterX() <= xMax;
    }

    public int getCurrentX() {
        return currentX;
    }

    public void setCurrentX(int currentX) {
        this.currentX = currentX;
    }

    public int getCurrentY() {
        return currentY;
    }

    public void setCurrentY(int getCurrentY) {
        this.currentY = currentY;
    }

    public int getEndX() {
        return endX;
    }

    public void setEndX(int endX) {
        this.endX = endX;
    }

    public int getEndY() {
        return endY;
    }

    public void setEndY(int endY) {
        this.endY = endY;
    }
}
