package be.kul.gantry.domain;

/**
 * Created by ruben on 21/11/18.
 */
public class GeenPlaatsException extends Exception {

    public GeenPlaatsException(){
        super("Er is te weinig plaats in de storage ruimte.");
    }
}
