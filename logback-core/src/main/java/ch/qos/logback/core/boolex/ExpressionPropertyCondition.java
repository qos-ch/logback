/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 *  Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *     or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.boolex;

import ch.qos.logback.core.util.IntHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A property condition that evaluates boolean expressions based on property lookups.
 * It supports logical operators (NOT, AND, OR) and functions like isNull, isDefined,
 * propertyEquals, and propertyContains. Expressions are parsed using the Shunting-Yard
 * algorithm into Reverse Polish Notation (RPN) for evaluation.
 *
 * <p>Example expression: {@code isDefined("key1") && propertyEquals("key2", "value")}</p>
 *
 * <p>Properties are resolved via {@link PropertyConditionBase#property(String)}.</p>
 *
 * @since 1.5.24
 */
public class ExpressionPropertyCondition extends PropertyConditionBase {


    /**
     * A map that associates a string key with a function for evaluating boolean conditions.
     *
     * <p>This map defines the known functions. It can be overridden by subclasses to define
     * new functions.</p>
     *
     * <p>In the context of this class, a function is a function that takes a String
     * argument and returns a boolean.</p>
     */
    protected Map<String, Function<String, Boolean>> functionMap = new HashMap<>();


    /**
     * A map that associates a string key with a bi-function for evaluating boolean conditions.
     *
     * <p>This map defines the known bi-functions. It can be overridden by subclasses to define
     * new bi-functions.</p>
     *
     * <p>In the context of this class, a bi-function is a function that takes two String
     * arguments and returns a boolean.</p>
     */
    protected Map<String, BiFunction<String, String, Boolean>> biFunctionMap = new HashMap<>();

    private static final String IS_NULL_FUNCTION_KEY = "isNull";
    private static final String IS_DEFINEDP_FUNCTION_KEY = "isDefined";

    private static final String PROPERTY_EQUALS_FUNCTION_KEY = "propertyEquals";
    private static final String PROPERTY_CONTAINS_FUNCTION_KEY = "propertyContains";

    private static final char QUOTE = '"';
    private static final char COMMA = ',';
    private static final char LEFT_PAREN = '(';
    private static final char RIGHT_PAREN = ')';


    private static final char NOT_CHAR = '!';
    private static final char AMPERSAND_CHAR = '&';
    private static final char OR_CHAR = '|';

    enum Associativity {
        LEFT, RIGHT;
    }

    enum TokenType {
        NOT, AND, OR, FUNCTION, BI_FUNCTION, LEFT_PAREN, RIGHT_PAREN;

        boolean isLogicalOperator() {
            return this == NOT || this == AND || this == OR;
        }
    }


    static class Token {
        TokenType tokenType;
        String functionName;
        String param0;
        String param1;

        Token(TokenType tokenType) {
            this.tokenType = tokenType;

            switch (tokenType) {
                case LEFT_PAREN:
                case RIGHT_PAREN:
                case NOT:
                case AND:
                case OR:
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + tokenType);
            }
        }

        Token(TokenType tokenType, String functionName, String propertyKey, String value) {
            this.tokenType = tokenType;
            this.functionName = functionName;
            this.param0 = propertyKey;
            this.param1 = value;
        }

        public static Token valueOf(char c) {

            if (c == LEFT_PAREN)
                return new Token(TokenType.LEFT_PAREN);
            if (c == RIGHT_PAREN)
                return new Token(TokenType.RIGHT_PAREN);
            throw new IllegalArgumentException("Unexpected char: " + c);
        }
    }


    String expression;
    List<Token> rpn;

    /**
     * Constructs an ExpressionPropertyCondition and initializes the function maps
     * with supported unary and binary functions.
     */
    ExpressionPropertyCondition() {
        functionMap.put(IS_NULL_FUNCTION_KEY, this::isNull);
        functionMap.put(IS_DEFINEDP_FUNCTION_KEY, this::isDefined);
        biFunctionMap.put(PROPERTY_EQUALS_FUNCTION_KEY, this::propertyEquals);
        biFunctionMap.put(PROPERTY_CONTAINS_FUNCTION_KEY, this::propertyContains);
    }

    /**
     * Starts the condition by parsing the expression into tokens and converting
     * them to Reverse Polish Notation (RPN) for evaluation.
     *
     * <p>In case of malformed expression, the instance will not enter the "started" state.</p>
     */
    public void start() {
        if (expression == null || expression.isEmpty()) {
            addError("Empty expression");
            return;
        }

        try {
            List<Token> tokens = tokenize(expression.trim());
            this.rpn = infixToReversePolishNotation(tokens);
        } catch (IllegalArgumentException|IllegalStateException e) {
            addError("Malformed expression: " + e.getMessage());
            return;
        }
        super.start();
    }

    /**
     * Returns the current expression string.
     *
     * @return the expression, or null if not set
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Sets the expression to be evaluated.
     *
     * @param expression the boolean expression string
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * Evaluates the parsed expression against the current property context.
     *
     * <p>If the instance is not in started state, returns false.</p>
     *
     * @return true if the expression evaluates to true, false otherwise
     */
    @Override
    public boolean evaluate() {
        if (!isStarted()) {
            return false;
        }
        return evaluateRPN(rpn);
    }

    /**
     * Tokenizes the input expression string into a list of tokens, handling
     * functions, operators, and parentheses.
     *
     * @param expr the expression string to tokenize
     * @return list of tokens
     * @throws IllegalArgumentException if the expression is malformed
     */
    private List<Token> tokenize(String expr) throws IllegalArgumentException, IllegalStateException {
        List<Token> tokens = new ArrayList<>();

        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (c == LEFT_PAREN || c == RIGHT_PAREN) {
                tokens.add(Token.valueOf(c));
                i++;
                continue;
            }

            if (c == NOT_CHAR) {
                tokens.add(new Token(TokenType.NOT));
                i++;
                continue;
            }

            if (c == AMPERSAND_CHAR) {
                i++; // consume '&'
                c = expr.charAt(i);
                if (c == AMPERSAND_CHAR) {
                    tokens.add(new Token(TokenType.AND));
                    i++; // consume '&'
                    continue;
                } else {
                    throw new IllegalArgumentException("Expected '&' after '&'");
                }
            }

            if (c == OR_CHAR) {
                i++; // consume '|'
                c = expr.charAt(i);
                if (c == OR_CHAR) {
                    tokens.add(new Token(TokenType.OR));
                    i++; // consume '|'
                    continue;
                } else {
                    throw new IllegalArgumentException("Expected '|' after '|'");
                }
            }

            // Parse identifiers like isNull, isNotNull, etc.
            if (Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && Character.isLetter(expr.charAt(i))) {
                    sb.append(expr.charAt(i++));
                }
                String functionName = sb.toString();

                // Skip spaces
                i = skipWhitespaces(i);
                checkExpectedCharacter(LEFT_PAREN, i);
                i++; // consume '('

                IntHolder intHolder = new IntHolder(i);
                String param0 = extractQuotedString(intHolder);
                i = intHolder.value;
                // Skip spaces
                i = skipWhitespaces(i);


                if (biFunctionMap.containsKey(functionName)) {
                    checkExpectedCharacter(COMMA, i);
                    i++; // consume ','
                    intHolder.set(i);
                    String param1 = extractQuotedString(intHolder);
                    i = intHolder.get();
                    i = skipWhitespaces(i);
                    tokens.add(new Token(TokenType.BI_FUNCTION, functionName, param0, param1));
                } else {
                    tokens.add(new Token(TokenType.FUNCTION, functionName, param0, null));
                }

                // Skip spaces and expect ')'
                checkExpectedCharacter(RIGHT_PAREN, i);
                i++; // consume ')'

                continue;
            }
        }
        return tokens;
    }

    private String extractQuotedString(IntHolder intHolder) {
        int i = intHolder.get();
        i = skipWhitespaces(i);

        // Expect starting "
        checkExpectedCharacter(QUOTE, i);
        i++; // consume starting "

        int start = i;
        i = findIndexOfClosingQuote(i);
        String param = expression.substring(start, i);
        i++; // consume closing "
        intHolder.set(i);
        return param;
    }

    private int findIndexOfClosingQuote(int i) throws IllegalStateException{
        while (i < expression.length() && expression.charAt(i) != QUOTE) {
            i++;
        }
        if (i >= expression.length()) {
            throw new IllegalStateException("Missing closing quote");
        }
        return i;
    }

    void checkExpectedCharacter(char expectedChar, int i) throws IllegalArgumentException{
        if (i >= expression.length() || expression.charAt(i) != expectedChar) {
            throw new IllegalArgumentException("In [" + expression + "] expecting '" + expectedChar + "' at position " + i);
        }
    }

    private int skipWhitespaces(int i) {
        while (i < expression.length() && Character.isWhitespace(expression.charAt(i))) {
            i++;
        }
        return i;
    }

    /**
     * Converts infix notation tokens to Reverse Polish Notation (RPN) using
     * the Shunting-Yard algorithm.
     *
     * @param tokens list of infix tokens
     * @return list of tokens in RPN
     * @throws IllegalArgumentException if parentheses are mismatched
     */
    private List<Token> infixToReversePolishNotation(List<Token> tokens) {
        List<Token> output = new ArrayList<>();
        Stack<Token> operatorStack = new Stack<>();

        for (Token token : tokens) {
            TokenType tokenType = token.tokenType;
            if (isPredicate(token)) {
                output.add(token);
            } else if (tokenType.isLogicalOperator()) {  // one of NOT, AND, OR types
                while (!operatorStack.isEmpty() && precedence(operatorStack.peek()) >= precedence(token) &&
                        operatorAssociativity(token) == Associativity.LEFT) {
                    output.add(operatorStack.pop());
                }
                operatorStack.push(token);
            } else if (tokenType == TokenType.LEFT_PAREN) {
                operatorStack.push(token);
            } else if (tokenType == TokenType.RIGHT_PAREN) {
                while (!operatorStack.isEmpty() && operatorStack.peek().tokenType != TokenType.LEFT_PAREN) {
                    output.add(operatorStack.pop());
                }
                if (operatorStack.isEmpty())
                    throw new IllegalArgumentException("Mismatched parentheses, expecting '('");
                operatorStack.pop(); // remove '('
            }
        }

        while (!operatorStack.isEmpty()) {
            Token token = operatorStack.pop();
            TokenType tokenType = token.tokenType;
            if (tokenType == TokenType.LEFT_PAREN)
                throw new IllegalArgumentException("Mismatched parentheses");
            output.add(token);
        }

        return output;
    }

    private boolean isPredicate(Token token) {
        return token.tokenType == TokenType.FUNCTION || token.tokenType == TokenType.BI_FUNCTION;
    }

    private int precedence(Token token) {
        TokenType tokenType = token.tokenType;
        switch (tokenType) {
            case NOT:
                return 3;
            case AND:
                return 2;
            case OR:
                return 1;
            default:
                return 0;
        }
    }

    private Associativity operatorAssociativity(Token token) {
        TokenType tokenType = token.tokenType;

        return tokenType == TokenType.NOT ? Associativity.RIGHT : Associativity.LEFT;
    }

    /**
     * Evaluates the Reverse Polish Notation (RPN) expression.
     *
     * @param rpn list of tokens in RPN
     * @return the boolean result of the evaluation
     * @throws IllegalStateException if a function is not defined in the function map
     */
    private boolean evaluateRPN(List<Token> rpn) throws IllegalStateException {
        Stack<Boolean> resultStack = new Stack<>();

        for (Token token : rpn) {
            if (isPredicate(token)) {
                boolean value = evaluateFunctions(token);
                resultStack.push(value);
            } else {
                switch (token.tokenType) {
                    case NOT:
                        boolean a3 = resultStack.pop();
                        resultStack.push(!a3);
                        break;
                    case AND:
                        boolean b2 = resultStack.pop();
                        boolean a2 = resultStack.pop();
                        resultStack.push(a2 && b2);
                        break;

                    case OR:
                        boolean b1 = resultStack.pop();
                        boolean a1 = resultStack.pop();
                        resultStack.push(a1 || b1);
                        break;
                }
            }
        }

        return resultStack.pop();
    }

    // Evaluate a single predicate like isNull("key1")
    private boolean evaluateFunctions(Token token) throws IllegalStateException {
        String functionName = token.functionName;
        String param0 = token.param0;
        String param1 = token.param1;
        Function<String, Boolean> function = functionMap.get(functionName);
        if (function != null) {
            return function.apply(param0);
        }

        BiFunction<String, String, Boolean> biFunction = biFunctionMap.get(functionName);
        if (biFunction != null) {
            return biFunction.apply(param0, param1);
        }

        throw new IllegalStateException("Unknown function: " + token);
    }
}
