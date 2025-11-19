/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.hilla.parser.plugins.backbone.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class JacksonEndpoint {
    public Sample getSample() {
        return new Sample();
    }

    @JsonIgnoreProperties({ "publicPropWithJsonIgnoreProperties",
            "privatePropWithJsonIgnoreProperties" })
    static final class Sample extends SampleParent {
        public String publicProp;
        @JsonIgnore
        public String publicPropWithJsonIgnore;
        public String publicPropWithJsonIgnoreProperties;
        public IgnoredType publicPropWithJsonIgnoreType;
        public transient String publicTransientProp;
        @JsonProperty("renamedPublicProp0")
        public String renamedPublicProp;
        private String fieldNotExposedAsProperty;
        private String privateProp;
        private String privatePropWithJsonIgnore;
        private String privatePropWithJsonIgnoreProperties;
        private IgnoredType privatePropWithJsonIgnoreType;
        private transient String privateTransientProp;
        private transient String privateTransientPropWithGetter;
        private String propertyFieldOnly;
        private String renamedPrivateProp;

        public String getPrivateProp() {
            return privateProp;
        }

        @JsonIgnore
        public String getPrivatePropWithJsonIgnore() {
            return privatePropWithJsonIgnore;
        }

        public String getPrivatePropWithJsonIgnoreProperties() {
            return privatePropWithJsonIgnoreProperties;
        }

        public String getPrivateTransientPropWithGetter() {
            return privateTransientPropWithGetter;
        }

        public String getPropertyGetterOnly() {
            return "test";
        }

        public void setPropertyGetterOnly(String value) {
        }

        public String getPropertyWithDifferentField() {
            return fieldNotExposedAsProperty;
        }

        public void setPropertyWithDifferentField(String value) {
            fieldNotExposedAsProperty = value;
        }

        @JsonProperty("renamedPrivateProp0")
        public String getRenamedPrivateProp() {
            return renamedPublicProp;
        }

        public void setPropertySetterOnly(String value) {
        }

        @JsonIgnoreType
        static class IgnoredType {
        }
    }

    static class SampleParent {
        public String publicParentProperty;
        private String privateParentProperty;

        public String getPrivateParentProperty() {
            return privateParentProperty;
        }
    }
}
