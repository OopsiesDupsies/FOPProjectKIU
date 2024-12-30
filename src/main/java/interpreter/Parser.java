package interpreter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    static Map<String, Object> symbolTable = new HashMap<>();
    private Map<Integer, Integer> lineNumbers = new HashMap<>();

    public Parser(List<Token> tokens, Map<Integer, Integer> lineNumbers) {
        this.tokens = tokens;
        this.lineNumbers = lineNumbers;
    }

    // Method to parse the whole program
    public int parse() {
        while (!isAtEnd()) {
            Token token = advance();  // Get the first token of the line

            if (token.type == TokenType.LET) {
                parseLetStatement();
            } else if (token.type == TokenType.PRINT) {
                parsePrintStatement();
            } else if (token.type == TokenType.IF) {
                return parseIfStatement();
            } else if (token.type == TokenType.GOTO) {
                return parseGotoStatement();
            }
        }

        return -1;
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

    // Helper methods
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