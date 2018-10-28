package be.kul.gantry.domain;

import java.util.ArrayList;
import java.util.List;

public class ItemMovement {

    private int gID;
    private int x_destination;
    private int y_destination;
    private Integer itemID;
    double xyDuration = 0;
    double xDuration = 0;
    double yDuration = 0;
    private double time;


    public ItemMovement( double additionalTime, int x_destination, int y_destination, Integer itemID,Gantry gantry) {

        this.gID = gantry.getId();

        //absolute waarde bereken tijd kan niet negatief zijn
        xDuration = Math.abs(gantry.getX()- x_destination) / gantry.getXSpeed();
        yDuration = Math.abs((gantry.getY()- y_destination)/gantry.getYSpeed());

        //X tijd en y tijd vergelijken met elkaar daar de grootste van nemen om dat je wacht tot de traagste beweging klaar is
        if(xDuration <= yDuration) {
            xyDuration = yDuration;
        }
        else {
            xyDuration = xDuration;
        }

        //x en y kunnen tegelijk bewegen, z niet
        this.time = gantry.getTime() + additionalTime + xyDuration;
        this.x_destination = x_destination;
        this.y_destination = y_destination;
        this.itemID = itemID;

        //Kraan positie & tijd vernieuwen;
        gantry.setTime(time);
        gantry.setX(x_destination);
        gantry.setY(y_destination);

    }

    public String toString(){
        return gID + ";"+ time + ";"+ x_destination + ";"+ y_destination + ";" + itemID;
    }
}
