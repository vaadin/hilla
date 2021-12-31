/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.fusion.generator.typescript;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class TypeParser {
    private TypeParser() {
    }

    static Node parse(String type) {
        Objects.requireNonNull(type);
        List<Token> tokens = Token.process(type);
        Node root = Node.process(tokens);

        if (root == null) {
            throw new ParsingError(String.format("'%s' is not a type", type));
        }

        return root;
    }

    /**
     * A set of methods to interact with nodes during the walk across the AST.
     */
    interface Visitor {
        /**
         * A method invoked when node is entered. When it returns Node, it is
         * consumed, and then walk continues on the updates AST. To avoid
         * changing the AST, just return the `node` as is.
         *
         * @param node
         *            A current node to work with.
         * @param parent
         *            A node that has current node as a child.
         * @return an updated node.
         */
        default Node enter(Node node, Node parent) {
            return node;
        }

        /**
         * A method invoked when the walk over node's children is over. This
         * method isn't expected to return anything.
         *
         * @param node
         *            A current node to work with.
         * @param parent
         *            A node that has current node as a child.
         */
        default void exit(Node node, Node parent) {
        }
    }

    static class Node {
        private String name;
        private List<Node> nested = new ArrayList<>();
        private boolean undefined = false;

        Node(String name) {
            this.name = name;
        }

        @SuppressWarnings({ "squid:S134", "squid:S3776" })
        private static Node process(List<Token> tokens) {
            Deque<Node> unclosedNodes = new ArrayDeque<>();
            Node currentNode = null;
            boolean waitingForSuffix = false;

            for (final Token token : tokens) {
                if (token instanceof NameToken) {
                    if (currentNode != null && waitingForSuffix) {
                        if (Objects.equals(((NameToken) token).getName(),
                                "undefined")) {
                            currentNode.setUndefined(true);
                            waitingForSuffix = false;
                        } else {
                            throw new ParsingError(String.format(
                                    "Type union '%s | %s' is not expected",
                                    currentNode.getName(),
                                    ((NameToken) token).getName()));
                        }
                    } else {
                        currentNode = new Node(((NameToken) token).getName());

                        if (!unclosedNodes.isEmpty()) {
                            unclosedNodes.peek().addNested(currentNode);
                        }
                    }
                } else if (token instanceof PipeToken) {
                    waitingForSuffix = true;
                } else if (((BraceToken) token).isClosing()) {
                    currentNode = unclosedNodes.pop();
                } else {
                    if (currentNode == null) {
                        throw new ParsingError(
                                "Type open brace (<) cannot go before the type name");
                    }
                    unclosedNodes.push(currentNode);
                }
            }

            return currentNode;
        }

        /**
         * Returns a TypeScript representation of the type.
         *
         * @return a string representation
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(name);

            if (!nested.isEmpty()) {
                builder.append('<');
                builder.append(nested.stream().map(Node::toString)
                        .collect(Collectors.joining(", ")));
                builder.append('>');
            }

            if (undefined) {
                builder.append(" | undefined");
            }

            return builder.toString();
        }

        void addNested(final Node node) {
            nested.add(node);
        }

        Node copy() {
            Node copy = new Node(name);
            copy.setNested(nested.stream().map(Node::copy)
                    .collect(Collectors.toList()));
            copy.setUndefined(undefined);

            return copy;
        }

        String getName() {
            return name;
        }

        void setName(final String name) {
            this.name = name;
        }

        List<Node> getNested() {
            return nested;
        }

        void setNested(List<Node> nested) {
            this.nested = nested;
        }

        boolean hasNested() {
            return !nested.isEmpty();
        }

        boolean isUndefined() {
            return undefined;
        }

        void setUndefined(final boolean undefined) {
            this.undefined = undefined;
        }

        Traverse traverse() {
            return new Traverse(this);
        }
    }

    static class ParsingError extends Error {
        ParsingError(String message) {
            super(message);
        }
    }

    static class Traverse {
        private final Node root;
        private final List<Visitor> visitors = new ArrayList<>();

        Traverse(Node root) {
            this.root = root;
        }

        Node finish() {
            return applyVisitors(root, null);
        }

        Traverse visit(Visitor visitor) {
            visitors.add(visitor);

            return this;
        }

        private Node applyVisitors(Node node, Node parent) {
            Node tmp = node;

            for (Visitor visitor : visitors) {
                tmp = visitor.enter(tmp, parent);

                if (tmp == null) {
                    return null;
                }
            }

            visit(tmp);

            for (Visitor visitor : visitors) {
                visitor.exit(tmp, parent);
            }

            return tmp;
        }

        private void visit(Node node) {
            if (node.hasNested()) {
                node.setNested(node.nested.stream()
                        .map(n -> applyVisitors(n, node))
                        .filter(Objects::nonNull).collect(Collectors.toList()));
            }
        }
    }

    private static class BraceToken extends Token {
        private final boolean closing;

        BraceToken(boolean closing) {
            this.closing = closing;
        }

        boolean isClosing() {
            return closing;
        }
    }

    private static class NameToken extends Token {
        private final StringBuilder name = new StringBuilder();

        void append(char ch) {
            name.append(ch);
        }

        String getName() {
            return name.toString();
        }
    }

    private static class PipeToken extends Token {
    }

    private abstract static class Token {
        private static final char CLOSE_BRACE = '>';
        private static final char COMMA = ',';
        private static final char OPEN_BRACE = '<';
        private static final char PIPE = '|';
        private static final char SPACE = ' ';

        private Token() {
        }

        static List<Token> process(CharSequence sequence) {
            List<Token> tokens = new ArrayList<>();
            NameToken token = null;

            for (int i = 0; i < sequence.length(); i++) {
                char ch = sequence.charAt(i);

                switch (ch) {
                case SPACE:
                    break;
                case PIPE:
                    tokens.add(new PipeToken());
                    token = null;
                    break;
                case OPEN_BRACE:
                    tokens.add(new BraceToken(false));
                    token = null;
                    break;
                case CLOSE_BRACE:
                    tokens.add(new BraceToken(true));
                    token = null;
                    break;
                case COMMA:
                    token = null;
                    break;
                default:
                    if (token == null) {
                        token = new NameToken();
                        tokens.add(token);
                    }
                    token.append(ch);
                    break;
                }

            }

            return tokens;
        }
    }
}
