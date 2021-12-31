/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.fusion.generator.endpoints.modelpackage;

import java.util.ArrayList;

import com.vaadin.fusion.Endpoint;

@Endpoint
public class ModelPackageEndpoint {

    /**
     * Get a collection by author name. The generator should not mix this type
     * with the Java's Collection type.
     *
     * @param name
     *            author name
     * @return a collection
     */
    public Account getSameModelPackage(String name) {
        return new Account();
    }

    /**
     * Get a list of user name.
     *
     * @return list of user name
     */
    public java.util.Collection<String> getListOfUserName() {
        return new ArrayList<>();
    }

    public static class Account {
        private String type;
        private String author;
    }

}
