import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Random;

public class LogicalGP implements GP {
	public Node root;
	public ArrayList<Data> data;
	public LinkedHashMap<Node, Float> trees;
	public ArrayList<String> terminalSet;
	public ArrayList<String> functionSet;
	public int treeDepth;
	public int maxOffspringDepth;
	public int tournamentSize;
	public float crossoverRate;
	public float mutationRate;
	public int mutationOffspringDepth;
	public long seed;
	public int numElites = 4;

	public static final int POPULATION_SIZE = 200;
	public static final int MAX_GENERATIONS = 100;

	private Random rand;
	private HashMap<String, Float> fitnessCache;
	private ArrayList<Node> selectedParents;
	private ArrayList<Node> offspring;

	public LogicalGP(
			String filename,
			ArrayList<String> terminalSet,
			ArrayList<String> functionSet,
			int treeDepth,
			int maxOffspringDepth,
			int tournamentSize,
			float crossoverRate,
			float mutationRate,
			int mutationOffspringDepth,
			long seed) {
		this.data = DataCollection.getCSVValues(filename);
		this.terminalSet = terminalSet;
		this.functionSet = functionSet;
		this.treeDepth = treeDepth;
		this.maxOffspringDepth = maxOffspringDepth;
		this.tournamentSize = tournamentSize;
		this.crossoverRate = crossoverRate;
		this.mutationRate = mutationRate;
		this.mutationOffspringDepth = mutationOffspringDepth;
		this.seed = seed;

		this.root = null;
		this.trees = new LinkedHashMap<>();
		this.fitnessCache = new HashMap<>();
		this.selectedParents = new ArrayList<>();
		this.offspring = new ArrayList<>();
		this.rand = new Random(seed);
	}

	@Override
	public Node bestTree() {
		Node best = null;
		float bestFit = -1f;

		for (Node node : trees.keySet()) {
			float fit = trees.get(node);
			if (fit > bestFit) {
				bestFit = fit;
				best = node;
			}
		}

		return best;
	}

	@Override
	public Node build() {
		System.out.println("=== Logical GP Evolutionary Run ===");
		System.out.println("Parameters: popSize=" + POPULATION_SIZE
				+ ", generations=" + MAX_GENERATIONS
				+ ", treeDepth=" + treeDepth
				+ ", maxOffspringDepth=" + maxOffspringDepth
				+ ", tournamentSize=" + tournamentSize
				+ ", crossover=" + crossoverRate
				+ ", mutation=" + mutationRate 
				+ ", mutDepth=" + mutationOffspringDepth
				+ ", numElites=" + numElites + "\n");

		intialisePopulation();
		Node bestOverall = null;
		float bestFitness = -1f;

		for (int gen = 0; gen < MAX_GENERATIONS; gen++) {

			Node currentBest = bestTree();
			float currentFit = trees.get(currentBest);

			if (currentFit > bestFitness) {
				bestFitness = currentFit;
				bestOverall = deepCopy(currentBest);
			}

			System.out.printf("Gen %3d | Best fitness: %.4f%n", gen + 1, bestFitness);

			selectedParents = selection();
			offspring = geneticOperators();
			populationReplacement();
		}

		System.out.println("\n=== Evolution Complete ===");
		System.out.printf("Best fitness (accuracy): %.4f%n", bestFitness);

		root = bestOverall;
		return root;
	}

	@Override
	public ArrayList<Node> intialisePopulation() {
		trees.clear();
		ArrayList<Node> newPopulation = new ArrayList<>();
		HashSet<String> signatures = new HashSet<>();

		int numDepths = treeDepth - 1;
		int perSlot = Math.max(1, POPULATION_SIZE / (numDepths * 2));

		for (int depth = 2; depth <= treeDepth; depth++) {
			for (int i = 0; i < perSlot; i++) {
				Node t = buildTree(depth, true);
				String key = t.TreeToString();
				if (!signatures.contains(key)) {
					newPopulation.add(t);
					trees.put(t, computeFitness(t));
					signatures.add(key);
				}
			}
			for (int i = 0; i < perSlot; i++) {
				Node t = buildTree(depth, false);
				String key = t.TreeToString();
				if (!signatures.contains(key)) {
					newPopulation.add(t);
					trees.put(t, computeFitness(t));
					signatures.add(key);
				}
			}
		}

		while (newPopulation.size() < POPULATION_SIZE) {
			Node t = buildTree(treeDepth, rand.nextBoolean());
			String key = t.TreeToString();
			if (!signatures.contains(key)) {
				newPopulation.add(t);
				trees.put(t, computeFitness(t));
				signatures.add(key);
			}
		}

		return newPopulation;
	}

	@Override
	public ArrayList<Node> selection() {
		ArrayList<Node> selected = new ArrayList<>();
		ArrayList<Node> pool = new ArrayList<>(trees.keySet());
		// Sort by tree structure string so iteration order is deterministic
		// regardless of how HashMap assigned memory addresses to Node objects
		pool.sort((a, b) -> a.TreeToString().compareTo(b.TreeToString()));

		for (int i = 0; i < POPULATION_SIZE; i++) {
			selected.add(runTournament(pool));
		}

		return selected;
	}

	@Override
	public ArrayList<Node> geneticOperators() {
		ArrayList<Node> children = new ArrayList<>();
		HashSet<String> signatures = new HashSet<>();
		ArrayList<Node> parents = new ArrayList<>(selectedParents);

		if (parents.size() % 2 != 0) {
			parents.remove(parents.size() - 1);
		}

		for (int i = 0; i + 1 < parents.size(); i += 2) {
			Node child1 = deepCopy(parents.get(i));
			Node child2 = deepCopy(parents.get(i + 1));

			if (rand.nextFloat() < crossoverRate) {
				Node[] crossed = crossover(child1, child2);
				child1 = crossed[0];
				child2 = crossed[1];
			}

			if (rand.nextFloat() < mutationRate) {
				child1 = pointMutate(child1);
			}

			if (rand.nextFloat() < mutationRate) {
				child2 = pointMutate(child2);
			}

			if (treeHeight(child1) <= maxOffspringDepth) {
				String key1 = child1.TreeToString();
				if (!signatures.contains(key1)) {
					children.add(child1);
					signatures.add(key1);
				}
			}

			if (treeHeight(child2) <= maxOffspringDepth) {
				String key2 = child2.TreeToString();
				if (!signatures.contains(key2)) {
					children.add(child2);
					signatures.add(key2);
				}
			}
		}

		return children;
	}

	@Override
	public ArrayList<Node> populationReplacement() {
		ArrayList<Node> nextPopulation = new ArrayList<>();
		HashSet<String> signatures = new HashSet<>();

		ArrayList<Node> sorted = new ArrayList<>(trees.keySet());
		sorted.sort((a, b) -> Float.compare(trees.get(b), trees.get(a)));

		for (int i = 0; i < Math.min(numElites, sorted.size()); i++) {
			Node elite = deepCopy(sorted.get(i));
			String key = elite.TreeToString();
			if (!signatures.contains(key)) {
				nextPopulation.add(elite);
				signatures.add(key);
			}
		}

		for (Node child : offspring) {
			if (nextPopulation.size() >= POPULATION_SIZE) {
				break;
			}
			String key = child.TreeToString();
			if (!signatures.contains(key)) {
				nextPopulation.add(child);
				signatures.add(key);
			}
		}

		while (nextPopulation.size() < POPULATION_SIZE) {
			Node candidate = buildTree(treeDepth, rand.nextBoolean());
			String key = candidate.TreeToString();
			if (!signatures.contains(key)) {
				nextPopulation.add(candidate);
				signatures.add(key);
			}
		}

		trees.clear();
		for (Node node : nextPopulation) {
			trees.put(node, computeFitness(node));
		}

		return nextPopulation;
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
		node.left = buildTree(maxDepth - 1, isFull);
		node.right = buildTree(maxDepth - 1, isFull);
		return node;
	}

	private Node runTournament(ArrayList<Node> pool) {
		Node best = null;
		float bestFit = Float.NEGATIVE_INFINITY;

		for (int i = 0; i < tournamentSize; i++) {
			Node contender = pool.get(rand.nextInt(pool.size()));
			float fit = computeFitness(contender);
			if (fit > bestFit) {
				bestFit = fit;
				best = contender;
			}
		}

		return best;
	}

	private Node[] crossover(Node child1, Node child2) {
		ArrayList<Node> nodes1 = getAllNodes(child1);
		ArrayList<Node> nodes2 = getAllNodes(child2);

		Node[] result = new Node[] { child1, child2 };
		if (nodes1.size() <= 1 || nodes2.size() <= 1) {
			return result;
		}

		int idx1 = 1 + rand.nextInt(nodes1.size() - 1);
		int idx2 = 1 + rand.nextInt(nodes2.size() - 1);
		Node cutPoint1 = nodes1.get(idx1);
		Node cutPoint2 = nodes2.get(idx2);

		Node parent1 = findParent(child1, cutPoint1);
		Node parent2 = findParent(child2, cutPoint2);

		if (parent1 == null || parent2 == null) {
			return result;
		}

		if (parent1.left == cutPoint1) {
			parent1.left = cutPoint2;
		} else {
			parent1.right = cutPoint2;
		}

		if (parent2.left == cutPoint2) {
			parent2.left = cutPoint1;
		} else {
			parent2.right = cutPoint1;
		}

		result[0] = child1;
		result[1] = child2;
		return result;
	}

	// point mutation: changes the value at one randomly chosen node,
	// leaving the tree structure intact
	private Node pointMutate(Node tree) {
		Node mutated = deepCopy(tree);
		ArrayList<Node> nodes = getAllNodes(mutated);

		if (nodes.isEmpty()) {
			return mutated;
		}

		Node target = nodes.get(rand.nextInt(nodes.size()));

		if (target.type == Node.Type.FUNCTION) {
			String newValue = target.value;
			int attempts = 0;
			while (newValue.equals(target.value) && functionSet.size() > 1 && attempts < 20) {
				newValue = functionSet.get(rand.nextInt(functionSet.size()));
				attempts++;
			}
			target.value = newValue;
		} else {
			String newValue = target.value;
			int attempts = 0;
			while (newValue.equals(target.value) && terminalSet.size() > 1 && attempts < 20) {
				newValue = terminalSet.get(rand.nextInt(terminalSet.size()));
				attempts++;
			}
			target.value = newValue;
		}

		return mutated;
	}

	private int evaluateTree(Node node, Data d) {
		if (node == null) {
			return 0;
		}

		if (node.type == Node.Type.TERMINAL) {
			try {
				return Integer.parseInt(node.value);
			} catch (NumberFormatException e) {
				return 0;
			}
		}

		if (evaluateCondition(node.value, d)) {
			return evaluateTree(node.left, d);
		}

		return evaluateTree(node.right, d);
	}

	private boolean evaluateCondition(String condition, Data d) {
		condition = condition.trim();

		if (condition.contains("==")) {
			String[] parts = condition.split("==");
			return getFeatureValue(parts[0].trim(), d) == Integer.parseInt(parts[1].trim());
		}
		if (condition.contains("!=")) {
			String[] parts = condition.split("!=");
			return getFeatureValue(parts[0].trim(), d) != Integer.parseInt(parts[1].trim());
		}
		if (condition.contains("<=")) {
			String[] parts = condition.split("<=");
			return getFeatureValue(parts[0].trim(), d) <= Integer.parseInt(parts[1].trim());
		}
		if (condition.contains(">=")) {
			String[] parts = condition.split(">=");
			return getFeatureValue(parts[0].trim(), d) >= Integer.parseInt(parts[1].trim());
		}
		if (condition.contains(">")) {
			String[] parts = condition.split(">");
			return getFeatureValue(parts[0].trim(), d) > Integer.parseInt(parts[1].trim());
		}
		if (condition.contains("<")) {
			String[] parts = condition.split("<");
			return getFeatureValue(parts[0].trim(), d) < Integer.parseInt(parts[1].trim());
		}
		if (condition.startsWith("NOT(") && condition.endsWith(")")) {
			return !evaluateCondition(condition.substring(4, condition.length() - 1), d);
		}

		return false;
	}

	private int getFeatureValue(String feature, Data d) {
		switch (feature) {
			case "age":
				return d.age;
			case "menopause":
				return d.menopause;
			case "tumorSize":
				return d.tumorSize;
			case "nodes":
				return d.nodes;
			case "nodeCaps":
				return d.nodeCaps;
			case "degMalig":
				return d.degMalig;
			case "breast":
				return d.breast;
			case "quad":
				return d.quad;
			case "irradiat":
				return d.irradiat;
			default:
				return 0;
		}
	}

	public float computeFitness(Node tree) {
		String cacheKey = tree.TreeToString();
		if (fitnessCache.containsKey(cacheKey)) {
			return fitnessCache.get(cacheKey);
		}

		if (data == null || data.isEmpty()) {
			return 0f;
		}

		int tp = 0, fp = 0, fn = 0, tn = 0;
		for (Data d : data) {
			int pred = evaluateTree(tree, d);
			if (pred == 1 && d.result == 1) {
				tp++;
			} else if (pred == 1 && d.result == 0) {
				fp++;
			} else if (pred == 0 && d.result == 1) {
				fn++;
			} else {
				tn++;
			}
		}

		float sensitivity;
		if (tp + fn == 0) {
			sensitivity = 0f;
		} else {
			sensitivity = (float) tp / (tp + fn);
		}

		float specificity;
		if (tn + fp == 0) {
			specificity = 0f;
		} else {
			specificity = (float) tn / (tn + fp);
		}

		float result = (sensitivity + specificity) / 2f;
		fitnessCache.put(cacheKey, result);
		return result;
	}

	public float[] computeMetrics(Node tree, ArrayList<Data> dataset) {
		int tp = 0, fp = 0, fn = 0, tn = 0;

		for (Data d : dataset) {
			int predicted = evaluateTree(tree, d);
			if (predicted == 1 && d.result == 1) {
				tp++;
			} else if (predicted == 1 && d.result == 0) {
				fp++;
			} else if (predicted == 0 && d.result == 1) {
				fn++;
			} else {
				tn++;
			}
		}

		float accuracy = (float) (tp + tn) / dataset.size();

		float precision;
		if (tp + fp == 0) {
			precision = 0f;
		} else {
			precision = (float) tp / (tp + fp);
		}

		float recall;
		if (tp + fn == 0) {
			recall = 0f;
		} else {
			recall = (float) tp / (tp + fn);
		}

		float f1;
		if (precision + recall == 0) {
			f1 = 0f;
		} else {
			f1 = 2 * precision * recall / (precision + recall);
		}

		return new float[] { accuracy, f1 };
	}

	public void autoGenerateFunctionSet(int thresholdsPerFeature) {
		this.functionSet.clear();
		if (data == null || data.isEmpty()) {
			return;
		}

		String[] features = new String[] { "age", "menopause", "nodes", "tumorSize", "nodeCaps", "degMalig", "irradiat",
				"breast", "quad" };

		for (String feat : features) {
			java.util.TreeSet<Integer> uniq = new java.util.TreeSet<>();
			for (Data d : data) {
				uniq.add(getFeatureValue(feat, d));
			}
			if (uniq.isEmpty()) {
				continue;
			}

			if (uniq.size() <= Math.max(2, thresholdsPerFeature)) {
				for (Integer v : uniq) {
					this.functionSet.add(feat + "==" + v);
					this.functionSet.add(feat + "!=" + v);
				}
			} else {
				ArrayList<Integer> vals = new ArrayList<>(uniq);
				int n = vals.size();
				for (int t = 1; t <= thresholdsPerFeature; t++) {
					int idx = (int) Math.round((double) t * n / (thresholdsPerFeature + 1)) - 1;
					idx = Math.max(0, Math.min(n - 1, idx));
					int threshold = vals.get(idx);
					this.functionSet.add(feat + "<=" + threshold);
					this.functionSet.add(feat + ">" + threshold);
					this.functionSet.add(feat + ">=" + threshold);
					this.functionSet.add(feat + "<"  + threshold);
				}
			}
		}
	}

	public void setNumElites(int numElites) {
		this.numElites = Math.max(0, Math.min(numElites, POPULATION_SIZE / 2));
	}

	private int treeHeight(Node node) {
		if (node == null) {
			return 0;
		}
		int leftHeight = treeHeight(node.left);
		int rightHeight = treeHeight(node.right);
		if (leftHeight > rightHeight) {
			return 1 + leftHeight;
		}
		return 1 + rightHeight;
	}

	private Node deepCopy(Node node) {
		if (node == null) {
			return null;
		}
		Node copy = new Node(node.type, node.value);
		copy.left = deepCopy(node.left);
		copy.right = deepCopy(node.right);
		return copy;
	}

	private ArrayList<Node> getAllNodes(Node node) {
		ArrayList<Node> nodes = new ArrayList<>();
		if (node == null) {
			return nodes;
		}
		nodes.add(node);
		nodes.addAll(getAllNodes(node.left));
		nodes.addAll(getAllNodes(node.right));
		return nodes;
	}

	private Node findParent(Node current, Node target) {
		if (current == null) {
			return null;
		}
		if (current.left == target || current.right == target) {
			return current;
		}
		Node leftResult = findParent(current.left, target);
		if (leftResult != null) {
			return leftResult;
		}
		return findParent(current.right, target);
	}
}