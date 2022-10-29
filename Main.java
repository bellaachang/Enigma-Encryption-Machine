package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Bella Chang
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine machine = readConfig();
        setUp(machine, _input.nextLine());

        while (_input.hasNextLine()) {
            String message = _input.nextLine();
            if (!message.contains("*")) {
                setUp(machine, message);
            } else {
                printMessageLine(machine.convert(message));
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.next());
            int numRotors = _config.nextInt();
            int numPawls = _config.nextInt();
            ArrayList<Rotor> allRotors = new ArrayList<>();

            while (_config.hasNext()) {
                allRotors.add(readRotor());
            }

            return new Machine(_alphabet, numRotors, numPawls,
                    allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String rotorName = "";
            if (_config.hasNext()) {
                rotorName = _config.next();
            }

            String typeAndNotches = "";
            if (_config.hasNext()) {
                typeAndNotches = _config.next();
            }
            String cycles = "";
            String notches = "";
            if (_config.hasNext()) {
                while (_config.hasNext("(\\((\\S)+\\))+")) {
                    cycles += (_config.next() + " ");
                }
            }

            Permutation rotorPerm = new Permutation(cycles, _alphabet);

            if (typeAndNotches.charAt(0) == 'M') {
                for (int i = 1; i < typeAndNotches.length(); i++) {
                    notches += String.valueOf(typeAndNotches.charAt(i));
                }
                return new MovingRotor(rotorName, rotorPerm, notches);
            } else if (typeAndNotches.charAt(0) == 'N') {
                return new FixedRotor(rotorName, rotorPerm);
            } else if (typeAndNotches.charAt(0) == 'R') {
                return new Reflector(rotorName, rotorPerm);
            } else {
                throw error("Rotor must be of type MovingRotor"
                        + ", FixedRotor or Reflector.");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {

        if (settings.charAt(0) == '*') {
            Scanner scanSettings = new Scanner(settings);
            String[] desiredRotors = new String[M.numRotors()];

            scanSettings.next();

            for (int i = 0; i < M.numRotors(); i++) {
                String nextRotorName = scanSettings.next();
                desiredRotors[i] = nextRotorName;
            }

            M.insertRotors(desiredRotors);

            if (!scanSettings.hasNext("[A-Za-z\\d]+")) {
                throw error("Bad character in wheel settings.");
            }
            String setRotors = scanSettings.next();
            if (setRotors.length() != desiredRotors.length - 1) {
                throw error("Wheel settings wrong size.");
            }

            if (scanSettings.hasNext("[A-Za-z\\d]+")) {
                M.setRing(scanSettings.next());
            }
            M.setRotors(setRotors);

            String plugboardSettings = "";

            while (scanSettings.hasNext("(\\((\\S)+\\))+")) {
                plugboardSettings = plugboardSettings + scanSettings.next();
            }

            M.setPlugboard(new Permutation(plugboardSettings, _alphabet));
        } else {
            throw error("Not a valid setting.");
        }
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        String messageWithoutSpaces = msg.replaceAll(" ", "");
        String finalMessage = "";

        int messageIndex = 0;
        while (messageIndex < messageWithoutSpaces.length()) {
            finalMessage = finalMessage
                    + messageWithoutSpaces.charAt(messageIndex);
            if ((messageIndex + 1) % 5 == 0) {
                finalMessage += " ";
            }
            messageIndex += 1;
        }

        _output.print(finalMessage);
        _output.println();
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;
}
