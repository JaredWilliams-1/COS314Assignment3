import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        String trainFile = "excel/Breast_train.csv";
        String testFile  = "excel/Breast_test.csv";

        ArrayList<String> terminals = new ArrayList<>();
        terminals.add("0");
        terminals.add("1");

        ArrayList<String> functions = new ArrayList<>();
        functions.add("age<=1");       functions.add("age>1");
        functions.add("age<=2");       functions.add("age>2");
        functions.add("age<=3");       functions.add("age>3");
        functions.add("age<=4");       functions.add("age>4");
        functions.add("menopause<=0"); functions.add("menopause>0");
        functions.add("menopause<=1"); functions.add("menopause>1");
        functions.add("tumorSize<=2"); functions.add("tumorSize>2");
        functions.add("tumorSize<=4"); functions.add("tumorSize>4");
        functions.add("tumorSize<=6"); functions.add("tumorSize>6");
        functions.add("tumorSize<=8"); functions.add("tumorSize>8");
        functions.add("invNodes<=0");  functions.add("invNodes>0");
        functions.add("invNodes<=1");  functions.add("invNodes>1");
        functions.add("invNodes<=3");  functions.add("invNodes>3");
        functions.add("nodeCaps<=0");  functions.add("nodeCaps>0");
        functions.add("degMalig<=1");  functions.add("degMalig>1");
        functions.add("degMalig<=2");  functions.add("degMalig>2");
        functions.add("breast<=0");    functions.add("breast>0");
        functions.add("breastQuad<=1");functions.add("breastQuad>1");
        functions.add("breastQuad<=2");functions.add("breastQuad>2");
        functions.add("breastQuad<=3");functions.add("breastQuad>3");
        functions.add("irradiat<=0");  functions.add("irradiat>0");

        // Tuned parameters
        int   populationSize    = 200;
        int   treeDepth         = 3;    // was 4: shallower trees generalise better
        int   mutOffspringDepth = 2;    // was 3
        int   tournamentSize    = 3;    // was 5: less selection pressure -> more diversity
        float crossoverRate     = 0.80f;
        float mutationRate      = 0.30f;// was 0.20: more exploration
        int   maxGenerations    = 100;
        int   seed              = 42;

        LogicalGP gp = new LogicalGP(
                trainFile, terminals, functions,
                treeDepth, populationSize, tournamentSize,
                crossoverRate, mutationRate, mutOffspringDepth,
                maxGenerations, seed);

        Node best = gp.run();

        float[] trainMetrics = gp.computeMetrics(best, gp.data);
        System.out.println("\n=== Training Results ===");
        System.out.printf("Training accuracy : %.4f%n", trainMetrics[0]);
        System.out.printf("Training F1-score : %.4f%n", trainMetrics[1]);
        System.out.println("Best tree: " + best.TreeToString());

        ArrayList<Data> testData = DataCollection.getCSVValues(testFile);
        float[] testMetrics = gp.computeMetrics(best, testData);
        System.out.println("\n=== Test Results ===");
        System.out.printf("Test accuracy : %.4f%n", testMetrics[0]);
        System.out.printf("Test F1-score : %.4f%n", testMetrics[1]);
    }
}
