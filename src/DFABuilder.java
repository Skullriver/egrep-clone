import java.util.*;

public class DFABuilder {

    public static DFA convertToDFA(NFA nfa) {
        Set<Set<NFAState>> statesDFA = new HashSet<>();

        Map<Set<NFAState>, DFAState> mapping = new HashMap<>();
        Set<Integer> alphabet = new HashSet<>();

        Set<NFAState> visited = new HashSet<>();
        Set<Integer> fullAlphabet = getAlphabetFromNFA(alphabet, nfa.getStartState(), visited);

        Queue<Set<NFAState>> queue = new LinkedList<>();
        Set<NFAState> initialStates = new HashSet<>();
        initialStates.add(nfa.getStartState());
        initialStates.addAll(epsilonClosure(initialStates));

        statesDFA.add(initialStates);
        queue.add(initialStates);

        while (!queue.isEmpty()) {
            Set<NFAState> currStates = queue.poll();
            currStates.addAll(epsilonClosure(currStates));

            for (int symbol : fullAlphabet) {
                Set<NFAState> newStates = move(currStates, symbol);
                newStates.addAll(epsilonClosure(newStates));

                if (newStates.isEmpty()) continue;

                if (!statesDFA.contains(newStates)) {
                    statesDFA.add(newStates);
                    queue.add(newStates);
                }

                if (!mapping.containsKey(currStates)) {
                    mapping.put(currStates, new DFAState(mapping.size(), isStartState(currStates, nfa), isAcceptState(currStates, nfa)));
                }

                if (!mapping.containsKey(newStates) ) {
                    mapping.put(newStates, new DFAState(mapping.size(), isStartState(newStates, nfa), isAcceptState(newStates, nfa)));
                }
                mapping.get(currStates).addTransition(symbol, mapping.get(newStates));
            }
        }

        return buildDFAFromMapping(mapping, nfa, fullAlphabet);
    }

    public static DFA minimizeDFA(DFA dfa) {
        // Step 1: Partition states into accepting and non-accepting groups
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

        // Step 2: Refinement process
        boolean changed;
        do {
            changed = false;
            List<Set<DFAState>> newPartitions = new ArrayList<>();

            for (Set<DFAState> partition : partitions) {
                List<Set<DFAState>> split = splitPartition(partition, dfa);
                newPartitions.addAll(split);
                if (split.size() > 1) {
                    changed = true;
                }
            }

            partitions = newPartitions;
        } while (changed);

        // Step 3: Construct the minimized DFA
        Map<Set<DFAState>, DFAState> minimizedStates = new HashMap<>();
        Set<DFAState> startStatePartition = new HashSet<>();
        Set<DFAState> acceptStatePartition = new HashSet<>();
        for (Set<DFAState> partition : partitions) {
            if (!partition.isEmpty()) {
                DFAState representativeState = partition.iterator().next();
                minimizedStates.put(partition, representativeState);
            }
            if(!partition.isEmpty() && partition.contains(dfa.getStartState()))
                startStatePartition = partition;
            if(!partition.isEmpty() && partition.contains(dfa.getAcceptState()))
                acceptStatePartition = partition;
        }

        Map<Integer, DFAState> newStates = new HashMap<>();
        for (Set<DFAState> part : minimizedStates.keySet()){
            if (part.equals(startStatePartition) && !minimizedStates.get(part).isStart())
                minimizedStates.get(part).setStart();
            if (part.equals(acceptStatePartition) && !minimizedStates.get(part).isAccept())
                minimizedStates.get(part).setAccept();
            Map<Integer, DFAState> newTransitions = new HashMap<>();
            for (int symbol : minimizedStates.get(part).getTransitions().keySet()){
                for (Set<DFAState> partition : partitions) {
                    if (partition.contains(minimizedStates.get(part).getTransitions(symbol))) {
                        newTransitions.put(symbol, minimizedStates.get(partition));
                    }
                }
            }
            minimizedStates.get(part).removeAllTransition();
            for (int symbol : newTransitions.keySet()){
                minimizedStates.get(part).addTransition(symbol, newTransitions.get(symbol));
            }
            newStates.put(minimizedStates.get(part).getId(), minimizedStates.get(part));
        }

        //DFAState minimizedStartState = newStates.get(dfa.getStartState().getId());

//        DFA minimizedDFA = new DFA(minimizedStartState, dfa.getAlphabet());
//
//        for (Map.Entry<Integer, DFAState> entry : newStates.entrySet()) {
//            DFAState representativeState = entry.getValue();
//            minimizedDFA.addState(representativeState);
//        }

        DFA minimizedDFA = new DFA(dfa.getAlphabet());
        for (Map.Entry<Integer, DFAState> entry : newStates.entrySet()) {
            DFAState representativeState = entry.getValue();
            minimizedDFA.addState(representativeState);
        }

        return minimizedDFA;
    }

    // Helper method to split a partition based on transitions
    private static List<Set<DFAState>> splitPartition(Set<DFAState> partition, DFA dfa) {
        List<Set<DFAState>> split = new ArrayList<>();
        Map<Integer, Set<DFAState>> transitions = new HashMap<>();

        // Group states in the partition by their transitions
        for (DFAState state : partition) {
            int transitionHashCode = state.getTransitionsHashCode();
            transitions.computeIfAbsent(transitionHashCode, k -> new HashSet<>()).add(state);
        }

        // Add groups with more than one state to the split
        for (Set<DFAState> group : transitions.values()) {
            if (group.size() > 1) {
                split.add(group);
            }
        }

        // Add remaining single-state groups
        for (Set<DFAState> group : transitions.values()) {
            if (group.size() == 1) {
                split.add(group);
            }
        }

        return split;
    }

    private static Set<Integer> getAlphabetFromNFA(Set<Integer> alphabet, NFAState state, Set<NFAState> visited) {

        visited.add(state);

        for (int symbol : state.getTransitions().keySet()) {
            for (NFAState toState : state.getTransitions().get(symbol)) {
                if(symbol != NFA.EPSILON)
                    alphabet.add(symbol);
                if (!visited.contains(toState)) {
                    getAlphabetFromNFA(alphabet, toState, visited);
                }
            }
        }

        return alphabet;
    }

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

    private static DFA buildDFAFromMapping(Map<Set<NFAState>, DFAState> mapping, NFA nfa, Set<Integer> alphabet) {
        DFA dfa = new DFA(mapping.get(epsilonClosure(Collections.singleton(nfa.getStartState()))), alphabet);
        for (Map.Entry<Set<NFAState>, DFAState> entry : mapping.entrySet()) {
            dfa.addState(entry.getValue());
        }
        return dfa;
    }

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
            dot.append("  ").append(fromStateId).append(" -> ").append(toStateId)
                    .append(" [label=\"").append(symbolChar).append("\"];\n");

            if(toState.isAccept() && !visited.contains(toState))
                dot.append("  ").append(toStateId).append(" [shape=doublecircle];\n");

            if (!visited.contains(toState)) {
                generateDOTRecursive(dot, toState, visited);
            }
        }
    }

}
