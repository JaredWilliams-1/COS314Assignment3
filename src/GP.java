import java.util.HashMap;

public interface GP {
    public Node bestTree(); // i know Unathi said it needs a name change, but it's fine as is imo
    public Node build(); //changes this to public coz interfaces dont allow
                         //  you to implement protected things, and if it was private I don't think it'd let you inherit it -- not sure abt that tho
    public HashMap<Node, Float> intialisePopulation();

    // I removed the parameters coz they are already attributes of the class
    public HashMap<Node, Float> selection();
    
    public HashMap<Node, Float> geneticOperators(); 
    
    public HashMap<Node, Float> populationReplacement();
}
