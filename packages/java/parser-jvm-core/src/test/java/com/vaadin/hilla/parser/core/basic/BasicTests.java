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
        STEPS.add("-> Root(List)");
        STEPS.add("-> Root(List)/Endpoint(BasicEndpoint)");
        STEPS.add("-> Root(List)/Endpoint(BasicEndpoint)/Field(foo)");
        STEPS.add("<- Root(List)/Endpoint(BasicEndpoint)/Field(foo)");
        STEPS.add("-> Root(List)/Endpoint(BasicEndpoint)/Field(fieldFoo)");
        STEPS.add("<- Root(List)/Endpoint(BasicEndpoint)/Field(fieldFoo)");
        STEPS.add("-> Root(List)/Endpoint(BasicEndpoint)/Field(fieldBar)");
        STEPS.add("<- Root(List)/Endpoint(BasicEndpoint)/Field(fieldBar)");
        STEPS.add("<- Root(List)/Endpoint(BasicEndpoint)");
        STEPS.add("-> Root(List)/Entity(Sample)");
        STEPS.add("-> Root(List)/Entity(Sample)/Method(methodFoo)");
        STEPS.add("<- Root(List)/Entity(Sample)/Method(methodFoo)");
        STEPS.add("-> Root(List)/Entity(Sample)/Method(methodBar)");
        STEPS.add("<- Root(List)/Entity(Sample)/Method(methodBar)");
        STEPS.add("<- Root(List)/Entity(Sample)");
        STEPS.add("<- Root(List)");
    }

    private final List<String> classPath;
    private final ResourceLoader resourceLoader = new ResourceLoader(
            getClass());
    private final List<Class<?>> endpoints = List.of(BasicEndpoint.class);

    {
        try {
            classPath = List.of(resourceLoader.findTargetDirPath().toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void should_TraverseInConsistentOrder() {
        var openAPI = new Parser().classPath(classPath)
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BasicPlugin()).execute(endpoints);

        // The list of endpoints seems to be serialized as "List12". The
        // replacement tries to accommodate for similar representations.
        assertEquals(String.join("\n", STEPS),
                ((String) openAPI.getExtensions()
                        .get(BasicPlugin.FOOTSTEPS_STORAGE_KEY))
                        .replaceAll("List\\w*", "List"));
    }

    @Test
    public void should_UpdateNodesAndCollectNames() {
        var openAPI = new Parser().classPath(classPath)
                .endpointAnnotations(List.of(Endpoint.class))
                .endpointExposedAnnotations(List.of(EndpointExposed.class))
                .addPlugin(new BasicPlugin()).execute(endpoints);

        assertEquals(String.join(", ",
                List.of("FieldInfoModel foo", "FieldInfoModel fieldFoo",
                        "FieldInfoModel fieldBar", "MethodInfoModel methodFoo",
                        "MethodInfoModel methodBar")),
                openAPI.getExtensions().get(BasicPlugin.STORAGE_KEY));
    }
}
