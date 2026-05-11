import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LogicalGP implements GP {
    public Node root;
    public ArrayList<Data> data;
    public HashMap<Node, Float> trees;
    public ArrayList<String> terminalSet;
    public ArrayList<String> functionSet;
    public int treeDepth;
    public int maxOffspring;
    public int tournamentSize;
    public float crossoverRate;
    public float mutationRate;
    public int mutationOffspringDepth;
    public int maxGenerations;
    public int seed;

    private Random rand;
    private Node  bestIndividual;
    private float bestFitness;

    public LogicalGP(String filename, ArrayList<String> terminalSet, ArrayList<String> functionSet,
            int treeDepth, int maxOffspring, int tournamentSize, float crossoverRate,
            float mutationRate, int mutationOffspringDepth, int maxGenerations, int seed) {
        this.data = DataCollection.getCSVValues(filename);
        this.terminalSet = terminalSet;
        this.functionSet = functionSet;
        this.treeDepth = treeDepth;
        this.maxOffspring = maxOffspring;
        this.tournamentSize = tournamentSize;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.mutationOffspringDepth = mutationOffspringDepth;
        this.maxGenerations = maxGenerations;
        this.seed = seed;
        this.root = null;
        this.trees = new HashMap<>();
        this.rand = new Random(seed);
        this.bestIndividual = null;
        this.bestFitness    = -1f;
    }

    // -- build -----------------------------------------------------------------

    @Override
    public Node build() {
        return buildTree(treeDepth, rand.nextBoolean());
    }

    private Node buildTree(int maxDepth, boolean isFull) {
        if (maxDepth == 0) {
            return new Node(Node.Type.TERMINAL, terminalSet.get(rand.nextInt(terminalSet.size())));
        }
        if (!isFull && rand.nextFloat() < 0.3f) {
            return new Node(Node.Type.TERMINAL, terminalSet.get(rand.nextInt(terminalSet.size())));
        }
        String condition = functionSet.get(rand.nextInt(functionSet.size()));
        Node node = new Node(Node.Type.FUNCTION, condition);
        node.left  = buildTree(maxDepth - 1, isFull);
        node.right = buildTree(maxDepth - 1, isFull);
        return node;
    }

    // -- evaluation ------------------------------------------------------------

    private int evaluateTree(Node node, Data d) {
        if (node.type == Node.Type.TERMINAL) {
            return Integer.parseInt(node.value);
        }
        return evaluateCondition(node.value, d)
            ? evaluateTree(node.left, d)
            : evaluateTree(node.right, d);
    }

    private boolean evaluateCondition(String condition, Data d) {
        if (condition.contains("<=")) {
            String[] parts = condition.split("<=");
            return getFeatureValue(parts[0], d) <= Integer.parseInt(parts[1]);
        } else {
            String[] parts = condition.split(">");
            return getFeatureValue(parts[0], d) > Integer.parseInt(parts[1]);
        }
    }

    private int getFeatureValue(String feature, Data d) {
        switch (feature) {
            case "age":        return d.age;
            case "menopause":  return d.menopause;
            case "tumorSize":  return d.tumorSize;
            case "invNodes":   return d.invNodes;
            case "nodeCaps":   return d.nodeCaps;
            case "degMalig":   return d.degMalig;
            case "breast":     return d.breast;
            case "breastQuad": return d.breastQuad;
            case "irradiat":   return d.irradiat;
            default:           return 0;
        }
    }

    // -- fitness ---------------------------------------------------------------

    // Balanced accuracy: average recall of each class.
    // A tree predicting all-0 scores 0.50, not 0.787, so the GP is
    // forced to learn the minority class (recurrence = 1) as well.
    private float computeFitness(Node tree) {
        int tp = 0, fp = 0, fn = 0, tn = 0;
        for (Data d : data) {
            int pred = evaluateTree(tree, d);
            if      (pred == 1 && d.result == 1) tp++;
            else if (pred == 1 && d.result == 0) fp++;
            else if (pred == 0 && d.result == 1) fn++;
            else                                 tn++;
        }
        float sensitivity = (tp + fn == 0) ? 0f : (float) tp / (tp + fn); // recall class 1
        float specificity = (tn + fp == 0) ? 0f : (float) tn / (tn + fp); // recall class 0
        return (sensitivity + specificity) / 2f;
    }

    // -- initialise population -------------------------------------------------

    @Override
    public HashMap<Node, Float> intialisePopulation() {
        trees.clear();
        int numDepths = treeDepth - 1;
        int perSlot   = Math.max(1, maxOffspring / (numDepths * 2));
        for (int depth = 2; depth <= treeDepth; depth++) {
            for (int i = 0; i < perSlot; i++) { Node t = buildTree(depth, true);  trees.put(t, computeFitness(t)); }
            for (int i = 0; i < perSlot; i++) { Node t = buildTree(depth, false); trees.put(t, computeFitness(t)); }
        }
        while (trees.size() < maxOffspring) { Node t = buildTree(treeDepth, rand.nextBoolean()); trees.put(t, computeFitness(t)); }
        return trees;
    }

    // -- selection -------------------------------------------------------------

    @Override
    public HashMap<Node, Float> selection() {
        List<Node> pool = new ArrayList<>(trees.keySet());
        HashMap<Node, Float> selected = new HashMap<>();
        for (int i = 0; i < maxOffspring; i++) {
            Node winner = runTournament(pool);
            selected.put(winner, trees.get(winner));
        }
        return selected;
    }

    private Node runTournament(List<Node> pool) {
        Node best = null; float bestFit = -1f;
        for (int i = 0; i < tournamentSize; i++) {
            Node c = pool.get(rand.nextInt(pool.size()));
            float f = trees.get(c);
            if (f > bestFit) { bestFit = f; best = c; }
        }
        return best;
    }

    // -- genetic operators -----------------------------------------------------

    @Override
    public HashMap<Node, Float> geneticOperators() {
        List<Node> parents = new ArrayList<>(selection().keySet());
        HashMap<Node, Float> offspring = new HashMap<>();
        for (int i = 0; i + 1 < parents.size(); i += 2) {
            Node c1 = deepCopy(parents.get(i));
            Node c2 = deepCopy(parents.get(i + 1));
            if (rand.nextFloat() < crossoverRate) { Node[] x = crossover(c1, c2); c1 = x[0]; c2 = x[1]; }
            if (rand.nextFloat() < mutationRate) c1 = mutate(c1);
            if (rand.nextFloat() < mutationRate) c2 = mutate(c2);
            offspring.put(c1, computeFitness(c1));
            offspring.put(c2, computeFitness(c2));
        }
        if (parents.size() % 2 != 0) { Node last = deepCopy(parents.get(parents.size()-1)); offspring.put(last, computeFitness(last)); }
        return offspring;
    }

    private Node[] crossover(Node t1, Node t2) {
        List<Object[]> n1 = getAllNodes(t1, null, true);
        List<Object[]> n2 = getAllNodes(t2, null, true);
        if (n1.size() <= 1 || n2.size() <= 1) return new Node[]{t1, t2};
        Object[] c1 = n1.get(1 + rand.nextInt(n1.size()-1));
        Object[] c2 = n2.get(1 + rand.nextInt(n2.size()-1));
        Node s1=(Node)c1[0]; Node p1=(Node)c1[1]; boolean l1=(boolean)c1[2];
        Node s2=(Node)c2[0]; Node p2=(Node)c2[1]; boolean l2=(boolean)c2[2];
        if (l1) p1.left=s2; else p1.right=s2;
        if (l2) p2.left=s1; else p2.right=s1;
        return new Node[]{t1, t2};
    }

    private Node mutate(Node tree) {
        List<Object[]> nodes = getAllNodes(tree, null, true);
        if (nodes.isEmpty()) return tree;
        Node node = (Node) nodes.get(rand.nextInt(nodes.size()))[0];
        if (node.type == Node.Type.TERMINAL) node.value = terminalSet.get(rand.nextInt(terminalSet.size()));
        else node.value = functionSet.get(rand.nextInt(functionSet.size()));
        return tree;
    }

    // -- population replacement ------------------------------------------------

    @Override
    public HashMap<Node, Float> populationReplacement() {
        Node elite = getBestFromMap(trees);
        float eliteFit = trees.get(elite);
        Node eliteCopy = deepCopy(elite);
        HashMap<Node, Float> offspring = geneticOperators();
        trees.clear();
        trees.putAll(offspring);
        while (trees.size() < maxOffspring) { Node f = buildTree(treeDepth, rand.nextBoolean()); trees.put(f, computeFitness(f)); }
        trees.put(eliteCopy, eliteFit);
        return trees;
    }

    // -- best tree -------------------------------------------------------------

    @Override
    public Node bestTree() { return bestIndividual; }

    // -- run -------------------------------------------------------------------

    public Node run() {
        System.out.println("\n=== Logical GP Training ===");
        intialisePopulation();
        for (int gen = 0; gen < maxGenerations; gen++) {
            Node cur = getBestFromMap(trees);
            float curFit = trees.get(cur);
            if (curFit > bestFitness) { bestFitness = curFit; bestIndividual = deepCopy(cur); }
            System.out.printf("Gen %3d | TrainAcc: %.4f | Tree: %s%n", gen+1, bestFitness, bestIndividual.TreeToString());
            populationReplacement();
        }
        return bestIndividual;
    }

    // -- metrics ---------------------------------------------------------------

    public float[] computeMetrics(Node tree, ArrayList<Data> dataset) {
        int tp=0, fp=0, fn=0, tn=0;
        for (Data d : dataset) {
            int pred = evaluateTree(tree, d);
            if      (pred==1 && d.result==1) tp++;
            else if (pred==1 && d.result==0) fp++;
            else if (pred==0 && d.result==1) fn++;
            else tn++;
        }
        float acc  = (float)(tp+tn)/dataset.size();
        float prec = (tp+fp==0) ? 0f : (float)tp/(tp+fp);
        float rec  = (tp+fn==0) ? 0f : (float)tp/(tp+fn);
        float f1   = (prec+rec==0) ? 0f : 2*prec*rec/(prec+rec);
        return new float[]{acc, f1};
    }

    // -- utilities -------------------------------------------------------------

    private List<Object[]> getAllNodes(Node node, Node parent, boolean isLeft) {
        List<Object[]> result = new ArrayList<>();
        if (node == null) return result;
        result.add(new Object[]{node, parent, isLeft});
        result.addAll(getAllNodes(node.left,  node, true));
        result.addAll(getAllNodes(node.right, node, false));
        return result;
    }

    private Node deepCopy(Node node) {
        if (node == null) return null;
        Node copy = new Node(node.type, node.value);
        copy.left  = deepCopy(node.left);
        copy.right = deepCopy(node.right);
        return copy;
    }

    private Node getBestFromMap(HashMap<Node, Float> map) {
        Node best = null; float bestFit = -1f;
        for (Map.Entry<Node, Float> e : map.entrySet()) {
            if (e.getValue() > bestFit) { bestFit = e.getValue(); best = e.getKey(); }
        }
        return best;
    }
}


