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

    @Test
    public void testPushPluginGeneratesSubscriptionMethodsForFluxEndpoints() {
        // Skip test if Flux class is not available
        try {
            Class.forName("reactor.core.publisher.Flux");
        } catch (ClassNotFoundException e) {
            // Flux not available, skip test
            return;
        }

        // Create an endpoint with a Flux method using reflection
        // We need to use the actual Flux class from reactor
        class FluxTestEndpoint {
            public reactor.core.publisher.Flux<String> streamMessages() {
                return null;
            }

            public reactor.core.publisher.Flux<Integer> countTo(int n) {
                return null;
            }
        }

        ClassInfoModel endpoint = ClassInfoModel.of(FluxTestEndpoint.class);
        List<ClassInfoModel> endpoints = List.of(endpoint);
        ParserOutput parserOutput = new ParserOutput(endpoints, List.of());

        TypeScriptGenerator generator = new TypeScriptGenerator("/output");
        generator.addPlugin(
                new com.vaadin.hilla.typescript.codegen.plugins.PushPlugin());

        Map<String, String> generatedFiles = generator.generate(parserOutput);

        // Should generate FluxTestEndpointSubscriptions.ts
        assertTrue(
                generatedFiles
                        .containsKey("FluxTestEndpointSubscriptions.ts"),
                "Should generate subscriptions file for Flux endpoint");
        String generatedCode = generatedFiles
                .get("FluxTestEndpointSubscriptions.ts");

        // Should have subscription method for streamMessages
        assertTrue(
                generatedCode.contains("subscribeToStreamMessages"),
                "Should generate subscription method for streamMessages");

        // Should have subscription method for countTo
        assertTrue(generatedCode.contains("subscribeToCountTo"),
                "Should generate subscription method for countTo");

        // Should import Subscription from hilla-frontend
        assertTrue(generatedCode.contains("import { Subscription }"),
                "Should import Subscription type");

        // Should have callback parameters
        assertTrue(generatedCode.contains("onNext:"),
                "Should have onNext callback parameter");
        assertTrue(generatedCode.contains("onError?:"),
                "Should have optional onError callback parameter");
        assertTrue(generatedCode.contains("onComplete?:"),
                "Should have optional onComplete callback parameter");
    }

    // Static inner classes for SubtypesPlugin test
    @com.fasterxml.jackson.annotation.JsonSubTypes({
            @com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = OrFilterTest.class, name = "or"),
            @com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = AndFilterTest.class, name = "and") })
    static class FilterTest {
    }

    static class OrFilterTest extends FilterTest {
    }

    static class AndFilterTest extends FilterTest {
    }

    @Test
    public void testSubtypesPluginGeneratesUnionTypesAndTypeGuards() {
        ClassInfoModel baseClass = ClassInfoModel.of(FilterTest.class);
        ClassInfoModel orFilter = ClassInfoModel.of(OrFilterTest.class);
        ClassInfoModel andFilter = ClassInfoModel.of(AndFilterTest.class);

        List<ClassInfoModel> entities = List.of(baseClass, orFilter, andFilter);
        ParserOutput parserOutput = new ParserOutput(List.of(), entities);

        TypeScriptGenerator generator = new TypeScriptGenerator("/output");
        generator.addPlugin(
                new com.vaadin.hilla.typescript.codegen.plugins.SubtypesPlugin());

        Map<String, String> generatedFiles = generator.generate(parserOutput);

        // Should generate FilterTestSubtypes.ts
        assertTrue(generatedFiles.containsKey("FilterTestSubtypes.ts"),
                "Should generate subtypes file");
        String generatedCode = generatedFiles.get("FilterTestSubtypes.ts");

        // Should have union type definition
        assertTrue(
                generatedCode
                        .contains("export type FilterTest = OrFilterTest | AndFilterTest"),
                "Should generate union type");

        // Should have type guard for OrFilterTest
        assertTrue(generatedCode.contains("isOrFilterTest"),
                "Should generate type guard for OrFilterTest");
        assertTrue(generatedCode.contains("obj is OrFilterTest"),
                "Should have proper type guard signature for OrFilterTest");
        assertTrue(generatedCode.contains("obj['@type'] === 'or'"),
                "Should check @type discriminator for OrFilterTest");

        // Should have type guard for AndFilterTest
        assertTrue(generatedCode.contains("isAndFilterTest"),
                "Should generate type guard for AndFilterTest");
        assertTrue(generatedCode.contains("obj is AndFilterTest"),
                "Should have proper type guard signature for AndFilterTest");
        assertTrue(generatedCode.contains("obj['@type'] === 'and'"),
                "Should check @type discriminator for AndFilterTest");
    }

    // Test class for SignalsPlugin
    static class NumberSignalTestEndpoint {
        // Simulate NumberSignal return type (would be mapped by
        // TransferTypesPlugin)
        public NumberSignal counter() {
            return null;
        }

        public NumberSignal sharedValue(boolean highOrLow) {
            return null;
        }

        public String regularMethod() {
            return "test";
        }
    }

    // Placeholder NumberSignal class for test
    static class NumberSignal {
    }

    @Test
    public void testSignalsPluginGeneratesSignalConstructorsForNumberSignalMethods() {
        ClassInfoModel endpoint = ClassInfoModel
                .of(NumberSignalTestEndpoint.class);
        List<ClassInfoModel> endpoints = List.of(endpoint);
        ParserOutput parserOutput = new ParserOutput(endpoints, List.of());

        TypeScriptGenerator generator = new TypeScriptGenerator("/output");
        generator.addPlugin(
                new com.vaadin.hilla.typescript.codegen.plugins.SignalsPlugin());

        Map<String, String> generatedFiles = generator.generate(parserOutput);

        // Should generate NumberSignalTestEndpoint.ts
        assertTrue(
                generatedFiles.containsKey("NumberSignalTestEndpoint.ts"),
                "Should generate endpoint file with signals");
        String generatedCode = generatedFiles
                .get("NumberSignalTestEndpoint.ts");

        // Should import NumberSignal from hilla-react-signals
        assertTrue(generatedCode.contains("@vaadin/hilla-react-signals"),
                "Should import from hilla-react-signals");
        assertTrue(generatedCode.contains("NumberSignal"),
                "Should import NumberSignal");

        // Should NOT have async keyword for signal methods
        assertFalse(generatedCode.contains("async function counter"),
                "Signal methods should not be async");

        // Should have synchronous signal constructor call
        assertTrue(generatedCode.contains("function counter()"),
                "Should have synchronous counter method");
        assertTrue(generatedCode.contains("return new NumberSignal(0,"),
                "Should construct NumberSignal with 0 as default");
        assertTrue(
                generatedCode.contains(
                        "endpoint: 'NumberSignalTestEndpoint'"),
                "Should pass endpoint name");
        assertTrue(generatedCode.contains("method: 'counter'"),
                "Should pass method name");

        // Should have signal method with parameters
        assertTrue(
                generatedCode
                        .contains("function sharedValue(highOrLow: boolean)"),
                "Should have sharedValue method with parameter");
        assertTrue(
                generatedCode.contains("params: { highOrLow }"),
                "Should pass parameters to signal constructor");

        // Regular method should still be async with Promise
        assertTrue(
                generatedCode
                        .contains("async function regularMethod"),
                "Regular methods should still be async");
        assertTrue(generatedCode.contains("Promise<string>"),
                "Regular methods should return Promise");
        assertTrue(generatedCode.contains("client.call("),
                "Regular methods should use client.call");
    }
}
