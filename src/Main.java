import java.util.ArrayList;

public class Main {
    public static void main(String [] args){
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

    }
}