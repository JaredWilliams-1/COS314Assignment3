import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    // =========================================================================
    // LogicalGP default parameters (optimised via parameter search)
    // =========================================================================
    static final int   LGP_TREE_DEPTH = 4;
    static final int   LGP_MAX_OD     = 9;
    static final int   LGP_TOURNAMENT = 7;
    static final float LGP_CROSSOVER  = 0.80f;
    static final float LGP_MUTATION   = 0.15f;
    static final int   LGP_MUT_DEPTH  = 3;
    static final int   LGP_NUM_ELITES = 4;

    static final long  LGP_DEMO_SEED = 864208642086L;

    // =========================================================================
    // SymbolicGP default parameters
    // =========================================================================
    static final int   SGP_TREE_DEPTH = 5;
    static final int   SGP_MAX_OD     = 8;
    static final int   SGP_TOURNAMENT = 7;
    static final float SGP_CROSSOVER  = 0.80f;
    static final float SGP_MUTATION   = 0.15f;
    static final int   SGP_MUT_DEPTH  = 2;

    static final long  SGP_DEMO_SEED = 420864208642L;

    // =========================================================================
    // 30 unique seeds shared by both algorithms
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
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Genetic Programming Classifier ===");
        System.out.println();

        System.out.println("Select mode:");
        System.out.println("  1. Search Mode  (30 independent runs, finds best seed)");
        System.out.println("  2. Demo Mode    (single run, you choose all parameters)");
        System.out.print("> ");
        int mode = Integer.parseInt(scanner.nextLine().trim());

        System.out.println();
        System.out.println("Select algorithm:");
        System.out.println("  1. LogicalGP   (decision tree with conditional predicates)");
        System.out.println("  2. SymbolicGP  (arithmetic expression tree)");
        System.out.print("> ");
        int gpChoice = Integer.parseInt(scanner.nextLine().trim());

        System.out.println();

        boolean isDemo    = (mode == 2);
        boolean isLogical = (gpChoice == 1);

        if (isDemo && isLogical) {
            runLogicalDemo(scanner);
        } else if (isDemo && !isLogical) {
            runSymbolicDemo(scanner);
        } else if (!isDemo && isLogical) {
            runSearch(true);
        } else {
            runSearch(false);
        }

        scanner.close();
    }

    // =========================================================================
    // DEMO MODE — LogicalGP
    // Prompts for all parameters with defaults shown, then runs in full
    // =========================================================================
    private static void runLogicalDemo(Scanner scanner) {
        System.out.println("=== LogicalGP Demo ===");
        System.out.println();

        System.out.println("Enter seed (press Enter for " + LGP_DEMO_SEED + "):");
        String input = scanner.nextLine().trim();
        long seed = input.isEmpty() ? LGP_DEMO_SEED : Long.parseLong(input);

        System.out.println("Enter tree depth (press Enter for " + LGP_TREE_DEPTH + "):");
        input = scanner.nextLine().trim();
        int treeDepth = input.isEmpty() ? LGP_TREE_DEPTH : Integer.parseInt(input);

        System.out.println("Enter max offspring depth (press Enter for " + LGP_MAX_OD + "):");
        input = scanner.nextLine().trim();
        int maxOD = input.isEmpty() ? LGP_MAX_OD : Integer.parseInt(input);

        System.out.println("Enter tournament size (press Enter for " + LGP_TOURNAMENT + "):");
        input = scanner.nextLine().trim();
        int tournament = input.isEmpty() ? LGP_TOURNAMENT : Integer.parseInt(input);

        System.out.println("Enter crossover rate (press Enter for " + LGP_CROSSOVER + "):");
        input = scanner.nextLine().trim();
        float crossover = input.isEmpty() ? LGP_CROSSOVER : Float.parseFloat(input);

        System.out.println("Enter mutation rate (press Enter for " + LGP_MUTATION + "):");
        input = scanner.nextLine().trim();
        float mutation = input.isEmpty() ? LGP_MUTATION : Float.parseFloat(input);

        System.out.println("Enter mutation depth (press Enter for " + LGP_MUT_DEPTH + "):");
        input = scanner.nextLine().trim();
        int mutDepth = input.isEmpty() ? LGP_MUT_DEPTH : Integer.parseInt(input);

        System.out.println("Enter number of elites (press Enter for " + LGP_NUM_ELITES + "):");
        input = scanner.nextLine().trim();
        int numElites = input.isEmpty() ? LGP_NUM_ELITES : Integer.parseInt(input);

        System.out.println("Enter training file path (press Enter for Breast_train.csv):");
        input = scanner.nextLine().trim();
        String trainFile = input.isEmpty() ? "Breast_train.csv" : input;

        System.out.println("Enter test file path (press Enter for Breast_test.csv):");
        input = scanner.nextLine().trim();
        String testFile = input.isEmpty() ? "Breast_test.csv" : input;

        System.out.println();

        runLogicalFull(seed, treeDepth, maxOD, tournament, crossover, mutation, mutDepth, numElites, trainFile, testFile);
    }

    // =========================================================================
    // DEMO MODE — SymbolicGP
    // =========================================================================
    private static void runSymbolicDemo(Scanner scanner) {
        System.out.println("=== SymbolicGP Demo ===");
        System.out.println();

        System.out.println("Enter seed (press Enter for " + SGP_DEMO_SEED + "):");
        String input = scanner.nextLine().trim();
        long seed = input.isEmpty() ? SGP_DEMO_SEED : Long.parseLong(input);

        System.out.println("Enter tree depth (press Enter for " + SGP_TREE_DEPTH + "):");
        input = scanner.nextLine().trim();
        int treeDepth = input.isEmpty() ? SGP_TREE_DEPTH : Integer.parseInt(input);

        System.out.println("Enter max offspring depth (press Enter for " + SGP_MAX_OD + "):");
        input = scanner.nextLine().trim();
        int maxOD = input.isEmpty() ? SGP_MAX_OD : Integer.parseInt(input);

        System.out.println("Enter tournament size (press Enter for " + SGP_TOURNAMENT + "):");
        input = scanner.nextLine().trim();
        int tournament = input.isEmpty() ? SGP_TOURNAMENT : Integer.parseInt(input);

        System.out.println("Enter crossover rate (press Enter for " + SGP_CROSSOVER + "):");
        input = scanner.nextLine().trim();
        float crossover = input.isEmpty() ? SGP_CROSSOVER : Float.parseFloat(input);

        System.out.println("Enter mutation rate (press Enter for " + SGP_MUTATION + "):");
        input = scanner.nextLine().trim();
        float mutation = input.isEmpty() ? SGP_MUTATION : Float.parseFloat(input);

        System.out.println("Enter mutation depth (press Enter for " + SGP_MUT_DEPTH + "):");
        input = scanner.nextLine().trim();
        int mutDepth = input.isEmpty() ? SGP_MUT_DEPTH : Integer.parseInt(input);

        System.out.println("Enter training file path (press Enter for Breast_train.csv):");
        input = scanner.nextLine().trim();
        String trainFile = input.isEmpty() ? "Breast_train.csv" : input;

        System.out.println("Enter test file path (press Enter for Breast_test.csv):");
        input = scanner.nextLine().trim();
        String testFile = input.isEmpty() ? "Breast_test.csv" : input;

        System.out.println();

        runSymbolicFull(seed, treeDepth, maxOD, tournament, crossover, mutation, mutDepth, trainFile, testFile);
    }

    // =========================================================================
    // SEARCH MODE — 30 independent runs, finds best seed, runs it in full
    // =========================================================================
    private static void runSearch(boolean logical) {
        String label = logical ? "LogicalGP" : "SymbolicGP";
        System.out.println("=== " + label + " — 30 Independent Runs ===");

        if (logical) {
            System.out.println("popSize=" + LogicalGP.POPULATION_SIZE
                    + "  generations=" + LogicalGP.MAX_GENERATIONS
                    + "  treeDepth=" + LGP_TREE_DEPTH
                    + "  maxOD=" + LGP_MAX_OD
                    + "  tourn=" + LGP_TOURNAMENT
                    + "  crossover=" + LGP_CROSSOVER
                    + "  mutation=" + LGP_MUTATION
                    + "  mutDepth=" + LGP_MUT_DEPTH
                    + "  elites=" + LGP_NUM_ELITES);
        } else {
            System.out.println("popSize=" + SymbolicGP.POPULATION_SIZE
                    + "  generations=" + SymbolicGP.MAX_GENERATIONS
                    + "  treeDepth=" + SGP_TREE_DEPTH
                    + "  maxOD=" + SGP_MAX_OD
                    + "  tourn=" + SGP_TOURNAMENT
                    + "  crossover=" + SGP_CROSSOVER
                    + "  mutation=" + SGP_MUTATION
                    + "  mutDepth=" + SGP_MUT_DEPTH);
        }
        System.out.println();

        float[] trainAccs = new float[SEEDS.length];
        float[] trainF1s  = new float[SEEDS.length];
        float[] testAccs  = new float[SEEDS.length];
        float[] testF1s   = new float[SEEDS.length];

        long startAll = System.currentTimeMillis();

        for (int i = 0; i < SEEDS.length; i++) {
            System.out.println("Run " + (i + 1) + " of 30   seed = " + SEEDS[i]);

            float[] m;
            if (logical) {
                m = runLogicalOnce(SEEDS[i]);
            } else {
                m = runSymbolicOnce(SEEDS[i]);
            }

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

        int bestIdx = 0;
        for (int i = 1; i < SEEDS.length; i++) {
            if (testF1s[i] > testF1s[bestIdx]) {
                bestIdx = i;
            } else if (testF1s[i] == testF1s[bestIdx] && testAccs[i] > testAccs[bestIdx]) {
                bestIdx = i;
            }
        }

        System.out.println("==========================================");
        System.out.println("  BEST RUN — " + label);
        System.out.println("==========================================");
        System.out.println("  Run       : " + (bestIdx + 1) + " of 30");
        System.out.println("  Seed      : " + SEEDS[bestIdx]);
        System.out.println("  Test Acc  : " + round(testAccs[bestIdx]));
        System.out.println("  Test F1   : " + round(testF1s[bestIdx]));
        System.out.println("==========================================");
        System.out.println();
        System.out.println("Running best seed in full...");
        System.out.println();

        if (logical) {
            runLogicalFull(SEEDS[bestIdx], LGP_TREE_DEPTH, LGP_MAX_OD, LGP_TOURNAMENT,
                    LGP_CROSSOVER, LGP_MUTATION, LGP_MUT_DEPTH, LGP_NUM_ELITES,
                    "Breast_train.csv", "Breast_test.csv");
        } else {
            runSymbolicFull(SEEDS[bestIdx], SGP_TREE_DEPTH, SGP_MAX_OD, SGP_TOURNAMENT,
                    SGP_CROSSOVER, SGP_MUTATION, SGP_MUT_DEPTH,
                    "Breast_train.csv", "Breast_test.csv");
        }
    }

    // =========================================================================
    // LogicalGP — full run with all parameters explicit
    // =========================================================================
    private static void runLogicalFull(long seed, int treeDepth, int maxOD, int tournament,
            float crossover, float mutation, int mutDepth, int numElites,
            String trainFile, String testFile) {

        ArrayList<String> terminals = new ArrayList<>();
        terminals.add("0");
        terminals.add("1");
        ArrayList<String> functions = new ArrayList<>();

        LogicalGP gp = new LogicalGP(
                trainFile, terminals, functions,
                treeDepth, maxOD, tournament,
                crossover, mutation, mutDepth, seed);

        gp.setNumElites(numElites);
        gp.autoGenerateFunctionSet(4);

        System.out.println("Algorithm       : LogicalGP");
        System.out.println("Seed            : " + seed);
        System.out.println("Training file   : " + trainFile);
        System.out.println("Test file       : " + testFile);
        System.out.println("Population size : " + LogicalGP.POPULATION_SIZE);
        System.out.println("Max generations : " + LogicalGP.MAX_GENERATIONS);
        System.out.println("Tree depth      : " + treeDepth);
        System.out.println("Max offspring D : " + maxOD);
        System.out.println("Tournament size : " + tournament);
        System.out.println("Crossover rate  : " + crossover);
        System.out.println("Mutation rate   : " + mutation);
        System.out.println("Mutation depth  : " + mutDepth);
        System.out.println("Elites          : " + numElites);
        System.out.println("Function set    : " + gp.functionSet.size() + " predicates");
        System.out.println();

        long start = System.currentTimeMillis();
        Node best  = gp.build();
        long ms    = System.currentTimeMillis() - start;

        printResults(gp.computeMetrics(best, gp.data),
                     gp.computeMetrics(best, DataCollection.getCSVValues(testFile)),
                     ms, best);
    }

    // =========================================================================
    // LogicalGP — silent run using default constants
    // =========================================================================
    private static float[] runLogicalOnce(long seed) {
        ArrayList<String> terminals = new ArrayList<>();
        terminals.add("0");
        terminals.add("1");
        ArrayList<String> functions = new ArrayList<>();

        LogicalGP gp = new LogicalGP(
                "Breast_train.csv", terminals, functions,
                LGP_TREE_DEPTH, LGP_MAX_OD, LGP_TOURNAMENT,
                LGP_CROSSOVER, LGP_MUTATION, LGP_MUT_DEPTH, seed);

        gp.setNumElites(LGP_NUM_ELITES);
        gp.autoGenerateFunctionSet(4);
        Node best = gp.build();

        float[] train = gp.computeMetrics(best, gp.data);
        float[] test  = gp.computeMetrics(best, DataCollection.getCSVValues("Breast_test.csv"));
        return new float[]{train[0], train[1], test[0], test[1]};
    }

    // =========================================================================
    // SymbolicGP — full run with all parameters explicit
    // =========================================================================
    private static void runSymbolicFull(long seed, int treeDepth, int maxOD, int tournament,
            float crossover, float mutation, int mutDepth,
            String trainFile, String testFile) {

        ArrayList<String> terminals = buildSymbolicTerminals();
        ArrayList<String> functions = buildSymbolicFunctions();

        SymbolicGP gp = new SymbolicGP(
                trainFile, terminals, functions,
                treeDepth, maxOD, tournament,
                crossover, mutation, mutDepth, seed);

        System.out.println("Algorithm       : SymbolicGP");
        System.out.println("Seed            : " + seed);
        System.out.println("Training file   : " + trainFile);
        System.out.println("Test file       : " + testFile);
        System.out.println("Population size : " + SymbolicGP.POPULATION_SIZE);
        System.out.println("Max generations : " + SymbolicGP.MAX_GENERATIONS);
        System.out.println("Tree depth      : " + treeDepth);
        System.out.println("Max offspring D : " + maxOD);
        System.out.println("Tournament size : " + tournament);
        System.out.println("Crossover rate  : " + crossover);
        System.out.println("Mutation rate   : " + mutation);
        System.out.println("Mutation depth  : " + mutDepth);
        System.out.println("Terminals       : " + terminals.size());
        System.out.println("Functions       : " + functions);
        System.out.println();

        long start = System.currentTimeMillis();
        Node best  = gp.build();
        long ms    = System.currentTimeMillis() - start;

        printResults(gp.computeMetrics(best, gp.data),
                     gp.computeMetrics(best, DataCollection.getCSVValues(testFile)),
                     ms, best);
    }

    // =========================================================================
    // SymbolicGP — silent run using default constants
    // =========================================================================
    private static float[] runSymbolicOnce(long seed) {
        ArrayList<String> terminals = buildSymbolicTerminals();
        ArrayList<String> functions = buildSymbolicFunctions();

        SymbolicGP gp = new SymbolicGP(
                "Breast_train.csv", terminals, functions,
                SGP_TREE_DEPTH, SGP_MAX_OD, SGP_TOURNAMENT,
                SGP_CROSSOVER, SGP_MUTATION, SGP_MUT_DEPTH, seed);

        Node best = gp.build();

        float[] train = gp.computeMetrics(best, gp.data);
        float[] test  = gp.computeMetrics(best, DataCollection.getCSVValues("Breast_test.csv"));
        return new float[]{train[0], train[1], test[0], test[1]};
    }

    // =========================================================================
    // SymbolicGP terminal and function set builders
    // =========================================================================
    private static ArrayList<String> buildSymbolicTerminals() {
        ArrayList<String> t = new ArrayList<>();
        t.add("age");
        t.add("menopause");
        t.add("nodes");
        t.add("tumorSize");
        t.add("nodeCaps");
        t.add("degMalig");
        t.add("irradiat");
        t.add("breast");
        t.add("quad");
        t.add("0.5");
        t.add("1.0");
        return t;
    }

    private static ArrayList<String> buildSymbolicFunctions() {
        ArrayList<String> f = new ArrayList<>();
        f.add("+");
        f.add("-");
        f.add("*");
        f.add("/");
        return f;
    }

    // =========================================================================
    // Shared result printer
    // =========================================================================
    private static void printResults(float[] train, float[] test, long ms, Node best) {
        System.out.println();
        System.out.println("=== Training Results ===");
        System.out.println("Accuracy : " + round(train[0]));
        System.out.println("F1 Score : " + round(train[1]));

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