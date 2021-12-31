package com.vaadin.fusion.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

final class IndentationUtils {
    private static final Pattern emptyOrWhiteSpaceLinePattern = Pattern
            .compile("^\\s*$");

    static String unifyIndentation(String code, int indentation)
            throws IndentationSyntaxException {
        Token[] tokens = Token.process(code);
        Node ast = Node.process(tokens);
        StringBuilder indentedCodeBuilder = new StringBuilder();

        Node currentNode = ast;
        // Our initial `ast` container is virtual, so nesting level is not
        // applicable.
        int level = -1;

        while (currentNode != null) {
            if (currentNode instanceof TextNode) {
                String text = ((TextNode) currentNode).getText();

                // Workaround for the whitespace issue
                // https://github.com/vaadin/flow/issues/10843
                if (text.startsWith("*") || text.startsWith("//")) {
                    // Applies only for comments
                    text = text.replaceAll("\\s+", " ");
                }

                if (!text.matches("[,:|;].*")) {
                    indentedCodeBuilder.append('\n')
                            .append(repeat(" ", level * indentation));
                }

                indentedCodeBuilder.append(text);

                currentNode.setVisited(true);
                currentNode = currentNode.getParent();
                level -= 1;
            } else {
                if (((ContainerNode) currentNode).hasUnvisitedChildren()) {
                    if (currentNode instanceof ContainerNodeWithBraces
                            && ((ContainerNode) currentNode).isFirstVisit()) {
                        indentedCodeBuilder.append(' ')
                                .append(((ContainerNodeWithBraces) currentNode)
                                        .getBrace(true));
                    }

                    currentNode = ((ContainerNode) currentNode)
                            .getNextUnvisited();
                    level += 1;
                } else {
                    if (currentNode instanceof ContainerNodeWithBraces) {
                        // If there is nothing in parenthesis or braces.
                        if (((ContainerNode) currentNode).isEmpty()) {
                            indentedCodeBuilder.append(
                                    ((ContainerNodeWithBraces) currentNode)
                                            .getBrace(true))
                                    .append(((ContainerNodeWithBraces) currentNode)
                                            .getBrace(false));
                        } else {
                            indentedCodeBuilder.append('\n')
                                    .append(repeat(" ", level * indentation))
                                    .append(((ContainerNodeWithBraces) currentNode)
                                            .getBrace(false));
                        }
                    }

                    currentNode.setVisited(true);
                    currentNode = currentNode.getParent();
                    level -= 1;
                }
            }
        }

        return indentedCodeBuilder.toString().trim();
    }

    static String repeat(String part, int times) {
        return String.join("", Collections.nCopies(times, part));
    }

    abstract static class Token {

        static Token[] process(String str) {
            TextToken currentTextToken = null;
            List<Token> tokens = new ArrayList<>();

            for (int i = 0; i < str.length(); i++) {
                char symbol = str.charAt(i);

                if (symbol == '(' || symbol == '{' || symbol == '[') {
                    currentTextToken = null;
                    tokens.add(new BraceToken(true,
                            BraceToken.BraceType.getBraceType(symbol)));
                } else if (symbol == ')' || symbol == '}' || symbol == ']') {
                    currentTextToken = null;
                    tokens.add(new BraceToken(false,
                            BraceToken.BraceType.getBraceType(symbol)));
                } else {
                    if (currentTextToken == null) {
                        currentTextToken = new TextToken();
                        tokens.add(currentTextToken);
                    }

                    if (symbol == '\n') {
                        currentTextToken = null;
                    } else {
                        currentTextToken.addSymbol(symbol);
                    }
                }
            }

            return tokens.toArray(new Token[0]);
        }
    }

    static class BraceToken extends Token {

        private final boolean open;
        private final BraceType type;

        BraceToken(boolean open, BraceType type) {
            this.open = open;
            this.type = type;
        }

        boolean isOpen() {
            return open;
        }

        BraceType getType() {
            return type;
        }

        enum BraceType {

            CURVE_BRACE, PARENTHESIS, ROUND_BRACE;

            static BraceType getBraceType(char symbol) {
                switch (symbol) {
                case '{':
                case '}':
                    return CURVE_BRACE;
                case '[':
                case ']':
                    return ROUND_BRACE;
                case '(':
                case ')':
                    return PARENTHESIS;
                default:
                    throw new IllegalArgumentException(String.format(
                            "Symbol %c is not allowed for bracket", symbol));
                }
            }

            static char getBraceByType(BraceType type, boolean open) {
                switch (type) {
                case CURVE_BRACE:
                    return open ? '{' : '}';
                case ROUND_BRACE:
                    return open ? '[' : ']';
                case PARENTHESIS:
                    return open ? '(' : ')';
                default:
                    throw new IllegalArgumentException("Unexpected argument");
                }
            }

        }

    }

    static class TextToken extends Token {

        private final StringBuilder content = new StringBuilder();

        void addSymbol(char symbol) {
            content.append(symbol);
        }

        String getContent() {
            return content.toString().trim();
        }

        boolean isEmpty() {
            return emptyOrWhiteSpaceLinePattern.matcher(content).find();
        }

    }

    abstract static class Node {

        private final ContainerNode parent;
        private boolean visited = false;

        Node(ContainerNode parent) {
            this.parent = parent;
        }

        static Node process(Token[] tokens) throws IndentationSyntaxException {
            Stack<ContainerNode> unclosedNodes = new Stack<>();
            // Create root node
            unclosedNodes.push(new ContainerNode(null));

            for (Token token : tokens) {
                ContainerNode container = unclosedNodes.peek();

                if (token instanceof BraceToken) {
                    if (((BraceToken) token).isOpen()) {
                        ContainerNode node = new ContainerNodeWithBraces(
                                container, ((BraceToken) token).getType());
                        container.addChild(node);
                        unclosedNodes.push(node);
                    } else {
                        // If only root node is left
                        if (unclosedNodes.size() == 1) {
                            throw new IndentationSyntaxException(
                                    "Unexpected closing brace is found");
                        }

                        unclosedNodes.pop();
                    }
                } else if (!((TextToken) token).isEmpty()) {
                    container.addChild(new TextNode(container,
                            ((TextToken) token).getContent()));
                }
            }

            return unclosedNodes.pop();
        }

        ContainerNode getParent() {
            return parent;
        }

        boolean isVisited() {
            return visited;
        }

        void setVisited(boolean visited) {
            this.visited = visited;
        }

    }

    static class ContainerNode extends Node {

        private final List<Node> children = new ArrayList<>();

        ContainerNode(ContainerNode parent) {
            super(parent);
        }

        void addChild(Node node) {
            children.add(node);
        }

        boolean isFirstVisit() {
            return children.stream().noneMatch(Node::isVisited);
        }

        boolean isEmpty() {
            return children.isEmpty();
        }

        boolean hasUnvisitedChildren() {
            return children.stream().anyMatch(node -> !node.isVisited());
        }

        Node getNextUnvisited() {
            return children.stream().filter(node -> !node.isVisited())
                    .findFirst().orElse(null);
        }

    }

    static class ContainerNodeWithBraces extends ContainerNode {

        private final BraceToken.BraceType braceType;

        ContainerNodeWithBraces(ContainerNode parent,
                BraceToken.BraceType braceType) {
            super(parent);
            this.braceType = braceType;
        }

        char getBrace(boolean open) {
            return BraceToken.BraceType.getBraceByType(braceType, open);
        }

    }

    static class TextNode extends Node {

        private final String text;

        TextNode(ContainerNode parent, String text) {
            super(parent);
            this.text = text;
        }

        String getText() {
            return text;
        }

    }

    static class IndentationSyntaxException extends Exception {

        IndentationSyntaxException(String message) {
            super(message);
        }

    }
}
