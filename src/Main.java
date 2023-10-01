public class Main {
    public static void main(String[] args) throws Exception {

        String expression = "(Sa|r|g)on*";

        SyntaxTreeNode root = SyntaxTreeBuilder.buildSyntaxTree(expression);

        // Now, you have the syntax tree representing the expression.
        SyntaxTreeBuilder.printSyntaxTree(root);
        System.out.println("\n");

        NFA graph = NFABuilder.convertToNFA(root);

        System.out.println(NFABuilder.generateDOT(graph));

        DFA dfa = DFABuilder.convertToDFA(graph);
        System.out.println(DFABuilder.generateDOT(dfa));

        DFA minDfa = DFABuilder.minimizeDFA(dfa);
        //System.out.println(minDfa.getStartState().getTransitions().get(83).getTransitions().get(83));
        System.out.println(DFABuilder.generateDOT(minDfa));
    }
}