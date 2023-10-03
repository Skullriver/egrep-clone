import java.util.HashSet;
import java.util.Set;

//TODO add processing of +
public class NFABuilder {
    private static int stateIdCounter = 0;

    public static NFA syntaxTreeToNFA(SyntaxTreeNode syntaxTree) {

        if (syntaxTree == null) {
            return null;
        }

        NFA automaton = new NFA();

        // handle concatenation
        if (syntaxTree.operation == SyntaxTreeBuilder.CONCAT) {

            // build left and right automatons
            NFA leftNFA = syntaxTreeToNFA(syntaxTree.left);
            NFA rightNFA = syntaxTreeToNFA(syntaxTree.right);
            // connect leftNFA's end state to rightNFA's start state with ε-transition
            NFAState startState = leftNFA.getStartState();
            leftNFA.getAcceptState().addTransition(NFA.EPSILON, rightNFA.getStartState());
            NFAState endState = rightNFA.getAcceptState();

            automaton.addState(startState);
            automaton.addState(endState);
        } else if (syntaxTree.operation == SyntaxTreeBuilder.ALTERN) { // handle alternation
            // create new start and end states
            NFAState startState = new NFAState(stateIdCounter++, true, false);
            NFAState endState = new NFAState(stateIdCounter++, false, true);

            // create sub-automatons for each option
            NFA leftNFA = syntaxTreeToNFA(syntaxTree.left);
            NFA rightNFA = syntaxTreeToNFA(syntaxTree.right);

            // connect startState to leftNFA and rightNFA with ε-transitions
            startState.addTransition(NFA.EPSILON, leftNFA.getStartState());
            startState.addTransition(NFA.EPSILON, rightNFA.getStartState());

            // connect endState to leftNFA and rightNFA with ε-transitions
            leftNFA.getAcceptState().addTransition(NFA.EPSILON, endState);
            rightNFA.getAcceptState().addTransition(NFA.EPSILON, endState);


            automaton.addState(startState);
            automaton.addState(endState);
        } else if (syntaxTree.operation == SyntaxTreeBuilder.ASTERISK) { // handle kleene star
            // create new start and end states
            NFAState startState = new NFAState(stateIdCounter++, true, false);
            NFAState endState = new NFAState(stateIdCounter++, false, true);

            // create sub-automaton
            NFA subNFA = syntaxTreeToNFA(syntaxTree.left);
            // connect startState to subNFA with ε-transitions
            startState.addTransition(NFA.EPSILON, subNFA.getStartState());
            startState.addTransition(NFA.EPSILON, endState);
            // connect endState to subNFA with ε-transitions
            subNFA.getAcceptState().addTransition(NFA.EPSILON, endState);
            // also add ε-transition from subNFA's end state to its start state
            subNFA.getAcceptState().addTransition(NFA.EPSILON, subNFA.getStartState());

            automaton.addState(startState);
            automaton.addState(endState);
        } else { // handle character
            // create a simple NFA with a single state, transition labeled with syntaxTree.operation
            NFAState startState = new NFAState(stateIdCounter++, true, false);
            NFAState endState = new NFAState(stateIdCounter++, false, true);

            startState.addTransition(syntaxTree.operation, endState);

            automaton.addState(startState);
            automaton.addState(endState);
        }

        // return the NFA for the current subtree
        return automaton;
    }

    // Method to visualize NFA in language DOT https://graphs.grevian.org/graph
    public static String generateDOT(NFA nfa) {
        StringBuilder dot = new StringBuilder();

        // DOT header
        dot.append("digraph NFA {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape=circle];\n");
        dot.append("  start [shape=point];\n");

        // define start state
        dot.append("  start -> ").append(nfa.getStartState().getId()).append(";\n");

        // define transitions
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

                if (symbol == NFA.EPSILON)
                    symbolChar = 'ε';

                if (symbol == SyntaxTreeBuilder.DOT)
                    symbolChar = '.';

                dot.append("  ").append(fromStateId).append(" -> ").append(toStateId)
                        .append(" [label=\"").append(symbolChar).append("\"];\n");

                if (toState.isAccept() && !visited.contains(toState))
                    dot.append("  ").append(toStateId).append(" [shape=doublecircle];\n");

                if (!visited.contains(toState)) {
                    generateDOTRecursive(dot, toState, visited);
                }
            }
        }
    }

}
