package com.vaadin.hilla.typescript.codegen;

import com.vaadin.hilla.typescript.codegen.plugins.BarrelPlugin;
import com.vaadin.hilla.typescript.codegen.plugins.ClientPlugin;
import com.vaadin.hilla.typescript.codegen.plugins.ModelPlugin;
import com.vaadin.hilla.typescript.parser.models.ClassInfoModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TypeScriptGeneratorTest {

    @Test
    public void testBasicGeneration(@TempDir Path tempDir) {
        // Create a simple ParserOutput with mock data
        // For now, use empty lists since we need real ClassInfoModel instances
        // which require actual Java classes to parse
        List<ClassInfoModel> endpoints = new ArrayList<>();
        List<ClassInfoModel> entities = new ArrayList<>();

        ParserOutput parserOutput = new ParserOutput(endpoints, entities);

        // Create generator with plugins
        TypeScriptGenerator generator = new TypeScriptGenerator(
                tempDir.toString());
        generator.addPlugin(new ModelPlugin());

        // Generate code
        Map<String, String> generatedFiles = generator.generate(parserOutput);

        // With empty input, no files should be generated
        assertTrue(generatedFiles.isEmpty());
    }

    @Test
    public void testTypeScriptWriter() {
        TypeScriptWriter writer = new TypeScriptWriter();

        writer.addNamedImport(List.of("Component"), "@angular/core");
        writer.addTypeImport(List.of("User"), "./models");
        writer.appendLine("export class MyClass {");
        writer.appendLine("}");

        String result = writer.build();

        assertTrue(result.contains("import { Component } from '@angular/core';"));
        assertTrue(result.contains("import type { User } from './models';"));
        assertTrue(result.contains("export class MyClass"));
    }

    @Test
    public void testIndentationHelper() {
        String code = "function foo() {\n  return 42;\n}";
        String indented = IndentationHelper.indent(code, 1);

        assertTrue(indented.startsWith("  function"));
        assertTrue(indented.contains("    return 42;"));
    }

    @Test
    public void testGenerationContext() {
        GenerationContext context = new GenerationContext("/output");

        assertEquals("/output", context.getOutputDirectory());

        context.setAttribute("test", "value");
        assertTrue(context.hasAttribute("test"));
        assertEquals("value", context.getAttribute("test").orElse(null));

        context.setAttribute("number", 42);
        assertEquals(42, context.getAttribute("number", Integer.class).orElse(null));

        context.removeAttribute("test");
        assertFalse(context.hasAttribute("test"));
    }

    @Test
    public void testPluginOrdering() {
        TypeScriptGenerator generator = new TypeScriptGenerator("/output");

        ModelPlugin modelPlugin = new ModelPlugin();
        ClientPlugin clientPlugin = new ClientPlugin();
        BarrelPlugin barrelPlugin = new BarrelPlugin();

        generator.addPlugin(barrelPlugin);
        generator.addPlugin(modelPlugin);
        generator.addPlugin(clientPlugin);

        List<TypeScriptGeneratorPlugin> plugins = generator.getPlugins();
        assertEquals(3, plugins.size());

        // Plugins should be ordered by their order value when executed
        // ModelPlugin: order 10
        // ClientPlugin: order 20
        // BarrelPlugin: order 50
    }

    @Test
    public void testParserOutput() {
        List<ClassInfoModel> endpoints = new ArrayList<>();
        List<ClassInfoModel> entities = new ArrayList<>();

        ParserOutput output = new ParserOutput(endpoints, entities);

        assertEquals(0, output.getEndpoints().size());
        assertEquals(0, output.getEntities().size());
        assertEquals(0, output.getAllClasses().size());
    }

    @Test
    public void testClientPluginGeneratesValidTypeScriptForMethodsWithNoParameters() {
        // Create a simple endpoint with a method that has no parameters
        class TestEndpoint {
            public String getEntity() {
                return "test";
            }
        }

        ClassInfoModel endpoint = ClassInfoModel.of(TestEndpoint.class);
        List<ClassInfoModel> endpoints = List.of(endpoint);
        ParserOutput parserOutput = new ParserOutput(endpoints, List.of());

        TypeScriptGenerator generator = new TypeScriptGenerator("/output");
        generator.addPlugin(new ClientPlugin());

        Map<String, String> generatedFiles = generator.generate(parserOutput);

        // Should generate TestEndpoint.ts
        assertTrue(generatedFiles.containsKey("TestEndpoint.ts"));
        String generatedCode = generatedFiles.get("TestEndpoint.ts");

        // Should not have a comma before init parameter
        assertFalse(generatedCode.contains("(, init?:"),
                "Generated TypeScript should not have comma before init parameter");

        // Should have valid syntax for no-parameter method
        assertTrue(
                generatedCode.contains(
                        "getEntity(init?: EndpointRequestInit)"),
                "Generated TypeScript should have valid syntax for no-parameter method");

        // Should have empty object for parameters in call
        assertTrue(generatedCode.contains("'getEntity', {}, init"),
                "Generated TypeScript should pass empty object for parameters");
    }
}
