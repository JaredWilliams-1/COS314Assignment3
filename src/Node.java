public class Node {
    enum Type {
        FUNCTION,
        TERMINAL
    }
    public Type type;
    public String value;
    public Node left;
    public Node right;

    public Node(Type type, String value){
        // type is whether it is function or terminal
        // value is the symbol (will obviously differ depending on whether it is terminal or terminal)
        this.type = type;
        this.value = value;
        this.left = null;
        this.right = null;
    }

    //This is what is going to turn the tree into a unique String
    //so you just use it by going into one of them GP functions and saying String uniqueString = root.TreeToString
	public String TreeToString(){
		return ("root: " + TreeToStringHelper(this));
	}

	private String TreeToStringHelper(Node curr){
		if (curr == null){
			return "null";
		}
		return (curr.value + "(L:(" + TreeToStringHelper(curr.left) + ")" + "R:(" + TreeToStringHelper(curr.right) +"))");
	}

    public void addLeft(Node leftNode){
        this.left = leftNode;
    }

    public void addRight(Node rightNode){
        this.right = rightNode;
    }
}
