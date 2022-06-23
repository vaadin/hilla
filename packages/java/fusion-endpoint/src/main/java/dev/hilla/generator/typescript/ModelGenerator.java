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
package dev.hilla.generator.typescript;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jknack.handlebars.Helper;
import io.swagger.codegen.v3.CodegenProperty;

import dev.hilla.generator.GeneratorUtils;
import dev.hilla.generator.MainGenerator;
import dev.hilla.generator.OpenAPIObjectGenerator;

class ModelGenerator {
    private ModelGenerator() {
    }

    static Helper<CodegenProperty> getModelArgumentsHelper() {
        return (prop, options) -> getModelArguments(prop, options.param(0));
    }

    static Helper<CodegenProperty> getModelFullTypeHelper() {
        return (prop, options) -> getModelFullType(CodeGeneratorUtils
                .getSimpleNameFromImports(prop.datatype, options.param(0)));
    }

    private static List<String> getConstrainsArguments(
            CodegenProperty property) {
        List<String> annotations = (List) property.getVendorExtensions()
                .get(OpenAPIObjectGenerator.CONSTRAINT_ANNOTATIONS);
        if (annotations != null) {
            return annotations.stream()
                    .map(annotation -> String.format("new " + "%s", annotation))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static String getModelArguments(CodegenProperty property,
            List<Map<String, String>> imports) {
        String dataType = property.datatype;
        boolean optional = !property.required;
        String simpleName = CodeGeneratorUtils
                .getSimpleNameFromImports(dataType, imports);
        return getModelVariableArguments(simpleName, optional,
                getConstrainsArguments(property));
    }

    private static String getModelFullType(String name) {
        return TypeParser.parse(name).traverse()
                .visit(new ModelTypeModelVisitor()).finish().toString();
    }

    private static String getModelVariableArguments(String name,
            boolean optional, List<String> constrainArguments) {
        ModelArgumentsModelVisitor visitor = new ModelArgumentsModelVisitor(
                optional, constrainArguments);

        TypeParser.parse(name).traverse().visit(visitor).finish();

        return visitor.getResult();
    }

    private static class ModelArgumentsModelVisitor extends ModelVisitor {
        private final StringBuilder builder = new StringBuilder();
        private final List<String> constrainArguments;
        private final boolean isRootOptional;

        ModelArgumentsModelVisitor(boolean isRootOptional,
                List<String> constrainArguments) {
            this.isRootOptional = isRootOptional;
            this.constrainArguments = constrainArguments;
        }

        @Override
        public TypeParser.Node enter(TypeParser.Node node,
                TypeParser.Node parent) {
            if (parent == null || isArray(parent)) {
                if (parent != null) {
                    builder.append(", ");
                }

                builder.append(prepareModelName(node));
                builder.append(", [");
                builder.append(
                        parent == null ? isRootOptional : node.isUndefined());

                return node;
            }

            // If array chain ended, let's just remove all children nodes
            return null;
        }

        @Override
        public void exit(TypeParser.Node node, TypeParser.Node parent) {
            if (parent == null && !constrainArguments.isEmpty()) {
                builder.append(", ");
                builder.append(String.join(", ", constrainArguments));
            }

            if (parent == null || isArray(parent)) {
                builder.append("]");
            }
        }

        String getResult() {
            return builder.toString();
        }

        private String prepareModelName(TypeParser.Node node) {
            if (isArray(node)) {
                return ARRAY_MODEL_NAME;
            } else if (isObject(node)) {
                return OBJECT_MODEL_NAME;
            } else if (isPrimitive(node)) {
                return getPrimitiveModelName(node);
            }

            return getOtherModelName(node);
        }
    }

    private static class ModelTypeModelVisitor extends ModelVisitor {
        private final Set<TypeParser.Node> visitedNodes = new HashSet<>();

        @Override
        public TypeParser.Node enter(TypeParser.Node node,
                TypeParser.Node parent) {
            node.setUndefined(false);

            if (isArray(node)) {
                // Array<Type, Type>
                TypeParser.Node newNode = new TypeParser.Node(ARRAY_MODEL_NAME);

                TypeParser.Node arrayItem = node.getNested().get(0);

                if (isPrimitive(arrayItem)) {
                    newNode.addNested(arrayItem);
                    visitedNodes.add(arrayItem);
                } else {
                    newNode.addNested(getModelValueType(arrayItem));
                }

                newNode.addNested(arrayItem.copy());

                visitedNodes.add(newNode);

                return newNode;
            } else if (isObject(node)
                    && (parent == null || !isObjectModel(parent))) {
                // Record<Type, Type>
                TypeParser.Node wrapper = new TypeParser.Node(
                        OBJECT_MODEL_NAME);
                wrapper.addNested(node);

                // Record<Type, Type>
                TypeParser.Node key = node.getNested().get(0);
                TypeParser.Node value = node.getNested().get(1);

                if (isPrimitive(value)) {
                    node.getNested().set(1, value);
                    visitedNodes.add(value);
                } else {
                    node.getNested().set(1, getModelValueType(value));
                }

                visitedNodes.add(wrapper);
                visitedNodes.add(node);
                visitedNodes.add(key);

                return wrapper;
            } else if (isPrimitive(node) && !visitedNodes.contains(node)) {
                node.setName(getPrimitiveModelName(node));
                visitedNodes.add(node);
                return node;
            }

            if (!visitedNodes.contains(node)) {
                node.setName(getOtherModelName(node));
            }

            return node;
        }

        private TypeParser.Node getModelValueType(TypeParser.Node node) {
            TypeParser.Node modelValueNode = new TypeParser.Node(
                    MainGenerator.MODEL + "Value");
            modelValueNode.addNested(node);

            visitedNodes.add(modelValueNode);

            return modelValueNode;
        }
    }

    private abstract static class ModelVisitor implements TypeParser.Visitor {
        protected static final String ARRAY_MODEL_NAME = "Array"
                + MainGenerator.MODEL;
        protected static final String OBJECT_MODEL_NAME = "Object"
                + MainGenerator.MODEL;
        private static final Set<String> PRIMITIVES = Collections
                .unmodifiableSet(new HashSet<>(
                        Arrays.asList("string", "number", "boolean")));

        protected String getOtherModelName(TypeParser.Node node) {
            return node.getName() + MainGenerator.MODEL;
        }

        protected String getPrimitiveModelName(TypeParser.Node node) {
            return GeneratorUtils.capitalize(node.getName())
                    + MainGenerator.MODEL;
        }

        protected boolean isArray(TypeParser.Node node) {
            return Objects.equals(node.getName(), "Array");
        }

        protected boolean isObject(TypeParser.Node node) {
            return Objects.equals(node.getName(), "Record");
        }

        protected boolean isObjectModel(TypeParser.Node node) {
            return Objects.equals(node.getName(), OBJECT_MODEL_NAME);
        }

        protected boolean isPrimitive(TypeParser.Node node) {
            return PRIMITIVES.contains(node.getName());
        }
    }
}
