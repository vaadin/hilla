package com.vaadin.hilla.parser.core.basic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.parser.core.Parser;
import com.vaadin.hilla.parser.testutils.ResourceLoader;

public class BasicTests {
    private static final List<String> STEPS = new LinkedList<>();

    static {
        STEPS.add("-> Root(ScanResult)");
        STEPS.add("-> Root(ScanResult)/Endpoint(BasicEndpoint)");
        STEPS.add("-> Root(ScanResult)/Endpoint(BasicEndpoint)/Field(foo)");
        STEPS.add("<- Root(ScanResult)/Endpoint(BasicEndpoint)/Field(foo)");
        STEPS.add(
                "-> Root(ScanResult)/Endpoint(BasicEndpoint)/Field(fieldFoo)");
        STEPS.add(
                "<- Root(ScanResult)/Endpoint(BasicEndpoint)/Field(fieldFoo)");
        STEPS.add(
                "-> Root(ScanResult)/Endpoint(BasicEndpoint)/Field(fieldBar)");
        STEPS.add(
                "<- Root(ScanResult)/Endpoint(BasicEndpoint)/Field(fieldBar)");
        STEPS.add("<- Root(ScanResult)/Endpoint(BasicEndpoint)");
        STEPS.add("-> Root(ScanResult)/Entity(Sample)");
        STEPS.add("-> Root(ScanResult)/Entity(Sample)/Method(methodFoo)");
        STEPS.add("<- Root(ScanResult)/Entity(Sample)/Method(methodFoo)");
        STEPS.add("-> Root(ScanResult)/Entity(Sample)/Method(methodBar)");
        STEPS.add("<- Root(ScanResult)/Entity(Sample)/Method(methodBar)");
        STEPS.add("<- Root(ScanResult)/Entity(Sample)");
        STEPS.add("<- Root(ScanResult)");
    }

    private final List<String> classPath;
    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());

    {
        try {
            classPath = List.of(resourceLoader.findTargetDirPath().toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void should_TraverseInConsistentOrder() {
        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .classPath(classPath)
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new BasicPlugin()).execute();

        assertEquals(String.join("\n", STEPS),
                openAPI.getExtensions().get(BasicPlugin.FOOTSTEPS_STORAGE_KEY));
    }

    @Test
    public void should_UpdateNodesAndCollectNames() {
        var openAPI = new Parser().classLoader(getClass().getClassLoader())
                .classPath(classPath)
                .endpointAnnotation(Endpoint.class.getName())
                .addPlugin(new BasicPlugin()).execute();

        assertEquals(String.join(", ",
                List.of("FieldInfoModel foo", "FieldInfoModel fieldFoo",
                        "FieldInfoModel fieldBar", "MethodInfoModel methodFoo",
                        "MethodInfoModel methodBar")),
                openAPI.getExtensions().get(BasicPlugin.STORAGE_KEY));
    }
}
