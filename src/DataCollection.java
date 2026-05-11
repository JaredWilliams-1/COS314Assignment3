import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DataCollection {
    public static ArrayList<Data> getCSVValues(String filename) {
        ArrayList<Data> output = new ArrayList<>();
        String line;
        boolean firstLine = true;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                String[] values = line.split(",");
                if (values.length < 10) continue;
                output.add(new Data(
                    Integer.parseInt(values[0].trim()),
                    Integer.parseInt(values[1].trim()),
                    Integer.parseInt(values[2].trim()),
                    Integer.parseInt(values[3].trim()),
                    Integer.parseInt(values[4].trim()),
                    Integer.parseInt(values[5].trim()),
                    Integer.parseInt(values[6].trim()),
                    Integer.parseInt(values[7].trim()),
                    Integer.parseInt(values[8].trim()),
                    Integer.parseInt(values[9].trim())
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }
}
