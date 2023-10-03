public class SyntaxTreeNode {
    int operation; // Operator or character
    SyntaxTreeNode left; // Left subtree
    SyntaxTreeNode right; // Right subtree

    public SyntaxTreeNode(int operation) {
        this.operation = operation;
        this.left = null;
        this.right = null;
    }
}
