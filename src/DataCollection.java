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

                if (values.length == 0) continue;
                // Skip header lines that start with a non-numeric label like "class"
                if (values[0].trim().equalsIgnoreCase("class")) continue;

                if (values.length < 10) continue; // not enough columns

                Integer[] intValues = new Integer[values.length];
                boolean parseError = false;
                for (int i = 0; i < values.length; i++){
                    try {
                        intValues[i] = Integer.parseInt(values[i].trim());
                    } catch (NumberFormatException e) {
                        parseError = true;
                        break;
                    }
                }
                if (parseError) continue; // skip malformed lines

                Data data = new Data(
                    intValues[0], 
                    intValues[1], 
                    intValues[2], 
                    intValues[4],
                    intValues[3], 
                    intValues[5], 
                    intValues[6], 
                    intValues[9], 
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