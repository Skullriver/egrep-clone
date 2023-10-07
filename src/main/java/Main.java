import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Main {

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
                    textIndex = matchEndIndex + 1;
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

    public static boolean isSimpleConcatenation(String regex) {

        regex = regex.trim().replaceAll("^[^\\w]+|[^\\w]+$", "");
        String[] parts = regex.split("[^\\w]+");

        return parts.length == 1 && parts[0].equals(regex);
    }

    public static void printResult(List<Pair> matches, String line) {
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

    public static void main(String[] args) throws Exception {

        String regEx, filename;
        File file;
        boolean print = true;

        if (args.length < 2) {
            System.out.println("To use : \"<RegEx>\" <filename>");
            return;
        }

        if (args[1].length() < 2) {
            System.out.println("To use : \"<RegEx>\" <filename>");
            return;
        }

        if (args.length == 3) {
            if (args[2].equals("--no-print")) print = false;
        }

        regEx = args[0];
        filename = args[1];
        System.out.println("RegEx : \"" + regEx + "\"");
        System.out.println("filename : " + filename);

        file = new File(filename);

        if (regEx.length() < 1) {
            System.out.println(">> ERROR: empty regEx.");
            return;
        }

        long startTime = System.currentTimeMillis();

        boolean simpleConcat = isSimpleConcatenation(regEx);

        if (simpleConcat) {
            KMP kmp = new KMP(regEx);
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    List<Pair> matches = kmp.search(line);
                    if (print) printResult(matches, line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            long startTree = System.currentTimeMillis();
            SyntaxTreeNode root = SyntaxTreeBuilder.buildSyntaxTree(regEx);
//            System.out.println("Time tree: " + (System.currentTimeMillis() - startTree) + "ms");
            long startNFA = System.currentTimeMillis();
            NFA nfa = NFABuilder.syntaxTreeToNFA(root);
//            System.out.println("Time NFA: " + (System.currentTimeMillis() - startNFA) + "ms");
//            System.out.println(NFABuilder.generateDOT(nfa));
            long startDFA = System.currentTimeMillis();
            DFA dfa = DFABuilder.NFAToDFA(nfa);
//            System.out.println("Time DFA: " + (System.currentTimeMillis() - startDFA) + "ms");
            //System.out.println(DFABuilder.generateDOT(dfa));
            long startMin = System.currentTimeMillis();
            DFA minDfa = DFABuilder.minimizeDFA(dfa);
//            System.out.println("Time min: " + (System.currentTimeMillis() - startMin) + "ms");
//            System.out.println(DFABuilder.generateDOT(minDfa));

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    List<Pair> matches = search(minDfa, line);
                    if (print) printResult(matches, line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//
        long searchEndTime = System.currentTimeMillis();

//        System.out.println("Time total: " + (searchEndTime - startTime) + "ms");

    }
}