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
package com.vaadin.hilla.parser.plugins.backbone.accessororder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class AccessorOrderEndpoint {
    public Ordered get() {
        return new Ordered();
    }

    public PartlyOrdered getPartlyOrdered() {
        return new PartlyOrdered();
    }

    public Range getRange() {
        return new Range(2, 1);
    }

    /**
     * Properties without a field, declared out of the order of their names: the
     * JVM lists methods in no particular order, so they are written in the
     * order of their names, after the one backed by a field.
     */
    public static class Ordered {
        public String field;

        public String getZeta() {
            return "";
        }

        public String getAlpha() {
            return "";
        }

        public String getMiddle() {
            return "";
        }
    }

    /**
     * An order which names only some of the properties: those keep their place,
     * and the ones it leaves out without a field come after them in the order
     * of their names. A name the order repeats is taken once, as Jackson takes
     * it.
     */
    @JsonPropertyOrder({ "zeta", "field", "zeta" })
    public static class PartlyOrdered {
        public String field;

        public String getZeta() {
            return "";
        }

        public String getMiddle() {
            return "";
        }

        public String getAlpha() {
            return "";
        }
    }

    /**
     * Properties a constructor takes and no field keeps, which stay in the
     * order of the constructor rather than of their names.
     */
    public static class Range {
        private final int[] bounds;

        @JsonCreator
        public Range(@JsonProperty("upper") int upper,
                @JsonProperty("lower") int lower) {
            this.bounds = new int[] { lower, upper };
        }

        public int getUpper() {
            return bounds[1];
        }

        public int getLower() {
            return bounds[0];
        }
    }
}
