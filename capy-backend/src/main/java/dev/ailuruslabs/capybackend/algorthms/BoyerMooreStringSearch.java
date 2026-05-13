package dev.ailuruslabs.capybackend.algorthms;

import java.util.HashMap;

public class BoyerMooreStringSearch {
    private final String pattern;
    private final HashMap<Character, Integer> badCharTable;
    private final int[] shiftArray;

    public BoyerMooreStringSearch(String pattern) {
        this.pattern = pattern;

        this.badCharTable = new HashMap<>();
        this.shiftArray = new int[pattern.length() + 1];

        preprocessBadCharacter();
        preprocessGoodSuffix();
    }

    private void preprocessBadCharacter() {
        for (int i = 0; i < pattern.length(); i++) {
            badCharTable.put(pattern.charAt(i), i);
        }
    }

    private void preprocessGoodSuffix() {
        int[] bpos = new int[pattern.length() + 1]; // Temporary array to hold border positions

        int m = pattern.length();
        int i = m;
        int j = m + 1;

        bpos[i] = j;

        while (i > 0) {
            while (j <= m && pattern.charAt(i - 1) != pattern.charAt(j - 1)) {
                if (shiftArray[j] == 0) {
                    shiftArray[j] = j - i;
                }
                j = bpos[j];
            }

            i--;
            j--;
            bpos[i] = j;
        }

        j = bpos[0];

        for (i = 0; i <= m; i++) {
            if (shiftArray[i] == 0) {
                shiftArray[i] = j;
            }

            if (i == j) {
                j = bpos[j];
            }
        }
    }

    public void search(String text) {
        int patternLength = pattern.length();
        int textLength = text.length();
        int shiftDistance = 0;

        while (shiftDistance <= (textLength - patternLength)) {
            int pos = patternLength - 1; // pos = pointer tracking our position inside the pattern

            // Scan right-to-left looking for a match
            while (pos >= 0 && pattern.charAt(pos) == text.charAt(shiftDistance + pos)) {
                pos--;
            }

            if (pos < 0) {
                System.out.println("Match found at index " + shiftDistance);

                // Shift to find the next possible occurrence in the text
                shiftDistance += shiftArray[0];
            } else {
                // A mismatch occurred
                char badCharInText = text.charAt(shiftDistance + pos);

                // If character isn't in map, lookup returns -1, shifting past the bad char
                int badCharShift = pos - badCharTable.getOrDefault(badCharInText, -1);

                // Lookup Good Suffix shift
                // We use pos + 1 because the shift array indices represent the length
                // of the suffix we SUCCESSFULLY matched, which sits to the right of p.
                int goodSuffixShift = shiftArray[pos + 1];

                // Take the maximum of the two rules, ensuring we always progress forward
                shiftDistance += Math.max(1, Math.max(badCharShift, goodSuffixShift));
            }
        }
    }
}
