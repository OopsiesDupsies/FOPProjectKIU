package interpreter;

import java.io.File;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Main class to handle user input and manage the BASIC interpreter.
 */
public class Main {
    static TreeMap<Integer, String> programLines = new TreeMap<>(); // Stores program lines with line numbers
    private static Interpreter interpreter = new Interpreter(); // Interpreter instance for executing commands
    private static final String SAVES_DIRECTORY = "saves"; // Directory for saving program files

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Scanner for user input

        // Welcome message
        System.out.println("*****************************************");
        System.out.println("*     WELCOME TO THE BASIC INTERPRETER  *");
        System.out.println("*  TYPE YOUR CODE OR COMMANDS TO BEGIN  *");
        System.out.println("*  AVAILABLE COMMANDS: LIST, RUN, SAVE, *");
        System.out.println("*  LOAD, DELETE, END                    *");
        System.out.println("*****************************************");

        while (true) {
            System.out.print("BASIC> "); // Prompt for input
            String input = scanner.nextLine().trim(); // Read user input and remove leading/trailing spaces

            // Handle commands
            if (input.equalsIgnoreCase("END")) {
                System.out.println("BASIC> Program ended. Goodbye!");
                break; // Exit the program
            } else if (input.equalsIgnoreCase("LIST")) {
                listProgram(); // Display program lines
            } else if (input.equalsIgnoreCase("RUN")) {
                interpreter.runProgram(programLines); // Execute the program
            } else if (input.equalsIgnoreCase("SAVE")) {
                interpreter.saveProgram(programLines); // Save the current program
            } else if (input.startsWith("LOAD")) {
                loadProgram(input); // Load a saved program
            } else if (input.equalsIgnoreCase("DELETE")) {
                deleteProgram(); // Delete a saved program
            } else {
                processInputLine(input); // Handle input as a program line
            }
        }

        scanner.close(); // Close the scanner to release resources
    }

    /**
     * Process a line of input with a line number and code.
     * Adds or removes the line in the program.
     */
    private static void processInputLine(String input) {
        try {
            // Split input into line number and code
            int spaceIndex = input.indexOf(" ");
            if (spaceIndex == -1) {
                System.out.println("BASIC> SYNTAX ERROR: Line number and code required.");
                return;
            }

            int lineNumber = Integer.parseInt(input.substring(0, spaceIndex)); // Parse line number
            String code = input.substring(spaceIndex + 1).trim(); // Extract the rest as code

            if (code.isEmpty()) {
                programLines.remove(lineNumber); // Delete the line if the code is empty
                System.out.println("BASIC> Line " + lineNumber + " deleted.");
            } else {
                programLines.put(lineNumber, code); // Add or update the line
                System.out.println("BASIC> Line " + lineNumber + " saved.");
            }
        } catch (NumberFormatException e) {
            System.out.println("BASIC> SYNTAX ERROR: Invalid line number.");
        }
    }

    /**
     * Display the current program lines in numerical order.
     */
    private static void listProgram() {
        if (programLines.isEmpty()) {
            System.out.println("BASIC> NO PROGRAM LINES TO LIST.");
        } else {
            System.out.println("BASIC> Current Program:");
            programLines.forEach((lineNumber, code) ->
                    System.out.println("  " + lineNumber + " " + code)
            );
        }
    }

    /**
     * Load a program from the saves directory.
     * Supports two modes:
     * - LOAD: Lists available saved programs.
     * - LOAD [name]: Loads a specific program by name.
     */
    private static void loadProgram(String input) {
        File savesFolder = new File(SAVES_DIRECTORY);

        // Check if the saves directory exists
        if (!savesFolder.exists() || !savesFolder.isDirectory()) {
            System.out.println("BASIC> NO SAVES DIRECTORY FOUND.");
            return;
        }

        // If just "LOAD", list available programs
        if (input.equalsIgnoreCase("LOAD")) {
            System.out.println("BASIC> Saved Programs:");
            File[] files = savesFolder.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null && files.length > 0) {
                for (File file : files) {
                    System.out.println("  " + file.getName().replace(".txt", ""));
                }
            } else {
                System.out.println("  NONE.");
            }
        } else {
            // LOAD [name]
            String[] parts = input.split(" ");
            if (parts.length != 2) {
                System.out.println("BASIC> USAGE: LOAD [name]");
                return;
            }

            String filename = parts[1] + ".txt"; // Append .txt to the provided name
            File programFile = new File(SAVES_DIRECTORY, filename);
            if (programFile.exists()) {
                programLines = interpreter.loadProgram(programFile); // Load the program into memory
                System.out.println("BASIC> Program loaded from " + filename);
            } else {
                System.out.println("BASIC> NO PROGRAM FOUND WITH NAME: " + parts[1]);
            }
        }
    }

    /**
     * Delete a saved program from the saves directory.
     */
    private static void deleteProgram() {
        File savesFolder = new File(SAVES_DIRECTORY);

        // Check if the saves directory exists
        if (!savesFolder.exists() || !savesFolder.isDirectory()) {
            System.out.println("BASIC> NO SAVES DIRECTORY FOUND.");
            return;
        }

        // List saved programs
        System.out.println("BASIC> Saved Programs:");
        File[] files = savesFolder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            System.out.println("  NONE.");
            return;
        }

        for (int i = 0; i < files.length; i++) {
            System.out.println("  " + (i + 1) + ". " + files[i].getName().replace(".txt", ""));
        }

        // Ask user to select a program to delete
        System.out.println("BASIC> ENTER THE NUMBER OF THE PROGRAM TO DELETE:");
        Scanner scanner = new Scanner(System.in);
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice < 1 || choice > files.length) {
                System.out.println("BASIC> INVALID CHOICE.");
                return;
            }

            if (files[choice - 1].delete()) {
                System.out.println("BASIC> PROGRAM DELETED.");
            } else {
                System.out.println("BASIC> FAILED TO DELETE THE PROGRAM.");
            }
        } catch (NumberFormatException e) {
            System.out.println("BASIC> INVALID INPUT.");
        }
    }
}