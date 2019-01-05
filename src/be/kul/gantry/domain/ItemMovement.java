package be.kul.gantry.domain;

import static be.kul.gantry.domain.Problem.gantries;
import static be.kul.gantry.domain.Problem.itemMovements;

public class ItemMovement {

    private int gID;
    private int x_destination;
    private int y_destination;
    private Integer itemID;
    double xyDuration = 0;
    double xDuration = 0;
    double yDuration = 0;
    private double endTime;
    private double startTime;
    private int x_start;
    private int y_start;


    public ItemMovement(double startTijd, double additionalTime, int x_start, int y_start, int x_destination, int y_destination, Integer itemID, Gantry gantry) {

        this.startTime = startTijd;
        this.x_start = x_start;
        this.y_start = y_start;
        this.x_destination = x_destination;
        this.y_destination = y_destination;
        this.itemID = itemID;

        if(gantry != null) {
            this.gID = gantry.getId();

            //absolute waarde bereken tijd kan niet negatief zijn
            xDuration = Math.abs(x_start - x_destination) / gantry.getXSpeed();
            yDuration = Math.abs((y_start - y_destination) / gantry.getYSpeed());

            //X tijd en y tijd vergelijken met elkaar daar de grootste van nemen om dat je wacht tot de traagste beweging klaar is
            if (xDuration <= yDuration) {
                xyDuration = yDuration;
            } else {
                xyDuration = xDuration;
            }

            //x en y kunnen tegelijk bewegen, z niet
            if (startTijd == -1)
                startTime = gantry.getTime();
            double endTime = startTime + additionalTime + xyDuration;
            this.endTime = endTime;


            gantry.setTime(endTime);
            gantry.setX(x_destination);
            gantry.setY(y_destination);
            gantry.addMovement(this);

        }

    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public int getX_destination() {
        return x_destination;
    }

    public int getY_destination() {
        return y_destination;
    }

    public int getX_start() {
        return x_start;
    }

    public int getY_start() {
        return y_start;
    }

    public int getgID() {
        return gID;
    }

    public void setgID(int gID) {
        this.gID = gID;
    }

    public String toString(){
        return gID + ";"+ endTime + ";"+ x_destination + ";"+ y_destination + ";" + itemID;
    }
}
