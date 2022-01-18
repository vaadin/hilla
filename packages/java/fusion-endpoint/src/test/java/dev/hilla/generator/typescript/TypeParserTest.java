package dev.hilla.generator.typescript;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TypeParserTest {
    @Test
    public void should_AllowNodeReplacing() {
        TypeParser.Visitor visitor = new NodeReplacingTestVisitor();
        TypeParser.Node node = TypeParser.parse(
                "Record<ReadonlyArray<string>, Record<string, ReadonlyArray<string>>>")
                .traverse().visit(visitor).finish();

        assertEquals(
                "Readonly<Record<ReadonlyArray<string>, Readonly<Record<string, ReadonlyArray<string>>>>>",
                node.toString());
    }

    @Test
    public void should_AllowTypeRenaming() {
        TypeParser.Visitor visitor = new NodeRenamingTestVisitor();
        TypeParser.Node node = TypeParser.parse(
                "Map<ReadonlyArray<string>, Map<string, ReadonlyArray<string>>>")
                .traverse().visit(visitor).finish();

        assertEquals(
                "Record<ReadonlyArray<string>, Record<string, ReadonlyArray<string>>>",
                node.toString());
    }

    @Test
    public void should_AllowsTypeNullability() {
        String type = "Readonly<Record<string, Readonly<Record<string, ReadonlyArray<MyEntity | undefined> | undefined>> | undefined>>";
        TypeParser.Node node = TypeParser.parse(type);
        assertEquals(type, node.toString());
    }

    @Test
    public void should_CorrectlyEnterAndExitNode() {
        NodeEnterExitTestVisitor visitor = new NodeEnterExitTestVisitor();

        TypeParser.parse(
                "Record<ReadonlyArray<string>, Record<string, ReadonlyArray<string>>>")
                .traverse().visit(visitor).finish();

        assertEquals(
                "[Record, [ReadonlyArray, [string], Record, [string, ReadonlyArray, [string]]]]",
                visitor.getResult());
    }

    @Test
    public void should_ParseComplexType() {
        String type = "Map<ReadonlyArray<string>, Map<string, ReadonlyArray<string>>>";
        TypeParser.Node node = TypeParser.parse(type);

        assertEquals(type, node.toString());
    }

    @Test
    public void should_ParseSimpleType() {
        String type = "string";
        TypeParser.Node node = TypeParser.parse(type);

        assertEquals(type, node.toString());
    }

    static class NodeEnterExitTestVisitor implements TypeParser.Visitor {
        private final StringBuilder builder = new StringBuilder();

        @Override
        public TypeParser.Node enter(TypeParser.Node node,
                TypeParser.Node parent) {
            if (parent == null || parent.getNested().indexOf(node) == 0) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }

                builder.append("[");
            }

            builder.append(node.getName());

            return node;
        }

        @Override
        public void exit(TypeParser.Node node, TypeParser.Node parent) {
            if (parent == null || parent.getNested()
                    .indexOf(node) == parent.getNested().size() - 1) {
                builder.append("]");
            } else {
                builder.append(", ");
            }
        }

        public String getResult() {
            return builder.toString();
        }
    }

    static class NodeRenamingTestVisitor implements TypeParser.Visitor {
        @Override
        public TypeParser.Node enter(TypeParser.Node node,
                TypeParser.Node parent) {
            if ("Map".equals(node.getName())) {
                node.setName("Record");
                return node;
            }

            return node;
        }
    }

    static class NodeReplacingTestVisitor implements TypeParser.Visitor {
        @Override
        public TypeParser.Node enter(TypeParser.Node node,
                TypeParser.Node parent) {
            if ("Record".equals(node.getName()) && (parent == null
                    || !"Readonly".equals(parent.getName()))) {
                TypeParser.Node w = new TypeParser.Node("Readonly");
                w.addNested(node);

                return w;
            }

            return node;
        }
    }
}
