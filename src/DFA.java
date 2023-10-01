import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DFA {
    private final Map<Integer, DFAState> states;
    private DFAState startState;
    private DFAState acceptState;
    private final Set<Integer> alphabet;

    public DFA(Set<Integer> alphabet) {
        this.alphabet = alphabet;
        this.states = new HashMap<>();
    }
    public DFA(DFAState startState, Set<Integer> alphabet) {
        this.startState = startState;
        this.alphabet = alphabet;
        this.states = new HashMap<>();
        addState(startState);
    }

    public DFAState getStartState() {
        return startState;
    }
    public DFAState getAcceptState() {
        return acceptState;
    }

    public void addState(DFAState state) {
        states.put(state.getId(), state);
        if (state.isStart()) {
            startState = state;
        }
        if (state.isAccept()) {
            acceptState = state;
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
