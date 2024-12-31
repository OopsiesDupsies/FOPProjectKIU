package interpreter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // Keywords map for BASIC
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("LET", TokenType.LET);
        keywords.put("PRINT", TokenType.PRINT);
        keywords.put("INPUT", TokenType.INPUT);
        keywords.put("IF", TokenType.IF);
        keywords.put("THEN", TokenType.THEN);
        keywords.put("ELSE", TokenType.ELSE);
        keywords.put("ENDIF", TokenType.ENDIF);
        keywords.put("WHILE", TokenType.WHILE);
        keywords.put("WEND", TokenType.WEND);
        keywords.put("END", TokenType.END);
        keywords.put("REM", TokenType.REM);
        keywords.put("GOTO", TokenType.GOTO);
    }

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // Single character tokens
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;
            case '*': addToken(TokenType.MULTIPLY); break;
            case '/': addToken(TokenType.DIVIDE); break;
            case '%': addToken(TokenType.MOD); break;
            case '=': addToken(TokenType.EQUALS); break;

            // Two character tokens
            case '<':
                if (match('=')) addToken(TokenType.LESS_EQUAL);
                else if (match('>')) addToken(TokenType.NOT_EQUALS);
                else addToken(TokenType.LESS);
                break;
            case '>':
                if (match('=')) addToken(TokenType.GREATER_EQUAL);
                else addToken(TokenType.GREATER);
                break;

            // Ignore whitespace
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;

            // String literals
            case '"': string(); break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    throw new RuntimeException("Unexpected character at line " + line);
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text.toUpperCase());
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for decimal point
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // Consume the '.'
            while (isDigit(peek())) advance();
        }

        addToken(TokenType.NUMBER,
                Double.valueOf(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            throw new RuntimeException("Unterminated string at line " + line);
        }

        advance(); // Closing "

        // Trim the surrounding quotes
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}