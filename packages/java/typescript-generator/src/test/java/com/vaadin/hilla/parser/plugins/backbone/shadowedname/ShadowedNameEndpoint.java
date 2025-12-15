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
package com.vaadin.hilla.parser.plugins.backbone.shadowedname;

import java.util.ArrayList;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class ShadowedNameEndpoint {
    /**
     * Get a list of usernames.
     *
     * @return list of usernames
     */
    public java.util.Collection<String> getJavaCollection() {
        return new ArrayList<>();
    }

    /**
     * Get a collection by author name. The generator should not mix this type
     * with the Java's Collection type.
     *
     * @param name
     *            author name
     * @return a collection
     */
    public Collection getNestedUserDefinedCollection(String name) {
        return new Collection();
    }

    public com.vaadin.hilla.parser.plugins.backbone.shadowedname.subpackage.Collection<String> getSeparateUserDefinedCollection() {
        return new com.vaadin.hilla.parser.plugins.backbone.shadowedname.subpackage.Collection<>();
    }

    public static class Collection {
        private String author;
        private String collectionName;
        private String type;

        public String getAuthor() {
            return author;
        }

        public String getCollectionName() {
            return collectionName;
        }

        public String getType() {
            return type;
        }
    }

}
