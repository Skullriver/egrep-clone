import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {
    static Set<DFAState> acceptStates;
    static DFAState startState;
    static Set<Integer> alphabet;
    static boolean flag;
    static ArrayList<Integer> toColor;

    public static List<Pair> search(DFA minDfa, String line) {

        List<Pair> matches = new ArrayList<>();

        DFAState currentState = minDfa.getStartState();
        int matchStartIndex = 0;
        int textIndex = 0;

        while (textIndex < line.length()) {
            int currentChar = line.charAt(textIndex);

            DFAState nextState = currentState.getTransitions(currentChar);
            if (nextState == null)
                nextState = currentState.getTransitions(SyntaxTreeBuilder.DOT);

            if (nextState != null) {
                currentState = nextState;

                if (currentState.isAccept()) {
                    int matchEndIndex = textIndex;
                    matches.add(new Pair(matchStartIndex, matchEndIndex));

                    // continue searching for the next match
                    currentState = minDfa.getStartState();
                    textIndex = matchStartIndex + 1;
                    matchStartIndex = textIndex;
                } else {
                    textIndex++;
                }
            } else {
                // no valid transition, return to the start state
                currentState = minDfa.getStartState();
                textIndex = matchStartIndex + 1;
                matchStartIndex = textIndex;
            }
        }

        return matches;
    }

    public static String readText(File file) {
        String data = "";
        try {
            data = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static void main(String[] args) throws Exception {
        flag = false;
        File file = new File("56667-0.txt");
        String text = readText(file);
        String[] lines = text.split("\\n");

        String expression = "S(a|r|g)*on";
        //String expression = "aabc";

        SyntaxTreeNode root = SyntaxTreeBuilder.buildSyntaxTree(expression);
        NFA nfa = NFABuilder.syntaxTreeToNFA(root);
        //System.out.println(NFABuilder.generateDOT(nfa));
        DFA dfa = DFABuilder.NFAToDFA(nfa);
        //System.out.println(DFABuilder.generateDOT(dfa));
        DFA minDfa = DFABuilder.minimizeDFA(dfa);
        //System.out.println(DFABuilder.generateDOT(minDfa));

        /////////////////TEST
        long startTime = System.currentTimeMillis();

        for (String line : lines) {
            List<Pair> matches = search(minDfa, line);
            if (!matches.isEmpty()) {
                StringBuilder highlightedLine = new StringBuilder();
                int currentIndex = 0;
                for (Pair couple : matches) {
                    // Append text before the match
                    if (couple.getStartIndex() > currentIndex) {
                        highlightedLine.append(line, currentIndex, couple.getStartIndex());
                    }

                    // Append the matched text in red
                    String matchedText = line.substring(couple.getStartIndex(), couple.getEndIndex() + 1);
                    highlightedLine.append("\u001B[31m"); // ANSI escape code for red text
                    highlightedLine.append(matchedText);
                    highlightedLine.append("\u001B[0m"); // Reset color

                    currentIndex = couple.getEndIndex() + 1;
                }
                // Append any remaining text after the last match
                if (currentIndex < line.length()) {
                    highlightedLine.append(line.substring(currentIndex));
                }

                // Print the line with highlighted matches
                System.out.println(highlightedLine.toString());
            }

        }

        long searchEndTime = System.currentTimeMillis();

        System.out.println("Time used: "+ (searchEndTime-startTime) + "ms");

        /////////////////

    }
}