package dev.hilla.parser.plugins.backbone.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Endpoint
public class JSONAnnotationsEndpoint {
    public AnnotatedEntity getEntity() {
        return new AnnotatedEntity();
    }

    @JsonIgnoreProperties({ "ignoredA", "ignoredB" })
    private static class AnnotatedEntity {
        @JsonIgnore
        public String ignored;
        public String ignoredA;
        public String ignoredB;
        public JsonIgnoredType ignoredType;
        public String normal;
    }
}
