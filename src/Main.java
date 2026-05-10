public class Main {
    public static void main(String [] args){

        Node test1 = new Node(Node.Type.FUNCTION, "+");
        test1.addLeft(new Node(Node.Type.TERMINAL, "2"));
        test1.addRight(new Node(Node.Type.TERMINAL, "4"));

        System.out.println(test1.TreeToString());

    }
}