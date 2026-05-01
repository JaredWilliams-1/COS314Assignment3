import java.util.ArrayList;
import java.util.HashMap;

public class SymbolicGP implements GP {

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

	public SymbolicGP(
			String filename,
			ArrayList<String> terminalSet,
			ArrayList<String> functionSet,
			int treeDepth,
			int maxOffspring,
			int tournamentSize,
			float crossoverRate,
			float mutationRate,
			int mutationOffspringDepth,
			int maxGenerations,
			int seed) {
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
	}

	@Override
	public Node bestTree() {
		return null;
	}

	@Override
	public Node build() {
		return null;
	}

	@Override
	public HashMap<Node, Float> intialisePopulation() {
		return null;
	}

	@Override
	public HashMap<Node, Float> selection() {
		return null;
	}

	@Override
	public HashMap<Node, Float> geneticOperators() {
		return null;
	}

	@Override
	public HashMap<Node, Float> populationReplacement() {
		return null;
	}
    
}
