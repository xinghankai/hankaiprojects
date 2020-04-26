package enigma;

import java.util.ArrayList;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Hankai Xing
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = new ArrayList<>(allRotors);
        _rotors = new Rotor[this.numRotors()];
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        if (rotors.length != numRotors()) {
            throw new EnigmaException("Wrong Rotor size");
        }
        for (int i = 0; i < rotors.length; i++) {
            boolean check = true;
            for (Rotor allRotor : _allRotors) {
                if (rotors[i].equals(allRotor.name())) {
                    _rotors[i] = allRotor;
                    check = false;
                }
            }

            if (i == 0 && !_rotors[i].reflecting()) {
                throw new EnigmaException("not a refelctor!");
            }
            if (check) {
                throw new EnigmaException("wrong rotor");
            }
            for (int j = 0; j < i; j++) {
                if (_rotors[i].name().equals(_rotors[j].name())) {
                    throw new EnigmaException("repeated name");
                }
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != numRotors() - 1) {
            throw
                    new EnigmaException("length must be numRotors - 1");
        }
        for (int i = 1; i < numRotors(); i++) {
            if (!_alphabet.contains(setting.charAt(i - 1))) {
                throw
                        new EnigmaException("Characters isn't in my alphabet");
            }
            _rotors[i].set(setting.charAt(i - 1));
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        boolean[] ifAdvance = new boolean[numRotors()];
        ifAdvance[0] = false;
        ifAdvance[ifAdvance.length - 1] = true;
        for (int i = 1; i < ifAdvance.length; i++) {
            if (i != _rotors.length - 1
                    && ((_rotors[i]).atNotch() && _rotors[i - 1].rotates()
                    || _rotors[i + 1].atNotch())) {
                ifAdvance[i] = true;
            }
        }
        for (int i = 0; i < ifAdvance.length; i++) {
            if (ifAdvance[i]) {
                _rotors[i].advance();
            }
        }
        if (_plugboard != null) {
            c = _plugboard.permute(c);
        }
        for (int i = _rotors.length - 1; i > 0; i--) {
            c = _rotors[i].convertForward(c);
        }
        for (int i = 0; i < _rotors.length; i++) {
            c = _rotors[i].convertBackward(c);
        }
        if (_plugboard != null) {
            c = _plugboard.permute(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        StringBuilder message = new StringBuilder();
        msg = msg.trim();
        msg = msg.replaceAll(" ", "");
        for (int i = 0; i < msg.length(); i++) {
            char converted =
                    _alphabet.toChar(convert(_alphabet.toInt(msg.charAt(i))));
            message.append(converted);
        }
        return message.toString();
    }

    /** Getter method of my rotors array.
     * @return _rotors*/
    Rotor[] getRotors() {
        return _rotors;
    }

    /** Common alphabet of my rotors.*/
    private final Alphabet _alphabet;

    /** the number of rotors this machine has.*/
    private final int _numRotors;

    /** the number of pawls, or aka the number of moving rotors,
     *  this machine has.*/
    private final int _pawls;

    /** the ArrayList of all rotors.*/
    private ArrayList<Rotor> _allRotors;

    /** the array of my rotors.*/
    private Rotor[] _rotors;

    /** my plugboard(s).*/
    private Permutation _plugboard;
}
