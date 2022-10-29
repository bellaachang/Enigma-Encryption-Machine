package enigma;

import java.util.HashMap;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Bella Chang
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        if (_numRotors <= 0) {
            throw error("Must have at least 1 rotor slot.");
        }

        _pawls = pawls;
        if (pawls < 0 || pawls >= _numRotors) {
            throw error("Pawls must be between 0 (inclusive)"
                    + "and numRotors (exclusive).");
        }

        _rotorMap = new HashMap<>();
        for (Rotor r : allRotors) {
            _rotorMap.put(r.name(), r);
        }

        _rotors = new Rotor[_numRotors];

        _plugboard = new Permutation("", alpha);
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _rotors[k];
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        if (rotors.length != numRotors()) {
            throw error("Settings line must specify %d rotors", numRotors());
        }

        _rotors = new Rotor[numRotors()];

        for (int i = 0; i < rotors.length; i++) {
            _rotors[i] = _rotorMap.get(rotors[i]);
            if (_rotors[i] == null) {
                throw error("Unknown rotor %d", rotors[i]);
            }
            _rotors[i].set(0);
        }

        if (!_rotors[0].reflecting()) {
            throw error("First rotor must be a reflector");
        }

        for (int i = _rotors.length - _pawls; i < _rotors.length; i++) {
            if (!_rotors[i].rotates()) {
                throw error("Rotor %d must rotate", i);
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != _rotors.length - 1) {
            throw error("Settings line must have %d characters",
                    _rotors.length - 1);
        }

        for (int i = 1; i < _rotors.length; i++) {
            _rotors[i].set(setting.charAt(i - 1));
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        for (int i = 0; i < plugboard.size(); i++) {
            if (plugboard.permute(i) != i
                    && plugboard.permute(plugboard.permute(i)) != i) {
                throw error("Plugboard has cycles longer than 2");
            }
        }
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }

        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        if (_rotors[0] == null) {
            throw error("No rotors in machine");
        }

        boolean[] advance = new boolean[numRotors()];
        advance[_rotors.length - 1] = true;

        for (int i = 1; i < _rotors.length - 1; i++) {
            if (_rotors[i + 1].atNotch()) {
                advance[i] = true;
            }
        }

        for (int i = 2; i < _rotors.length - 1; i++) {
            if (_rotors[i].atNotch() && _rotors[i - 1].rotates()) {
                advance[i] = true;
            }
        }

        for (int i = 0; i < _rotors.length; i++) {
            if (advance[i]) {
                _rotors[i].advance();
            }
        }
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        for (int i = _rotors.length - 1; i >= 0; i--) {
            c = _rotors[i].convertForward(c);
        }
        for (int i = 1; i < _rotors.length; i++) {
            c = _rotors[i].convertBackward(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String result = "";
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (_alphabet.contains(c)) {
                result = result + _alphabet.toChar(convert(_alphabet.toInt(c)));
            } else {
                throw error("Message character %c not in alphabet", c);
            }
        }
        return result;
    }

    /** Ringstellung set rings.
     * @param settings sets my rotors;
     * */
    void setRing(String settings) {
        for (int i = 0; i < settings.length(); i++) {
            _rotors[i + 1].setAlphaRing(settings.charAt(i));
        }
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of my rotors. */
    private int _numRotors;

    /** Number of my pawls. */
    private int _pawls;

    /** Collection of all rotors available to us. */
    private HashMap<String, Rotor> _rotorMap;

    /** Rotors in order as they go into the machine. */
    private Rotor[] _rotors;

    /** Plugboard that we will be using for our machine. */
    private Permutation _plugboard;
}
