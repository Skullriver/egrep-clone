import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DFA {
    private final Map<Integer, DFAState> states;
    private final Set<DFAState> acceptStates;
    private final Set<Integer> alphabet;
    private DFAState startState;

    public DFA(Set<Integer> alphabet) {
        this.alphabet = alphabet;
        this.states = new HashMap<>();
        this.acceptStates = new HashSet<>();
    }

    public DFAState getStartState() {
        return startState;
    }

    public Set<DFAState> getAcceptStates() {
        return acceptStates;
    }

    public void addState(DFAState state) {
        states.put(state.getId(), state);
        if (state.isStart()) {
            startState = state;
        }
        if (state.isAccept()) {
            acceptStates.add(state);
        }
    }

    public DFAState getState(int id) {
        return states.get(id);
    }

    public Map<Integer, DFAState> getStates() {
        return states;
    }

    public Set<Integer> getAlphabet() {
        return alphabet;
    }
}
