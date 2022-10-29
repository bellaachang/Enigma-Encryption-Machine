package enigma;

import java.util.HashMap;
import java.util.Map;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Bella Chang
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _derangement = true;
        _forwardMap = new HashMap<>();
        _backwardMap = new HashMap<>();

        for (String cycle : cycles.split(" ")) {
            if (cycle.length() > 0) {
                addCycle(cycle);
            }
        }
        for (int i = 0; i < alphabet.size(); i++) {
            char c = alphabet.toChar(i);
            if (!_forwardMap.containsKey(c)) {
                _derangement = false;
                _forwardMap.put(c, c);
                _backwardMap.put(c, c);
            }
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        if (cycle.length() < 2 || cycle.charAt(0) != '('
                || cycle.charAt(cycle.length() - 1) != ')') {
            throw error("Bad cycle format");
        }

        char curr;
        char next;
        for (int i = 1; i < cycle.length() - 1; i++) {
            curr = cycle.charAt(i);
            if (i + 1 < cycle.length() - 1) {
                next = cycle.charAt(i + 1);
            } else {
                next = cycle.charAt(1);
            }
            _forwardMap.put(curr, next);
            _backwardMap.put(next, curr);
        }
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
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        return _alphabet.toInt(_forwardMap.get(_alphabet.toChar(wrap(p))));
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        return _alphabet.toInt(_backwardMap.get(_alphabet.toChar(wrap(c))));
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        return _forwardMap.get(p);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        return _backwardMap.get(c);
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        return _derangement;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Maps for our translation/permutation system forwards. */
    private Map<Character, Character> _forwardMap;

    /** Map for our permutation system backwards. */
    private Map<Character, Character> _backwardMap;

    /** Derangement instance variable. */
    private boolean _derangement;
}
