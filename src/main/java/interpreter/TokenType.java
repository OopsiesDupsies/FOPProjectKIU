package interpreter;
public enum TokenType {
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN,
    PLUS, MINUS, MULTIPLY, DIVIDE, MOD,
    EQUALS,

    // Two-character tokens
    LESS, LESS_EQUAL,
    GREATER, GREATER_EQUAL,
    NOT_EQUALS,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    LET, PRINT, INPUT,
    IF, THEN, ELSE, ENDIF, GOTO,
    WHILE, WEND,
    END, REM,

    // Special
    EOF
}