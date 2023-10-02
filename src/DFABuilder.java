import java.util.*;

public class DFABuilder {

    public static DFA NFAToDFA(NFA nfa) {

        // subsets method to regroup NFA states into DFA state
        Set<Set<NFAState>> statesDFA = new HashSet<>();
        // correspondence of NFA states groups to DFA state
        Map<Set<NFAState>, DFAState> mapping = new HashMap<>();

        Set<NFAState> visited = new HashSet<>();
        Set<Integer> fullAlphabet = getAlphabetFromNFA(new HashSet<>(), nfa.getStartState(), visited);

        Queue<Set<NFAState>> queue = new LinkedList<>();
        Set<NFAState> initialStates = new HashSet<>();

        // add start state and all states connected to it with ε-transitions in the queue
        initialStates.add(nfa.getStartState());
        initialStates.addAll(epsilonClosure(initialStates));
        statesDFA.add(initialStates);
        queue.add(initialStates);

        while (!queue.isEmpty()) {
            // add current state from queue and all states connected to it with ε-transitions
            Set<NFAState> currStates = queue.poll();
            currStates.addAll(epsilonClosure(currStates));

            //for each possible symbol check if there is transition
            //and look for all states connected to it with ε-transitions
            for (int symbol : fullAlphabet) {
                Set<NFAState> newStates = move(currStates, symbol);
                newStates.addAll(epsilonClosure(newStates));

                if (newStates.isEmpty()) continue;

                //add new group to the queue
                if (!statesDFA.contains(newStates)) {
                    statesDFA.add(newStates);
                    queue.add(newStates);
                }

                //create new DFA state corresponding to current group of NFA states
                if (!mapping.containsKey(currStates)) {
                    mapping.put(currStates, new DFAState(mapping.size(), isStartState(currStates, nfa), isAcceptState(currStates, nfa)));
                }
                //create new DFA state corresponding to next group of NFA states
                if (!mapping.containsKey(newStates)) {
                    mapping.put(newStates, new DFAState(mapping.size(), isStartState(newStates, nfa), isAcceptState(newStates, nfa)));
                }
                //add transition between created DFA states
                mapping.get(currStates).addTransition(symbol, mapping.get(newStates));
            }
        }

        //build DFA from correspondence of NFA states groups to DFA state
        return buildDFAFromMapping(mapping, fullAlphabet);
    }

    public static DFA minimizeDFA(DFA dfa) {

        // partition states into accepting and non-accepting groups
        Set<DFAState> acceptingStates = new HashSet<>();
        Set<DFAState> nonAcceptingStates = new HashSet<>();
        for (DFAState state : dfa.getStates().values()) {
            if (state.isAccept()) {
                acceptingStates.add(state);
            } else {
                nonAcceptingStates.add(state);
            }
        }

        List<Set<DFAState>> partitions = new ArrayList<>();
        partitions.add(acceptingStates);
        partitions.add(nonAcceptingStates);

        // refinement process
        // look for states that have the same set of transitions and regroup them
        boolean changed;
        do {
            changed = false;
            List<Set<DFAState>> newPartitions = new ArrayList<>();

            for (Set<DFAState> partition : partitions) {
                List<Set<DFAState>> split = splitPartition(partition);
                newPartitions.addAll(split);
                if (split.size() > 1) {
                    changed = true;
                }
            }

            partitions = newPartitions;
        } while (changed);

        // build the minimized DFA using groups of states
        // if there are many states in the same group we can consider them equal
        Map<Set<DFAState>, DFAState> minimizedStates = new HashMap<>();
        //helper to save group where the start state is in
        Set<DFAState> startStatePartition = new HashSet<>();
        //helper to save groups where the accepting states are in
        Set<Set<DFAState>> acceptStatePartitions = new HashSet<>();

        for (Set<DFAState> partition : partitions) {
            if (!partition.isEmpty()) {
                //choose randomly one of the state from the group as representativeState
                DFAState representativeState = partition.iterator().next();
                minimizedStates.put(partition, representativeState);
            }
            //save group where the start state is in
            // (if the start state was regrouped with other states, we could not choose it previously)
            if (!partition.isEmpty() && partition.contains(dfa.getStartState())) startStatePartition = partition;
            //same for accepting state
            if (!partition.isEmpty() && partition.stream().anyMatch(dfa.getAcceptStates()::contains))
                acceptStatePartitions.add(partition);
        }

        Map<Integer, DFAState> newStates = new HashMap<>();

        for (Set<DFAState> currentPartition : minimizedStates.keySet()) {
            //if current partition is the one where start state is, and we didn't choose initial start state, them make it start state
            if (currentPartition.equals(startStatePartition) && !minimizedStates.get(currentPartition).isStart())
                minimizedStates.get(currentPartition).setStart();
            //same for accept state
            if (acceptStatePartitions.contains(currentPartition) && !minimizedStates.get(currentPartition).isAccept())
                minimizedStates.get(currentPartition).setAccept();

            Map<Integer, DFAState> newTransitions = new HashMap<>();
            //save new transitions for each minimized state
            for (int symbol : minimizedStates.get(currentPartition).getTransitions().keySet()) {
                for (Set<DFAState> partition : partitions) {
                    if (partition.contains(minimizedStates.get(currentPartition).getTransitions(symbol))) {
                        newTransitions.put(symbol, minimizedStates.get(partition));
                    }
                }
            }
            minimizedStates.get(currentPartition).removeAllTransition();
            //update transitions for each minimized state
            for (int symbol : newTransitions.keySet()) {
                minimizedStates.get(currentPartition).addTransition(symbol, newTransitions.get(symbol));
            }
            newStates.put(minimizedStates.get(currentPartition).getId(), minimizedStates.get(currentPartition));
        }

        //create minimized DFA
        DFA minimizedDFA = new DFA(dfa.getAlphabet());
        for (Map.Entry<Integer, DFAState> entry : newStates.entrySet()) {
            DFAState representativeState = entry.getValue();
            minimizedDFA.addState(representativeState);
        }

        return minimizedDFA;
    }

    // helper method to split a partition based on transitions
    private static List<Set<DFAState>> splitPartition(Set<DFAState> partition) {
        Map<Integer, Set<DFAState>> transitions = new HashMap<>();

        // group states in the partition by their transitions
        for (DFAState state : partition) {
            int transitionHashCode = state.getTransitionsHashCode();
            transitions.computeIfAbsent(transitionHashCode, k -> new HashSet<>()).add(state);
        }

        return new ArrayList<>(transitions.values());
    }

    //get all acceptable symbols
    private static Set<Integer> getAlphabetFromNFA(Set<Integer> alphabet, NFAState state, Set<NFAState> visited) {

        visited.add(state);

        for (int symbol : state.getTransitions().keySet()) {
            for (NFAState toState : state.getTransitions().get(symbol)) {
                if (symbol != NFA.EPSILON) alphabet.add(symbol);
                if (!visited.contains(toState)) {
                    getAlphabetFromNFA(alphabet, toState, visited);
                }
            }
        }

        return alphabet;
    }

    //get all states reachable through ε-transitions
    private static Set<NFAState> epsilonClosure(Set<NFAState> states) {
        Set<NFAState> closure = new HashSet<>(states);
        Queue<NFAState> queue = new LinkedList<>(states);

        while (!queue.isEmpty()) {
            NFAState state = queue.poll();
            Set<NFAState> epsilonTransitions = state.getTransitions(NFA.EPSILON);
            for (NFAState nextState : epsilonTransitions) {
                if (!closure.contains(nextState)) {
                    closure.add(nextState);
                    queue.add(nextState);
                }
            }
        }

        return closure;
    }

    //get all next states reachable through symbol-transitions
    private static Set<NFAState> move(Set<NFAState> states, int symbol) {
        Set<NFAState> moveStates = new HashSet<>();
        for (NFAState state : states) {
            moveStates.addAll(state.getTransitions(symbol));
        }
        return moveStates;
    }

    private static boolean isAcceptState(Set<NFAState> states, NFA nfa) {
        for (NFAState state : states) {
            if (state.isAccept() && state.equals(nfa.getAcceptState())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStartState(Set<NFAState> states, NFA nfa) {
        for (NFAState state : states) {
            if (state.isStart() && state.equals(nfa.getStartState())) {
                return true;
            }
        }
        return false;
    }

    private static DFA buildDFAFromMapping(Map<Set<NFAState>, DFAState> mapping, Set<Integer> alphabet) {
        DFA dfa = new DFA(alphabet);
        for (Map.Entry<Set<NFAState>, DFAState> entry : mapping.entrySet()) {
            dfa.addState(entry.getValue());
        }
        return dfa;
    }

    //Method to visualize NFA in language DOT https://graphs.grevian.org/graph
    public static String generateDOT(DFA dfa) {
        StringBuilder dot = new StringBuilder();

        // DOT header
        dot.append("digraph DFA {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape=circle];\n");
        dot.append("  start [shape=point];\n");

        // Define start state
        dot.append("  start -> ").append(dfa.getStartState().getId()).append(";\n");

        // Define accept state
        //dot.append("  ").append(dfa.getAcceptState().getId()).append(" [shape=doublecircle];\n");

        // Define transitions
        Set<DFAState> visited = new HashSet<>();
        generateDOTRecursive(dot, dfa.getStartState(), visited);

        // DOT footer
        dot.append("}\n");

        return dot.toString();
    }

    private static void generateDOTRecursive(StringBuilder dot, DFAState state, Set<DFAState> visited) {
        visited.add(state);
        int fromStateId = state.getId();

        for (int symbol : state.getTransitions().keySet()) {
            DFAState toState = state.getTransitions().get(symbol);
            int toStateId = toState.getId();
            char symbolChar = (char) symbol;

            if (symbol == SyntaxTreeBuilder.DOT)
                symbolChar = '.';

            dot.append("  ").append(fromStateId).append(" -> ").append(toStateId).append(" [label=\"").append(symbolChar).append("\"];\n");

            if (toState.isAccept() && !visited.contains(toState))
                dot.append("  ").append(toStateId).append(" [shape=doublecircle];\n");

            if (!visited.contains(toState)) {
                generateDOTRecursive(dot, toState, visited);
            }
        }
    }

}
