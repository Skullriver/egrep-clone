import java.util.ArrayList;
import java.util.List;

public class KMP {

    private final String pattern;
    private final int[] carryOver;

    public KMP(String pattern) {
        this.pattern = pattern;
        this.carryOver = computeCarryOver();
    }

    private int[] computeCarryOver() {
        int[] lps = initializeLPS();
        optimizeLPS(lps);
        return lps;
    }

    private int[] initializeLPS() {
        int[] lps = new int[pattern.length() + 1];
        lps[0] = -1;
        int j = -1;

        for (int i = 0; i < pattern.length(); i++) {
            while (j >= 0 && pattern.charAt(i) != pattern.charAt(j)) {
                j = lps[j];
            }
            j++;
            lps[i + 1] = j;
        }

        return lps;
    }

    private void optimizeLPS(int[] lps) {
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < pattern.length(); i++) {
                if (lps[i] != -1 && pattern.charAt(i) == pattern.charAt(lps[i])) {
                    if (lps[lps[i]] != -1) {
                        lps[i] = lps[lps[i]];
                        changed = true;
                    } else {
                        lps[i] = -1;
                        changed = true;
                    }
                }
            }
        } while (changed);
    }

    public List<Pair> search(String text) {

        List<Pair> matches = new ArrayList<>();

        int i = 0, j = 0;

        while (i < text.length()) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++;
                j++;

                if (j == pattern.length()) {
                    matches.add(new Pair(i - j, i - 1));
                    j = 0;
                }
            } else if (j != 0) {
                j = carryOver[j];
                if (j == -1) {
                    j = 0;
                    i++;
                }
            } else {
                i++;
            }

        }

        return matches; // No match found
    }
}