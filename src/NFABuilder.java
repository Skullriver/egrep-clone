import java.util.HashSet;
import java.util.Set;

//TODO add processing of +
public class NFABuilder {
    private static int stateIdCounter = 0;
    public static NFA convertToNFA(SyntaxTreeNode syntaxTree) {


        if (syntaxTree == null) {
            return null;
        }

        NFA automaton = new NFA();

        if (syntaxTree.operation == SyntaxTreeBuilder.CONCAT) {
            // Handle concatenation
            NFA leftNFA = convertToNFA(syntaxTree.left);
            NFA rightNFA = convertToNFA(syntaxTree.right);
            // Connect leftNFA's end state to rightNFA's start state with ε-transitions.
            NFAState startState = leftNFA.getStartState();
            leftNFA.getAcceptState().addTransition(NFA.EPSILON, rightNFA.getStartState());
            NFAState endState = rightNFA.getAcceptState();
            // Update start and end states accordingly.
            automaton.addState(startState);
            automaton.addState(endState);
        } else if (syntaxTree.operation == SyntaxTreeBuilder.ALTERN) {

            NFAState startState = new NFAState(stateIdCounter++, true, false);
            NFAState endState = new NFAState(stateIdCounter++, false, true);
            // Handle alternation
            NFA leftNFA = convertToNFA(syntaxTree.left);
            NFA rightNFA = convertToNFA(syntaxTree.right);
            // Create new start and end states, connect them to leftNFA and rightNFA with ε-transitions.
            startState.addTransition(NFA.EPSILON, leftNFA.getStartState());
            startState.addTransition(NFA.EPSILON, rightNFA.getStartState());

            leftNFA.getAcceptState().addTransition(NFA.EPSILON, endState);
            rightNFA.getAcceptState().addTransition(NFA.EPSILON, endState);
            // Update start and end states accordingly.
            automaton.addState(startState);
            automaton.addState(endState);
        } else if (syntaxTree.operation == SyntaxTreeBuilder.ETOILE) {

            NFAState startState = new NFAState(stateIdCounter++, true, false);
            NFAState endState = new NFAState(stateIdCounter++, false, true);
            // Handle Kleene star
            NFA subNFA = convertToNFA(syntaxTree.left);
            // Create new start and end states, connect them to subNFA with ε-transitions.
            startState.addTransition(NFA.EPSILON, subNFA.getStartState());
            startState.addTransition(NFA.EPSILON, endState);

            subNFA.getAcceptState().addTransition(NFA.EPSILON, endState);
            // Also, add ε-transitions from subNFA's end state to its start state.
            subNFA.getAcceptState().addTransition(NFA.EPSILON, subNFA.getStartState());
            // Update start and end states accordingly.
            automaton.addState(startState);
            automaton.addState(endState);
        } else {
            // Handle character or state
            // Create a simple NFA with a single state, transition labeled with syntaxTree.operation.
            NFAState startState = new NFAState(stateIdCounter++, true, false);
            NFAState endState = new NFAState(stateIdCounter++, false, true);

            startState.addTransition(syntaxTree.operation, endState);
            // Update start and end states accordingly.
            automaton.addState(startState);
            automaton.addState(endState);
        }


        // Return the NFA for the current subtree.
        // You'll need to return the start and end states.
        return automaton;

        
    }

    public static String generateDOT(NFA nfa) {
        StringBuilder dot = new StringBuilder();

        // DOT header
        dot.append("digraph NFA {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape=circle];\n");
        dot.append("  start [shape=point];\n");

        // Define start state
        dot.append("  start -> ").append(nfa.getStartState().getId()).append(";\n");

        // Define accept state
        dot.append("  ").append(nfa.getAcceptState().getId()).append(" [shape=doublecircle];\n");

        // Define transitions
        Set<NFAState> visited = new HashSet<>();
        generateDOTRecursive(dot, nfa.getStartState(), visited);

        // DOT footer
        dot.append("}\n");

        return dot.toString();
    }

    private static void generateDOTRecursive(StringBuilder dot, NFAState state, Set<NFAState> visited) {
        visited.add(state);
        int fromStateId = state.getId();

        for (int symbol : state.getTransitions().keySet()) {
            for (NFAState toState : state.getTransitions().get(symbol)) {
                int toStateId = toState.getId();
                char symbolChar = (char) symbol;
                if(symbol == NFA.EPSILON)
                    symbolChar = 'ε';
                dot.append("  ").append(fromStateId).append(" -> ").append(toStateId)
                        .append(" [label=\"").append(symbolChar).append("\"];\n");

                if (!visited.contains(toState)) {
                    generateDOTRecursive(dot, toState, visited);
                }
            }
        }
    }

}
