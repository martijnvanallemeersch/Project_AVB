package be.kul.gantry.domain;

public class ItemMovement {

    private int gID;
    private int x_destination;
    private int y_destination;
    private Integer itemID;
    private double xyDuration = 0;
    private double xDuration = 0;
    private double yDuration = 0;
    private double startTime;
    private double endTime;
    private double duration;


    public ItemMovement(double startTime, double additionalTime, int x_destination, int y_destination, Integer itemID, Gantry gantry) {

        this.gID = gantry.getId();
        this.startTime = startTime;

        //absolute waarde bereken tijd kan niet negatief zijn
        xDuration = Math.abs(gantry.getX() - x_destination) / gantry.getXSpeed();
        yDuration = Math.abs((gantry.getY() - y_destination)/gantry.getYSpeed());

        //X tijd en y tijd vergelijken met elkaar daar de grootste van nemen om dat je wacht tot de traagste beweging klaar is
        if(xDuration <= yDuration) {
            xyDuration = yDuration;
        }
        else {
            xyDuration = xDuration;
        }

        duration = additionalTime + xyDuration;

        //x en y kunnen tegelijk bewegen, z niet
        this.endTime = startTime + duration;
        this.x_destination = x_destination;
        this.y_destination = y_destination;
        this.itemID = itemID;

        //Kraan positie & tijd vernieuwen;
        gantry.setAvailableTime(endTime);
        gantry.setX(x_destination);
        gantry.setY(y_destination);

    }

    public double getEndTime() {
        return endTime;
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

    public double getDuration() {
        return duration;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }
}
