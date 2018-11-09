package be.kul.gantry.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by Wim on 27/04/2015.
 */
public class Problem {

    private final int minX, maxX, minY, maxY;
    private final int maxLevels;
    private final List<Item> items;
    private final List<Job> inputJobSequence;
    private final List<Job> outputJobSequence;

    private final List<Gantry> gantries;
    private final List<Slot> slots;
    private final int safetyDistance;
    private final int pickupPlaceDuration;

    //We maken een 2D Array aan voor alle bodem slots (z=0)
    private HashMap<Integer, HashMap<Integer, Slot>> grondSlots = new HashMap<>();
    private HashMap<Integer, HashMap<Integer, Slot>> slotsNiveau1 = new HashMap<>();
    private HashMap<Integer, HashMap<Integer, Slot>> slotsNiveau2 = new HashMap<>();
    private HashMap<Integer, HashMap<Integer, Slot>> slotsNiveau3 = new HashMap<>();


    //We vullen de array met nieuwe arrays
    private HashMap<Integer, Slot> itemSlotLocation = new HashMap<>();

    public enum Operatie {
        VerplaatsIntern,
        VerplaatsNaarOutput,
        VerplaatsNaarBinnen
    }

    public Problem(int minX, int maxX, int minY, int maxY, int maxLevels, List<Item> items, List<Job> inputJobSequence, List<Job> outputJobSequence, List<Gantry> gantries, List<Slot> slots, int safetyDistance, int pickupPlaceDuration) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.maxLevels = maxLevels;
        this.items = items;
        this.inputJobSequence = inputJobSequence;
        this.outputJobSequence = outputJobSequence;
        this.gantries = gantries;
        this.slots = slots;
        this.safetyDistance = safetyDistance;
        this.pickupPlaceDuration = pickupPlaceDuration;
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxLevels() {
        return maxLevels;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<Job> getInputJobSequence() {
        return inputJobSequence;
    }

    public List<Job> getOutputJobSequence() {
        return outputJobSequence;
    }

    public List<Gantry> getGantries() {
        return gantries;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public int getSafetyDistance() {
        return safetyDistance;
    }

    public int getPickupPlaceDuration() {
        return pickupPlaceDuration;
    }

    public HashMap<Integer, Slot> getItemSlotLocation() {
        return itemSlotLocation;
    }

    public void setItemSlotLocation(HashMap<Integer, Slot> itemSlotLocation) {
        this.itemSlotLocation = itemSlotLocation;
    }

    public void writeJsonFile(File file) throws IOException {
        JSONObject root = new JSONObject();

        JSONObject parameters = new JSONObject();
        root.put("parameters",parameters);

        parameters.put("gantrySafetyDistance",safetyDistance);
        parameters.put("maxLevels",maxLevels);
        parameters.put("pickupPlaceDuration",pickupPlaceDuration);

        JSONArray items = new JSONArray();
        root.put("items",items);

        for(Item item : this.items) {
            JSONObject jo = new JSONObject();
            jo.put("id",item.getId());

            items.add(jo);
        }


        JSONArray slots = new JSONArray();
        root.put("slots",slots);
        for(Slot slot : this.slots) {
            JSONObject jo = new JSONObject();
            jo.put("id",slot.getId());
            jo.put("cx",slot.getCenterX());
            jo.put("cy",slot.getCenterY());
            jo.put("minX",slot.getXMin());
            jo.put("maxX",slot.getXMax());
            jo.put("minY",slot.getYMin());
            jo.put("maxY",slot.getYMax());
            jo.put("z",slot.getZ());
            jo.put("type",slot.getType().name());
            jo.put("itemId",slot.getItem() == null ? null : slot.getItem().getId());

            slots.add(jo);
        }

        JSONArray gantries = new JSONArray();
        root.put("gantries",gantries);
        for(Gantry gantry : this.gantries) {
            JSONObject jo = new JSONObject();

            jo.put("id",gantry.getId());
            jo.put("xMin",gantry.getXMin());
            jo.put("xMax",gantry.getXMax());
            jo.put("startX",gantry.getStartX());
            jo.put("startY",gantry.getStartY());
            jo.put("xSpeed",gantry.getXSpeed());
            jo.put("ySpeed",gantry.getYSpeed());

            gantries.add(jo);
        }

        JSONArray inputSequence = new JSONArray();
        root.put("inputSequence",inputSequence);

        for(Job inputJ : this.inputJobSequence) {
            JSONObject jo = new JSONObject();
            jo.put("itemId",inputJ.getItem().getId());
            jo.put("fromId",inputJ.getPickup().getSlot().getId());

            inputSequence.add(jo);
        }

        JSONArray outputSequence = new JSONArray();
        root.put("outputSequence",outputSequence);

        for(Job outputJ : this.outputJobSequence) {
            JSONObject jo = new JSONObject();
            jo.put("itemId",outputJ.getItem().getId());
            jo.put("toId",outputJ.getPlace().getSlot().getId());

            outputSequence.add(jo);
        }

        try(FileWriter fw = new FileWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            fw.write(gson.toJson(root));
        }

    }

    public static Problem fromJson(File file) throws IOException, ParseException {


        JSONParser parser = new JSONParser();

        try(FileReader reader = new FileReader(file)) {
            JSONObject root = (JSONObject) parser.parse(reader);

            List<Item> itemList = new ArrayList<>();
            List<Slot> slotList = new ArrayList<>();
            List<Gantry> gantryList = new ArrayList<>();
            List<Job> inputJobList = new ArrayList<>();
            List<Job> outputJobList = new ArrayList<>();

            JSONObject parameters = (JSONObject) root.get("parameters");
            int safetyDist = ((Long)parameters.get("gantrySafetyDistance")).intValue();
            int maxLevels = ((Long)parameters.get("maxLevels")).intValue();
            int pickupPlaceDuration = ((Long)parameters.get("pickupPlaceDuration")).intValue();

            JSONArray items = (JSONArray) root.get("items");
            for(Object o : items) {
                int id = ((Long)((JSONObject)o).get("id")).intValue();

                Item c = new Item(id);
                itemList.add(c);
            }

            int overallMinX = Integer.MAX_VALUE, overallMaxX = Integer.MIN_VALUE;
            int overallMinY = Integer.MAX_VALUE, overallMaxY = Integer.MIN_VALUE;

            JSONArray slots = (JSONArray) root.get("slots");

            for(Object o : slots) {
                JSONObject slot = (JSONObject) o;

                int id = ((Long)slot.get("id")).intValue();
                int cx = ((Long)slot.get("cx")).intValue();
                int cy = ((Long)slot.get("cy")).intValue();
                int minX = ((Long)slot.get("minX")).intValue();
                int minY = ((Long)slot.get("minY")).intValue();
                int maxX = ((Long)slot.get("maxX")).intValue();
                int maxY = ((Long)slot.get("maxY")).intValue();
                int z = ((Long)slot.get("z")).intValue();

                overallMinX = Math.min(overallMinX,minX);
                overallMaxX = Math.max(overallMaxX,maxX);
                overallMinY = Math.min(overallMinY,minY);
                overallMaxY = Math.max(overallMaxY,maxY);

                Slot.SlotType type = Slot.SlotType.valueOf((String)slot.get("type"));
                Integer itemId = slot.get("itemId") == null ? null : ((Long)slot.get("itemId")).intValue();
                Item c = itemId == null ? null : itemList.get(itemId);

                Slot s = new Slot(id,cx,cy,minX,maxX,minY,maxY,z,type,c);
                slotList.add(s);
            }


            JSONArray gantries = (JSONArray) root.get("gantries");
            for(Object o : gantries) {
                JSONObject gantry = (JSONObject) o;

                int id = ((Long)gantry.get("id")).intValue();
                int xMin = ((Long)gantry.get("xMin")).intValue();
                int xMax = ((Long)gantry.get("xMax")).intValue();
                int startX = ((Long)gantry.get("startX")).intValue();
                int startY = ((Long)gantry.get("startY")).intValue();
                double xSpeed = ((Double)gantry.get("xSpeed")).doubleValue();
                double ySpeed = ((Double)gantry.get("ySpeed")).doubleValue();

                Gantry g = new Gantry(id, xMin, xMax, startX, startY, xSpeed, ySpeed);
                gantryList.add(g);
            }

            JSONArray inputJobs = (JSONArray) root.get("inputSequence");
            int jid = 0;
            for(Object o : inputJobs) {
                JSONObject inputJob = (JSONObject) o;

                int iid = ((Long) inputJob.get("itemId")).intValue();
                int sid = ((Long) inputJob.get("fromId")).intValue();

                Job job = new Job(jid++,itemList.get(iid),slotList.get(sid),null);
                inputJobList.add(job);
            }

            JSONArray outputJobs = (JSONArray) root.get("outputSequence");
            for(Object o : outputJobs) {
                JSONObject outputJob = (JSONObject) o;

                int iid = ((Long) outputJob.get("itemId")).intValue();
                int sid = ((Long) outputJob.get("toId")).intValue();

                Job job = new Job(jid++,itemList.get(iid),null, slotList.get(sid));
                outputJobList.add(job);
            }


            return new Problem(
                    overallMinX,
                    overallMaxX,
                    overallMinY,
                    overallMaxY,
                    maxLevels,
                    itemList,
                    inputJobList,
                    outputJobList,
                    gantryList,
                    slotList,
                    safetyDist,
                    pickupPlaceDuration
                    );

        }

    }

    // hier wordt de parent child link gemaakt dus alle grondsloten met hun ouders dus setparent en setchild van ieder slot
    private void MakeParentChildLink(Slot slot) {

        Slot child = grondSlots.get(slot.getCenterY() / 10).get(slot.getCenterX() / 10);

        // we stijgen telkens tot op de hoogste z
        while(child.getParent() != null)
        {
            child = child.getParent();
        }

        //Als we de child gevonden hebben zetten we de link
        child.setParent(slot);
        slot.setChild(child);
    }

    // Eerst proberen we outputjobs uit te voeren tot deze bepaalde items nodig heeft die nog niet in het veld staan,
    // dan schakelen we over op inputjobs tot dat item voor de outputjob gevonden is
    public List<ItemMovement> werkUit() {

        // Alle grondslots enzo verzamellen
        for(Slot slot: slots) {

            // Als de Z gelijk is aan nul weten we dat het slot zich op de grond bevindt
            if (slot.getZ() == 0) {
                //initialisatie (if grondslot get(slotcenterY) geen value heeft maak een value new Hashmap)
                grondSlots.computeIfAbsent(slot.getCenterY() / 10, s -> new HashMap<>());
                //Er zijn ook input en output slots dus enkel bij storage
                if (slot.getType().equals(Slot.SlotType.STORAGE))  grondSlots.get(slot.getCenterY() / 10).put(slot.getCenterX() / 10, slot);
            }
            else if(slot.getZ() == 1)
            {
                //initialisatie (if grondslot get(slotcenterY) geen value heeft maak een value new Hashmap)
                slotsNiveau1.computeIfAbsent(slot.getCenterY() / 10, s -> new HashMap<>());
                //Er zijn ook input en output slots dus enkel bij storage
                if (slot.getType().equals(Slot.SlotType.STORAGE))  slotsNiveau1.get(slot.getCenterY() / 10).put(slot.getCenterX() / 10, slot);

                //MakeParentChildLink(slot);
            }
            else if(slot.getZ() == 2)
            {
                //initialisatie (if grondslot get(slotcenterY) geen value heeft maak een value new Hashmap)
                slotsNiveau2.computeIfAbsent(slot.getCenterY() / 10, s -> new HashMap<>());
                //Er zijn ook input en output slots dus enkel bij storage
                if (slot.getType().equals(Slot.SlotType.STORAGE))  slotsNiveau2.get(slot.getCenterY() / 10).put(slot.getCenterX() / 10, slot);
            }
            else if(slot.getZ() == 3)
            {
                //initialisatie (if grondslot get(slotcenterY) geen value heeft maak een value new Hashmap)
                slotsNiveau3.computeIfAbsent(slot.getCenterY() / 10, s -> new HashMap<>());
                //Er zijn ook input en output slots dus enkel bij storage
                if (slot.getType().equals(Slot.SlotType.STORAGE))  slotsNiveau3.get(slot.getCenterY() / 10).put(slot.getCenterX() / 10, slot);
            }


            //Wanneer het slot gevuld is in een hashmap steken
            if (slot.getItem() != null) itemSlotLocation.put(slot.getItem().getId(), slot);
        }

        // tweede keer over uw slotten lopen
        for(Slot slot: slots) {
            if (slot.getZ() != 0) {
                MakeParentChildLink(slot);
            }
        }

            List<ItemMovement> itemMovements = new ArrayList<>();
        int inputJobCounter = 0;
        int outputJobCounter = 0;

        //We beginnen met het uitvoeren van de outputjobs
        while(outputJobCounter < outputJobSequence.size()) {
            Job outputJob = outputJobSequence.get(outputJobCounter++);

            Item item = outputJob.getItem();
            Slot slot = itemSlotLocation.get(item.getId());

            //kijken of het in field zit.
            if(slot != null) {
                //Als het item dat we nodig hebben containers op hem heeft staan eerste deze uitgraven en nieuwe plaats geven
                if(slot.getParent() != null && slot.getParent().getItem() != null){
                    itemMovements.addAll(uitGraven(slot.getParent(), gantries.get(0)));
                }

                //De verplaatsingen nodig om de outputjob te vervolledigen en alle sloten updaten met hun huidige items
                itemMovements.addAll(GeneralMeasures.doMoves(pickupPlaceDuration, gantries.get(0), slot, outputJob.getPlace().getSlot()));
                update(Operatie.VerplaatsNaarOutput, outputJob.getPlace().getSlot(),slot);
            }
            else {
                //AANGEPAST
                //een nieuw inputjob doen tot het item gevonden is
                //origineel ontwerp: plaatste het gewenste output item eerst in de storage
                // om direct daarna hem terug op te nemen voor naar de output te brengen?
                // dit beweegt direct van input slot naar output slot
               while(slot == null) {
                    Job inputJob = inputJobSequence.get(inputJobCounter);

                    if (inputJob.getItem().getId() != item.getId()) {
                        arrangeInputJob(inputJob, itemMovements);
                    } else {
                        slot = inputJob.getPickup().getSlot();
                        Slot outputSlot = outputJob.getPlace().getSlot();
                        itemMovements.addAll(GeneralMeasures.doMoves(pickupPlaceDuration,gantries.get(0), slot, outputSlot));
                        update(Operatie.VerplaatsNaarBinnen, outputSlot, slot);

                    }

                    inputJobCounter++;
                    //ook input item op input slot zetten, anders kan niet rechtstreeks van inputslot naar outputslot
                    // bewegen (als output item net aan begin van input staat)
                    if(inputJobCounter < inputJobSequence.size())
                        inputJobSequence.get(inputJobCounter).getPickup().getSlot().setItem(inputJobSequence.get(inputJobCounter).getItem());
                }
            }
        }

        //eventueele overblijvende inputjobs uitvoeren
        while( inputJobCounter < inputJobSequence.size()){
            arrangeInputJob(inputJobSequence.get(inputJobCounter), itemMovements);
            inputJobCounter++;

            //ook input item op input slot zetten, anders kan niet rechtstreeks van inputslot naar outputslot
            // bewegen (als output item net aan begin van input staat)
            if(inputJobCounter < inputJobSequence.size())
                inputJobSequence.get(inputJobCounter).getPickup().getSlot().setItem(inputJobSequence.get(inputJobCounter).getItem());

        }
        return itemMovements;
    }

    //inputjob uitvoeren
    //mss veiliger om Job mee te geven ipv jobcounter zodat counter niet per ongelijk in methode kan veranderd worden
    private void arrangeInputJob(Job inputJob, List<ItemMovement> itemMovements) {

        //overige inputjobs afwerken
        //int row = 0;
        Slot destination = GeneralMeasures.zoekLeegSlot(new HashSet<>(grondSlots.get(0).values()));

        //De verplaatsingen nodig om de outputjob te vervolledigen en alle sloten updaten met hun huidige items
        inputJob.getPickup().getSlot().setItem(inputJob.getItem());
        itemMovements.addAll(GeneralMeasures.doMoves(pickupPlaceDuration,gantries.get(0),inputJob.getPickup().getSlot(), destination));
        update(Operatie.VerplaatsNaarBinnen, destination,inputJob.getPickup().getSlot());
    }

    private void update(Operatie operatie, Slot destination, Slot startSlot){
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


    //Deze functie graaft een bepaald slot dat we nodig hebben uit en verplaatst al de bovenliggende sloten.
    private List<ItemMovement> uitGraven(Slot slot, Gantry gantry){

        List<ItemMovement> itemMovements = new ArrayList<>();

        //Recursief naar boven gaan, doordat we namelijk eerste de gevulde parents van een bepaald slot moeten uithalen
        if(slot.getParent() != null ){
            if( slot.getParent().getItem() != null)
            {
                List<ItemMovement> tussen =  uitGraven(slot.getParent(), gantry);
                itemMovements.addAll(tussen);
            }
        }

        //Slot in een zo dicht mogelijke rij zoeken
        Slot newSlot = GeneralMeasures.zoekSlot(slot,grondSlots);

        //verplaatsen
        itemMovements.addAll(GeneralMeasures.doMoves(pickupPlaceDuration,gantry, slot, newSlot));
        update(Operatie.VerplaatsIntern, newSlot, slot);

        return itemMovements;
    }
}
