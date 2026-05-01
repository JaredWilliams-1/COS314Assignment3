import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DataCollection {
    public static ArrayList<Data> getCSVValues(String filename){
        ArrayList<Data> output = new ArrayList<>();
        String line = "";
        
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Use comma as delimiter
                Integer[] intValues = new Integer[values.length];

                for (int i = 0; i < values.length; i++){
                    intValues[i] = Integer.parseInt(values[i]);
                }

                Data data = new Data(
                    intValues[0], 
                    intValues[1], 
                    intValues[2], 
                    intValues[3],
                    intValues[4], 
                    intValues[5], 
                    intValues[6], 
                    intValues[7], 
                    intValues[8]
                );
                output.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }
}
