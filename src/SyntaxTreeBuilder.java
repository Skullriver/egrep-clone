import java.util.ArrayList;

public class SyntaxTreeBuilder {

    static final int CONCAT = 0xC04CA7;
    static final int ETOILE = 0xE7011E;
    static final int ALTERN = 0xA17E54;
    static final int PROTECTION = 0xBADDAD;
    static final int OPENPARENTHESIS = 0x16641664;
    static final int CLOSEPARENTHESIS = 0x51515151;
    static final int DOT = 0xD07;
    public static SyntaxTreeNode buildSyntaxTree(String expression) throws Exception {
        if (expression == null || expression.isEmpty()) {
            return null;
        }

        ArrayList<SyntaxTreeNode> result = new ArrayList<SyntaxTreeNode>();
        for (int i=0;i<expression.length();i++) result.add(new SyntaxTreeNode(charToRoot(expression.charAt(i))));

        return parse(result);
    }

    private static SyntaxTreeNode parse(ArrayList<SyntaxTreeNode> result) throws Exception {

        while (containParenthesis(result)) result=processParenthesis(result);
        while (containEtoile(result)) result=processEtoile(result);
        while (containConcat(result)) result=processConcat(result);
        while (containAltern(result)) result=processAltern(result);

        return removeProtection(result.get(0));
    }

    private static ArrayList<SyntaxTreeNode> processParenthesis(ArrayList<SyntaxTreeNode> trees) throws Exception {
        ArrayList<SyntaxTreeNode> result = new ArrayList<SyntaxTreeNode>();
        boolean found = false;
        for (SyntaxTreeNode t: trees) {
            if (!found && t.operation==CLOSEPARENTHESIS) {
                boolean done = false;
                ArrayList<SyntaxTreeNode> content = new ArrayList<SyntaxTreeNode>();
                while (!done && !result.isEmpty())
                    if (result.get(result.size()-1).operation==OPENPARENTHESIS) { done = true; result.remove(result.size()-1); }
                    else content.add(0, result.remove(result.size()-1));
                if (!done) System.out.println("error");
                found = true;
                SyntaxTreeNode subTree = parse(content);
                SyntaxTreeNode protection = new SyntaxTreeNode(PROTECTION);
                protection.left = subTree;
                result.add(protection);
            } else {
                result.add(t);
            }
        }
        if (!found) System.out.println("err");
        return result;
    }

    private static ArrayList<SyntaxTreeNode> processAltern(ArrayList<SyntaxTreeNode> trees) {
        ArrayList<SyntaxTreeNode> result = new ArrayList<SyntaxTreeNode>();
        boolean found = false;
        SyntaxTreeNode gauche = null;
        boolean done = false;
        for (SyntaxTreeNode t: trees) {
            if (!found && t.operation==ALTERN && t.left == null && t.right == null) {
                if (result.isEmpty()) System.out.println("err");
                found = true;
                gauche = result.remove(result.size()-1);
                continue;
            }
            if (found && !done) {
                if (gauche==null) System.out.println("err");
                done=true;
                SyntaxTreeNode subTree = new SyntaxTreeNode(ALTERN);
                subTree.left = gauche;
                subTree.right = t;
                result.add(subTree);
            } else {
                result.add(t);
            }
        }
        return result;
    }

    private static ArrayList<SyntaxTreeNode> processConcat(ArrayList<SyntaxTreeNode> trees) {
        ArrayList<SyntaxTreeNode> result = new ArrayList<SyntaxTreeNode>();
        boolean found = false;
        boolean firstFound = false;
        for (SyntaxTreeNode t: trees) {
            if (!found && !firstFound && t.operation!=ALTERN) {
                firstFound = true;
                result.add(t);
                continue;
            }
            if (!found && firstFound && t.operation==ALTERN ) {
                firstFound = false;
                result.add(t);
                continue;
            }
            if (!found && firstFound && t.operation!=ALTERN) {
                found = true;
                SyntaxTreeNode last = result.remove(result.size()-1);
                SyntaxTreeNode subTree = new SyntaxTreeNode(CONCAT);
                subTree.left = last;
                subTree.right = t;
                result.add(subTree);
            } else {
                result.add(t);
            }
        }
        return result;
    }

    private static ArrayList<SyntaxTreeNode> processEtoile(ArrayList<SyntaxTreeNode> trees) {
        ArrayList<SyntaxTreeNode> result = new ArrayList<SyntaxTreeNode>();
        boolean found = false;
        for (SyntaxTreeNode t: trees) {
            if (!found && t.operation==ETOILE && t.left == null && t.right == null) {
                if (result.isEmpty()) System.out.println("err");
                found = true;
                SyntaxTreeNode last = result.remove(result.size()-1);
                SyntaxTreeNode subTree = new SyntaxTreeNode(ETOILE);
                subTree.left = last;
                result.add(subTree);
            } else {
                result.add(t);
            }
        }
        return result;
    }

    private static boolean containParenthesis(ArrayList<SyntaxTreeNode> trees) {
        for (SyntaxTreeNode t: trees) if (t.operation==OPENPARENTHESIS || t.operation==CLOSEPARENTHESIS) return true;
        return false;
    }
    private static boolean containEtoile(ArrayList<SyntaxTreeNode> trees) {
        for (SyntaxTreeNode t: trees) if (t.operation==ETOILE && t.left == null && t.right == null) return true;
        return false;
    }
    private static boolean containConcat(ArrayList<SyntaxTreeNode> trees) {
        boolean firstFound = false;
        for (SyntaxTreeNode t: trees) {
            if (!firstFound && t.operation!=ALTERN) { firstFound = true; continue; }
            if (firstFound) if (t.operation!=ALTERN) return true;
            else firstFound = false;
        }
        return false;
    }
    private static boolean containAltern(ArrayList<SyntaxTreeNode> trees) {
        for (SyntaxTreeNode t: trees) if (t.operation==ALTERN && t.left == null && t.right == null) return true;
        return false;
    }

    private static SyntaxTreeNode removeProtection(SyntaxTreeNode tree) throws Exception {
        if (tree.operation==PROTECTION && tree.right!=null) throw new Exception();
        if (tree.left == null) return tree;
        if (tree.operation==PROTECTION) return removeProtection(tree.left);

        SyntaxTreeNode subTree = new SyntaxTreeNode(tree.operation);
        if (tree.left != null) subTree.left = removeProtection(tree.left);
        if (tree.right != null) subTree.right = removeProtection(tree.right);
        return subTree;
    }

    private static int charToRoot(char c) {
        if (c=='.') return DOT;
        if (c=='*') return ETOILE;
        if (c=='|') return ALTERN;
        if (c=='(') return OPENPARENTHESIS;
        if (c==')') return CLOSEPARENTHESIS;
        return (int)c;
    }
    public static void printSyntaxTree(SyntaxTreeNode root) {
        if (root != null) {
            if (root.operation == CONCAT || root.operation == ALTERN || root.operation == ETOILE) {
                // Print the current operation as an operator
                System.out.print("(");
                printSyntaxTree(root.left);
                System.out.print(rootToString(root.operation));
                printSyntaxTree(root.right);
                System.out.print(")");
            } else {
                // Print a leaf node (character)
                System.out.print(rootToString(root.operation));
            }
        }
    }

    public static String rootToString(int operation) {
        if (operation==CONCAT) return ".";
        if (operation==ETOILE) return "*";
        if (operation==ALTERN) return "|";
        if (operation==DOT) return ".";
        return Character.toString((char)operation);
    }
}
