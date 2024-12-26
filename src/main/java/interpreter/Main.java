package interpreter;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Interpreter interpreter = new Interpreter();

        System.out.println("Welcome to BASIC Interpreter (With Save/Load). Type END to exit.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("END")) {
                System.out.println("Exiting...");
                break;
            }

            if (input.equalsIgnoreCase("RUN")) {
                System.out.println("run the interpreter");
            }
        }

        scanner.close();
    }
}
