package be.kul.gantry.domain;

import java.util.ArrayList;

/**
 * Created by Wim on 27/04/2015.
 */
public class Slot {

    private final int id;
    private final int centerX, centerY, xMin, xMax, yMin, yMax, z;
    private Item item;
    private final SlotType type;
    private ArrayList<Slot> parents = new ArrayList<>();
    //private Slot parentR;
    private ArrayList<Slot> childs = new ArrayList<>();
    //private Slot childR;


    public Slot(int id, int centerX, int centerY, int xMin, int xMax, int yMin, int yMax, int z, SlotType type, Item item) {
        this.id = id;
        this.centerX = centerX;
        this.centerY = centerY;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.z = z;
        this.item = item;
        this.type = type;
        parents.add(null);
        parents.add(null);
        childs.add(null);
        childs.add(null);
    }

    public int getId() {
        return id;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getZ() {
        return z;
    }

    public int getXMin() {
        return xMin;
    }

    public int getXMax() {
        return xMax;
    }

    public int getYMin() {
        return yMin;
    }

    public int getYMax() {
        return yMax;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public SlotType getType() {
        return type;
    }

    public boolean isInputSlot() { return type == SlotType.INPUT; }

    public boolean isOutputSlot() { return type == SlotType.OUTPUT; }

    public boolean isStorageSlot() { return type == SlotType.STORAGE; }

    public void addChildL(Slot child){
        childs.remove(0);
        childs.add(0,child);
    }

    public void addChildR(Slot child){
        childs.remove(1);
        childs.add(1,child);
    }

    public void addParentL(Slot parent){
        parents.remove(0);
        parents.add(0, parent);
    }

    public void addParentR(Slot parent){
        parents.remove(1);
        parents.add(1, parent);
    }

    public ArrayList<Slot> getParents() {
        return parents;
    }

    public ArrayList<Slot> getChildren() {
        return childs;
    }

//    public Slot getParentL() {
//        return parentL;
//    }

//    public Slot getParentR() {
//        return parentR;
//    }
//
//    public void setParentL(Slot parentL) {
//        this.parentL = parentL;
//    }
//
//    public void setParentR(Slot parentR) {
//        this.parentR = parentR;
//    }
//
//    public Slot getChildL() {
//        return childL;
//    }
//
//    public Slot getChildR() {
//        return childR;
//    }
//
//    public void setChildL(Slot childL) {
//        this.childL = childL;
//    }
//
//    public void setChildR(Slot childR) {
//        this.childR = childR;
//    }

    @Override
    public String toString() {
        return String.format("Slot %d (%d,%d,%d)",id,centerX,centerY,z);
    }

    public static enum SlotType {
        INPUT,
        OUTPUT,
        STORAGE
    }
}
