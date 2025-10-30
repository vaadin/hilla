package com.vaadin.hilla.parser.plugins.backbone.complextype;

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
        private List<Map<String, List<String>>> complexList;
        private Map<String, List<String>> complexMap;

        public List<Map<String, List<String>>> getComplexList() {
            return complexList;
        }

        public Map<String, List<String>> getComplexMap() {
            return complexMap;
        }
    }
}
