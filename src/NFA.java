import java.util.HashSet;
import java.util.Set;

public class NFA {

    static final int EPSILON = 0xE251104;
    private final Set<NFAState> states;
    private NFAState startState;
    private NFAState acceptState;

    public NFA() {
        this.states = new HashSet<>();
    }

    public void addState(NFAState state) {
        states.add(state);
        if (state.isStart()) {
            startState = state;
        }
        if (state.isAccept()) {
            acceptState = state;
        }
    }

    public NFAState getStartState() {
        return startState;
    }

    public NFAState getAcceptState() {
        return acceptState;
    }

    public Set<NFAState> getStates() {
        return states;
    }
}
