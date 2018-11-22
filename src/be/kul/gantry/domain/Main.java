package be.kul.gantry.domain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Main {
    private static final String inputfile = "1_10_100_4_TRUE_65_50_50.json";
    private static final String outputfile = "output.csv";

    public static void main(String[] args){
        try{
            Problem problem = Problem.fromJson(new File(inputfile));
            WriteToFile(problem);

        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    public static void WriteToFile(Problem problem)
    {
        try{
            File output = new File(outputfile);
            FileOutputStream fos = new FileOutputStream(output);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write("\"gID\";\"T\";\"x\";\"y\";\"itemsInCraneID\"");

            for(ItemMovement m: problem.werkUit()){
                osw.write("\n");
                osw.write(m.toString());
            }
            osw.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
