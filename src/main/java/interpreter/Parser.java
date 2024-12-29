package interpreter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    static Map<String, Object> symbolTable = new HashMap<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Method to parse the whole program
    public void parse() {
        while (!isAtEnd()) {
            Token token = advance();  // Get the first token of the line

            if (token.type == TokenType.LET) {
                parseLetStatement();
            } else if (token.type == TokenType.PRINT) {
                parsePrintStatement();
            } else {
                // Handle other types of statements
            }
        }
    }

    // Parse a LET statement (variable assignment)
    private void parseLetStatement() {
        Token identifier = consume(TokenType.IDENTIFIER, "Expect variable name after LET.");
        consume(TokenType.EQUALS, "Expect '=' after variable name.");
        Token value = consume(TokenType.NUMBER, "Expect value after '='.");

        // Store the variable in the symbol table
        symbolTable.put(identifier.lexeme, value.literal);

        System.out.println("LET statement: " + identifier.lexeme + " = " + value.lexeme);
    }

    // Parse a PRINT statement
    private void parsePrintStatement() {
        Token expression = advance();  // Move to the next token (expected to be expression)

        if (expression.type == TokenType.STRING) {
            // If it's a string, print it
            System.out.println(expression.lexeme);
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

    // Parse arithmetic expressions
    private Object parseExpression() {
        return parseTerm();
    }

    // Parse terms (handles *, /, %)
    private Object parseTerm() {
        Object expr = parseFactor();

        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MOD)) {
            Token operator = previous();
            Object right = parseFactor();
            expr = evaluateBinary(expr, operator, right);
        }

        return expr;
    }

    // Parse factors (handles +, -)
    private Object parseFactor() {
        Object expr = parsePrimary();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Object right = parsePrimary();
            expr = evaluateBinary(expr, operator, right);
        }

        return expr;
    }

    // Parse primary expressions (numbers, variables, parentheses)
    private Object parsePrimary() {
        if (match(TokenType.NUMBER)) {
            return Double.parseDouble(previous().lexeme);
        }
        if (match(TokenType.IDENTIFIER)) {
            String varName = previous().lexeme;
            Object value = symbolTable.get(varName);
            if (value == null) {
                throw new RuntimeException("Undefined variable: " + varName);
            }
            return value;
        }
        if (match(TokenType.LEFT_PAREN)) {
            Object expr = parseExpression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return expr;
        }

        throw new RuntimeException("Expect expression.");
    }

    // Evaluate binary operations
    private Object evaluateBinary(Object left, Token operator, Object right) {
        double leftNum = (double) left;
        double rightNum = (double) right;

        switch (operator.type) {
            case PLUS: return leftNum + rightNum;
            case MINUS: return leftNum - rightNum;
            case MULTIPLY: return leftNum * rightNum;
            case DIVIDE:
                if (rightNum == 0) throw new RuntimeException("Division by zero.");
                return leftNum / rightNum;
            case MOD: return leftNum % rightNum;
            default: throw new RuntimeException("Unknown operator.");
        }
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();  // Move to the next token if there's a match
                return true;
            }
        }
        return false;
    }
    // Parse conditionals
    private boolean parseConditional() {
        Object left = parseExpression();
        Token operator = advance();
        Object right = parseExpression();

        return evaluateConditional(left, operator, right);
    }

    // Evaluate conditionals
    private boolean evaluateConditional(Object left, Token operator, Object right) {
        double leftNum = (double) left;
        double rightNum = (double) right;

        switch (operator.type) {
            case LESS: return leftNum < rightNum;
            case LESS_EQUAL: return leftNum <= rightNum;
            case GREATER: return leftNum > rightNum;
            case GREATER_EQUAL: return leftNum >= rightNum;
            case NOT_EQUALS: return leftNum != rightNum;
            default: throw new RuntimeException("Unknown operator.");
        }
    }

    // Parse WHILE loop
    private void parseWhileLoop() {
        consume(TokenType.WHILE, "Expect 'WHILE' keyword.");
        int loopStart = current;  // Save the loop's starting point

        while (parseConditional()) {
            parse();  // Execute the body of the loop

            // Reset to the start of the loop for the next iteration
            current = loopStart;
        }

        consume(TokenType.WEND, "Expect 'WEND' keyword to close loop.");
    }





}