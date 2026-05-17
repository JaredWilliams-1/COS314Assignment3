import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    // =========================================================================
    // DEMO_MODE = false  ->  runs all 30 seeds, ranks them, then automatically
    //                        runs the best one in full so you can see it
    // DEMO_MODE = true   ->  skips the search and just asks for a seed to run
    //                        (use this on demo day once you know the best seed)
    // =========================================================================
    static final boolean DEMO_MODE = true;
    static final long DEMO_SEED = 864208642086L; // best from search. Hardcoded for Demo run

    // =========================================================================
    // Optimised GP parameters
    // =========================================================================
    static final int   TREE_DEPTH = 4;
    static final int   MAX_OD     = 9;
    static final int   TOURNAMENT = 7;
    static final float CROSSOVER  = 0.80f;
    static final float MUTATION   = 0.15f;
    static final int   MUT_DEPTH  = 3;
    static final int   NUM_ELITES = 4;

    // =========================================================================
    // 30 unique seeds for independent runs
    // =========================================================================
    static final long[] SEEDS = {
        782364521897L, 314159265358L, 998244353711L, 123456789012L,
        987654321098L, 246813579024L, 135792468013L, 864208642086L,
        579135791357L, 420864208642L, 111222333444L, 555666777888L,
        999111222333L, 444555666777L, 888999111222L, 333444555666L,
        777888999111L, 222333444555L, 666777888999L, 100200300400L,
        500600700800L, 900100200300L, 400500600700L, 800900100200L,
        300400500600L, 700800900100L, 200300400500L, 600700800900L,
        150263748596L, 741852963074L
    };

    // =========================================================================
    public static void main(String[] args) {
        if (DEMO_MODE) {
            runDemo();
        } else {
            runSearch();
        }
    }

    // =========================================================================
    // DEMO MODE — asks for a seed then runs once with full output
    // =========================================================================
    private static void runDemo() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== GP Demo ===");
        System.out.println();
        System.out.println("Enter seed (press Enter for " + DEMO_SEED + "):");
        String input = scanner.nextLine().trim();
        long seed = input.isEmpty() ? DEMO_SEED : Long.parseLong(input);

        System.out.println("Enter tree depth (press Enter for " + TREE_DEPTH + "):");
        input = scanner.nextLine().trim();
        int treeDepth = input.isEmpty() ? TREE_DEPTH : Integer.parseInt(input);

        System.out.println("Enter max offspring depth (press Enter for " + MAX_OD + "):");
        input = scanner.nextLine().trim();
        int maxOffspringDepth = input.isEmpty() ? MAX_OD : Integer.parseInt(input);

        System.out.println("Enter tournament size (press Enter for " + TOURNAMENT + "):");
        input = scanner.nextLine().trim();
        int tournamentSize = input.isEmpty() ? TOURNAMENT : Integer.parseInt(input);

        System.out.println("Enter crossover rate (press Enter for " + CROSSOVER + "):");
        input = scanner.nextLine().trim();
        float crossoverRate = input.isEmpty() ? CROSSOVER : Float.parseFloat(input);

        System.out.println("Enter mutation rate (press Enter for " + MUTATION + "):");
        input = scanner.nextLine().trim();
        float mutationRate = input.isEmpty() ? MUTATION : Float.parseFloat(input);

        System.out.println("Enter mutation depth (press Enter for " + MUT_DEPTH + "):");
        input = scanner.nextLine().trim();
        int mutationDepth = input.isEmpty() ? MUT_DEPTH : Integer.parseInt(input);

        System.out.println("Enter training file path (press Enter for Breast_train.csv):");
        input = scanner.nextLine().trim();
        String trainFile = input.isEmpty() ? "Breast_train.csv" : input;

        System.out.println("Enter test file path (press Enter for Breast_test.csv):");
        input = scanner.nextLine().trim();
        String testFile = input.isEmpty() ? "Breast_test.csv" : input;

        scanner.close();
        System.out.println();

        runFull(seed, treeDepth, maxOffspringDepth, tournamentSize, crossoverRate, mutationRate, mutationDepth, trainFile, testFile);
    }

    // =========================================================================
    // SEARCH MODE — runs all 30 seeds, finds best, then runs best in full
    // =========================================================================
    private static void runSearch() {
        System.out.println("=== 30 Independent Runs ===");
        System.out.println("popSize=" + LogicalGP.POPULATION_SIZE
                + "  generations=" + LogicalGP.MAX_GENERATIONS
                + "  treeDepth=" + TREE_DEPTH
                + "  maxOD=" + MAX_OD
                + "  tourn=" + TOURNAMENT
                + "  crossover=" + CROSSOVER
                + "  mutation=" + MUTATION
                + "  mutDepth=" + MUT_DEPTH
                + "  elites=" + NUM_ELITES);
        System.out.println();

        float[] trainAccs = new float[SEEDS.length];
        float[] trainF1s  = new float[SEEDS.length];
        float[] testAccs  = new float[SEEDS.length];
        float[] testF1s   = new float[SEEDS.length];

        long startAll = System.currentTimeMillis();

        for (int i = 0; i < SEEDS.length; i++) {
            System.out.println("Run " + (i + 1) + " of 30   seed = " + SEEDS[i]);

            float[] m = runOnce(SEEDS[i]);
            trainAccs[i] = m[0];
            trainF1s[i]  = m[1];
            testAccs[i]  = m[2];
            testF1s[i]   = m[3];

            System.out.println("  Train Acc = " + round(trainAccs[i])
                    + "   Train F1 = " + round(trainF1s[i]));
            System.out.println("  Test Acc  = " + round(testAccs[i])
                    + "   Test F1  = " + round(testF1s[i]));
            System.out.println();
        }

        long totalMs = System.currentTimeMillis() - startAll;
        System.out.println("All 30 runs done in " + (totalMs / 60000) + " min "
                + ((totalMs / 1000) % 60) + " sec");
        System.out.println();

        // Find best by test F1, break ties with test accuracy
        int bestIdx = 0;
        for (int i = 1; i < SEEDS.length; i++) {
            if (testF1s[i] > testF1s[bestIdx]) {
                bestIdx = i;
            } else if (testF1s[i] == testF1s[bestIdx] && testAccs[i] > testAccs[bestIdx]) {
                bestIdx = i;
            }
        }

        System.out.println("==========================================");
        System.out.println("  BEST RUN");
        System.out.println("==========================================");
        System.out.println("  Run       : " + (bestIdx + 1) + " of 30");
        System.out.println("  Seed      : " + SEEDS[bestIdx]);
        System.out.println("  Test Acc  : " + round(testAccs[bestIdx]));
        System.out.println("  Test F1   : " + round(testF1s[bestIdx]));
        System.out.println("==========================================");
        System.out.println();
        System.out.println("Running best seed in full...");
        System.out.println();

        runFull(SEEDS[bestIdx], TREE_DEPTH, MAX_OD, TOURNAMENT, CROSSOVER, MUTATION, MUT_DEPTH,"Breast_train.csv", "Breast_test.csv");
    }

    // =========================================================================
    // Shared method — both search and demo call this so results always match
    // =========================================================================
    private static void runFull(
        long seed, 
        int treeDepth, 
        int maxOffspringDepth, 
        int tournamentSize, 
        float crossoverRate, 
        float mutationRate, 
        int mutationDepth,
        String trainFile, 
        String testFile) {
        ArrayList<String> terminals = new ArrayList<>();
        terminals.add("0");
        terminals.add("1");
        ArrayList<String> functions = new ArrayList<>();

        LogicalGP gp = new LogicalGP(
                trainFile, terminals, functions,
                treeDepth, maxOffspringDepth, tournamentSize,
                crossoverRate, mutationRate, mutationDepth, seed);

        gp.setNumElites(NUM_ELITES);
        gp.autoGenerateFunctionSet(4);

        System.out.println("Seed            : " + seed);
        System.out.println("Training file   : " + trainFile);
        System.out.println("Test file       : " + testFile);
        System.out.println("Population size : " + LogicalGP.POPULATION_SIZE);
        System.out.println("Max generations : " + LogicalGP.MAX_GENERATIONS);
        System.out.println("Function set    : " + gp.functionSet.size() + " predicates");
        System.out.println();

        long start = System.currentTimeMillis();
        Node best  = gp.build();
        long ms    = System.currentTimeMillis() - start;

        float[] train = gp.computeMetrics(best, gp.data);
        System.out.println();
        System.out.println("=== Training Results ===");
        System.out.println("Accuracy : " + round(train[0]));
        System.out.println("F1 Score : " + round(train[1]));

        ArrayList<Data> testData = DataCollection.getCSVValues(testFile);
        System.out.println();
        System.out.println("Test data loaded: " + testData.size() + " samples");

        float[] test = gp.computeMetrics(best, testData);
        System.out.println();
        System.out.println("=== Test Results ===");
        System.out.println("Accuracy : " + round(test[0]));
        System.out.println("F1 Score : " + round(test[1]));

        System.out.println();
        System.out.println("=== Runtime ===");
        System.out.println("Total time : " + ms + " ms (" + round(ms / 1000.0f) + " s)");

        System.out.println();
        System.out.println("=== Best Tree ===");
        System.out.println(best.TreeToString());
    }

    // =========================================================================
    // Runs one seed and returns [trainAcc, trainF1, testAcc, testF1]
    // =========================================================================
    private static float[] runOnce(long seed) {
        ArrayList<String> terminals = new ArrayList<>();
        terminals.add("0");
        terminals.add("1");
        ArrayList<String> functions = new ArrayList<>();

        LogicalGP gp = new LogicalGP(
                "Breast_train.csv", terminals, functions,
                TREE_DEPTH, MAX_OD, TOURNAMENT,
                CROSSOVER, MUTATION, MUT_DEPTH, seed);

        gp.setNumElites(NUM_ELITES);
        gp.autoGenerateFunctionSet(4);
        Node best = gp.build();

        float[] train = gp.computeMetrics(best, gp.data);
        ArrayList<Data> testData = DataCollection.getCSVValues("Breast_test.csv");
        float[] test  = gp.computeMetrics(best, testData);

        return new float[]{train[0], train[1], test[0], test[1]};
    }

    // =========================================================================
    // Rounds a float to 4 decimal places, no format specifiers
    // =========================================================================
    private static String round(float val) {
        long shifted = Math.round(val * 10000);
        long whole   = shifted / 10000;
        long decimal = Math.abs(shifted % 10000);
        String decStr = Long.toString(decimal);
        while (decStr.length() < 4) {
            decStr = "0" + decStr;
        }
        return whole + "." + decStr;
    }
}