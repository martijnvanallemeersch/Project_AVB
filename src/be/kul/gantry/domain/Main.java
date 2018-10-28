package be.kul.gantry.domain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Main {
    public static void main(String[] args){
        try{
            Problem problem = Problem.fromJson(new File("1_10_100_4_FALSE_65_50_50.json"));
            WriteToFile(problem);

        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    public static void WriteToFile(Problem problem)
    {
        try{
            File output = new File("output.csv");

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
