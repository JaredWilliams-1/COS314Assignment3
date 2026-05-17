import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        String trainFile = "Breast_train.csv";
        String testFile  = "Breast_test.csv";

        ArrayList<String> terminals = new ArrayList<>();
        terminals.add("0");
        terminals.add("1");

        ArrayList<String> functions = new ArrayList<>();

        // ===== GP PARAMETERS =====
        int   treeDepth              = 5;
        int   maxOffspringDepth      = 8;
        int   tournamentSize         = 7;
        float crossoverRate          = 0.75f;
        float mutationRate           = 0.15f;
        int   mutationOffspringDepth = 2;
        long  seed                   = 42L;

        LogicalGP gp = new LogicalGP(
                trainFile, terminals, functions,
                treeDepth, maxOffspringDepth, tournamentSize,
                crossoverRate, mutationRate, mutationOffspringDepth,
                seed);

        System.out.println("Training data loaded: " + gp.data.size() + " samples");
        System.out.println("Population size      : " + LogicalGP.POPULATION_SIZE);
        System.out.println("Max generations      : " + LogicalGP.MAX_GENERATIONS);

        gp.autoGenerateFunctionSet(4);
        System.out.println("Function set size    : " + gp.functionSet.size() + " predicates\n");

        long startTime = System.currentTimeMillis();
        Node bestTree = gp.build();
        long runtimeMs = System.currentTimeMillis() - startTime;

        float[] trainMetrics = gp.computeMetrics(bestTree, gp.data);
        System.out.println("\n=== Training Results ===");
        System.out.printf("Accuracy : %.4f%n", trainMetrics[0]);
        System.out.printf("F1 Score : %.4f%n", trainMetrics[1]);

        ArrayList<Data> testData = DataCollection.getCSVValues(testFile);
        System.out.println("\nTest data loaded: " + testData.size() + " samples");

        float[] testMetrics = gp.computeMetrics(bestTree, testData);
        System.out.println("\n=== Test Results ===");
        System.out.printf("Accuracy : %.4f%n", testMetrics[0]);
        System.out.printf("F1 Score : %.4f%n", testMetrics[1]);

        System.out.println("\n=== Runtime ===");
        System.out.printf("Total time : %d ms (%.2f s)%n", runtimeMs, runtimeMs / 1000.0);

        System.out.println("\n=== Best Tree ===");
        System.out.println(bestTree.TreeToString());
    }
}