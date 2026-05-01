public class Node {
    public String type;
    public String value;
    public Node left;
    public Node right;

    public Node(String type, String value){
        // type is whether it is function or terminal
        // value is the symbol (will obviously differ depending on whether it is terminal or terminal)
        this.type = type;
        this.value = value;
        this.left = null;
        this.right = null;
    }

    public void addLeft(Node leftNode){
        this.left = leftNode;
    }

    public void addRight(Node rightNode){
        this.right = rightNode;
    }
}
