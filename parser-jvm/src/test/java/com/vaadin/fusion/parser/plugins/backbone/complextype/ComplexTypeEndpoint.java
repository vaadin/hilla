package com.vaadin.fusion.parser.plugins.backbone.complextype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        public List<Map<String, List<String>>> getComplexList() {
            return new ArrayList<>();
        }

        public Map<String, List<String>> getComplexMap() {
            return new HashMap<>();
        }
    }
}
