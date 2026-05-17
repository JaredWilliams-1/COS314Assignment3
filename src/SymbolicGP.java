import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.HashSet;

public class SymbolicGP implements GP {

	public Node root;// stores best tree found so far...
	public ArrayList<Data> data; // stores training rows
	public ArrayList<String> terminalSet; // features/constants
	public ArrayList<String> functionSet;

	public int treeDepth; // depth of initial trees
	public int maxOffspringDepth; // should be offspring depth limit
	public int tournamentSize; // how many compete
	public float crossoverRate;
	public float mutationRate;
	public int mutationOffspringDepth;
	public int seed;

	public static final int POPULATION_SIZE = 200;
	public static final int MAX_GENERATIONS = 100;

	public HashMap<String, Float> fitnessCache; // stores each trees fitness

	public ArrayList<Node> population; // current generation of trees
	public ArrayList<Node> selectedParents; // stores chosen parents for mutation
	public ArrayList<Node> offspring; // stores newly created children
	public Random random;

	public SymbolicGP(
			String filename,
			ArrayList<String> terminalSet,
			ArrayList<String> functionSet,
			int treeDepth,
			int maxOffspringDepth,
			int tournamentSize,
			float crossoverRate,
			float mutationRate,
			int mutationOffspringDepth,
			int seed) {

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
		this.fitnessCache = new HashMap<String, Float>();
		this.population = new ArrayList<Node>();
		this.selectedParents = new ArrayList<Node>();
		this.offspring = new ArrayList<Node>();
		this.random = new Random(seed);
	}

	@Override
	public Node bestTree() {
		Node best = population.get(0);

		for (int i = 1; i < population.size(); i++) {
			Node current = population.get(i);

			if (isBetter(current, best)) {
				best = current;
			}
		}

		return best;
	}

	private boolean isBetter(Node a, Node b) {
		float fitnessA = fitness(a);
		float fitnessB = fitness(b);

		if (fitnessA > fitnessB) {
			return true;
		}

		if (fitnessA == fitnessB && countNodes(a) < countNodes(b)) {
			return true;
		}

		return false;
	}

	private float fitness(Node tree) {
		String key = tree.TreeToString();

		if (fitnessCache.containsKey(key)) {
			return fitnessCache.get(key);
		}

		float fit = accuracy(tree, data);
		fitnessCache.put(key, fit);

		return fit;
	}

	public float accuracy(Node tree, ArrayList<Data> rows) {
		if (rows == null || rows.size() == 0) {
			return 0.0f;
		}

		int correct = 0;

		for (int i = 0; i < rows.size(); i++) {
			Data row = rows.get(i);
			int prediction = classify(tree, row);

			if (prediction == row.result) {
				correct++;
			}
		}

		return (float) correct / (float) rows.size();
	}

	@Override
	public Node build() {
		population = intialisePopulation();

		for (int gen = 0; gen < MAX_GENERATIONS; gen++) {
			fitnessCache.clear();

			root = bestTree();
			float acc = accuracy(root, data);

			System.out.println("Generation " + gen
					+ " | Best accuracy: " + acc
					+ " | Nodes: " + countNodes(root)
					+ " | Depth: " + depth(root));
			System.out.println(root.TreeToString());

			if (acc >= 1.0f) {
				break;
			}

			selectedParents = selection();
			offspring = geneticOperators();
			population = populationReplacement();
		}

		root = bestTree();
		return root;
	}

	@Override
	public ArrayList<Node> intialisePopulation() {
		ArrayList<Node> newPopulation = new ArrayList<Node>();
		HashSet<String> signatures = new HashSet<String>();

		int attempts = 0;

		while (newPopulation.size() < POPULATION_SIZE && attempts < POPULATION_SIZE * 20) {
			attempts++;

			int depthRange = Math.max(2, treeDepth);
			int chosenDepth = 2 + random.nextInt(depthRange - 1);

			boolean useFull = (newPopulation.size() % 2 == 0);
			Node candidate;

			if (useFull) {
				candidate = generateFull(chosenDepth);
			} else {
				candidate = generateGrow(chosenDepth, 0);
			}

			String key = candidate.TreeToString();

			if (!signatures.contains(key)) {
				newPopulation.add(candidate);
				signatures.add(key);
			}
		}

		return newPopulation;
	}

	private Node generateFull(int maxDepth) {
		if (maxDepth <= 1) {
			return randomTerminal();
		}

		Node node = randomFunction();
		node.left = generateFull(maxDepth - 1);
		node.right = generateFull(maxDepth - 1);

		return node;
	}

	private Node generateGrow(int maxDepth, int currentDepth) {
		if (currentDepth >= maxDepth - 1) {
			return randomTerminal();
		}

		if (currentDepth > 0 && random.nextFloat() < 0.35f) {
			return randomTerminal();
		}

		Node node = randomFunction();
		node.left = generateGrow(maxDepth, currentDepth + 1);
		node.right = generateGrow(maxDepth, currentDepth + 1);

		return node;
	}

	@Override
	public ArrayList<Node> selection() {
		ArrayList<Node> parents = new ArrayList<Node>();

		while (parents.size() < POPULATION_SIZE) {
			parents.add(tournamentSelect());
		}

		return parents;
	}

	private Node tournamentSelect() {
		Node best = population.get(random.nextInt(population.size()));

		for (int i = 1; i < tournamentSize; i++) {
			Node candidate = population.get(random.nextInt(population.size()));

			if (isBetter(candidate, best)) {
				best = candidate;
			}
		}

		return best;
	}

	@Override
	public ArrayList<Node> geneticOperators() {
		ArrayList<Node> children = new ArrayList<Node>();
		HashSet<String> signatures = new HashSet<String>();

		int attempts = 0;

		while (children.size() < POPULATION_SIZE && attempts < POPULATION_SIZE * 100) {
			attempts++;

			Node child;

			if (random.nextFloat() < crossoverRate) {
				Node p1 = selectedParents.get(random.nextInt(selectedParents.size()));
				Node p2 = selectedParents.get(random.nextInt(selectedParents.size()));
				child = crossover(p1, p2);
			} else {
				Node p = selectedParents.get(random.nextInt(selectedParents.size()));
				child = cloneTree(p);
			}

			if (random.nextFloat() < mutationRate) {
				child = pointMutation(child);
			}

			if (depth(child) > maxOffspringDepth) {
				continue;
			}

			String key = child.TreeToString();

			if (!signatures.contains(key)) {
				children.add(child);
				signatures.add(key);
			}
		}

		while (children.size() < POPULATION_SIZE) {
			Node fallback = generateGrow(treeDepth, 0);
			children.add(fallback);
		}

		return children;
	}

	@Override
	public ArrayList<Node> populationReplacement() {
		ArrayList<Node> nextPopulation = new ArrayList<Node>();
		HashSet<String> signatures = new HashSet<String>();

		Node elite = cloneTree(bestTree());
		nextPopulation.add(elite);
		signatures.add(elite.TreeToString());

		for (int i = 0; i < offspring.size() && nextPopulation.size() < POPULATION_SIZE; i++) {
			Node child = offspring.get(i);
			String key = child.TreeToString();

			if (!signatures.contains(key)) {
				nextPopulation.add(child);
				signatures.add(key);
			}
		}

		while (nextPopulation.size() < POPULATION_SIZE) {
			Node candidate = generateGrow(treeDepth, 0);
			String key = candidate.TreeToString();

			if (!signatures.contains(key)) {
				nextPopulation.add(candidate);
				signatures.add(key);
			}
		}

		return nextPopulation;
	}

	public Node getBestTree() {
		return root;
	}

	public int classify(Node tree, Data row) {
		double value = evaluate(tree, row);

		if (value >= 0.5) {
			return 1;
		}

		return 0;
	}

	private double evaluate(Node node, Data row) {
		if (node == null) {
			return 0.0;
		}

		if (node.type == Node.Type.TERMINAL) {
			return terminalValue(node.value, row);
		}

		double left = evaluate(node.left, row);
		double right = evaluate(node.right, row);

		if (node.value.equals("+")) {
			return left + right;
		}

		if (node.value.equals("-")) {
			return left - right;
		}

		if (node.value.equals("*")) {
			return left * right;
		}

		if (node.value.equals("/")) {
			if (Math.abs(right) < 0.000001) {
				return left;
			}

			return left / right;
		}

		return 0.0;
	}

	private double terminalValue(String terminal, Data row) {
		if (terminal.equals("age"))
			return row.age;
		if (terminal.equals("menopause"))
			return row.menopause;
		if (terminal.equals("nodes"))
			return row.nodes;
		if (terminal.equals("tumorSize"))
			return row.tumorSize;
		if (terminal.equals("nodeCaps"))
			return row.nodeCaps;
		if (terminal.equals("irradiat"))
			return row.irradiat;
		if (terminal.equals("breast"))
			return row.breast;
		if (terminal.equals("quad"))
			return row.quad;

		try {
			return Double.parseDouble(terminal);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}


	private Node randomTerminal() {
		String value = terminalSet.get(random.nextInt(terminalSet.size()));
		return new Node(Node.Type.TERMINAL, value);
	}

	private Node randomFunction() {
		String value = functionSet.get(random.nextInt(functionSet.size()));
		return new Node(Node.Type.FUNCTION, value);
	}

	private Node crossover(Node parent1, Node parent2) {
		Node child = cloneTree(parent1);

		ArrayList<ArrayList<Integer>> childPaths = getAllPaths(child);
		ArrayList<ArrayList<Integer>> donorPaths = getAllPaths(parent2);

		ArrayList<Integer> childPath = childPaths.get(random.nextInt(childPaths.size()));
		ArrayList<Integer> donorPath = donorPaths.get(random.nextInt(donorPaths.size()));

		Node donorSubtree = cloneTree(getNodeAtPath(parent2, donorPath));

		child = replaceAtPath(child, childPath, donorSubtree);

		return child;
	}

	private Node pointMutation(Node tree) {
		Node mutated = cloneTree(tree);

		ArrayList<ArrayList<Integer>> paths = getAllPaths(mutated);
		ArrayList<Integer> path = paths.get(random.nextInt(paths.size()));
		Node target = getNodeAtPath(mutated, path);

		if (target.type == Node.Type.FUNCTION) {
			String newValue = target.value;

			while (functionSet.size() > 1 && newValue.equals(target.value)) {
				newValue = functionSet.get(random.nextInt(functionSet.size()));
			}

			target.value = newValue;
		} else {
			String newValue = target.value;

			while (terminalSet.size() > 1 && newValue.equals(target.value)) {
				newValue = terminalSet.get(random.nextInt(terminalSet.size()));
			}

			target.value = newValue;
		}

		return mutated;
	}

	private Node cloneTree(Node node) {
		if (node == null) {
			return null;
		}

		Node copy = new Node(node.type, node.value);
		copy.left = cloneTree(node.left);
		copy.right = cloneTree(node.right);

		return copy;
	}

	private int countNodes(Node node) {
		if (node == null) {
			return 0;
		}

		return 1 + countNodes(node.left) + countNodes(node.right);
	}

	private int depth(Node node) {
		if (node == null) {
			return 0;
		}

		int leftDepth = depth(node.left);
		int rightDepth = depth(node.right);

		return 1 + Math.max(leftDepth, rightDepth);
	}

	private ArrayList<ArrayList<Integer>> getAllPaths(Node rootNode) {
		ArrayList<ArrayList<Integer>> paths = new ArrayList<ArrayList<Integer>>();
		collectPaths(rootNode, new ArrayList<Integer>(), paths);
		return paths;
	}

	private void collectPaths(Node node, ArrayList<Integer> currentPath, ArrayList<ArrayList<Integer>> paths) {
		if (node == null) {
			return;
		}

		paths.add(new ArrayList<Integer>(currentPath));

		currentPath.add(0);
		collectPaths(node.left, currentPath, paths);
		currentPath.remove(currentPath.size() - 1);

		currentPath.add(1);
		collectPaths(node.right, currentPath, paths);
		currentPath.remove(currentPath.size() - 1);
	}

	private Node getNodeAtPath(Node node, ArrayList<Integer> path) {
		Node current = node;

		for (int i = 0; i < path.size(); i++) {
			if (path.get(i) == 0) {
				current = current.left;
			} else {
				current = current.right;
			}
		}

		return current;
	}

	private Node replaceAtPath(Node rootNode, ArrayList<Integer> path, Node replacement) {
		if (path.size() == 0) {
			return replacement;
		}

		Node current = rootNode;

		for (int i = 0; i < path.size() - 1; i++) {
			if (path.get(i) == 0) {
				current = current.left;
			} else {
				current = current.right;
			}
		}

		int lastDirection = path.get(path.size() - 1);

		if (lastDirection == 0) {
			current.left = replacement;
		} else {
			current.right = replacement;
		}

		return rootNode;
	}

}
