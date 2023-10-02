import java.util.HashMap;
import java.util.Map;

public class DFAState {
    private final int id;
    private final Map<Integer, DFAState> transitions;
    private boolean isStart;
    private boolean isAccept;
    public DFAState(int id, boolean isStart, boolean isAccept) {
        this.id = id;
        this.isStart = isStart;
        this.isAccept = isAccept;
        this.transitions = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart() {
        isStart = true;
    }

    public boolean isAccept() {
        return isAccept;
    }

    public void setAccept() {
        isAccept = true;
    }

    public void addTransition(int symbol, DFAState nextState) {
        transitions.put(symbol, nextState);
    }

    public void removeAllTransition() {
        transitions.clear();
    }

    public DFAState getTransitions(int symbol) {
        return transitions.get(symbol);
    }

    public Map<Integer, DFAState> getTransitions() {
        return transitions;
    }

    public int getTransitionsHashCode() {
        int hashCode = 0;

        for (Map.Entry<Integer, DFAState> entry : transitions.entrySet()) {
            int symbol = entry.getKey();
            DFAState targetState = entry.getValue();

            // include the symbol and target state IDs in the hash code
            hashCode = hashCode * 31 + symbol;
            hashCode = hashCode * 31 + targetState.getId();
        }

        return hashCode;
    }
}
