package be.kul.gantry.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static be.kul.gantry.domain.Problem.gantries;

/**
 * Created by ruben on 29/12/18.
 */
public class MovementList {
    private List<ItemMovement> itemMovements = new ArrayList<>();


    public void addAll(Collection<? extends ItemMovement> movements){
        for(ItemMovement movement: movements){
            add(movement);
        }

    }

    public void add(ItemMovement movement){
        if(!itemMovements.isEmpty()) {
            ItemMovement lastMove = null;
            for(int i = itemMovements.size()-1; i>=0; i--){
                ItemMovement t = itemMovements.get(i);
                if(t.getgID() == movement.getgID()){
                    lastMove = t;
                    break;
                }
            }

            if(lastMove != null) {
                double endTime = lastMove.getEndTime();
                if (endTime != movement.getStartTime()) {
                    ItemMovement it = new ItemMovement(endTime, 0, lastMove.getX_destination(), lastMove.getY_destination(), lastMove.getX_destination(), lastMove.getY_destination(), null, null);
                    it.setgID(lastMove.getgID()); //vuile truc
                    it.setEndTime(movement.getStartTime());
                    itemMovements.add(it);

                }
            }
        }

        itemMovements.add(movement);
    }

    public List<ItemMovement> getMovements(){
        return itemMovements;
    }
}
