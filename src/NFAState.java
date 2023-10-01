import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NFAState {

    private final int id;
    private final Map<Integer, Set<NFAState>> transitions;
    private final boolean isStart;
    private final boolean isAccept;

    public NFAState(int id, boolean isStart, boolean isAccept) {
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

    public boolean isAccept() {
        return isAccept;
    }

    public void addTransition(int symbol, NFAState nextState) {
        transitions.computeIfAbsent(symbol, k -> new HashSet<>()).add(nextState);
    }

    public Set<NFAState> getTransitions(int symbol) {
        return transitions.getOrDefault(symbol, new HashSet<>());
    }
    public Map<Integer, Set<NFAState>> getTransitions() {
        return transitions;
    }

}
