import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Node test1 = new Node(Node.Type.FUNCTION, "+");
        test1.addLeft(new Node(Node.Type.TERMINAL, "2"));
        test1.addRight(new Node(Node.Type.TERMINAL, "4"));

        System.out.println(test1.TreeToString());

        // Simple CSV retrieval test
        System.out.println("Testing CSV loading...");
        ArrayList<Data> dataList = DataCollection.getCSVValues("Breast_train.csv");
        System.out.println("Loaded " + dataList.size() + " rows from Breast_train.csv");
        if (!dataList.isEmpty()) {
            Data first = dataList.get(0);
            System.out.println("First row: result=" + first.result
                    + ", age=" + first.age
                    + ", menopause=" + first.menopause
                    + ", nodes=" + first.nodes
                    + ", tumorSize=" + first.tumorSize
                    + ", nodeCaps=" + first.nodeCaps
                    + ", irradiat=" + first.irradiat
                    + ", breast=" + first.breast
                    + ", quad=" + first.quad);
        }

        Scanner input = new Scanner(System.in);

        System.out.println("================ SYMBOLIC GP ================");

        System.out.print("Enter seed: ");
        int seed = input.nextInt();

        System.out.print("Enter initial tree depth: ");
        int treeDepth = input.nextInt();

        System.out.print("Enter max offspring depth: ");
        int maxOffspringDepth = input.nextInt();

        System.out.print("Enter tournament size: ");
        int tournamentSize = input.nextInt();

        System.out.print("Enter crossover rate e.g. 0.85: ");
        float crossoverRate = input.nextFloat();

        System.out.print("Enter mutation rate e.g. 0.15: ");
        float mutationRate = input.nextFloat();

        System.out.print("Enter mutation offspring depth: ");
        int mutationOffspringDepth = input.nextInt();

        runSingleSymbolicGP(
                seed,
                treeDepth,
                maxOffspringDepth,
                tournamentSize,
                crossoverRate,
                mutationRate,
                mutationOffspringDepth);

        input.close();

        //ADD TEST AND TRAIN PROGRAM !?
        //30 INDEPENDENT RUN TO RECORD BEST PERFROMNIG RUN!!
    }

    public static void runSingleSymbolicGP(
            int seed,
            int treeDepth,
            int maxOffspringDepth,
            int tournamentSize,
            float crossoverRate,
            float mutationRate,
            int mutationOffspringDepth) {

        ArrayList<String> terminals = getTerminals();
        ArrayList<String> functions = getFunctions();

        long startTime = System.currentTimeMillis();

        SymbolicGP sg = new SymbolicGP(
                "Breast_train.csv",
                terminals,
                functions,
                treeDepth,
                maxOffspringDepth,
                tournamentSize,
                crossoverRate,
                mutationRate,
                mutationOffspringDepth,
                seed);

        Node best = sg.build();

        long endTime = System.currentTimeMillis();
        double runtimeSeconds = (endTime - startTime) / 1000.0;

        ArrayList<Data> trainData = DataCollection.getCSVValues("Breast_train.csv");
        ArrayList<Data> testData = DataCollection.getCSVValues("Breast_test.csv");

        float trainAccuracy = sg.accuracy(best, trainData);
        float testAccuracy = sg.accuracy(best, testData);
        float fMeasure = calculateFMeasure(sg, best, testData);

        System.out.println("\n================ FINAL SYMBOLIC GP RESULTS ================");
        System.out.println("Seed: " + seed);
        System.out.println("Tree depth: " + treeDepth);
        System.out.println("Max offspring depth: " + maxOffspringDepth);
        System.out.println("Tournament size: " + tournamentSize);
        System.out.println("Crossover rate: " + crossoverRate);
        System.out.println("Mutation rate: " + mutationRate);
        System.out.println("Mutation offspring depth: " + mutationOffspringDepth);
        System.out.println("Training accuracy: " + (trainAccuracy * 100.0f) + "%");
        System.out.println("Test accuracy: " + (testAccuracy * 100.0f) + "%");
        System.out.println("F-measure: " + fMeasure);
        System.out.println("Runtime: " + runtimeSeconds + " seconds");
        System.out.println("Best tree:");
        System.out.println(best.TreeToString());
    }

    public static float calculateFMeasure(SymbolicGP gp, Node tree, ArrayList<Data> data) {
        int truePositive = 0;
        int falsePositive = 0;
        int falseNegative = 0;

        for (int i = 0; i < data.size(); i++) {
            Data row = data.get(i);
            int predicted = gp.classify(tree, row);
            int actual = row.result;

            if (predicted == 1 && actual == 1) {
                truePositive++;
            } else if (predicted == 1 && actual == 0) {
                falsePositive++;
            } else if (predicted == 0 && actual == 1) {
                falseNegative++;
            }
        }

        float precision;
        float recall;

        if (truePositive + falsePositive == 0) {
            precision = 0.0f;
        } else {
            precision = (float) truePositive / (float) (truePositive + falsePositive);
        }

        if (truePositive + falseNegative == 0) {
            recall = 0.0f;
        } else {
            recall = (float) truePositive / (float) (truePositive + falseNegative);
        }

        if (precision + recall == 0.0f) {
            return 0.0f;
        }

        return 2.0f * ((precision * recall) / (precision + recall));
    }

    public static ArrayList<String> getTerminals() {
        ArrayList<String> terminals = new ArrayList<String>();

        terminals.add("age");
        terminals.add("menopause");
        terminals.add("nodes");
        terminals.add("tumorSize");
        terminals.add("nodeCaps");
        terminals.add("irradiat");
        terminals.add("breast");
        terminals.add("quad");
        terminals.add("0");
        terminals.add("1");

        return terminals;
    }

    public static ArrayList<String> getFunctions() {
        ArrayList<String> functions = new ArrayList<String>();

        functions.add("+");
        functions.add("-");
        functions.add("*");
        functions.add("/");

        return functions;
    }
}