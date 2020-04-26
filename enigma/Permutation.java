package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Hankai Xing
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        String tmp = cycles.trim();
        tmp = tmp.replaceAll("\\(", "");
        tmp = tmp.replaceAll(" ", "");
        _cycles = tmp.split("\\)");
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        String[] arr = new String[_cycles.length + 1];
        int k = 0;
        while (k < arr.length) {
            arr[k] = _cycles[k];
            k++;
        }
        arr[_cycles.length + 1] = cycle;
        _cycles = arr;
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return alphabet().size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char newChar = _alphabet.toChar(wrap(p));
        for (String cycle : _cycles) {
            for (int i = 0; i < cycle.length(); i++) {
                if (newChar == cycle.charAt(i)) {
                    if (i == cycle.length() - 1) {
                        newChar = cycle.charAt(0);
                    } else {
                        newChar = cycle.charAt(i + 1);
                    }
                    break;
                }
            }
        }
        int newInt = _alphabet.toInt(newChar);
        return newInt % size();
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char newChar = _alphabet.toChar(wrap(c));
        for (String cycle: _cycles) {
            for (int i = 0; i < cycle.length(); i++) {
                if (newChar == cycle.charAt(i)) {
                    if (i == 0) {
                        newChar = cycle.charAt(cycle.length() - 1);
                    } else {
                        newChar = cycle.charAt(i - 1);
                    }
                    break;
                }
            }
        }
        int newInt = _alphabet.toInt(newChar);
        return newInt % size();
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        return _alphabet.toChar(permute(_alphabet.toInt(p)));
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        return _alphabet.toChar(invert(_alphabet.toInt(c)));
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        int count = 0;
        for (String cycle: _cycles) {
            for (int i = 0; i < cycle.length(); i++) {
                if (cycle.length() == 1) {
                    return false;
                }
            }
            count += cycle.length();
        }
        if (count < size()) {
            return false;
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** String array that contains information of this permutation's cycles. */
    private String[] _cycles;

}
