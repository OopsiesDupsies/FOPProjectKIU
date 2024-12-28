package interpreter;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
/**
 * Interpreter class to handle saving, loading, and running BASIC programs.
 */
public class Interpreter {

    private static final String SAVES_DIRECTORY = "saves";

    /**
     * Save the current program lines to a file.
     *
     * @param programLines The program lines to save.
     */
    public void saveProgram(TreeMap<Integer, String> programLines) {
        if (programLines.isEmpty()) {
            System.out.println("BASIC> NO PROGRAM LINES TO SAVE.");
            return;
        }

        // Ensure the saves directory exists
        File savesFolder = new File(SAVES_DIRECTORY);
        if (!savesFolder.exists()) {
            savesFolder.mkdir();
        }

        // Ask the user for a filename
        System.out.println("BASIC> ENTER A NAME TO SAVE THE PROGRAM:");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String filename = reader.readLine().trim();
            if (filename.isEmpty()) {
                System.out.println("BASIC> INVALID NAME.");
                return;
            }

            // Save to file
            File saveFile = new File(SAVES_DIRECTORY, filename + ".txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
                for (Map.Entry<Integer, String> entry : programLines.entrySet()) {
                    writer.write(entry.getKey() + " " + entry.getValue());
                    writer.newLine();
                }
                System.out.println("BASIC> PROGRAM SAVED AS " + filename + ".txt");
            } catch (IOException e) {
                System.out.println("BASIC> ERROR WHILE SAVING PROGRAM.");
            }
        } catch (IOException e) {
            System.out.println("BASIC> ERROR READING INPUT.");
        }
    }

    /**
     * Load a program from a file.
     *
     * @param programFile The file to load the program from.
     * @return The loaded program lines.
     */
    public TreeMap<Integer, String> loadProgram(File programFile) {
        TreeMap<Integer, String> loadedLines = new TreeMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(programFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int spaceIndex = line.indexOf(" ");
                if (spaceIndex != -1) {
                    int lineNumber = Integer.parseInt(line.substring(0, spaceIndex));
                    String code = line.substring(spaceIndex + 1).trim();
                    loadedLines.put(lineNumber, code);
                }
            }
            System.out.println("BASIC> PROGRAM LOADED SUCCESSFULLY.");
        } catch (IOException | NumberFormatException e) {
            System.out.println("BASIC> ERROR WHILE LOADING PROGRAM.");
        }
        return loadedLines;
    }

    /**
     * Run the program lines in sequence.
     *
     * @param programLines The program lines to execute.
     */
    public void runProgram(TreeMap<Integer, String> programLines) {
        if (programLines.isEmpty()) {
            System.out.println("BASIC> NO PROGRAM TO RUN.");
            return;
        }

        System.out.println("BASIC> RUNNING PROGRAM...");

        int programCounter = 0; // Simulates the Program Counter (PC)
        for (Map.Entry<Integer, String> entry : programLines.entrySet()) {
            programCounter = entry.getKey(); // The current "line number"
            String code = entry.getValue();

            // For simplicity, just print the lines being executed
            System.out.println("Executing line " + programCounter + ": " + code);

            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.scanTokens(); // Get the tokens from the lexer
            Parser parser = new Parser(tokens);    // Pass the tokens to the parser
            parser.parse();                       // Parse the tokens
        }

        System.out.println("BASIC> PROGRAM EXECUTION COMPLETE.");
    }
}
