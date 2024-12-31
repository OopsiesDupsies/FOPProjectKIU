package interpreter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static interpreter.Main.programLines;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    static Map<String, Object> symbolTable = new HashMap<>();
    private Map<Integer, Integer> lineNumbers = new HashMap<>();
    // Fields for WHILE loop support
    private static boolean inWhileLoop = false;
    private static int whileLineNumber = -1;
    private final int currentLine;


    public Parser(List<Token> tokens, Map<Integer, Integer> lineNumbers, int currentLine) {
        this.tokens = tokens;
        this.lineNumbers = lineNumbers;
        this.currentLine = currentLine;
    }

    // Method to parse the whole program
    public int parse() {
        while (!isAtEnd()) {
            Token token = advance();

            switch (token.type) {
                case LET:
                    parseLetStatement();
                    break;
                case PRINT:
                    parsePrintStatement();
                    break;
                case IF:
                    return parseIfStatement();
                case GOTO:
                    return parseGotoStatement();
                case WHILE:
                    return parseWhileStatement();
                case WEND:
                    return handleWend();
                case IDENTIFIER:
                    // Handle direct assignment without LET
                    parseAssignment();
                    break;
                default:
                    throw new RuntimeException("Unexpected token: " + token.type);
            }

            // Check while loop condition if we're in one
            if (inWhileLoop && current >= tokens.size()) {
                return evaluateWhileLoop();
            }
        }

        return -1;
    }

    // Parse a LET statement (variable assignment)
    private void parseLetStatement() {
        Token identifier = consume(TokenType.IDENTIFIER, "Expect variable name after LET.");
        consume(TokenType.EQUALS, "Expect '=' after variable name.");
        // Get the first value (either number or variable)
        double result = parseValue();

        // Check if there are more tokens (arithmetic operations)
        while (!isAtEnd() && isArithmeticOperator(peek().type)) {
            Token operator = advance();
            double rightOperand = parseValue();
            // Perform the arithmetic operation
            switch (operator.type) {
                case PLUS:
                    result += rightOperand;
                    break;
                case MINUS:
                    result -= rightOperand;
                    break;
                case MULTIPLY:
                    result *= rightOperand;
                    break;
                case DIVIDE:
                    if (rightOperand == 0) {
                        throw new RuntimeException("Division by zero.");
                    }
                    result /= rightOperand;
                    break;
                case MOD:
                    if (rightOperand == 0) {
                        throw new RuntimeException("Modulo by zero.");
                    }
                    result %= rightOperand;
                    break;
            }
        }

        // Store the final result in the symbol table
        symbolTable.put(identifier.lexeme, result);
        System.out.println("LET statement: " + identifier.lexeme + " = " + result);
    }

    // Parse a PRINT statement
    private void parsePrintStatement() {
        Token expression = advance();  // Move to the next token (expected to be expression)

        if (expression.type == TokenType.STRING) {
            // If it's a string, print it
            System.out.println(expression.lexeme.substring(1, expression.lexeme.length() - 1));
        } else if (expression.type == TokenType.IDENTIFIER) {
            // If it's an identifier (variable), print its value from the symbol table
            Object value = symbolTable.get(expression.lexeme);
            if (value != null) {
                System.out.println(value);
            } else {
                System.out.println("Undefined variable: " + expression.lexeme);
            }
        } else {
            throw new RuntimeException("Expect expression to print (string or variable).");
        }
    }

    private int parseIfStatement() {
        double left = parseValue();

        // Get comparison operator
        Token operator = advance();
        if (!isComparisonOperator(operator.type)) {
            throw new RuntimeException("Expected comparison operator.");
        }

        double right = parseValue();

        // Evaluate condition
        boolean condition = evaluateCondition(left, operator.type, right);

        consume(TokenType.THEN, "Expected THEN after condition.");
        Token gotoLine = advance();
        int targetLine = Integer.parseInt(gotoLine.lexeme);

        if (condition) {
            return targetLine;
        }

        return -1;
    }

    private int parseGotoStatement() {
        Token lineToken = advance();
        if (lineToken.type != TokenType.NUMBER) {
            throw new RuntimeException("Expected line number after GOTO");
        }

        int targetLine = ((Double) lineToken.literal).intValue();
        if (!lineNumbers.containsKey(targetLine)) {
            throw new RuntimeException("Invalid GOTO line number: " + targetLine);
        }

        return targetLine;
    }

    // Add WHILE loop support
    private int parseWhileStatement() {
        // Only set inWhileLoop if we're not already in the loop
        if (!inWhileLoop) {
            whileLineNumber = getCurrentLineNumber();
            inWhileLoop = true;
        }

        // Parse condition
        double left = parseValue();
        Token operator = advance();
        if (!isComparisonOperator(operator.type)) {
            throw new RuntimeException("Expected comparison operator in WHILE statement");
        }
        double right = parseValue();

        // Evaluate condition
        boolean condition = evaluateCondition(left, operator.type, right);
        if (!condition) {
            // Reset while loop state
            inWhileLoop = false;
            whileLineNumber = -1;
            // Find and skip to after WEND
            int wendLine = findWendLine();
            Integer nextLine = programLines.higherKey(wendLine);
            return (nextLine != null) ? nextLine : -1;
        }

        return -1; // Continue to next line
    }

    private int handleWend() {
        if (!inWhileLoop) {
            throw new RuntimeException("WEND without WHILE");
        }
        return whileLineNumber; // Return to WHILE line for reevaluation
    }

    // Method to handle variable assignment without LET keyword
    private void parseAssignment() {
        Token identifier = previous(); // The identifier was already consumed
        consume(TokenType.EQUALS, "Expect '=' after variable name.");
        double result = parseValue();

        // Handle arithmetic operations
        while (!isAtEnd() && isArithmeticOperator(peek().type)) {
            Token operator = advance();
            double rightOperand = parseValue();

            switch (operator.type) {
                case PLUS:
                    result += rightOperand;
                    break;
                case MINUS:
                    result -= rightOperand;
                    break;
                case MULTIPLY:
                    result *= rightOperand;
                    break;
                case DIVIDE:
                    if (rightOperand == 0) {
                        throw new RuntimeException("Division by zero.");
                    }
                    result /= rightOperand;
                    break;
                case MOD:
                    if (rightOperand == 0) {
                        throw new RuntimeException("Modulo by zero.");
                    }
                    result %= rightOperand;
                    break;
            }
        }

        symbolTable.put(identifier.lexeme, result);
        System.out.println("Assignment: " + identifier.lexeme + " = " + result);
    }

    // Helper methods
    private int evaluateWhileLoop() {
        // Re-evaluate the while condition
        current = 0; // Reset position to start of tokens
        Token token = advance();
        if (token.type != TokenType.WHILE) {
            throw new RuntimeException("Expected WHILE statement");
        }

        double left = parseValue();
        Token operator = advance();
        double right = parseValue();

        boolean condition = evaluateCondition(left, operator.type, right);
        if (!condition) {
            inWhileLoop = false;
            whileLineNumber = -1;
            return findWendLine();
        }

        return -1; // Continue execution
    }

    private int findWendLine() {
        // Get the next line after current WHILE
        Integer nextLine = getCurrentLineNumber();

        // Search through program lines until we find WEND
        while (nextLine != null) {
            String code = programLines.get(nextLine);
            Lexer tempLexer = new Lexer(code);
            List<Token> tempTokens = tempLexer.scanTokens();

            if (!tempTokens.isEmpty() && tempTokens.get(0).type == TokenType.WEND) {
                return nextLine;
            }

            nextLine = programLines.higherKey(nextLine);
        }
        throw new RuntimeException("WEND not found for WHILE statement");
    }

    private int getCurrentLineNumber() {
        // Get current line number based on position in program
        for (Map.Entry<Integer, Integer> entry : lineNumbers.entrySet()) {
            if (entry.getValue() == currentLine) {
                return entry.getKey();
            }
        }
        return -1;
    }

    private double parseValue() {
        Token token = advance();
        if (token.type == TokenType.NUMBER) {
            return (double) token.literal;
        } else if (token.type == TokenType.IDENTIFIER) {
            Object value = symbolTable.get(token.lexeme);
            if (value == null) {
                throw new RuntimeException("Undefined variable: " + token.lexeme);
            }
            return (double) value;
        } else if (token.type == TokenType.LEFT_PAREN) {
            double result = parseParenthesizedExpression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return result;
        }
        throw new RuntimeException("Expect number or variable.");
    }

    private double parseParenthesizedExpression() {
        double result = parseValue();

        while (!isAtEnd() && peek().type != TokenType.RIGHT_PAREN && isArithmeticOperator(peek().type)) {
            Token operator = advance();
            double rightOperand = parseValue();

            switch (operator.type) {
                case PLUS:
                    result += rightOperand;
                    break;
                case MINUS:
                    result -= rightOperand;
                    break;
                case MULTIPLY:
                    result *= rightOperand;
                    break;
                case DIVIDE:
                    if (rightOperand == 0) {
                        throw new RuntimeException("Division by zero.");
                    }
                    result /= rightOperand;
                    break;
                case MOD:
                    if (rightOperand == 0) {
                        throw new RuntimeException("Modulo by zero.");
                    }
                    result %= rightOperand;
                    break;
            }
        }

        return result;
    }

    private boolean isArithmeticOperator(TokenType type) {
        return type == TokenType.PLUS || type == TokenType.MINUS ||
                type == TokenType.MULTIPLY || type == TokenType.DIVIDE ||
                type == TokenType.MOD;
    }

    private boolean isComparisonOperator(TokenType type) {
        return type == TokenType.LESS || type == TokenType.LESS_EQUAL ||
                type == TokenType.GREATER || type == TokenType.GREATER_EQUAL ||
                type == TokenType.EQUALS || type == TokenType.NOT_EQUALS;
    }

    private boolean evaluateCondition(double left, TokenType operator, double right) {
        switch (operator) {
            case LESS: return left < right;
            case LESS_EQUAL: return left <= right;
            case GREATER: return left > right;
            case GREATER_EQUAL: return left >= right;
            case EQUALS: return left == right;
            case NOT_EQUALS: return left != right;
            default: throw new RuntimeException("Invalid comparison operator.");
        }
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw new RuntimeException(message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}