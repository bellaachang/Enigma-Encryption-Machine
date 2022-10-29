package enigma;
import static enigma.EnigmaException.*;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Bella Chang
 */
class Alphabet {

    /** A new alphabet containing CHARS. The K-th character has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        _chars = chars;
        if (chars.length() == 0) {
            throw error("Alphabet cannot be null.");
        }
        for (int i = 0; i < _chars.length() - 1; i++) {
            int testChar = _chars.charAt(i);
            for (int j = i + 1; j < _chars.length(); j++) {
                if (_chars.charAt(j) == testChar) {
                    throw error("No character may "
                            + "be duplicated in the alphabet.");
                }
            }
        }
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _chars.length();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        return _chars.contains(ch + "");
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return _chars.charAt(index % size());
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        if (!this.contains(ch)) {
            throw error("Character must be in the alphabet.");
        }
        return _chars.indexOf(ch);
    }

    /** Characters of the alphabet. */
    private String _chars;

}
