/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.fusion.generator.endpoints.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.vaadin.fusion.generator.endpoints.model.subpackage.ModelFromDifferentPackage;
import com.vaadin.fusion.Endpoint;

@Endpoint
public class ModelEndpoint {

    /**
     * This field is irrelevant and the type shouldn't be generated.
     */
    private ShouldNotBeGenerated thisFieldShouldNotBeGenerated;

    /**
     * Get account by username.
     *
     * @param userName
     *            username of the account
     * @return the account with given userName
     */
    public Account getAccountByUserName(String userName) {
        return new Account();
    }

    public Map<String, Group> getMapGroups() {
        return Collections.emptyMap();
    }

    public Account getAccountByGroups(List<Group> groups) {
        return new Account();
    }

    public Account[] getArrayOfAccount() {
        return null;
    }

    /**
     * The import path of this model should be correct.
     */
    public ModelFromDifferentPackage getModelFromDifferentPackage() {
        return new ModelFromDifferentPackage();
    }

    public static class Account {
        /**
         * Javadoc for username.
         */
        String username;
        // make sure that recursive type works
        Account children;
        // cross reference
        /**
         * Multiple line description should work.This is very very very very
         * very very very very long.
         */
        List<Group> groups;
        ModelFromDifferentPackage modelFromDifferentPackage;
        @JsonIgnore
        String passwordShouldBeIgnore;
        transient Object nonSerializableField;
        static String staticNotEndupInJson;
    }

    public static class Group {
        String groupId;
        String groupName;
        Account creator;
    }
}
