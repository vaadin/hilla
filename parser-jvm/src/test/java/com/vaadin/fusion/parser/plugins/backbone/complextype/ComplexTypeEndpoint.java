package com.vaadin.fusion.parser.plugins.backbone.complextype;

import java.util.List;
import java.util.Map;

import com.vaadin.fusion.parser.testutils.Endpoint;

@Endpoint
public class ComplexTypeEndpoint {
    public ComplexTypeModel getComplexTypeModel(
            List<Map<String, String>> data) {
        return new ComplexTypeModel();
    }

    private ComplexTypeModel getPrivateComplexTypeModel() {
        return new ComplexTypeModel();
    }

    public static class ComplexTypeModel {
        List<Map<String, List<String>>> complexList;
        Map<String, List<String>> complexMap;
    }
}
